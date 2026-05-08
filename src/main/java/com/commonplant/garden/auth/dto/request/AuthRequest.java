package com.commonplant.garden.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRequest {

    @Getter
    @NoArgsConstructor
    public static class GoogleLogin {
        @NotBlank
        private String idToken;
    }
}
