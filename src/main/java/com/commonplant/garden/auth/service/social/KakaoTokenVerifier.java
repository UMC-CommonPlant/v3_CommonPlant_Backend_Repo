package com.commonplant.garden.auth.service.social;

import com.commonplant.garden.auth.exception.AuthErrorCode;
import com.commonplant.garden.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Kakao accessToken 검증
 * Flutter kakao_flutter_sdk → accessToken 전달
 * → Kakao 사용자 정보 API 호출로 사용자 정보 조회
 */
@Slf4j
@Component
public class KakaoTokenVerifier {

    @Value("${social.kakao.user-info-url}")
    private String userInfoUrl;

    private final RestClient restClient = RestClient.create();

    @SuppressWarnings("unchecked")
    public SocialUserInfo verify(String accessToken) {
        Map<String, Object> response;
        try {
            response = restClient.get()
                    .uri(userInfoUrl)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            log.warn("Kakao userinfo 4xx: {}", e.getResponseBodyAsString());
            throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        } catch (Exception e) {
            log.error("Kakao token verification failed", e);
            throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        if (response == null) {
            throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        String providerId = String.valueOf(response.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");
        if (kakaoAccount == null) {
            throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        String email = (String) kakaoAccount.get("email");
        if (email == null || email.isBlank()) {
            throw new BusinessException(AuthErrorCode.KAKAO_EMAIL_REQUIRED);
        }

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = profile != null ? (String) profile.getOrDefault("nickname", "") : "";
        String profileImageUrl = profile != null ? (String) profile.getOrDefault("profile_image_url", null) : null;

        return SocialUserInfo.builder()
                .providerId(providerId)
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
