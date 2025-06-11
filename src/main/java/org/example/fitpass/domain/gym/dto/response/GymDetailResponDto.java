package org.example.fitpass.domain.gym.dto.response;

import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import org.example.fitpass.domain.gym.enums.GymStatus;

@Getter
public class GymDetailResponDto {
    private final String name;
    private final String number;
    private final String content;
    private final String address;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final List<String> gymImage;
    private final List<String> trainerNames;
    private final GymStatus gymStatus;

    public GymDetailResponDto(String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, List<String> gymImage, List<String> trainerNames, GymStatus gymStatus) {
        this.name = name;
        this.number = number;
        this.content = content;
        this.address = address;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.gymImage = gymImage;
        this.trainerNames = trainerNames;
        this.gymStatus = gymStatus;
    }

    public static GymDetailResponDto from(String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, List<String> gymImage, List<String> trainerNames, GymStatus gymStatus) {
        return new GymDetailResponDto(name, number, content, address, openTime, closeTime, gymImage, trainerNames, gymStatus);
    }

}
