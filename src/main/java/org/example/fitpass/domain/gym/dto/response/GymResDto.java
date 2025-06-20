package org.example.fitpass.domain.gym.dto.response;

import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.domain.gym.entity.Gym;

public record GymResDto(
    String name,
    String number,
    String content,
    String address,
    LocalTime openTime,
    LocalTime closeTime,
    Long gymId,
    List<String> gymImage
) {

    public static GymResDto of(String name, String number, String content, String address,
        LocalTime openTime, LocalTime closeTime, Long gymId) {
        return new GymResDto(name, number, content, address, openTime, closeTime, gymId, List.of());
    }

    public static GymResDto from(Gym gym) {
        return new GymResDto(
            gym.getName(),
            gym.getNumber(),
            gym.getContent(),
            gym.getAddress(),
            gym.getOpenTime(),
            gym.getCloseTime(),
            gym.getId(),
            gym.getImages().stream()
                .map(Image::getUrl)
                .toList()
        );
    }
}
