package org.example.fitpass.domain.fitnessGoal.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;

@Schema(description = "피트니스 목표 생성 요청 DTO")
public record FitnessGoalCreateRequestDto(
    @Schema(description = "목표 제목", example = "10kg 감량하기")
    @NotBlank(message = "목표 제목은 필수입니다")
    String title,

    @Schema(description = "목표 설명", example = "건강한 다이어트를 통해 10kg 감량")
    String description,

    @Schema(description = "목표 타입", example = "WEIGHT_LOSS")
    @NotNull(message = "목표 타입은 필수입니다")
    GoalType goalType,

    @Schema(description = "시작 체중 (kg)", example = "70.5")
    @NotNull(message = "시작 체중은 필수입니다")
    @Positive(message = "체중은 양수여야 합니다")
    Double startWeight,

    @Schema(description = "목표 체중 (kg)", example = "60.5")
    @NotNull(message = "목표 체중은 필수입니다")
    @Positive(message = "체중은 양수여야 합니다")
    Double targetWeight,

    @Schema(description = "목표 시작일", example = "2024-12-01")
    @NotNull(message = "시작일은 필수입니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate,

    @Schema(description = "목표 종료일", example = "2025-06-01")
    @NotNull(message = "종료일은 필수입니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate
) {

    public FitnessGoalCreateRequestDto(String title, String description, GoalType goalType,
        Double startWeight, Double targetWeight, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.goalType = goalType;
        this.startWeight = startWeight;
        this.targetWeight = targetWeight;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
