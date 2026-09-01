package com.commonplant.garden.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.List;

@ConfigurationProperties(prefix = "garage")
public record GarageProperties(
        String endpoint,
        String region,
        String accessKey,
        String secretKey,
        String bucketName,
        Boolean pathStyleAccessEnabled,
        String publicBaseUrl,
        Image image
) {
    private static final String DEFAULT_ENDPOINT = "http://localhost:3900";
    private static final String DEFAULT_REGION = "garage";
    private static final String DEFAULT_BUCKET_NAME = "commonplant-local";
    private static final String DEFAULT_PUBLIC_BASE_URL = "http://localhost:3902";
    private static final int DEFAULT_MAX_UPLOAD_COUNT = 5;
    private static final long DEFAULT_MAX_SIZE_BYTES = 10_485_760;
    private static final List<String> DEFAULT_ALLOWED_CONTENT_TYPES =
            List.of("image/jpeg", "image/png", "image/webp");

    public GarageProperties {
        if (!StringUtils.hasText(endpoint)) {
            endpoint = DEFAULT_ENDPOINT;
        }
        if (!StringUtils.hasText(region)) {
            region = DEFAULT_REGION;
        }
        if (!StringUtils.hasText(bucketName)) {
            bucketName = DEFAULT_BUCKET_NAME;
        }
        if (pathStyleAccessEnabled == null) {
            pathStyleAccessEnabled = true;
        }
        if (!StringUtils.hasText(publicBaseUrl)) {
            publicBaseUrl = DEFAULT_PUBLIC_BASE_URL;
        } else {
            publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
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
        public Image {
            if (maxUploadCount <= 0) {
                maxUploadCount = DEFAULT_MAX_UPLOAD_COUNT;
            }
            if (maxSizeBytes <= 0) {
                maxSizeBytes = DEFAULT_MAX_SIZE_BYTES;
            }
            if (allowedContentTypes == null || allowedContentTypes.isEmpty()) {
                allowedContentTypes = DEFAULT_ALLOWED_CONTENT_TYPES;
            } else {
                allowedContentTypes = List.copyOf(allowedContentTypes);
            }
        }
    }
}
