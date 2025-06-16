package org.example.fitpass.domain.gym.dto.response;

import java.time.LocalTime;

import org.example.fitpass.domain.gym.entity.Gym;

public record GymResponseDto(
    String name,
    String number,
    String content,
    String address,
    LocalTime openTime,
    LocalTime closeTime,
    Long gymId
) {

    public static GymResponseDto of(String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, Long gymId) {
        return new GymResponseDto(name, number, content, address, openTime, closeTime, gymId);
    }

    public static GymResponseDto from(Gym gym) {
        return new GymResponseDto(
            gym.getName(),
            gym.getNumber(),
            gym.getContent(),
            gym.getAddress(),
            gym.getOpenTime(),
            gym.getCloseTime(),
            gym.getId()
        );
    }
}