package com.commonplant.garden.user.dto;

import com.commonplant.garden.user.enums.Provider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserRequest {

    @Getter
    @NoArgsConstructor
    public static class CreateRequest {

        @NotBlank
        private String name;

        @NotBlank
        @Email
        private String email;

        @NotNull
        private Provider provider;

        @NotBlank
        private String providerId;

        private String imgUrl;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {

        private String name;
        private String introduction;
        private String imgUrl;
    }
}
