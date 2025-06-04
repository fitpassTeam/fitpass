package org.example.fitpass.domain.post.enums;

import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;

import java.util.Arrays;

public enum PostType {

    NOTICE, GENERAL;

    public static PostType of(String type) {
        return Arrays.stream(PostType.values())
                .filter(r -> r.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new BaseException(ExceptionCode.POST_TYPE_NOT_ACCEPT));
    }
}
