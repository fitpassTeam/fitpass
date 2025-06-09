package org.example.fitpass.domain.fitnessGoal.dto.response;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.enums.GoalStatus;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;

@Getter
@NoArgsConstructor
public class FitnessGoalListResponseDto {
    private Long id;
    private String title;
    private GoalType goalType;
    private GoalStatus goalStatus;

    private Double startWeight;
    private Double targetWeight;
    private Double currentWeight;

    private LocalDate endDate;
    private double progressRate;

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
