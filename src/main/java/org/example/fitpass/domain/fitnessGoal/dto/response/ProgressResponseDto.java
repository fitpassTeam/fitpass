package org.example.fitpass.domain.fitnessGoal.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProgressResponseDto {
    private Long fitnessGoalId;
    private double progressRate;
    private Double currentWeight;
    private Double targetWeight;
    private int daysRemaining;

    private List<WeightRecordResponseDto> recentWeightRecords;
    private List<DailyRecordResponseDto> recentDailyRecords;

    private LocalDate lastRecordDate;
    private int consecutiveDays;
}
