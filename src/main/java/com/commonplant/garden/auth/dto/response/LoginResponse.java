package com.commonplant.garden.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private boolean isNewUser;

    // 기존 유저 (isNewUser=false)
    private String accessToken;
    private String refreshToken;

    // 신규 유저 (isNewUser=true) — 회원가입 완료 전까지 JWT 미발급
    private String signupToken;
    private String suggestedName;
    private String suggestedImgUrl;
}
