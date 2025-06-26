package org.example.fitpass.domain.trainer.dto.response;

import java.util.List;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;

public record TrainerResponseDto (
    Long id,
    String name,
    int price,
    String content,
    TrainerStatus trainerStatus,
    String experience,
    List<String> images
){

    public static TrainerResponseDto fromEntity(Trainer trainer) {
        return new TrainerResponseDto(
            trainer.getId(),
            trainer.getName(),
            trainer.getPrice(),
            trainer.getContent(),
            trainer.getTrainerStatus(),
            trainer.getExperience(),
            trainer.getImages().stream()
                .map(Image::getUrl)
                .toList()
        );
    }

    public static TrainerResponseDto of(Long id, String name, int price, String content,
        TrainerStatus trainerStatus, String experience, List<String> images) {
        return new TrainerResponseDto(id, name, price, content, trainerStatus, experience, images);
    }

}
