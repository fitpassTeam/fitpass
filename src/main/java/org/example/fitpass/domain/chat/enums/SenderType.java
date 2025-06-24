package org.example.fitpass.domain.chat.enums;

import java.util.Arrays;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;

public enum SenderType {

    USER, TRAINER;

    public static SenderType of(String type) {
        return Arrays.stream(SenderType.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new BaseException(ExceptionCode.INVALID_SENDER_TYPE));
    }
}


