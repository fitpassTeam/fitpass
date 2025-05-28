package org.example.fitpass.domain.gym;

import java.util.Arrays;

public enum GymStatus {
    OPEN, CLOSE;

    public static GymStatus of(String type){
        return Arrays.stream(GymStatus.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new RuntimeException());
    }
}
