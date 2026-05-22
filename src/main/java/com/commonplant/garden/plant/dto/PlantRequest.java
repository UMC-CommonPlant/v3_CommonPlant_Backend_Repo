package com.commonplant.garden.plant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class PlantRequest {
    public static final int SCIENTIFIC_NAME_MAX_LENGTH = 20;
    public static final int NICKNAME_MAX_LENGTH = 20;
    public static final int DESCRIPTION_MAX_LENGTH = 200;

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

        @Size(max = SCIENTIFIC_NAME_MAX_LENGTH, message = "식물 한글 학명은 20자 이하여야 합니다.")
        @Schema(description = "식물 한글 학명(최대 20자)", example = "몬스테라", maxLength = SCIENTIFIC_NAME_MAX_LENGTH)
        private String scientificNameKo;

        @Size(max = SCIENTIFIC_NAME_MAX_LENGTH, message = "식물 영문 학명은 20자 이하여야 합니다.")
        @Schema(description = "식물 영문 학명(최대 20자)", example = "Monstera deliciosa", maxLength = SCIENTIFIC_NAME_MAX_LENGTH)
        private String scientificNameEn;

        @NotBlank
        @Size(max = NICKNAME_MAX_LENGTH, message = "식물 애칭은 20자 이하여야 합니다.")
        @Schema(description = "식물 애칭(최대 20자)", example = "거실 몬스테라", maxLength = NICKNAME_MAX_LENGTH, requiredMode = Schema.RequiredMode.REQUIRED)
        private String nickname;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "마지막으로 물을 준 날짜(yyyy-MM-dd)", example = "2026-05-12")
        private LocalDate lastWateredDate;

        @Size(max = DESCRIPTION_MAX_LENGTH, message = "식물 설명은 200자 이하여야 합니다.")
        @Schema(description = "식물 설명(최대 200자)", example = "햇빛이 잘 드는 거실에서 키우는 몬스테라입니다.", maxLength = DESCRIPTION_MAX_LENGTH)
        private String description;
    }

    @Getter
    @NoArgsConstructor
    @Schema(description = "식물 수정 요청")
    public static class UpdateRequest {
        @Schema(description = "현재 대표 이미지 key. 새 파일이 있으면 값과 무관하게 교체하고, 새 파일이 없을 때 기존 key와 같으면 유지하며, 없거나 null이면 삭제합니다.", example = "images/user-nano-id/monstera.png", nullable = true)
        private String imageKey;

        @Size(max = NICKNAME_MAX_LENGTH, message = "식물 애칭은 20자 이하여야 합니다.")
        @Schema(description = "식물 애칭(최대 20자)", example = "새 몬스테라", maxLength = NICKNAME_MAX_LENGTH)
        private String nickname;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "마지막으로 물을 준 날짜(yyyy-MM-dd)", example = "2026-05-13")
        private LocalDate lastWateredDate;
    }
}
