package com.commonplant.garden.auth.exception;

import com.commonplant.garden.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "U001", "유효하지 않은 소셜 토큰입니다."),
    EXPIRED_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "U002", "만료된 소셜 토큰입니다."),
    INVALID_JWT_TOKEN   (HttpStatus.UNAUTHORIZED, "U003", "유효하지 않은 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN   (HttpStatus.UNAUTHORIZED, "U004", "만료된 JWT 토큰입니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST,  "U005", "지원하지 않는 소셜 제공자입니다."),
    USER_NOT_FOUND      (HttpStatus.NOT_FOUND,    "U006", "사용자를 찾을 수 없습니다."),
    UNAUTHORIZED (HttpStatus.UNAUTHORIZED, "U003", "인증이 필요합니다."),
    ACCESS_DENIED (HttpStatus.FORBIDDEN, "U007", "접근 권한이 없습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}