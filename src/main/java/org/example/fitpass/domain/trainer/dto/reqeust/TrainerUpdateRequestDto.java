package org.example.fitpass.domain.trainer.dto.reqeust;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.Image;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerUpdateRequestDto {

    private List<Image> trainerImage;
    @NotBlank(message = "이름은 필수입니다.")
    private String name;
    @Min(value = 15000, message = "가격은 15000 이상이어야 합니다.")
    private int price;
    @NotBlank(message = "트레이너 정보를 입력해주세요.")
    @Size(max = 200, message = "트레이너 소개는 200글자 내로 입력해주세요.")
    private String content;
    private TrainerStatus trainerStatus;

}
