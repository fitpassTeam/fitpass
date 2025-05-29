package org.example.fitpass.domain.trainer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.trainer.TrainerStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class TrainerReqeustDto {

    private String trainerImage;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Min(value = 15000, message = "가격은 15000 이상이어야 합니다.")
    private int price;

    @NotBlank(message = "정보는 필수입니다.")
    private String content;

    @NotBlank(message = "현재상태는 필수입니다.")
    private TrainerStatus trainerStatus;
}
