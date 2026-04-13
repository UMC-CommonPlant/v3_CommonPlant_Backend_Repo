package com.order.poteto.common.exception.dto;

import com.order.poteto.common.exception.ErrorCode;

import lombok.Getter;

/**
 * 모든 비즈니스 예외의 기본 클래스
 * - ErrorCode를 interface로 받아서 모든 도메인의 ErrorCode 처리 가능
 * - ErrorCode만으로 예외 유형 구분 가능
 */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    protected BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}