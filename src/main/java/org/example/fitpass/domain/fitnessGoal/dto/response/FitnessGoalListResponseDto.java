package org.example.fitpass.domain.fitnessGoal.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.enums.GoalStatus;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;

@Schema(description = "피트니스 목표 목록 응답 DTO")
public record FitnessGoalListResponseDto(
    @Schema(description = "피트니스 목표 ID", example = "1")
    Long fitnessGoalId,
    
    @Schema(description = "목표 제목", example = "10kg 감량하기")
    String title,
    
    @Schema(description = "목표 타입", example = "WEIGHT_LOSS")
    GoalType goalType,
    
    @Schema(description = "목표 상태", example = "IN_PROGRESS")
    GoalStatus goalStatus,
    
    @Schema(description = "시작 체중 (kg)", example = "70.5")
    Double startWeight,
    
    @Schema(description = "목표 체중 (kg)", example = "60.5")
    Double targetWeight,
    
    @Schema(description = "현재 체중 (kg)", example = "65.0")
    Double currentWeight,
    
    @Schema(description = "목표 종료일", example = "2025-06-01")
    LocalDate endDate,
    
    @Schema(description = "진행률 (%)", example = "55.0")
    Double progressRate) {

    public static FitnessGoalListResponseDto from(FitnessGoal goal) {
        return new FitnessGoalListResponseDto(
            goal.getId(),
            goal.getTitle(),
            goal.getGoalType(),
            goal.getGoalStatus(),
            goal.getStartWeight(),
            goal.getTargetWeight(),
            goal.getCurrentWeight(),
            goal.getEndDate(),
            goal.calculateProgressRate()
        );
    }
}
