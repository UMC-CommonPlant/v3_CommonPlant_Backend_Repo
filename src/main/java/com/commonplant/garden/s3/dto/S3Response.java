package com.commonplant.garden.s3.dto;

import com.commonplant.garden.s3.entity.Image;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

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
        @Schema(description = "이미지 객체 key", example = "images/place-code/plants/1/sample.png")
        private String key;

        @Schema(description = "이미지가 연결된 장소 ID(없을 수 있음)", example = "1", nullable = true)
        private Long placeId;

        @Schema(description = "이미지 MIME 타입", example = "image/png")
        private String contentType;

        @Schema(description = "이미지 파일 크기(byte)", example = "204800")
        private Long sizeBytes;

        @Schema(description = "만료되지 않는 공개 이미지 URL", example = "https://images.commonplant.com/images/place-code/plants/1/sample.png")
        private String imageUrl;

        public static ImageInfo from(Image image) {
            return ImageInfo.builder()
                    .key(image.getImageKey())
                    .placeId(image.getPlaceId())
                    .contentType(image.getContentType())
                    .sizeBytes(image.getSizeBytes())
                    .build();
        }

        public static ImageInfo of(Image image, String imageUrl) {
            return ImageInfo.builder()
                    .key(image.getImageKey())
                    .placeId(image.getPlaceId())
                    .contentType(image.getContentType())
                    .sizeBytes(image.getSizeBytes())
                    .imageUrl(imageUrl)
                    .build();
        }
    }
}
