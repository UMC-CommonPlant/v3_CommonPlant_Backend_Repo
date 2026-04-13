package com.commonplant.garden.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 400 - @Valid, @Validated 바인딩 에러
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        log.warn("[{}] {} - Validation failed: {}",
                request.getMethod(), request.getRequestURI(), e.getMessage());

        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, e.getBindingResult()));
    }

    /**
     * 400 - @ModelAttribute 바인딩 에러
     */
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ErrorResponse> handleBindException(
            BindException e, HttpServletRequest request) {

        log.warn("[{}] {} - Bind failed: {}",
                request.getMethod(), request.getRequestURI(), e.getMessage());

        ErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, e.getBindingResult()));
    }

    /**
     * 400 - 요청 파라미터 타입 불일치
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {

        log.warn("[{}] {} - Type mismatch: {}",
                request.getMethod(), request.getRequestURI(), e.getMessage());

        ErrorCode errorCode = CommonErrorCode.INVALID_TYPE_VALUE;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, e));
    }

    /**
     * 400 - 필수 파라미터 누락
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e, HttpServletRequest request) {

        log.warn("[{}] {} - Missing parameter: {}",
                request.getMethod(), request.getRequestURI(), e.getMessage());

        String message = String.format("'%s' 파라미터는 필수입니다.", e.getParameterName());
        ErrorCode errorCode = CommonErrorCode.MISSING_REQUEST_PARAMETER;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, message));
    }

    /**
     * 401 - 인증 실패 (Spring Security)
     */
    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException e, HttpServletRequest request) {

        log.warn("[{}] {} - Authentication failed: {}",
                request.getMethod(), request.getRequestURI(), e.getMessage());

        ErrorCode errorCode = CommonErrorCode.UNAUTHORIZED;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    /**
     * 403 - 인가 실패 (Spring Security)
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {

        log.warn("[{}] {} - Access denied: {}",
                request.getMethod(), request.getRequestURI(), e.getMessage());

        ErrorCode errorCode = CommonErrorCode.ACCESS_DENIED;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    /**
     * 404 - 핸들러 없음
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException e, HttpServletRequest request) {

        log.warn("[{}] {} - No handler found",
                request.getMethod(), request.getRequestURI());

        ErrorCode errorCode = CommonErrorCode.ENTITY_NOT_FOUND;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    /**
     * 405 - 허용되지 않는 HTTP Method
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {

        log.warn("[{}] {} - Method not allowed",
                request.getMethod(), request.getRequestURI());

        ErrorCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    /**
     * 비즈니스 예외 — 모든 도메인 예외 통합 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e, HttpServletRequest request) {

        // 4xx → warn, 5xx → error 로 로그 레벨 분리
        if (e.getErrorCode().is5xxError()) {
            log.error("[{}] {} - BusinessException [{}]: {}",
                    request.getMethod(), request.getRequestURI(),
                    e.getErrorCode().getCode(), e.getMessage(), e.getCause());
        } else {
            log.warn("[{}] {} - BusinessException [{}]: {}",
                    request.getMethod(), request.getRequestURI(),
                    e.getErrorCode().getCode(), e.getMessage());
        }

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, e.getMessage()));
    }

    /**
     * 500 - 그 외 모든 예외 (예상치 못한 서버 에러)
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(
            Exception e, HttpServletRequest request) {

        log.error("[{}] {} - Unhandled Exception: ",
                request.getMethod(), request.getRequestURI(), e);

        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }
}