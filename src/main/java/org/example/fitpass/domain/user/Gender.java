package org.example.fitpass.domain.user;

import java.util.Arrays;

public enum Gender {
    MAN, WOMEN, NONE;

    public static Gender of(String gender){
        return Arrays.stream(Gender.values())
            .filter(r -> r.name().equalsIgnoreCase(gender))
            .findFirst()
            .orElseThrow(() -> new RuntimeException());
    }
}
