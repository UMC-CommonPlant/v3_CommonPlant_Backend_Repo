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
 * Google idToken 검증
 * Flutter google_sign_in SDK → idToken 전달
 * → Google tokeninfo API 호출로 검증
 */
@Slf4j
@Component
public class GoogleTokenVerifier {

    @Value("${social.google.token-info-url}")
    private String tokenInfoUrl;

    private final RestClient restClient = RestClient.create();

    public SocialUserInfo verify(String idToken) {
        Map<String, Object> claims;
        try {
            claims = restClient.get()
                    .uri(tokenInfoUrl + "?id_token=" + idToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (HttpClientErrorException e) {
            log.warn("Google tokeninfo 4xx: {}", e.getResponseBodyAsString());
            throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        } catch (Exception e) {
            log.error("Google token verification failed", e);
            throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        if (claims == null || claims.containsKey("error")) {
            throw new BusinessException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
        }

        long exp = Long.parseLong(claims.get("exp").toString());
        if (exp < System.currentTimeMillis() / 1000) {
            throw new BusinessException(AuthErrorCode.EXPIRED_SOCIAL_TOKEN);
        }

        return SocialUserInfo.builder()
                .providerId((String) claims.get("sub"))
                .email((String) claims.get("email"))
                .nickname((String) claims.getOrDefault("name", ""))
                .profileImageUrl((String) claims.getOrDefault("picture", null))
                .build();
    }
}
