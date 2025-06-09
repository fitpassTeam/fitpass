package org.example.fitpass.domain.fitnessGoal.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.enums.GoalStatus;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;

@Getter
@NoArgsConstructor
public class FitnessGoalResponseDto {

    private Long id;
    private String title;
    private String description;
    private GoalType goalType;
    private GoalStatus goalStatus;

    private Double startWeight;
    private Double targetWeight;
    private Double currentWeight;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime achievementDate;

    private double progressRate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FitnessGoalResponseDto(Long id, String title, String description,
        GoalType goalType, GoalStatus goalStatus,
        Double startWeight, Double targetWeight, Double currentWeight,
        LocalDate startDate, LocalDate endDate, LocalDateTime achievementDate,
        double progressRate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.goalType = goalType;
        this.goalStatus = goalStatus;
        this.startWeight = startWeight;
        this.targetWeight = targetWeight;
        this.currentWeight = currentWeight;
        this.startDate = startDate;
        this.endDate = endDate;
        this.achievementDate = achievementDate;
        this.progressRate = progressRate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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
