package com.commonplant.garden.plant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class PlantRequest {

    @Getter
    @NoArgsConstructor
    @Schema(description = "식물 생성 요청")
    public static class CreateRequest {
        @NotNull
        @Schema(description = "식물을 등록할 장소 ID", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long placeId;

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
        @Schema(description = "식물 애칭", example = "새 몬스테라")
        private String nickname;

        @Schema(description = "마지막으로 물을 준 날짜", example = "2026-05-13")
        private LocalDate lastWateredDate;
    }
}
