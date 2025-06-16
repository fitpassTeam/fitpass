package org.example.fitpass.domain.trainer.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import org.example.fitpass.common.Image.dto.ImageDto;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;

public record TrainerDetailResponseDto (
    String name,
    int price,
    String content,
    TrainerStatus trainerStatus,
    List<ImageDto> trainerImage,
    LocalDateTime createdAt
){

    public static TrainerDetailResponseDto from(String name, int price, String content,
        TrainerStatus trainerStatus, List<Image> trainerImage, LocalDateTime createdAt) {
        List<ImageDto> imageDto = trainerImage.stream()
            .map(ImageDto::from)
            .toList();
        return new TrainerDetailResponseDto(name, price, content, trainerStatus, imageDto,
            createdAt);
    }
}
