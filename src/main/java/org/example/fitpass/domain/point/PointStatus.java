package org.example.fitpass.domain.point;

import java.util.Arrays;

public enum PointStatus {
    PENDING, COMPLETED, CANCELED;

    public static PointStatus of(String type){
        return Arrays.stream(PointStatus.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new RuntimeException());
    }
}
