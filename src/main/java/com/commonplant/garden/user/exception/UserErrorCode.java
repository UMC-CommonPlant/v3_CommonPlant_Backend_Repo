package com.commonplant.garden.user.exception;

import com.commonplant.garden.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    // entity error code
    PROVIDER_NOT_FOUND(HttpStatus.BAD_REQUEST, "U001", "지원하지 않는 소셜 로그인 Provider입니다"),
    INVALID_SEARCH_KEYWORD(HttpStatus.BAD_REQUEST, "U002", "지원하지 않는 키워드 형식입니다."),

    // validation error code
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U101", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U102", "이미 사용 중인 이메일입니다."),
    DUPLICATE_PROVIDER(HttpStatus.CONFLICT, "U103", "이미 가입된 소셜 계정입니다."),


    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}