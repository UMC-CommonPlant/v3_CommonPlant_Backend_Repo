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
    public S3Response.ImageInfo getImage(String nanoId, String key) {
        findActiveUser(nanoId);
        Image image = findImageByKey(key);
        Duration expiresIn = Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes());
        Instant expiresAt = Instant.now().plus(expiresIn);

        return S3Response.ImageInfo.of(image, createPresignedGetUrl(image.getImageKey(), expiresIn), expiresAt);
    }

    @Override
    @Transactional
    public void deleteImage(String nanoId, String key) {
        findActiveUser(nanoId);
        Image image = findImageByKey(key);
        deleteObject(image.getImageKey());
        imageRepository.delete(image);
    }

    @Override
    @Transactional
    public S3Response.ImageInfo updateImage(String nanoId, String key, MultipartFile imageFile) {
        findActiveUser(nanoId);
        Image image = findImageByKey(key);
        validateMultipartImage(imageFile);

        String oldImageKey = image.getImageKey();
        String newImageKey = createImageKey(nanoId, imageFile.getOriginalFilename());
        putImageObject(newImageKey, imageFile);

        image.update(newImageKey, normalizeContentType(imageFile.getContentType()), imageFile.getSize());
        deleteObject(oldImageKey);

        return S3Response.ImageInfo.from(image);
    }

    @Override
    @Transactional
    public S3Response.CompletedImages uploadImages(String nanoId, List<MultipartFile> imageFiles) {
        User user = findActiveUser(nanoId);
        validateImageCount(imageFiles == null ? 0 : imageFiles.size());

        List<Image> images = imageFiles.stream()
                .map(imageFile -> uploadImage(user, imageFile))
                .toList();

        return S3Response.CompletedImages.builder()
                .images(images.stream()
                        .map(S3Response.ImageInfo::from)
                        .toList())
                .build();
    }

    @Override
    @Transactional
    public S3Response.ImageInfo uploadImage(String nanoId, MultipartFile imageFile) {
        User user = findActiveUser(nanoId);
        return S3Response.ImageInfo.from(uploadImage(user, imageFile));
    }

    private Image uploadImage(User user, MultipartFile imageFile) {
        validateMultipartImage(imageFile);

        String imageKey = createImageKey(user.getNanoId(), imageFile.getOriginalFilename());
        putImageObject(imageKey, imageFile);

        return imageRepository.save(Image.builder()
                .user(user)
                .imageKey(imageKey)
                .contentType(normalizeContentType(imageFile.getContentType()))
                .sizeBytes(imageFile.getSize())
                .build());
    }

    private User findActiveUser(String nanoId) {
        return userRepository.findByNanoIdAndStatus(nanoId, UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private Image findImageByKey(String imageKey) {
        return imageRepository.findByImageKey(imageKey)
                .orElseThrow(() -> new BusinessException(S3ErrorCode.IMAGE_NOT_FOUND));
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

    private String createImageKey(String nanoId, String fileName) {
        return IMAGE_KEY_PREFIX + "/" + nanoId + "/" + IdUtil.generateUuid() + extractExtension(fileName);
    }

    private String extractExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }

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
