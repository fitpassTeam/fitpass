package org.example.fitpass.domain.fitnessGoal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FitnessGoalUpdateRequestDto {

    @NotBlank(message = "목표 제목은 필수입니다")
    private String title;

    private String description;

    @NotNull(message = "목표 체중은 필수입니다")
    @Positive(message = "체중은 양수여야 합니다")
    private Double targetWeight;

    @NotNull(message = "종료일은 필수입니다")
    private LocalDate endDate;

}
