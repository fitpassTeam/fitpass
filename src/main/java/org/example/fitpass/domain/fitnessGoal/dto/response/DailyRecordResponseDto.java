package org.example.fitpass.domain.fitnessGoal.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.domain.fitnessGoal.entity.DailyRecord;

@Schema(description = "일일 운동 기록 응답 DTO")
public record DailyRecordResponseDto(
    @Schema(description = "일일 기록 ID", example = "1")
    Long dailyRecordId,
    
    @Schema(description = "피트니스 목표 ID", example = "1")
    Long fitnessGoalId,
    
    @Schema(description = "운동 사진 URL 목록", example = "[\"https://s3.amazonaws.com/.../workout1.jpg\"]")
    List<String> imageUrls,
    
    @Schema(description = "운동 메모", example = "스쿼트 3세트 완료, 오늘 컨디션 좋음")
    String memo,
    
    @Schema(description = "운동 기록 날짜", example = "2024-12-01")
    LocalDate recordDate,
    
    @Schema(description = "생성 시간", example = "2024-12-01T19:30:00")
    LocalDateTime createdAt) {

    public static DailyRecordResponseDto from(DailyRecord dailyRecord) {
        List<String> imageUrls = dailyRecord.getImages().stream()
            .map(Image::getUrl)
            .collect(Collectors.toList());

        return new DailyRecordResponseDto(
            dailyRecord.getId(),
            dailyRecord.getFitnessGoal().getId(),
            imageUrls,
            dailyRecord.getMemo(),
            dailyRecord.getRecordDate(),
            dailyRecord.getCreatedAt()
        );
    }
}
