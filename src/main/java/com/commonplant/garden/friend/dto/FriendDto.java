package com.commonplant.garden.friend.dto;

import com.commonplant.garden.friend.enums.FriendStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class FriendDto {

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "친구 요청 관련 Request")
    public static class sendFriendReq {
        @Schema(description = "친구 요청 받는 사람", example = "[\"커먼2\", \"커먼3\"]")
        private List<String> receiverName = new ArrayList<>();
        @Schema(description = "장소 코드", example = "aBcDeF")
        private String placeCode;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "친구 요청 Response")
    public static class sendFriendRes {
        private String placeCode;
        private List<String> receiverList;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "친구 요청 수락/거절 Request")
    public static class friendDecisionReq {
        private Long friendId;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "친구 요청 목록 Response Item")
    public static class friendRequestItem {
        private Long friendId;
        private String senderName;
        private String senderImgUrl;
        private String placeCode;
        private String placeName;
        private String placeAddress;
        private FriendStatus status;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "친구 요청 목록 Response")
    public static class friendRequestListRes {
        private List<friendRequestItem> requests;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "장소등록 및 친구 요청 관련 Response")
    public static class placeCodeAndFriendResponse {
        private String placeCode;
        private List<String> receiverList;
    }
}
