package org.example.fitpass.domain.fitnessGoal.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import org.example.fitpass.domain.fitnessGoal.enums.RecordType;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class DailyRecordCreateRequestDto {

    @NotNull(message = "목표 ID는 필수입니다")
    private final Long fitnessGoalId;

    @NotNull(message = "기록 타입은 필수입니다")
    private final RecordType recordType;

    private final List<MultipartFile> imageUrls;

    private final String memo;

    @NotNull(message = "기록일은 필수입니다")
    private final LocalDate recordDate;

    public DailyRecordCreateRequestDto(Long fitnessGoalId, RecordType recordType,
        List<MultipartFile> imageUrls, String memo, LocalDate recordDate) {
        this.fitnessGoalId = fitnessGoalId;
        this.recordType = recordType;
        this.imageUrls = imageUrls;
        this.memo = memo;
        this.recordDate = recordDate;
    }
}
