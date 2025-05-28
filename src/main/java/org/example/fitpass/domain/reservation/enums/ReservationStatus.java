package org.example.fitpass.domain.reservation.enums;

import java.util.Arrays;

public enum ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED;

    public static ReservationStatus of(String type){
        return Arrays.stream(ReservationStatus.values())
            .filter(r -> r.name().equalsIgnoreCase(type))
            .findFirst()
            .orElseThrow(() -> new RuntimeException());
    }
}
