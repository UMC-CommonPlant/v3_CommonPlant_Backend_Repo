package com.commonplant.garden.auth.service.social;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialUserInfo {
    private String providerId;
    private String email;
    private String name;
    private String profileImageUrl;
}
