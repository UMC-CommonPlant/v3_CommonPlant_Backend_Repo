package com.commonplant.garden.plant.dto;

import com.commonplant.garden.plant.entity.Plant;
import lombok.Builder;
import lombok.Getter;

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
}
