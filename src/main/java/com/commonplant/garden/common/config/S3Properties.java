package com.commonplant.garden.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "s3")
public record S3Properties(
        String bucket,
        long presignedUrlExpirationMinutes,
        Image image
) {
    public record Image(
            int maxUploadCount,
            long maxSizeBytes,
            List<String> allowedContentTypes
    ) {
    }
}
