package org.example.fitpass.domain.gym.dto.response;

import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.gym.entity.Gym;

@Getter
public class GymResponseDto {
    private final String name;
    private final String number;
    private final String content;
    private final String address;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final Long gymId;

    public GymResponseDto(String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, Long gymId) {
        this.name = name;
        this.number = number;
        this.content = content;
        this.address = address;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.gymId = gymId;
    }

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
