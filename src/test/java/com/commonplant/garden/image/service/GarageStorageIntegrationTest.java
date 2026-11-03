package com.commonplant.garden.image.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "GARAGE_INTEGRATION_TEST", matches = "true")
class GarageStorageIntegrationTest {

    private static final String DEFAULT_ENDPOINT = "http://localhost:3900";
    private static final String DEFAULT_REGION = "garage";
    private static final String DEFAULT_ACCESS_KEY = "GK0123456789abcdef0123456789abcdef";
    private static final String DEFAULT_SECRET_KEY =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
    private static final String DEFAULT_BUCKET = "commonplant-local";
    private static final String DEFAULT_PUBLIC_BASE_URL =
            "http://localhost:3902";

    @Test
    void uploadsDownloadsFromPublicUrlAndDeletesObject() throws Exception {
        URI endpoint = URI.create(environment("GARAGE_ENDPOINT", DEFAULT_ENDPOINT));
        Region region = Region.of(environment("GARAGE_REGION", DEFAULT_REGION));
        String accessKey = environment("GARAGE_ACCESS_KEY", DEFAULT_ACCESS_KEY);
        String secretKey = environment("GARAGE_SECRET_KEY", DEFAULT_SECRET_KEY);
        String bucket = environment("GARAGE_BUCKET_NAME", DEFAULT_BUCKET);
        String publicBaseUrl = environment("GARAGE_PUBLIC_BASE_URL", DEFAULT_PUBLIC_BASE_URL);
        String objectKey = "images/integration-tests/" + UUID.randomUUID() + ".txt";
        byte[] content = "garage-integration-test".getBytes(StandardCharsets.UTF_8);

        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
        S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        try (S3Client client = S3Client.builder()
                .endpointOverride(endpoint)
                .region(region)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(s3Configuration)
                .build()) {
            try {
                client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(objectKey)
                                .contentType("text/plain")
                                .cacheControl("public, max-age=31536000, immutable")
                                .build(),
                        RequestBody.fromBytes(content)
                );

                URI imageUri = URI.create(publicBaseUrl.replaceAll("/+$", "") + "/" + objectKey);

                HttpResponse<byte[]> response = HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder(imageUri).GET().build(),
                        HttpResponse.BodyHandlers.ofByteArray()
                );

                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.body()).isEqualTo(content);
            } finally {
                client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .build());
            }
        }
    }

    private String environment(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
