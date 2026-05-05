package com.commonplant.garden.plant.dto;

import com.commonplant.garden.plant.entity.Plant;
import lombok.Builder;
import lombok.Getter;

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
}
