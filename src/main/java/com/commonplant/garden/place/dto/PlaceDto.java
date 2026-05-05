package com.commonplant.garden.place.dto;

import com.commonplant.garden.place.entity.Place;
// import com.commonplant.garden.plant.dto.PlantDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class PlaceDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class createPlaceReq {
        private String name;
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class updatePlaceReq {
        private String name;
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class updatePlaceRes {
        private String code;
        private String name;
        private String address;
        private String imgUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class getPlaceRes {
        private String name;
        private String code;
        private String address;
        private boolean isOwner;
        private List<getPlaceResUser> userList;
        // private List<PlantDto.getMyGardenPlantListRes> plantList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getPlaceResUser {
        private String name;
        private String image;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getPlaceGridRes {
        private String nx;
        private String ny;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class getPlaceListRes {
        private String image;
        private String code;
        private String name;
        private String member;
        private String plant;

        public getPlaceListRes(Place place, String member, String plant) {
            this.image = place.getImgUrl();
            this.code = place.getCode();
            this.name = place.getName();
            this.member = member;
            this.plant = plant;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class getMainPage {
        private String name;
        private List<getPlaceListRes> placeList;
        // private List<PlantDto.getPlantListRes> plantList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class getPlaceBelongUser {
        private String code;
        private String name;
        private String imgUrl;
    }
}
