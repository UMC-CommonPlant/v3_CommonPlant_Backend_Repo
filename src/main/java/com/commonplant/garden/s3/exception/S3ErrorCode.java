package com.commonplant.garden.s3.exception;

import com.commonplant.garden.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum S3ErrorCode implements ErrorCode {

    INVALID_IMAGE_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "S001", "지원하지 않는 이미지 타입입니다."),
    TOO_MANY_IMAGES(HttpStatus.BAD_REQUEST, "S002", "이미지 개수 제한을 초과했습니다."),
    INVALID_IMAGE_SIZE(HttpStatus.BAD_REQUEST, "S003", "이미지 크기 제한을 초과했습니다."),
    INVALID_IMAGE_KEY(HttpStatus.BAD_REQUEST, "S004", "이미지 키가 올바르지 않습니다."),
    IMAGE_NOT_UPLOADED(HttpStatus.BAD_REQUEST, "S005", "업로드된 이미지를 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "S006", "이미지를 찾을 수 없습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S007", "이미지 업로드에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
