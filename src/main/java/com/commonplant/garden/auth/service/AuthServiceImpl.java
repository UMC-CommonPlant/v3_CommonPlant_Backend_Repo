package com.commonplant.garden.auth.service;

import com.commonplant.garden.auth.dto.request.AuthRequest;
import com.commonplant.garden.auth.dto.response.AuthResponse;
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

    @Override
    @Transactional
    public AuthResponse googleLogin(AuthRequest.GoogleLogin request) {
        SocialUserInfo socialUser = googleTokenVerifier.verify(request.getIdToken());
        log.info("google Login verify : " + socialUser);
        return loginOrRegister(socialUser, Provider.GOOGLE);
    }

    @Override
    @Transactional
    public AuthResponse kakaoLogin(AuthRequest.KakaoLogin request) {
        SocialUserInfo socialUser = kakaoTokenVerifier.verify(request.getAccessToken());
        log.info("kakao Login verify : " + socialUser);
        return loginOrRegister(socialUser, Provider.KAKAO);
    }

    // ── helper ──────────────────────────────────────────────────────

    private AuthResponse loginOrRegister(SocialUserInfo socialUser, Provider provider) {
        boolean isNewUser = false;
        User user = userRepository
                .findByProviderAndProviderIdAndStatus(provider, socialUser.getProviderId(), UserStatus.ACTIVE)
                .orElse(null);

        if (user == null) {
            if (userRepository.existsByEmail(socialUser.getEmail())) {
                throw new BusinessException(AuthErrorCode.DUPLICATE_EMAIL);
            }
            user = User.builder()
                    .nanoId(IdUtil.generateNanoId())
                    .name(socialUser.getNickname())
                    .email(socialUser.getEmail())
                    .provider(provider)
                    .providerId(socialUser.getProviderId())
                    .imgUrl(socialUser.getProfileImageUrl())
                    .introduction(null)
                    .build();
            userRepository.save(user);
            isNewUser = true;
        }

        String accessToken = jwtUtil.generateAccessToken(user.getNanoId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getNanoId());
        user.updateRefreshToken(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNewUser(isNewUser)
                .build();
    }
}
