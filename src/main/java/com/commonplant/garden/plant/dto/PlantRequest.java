package com.commonplant.garden.plant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class PlantRequest {

    @Getter
    @NoArgsConstructor
    @Schema(description = "식물 생성 multipart 요청")
    public static class CreateMultipartRequest {
        @Schema(description = "식물 생성 정보(JSON)", implementation = CreateRequest.class, requiredMode = Schema.RequiredMode.REQUIRED)
        private CreateRequest plant;

        @Schema(description = "식물 이미지 파일", type = "string", format = "binary", nullable = true)
        private String image;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "식물 수정 multipart 요청")
    public static class UpdateMultipartRequest {
        @Schema(description = "식물 수정 정보(JSON)", implementation = UpdateRequest.class, nullable = true)
        private UpdateRequest plant;

        @Schema(description = "식물 이미지 파일", type = "string", format = "binary", nullable = true)
        private String image;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "식물 생성 요청")
    public static class CreateRequest {
        @NotBlank
        @Schema(description = "식물을 등록할 장소 코드", example = "Abc123", requiredMode = Schema.RequiredMode.REQUIRED)
        private String placeCode;

        @Schema(description = "식물 한글 학명", example = "몬스테라")
        private String scientificNameKo;

        @Schema(description = "식물 영문 학명", example = "Monstera deliciosa")
        private String scientificNameEn;

        @NotBlank
        @Schema(description = "식물 애칭", example = "거실 몬스테라", requiredMode = Schema.RequiredMode.REQUIRED)
        private String nickname;

        @Schema(description = "마지막으로 물을 준 날짜", example = "2026-05-12")
        private LocalDate lastWateredDate;

        @Schema(description = "식물 설명", example = "햇빛이 잘 드는 거실에서 키우는 몬스테라입니다.")
        private String description;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "식물 수정 요청")
    public static class UpdateRequest {
        @Schema(description = "현재 대표 이미지 key. 새 파일이 있으면 값과 무관하게 교체하고, 새 파일이 없을 때 기존 key와 같으면 유지하며, 없거나 null이면 삭제합니다.", example = "images/user-nano-id/monstera.png", nullable = true)
        private String imageKey;

        @Schema(description = "식물 애칭", example = "새 몬스테라")
        private String nickname;

        @Schema(description = "마지막으로 물을 준 날짜", example = "2026-05-13")
        private LocalDate lastWateredDate;
    }
}
