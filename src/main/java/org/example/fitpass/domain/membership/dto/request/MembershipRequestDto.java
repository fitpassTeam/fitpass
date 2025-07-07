package org.example.fitpass.domain.membership.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이용권 등록/수정 요청 DTO")
public record MembershipRequestDto(
    @Schema(description = "이용권 이름", example = "1개월 자유 이용권")
    @NotBlank(message = "이용권 이름은 필수입니다.")
    String name,

    @Schema(description = "이용권 가격 (원 단위)", example = "80000", required = true)
    @Min(value = 10000, message = "이용권 가격은 10,000원 이상이어야 합니다.")
    int price,

    @Schema(description = "이용권 상세 정보", example = "헬스장 자유 이용 가능, 샤워실 이용 포함, 주차 2시간 무료", required = true)
    @NotBlank(message = "이용권 설명은 필수입니다.")
    @Size(min = 10, max = 500, message = "이용권 설명은 10-500자 사이여야 합니다.")
    String content,

    @Schema(description = "이용 가능 일수", example = "30", required = true)
    @Min(value = 1, message = "이용 기간은 최소 1일 이상이어야 합니다.")
    int durationInDays
) {

}
