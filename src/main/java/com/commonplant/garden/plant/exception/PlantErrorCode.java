package com.commonplant.garden.plant.exception;

import com.commonplant.garden.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlantErrorCode implements ErrorCode {

    PLANT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "식물을 찾을 수 없습니다."),
    PLACE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "P002", "장소 접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
