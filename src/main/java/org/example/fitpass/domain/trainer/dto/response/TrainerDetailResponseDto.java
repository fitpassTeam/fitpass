package org.example.fitpass.domain.trainer.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;

public record TrainerDetailResponseDto(
    String name,
    int price,
    String content,
    String experience,
    TrainerStatus trainerStatus,
    List<String> trainerImage,
    LocalDateTime createdAt
) {

    public static TrainerDetailResponseDto from(String name, int price, String content,
        String experience, TrainerStatus trainerStatus, List<String> trainerImage, LocalDateTime createdAt) {
        return new TrainerDetailResponseDto(
            name,
            price,
            content,
            experience,
            trainerStatus,
            trainerImage,
            createdAt);
    }
}
