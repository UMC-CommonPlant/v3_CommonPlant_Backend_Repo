package com.commonplant.garden.user.dto;

import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.enums.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "사용자 정보")
public class UserResponse {

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "사용자 nanoId (고유 식별자)", example = "V1StGXR8_Z5jdHi6B-myT")
    private String id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "소셜 로그인 제공자", example = "GOOGLE")
    private Provider provider;

    @Schema(description = "프로필 이미지 URL", example = "https://bucket.s3.ap-northeast-2.amazonaws.com/images/profile.png", nullable = true)
    private String imgUrl;

    @Schema(description = "자기소개", example = "몬스테라를 키우는 식집사입니다.", nullable = true)
    private String introduction;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .name(user.getName())
                .id(user.getNanoId())
                .email(user.getEmail())
                .provider(user.getProvider())
                .imgUrl(user.getImgUrl())
                .introduction(user.getIntroduction())
                .build();
    }

    // ── Swagger 응답 래퍼 ──────────────────────────────────────────────────────

    @Getter
    @Schema(name = "UserJsonResponse", description = "사용자 단건 조회/수정 성공 응답")
    public static class UserJsonResponse extends SuccessJsonResponse {
        @Schema(description = "응답 데이터")
        private UserResponse result;
    }

    @Getter
    @Schema(name = "UserListJsonResponse", description = "사용자 목록 조회 성공 응답")
    public static class UserListJsonResponse extends SuccessJsonResponse {
        @Schema(description = "응답 데이터")
        private List<UserResponse> result;
    }

    @Getter
    @Schema(name = "UserDeleteJsonResponse", description = "회원 탈퇴 성공 응답")
    public static class DeleteJsonResponse extends SuccessJsonResponse {
        @Schema(description = "응답 데이터 (null)", nullable = true)
        private Object result;
    }

    @Getter
    @Schema(name = "UserSuccessJsonResponse")
    public static class SuccessJsonResponse {
        @Schema(description = "응답 시간", example = "2026-05-12 19:30:00")
        private String timeStamp;

        @Schema(description = "성공 여부", example = "true")
        private boolean success;

        @Schema(description = "HTTP 상태 코드", example = "200")
        private int status;

        @Schema(description = "응답 메시지", example = "getUserByNanoId")
        private String message;
    }
}
