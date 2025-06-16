package org.example.fitpass.domain.trainer.dto.response;

import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;

public record TrainerResponseDto (
    String name,
    int price,
    String content,
    TrainerStatus trainerStatus
){

    public static TrainerResponseDto fromEntity(Trainer trainer) {
        return new TrainerResponseDto(
            trainer.getName(),
            trainer.getPrice(),
            trainer.getContent(),
            trainer.getTrainerStatus()
        );
    }

    public static TrainerResponseDto of(String name, int price, String content,
        TrainerStatus trainerStatus) {
        return new TrainerResponseDto(name, price, content, trainerStatus);
    }

}
