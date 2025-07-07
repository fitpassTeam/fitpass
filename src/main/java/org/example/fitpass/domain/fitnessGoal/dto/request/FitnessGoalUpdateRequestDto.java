package org.example.fitpass.domain.fitnessGoal.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "피트니스 목표 수정 요청 DTO")
public record FitnessGoalUpdateRequestDto(
    @Schema(description = "수정할 목표 제목", example = "8kg 감량하기")
    @NotBlank(message = "목표 제목은 필수입니다")
    String title,

    @Schema(description = "수정할 목표 설명", example = "건강한 다이어트를 통해 8kg 감량")
    String description,

    @Schema(description = "수정할 목표 체중 (kg)", example = "62.5")
    @NotNull(message = "목표 체중은 필수입니다")
    @Positive(message = "체중은 양수여야 합니다")
    Double targetWeight,

    @Schema(description = "수정할 목표 종료일", example = "2025-08-01")
    @NotNull(message = "종료일은 필수입니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate
) {

    public FitnessGoalUpdateRequestDto(String title, String description, Double targetWeight,
        LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.targetWeight = targetWeight;
        this.endDate = endDate;
    }
}
