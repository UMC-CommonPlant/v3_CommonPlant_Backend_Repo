package com.commonplant.garden.plant.dto;

import com.commonplant.garden.plant.entity.Plant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PlantResponse {

    @Getter
    @Schema(description = "식물 생성 성공 응답")
    public static class CreateJsonResponse extends SuccessJsonResponse {
        @Schema(description = "응답 데이터")
        private CreateResponse result;
    }

    @Getter
    @Schema(description = "식물 삭제 성공 응답")
    public static class DeleteJsonResponse extends SuccessJsonResponse {
        @Schema(description = "응답 데이터")
        private DeleteResponse result;
    }

    @Getter
    @Schema(description = "식물 목록 조회 성공 응답")
    public static class PlantListJsonResponse extends SuccessJsonResponse {
        @Schema(description = "응답 데이터")
        private PlantListResponse result;
    }

    @Getter
    @Schema(description = "식물 상세 조회 성공 응답")
    public static class DetailJsonResponse extends SuccessJsonResponse {
        @Schema(description = "응답 데이터")
        private DetailResponse result;
    }

    @Getter
    @Schema(description = "식물 수정 정보 성공 응답")
    public static class EditInfoJsonResponse extends SuccessJsonResponse {
        @Schema(description = "응답 데이터")
        private EditInfoResponse result;
    }

    @Getter
    public static class SuccessJsonResponse {
        @Schema(description = "응답 시간", example = "2026-05-12 19:30:00")
        private String timeStamp;

        @Schema(description = "성공 여부", example = "true")
        private boolean success;

        @Schema(description = "HTTP 상태 코드", example = "200")
        private int status;

        @Schema(description = "응답 메시지", example = "getPlants")
        private String message;
    }

    @Getter
    @Builder
    @Schema(description = "식물 생성 응답")
    public static class CreateResponse {
        @Schema(description = "생성된 식물 ID", example = "1")
        private Long plantId;

        public static CreateResponse from(Plant plant) {
            return CreateResponse.builder()
                    .plantId(plant.getPlantIdx())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "식물 삭제 응답")
    public static class DeleteResponse {
        @Schema(description = "삭제된 식물 ID", example = "1")
        private Long plantId;

        public static DeleteResponse of(Long plantId) {
            return DeleteResponse.builder()
                    .plantId(plantId)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "식물 목록 응답")
    public static class PlantListResponse {
        @Schema(description = "식물 목록")
        private List<PlantSummary> plants;

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        private boolean hasNext;
    }

    @Getter
    @Builder
    @Schema(description = "식물 요약 정보")
    public static class PlantSummary {
        @Schema(description = "식물 ID", example = "1")
        private Long plantId;

        @Schema(description = "식물 애칭", example = "거실 몬스테라")
        private String nickname;

        @Schema(description = "대표 이미지 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/images/user-nano-id/monstera.png?X-Amz-Algorithm=...")
        private String representativeImageUrl;

        public static PlantSummary of(Plant plant, String representativeImageUrl) {
            return PlantSummary.builder()
                    .plantId(plant.getPlantIdx())
                    .nickname(plant.getNickname())
                    .representativeImageUrl(representativeImageUrl)
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "식물 상세 응답")
    public static class DetailResponse {
        @Schema(description = "식물 ID", example = "1")
        private Long plantId;

        @Schema(description = "식물 한글 학명", example = "몬스테라")
        private String scientificNameKo;

        @Schema(description = "식물 영문 학명", example = "Monstera deliciosa")
        private String scientificNameEn;

        @Schema(description = "등록 일시", example = "2026-05-12T19:30:00")
        private LocalDateTime registeredAt;

        @Schema(description = "마지막으로 물을 준 날짜", example = "2026-05-12")
        private LocalDate lastWateredDate;

        @Schema(description = "대표 이미지 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/images/user-nano-id/monstera.png?X-Amz-Algorithm=...")
        private String imageUrl;

        @Schema(description = "대표/최근 메모", example = "새 잎이 올라옴", nullable = true)
        private String memo;

        @Schema(description = "장소 이름", example = "거실 정원")
        private String placeName;

        @Schema(description = "식물 설명", example = "햇빛이 잘 드는 거실에서 키우는 몬스테라입니다.")
        private String plantInfo;

        public static DetailResponse of(Plant plant, String memo, String placeName, String imageUrl) {
            return DetailResponse.builder()
                    .plantId(plant.getPlantIdx())
                    .scientificNameKo(plant.getScientificNameKo())
                    .scientificNameEn(plant.getScientificNameEn())
                    .registeredAt(plant.getCreatedAt())
                    .lastWateredDate(plant.getLastWateredDate())
                    .imageUrl(imageUrl)
                    .memo(memo)
                    .placeName(placeName)
                    .plantInfo(plant.getDescription())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "식물 수정 정보 응답")
    public static class EditInfoResponse {
        @Schema(description = "대표 이미지 key", example = "images/user-nano-id/monstera.png", nullable = true)
        private String imageKey;

        @Schema(description = "대표 이미지 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/images/user-nano-id/monstera.png?X-Amz-Algorithm=...")
        private String imageUrl;

        @Schema(description = "식물 애칭", example = "거실 몬스테라")
        private String nickname;

        @Schema(description = "마지막으로 물을 준 날짜", example = "2026-05-12")
        private LocalDate lastWateredDate;

        public static EditInfoResponse of(Plant plant, String imageUrl) {
            return EditInfoResponse.builder()
                    .imageKey(plant.getImageKey())
                    .imageUrl(imageUrl)
                    .nickname(plant.getNickname())
                    .lastWateredDate(plant.getLastWateredDate())
                    .build();
        }
    }
}
