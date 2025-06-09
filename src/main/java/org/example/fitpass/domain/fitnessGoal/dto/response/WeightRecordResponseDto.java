package org.example.fitpass.domain.fitnessGoal.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.fitnessGoal.entity.WeightRecord;

@Getter
@NoArgsConstructor
public class WeightRecordResponseDto {

    private Long id;
    private Long fitnessGoalId;
    private Double weight;
    private LocalDate recordDate;
    private String memo;
    private LocalDateTime createdAt;

    public static WeightRecordResponseDto from(WeightRecord weightRecord) {
        WeightRecordResponseDto dto = new WeightRecordResponseDto();
        dto.id = weightRecord.getId();
        dto.fitnessGoalId = weightRecord.getFitnessGoal().getId();
        dto.weight = weightRecord.getWeight();
        dto.recordDate = weightRecord.getRecordDate();
        dto.memo = weightRecord.getMemo();
        dto.createdAt = weightRecord.getCreatedAt();
        return dto;
    }

}
