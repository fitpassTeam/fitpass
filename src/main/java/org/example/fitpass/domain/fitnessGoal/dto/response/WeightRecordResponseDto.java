package org.example.fitpass.domain.fitnessGoal.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.fitpass.domain.fitnessGoal.entity.WeightRecord;

public record WeightRecordResponseDto(
    Long weightRecordId,
    Long fitnessGoalId,
    Double weight,
    LocalDate recordDate,
    String memo,
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
