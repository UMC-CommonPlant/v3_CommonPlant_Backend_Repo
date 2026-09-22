package com.commonplant.garden.common.util;

import com.commonplant.garden.auth.exception.AuthErrorCode;
import com.commonplant.garden.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = "test-only-secret-key-that-is-long-enough-for-hs256-signing-123456789";

    @Test
    void refreshTokenContainsRefreshTypeAndSubject() {
        JwtUtil jwtUtil = createJwtUtil(Duration.ofDays(7), Duration.ofDays(1));

        String refreshToken = jwtUtil.generateRefreshToken("user-nano-id");

        assertThat(jwtUtil.getRefreshTokenInfo(refreshToken).nanoId()).isEqualTo("user-nano-id");
    }

    @Test
    void refreshTokenCannotBeUsedAsAccessToken() {
        JwtUtil jwtUtil = createJwtUtil(Duration.ofDays(7), Duration.ofDays(1));
        String refreshToken = jwtUtil.generateRefreshToken("user-nano-id");

        assertThatThrownBy(() -> jwtUtil.getNanoId(refreshToken))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_JWT_TOKEN);
    }

    @Test
    void accessTokenCannotBeUsedAsRefreshToken() {
        JwtUtil jwtUtil = createJwtUtil(Duration.ofDays(7), Duration.ofDays(1));
        String accessToken = jwtUtil.generateAccessToken("user-nano-id");

        assertThatThrownBy(() -> jwtUtil.getRefreshTokenInfo(accessToken))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void refreshTokenIsRenewedWithinConfiguredThreshold() {
        JwtUtil jwtUtil = createJwtUtil(Duration.ofMinutes(1), Duration.ofMinutes(2));
        JwtUtil.RefreshTokenInfo tokenInfo = jwtUtil.getRefreshTokenInfo(jwtUtil.generateRefreshToken("user-nano-id"));

        assertThat(jwtUtil.isRefreshTokenRenewalRequired(tokenInfo)).isTrue();
    }

    private JwtUtil createJwtUtil(Duration refreshTokenExpiry, Duration renewalThreshold) {
        return new JwtUtil(
                SECRET,
                Duration.ofHours(1),
                refreshTokenExpiry,
                Duration.ofMinutes(10),
                renewalThreshold
        );
    }
}
