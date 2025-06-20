package org.example.fitpass.domain.fitnessGoal.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record DailyRecordCreateRequestDto(

    List<String> imageUrls,

    String memo,

    @NotNull(message = "기록일은 필수입니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate recordDate
) {

    public DailyRecordCreateRequestDto(
        List<String> imageUrls, String memo, LocalDate recordDate) {
        this.imageUrls = imageUrls;
        this.memo = memo;
        this.recordDate = recordDate;
    }
}
