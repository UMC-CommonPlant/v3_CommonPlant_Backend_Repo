package com.commonplant.garden.auth.dto.request;

import com.commonplant.garden.user.enums.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRequest {

    @Getter
    @NoArgsConstructor
    public static class SocialLogin {
        @NotNull
        private Provider provider;

        @NotBlank
        private String token;
    }
}
