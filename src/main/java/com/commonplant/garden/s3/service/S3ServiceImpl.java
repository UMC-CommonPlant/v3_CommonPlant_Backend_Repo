package com.commonplant.garden.s3.service;

import com.commonplant.garden.common.config.S3Properties;
import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.common.util.IdUtil;
import com.commonplant.garden.s3.dto.S3Request;
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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

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
    public S3Response.ImageInfo getImage(String nanoId, Long imageId) {
        Image image = findImageByIdAndOwner(imageId, nanoId);
        Duration expiresIn = Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes());
        Instant expiresAt = Instant.now().plus(expiresIn);

        return S3Response.ImageInfo.of(image, createPresignedGetUrl(image.getImageKey(), expiresIn), expiresAt);
    }

    @Override
    @Transactional
    public void deleteImage(String nanoId, Long imageId) {
        Image image = findImageByIdAndOwner(imageId, nanoId);
        deleteObject(image.getImageKey());
        imageRepository.delete(image);
    }

    @Override
    public S3Response.ImageUploadUrls createImageUploadUrls(String nanoId, S3Request.CreateImageUploadUrls request) {
        User user = findActiveUser(nanoId);
        validateImageCount(request.getFiles().size());

        Duration expiresIn = Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes());
        Instant expiresAt = Instant.now().plus(expiresIn);

        List<S3Response.ImageUploadUrl> files = request.getFiles().stream()
                .map(file -> createImageUploadUrl(user, file, expiresIn, expiresAt))
                .toList();

        return S3Response.ImageUploadUrls.builder()
                .files(files)
                .expiresInMinutes((int) s3Properties.presignedUrlExpirationMinutes())
                .build();
    }

    @Override
    @Transactional
    public S3Response.CompletedImages completeImageUpload(String nanoId, S3Request.CompleteImageUpload request) {
        User user = findActiveUser(nanoId);
        validateImageCount(request.getKeys().size());

        List<Image> images = request.getKeys().stream()
                .map(key -> completeImageUpload(user, key))
                .toList();

        return S3Response.CompletedImages.builder()
                .images(images.stream()
                        .map(S3Response.ImageInfo::from)
                        .toList())
                .build();
    }

    private S3Response.ImageUploadUrl createImageUploadUrl(
            User user,
            S3Request.ImageFile file,
            Duration expiresIn,
            Instant expiresAt
    ) {
        validateRequestedImage(file.getContentType());

        String imageKey = createImageKey(user.getNanoId(), file.getFileName());
        String uploadUrl = createPresignedPutUrl(imageKey, file.getContentType(), expiresIn);

        return S3Response.ImageUploadUrl.builder()
                .key(imageKey)
                .uploadUrl(uploadUrl)
                .expiresAt(expiresAt)
                .build();
    }

    private Image completeImageUpload(User user, String imageKey) {
        validateImageKey(user.getNanoId(), imageKey);
        if (imageRepository.existsByImageKey(imageKey)) {
            throw new BusinessException(S3ErrorCode.INVALID_IMAGE_KEY);
        }

        HeadObjectResponse object = getUploadedObject(imageKey);
        validateUploadedImage(object);

        return imageRepository.save(Image.builder()
                .user(user)
                .imageKey(imageKey)
                .contentType(object.contentType())
                .sizeBytes(object.contentLength())
                .build());
    }

    private User findActiveUser(String nanoId) {
        return userRepository.findByNanoIdAndStatus(nanoId, UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private Image findImageByIdAndOwner(Long imageId, String nanoId) {
        return imageRepository.findByImageIdxAndUser_NanoIdAndUser_Status(imageId, nanoId, UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(S3ErrorCode.IMAGE_NOT_FOUND));
    }

    private void validateImageCount(int imageCount) {
        if (imageCount < 1 || imageCount > s3Properties.image().maxUploadCount()) {
            throw new BusinessException(S3ErrorCode.TOO_MANY_IMAGES);
        }
    }

    private void validateRequestedImage(String contentType) {
        validateImageContentType(contentType);
    }

    private void validateUploadedImage(HeadObjectResponse object) {
        validateImageContentType(object.contentType());
        if (object.contentLength() == null || object.contentLength() > s3Properties.image().maxSizeBytes()) {
            throw new BusinessException(S3ErrorCode.INVALID_IMAGE_SIZE);
        }
    }

    private void validateImageContentType(String contentType) {
        String normalizedContentType = normalizeContentType(contentType);
        if (!s3Properties.image().allowedContentTypes().contains(normalizedContentType)) {
            throw new BusinessException(S3ErrorCode.INVALID_IMAGE_CONTENT_TYPE);
        }
    }

    private void validateImageKey(String nanoId, String imageKey) {
        String expectedPrefix = IMAGE_KEY_PREFIX + "/" + nanoId + "/";
        if (!StringUtils.hasText(imageKey) || !imageKey.startsWith(expectedPrefix) || imageKey.contains("..")) {
            throw new BusinessException(S3ErrorCode.INVALID_IMAGE_KEY);
        }
    }

    private HeadObjectResponse getUploadedObject(String imageKey) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(imageKey)
                    .build());
        } catch (NoSuchKeyException e) {
            throw new BusinessException(S3ErrorCode.IMAGE_NOT_UPLOADED);
        }
    }

    private String createPresignedPutUrl(String imageKey, String contentType, Duration expiresIn) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(imageKey)
                .contentType(normalizeContentType(contentType))
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(expiresIn)
                .putObjectRequest(putObjectRequest)
                .build();

        return s3Presigner.presignPutObject(presignRequest)
                .url()
                .toString();
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
