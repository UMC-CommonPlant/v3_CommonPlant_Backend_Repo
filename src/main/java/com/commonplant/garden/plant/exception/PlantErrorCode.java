package com.commonplant.garden.plant.exception;

import com.commonplant.garden.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlantErrorCode implements ErrorCode {

    PLANT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "식물을 찾을 수 없습니다."),
    PLACE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P002", "장소 접근 권한이 없습니다."),
    NO_FIELDS_TO_UPDATE(HttpStatus.BAD_REQUEST, "P003", "수정할 식물 정보가 없습니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "P004", "식물 애칭이 올바르지 않습니다."),
    INVALID_IMAGE_KEY(HttpStatus.BAD_REQUEST, "P005", "식물 이미지가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
