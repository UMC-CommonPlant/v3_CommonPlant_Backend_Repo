package com.commonplant.garden.user.dto;

import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.enums.Provider;
import com.commonplant.garden.user.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long userIdx;
    private String uuid;
    private String name;
    private String email;
    private String imgUrl;
    private String introduction;
    private UserStatus status;
    private Provider provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userIdx(user.getUserIdx())
                .uuid(user.getUuid())
                .name(user.getName())
                .email(user.getEmail())
                .imgUrl(user.getImgUrl())
                .introduction(user.getIntroduction())
                .status(user.getStatus())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
