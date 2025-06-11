package org.example.fitpass.domain.fitnessGoal.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class WeightRecordCreateRequestDto {

    @NotNull(message = "목표 ID는 필수입니다")
    private final Long fitnessGoalId;

    @NotNull(message = "체중은 필수입니다")
    @Positive(message = "체중은 양수여야 합니다")
    private final Double weight;

    @NotNull(message = "기록일은 필수입니다")
    private final LocalDate recordDate;

    private final String memo;

    public WeightRecordCreateRequestDto(Long fitnessGoalId, Double weight, LocalDate recordDate,
        String memo) {
        this.fitnessGoalId = fitnessGoalId;
        this.weight = weight;
        this.recordDate = recordDate;
        this.memo = memo;
    }
}
