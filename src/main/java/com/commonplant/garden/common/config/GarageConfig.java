package com.commonplant.garden.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(GarageProperties.class)
public class GarageConfig {

    @Bean
    public S3Client garageClient(GarageProperties properties) {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider(properties))
                .serviceConfiguration(serviceConfiguration(properties))
                .build();
    }

    @Bean
    public S3Presigner garagePresigner(GarageProperties properties) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(properties.region()))
                .credentialsProvider(credentialsProvider(properties))
                .serviceConfiguration(serviceConfiguration(properties))
                .build();
    }

    private StaticCredentialsProvider credentialsProvider(GarageProperties properties) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.accessKey(),
                properties.secretKey()
        );
        return StaticCredentialsProvider.create(credentials);
    }

    private S3Configuration serviceConfiguration(GarageProperties properties) {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(Boolean.TRUE.equals(properties.pathStyleAccessEnabled()))
                .build();
    }
}
