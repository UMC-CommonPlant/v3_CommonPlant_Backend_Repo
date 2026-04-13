package com.commonplant.garden.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 공통 에러 클래스 (도메인 별 Enum 분리)
 */

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    // 400
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST,             "C001", "입력값이 올바르지 않습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST,              "C002", "요청 타입이 올바르지 않습니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST,       "C003", "필수 요청 파라미터가 누락되었습니다."),

    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,                   "C004", "인증이 필요합니다."),

    // 403
    ACCESS_DENIED(HttpStatus.FORBIDDEN,                     "C005", "접근 권한이 없습니다."),

    // 404
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND,                  "C006", "요청한 리소스를 찾을 수 없습니다."),

    // 405
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,       "C007", "허용되지 않는 HTTP 메서드입니다."),

    // 409
    CONFLICT(HttpStatus.CONFLICT,                           "C008", "요청이 현재 서버 상태와 충돌합니다."),

    // 429
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS,         "C009", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C010", "서버 내부 오류가 발생했습니다.");
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}