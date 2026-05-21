package com.commonplant.garden.place.dto;

import com.commonplant.garden.place.entity.Place;
// import com.commonplant.garden.plant.dto.PlantDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class PlaceDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "장소 생성 요청")
    public static class createPlaceReq {

        @NotBlank(message = "P107")
        @Size(max = 10, message = "P107")
        @Schema(description = "장소 이름(최대 10자)", example = "카페")
        private String name;

        @NotBlank(message = "P108")
        @Schema(description = "장소 주소(필수)", example = "서울특별시 ...")
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "장소 수정 요청")
    public static class updatePlaceReq {
        @Schema(description = "현재 대표 이미지 key. 새 파일이 있으면 값과 무관하게 교체하고, 새 파일이 없을 때 기존 key와 같으면 유지하며, 없거나 null이면 삭제합니다.", example = "images/user-nano-id/garden.png", nullable = true)
        private String imageKey;

        @NotBlank(message = "P107")
        @Size(max = 10, message = "P107")
        @Schema(description = "장소 이름", example = "정원")
        private String name;

        @NotBlank(message = "P108")
        @Schema(description = "장소 주소", example = "경기도 ...")
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "장소 수정 응답")
    public static class updatePlaceRes {
        @Schema(description = "장소 코드", example = "ABCabc")
        private String code;

        @Schema(description = "장소 이름", example = "카페")
        private String name;

        @Schema(description = "장소 주소", example = "서울특별시 ...")
        private String address;

        @Schema(description = "이미지 URL", example = "https://.../image.png")
        private String imgUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "장소 조회 응답")
    public static class getPlaceRes {
        @Schema(description = "장소 이름", example = "카페")
        private String name;

        @Schema(description = "장소 코드", example = "ABCabc")
        private String code;

        @Schema(description = "장소 주소", example = "서울특별시 ...")
        private String address;

        @Schema(description = "이미지 URL", example = "https://.../image.png")
        private String imgUrl;

        @Schema(description = "소유자 여부", example = "true")
        private boolean isOwner;

        @Schema(description = "소속 사용자 목록")
        private List<getPlaceResUser> userList;

        // private List<PlantDto.getMyGardenPlantListRes> plantList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "장소의 사용자 정보")
    public static class getPlaceResUser {
        @Schema(description = "사용자 이름", example = "홍길동")
        private String name;

        @Schema(description = "사용자 이미지 URL", example = "https://.../user.png")
        private String image;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "장소의 좌표 정보")
    public static class getPlaceGridRes {
        @Schema(description = "X 좌표", example = "60")
        private String nx;

        @Schema(description = "Y 좌표", example = "127")
        private String ny;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "장소 목록")
    public static class getPlaceListRes {
        @Schema(description = "이미지 URL", example = "https://.../image.png")
        private String image;

        @Schema(description = "장소 코드", example = "ABCabc")
        private String code;

        @Schema(description = "장소 이름", example = "카페")
        private String name;

        @Schema(description = "멤버 수", example = "3")
        private String member;

        @Schema(description = "식물 수", example = "12")
        private String plant;

        public getPlaceListRes(Place place, String member, String plant, String imageUrl) {
            this.image = imageUrl;
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
    @Schema(description = "My Garden 페이지")
    public static class getMainPage {
        @Schema(description = "사용자 이름", example = "홍길동")
        private String name;

        @Schema(description = "장소 목록")
        private List<getPlaceListRes> placeList;

        // private List<PlantDto.getPlantListRes> plantList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "사용자가 속한 장소 정보")
    public static class getPlaceBelongUser {
        @Schema(description = "장소 코드", example = "ABCabc")
        private String code;

        @Schema(description = "장소 이름", example = "카페")
        private String name;

        @Schema(description = "이미지 URL", example = "https://.../image.png")
        private String imgUrl;
    }
}
