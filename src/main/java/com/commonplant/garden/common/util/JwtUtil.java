package com.commonplant.garden.common.util;

import com.commonplant.garden.auth.exception.AuthErrorCode;
import com.commonplant.garden.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    private static final String CLAIM_TYPE     = "type";
    private static final String TYPE_SIGNUP    = "SIGNUP";
    private static final String CLAIM_PROVIDER = "provider";
    private static final String CLAIM_EMAIL    = "email";

    /** signupToken 파싱 결과를 담는 record */
    public record SignupTokenInfo(String providerId, String provider, String email) {}

    private final SecretKey secretKey;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;
    private final long signupTokenExpiry;

    public JwtUtil(
            @Value("${jwt.secret}")                String secret,
            @Value("${jwt.access-token-expiry}")   long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}")  long refreshTokenExpiry,
            @Value("${jwt.signup-token-expiry}")   long signupTokenExpiry
    ) {
        this.secretKey         = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry  = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
        this.signupTokenExpiry  = signupTokenExpiry;
    }

    // ── 토큰 생성 ────────────────────────────────────────────────────

    public String generateAccessToken(String nanoId) {
        return buildToken(nanoId, accessTokenExpiry);
    }

    public String generateRefreshToken(String nanoId) {
        return buildToken(nanoId, refreshTokenExpiry);
    }

    /** 회원가입 전용 단기 토큰 — providerId를 subject로, type=SIGNUP claim 포함 */
    public String generateSignupToken(String providerId, String provider, String email) {
        return buildToken(providerId, signupTokenExpiry, Map.of(
                CLAIM_TYPE, TYPE_SIGNUP,
                CLAIM_PROVIDER, provider,
                CLAIM_EMAIL, email
        ));
    }

    // ── 토큰 검증 & 파싱 ─────────────────────────────────────────────

    /**
     * accessToken에서 nanoId 추출.
     * signupToken을 넘기면 INVALID_JWT_TOKEN 예외 발생 — 인증 필터 보호용.
     */
    public String getNanoId(String token) {
        Claims claims = validateAndGetClaims(token);
        if (TYPE_SIGNUP.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new BusinessException(AuthErrorCode.INVALID_JWT_TOKEN);
        }
        return claims.getSubject();
    }

    /**
     * signupToken 검증 후 파싱 결과 반환.
     * type=SIGNUP 이 아니면 INVALID_JWT_TOKEN 예외 발생.
     */
    public SignupTokenInfo getSignupInfo(String signupToken) {
        Claims claims = validateAndGetClaims(signupToken);
        if (!TYPE_SIGNUP.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new BusinessException(AuthErrorCode.INVALID_JWT_TOKEN);
        }
        return new SignupTokenInfo(
                claims.getSubject(),
                claims.get(CLAIM_PROVIDER, String.class),
                claims.get(CLAIM_EMAIL, String.class)
        );
    }

    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(AuthErrorCode.EXPIRED_JWT_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.INVALID_JWT_TOKEN);
        }
    }

    // ── 내부 유틸 ────────────────────────────────────────────────────

    private String buildToken(String subject, long expiryMs) {
        return buildToken(subject, expiryMs, Map.of());
    }

    private String buildToken(String subject, long expiryMs, Map<String, String> extraClaims) {
        Date now = new Date();
        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs));
        extraClaims.forEach(builder::claim);
        return builder.signWith(secretKey).compact();
    }

    public long getAccessTokenExpirySeconds() { return accessTokenExpiry / 1000; }
}
