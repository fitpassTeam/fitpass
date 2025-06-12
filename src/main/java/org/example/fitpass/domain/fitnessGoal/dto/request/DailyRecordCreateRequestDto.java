package org.example.fitpass.domain.fitnessGoal.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record DailyRecordCreateRequestDto(
    @NotNull(message = "목표 ID는 필수입니다")
    Long fitnessGoalId,

    List<String> imageUrls,

    String memo,

    @NotNull(message = "기록일은 필수입니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate recordDate
) {

    public DailyRecordCreateRequestDto(Long fitnessGoalId,
        List<String> imageUrls, String memo, LocalDate recordDate) {
        this.fitnessGoalId = fitnessGoalId;
        this.imageUrls = imageUrls;
        this.memo = memo;
        this.recordDate = recordDate;
    }
}
