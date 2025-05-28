package org.example.fitpass.domain.user;

import java.util.Arrays;

public enum UserRole {
    OWNER, ADMIN, USER;

    public static UserRole of(String role){
        return Arrays.stream(UserRole.values())
            .filter(r -> r.name().equalsIgnoreCase(role))
            .findFirst()
            .orElseThrow(() -> new RuntimeException());
    }
}
