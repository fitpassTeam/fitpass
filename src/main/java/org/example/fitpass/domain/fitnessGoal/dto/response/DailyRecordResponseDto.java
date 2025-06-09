package org.example.fitpass.domain.fitnessGoal.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.Image;
import org.example.fitpass.domain.fitnessGoal.entity.DailyRecord;
import org.example.fitpass.domain.fitnessGoal.enums.RecordType;

@Getter
@NoArgsConstructor
public class DailyRecordResponseDto {

    private Long id;
    private Long fitnessGoalId;
    private RecordType recordType;
    private List<String> imageUrls;
    private String memo;
    private LocalDate recordDate;
    private LocalDateTime createdAt;

    public static DailyRecordResponseDto from(DailyRecord dailyRecord) {
        DailyRecordResponseDto dto = new DailyRecordResponseDto();
        dto.id = dailyRecord.getId();
        dto.fitnessGoalId = dailyRecord.getFitnessGoal().getId();
        dto.recordType = dailyRecord.getRecordType();
        dto.imageUrls = dailyRecord.getImages().stream()
            .map(Image::getUrl) // Image 엔티티에 getUrl() 메서드 필요
            .collect(Collectors.toList());
        dto.memo = dailyRecord.getMemo();
        dto.recordDate = dailyRecord.getRecordDate();
        dto.createdAt = dailyRecord.getCreatedAt();
        return dto;
    }
}
