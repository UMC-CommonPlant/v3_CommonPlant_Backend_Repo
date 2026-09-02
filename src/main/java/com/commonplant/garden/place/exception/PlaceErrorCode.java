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
    USER_ALREADY_ON_PLACE(HttpStatus.CONFLICT, "P104", "이미 해당 장소에 참여한 사용자입니다."),
    NO_FIELDS_TO_UPDATE(HttpStatus.BAD_REQUEST, "P105", "수정할 장소 정보가 없습니다."),
    INVALID_IMAGE_KEY(HttpStatus.BAD_REQUEST, "P106", "장소 이미지가 올바르지 않습니다."),

    // createPlace validation
    PLACE_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "P107", "장소 이름은 최대 10자까지 가능합니다."),
    PLACE_ADDRESS_REQUIRED(HttpStatus.BAD_REQUEST, "P108", "장소 주소는 필수입니다."),
    PLACE_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "P109", "장소 이름은 필수입니다."),

    // deletePlace validation
    NOT_PLACE_OWNER(HttpStatus.FORBIDDEN, "P110", "장소는 팀짱만 삭제할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
