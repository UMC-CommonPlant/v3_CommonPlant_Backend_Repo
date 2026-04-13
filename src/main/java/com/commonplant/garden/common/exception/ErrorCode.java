package com.commonplant.garden.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 모든 도메인의 ErrorCode enum이 구현해야 하는 Interface
 * - 기존 단일 enum 대신 interface를 정의
 * - 각 도메인이 독립적으로 ErrorCode를 관리 가능
 */
public interface ErrorCode {
    String getCode();
    String getMessage();
    HttpStatus getStatus();

    default boolean is4xxError() {
        return getStatus().is4xxClientError();
    }

    default boolean is5xxError() {
        return getStatus().is5xxServerError();
    }
}