package com.commonplant.garden.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "s3")
public record S3Properties(
        String bucket,
        long presignedUrlExpirationMinutes
) {
}
