package com.commonplant.garden.image.service;

import com.commonplant.garden.common.config.GarageProperties;
import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.common.util.IdUtil;
import com.commonplant.garden.s3.dto.S3Response;
import com.commonplant.garden.s3.entity.Image;
import com.commonplant.garden.s3.entity.ImageRepository;
import com.commonplant.garden.s3.exception.S3ErrorCode;
import com.commonplant.garden.s3.service.S3Service;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.entity.UserRepository;
import com.commonplant.garden.user.enums.UserStatus;
import com.commonplant.garden.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GarageImageService implements S3Service {

    private static final String IMAGE_KEY_PREFIX = "images";
    private static final int MAX_EXTENSION_LENGTH = 10;

    private final S3Client garageClient;
    private final S3Presigner garagePresigner;
    private final GarageProperties garageProperties;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Override
    public String getImageUrl(String key) {
        Image image = findImageByKey(key);
        return createPresignedGetUrl(image.getImageKey());
    }

    @Override
    public S3Response.ImageInfo getImage(String nanoId, String key) {
        findActiveUser(nanoId);
        Image image = findImageByKey(key);
        Duration expiresIn = presignedUrlDuration();
        Instant expiresAt = Instant.now().plus(expiresIn);

        return S3Response.ImageInfo.of(image, createPresignedGetUrl(image.getImageKey()), expiresAt);
    }

    @Override
    @Transactional
    public void deleteImage(String nanoId, String key) {
        findActiveUser(nanoId);
        Image image = findImageByKey(key);
        imageRepository.delete(image);
        deleteObjectAfterCommit(image.getImageKey());
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
        deleteObjectOnRollback(newImageKey);

        image.update(newImageKey, normalizeContentType(imageFile.getContentType()), imageFile.getSize());
        deleteObjectAfterCommit(oldImageKey);

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
        deleteObjectOnRollback(imageKey);

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
        if (imageCount < 1 || imageCount > garageProperties.image().maxUploadCount()) {
            throw new BusinessException(S3ErrorCode.TOO_MANY_IMAGES);
        }
    }

    private void validateMultipartImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new BusinessException(S3ErrorCode.INVALID_IMAGE_SIZE);
        }

        validateImageContentType(imageFile.getContentType());
        if (imageFile.getSize() > garageProperties.image().maxSizeBytes()) {
            throw new BusinessException(S3ErrorCode.INVALID_IMAGE_SIZE);
        }
    }

    private void validateImageContentType(String contentType) {
        String normalizedContentType = normalizeContentType(contentType);
        if (!garageProperties.image().allowedContentTypes().contains(normalizedContentType)) {
            throw new BusinessException(S3ErrorCode.INVALID_IMAGE_CONTENT_TYPE);
        }
    }

    private String createPresignedGetUrl(String imageKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(garageProperties.bucketName())
                .key(imageKey)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(presignedUrlDuration())
                .getObjectRequest(getObjectRequest)
                .build();

        try {
            return garagePresigner.presignGetObject(presignRequest).url().toString();
        } catch (SdkException e) {
            throw new BusinessException(S3ErrorCode.IMAGE_URL_GENERATION_FAILED);
        }
    }

    private void putImageObject(String imageKey, MultipartFile imageFile) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(garageProperties.bucketName())
                .key(imageKey)
                .contentType(normalizeContentType(imageFile.getContentType()))
                .contentLength(imageFile.getSize())
                .build();

        try (InputStream inputStream = imageFile.getInputStream()) {
            garageClient.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(inputStream, imageFile.getSize())
            );
        } catch (IOException | SdkException e) {
            throw new BusinessException(S3ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    private void deleteObject(String imageKey) {
        try {
            garageClient.deleteObject(DeleteObjectRequest.builder()
                    .bucket(garageProperties.bucketName())
                    .key(imageKey)
                    .build());
        } catch (SdkException e) {
            throw new BusinessException(S3ErrorCode.IMAGE_DELETE_FAILED);
        }
    }

    private void deleteObjectAfterCommit(String imageKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteObject(imageKey);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteObjectQuietly(imageKey, "committed object replacement");
            }
        });
    }

    private void deleteObjectOnRollback(String imageKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    deleteObjectQuietly(imageKey, "rolled-back object upload");
                }
            }
        });
    }

    private void deleteObjectQuietly(String imageKey, String operation) {
        try {
            deleteObject(imageKey);
        } catch (BusinessException e) {
            log.error("Failed to clean up Garage image after {}: key={}", operation, imageKey, e);
        }
    }

    private Duration presignedUrlDuration() {
        return Duration.ofMinutes(garageProperties.presignedUrlExpirationMinutes());
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
