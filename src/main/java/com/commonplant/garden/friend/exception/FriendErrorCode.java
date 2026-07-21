package com.commonplant.garden.friend.exception;

import com.commonplant.garden.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FriendErrorCode implements ErrorCode {

    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "F201", "친구 요청을 찾을 수 없습니다."),
    USER_ALREADY_ON_PLACE(HttpStatus.CONFLICT, "F202", "이미 해당 장소에 참여한 사용자입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
