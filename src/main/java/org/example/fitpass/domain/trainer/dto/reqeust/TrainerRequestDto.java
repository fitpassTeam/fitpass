package org.example.fitpass.domain.trainer.dto.reqeust;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "트레이너 등록 요청 DTO")
public record TrainerRequestDto (
    @Schema(description = "트레이너 이름", example = "김철수")
    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @Schema(description = "PT 비용 (원 단위)", example = "50000")
    @Min(value = 50000, message = "가격은 50000 이상이어야 합니다.")
    int price,

    @Schema(description = "트레이너 소개 정보", example = "10년 경력의 전문 트레이너입니다.")
    @NotBlank(message = "트레이너 정보를 입력해주세요.")
    @Size(max = 1000, message = "트레이너 소개는 1000글자 내로 입력해주세요.")
    String content,

    @Schema(description = "트레이너 경력", example = "헬스 전문 10년, 필라테스 자격증 보유")
    @NotBlank(message = "경력을 입력해주세요.")
    @Size(max = 1000, message = "트레이너 경력은 1000글자 내로 입력해주세요.")
    String experience,

    @Schema(description = "트레이너 사진 URL 목록", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    List<String> trainerImage
){
    public TrainerRequestDto(String name, int price, String content, String experience, List<String> trainerImage){
        this.name = name;
        this.price = price;
        this.content = content;
        this.experience = experience;
        this.trainerImage = trainerImage;

    }
}
