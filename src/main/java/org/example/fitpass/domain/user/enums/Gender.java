package org.example.fitpass.domain.user.enums;

import java.util.Arrays;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;

public enum Gender {
    MAN, WOMAN, NONE;

    public static Gender of(String gender){
        return Arrays.stream(Gender.values())
            .filter(r -> r.name().equalsIgnoreCase(gender))
            .findFirst()
            .orElseThrow(() -> new BaseException(ExceptionCode.INVALID_GENDER));
    }
}
