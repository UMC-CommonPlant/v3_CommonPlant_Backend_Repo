package com.commonplant.garden.plant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class PlantRequest {

    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        @NotNull
        private Long placeId;

        private String scientificNameKo;

        private String scientificNameEn;

        @NotBlank
        private String nickname;

        private LocalDate lastWateredDate;

        private String imageKey;

        private String description;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String imageKey;

        private String nickname;

        private LocalDate lastWateredDate;
    }
}
