package org.example.fitpass.domain.trainer.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.fitpass.domain.trainer.TrainerStatus;
import org.example.fitpass.domain.trainer.entity.Trainer;

@Getter
@AllArgsConstructor

public class TrainerResponseDto {
    private long id;
    private String trainerImage;
    private String name;
    private int price;
    private String content;
    private TrainerStatus trainerStatus;
    private LocalDateTime createdAt;

    public static TrainerResponseDto fromEntity(Trainer trainer){
        return new TrainerResponseDto(
                trainer.getId(),
                trainer.getTrainerImage(),
                trainer.getName(),
                trainer.getPrice(),
                trainer.getContent(),
                trainer.getTrainerStatus(),
                trainer.getCreatedAt()
        );
    }
}
