package org.example.fitpass.domain.post.enums;

import java.util.Arrays;

public enum PostType {

    NOTICE, GENERAL;

    public static PostType of(String type) {
        return Arrays.stream(PostType.values())
                .filter(r -> r.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("정확한 종류를 선택해주세요."));
    }
}
