package org.example.fitpass.fitnessGoal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.s3.service.S3Service;
import org.example.fitpass.domain.fitnessGoal.dto.response.DailyRecordResponseDto;
import org.example.fitpass.domain.fitnessGoal.entity.DailyRecord;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;
import org.example.fitpass.domain.fitnessGoal.repository.DailyRecordRepository;
import org.example.fitpass.domain.fitnessGoal.repository.FitnessGoalRepository;
import org.example.fitpass.domain.fitnessGoal.service.DailyRecordService;
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
class DailyRecordServiceTest {

    @Mock
    DailyRecordRepository dailyRecordRepository;

    @Mock
    FitnessGoalRepository fitnessGoalRepository;

    @Mock
    S3Service s3Service;

    @InjectMocks
    DailyRecordService dailyRecordService;

    User user = new User("email@test.com", "홍길동", "GOOGLE");
    FitnessGoal fitnessGoal;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(user, "id", 1L);
        fitnessGoal = FitnessGoal.of(user, "목표", "desc", GoalType.WEIGHT_LOSS,
            80.0, 70.0, LocalDate.now(), LocalDate.now().plusMonths(1));
        ReflectionTestUtils.setField(fitnessGoal, "id", 10L);
    }

    @Test
    void createDailyRecord_success() {
        // given
        Long fitnessGoalId = 10L;
        Long userId = 1L;
        LocalDate recordDate = LocalDate.now();
        List<String> imageUrls = List.of("url1", "url2");
        String memo = "운동 열심히 했어요";

        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(fitnessGoalId, userId)).willReturn(fitnessGoal);
        given(dailyRecordRepository.existsByFitnessGoalIdAndRecordDate(fitnessGoalId, recordDate)).willReturn(false);
        DailyRecord savedRecord = DailyRecord.of(imageUrls, fitnessGoal, recordDate, memo);
        ReflectionTestUtils.setField(savedRecord, "id", 100L);
        given(dailyRecordRepository.save(Mockito.any(DailyRecord.class))).willReturn(savedRecord);

        // when
        DailyRecordResponseDto response = dailyRecordService.createDailyRecord(fitnessGoalId, imageUrls, memo, recordDate, userId);

        // then
        assertEquals(100L, response.id());
        assertEquals(memo, response.memo());
        assertEquals(imageUrls.size(), response.imageUrls().size());
        verify(fitnessGoalRepository).findByIdAndUserIdOrElseThrow(fitnessGoalId, userId);
        verify(dailyRecordRepository).save(Mockito.any(DailyRecord.class));
    }

    @Test
    void createDailyRecord_fail_duplicate() {
        // given
        Long fitnessGoalId = 10L;
        Long userId = 1L;
        LocalDate recordDate = LocalDate.now();
        List<String> imageUrls = List.of("url1");
        String memo = "중복 기록";

        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(fitnessGoalId, userId)).willReturn(fitnessGoal);
        given(dailyRecordRepository.existsByFitnessGoalIdAndRecordDate(fitnessGoalId, recordDate)).willReturn(true);

        // when & then
        BaseException ex = assertThrows(BaseException.class, () -> {
            dailyRecordService.createDailyRecord(fitnessGoalId, imageUrls, memo, recordDate, userId);
        });
        assertEquals(ExceptionCode.DAILY_RECORD_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    void getDailyRecords_success() {
        // given
        Long fitnessGoalId = 10L;
        Long userId = 1L;
        DailyRecord record = DailyRecord.of(List.of("url"), fitnessGoal, LocalDate.now(), "memo");
        ReflectionTestUtils.setField(record, "id", 200L);
        List<DailyRecord> records = List.of(record);

        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(fitnessGoalId, userId)).willReturn(fitnessGoal);
        given(dailyRecordRepository.findByFitnessGoalIdOrderByRecordDateDesc(fitnessGoalId)).willReturn(records);

        // when
        List<DailyRecordResponseDto> response = dailyRecordService.getDailyRecords(fitnessGoalId, userId);

        // then
        assertEquals(1, response.size());
        assertEquals("memo", response.get(0).memo());
    }

    @Test
    void getDailyRecord_success() {
        // given
        Long fitnessGoalId = 10L;
        Long dailyRecordId = 200L;
        Long userId = 1L;

        DailyRecord record = spy(DailyRecord.of(List.of("url"), fitnessGoal, LocalDate.now(), "memo"));
        ReflectionTestUtils.setField(record, "id", dailyRecordId);

        given(dailyRecordRepository.findByIdOrElseThrow(dailyRecordId)).willReturn(record);
        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(fitnessGoalId, userId)).willReturn(fitnessGoal);

        // when
        DailyRecordResponseDto response = dailyRecordService.getDailyRecord(dailyRecordId, fitnessGoalId, userId);

        // then
        assertEquals(dailyRecordId, response.id());
        verify(record, times(2)).getFitnessGoal();
    }

    @Test
    void getDailyRecord_fail_notOwner() {
        // given
        Long fitnessGoalId = 10L;
        Long dailyRecordId = 200L;
        Long userId = 2L; // 다른 사용자

        User otherUser = new User("other@test.com", "타인", "GOOGLE");
        ReflectionTestUtils.setField(otherUser, "id", userId);

        FitnessGoal otherGoal = FitnessGoal.of(otherUser, "다른 목표", "desc", GoalType.WEIGHT_LOSS,
            80.0, 70.0, LocalDate.now(), LocalDate.now().plusMonths(1));
        ReflectionTestUtils.setField(otherGoal, "id", fitnessGoalId);

        DailyRecord record = DailyRecord.of(List.of("url"), otherGoal, LocalDate.now(), "memo");
        ReflectionTestUtils.setField(record, "id", dailyRecordId);

        given(dailyRecordRepository.findByIdOrElseThrow(dailyRecordId)).willReturn(record);
        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(fitnessGoalId, userId))
            .willThrow(new BaseException(ExceptionCode.NOT_DAILY_RECORD_OWNER));

        // when & then
        BaseException ex = assertThrows(BaseException.class, () -> {
            dailyRecordService.getDailyRecord(dailyRecordId, fitnessGoalId, userId);
        });
        assertEquals(ExceptionCode.NOT_DAILY_RECORD_OWNER, ex.getErrorCode());
    }

    @Test
    void deleteDailyRecord_success() {
        // given
        Long fitnessGoalId = 10L;
        Long dailyRecordId = 200L;
        Long userId = 1L;

        DailyRecord record = DailyRecord.of(List.of("url"), fitnessGoal, LocalDate.now(), "memo");
        ReflectionTestUtils.setField(record, "id", dailyRecordId);

        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(fitnessGoalId, userId)).willReturn(fitnessGoal);
        given(dailyRecordRepository.findByIdOrElseThrow(dailyRecordId)).willReturn(record);

        // when
        dailyRecordService.deleteDailyRecord(dailyRecordId, fitnessGoalId, userId);

        // then
        verify(dailyRecordRepository).delete(record);
    }

    @Test
    void deleteDailyRecord_fail_notOwner() {
        Long fitnessGoalId = 10L;
        Long dailyRecordId = 200L;
        Long userId = 2L; // 삭제 시도하는 사용자 ID

        User ownerUser = new User("owner@test.com", "소유자", "GOOGLE");
        ReflectionTestUtils.setField(ownerUser, "id", 1L); // 실제 소유자 ID

        FitnessGoal ownerGoal = FitnessGoal.of(ownerUser, "다른 목표", "desc", GoalType.WEIGHT_LOSS,
            80.0, 70.0, LocalDate.now(), LocalDate.now().plusMonths(1));
        ReflectionTestUtils.setField(ownerGoal, "id", fitnessGoalId);

        DailyRecord record = DailyRecord.of(List.of("url"), ownerGoal, LocalDate.now(), "memo");
        ReflectionTestUtils.setField(record, "id", dailyRecordId);

        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(fitnessGoalId, userId)).willReturn(ownerGoal);
        given(dailyRecordRepository.findByIdOrElseThrow(dailyRecordId)).willReturn(record);

        BaseException ex = assertThrows(BaseException.class, () -> {
            dailyRecordService.deleteDailyRecord(dailyRecordId, fitnessGoalId, userId);
        });

        assertEquals(ExceptionCode.NOT_DAILY_RECORD_OWNER, ex.getErrorCode());
    }


}
