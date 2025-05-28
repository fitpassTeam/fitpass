package org.example.fitpass.domain.point.enums;

import java.util.Arrays;

public enum PointType {
    CHARGE, USE, REFUND;

    public static PointType of(String type){
        return Arrays.stream(PointType.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new RuntimeException());
    }
}
