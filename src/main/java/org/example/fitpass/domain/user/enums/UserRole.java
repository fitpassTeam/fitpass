package org.example.fitpass.domain.user.enums;

import java.util.Arrays;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;

public enum UserRole {
    OWNER, PENDING_OWNER, ADMIN, USER;

    public static UserRole of(String role){
        return Arrays.stream(UserRole.values())
            .filter(r -> r.name().equalsIgnoreCase(role))
            .findFirst()
            .orElseThrow(() -> new BaseException(ExceptionCode.INVALID_USER_ROLE));
    }
}
