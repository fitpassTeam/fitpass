package org.example.fitpass.domain.fitnessGoal.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.enums.GoalStatus;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;

public record FitnessGoalResponseDto(
    Long fitnessGoalId,
    String title,
    String description,
    GoalType goalType,
    GoalStatus goalStatus,
    Double startWeight,
    Double targetWeight,
    Double currentWeight,
    LocalDate startDate,
    LocalDate endDate,
    LocalDateTime achievementDate,
    Double progressRate,
    LocalDateTime createdAt,
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
