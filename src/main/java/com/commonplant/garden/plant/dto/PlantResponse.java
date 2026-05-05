package com.commonplant.garden.plant.dto;

import com.commonplant.garden.plant.entity.Plant;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PlantResponse {

    @Getter
    @Builder
    public static class CreateResponse {
        private Long plantId;

        public static CreateResponse from(Plant plant) {
            return CreateResponse.builder()
                    .plantId(plant.getPlantIdx())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class PlantListResponse {
        private List<PlantSummary> plants;
        private boolean hasNext;
    }

    @Getter
    @Builder
    public static class PlantSummary {
        private Long plantId;
        private String nickname;
        private String representativeImageKey;

        public static PlantSummary from(Plant plant) {
            return PlantSummary.builder()
                    .plantId(plant.getPlantIdx())
                    .nickname(plant.getNickname())
                    .representativeImageKey(plant.getImageKey())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class DetailResponse {
        private Long plantId;
        private String scientificNameKo;
        private String scientificNameEn;
        private LocalDateTime registeredAt;
        private LocalDate lastWateredDate;
        private String imageKey;
        private String memo;
        private String placeName;
        private String plantInfo;

        public static DetailResponse of(Plant plant, String memo, String placeName) {
            return DetailResponse.builder()
                    .plantId(plant.getPlantIdx())
                    .scientificNameKo(plant.getScientificNameKo())
                    .scientificNameEn(plant.getScientificNameEn())
                    .registeredAt(plant.getCreatedAt())
                    .lastWateredDate(plant.getLastWateredDate())
                    .imageKey(plant.getImageKey())
                    .memo(memo)
                    .placeName(placeName)
                    .plantInfo(plant.getDescription())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class EditInfoResponse {
        private String imageKey;
        private String nickname;
        private LocalDate lastWateredDate;

        public static EditInfoResponse from(Plant plant) {
            return EditInfoResponse.builder()
                    .imageKey(plant.getImageKey())
                    .nickname(plant.getNickname())
                    .lastWateredDate(plant.getLastWateredDate())
                    .build();
        }
    }
}
