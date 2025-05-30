package org.example.fitpass.domain.trainer.enums;

import java.util.Arrays;

public enum TrainerStatus {
    ACTIVE, HOLIDAY, DELETED;

    public static TrainerStatus of(String type){
        return Arrays.stream(TrainerStatus.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new RuntimeException());
    }
}
