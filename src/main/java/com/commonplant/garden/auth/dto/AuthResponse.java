package com.commonplant.garden.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    // ── Swagger 응답 래퍼 ──────────────────────────────────────────────────────

    @Getter
    @Schema(description = "소셜 로그인 성공 응답 (기존 유저 - isNewUser: false)")
    public static class LoginSuccessJsonResponse extends SuccessJsonResponse {
        @Schema(description = "응답 데이터")
        private LoginSuccess result;
    }

    @Getter
    @Schema(description = "소셜 로그인 신규 유저 응답 (isNewUser: true) - /auth/register 호출 필요")
    public static class LoginNewUserJsonResponse extends SuccessJsonResponse {
        @Schema(description = "응답 데이터")
        private LoginFailed result;
    }

    @Getter
    @Schema(description = "회원가입 성공 응답")
    public static class RegisterJsonResponse extends SuccessJsonResponse {
        @Schema(description = "응답 데이터")
        private Register result;
    }

    @Getter
    public static class SuccessJsonResponse {
        @Schema(description = "응답 시간", example = "2026-05-12 19:30:00")
        private String timeStamp;

        @Schema(description = "성공 여부", example = "true")
        private boolean success;

        @Schema(description = "HTTP 상태 코드", example = "200")
        private int status;

        @Schema(description = "응답 메시지", example = "login")
        private String message;
    }

    // ── 실제 응답 DTO ──────────────────────────────────────────────────────────

    @Getter
    @Builder
    @Schema(description = "소셜 로그인 응답")
    public static class LoginSuccess {
        @Schema(description = "신규 유저 여부 (true: 회원가입 필요, false: 기존 유저)", example = "false")
        private boolean isNewUser;

        // 기존 유저 (isNewUser=false)
        @Schema(description = "액세스 토큰 (기존 유저일 때 반환)", example = "eyJhbGciOiJIUzI1NiJ9...", nullable = true)
        private String accessToken;

        @Schema(description = "리프레시 토큰 (기존 유저일 때 반환)", example = "eyJhbGciOiJIUzI1NiJ9...", nullable = true)
        private String refreshToken;

    }

    @Getter
    @Builder
    @Schema(description = "소셜 로그인 응답")
    public static class LoginFailed {
        @Schema(description = "신규 유저 여부 (true: 회원가입 필요, false: 기존 유저)", example = "false")
        private boolean isNewUser;

        @Schema(description = "회원가입 완료용 토큰 (신규 유저일 때 반환, 유효시간 10분)", example = "eyJhbGciOiJIUzI1NiJ9...", nullable = true)
        private String signupToken;

        @Schema(description = "소셜 프로필에서 가져온 이름 제안 (신규 유저일 때 반환)", example = "홍길동", nullable = true)
        private String suggestedName;

        @Schema(description = "소셜 프로필에서 가져온 이미지 URL 제안 (신규 유저일 때 반환)", example = "https://lh3.googleusercontent.com/...", nullable = true)
        private String suggestedImgUrl;
    }


    @Getter
    @Builder
    @Schema(description = "회원가입 응답")
    public static class Register {
        @Schema(description = "신규 유저 여부 (항상 false)", example = "false")
        private boolean isNewUser;

        @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        private String accessToken;

        @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        private String refreshToken;
    }

}
