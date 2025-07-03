package org.example.fitpass.domain.gym.dto.response;

import java.time.LocalTime;
import org.example.fitpass.domain.gym.enums.GymPostStatus;

public record GymStatusResponseDto(
    String name,
    String number,
    String content,
    String address,
    LocalTime openTime,
    LocalTime closeTime,
    Long gymId,
    String summary,
    GymPostStatus gymPostStatus
) {

    public static GymStatusResponseDto of(String name, String number, String content, String fullAddress,
        LocalTime openTime, LocalTime closeTime, Long gymId, String summary, GymPostStatus gymPostStatus) {
        return new GymStatusResponseDto(name, number, content, fullAddress, openTime, closeTime, gymId, summary, gymPostStatus);
    }

}