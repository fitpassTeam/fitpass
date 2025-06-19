package org.example.fitpass.domain.gym.dto.response;

import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.domain.gym.enums.GymStatus;

public record GymDetailResponDto(
    String name,
    String number,
    String content,
    String address,
    LocalTime openTime,
    LocalTime closeTime,
    List<String> gymImage,
    List<String> trainerNames,
    GymStatus gymStatus
) {

    public static GymDetailResponDto from(String name, String number, String content,
        String address, LocalTime openTime, LocalTime closeTime, List<String> gymImage,
        List<String> trainerNames, GymStatus gymStatus) {
        return new GymDetailResponDto(
            name,
            number,
            content,
            address,
            openTime,
            closeTime,
            gymImage,
            trainerNames,
            gymStatus
        );
    }
}
