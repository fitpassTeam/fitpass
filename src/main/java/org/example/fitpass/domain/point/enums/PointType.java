package org.example.fitpass.domain.point.enums;

import java.util.Arrays;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;

public enum PointType {
    CHARGE, USE, REFUND, CASH_OUT;

    public static PointType of(String type){
        return Arrays.stream(PointType.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new BaseException(ExceptionCode.INVALID_POINT_TYPE));
    }
}
