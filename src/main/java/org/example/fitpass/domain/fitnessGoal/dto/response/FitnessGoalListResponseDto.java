package org.example.fitpass.domain.fitnessGoal.dto.response;

import java.time.LocalDate;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.enums.GoalStatus;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;

public record FitnessGoalListResponseDto(
    Long id,
    String title,
    GoalType goalType,
    GoalStatus goalStatus,
    Double startWeight,
    Double targetWeight,
    Double currentWeight,
    LocalDate endDate,
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
