package org.example.fitpass.domain.fitnessGoal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;

public record FitnessGoalCreateRequestDto(
    @NotBlank(message = "목표 제목은 필수입니다")
    String title,

    String description,

    @NotNull(message = "목표 타입은 필수입니다")
    GoalType goalType,

    @NotNull(message = "시작 체중은 필수입니다")
    @Positive(message = "체중은 양수여야 합니다")
    Double startWeight,

    @NotNull(message = "목표 체중은 필수입니다")
    @Positive(message = "체중은 양수여야 합니다")
    Double targetWeight,

    @NotNull(message = "시작일은 필수입니다")
    LocalDate startDate,

    @NotNull(message = "종료일은 필수입니다")
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
