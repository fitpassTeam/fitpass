package org.example.fitpass.domain.fitnessGoal.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "체중 기록 생성 요청 DTO")
public record WeightRecordCreateRequestDto(

    @Schema(description = "체중 (kg)", example = "68.5")
    @NotNull(message = "체중은 필수입니다")
    @Positive(message = "체중은 양수여야 합니다")
    Double weight,

    @Schema(description = "기록 날짜", example = "2024-12-01")
    @NotNull(message = "기록일은 필수입니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate recordDate,

    @Schema(description = "메모", example = "아침 공복 체중")
    String memo) {

    public WeightRecordCreateRequestDto( Double weight, LocalDate recordDate,
        String memo) {
        this.weight = weight;
        this.recordDate = recordDate;
        this.memo = memo;
    }
}
