package org.example.fitpass.domain.chat.enums;

import java.util.Arrays;

public enum SenderType {

    USER, TRAINER;

    public static SenderType of(String type) {
        return Arrays.stream(SenderType.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new RuntimeException());
    }
}


