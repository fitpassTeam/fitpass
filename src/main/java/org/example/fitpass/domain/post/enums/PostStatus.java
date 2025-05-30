package org.example.fitpass.domain.post.enums;

import java.util.Arrays;

public enum PostStatus {

    ACTIVE, HOLIDAY, DELETED;

    public static PostStatus of(String type) {
        return Arrays.stream(PostStatus.values())
                .filter(r -> r.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("정확한 상태를 기입해주세요."));
    }
}
