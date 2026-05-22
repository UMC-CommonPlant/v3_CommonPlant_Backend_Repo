package com.commonplant.garden.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    /*
    private boolean isNewUser;

    // 기존 유저 (isNewUser=false)
    private String accessToken;
    private String refreshToken;

    // 신규 유저 (isNewUser=true) — 회원가입 완료 전까지 JWT 미발급
    private String signupToken;
    private String suggestedName;
    private String suggestedImgUrl;
     */

    @Getter
    @Builder
    @Schema(description = "사용자 로그인 응답")
    public class Login {
        private boolean isNewUser;

        // 기존 유저 (isNewUser=false)
        private String accessToken;
        private String refreshToken;

        // 신규 유저 (isNewUser=true) — 회원가입 완료 전까지 JWT 미발급
        private String signupToken;
        private String suggestedName;
        private String suggestedImgUrl;
    }

    @Getter
    @Builder
    @Schema(description = "사용자 등록 응답")
    public class Register {
        private boolean isNewUser;

        private String accessToken;
        private String refreshToken;
    }

}
