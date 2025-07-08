package org.example.fitpass.domain.trainer.dto.reqeust;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;

@Schema(description = "트레이너 수정 요청 DTO")
public record TrainerUpdateRequestDto(
    @Schema(description = "트레이너 이름", example = "김철수")
    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @Schema(description = "PT 비용 (원 단위)", example = "55000")
    @Min(value = 15000, message = "가격은 15000 이상이어야 합니다.")
    int price,

    @Schema(description = "트레이너 소개 정보", example = "경력 10년의 전문 트레이너로 다양한 운동 프로그램을 제공합니다.")
    @NotBlank(message = "트레이너 정보를 입력해주세요.")
    @Size(max = 1000, message = "트레이너 소개는 1000글자 내로 입력해주세요.")
    String content,

    @Schema(description = "트레이너 경력", example = "헬스 전문 10년, 필라테스 자격증 보유, 국가공인 생활스포츠지도사")
    @NotBlank(message = "경력을 입력해주세요.")
    @Size(max = 1000, message = "트레이너 경력은 1000글자 내로 입력해주세요.")
    String experience,

    @Schema(description = "트레이너 상태", example = "ACTIVE")
    TrainerStatus trainerStatus,

    @Schema(description = "트레이너 사진 URL 목록", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    @NotNull(message = "사진을 골라주세요.")
    List<String> trainerImage

) {

    public TrainerUpdateRequestDto(String name, int price, String content, String experience,
        TrainerStatus trainerStatus, List<String> trainerImage) {
        this.name = name;
        this.price = price;
        this.content = content;
        this.experience = experience;
        this.trainerStatus = trainerStatus;
        this.trainerImage = trainerImage;
    }
}

