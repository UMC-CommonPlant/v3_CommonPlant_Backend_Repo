package com.commonplant.garden.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외 기본 클래스
 *
 * <pre>
 * 사용 예시:
 *   throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
 *   throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL, "test@email.com은 이미 사용 중입니다.");
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 동적 메시지가 필요한 경우 (ex. 어떤 값이 중복인지 명시)
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 외부 연동 실패 등 원인 예외를 함께 기록해야 하는 경우
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}