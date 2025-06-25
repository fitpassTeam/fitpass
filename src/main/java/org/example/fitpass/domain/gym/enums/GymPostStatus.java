package org.example.fitpass.domain.gym.enums;

import java.util.Arrays;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;

public enum GymPostStatus {  // 새로 추가
    PENDING,    // 등록 요청 대기 중
    APPROVED,   // 관리자 승인
    REJECTED;   // 관리자 거절

    public static GymPostStatus of(String status) {
        return Arrays.stream(values())
            .filter(s -> s.name().equalsIgnoreCase(status))
            .findFirst()
            .orElseThrow(() -> new BaseException(ExceptionCode.VALID_STATUS));
    }
}