package org.example.fitpass.domain.trainer.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.fitpass.domain.trainer.TrainerStatus;
import org.example.fitpass.domain.trainer.entity.Trainer;

@Getter
@AllArgsConstructor
@Builder
public class TrainerResponseDto {
    private long id;
    private String trainerImage;
    private String name;
    private int price;
    private String content;
    private TrainerStatus trainerStatus;
    private LocalDateTime createdAt;

    public static TrainerResponseDto fromEntity(Trainer trainer){
        return TrainerResponseDto.builder()
            .id(trainer.getId())
            .trainerImage(trainer.getTrainerImage())
            .name(trainer.getName())
            .price(trainer.getPrice())
            .content(trainer.getContent())
            .trainerStatus(trainer.getTrainerStatus())
            .createdAt(trainer.getCreatedAt())
            .build();
    }
}
