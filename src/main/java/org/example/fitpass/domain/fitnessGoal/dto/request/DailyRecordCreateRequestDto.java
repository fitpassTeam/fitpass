package org.example.fitpass.domain.fitnessGoal.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "일일 운동 기록 생성 요청 DTO")
public record DailyRecordCreateRequestDto(

    @Schema(description = "운동 사진 URL 목록", example = "[\"https://s3.amazonaws.com/.../workout1.jpg\"]")
    List<String> imageUrls,

    @Schema(description = "운동 메모", example = "스쿼트 3세트 완료, 오늘 컨디션 좋음")
    String memo,

    @Schema(description = "운동 기록 날짜", example = "2024-12-01")
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
