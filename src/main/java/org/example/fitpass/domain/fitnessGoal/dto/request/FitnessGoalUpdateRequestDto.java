package org.example.fitpass.domain.fitnessGoal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Getter;

public record FitnessGoalUpdateRequestDto(
    @NotBlank(message = "목표 제목은 필수입니다")
    String title,

    String description,

    @NotNull(message = "목표 체중은 필수입니다")
    @Positive(message = "체중은 양수여야 합니다")
    Double targetWeight,

    @NotNull(message = "종료일은 필수입니다")
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
