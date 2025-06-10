package org.example.fitpass.domain.trainer.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.entity.Image;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;

@Getter
@NoArgsConstructor
public class TrainerDetailResponseDto {

    private String name;
    private int price;
    private String content;
    private TrainerStatus trainerStatus;
    private List<Image> trainerImage;
    private LocalDateTime createdAt;

    public TrainerDetailResponseDto(String name, int price, String content,
        TrainerStatus trainerStatus, List<Image> trainerImage, LocalDateTime createdAt) {
        this.name = name;
        this.price = price;
        this.content = content;
        this.trainerStatus = trainerStatus;
        this.trainerImage = trainerImage;
        this.createdAt = createdAt;
    }

    public static TrainerDetailResponseDto from(String name, int price, String content,
        TrainerStatus trainerStatus, List<Image> trainerImage, LocalDateTime createdAt) {
        return new TrainerDetailResponseDto(name, price, content, trainerStatus, trainerImage,
            createdAt);
    }
}
