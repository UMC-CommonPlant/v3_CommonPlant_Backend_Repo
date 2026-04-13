package com.commonplant.garden.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 클라이언트에 반환되는 표준 에러 응답 형식
 *
 * <pre>
 * {
 *   "traceId": "a1b2c3d4",     // 로그 추적용 ID
 *   "status": 400,
 *   "code": "C001",
 *   "message": "입력값이 올바르지 않습니다.",
 *   "timestamp": "2024-04-13T10:30:00",
 *   "errors": [...]             // validation 오류 시에만 포함
 * }
 * </pre>
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {

    private final String traceId;

    private final int status;

    private final String code;

    private final String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd' 'HH:mm:ss")
    private final LocalDateTime timestamp;

    private final List<FieldError> errors;  // validation 오류 상세

    @Builder
    private ErrorResponse(HttpStatus status, String code, String message, List<FieldError> errors) {
        this.traceId   = MDC.get("traceId");    // MDC에서 자동으로 주입
        this.status    = status.value();
        this.code      = code;
        this.message   = message;
        this.timestamp = LocalDateTime.now();
        this.errors    = errors != null ? errors : Collections.emptyList();
    }

    // 기본 에러 (단순 에러코드)
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    // 동적 메시지가 있는 에러
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus())
                .code(errorCode.getCode())
                .message(message)
                .build();
    }

    // Validation 오류 상세 포함
    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(FieldError.of(bindingResult))
                .build();
    }

    // TypeMismatch 오류 상세 포함
    public static ErrorResponse of(ErrorCode errorCode, MethodArgumentTypeMismatchException e) {
        String message = String.format(
                "'%s' 파라미터의 값 '%s'이(가) 올바르지 않습니다. 필요한 타입: %s",
                e.getName(),
                e.getValue(),
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown"
        );

        return ErrorResponse.builder()
                .status(errorCode.getStatus())
                .code(errorCode.getCode())
                .message(message)
                .build();
    }

    @Getter
    public static class FieldError {
        private final String field;
        private final String value;
        private final String reason;

        @Builder
        private FieldError(String field, String value, String reason) {
            this.field  = field;
            this.value  = value;
            this.reason = reason;
        }

        public static List<FieldError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> FieldError.builder()
                            .field(error.getField())
                            .value(error.getRejectedValue() != null
                                    ? error.getRejectedValue().toString()
                                    : "")
                            .reason(error.getDefaultMessage())
                            .build())
                    .collect(Collectors.toList());
        }
    }
}