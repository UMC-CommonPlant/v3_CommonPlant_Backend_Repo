package com.commonplant.garden.user.enums;

import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.user.exception.UserErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

public enum Provider {
    GOOGLE,
    APPLE,
    KAKAO ;

    // Provider 검증 로직
    @JsonCreator
    public static Provider from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Stream.of(Provider.values())
                .filter(provider -> provider.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new BusinessException(UserErrorCode.PROVIDER_NOT_FOUND));
    }
}