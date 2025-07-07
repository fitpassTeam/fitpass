package org.example.fitpass.domain.post.enums;

import java.util.Arrays;
import lombok.Getter;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;

@Getter
public enum PostStatus {

    ACTIVE, DELETED;

    public static PostStatus of(String type) {
        return Arrays.stream(PostStatus.values())
                .filter(r -> r.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new BaseException(ExceptionCode.POST_STATUS_NOT_ACCEPT));
    }
}
