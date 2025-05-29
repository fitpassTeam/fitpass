package org.example.fitpass.domain.trainer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.trainer.TrainerStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class TrainerReqeustDto {
    private String trainerImage;
    private String name;
    private int price;
    private String content;
    private TrainerStatus trainerStatus;
}
