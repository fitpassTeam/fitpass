package org.example.fitpass.domain.post;

import java.util.Arrays;

public enum PostType {
    NOTICE, GENERAL;

    public static PostType of(String type){
        return Arrays.stream(PostType.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new RuntimeException());
    }
}
