package org.example.fitpass.fitnessGoal;

import static org.hamcrest.Matchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.fitnessGoal.dto.response.WeightRecordResponseDto;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.entity.WeightRecord;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;
import org.example.fitpass.domain.fitnessGoal.repository.FitnessGoalRepository;
import org.example.fitpass.domain.fitnessGoal.repository.WeightRecordRepository;
import org.example.fitpass.domain.fitnessGoal.service.WeightRecordService;
import org.example.fitpass.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WeightRecordServiceTest {

    @Mock
    private WeightRecordRepository weightRecordRepository;

    @Mock
    private FitnessGoalRepository fitnessGoalRepository;

    @InjectMocks
    private WeightRecordService weightRecordService;

    private FitnessGoal fitnessGoal;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@test.com", "tester", "GOOGLE");
        ReflectionTestUtils.setField(user, "id", 1L);

        fitnessGoal = FitnessGoal.of(
            user,
            "목표",
            "desc",
            GoalType.WEIGHT_LOSS,
            80.0,
            70.0,
            LocalDate.now().minusMonths(1),
            LocalDate.now().plusMonths(1)
        );
        ReflectionTestUtils.setField(fitnessGoal, "id", 10L);
    }

    @Test
    void createWeightRecord_success() {
        Double weight = 75.5;
        LocalDate recordDate = LocalDate.now();
        String memo = "좋은 컨디션";

        WeightRecord record = WeightRecord.of(fitnessGoal, weight, recordDate, memo);
        ReflectionTestUtils.setField(record, "id", 100L);

        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(fitnessGoal.getId(), user.getId()))
            .willReturn(fitnessGoal);

        given(weightRecordRepository.existsByFitnessGoalIdAndRecordDate(fitnessGoal.getId(), recordDate))
            .willReturn(false);

        given(weightRecordRepository.save(Mockito.any(WeightRecord.class))).willReturn(record);

        WeightRecordResponseDto responseDto = weightRecordService.createWeightRecord(
            fitnessGoal.getId(), weight, recordDate, memo, user.getId());

        assertNotNull(responseDto);
        assertEquals(100L, responseDto.weightRecordId());
        verify(weightRecordRepository).save(Mockito.any(WeightRecord.class));
        verify(fitnessGoalRepository).findByIdAndUserIdOrElseThrow(fitnessGoal.getId(), user.getId());
    }

    @Test
    void createWeightRecord_fail_duplicatedDate() {
        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(fitnessGoal.getId(), user.getId()))
            .willReturn(fitnessGoal);

        given(weightRecordRepository.existsByFitnessGoalIdAndRecordDate(fitnessGoal.getId(), LocalDate.now()))
            .willReturn(true);

        BaseException ex = assertThrows(BaseException.class, () ->
            weightRecordService.createWeightRecord(
                fitnessGoal.getId(), 70.0, LocalDate.now(), "memo", user.getId()));

        assertEquals(ExceptionCode.WEIGHT_RECORD_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    void getWeightRecord_success() {
        WeightRecord record = WeightRecord.of(fitnessGoal, 75.0, LocalDate.now(), "memo");
        ReflectionTestUtils.setField(record, "id", 100L);

        given(weightRecordRepository.findByIdOrElseThrow(100L)).willReturn(record);

        WeightRecordResponseDto dto = weightRecordService.getWeightRecord(
            user.getId(), fitnessGoal.getId(), 100L);

        assertEquals(100L, dto.weightRecordId());
    }

    @Test
    void getWeightRecord_fail_notOwner() {
        User otherUser = new User("other@test.com", "other", "GOOGLE");
        ReflectionTestUtils.setField(otherUser, "id", 99L);

        FitnessGoal otherGoal = FitnessGoal.of(
            otherUser,
            "목표",
            "desc",
            GoalType.WEIGHT_LOSS,
            80.0,
            70.0,
            LocalDate.now().minusMonths(1),
            LocalDate.now().plusMonths(1)
        );
        ReflectionTestUtils.setField(otherGoal, "id", fitnessGoal.getId());

        WeightRecord record = WeightRecord.of(otherGoal, 75.0, LocalDate.now(), "memo");
        ReflectionTestUtils.setField(record, "id", 100L);

        given(weightRecordRepository.findByIdOrElseThrow(100L)).willReturn(record);

        BaseException ex = assertThrows(BaseException.class, () ->
            weightRecordService.getWeightRecord(user.getId(), fitnessGoal.getId(), 100L));

        assertEquals(ExceptionCode.NOT_WEIGHT_RECORD_OWNER, ex.getErrorCode());
    }

    // 추가로 updateWeightRecord, deleteWeightRecord 테스트도 유사하게 작성하세요.
}

