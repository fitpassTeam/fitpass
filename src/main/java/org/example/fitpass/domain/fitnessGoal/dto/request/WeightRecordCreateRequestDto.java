package org.example.fitpass.domain.fitnessGoal.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Getter;

public record WeightRecordCreateRequestDto(

    @NotNull(message = "체중은 필수입니다")
    @Positive(message = "체중은 양수여야 합니다")
    Double weight,

    @NotNull(message = "기록일은 필수입니다")
    LocalDate recordDate,

    String memo) {

    public WeightRecordCreateRequestDto( Double weight, LocalDate recordDate,
        String memo) {
        this.weight = weight;
        this.recordDate = recordDate;
        this.memo = memo;
    }
}
