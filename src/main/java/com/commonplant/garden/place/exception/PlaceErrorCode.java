package com.commonplant.garden.place.exception;

import com.commonplant.garden.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceErrorCode implements ErrorCode {

    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "P101", "장소를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "P102", "사용자를 찾을 수 없습니다."),
    USER_NOT_ON_PLACE(HttpStatus.FORBIDDEN, "P103", "해당 장소에 속한 사용자가 아닙니다."),
    USER_ALREADY_ON_PLACE(HttpStatus.CONFLICT, "P104", "이미 해당 장소에 참여한 사용자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
