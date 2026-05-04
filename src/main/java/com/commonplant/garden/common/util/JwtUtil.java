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

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtUtil(
            @Value("${jwt.secret}")               String secret,
            @Value("${jwt.access-token-expiry}")  long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry
    ) {
        this.secretKey          = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry  = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
    }

    public String generateAccessToken(String nanoId) {
        return buildToken(nanoId, accessTokenExpiry);
    }

    public String generateRefreshToken(String nanoId) {
        return buildToken(nanoId, refreshTokenExpiry);
    }

    private String buildToken(String nanoId, long expiryMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(nanoId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMs))
                .signWith(secretKey)
                .compact();
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

    public String getNanoId(String token)           { return validateAndGetClaims(token).getSubject(); }
    public long getAccessTokenExpirySeconds()     { return accessTokenExpiry / 1000; }
}
