package com.commonplant.garden.auth.service;

import com.commonplant.garden.auth.dto.AuthRequest;
import com.commonplant.garden.auth.dto.AuthResponse;
import com.commonplant.garden.auth.exception.AuthErrorCode;
import com.commonplant.garden.auth.service.social.GoogleTokenVerifier;
import com.commonplant.garden.auth.service.social.KakaoTokenVerifier;
import com.commonplant.garden.auth.service.social.SocialUserInfo;
import com.commonplant.garden.common.exception.BusinessException;
import com.commonplant.garden.common.util.IdUtil;
import com.commonplant.garden.common.util.JwtUtil;
import com.commonplant.garden.user.entity.User;
import com.commonplant.garden.user.entity.UserRepository;
import com.commonplant.garden.user.enums.Provider;
import com.commonplant.garden.user.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final KakaoTokenVerifier kakaoTokenVerifier;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    /**
     * 소셜 로그인
     * - 기존 유저: JWT 토큰 즉시 발급
     * - 신규 유저: signupToken 발급 (유저 미생성) → /auth/register 로 이동
     */
    @Override
    @Transactional
    public Object login(AuthRequest.Login request) {
        SocialUserInfo socialUser = verifySocialToken(request.getProvider(), request.getToken());
        log.info("{} login verify: providerId={}", request.getProvider(), socialUser.getProviderId());

        return userRepository
                .findByProviderAndProviderIdAndStatus(request.getProvider(), socialUser.getProviderId(), UserStatus.ACTIVE)
                .map(user -> {
                    String accessToken  = jwtUtil.generateAccessToken(user.getNanoId());
                    String refreshToken = jwtUtil.generateRefreshToken(user.getNanoId());
                    user.updateRefreshToken(refreshToken);
                    return (Object) AuthResponse.LoginSuccess.builder()
                            .isNewUser(false)
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build();
                })
                .orElseGet(() -> {
                    if (userRepository.existsByEmail(socialUser.getEmail())) {
                        throw new BusinessException(AuthErrorCode.DUPLICATE_EMAIL);
                    }
                    String signupToken = jwtUtil.generateSignupToken(
                            socialUser.getProviderId(),
                            request.getProvider().name(),
                            socialUser.getEmail()
                    );
                    return AuthResponse.LoginFailed.builder()
                            .isNewUser(true)
                            .signupToken(signupToken)
                            .suggestedName(socialUser.getName())
                            .suggestedImgUrl(socialUser.getProfileImageUrl())
                            .build();
                });
    }

    /**
     * 회원가입 완료
     * signupToken 검증 후 사용자 입력 정보로 유저 생성 → JWT 발급
     */
    @Override
    @Transactional
    public AuthResponse.Register register(AuthRequest.Register request) {
        JwtUtil.SignupTokenInfo info = jwtUtil.getSignupInfo(request.getSignupToken());
        String providerId = info.providerId();
        Provider provider = Provider.from(info.provider());
        String email      = info.email();

        // signupToken 재사용 방지: 이미 가입된 경우 차단
        if (userRepository.existsByProviderAndProviderId(provider, providerId)) {
            throw new BusinessException(AuthErrorCode.ALREADY_REGISTERED);
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .nanoId(IdUtil.generateNanoId())
                .name(request.getName())
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .introduction(request.getIntroduction())
                .build();
        userRepository.save(user);

        String accessToken  = jwtUtil.generateAccessToken(user.getNanoId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getNanoId());
        user.updateRefreshToken(refreshToken);

        return AuthResponse.Register.builder()
                .isNewUser(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // ── helper ──────────────────────────────────────────────────────

    private SocialUserInfo verifySocialToken(Provider provider, String token) {
        return switch (provider) {
            case GOOGLE -> googleTokenVerifier.verify(token);
            case KAKAO  -> kakaoTokenVerifier.verify(token);
            default     -> throw new BusinessException(AuthErrorCode.UNSUPPORTED_PROVIDER);
        };
    }
}
