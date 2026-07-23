package com.commonplant.garden.s3.dto;

import com.commonplant.garden.s3.entity.Image;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

public class S3Response {

    @Getter
    @Builder
    @Schema(description = "이미지 업로드 결과")
    public static class CompletedImages {
        @Schema(description = "업로드된 이미지 목록")
        private List<ImageInfo> images;
    }

    @Getter
    @Builder
    @Schema(description = "이미지 정보")
    public static class ImageInfo {
        @Schema(description = "이미지 객체 key", example = "images/user-nano-id/sample.png")
        private String key;

        @Schema(description = "이미지가 연결된 장소 ID(없을 수 있음)", example = "1", nullable = true)
        private Long placeId;

        @Schema(description = "이미지 MIME 타입", example = "image/png")
        private String contentType;

        @Schema(description = "이미지 파일 크기(byte)", example = "204800")
        private Long sizeBytes;

        @Schema(description = "이미지 다운로드 presigned URL", example = "https://minio.example.com/garden-images/images/user-nano-id/sample.png?X-Amz-Algorithm=...")
        private String downloadUrl;

        @Schema(description = "다운로드 URL 만료 시각", example = "2026-05-12T12:00:00Z")
        private Instant expiresAt;

        public static ImageInfo from(Image image) {
            return ImageInfo.builder()
                    .key(image.getImageKey())
                    .placeId(image.getPlaceId())
                    .contentType(image.getContentType())
                    .sizeBytes(image.getSizeBytes())
                    .build();
        }

        public static ImageInfo of(Image image, String downloadUrl, Instant expiresAt) {
            return ImageInfo.builder()
                    .key(image.getImageKey())
                    .placeId(image.getPlaceId())
                    .contentType(image.getContentType())
                    .sizeBytes(image.getSizeBytes())
                    .downloadUrl(downloadUrl)
                    .expiresAt(expiresAt)
                    .build();
        }
    }
}
