package org.example.fitpass.domain.trainer.dto.reqeust;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.example.fitpass.common.Image.entity.Image;


public record TrainerRequestDto (
    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @Min(value = 15000, message = "가격은 15000 이상이어야 합니다.")
    int price,

    @NotBlank(message = "트레이너 정보를 입력해주세요.")
    @Size(max = 200, message = "트레이너 소개는 200글자 내로 입력해주세요.")
    String content,

    List<Image> trainerImage
){
    public TrainerRequestDto(String name, int price, String content, List<Image> trainerImage){
        this.name = name;
        this.price = price;
        this.content = content;
        this.trainerImage = trainerImage;

    }
}




