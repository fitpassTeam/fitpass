package org.example.fitpass.domain.fitnessGoal.dto.response;

import java.time.LocalDate;
import lombok.Getter;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.enums.GoalStatus;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;

@Getter
public class FitnessGoalListResponseDto {
    private final Long id;
    private final String title;
    private final GoalType goalType;
    private final GoalStatus goalStatus;

    private final Double startWeight;
    private final Double targetWeight;
    private final Double currentWeight;

    private final LocalDate endDate;
    private final double progressRate;

    public FitnessGoalListResponseDto (Long id, String title, GoalType goalType,
        GoalStatus goalStatus, Double startWeight,
        Double targetWeight, Double currentWeight,
        LocalDate endDate, double progressRate) {
        this.id = id;
        this.title = title;
        this.goalType = goalType;
        this.goalStatus = goalStatus;
        this.startWeight = startWeight;
        this.targetWeight = targetWeight;
        this.currentWeight = currentWeight;
        this.endDate = endDate;
        this.progressRate = progressRate;
    }

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
