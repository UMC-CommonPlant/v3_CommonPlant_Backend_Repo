package com.commonplant.garden.common.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.util.UUID;

public class IdUtil {

    private IdUtil() {}

    /*
     * NanoID
     */
    public static String generateNanoId() {
        return NanoIdUtils.randomNanoId(
                NanoIdUtils.DEFAULT_NUMBER_GENERATOR,
                NanoIdUtils.DEFAULT_ALPHABET,
                12
        );
    }

    /*
     * UUID
     */
    // 하이픈 없는 랜덤 UUID 생성 (32자)
    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 표준 하이픈 포함 UUID 생성 (36자)
    public static String generateUuidWithHyphen() {
        return UUID.randomUUID().toString();
    }

    // 문자열이 유효한 UUID 형식인지 검증
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