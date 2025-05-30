package org.example.fitpass.domain.trainer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;

@Getter
@AllArgsConstructor

public class TrainerResponseDto {

    private String name;
    private int price;
    private String content;
    private TrainerStatus trainerStatus;


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
