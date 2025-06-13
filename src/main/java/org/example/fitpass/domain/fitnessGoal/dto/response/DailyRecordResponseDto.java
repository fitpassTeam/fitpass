package org.example.fitpass.domain.fitnessGoal.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.domain.fitnessGoal.entity.DailyRecord;

public record DailyRecordResponseDto(
    Long id,
    Long fitnessGoalId,
    List<String> imageUrls,
    String memo,
    LocalDate recordDate,
    LocalDateTime createdAt) {

    public static DailyRecordResponseDto from(DailyRecord dailyRecord) {
        List<String> imageUrls = dailyRecord.getImages().stream()
            .map(Image::getUrl)
            .collect(Collectors.toList());

        return new DailyRecordResponseDto(
            dailyRecord.getId(),
            dailyRecord.getFitnessGoal().getId(),
            imageUrls,
            dailyRecord.getMemo(),
            dailyRecord.getRecordDate(),
            dailyRecord.getCreatedAt()
        );
    }
}
