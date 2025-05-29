package org.example.fitpass.domain.gym.dto.response;

import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.Image;
import org.example.fitpass.domain.gym.enums.GymStatus;

@Getter
@NoArgsConstructor
public class GymDetailResponDto {
    private String name;
    private String number;
    private String content;
    private String address;
    private LocalTime openTime;
    private LocalTime closeTime;
    private List<Image> gymImage;
    private List<String> trainerNames;
    private GymStatus gymStatus;

    public GymDetailResponDto(String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, List<Image> gymImage, List<String> trainerNames, GymStatus gymStatus) {
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

    public static GymDetailResponDto from(String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, List<Image> gymImage, List<String> trainerNames, GymStatus gymStatus) {
        return new GymDetailResponDto(name, number, content, address, openTime, closeTime, gymImage, trainerNames, gymStatus);
    }

}
