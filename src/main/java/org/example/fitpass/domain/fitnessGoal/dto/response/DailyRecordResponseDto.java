package org.example.fitpass.domain.fitnessGoal.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.example.fitpass.common.entity.Image;
import org.example.fitpass.domain.fitnessGoal.entity.DailyRecord;
import org.example.fitpass.domain.fitnessGoal.enums.RecordType;

@Getter
public class DailyRecordResponseDto {

    private final Long id;
    private final Long fitnessGoalId;
    private final RecordType recordType;
    private final List<String> imageUrls;
    private final String memo;
    private final LocalDate recordDate;
    private final LocalDateTime createdAt;

    public DailyRecordResponseDto(Long id, Long fitnessGoalId, RecordType recordType,
        List<String> imageUrls, String memo, LocalDate recordDate, LocalDateTime createdAt) {
        this.id = id;
        this.fitnessGoalId = fitnessGoalId;
        this.recordType = recordType;
        this.imageUrls = imageUrls;
        this.memo = memo;
        this.recordDate = recordDate;
        this.createdAt = createdAt;
    }

    public static DailyRecordResponseDto from(DailyRecord dailyRecord) {
        List<String> imageUrls = dailyRecord.getImages().stream()
            .map(Image::getUrl)
            .collect(Collectors.toList());

        return new DailyRecordResponseDto(
            dailyRecord.getId(),
            dailyRecord.getFitnessGoal().getId(),
            dailyRecord.getRecordType(),
            imageUrls,
            dailyRecord.getMemo(),
            dailyRecord.getRecordDate(),
            dailyRecord.getCreatedAt()
        );
    }
}
