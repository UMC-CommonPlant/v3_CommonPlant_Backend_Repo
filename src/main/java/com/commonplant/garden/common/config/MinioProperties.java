package com.commonplant.garden.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucketName,
        long presignedUrlExpirationMinutes,
        Image image
) {
    private static final long DEFAULT_PRESIGNED_URL_EXPIRATION_MINUTES = 10;
    private static final int DEFAULT_MAX_UPLOAD_COUNT = 5;
    private static final long DEFAULT_MAX_SIZE_BYTES = 10_485_760;
    private static final List<String> DEFAULT_ALLOWED_CONTENT_TYPES =
            List.of("image/jpeg", "image/png", "image/webp");

    public MinioProperties {
        if (presignedUrlExpirationMinutes <= 0) {
            presignedUrlExpirationMinutes = DEFAULT_PRESIGNED_URL_EXPIRATION_MINUTES;
        }
        if (image == null) {
            image = new Image(
                    DEFAULT_MAX_UPLOAD_COUNT,
                    DEFAULT_MAX_SIZE_BYTES,
                    DEFAULT_ALLOWED_CONTENT_TYPES
            );
        }
    }

    public record Image(
            int maxUploadCount,
            long maxSizeBytes,
            List<String> allowedContentTypes
    ) {
    }
}
