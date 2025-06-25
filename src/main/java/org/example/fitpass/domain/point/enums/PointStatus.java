package org.example.fitpass.domain.point.enums;

import java.util.Arrays;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;

public enum PointStatus {
    PENDING, COMPLETED, CANCELED;

    public static PointStatus of(String type){
        return Arrays.stream(PointStatus.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new BaseException(ExceptionCode.INVALID_POINT_STATUS));
    }
}
