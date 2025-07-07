package org.example.fitpass.domain.reservation.enums;

import java.util.Arrays;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;

public enum ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED;

    public static ReservationStatus of(String type){
        return Arrays.stream(ReservationStatus.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new BaseException(ExceptionCode.INVALID_RESERVATION_STATUS));
    }
}
