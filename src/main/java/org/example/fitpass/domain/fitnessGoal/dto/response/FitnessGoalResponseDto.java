package org.example.fitpass.domain.fitnessGoal.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.enums.GoalStatus;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;

@Schema(description = "피트니스 목표 응답 DTO")
public record FitnessGoalResponseDto(
    @Schema(description = "피트니스 목표 ID", example = "1")
    Long fitnessGoalId,
    
    @Schema(description = "목표 제목", example = "10kg 감량하기")
    String title,
    
    @Schema(description = "목표 설명", example = "건강한 다이어트를 통해 10kg 감량")
    String description,
    
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
    
    @Schema(description = "목표 시작일", example = "2024-12-01")
    LocalDate startDate,
    
    @Schema(description = "목표 종료일", example = "2025-06-01")
    LocalDate endDate,
    
    @Schema(description = "목표 달성일", example = "2025-05-15T10:30:00")
    LocalDateTime achievementDate,
    
    @Schema(description = "진행률 (%)", example = "55.0")
    Double progressRate,
    
    @Schema(description = "생성 시간", example = "2024-12-01T09:00:00")
    LocalDateTime createdAt,
    
    @Schema(description = "수정 시간", example = "2024-12-15T14:30:00")
    LocalDateTime updatedAt) {

    public static FitnessGoalResponseDto from(FitnessGoal fitnessGoal) {
        return new FitnessGoalResponseDto(
            fitnessGoal.getId(),
            fitnessGoal.getTitle(),
            fitnessGoal.getDescription(),
            fitnessGoal.getGoalType(),
            fitnessGoal.getGoalStatus(),
            fitnessGoal.getStartWeight(),
            fitnessGoal.getTargetWeight(),
            fitnessGoal.getCurrentWeight(),
            fitnessGoal.getStartDate(),
            fitnessGoal.getEndDate(),
            fitnessGoal.getAchievementDate(),
            fitnessGoal.calculateProgressRate(),
            fitnessGoal.getCreatedAt(),
            fitnessGoal.getUpdatedAt()
        );
    }

}
