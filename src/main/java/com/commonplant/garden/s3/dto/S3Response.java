package com.commonplant.garden.s3.dto;

import com.commonplant.garden.s3.entity.Image;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

public class S3Response {

    @Getter
    @Builder
    public static class ImageUploadUrls {
        private List<ImageUploadUrl> files;
        private int expiresInMinutes;
    }

    @Getter
    @Builder
    public static class ImageUploadUrl {
        private String key;
        private String uploadUrl;
        private Instant expiresAt;
    }

    @Getter
    @Builder
    public static class CompletedImages {
        private List<ImageInfo> images;
    }

    @Getter
    @Builder
    public static class ImageInfo {
        private String key;
        private Long placeId;
        private String contentType;
        private Long sizeBytes;
        private String downloadUrl;
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
