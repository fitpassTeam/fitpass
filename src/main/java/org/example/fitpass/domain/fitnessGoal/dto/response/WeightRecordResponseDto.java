package org.example.fitpass.domain.fitnessGoal.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import org.example.fitpass.domain.fitnessGoal.entity.WeightRecord;

@Getter
public class WeightRecordResponseDto {

    private final Long id;
    private final Long fitnessGoalId;
    private final Double weight;
    private final LocalDate recordDate;
    private final String memo;
    private final LocalDateTime createdAt;

    public WeightRecordResponseDto(Long id, Long fitnessGoalId, Double weight, LocalDate recordDate,
        String memo, LocalDateTime createdAt) {
        this.id = id;
        this.fitnessGoalId = fitnessGoalId;
        this.weight = weight;
        this.recordDate = recordDate;
        this.memo = memo;
        this.createdAt = createdAt;
    }

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
