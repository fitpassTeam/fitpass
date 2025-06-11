package org.example.fitpass.domain.gym.enums;

import java.util.Arrays;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;

public enum GymStatus {
    OPEN, CLOSE;

    public static GymStatus of(String type){
        return Arrays.stream(GymStatus.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new BaseException(ExceptionCode.VALID_STATUS));
    }
}
