package com.commonplant.garden.auth.service;

import com.commonplant.garden.auth.dto.AuthRequest;
import com.commonplant.garden.auth.dto.AuthResponse;
import com.commonplant.garden.auth.exception.AuthErrorCode;
import com.commonplant.garden.auth.service.social.GoogleTokenVerifier;
import com.commonplant.garden.auth.service.social.KakaoTokenVerifier;
import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.common.util.JwtUtil;
import com.commonplant.garden.s3.service.S3Service;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.entity.UserRepository;
import com.commonplant.garden.user.enums.Provider;
import com.commonplant.garden.user.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String NANO_ID = "user-nano-id";
    private static final String REFRESH_TOKEN = "stored-refresh-token";

    @Mock private GoogleTokenVerifier googleTokenVerifier;
    @Mock private KakaoTokenVerifier kakaoTokenVerifier;
    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private S3Service s3Service;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void refresh_returnsOnlyAccessToken_whenRefreshTokenIsNotNearExpiry() {
        User user = createUser(REFRESH_TOKEN);
        JwtUtil.RefreshTokenInfo tokenInfo = tokenInfo();
        given(jwtUtil.getRefreshTokenInfo(REFRESH_TOKEN)).willReturn(tokenInfo);
        given(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).willReturn(Optional.of(user));
        given(jwtUtil.isRefreshTokenRenewalRequired(tokenInfo)).willReturn(false);
        given(jwtUtil.generateAccessToken(NANO_ID)).willReturn("new-access-token");

        AuthResponse.RefreshResponse response = authService.refresh(new AuthRequest.Refresh(REFRESH_TOKEN));

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isNull();
        verify(jwtUtil, never()).generateRefreshToken(NANO_ID);
    }

    @Test
    void refresh_rotatesAndStoresRefreshToken_whenItIsNearExpiry() {
        User user = createUser(REFRESH_TOKEN);
        JwtUtil.RefreshTokenInfo tokenInfo = tokenInfo();
        given(jwtUtil.getRefreshTokenInfo(REFRESH_TOKEN)).willReturn(tokenInfo);
        given(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).willReturn(Optional.of(user));
        given(jwtUtil.isRefreshTokenRenewalRequired(tokenInfo)).willReturn(true);
        given(jwtUtil.generateRefreshToken(NANO_ID)).willReturn("new-refresh-token");
        given(jwtUtil.generateAccessToken(NANO_ID)).willReturn("new-access-token");

        AuthResponse.RefreshResponse response = authService.refresh(new AuthRequest.Refresh(REFRESH_TOKEN));

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(user.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void refresh_rejectsTokenThatDoesNotMatchStoredToken() {
        User user = createUser("another-refresh-token");
        given(jwtUtil.getRefreshTokenInfo(REFRESH_TOKEN)).willReturn(tokenInfo());
        given(userRepository.findByNanoIdAndStatus(NANO_ID, UserStatus.ACTIVE)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refresh(new AuthRequest.Refresh(REFRESH_TOKEN)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);

        verify(jwtUtil, never()).generateAccessToken(NANO_ID);
    }

    private JwtUtil.RefreshTokenInfo tokenInfo() {
        return new JwtUtil.RefreshTokenInfo(NANO_ID, new Date(System.currentTimeMillis() + 60_000));
    }

    private User createUser(String refreshToken) {
        User user = User.builder()
                .nanoId(NANO_ID)
                .name("홍길동")
                .email("user@example.com")
                .provider(Provider.GOOGLE)
                .providerId("provider-id")
                .build();
        user.updateRefreshToken(refreshToken);
        return user;
    }
}
