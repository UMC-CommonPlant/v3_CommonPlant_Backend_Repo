package com.commonplant.garden.auth.dto;

import com.commonplant.garden.user.enums.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRequest {
    public static final int TOKEN_MIM_LENGTH = 512;
    public static final int NAME_MIN_LENGTH = 1;
    public static final int NAME_MAX_LENGTH = 20;
    public static final int INTRODUCTION_MAX_LENGTH = 200;

    @Getter
    @NoArgsConstructor
    @Schema(description = "회원가입 multipart 요청")
    public static class RegisterMultipartRequest {
        @Schema(description = "회원가입 정보(JSON)", implementation = RegisterRequest.class,
                requiredMode = Schema.RequiredMode.REQUIRED)
        private RegisterRequest register;

        @Schema(description = "사용자 프로필 이미지(선택)", type = "string", format = "binary", nullable = true)
        private String image;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "소셜 로그인 요청")
    public static class Login {
        @NotNull
        @Schema(description = "소셜 로그인 제공자", example = "GOOGLE", allowableValues = {"GOOGLE", "KAKAO", "APPLE"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        private Provider provider;

        @NotBlank
        @Schema(description = "소셜 SDK 토큰", example = "ya29.a0AfH6SMBx...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String token;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "회원가입 요청")
    public static class RegisterRequest {
        /** login 응답으로 받은 signupToken */
        @NotBlank
        @Size(max = TOKEN_MIM_LENGTH, message = "signupToken은 512자를 초과할 수 없습니다.")
        @Schema(description = "/auth/login 응답에서 받은 signupToken (유효시간 10분)", example = "eyJhbGciOiJIUzI1NiJ9...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String signupToken;

        @NotBlank
        @Size(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH, message = "이름은 1자 이상 20자 이하여야 합니다.")
        @Pattern(
                regexp = "^[a-zA-Z가-힣\\s]+$",
                message = "이름은 한글, 영문, 공백만 허용됩니다."
        )
        @Schema(description = "1~20자 사이 (허용: 한글, 영문, 공백)", example = "홍길동",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Size(max = INTRODUCTION_MAX_LENGTH, message = "소개는 200자를 초과할 수 없습니다.")
        @Pattern(
                regexp = "^[^\\\\<>%;&+]*$",
                message = "소개에 사용할 수 없는 특수문자가 포함되어 있습니다."
        )
        @Schema(description = "200자 이내 (비허용: <>%;&+\\)", example = "몬스테라를 키우는 식집사입니다.", nullable = true)
        private String introduction;
    }
}
