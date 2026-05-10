package com.commonplant.garden.s3.service;

import com.commonplant.garden.common.config.S3Properties;
import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.common.util.IdUtil;
import com.commonplant.garden.s3.dto.S3Response;
import com.commonplant.garden.s3.entity.Image;
import com.commonplant.garden.s3.entity.ImageRepository;
import com.commonplant.garden.s3.exception.S3ErrorCode;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.entity.UserRepository;
import com.commonplant.garden.user.enums.UserStatus;
import com.commonplant.garden.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class S3ServiceImpl implements S3Service {

    private static final String IMAGE_KEY_PREFIX = "images";
    private static final int MAX_EXTENSION_LENGTH = 10;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Override
    public S3Response.ImageInfo getImage(String nanoId, Long placeId, String key) {
        Image image = findAccessibleImage(nanoId, placeId, key);
        Duration expiresIn = Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes());
        Instant expiresAt = Instant.now().plus(expiresIn);

        return S3Response.ImageInfo.of(image, createPresignedGetUrl(image.getImageKey(), expiresIn), expiresAt);
    }

    @Override
    @Transactional
    public void deleteImage(String nanoId, Long placeId, String key) {
        Image image = findAccessibleImage(nanoId, placeId, key);
        deleteObject(image.getImageKey());
        imageRepository.delete(image);
    }

    @Override
    @Transactional
    public S3Response.ImageInfo updateImage(String nanoId, Long placeId, String key, MultipartFile imageFile) {
        Image image = findAccessibleImage(nanoId, placeId, key);
        validateMultipartImage(imageFile);

        String oldImageKey = image.getImageKey();
        String newImageKey = createImageKey(placeId, nanoId, imageFile.getOriginalFilename());
        putImageObject(newImageKey, imageFile);

        image.update(newImageKey, normalizeContentType(imageFile.getContentType()), imageFile.getSize());
        deleteObject(oldImageKey);

        return S3Response.ImageInfo.from(image);
    }

    @Override
    @Transactional
    public S3Response.CompletedImages uploadImages(String nanoId, Long placeId, List<MultipartFile> imageFiles) {
        User user = findActiveUser(nanoId);
        validatePlaceAccess(nanoId, placeId);
        validateImageCount(imageFiles == null ? 0 : imageFiles.size());

        List<Image> images = imageFiles.stream()
                .map(imageFile -> uploadImage(user, placeId, imageFile))
                .toList();

        return S3Response.CompletedImages.builder()
                .images(images.stream()
                        .map(S3Response.ImageInfo::from)
                        .toList())
                .build();
    }

    private Image uploadImage(User user, Long placeId, MultipartFile imageFile) {
        validateMultipartImage(imageFile);

        String imageKey = createImageKey(placeId, user.getNanoId(), imageFile.getOriginalFilename());
        putImageObject(imageKey, imageFile);

        return imageRepository.save(Image.builder()
                .user(user)
                .placeId(placeId)
                .imageKey(imageKey)
                .contentType(normalizeContentType(imageFile.getContentType()))
                .sizeBytes(imageFile.getSize())
                .build());
    }

    private User findActiveUser(String nanoId) {
        return userRepository.findByNanoIdAndStatus(nanoId, UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private Image findAccessibleImage(String nanoId, Long placeId, String imageKey) {
        validatePlaceAccess(nanoId, placeId);
        return imageRepository.findByImageKeyAndPlaceId(imageKey, placeId)
                .orElseThrow(() -> new BusinessException(S3ErrorCode.IMAGE_NOT_FOUND));
    }

    private void validatePlaceAccess(String nanoId, Long placeId) {
        if (!findAccessiblePlaceIds(nanoId).contains(placeId)) {
            throw new BusinessException(S3ErrorCode.PLACE_ACCESS_DENIED);
        }
    }

    private List<Long> findAccessiblePlaceIds(String nanoId) {
        // TODO: place 도메인 구현 후 nanoId 기준으로 사용자가 속한 place id 목록을 조회한다.
        // 테스트용: 무조건 placeId 1 반환
        return List.of(1L);
    }

    private void validateImageCount(int imageCount) {
        if (imageCount < 1 || imageCount > s3Properties.image().maxUploadCount()) {
            throw new BusinessException(S3ErrorCode.TOO_MANY_IMAGES);
        }
    }

    private void validateMultipartImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new BusinessException(S3ErrorCode.INVALID_IMAGE_SIZE);
        }

        validateImageContentType(imageFile.getContentType());
        if (imageFile.getSize() > s3Properties.image().maxSizeBytes()) {
            throw new BusinessException(S3ErrorCode.INVALID_IMAGE_SIZE);
        }
    }

    private void validateImageContentType(String contentType) {
        String normalizedContentType = normalizeContentType(contentType);
        if (!s3Properties.image().allowedContentTypes().contains(normalizedContentType)) {
            throw new BusinessException(S3ErrorCode.INVALID_IMAGE_CONTENT_TYPE);
        }
    }

    private String createPresignedGetUrl(String imageKey, Duration expiresIn) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(imageKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiresIn)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    private void putImageObject(String imageKey, MultipartFile imageFile) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(imageKey)
                .contentType(normalizeContentType(imageFile.getContentType()))
                .contentLength(imageFile.getSize())
                .build();

        try (InputStream inputStream = imageFile.getInputStream()) {
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(inputStream, imageFile.getSize())
            );
        } catch (IOException | SdkException e) {
            throw new BusinessException(S3ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    private void deleteObject(String imageKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(imageKey)
                .build());
    }

    private String createImageKey(Long placeId, String nanoId, String fileName) {
        return IMAGE_KEY_PREFIX + "/" + placeId + "/" + nanoId + "/" + IdUtil.generateUuid() + extractExtension(fileName);
    }

    private String extractExtension(String fileName) {
        String trimmedFileName = fileName.trim();
        int dotIndex = trimmedFileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == trimmedFileName.length() - 1) {
            return "";
        }

        String extension = trimmedFileName.substring(dotIndex + 1)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
        if (!StringUtils.hasText(extension) || extension.length() > MAX_EXTENSION_LENGTH) {
            return "";
        }
        return "." + extension;
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.toLowerCase(Locale.ROOT).trim();
    }
}
