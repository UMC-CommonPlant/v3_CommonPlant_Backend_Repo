package com.commonplant.garden.common.util;

import java.util.UUID;

public class UUIDUtil {

    private UUIDUtil() {}

    /**
     * 하이픈 없는 랜덤 UUID 생성 (32자)
     * 신규 유저 생성 시 uuid 필드에 사용
     *
     * @return "a1b2c3d4e5f6..." 형태의 32자 문자열
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 표준 하이픈 포함 UUID 생성 (36자)
     *
     * @return "a1b2c3d4-e5f6-..." 형태의 36자 문자열
     */
    public static String generateUuidWithHyphen() {
        return UUID.randomUUID().toString();
    }

    /**
     * 문자열이 유효한 UUID 형식인지 검증
     * 하이픈 포함(36자) / 미포함(32자) 모두 허용
     *
     * @param value 검증할 문자열
     * @return 유효한 UUID 형식이면 true
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        try {
            String normalized = value.length() == 32
                    ? value.replaceFirst(
                    "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                    "$1-$2-$3-$4-$5")
                    : value;
            UUID.fromString(normalized);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}