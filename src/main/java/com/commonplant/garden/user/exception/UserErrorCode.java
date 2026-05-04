package com.commonplant.garden.user.exception;

import com.commonplant.garden.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 공통 에러 클래스 (도메인 별 Enum 분리)
 */

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "U001", "잘못된 입력 값입니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}