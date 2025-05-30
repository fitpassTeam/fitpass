package org.example.fitpass.domain.gym.dto.response;

import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.gym.entity.Gym;

@Getter
@NoArgsConstructor
public class GymResponseDto {
    private String name;
    private String number;
    private String content;
    private String address;
    private LocalTime openTime;
    private LocalTime closeTime;

    public GymResponseDto(String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime) {
        this.name = name;
        this.number = number;
        this.content = content;
        this.address = address;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public static GymResponseDto of(String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime) {
        return new GymResponseDto(name, number, content, address, openTime, closeTime);
    }

    public static GymResponseDto from(Gym gym) {
        return new GymResponseDto(
            gym.getName(),
            gym.getNumber(),
            gym.getContent(),
            gym.getAddress(),
            gym.getOpenTime(),
            gym.getCloseTime()
        );
    }

}
