package org.example.fitpass.domain.fitnessGoal.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.fitnessGoal.enums.RecordType;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public class DailyRecordCreateRequestDto {

    @NotNull(message = "목표 ID는 필수입니다")
    private Long fitnessGoalId;

    @NotNull(message = "기록 타입은 필수입니다")
    private RecordType recordType;

    private List<MultipartFile> imageUrls;

    private String memo;

    @NotNull(message = "기록일은 필수입니다")
    private LocalDate recordDate;

}
