package com.commonplant.garden.auth.exception;

import com.commonplant.garden.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 소셜 토큰입니다."),
    EXPIRED_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "만료된 소셜 토큰입니다."),
    INVALID_JWT_TOKEN   (HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN   (HttpStatus.UNAUTHORIZED, "A004", "만료된 JWT 토큰입니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST,  "A005", "지원하지 않는 소셜 제공자입니다."),
    USER_NOT_FOUND      (HttpStatus.NOT_FOUND,    "A006", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL     (HttpStatus.CONFLICT,      "A007", "이미 다른 소셜 계정으로 가입된 이메일입니다."),
    KAKAO_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST,   "A008", "카카오 계정의 이메일 제공에 동의해주세요."),
    ALREADY_REGISTERED  (HttpStatus.CONFLICT,      "A011", "이미 가입된 소셜 계정입니다."),
    UNAUTHORIZED        (HttpStatus.UNAUTHORIZED,  "A009", "인증이 필요합니다."),
    ACCESS_DENIED       (HttpStatus.FORBIDDEN,     "A010", "접근 권한이 없습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}