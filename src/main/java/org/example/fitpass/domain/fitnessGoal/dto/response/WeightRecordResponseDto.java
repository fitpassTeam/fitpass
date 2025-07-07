package org.example.fitpass.domain.fitnessGoal.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.fitpass.domain.fitnessGoal.entity.WeightRecord;

@Schema(description = "체중 기록 응답 DTO")
public record WeightRecordResponseDto(
    @Schema(description = "체중 기록 ID", example = "1")
    Long weightRecordId,
    
    @Schema(description = "피트니스 목표 ID", example = "1")
    Long fitnessGoalId,
    
    @Schema(description = "체중 (kg)", example = "68.5")
    Double weight,
    
    @Schema(description = "기록 날짜", example = "2024-12-01")
    LocalDate recordDate,
    
    @Schema(description = "메모", example = "아침 공복 체중")
    String memo,
    
    @Schema(description = "생성 시간", example = "2024-12-01T07:30:00")
    LocalDateTime createdAt) {

    public static WeightRecordResponseDto from(WeightRecord weightRecord) {
        return new WeightRecordResponseDto(
            weightRecord.getId(),
            weightRecord.getFitnessGoal().getId(),
            weightRecord.getWeight(),
            weightRecord.getRecordDate(),
            weightRecord.getMemo(),
            weightRecord.getCreatedAt()
        );
    }

}
