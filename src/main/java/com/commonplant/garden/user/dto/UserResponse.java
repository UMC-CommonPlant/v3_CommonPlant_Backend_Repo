package com.commonplant.garden.user.dto;

import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.enums.Provider;
import com.commonplant.garden.user.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private String name;
    private String id;
    private String email;
    private Provider provider;
    private String imgUrl;
    private String introduction;
    private UserStatus status;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .name(user.getName())
                .id(user.getNanoId())
                .email(user.getEmail())
                .provider(user.getProvider())
                .imgUrl(user.getImgUrl())
                .introduction(user.getIntroduction())
                .status(user.getStatus())
                .build();
    }
}
