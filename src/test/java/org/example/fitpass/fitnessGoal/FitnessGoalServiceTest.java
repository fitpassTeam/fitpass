package org.example.fitpass.fitnessGoal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.fitnessGoal.dto.response.FitnessGoalListResponseDto;
import org.example.fitpass.domain.fitnessGoal.dto.response.FitnessGoalResponseDto;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.enums.GoalStatus;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;
import org.example.fitpass.domain.fitnessGoal.repository.FitnessGoalRepository;
import org.example.fitpass.domain.fitnessGoal.service.FitnessGoalService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class FitnessGoalServiceTest {

    @InjectMocks
    private FitnessGoalService fitnessGoalService;

    @Mock
    private FitnessGoalRepository fitnessGoalRepository;

    @Mock
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User("email@test.com", "홍길동", "GOOGLE");
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    void createGoal_성공() {
        // given
        given(userRepository.findByIdOrElseThrow(1L)).willReturn(user);

        FitnessGoal savedGoal = FitnessGoal.of(user, "목표1", "desc", GoalType.WEIGHT_LOSS,
            80.0, 70.0, LocalDate.now(), LocalDate.now().plusMonths(1));
        ReflectionTestUtils.setField(savedGoal, "id", 10L);

        given(fitnessGoalRepository.save(Mockito.any(FitnessGoal.class))).willReturn(savedGoal);

        // when
        FitnessGoalResponseDto response = fitnessGoalService.createGoal(
            1L, "목표1", "desc", GoalType.WEIGHT_LOSS, 80.0, 70.0, LocalDate.now(), LocalDate.now().plusMonths(1)
        );

        // then
        assertNotNull(response);
        assertEquals(10L, response.fitnessGoalId());
        assertEquals("목표1", response.title());
    }

    @Test
    void getMyGoals_성공() {
        // given
        FitnessGoal goal1 = spy(FitnessGoal.of(user, "목표1", "desc", GoalType.WEIGHT_LOSS,
            80.0, 70.0, LocalDate.now(), LocalDate.now().plusMonths(1)));
        ReflectionTestUtils.setField(goal1, "id", 10L);

        List<FitnessGoal> goals = List.of(goal1);

        given(fitnessGoalRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(goals);

        doNothing().when(goal1).checkAndUpdateExpiredStatus();

        // when
        List<FitnessGoalListResponseDto> result = fitnessGoalService.getMyGoals(1L);

        // then
        assertEquals(1, result.size());
        assertEquals("목표1", result.get(0).title());
        verify(goal1).checkAndUpdateExpiredStatus();
    }

    @Test
    void getGoal_성공() {
        // given
        FitnessGoal goal = spy(FitnessGoal.of(user, "목표1", "desc", GoalType.WEIGHT_LOSS,
            80.0, 70.0, LocalDate.now(), LocalDate.now().plusMonths(1)));
        ReflectionTestUtils.setField(goal, "id", 10L);

        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(10L, 1L)).willReturn(goal);
        doNothing().when(goal).checkAndUpdateExpiredStatus();

        // when
        FitnessGoalResponseDto response = fitnessGoalService.getGoal(1L, 10L);

        // then
        assertEquals(10L, response.fitnessGoalId());
        assertEquals("목표1", response.title());
        verify(goal).checkAndUpdateExpiredStatus();
    }


    @Test
    void updateGoal_성공() {
        // given
        FitnessGoal goal = FitnessGoal.of(user, "목표1", "desc", GoalType.WEIGHT_LOSS,
            80.0, 75.0, LocalDate.now(), LocalDate.now().plusMonths(1));
        ReflectionTestUtils.setField(goal, "id", 10L);

        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(10L, 1L)).willReturn(goal);

        // when
        FitnessGoalResponseDto updated = fitnessGoalService.updateGoal(
            10L, "수정된 목표", "변경된 설명", 70.0, LocalDate.now().plusMonths(2), 1L);

        // then
        assertEquals("수정된 목표", updated.title());
        assertEquals(70.0, updated.targetWeight());
    }

    @Test
    void updateGoal_이미완료_예외() {
        // given
        FitnessGoal goal = FitnessGoal.of(user, "목표1", "desc", GoalType.WEIGHT_LOSS,
            80.0, 75.0, LocalDate.now(), LocalDate.now().plusMonths(1));
        ReflectionTestUtils.setField(goal, "id", 10L);
        // 완료 상태 세팅
        ReflectionTestUtils.setField(goal, "goalStatus", GoalStatus.COMPLETED);

        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(10L, 1L)).willReturn(goal);

        // when & then
        BaseException e = assertThrows(BaseException.class, () ->
            fitnessGoalService.updateGoal(10L, "수정된 목표", "desc", 70.0, LocalDate.now().plusMonths(1), 1L));

        assertEquals(ExceptionCode.FITNESS_GOAL_ALREADY_COMPLETED, e.getErrorCode());
    }

    @Test
    void cancelGoal_성공() {
        // given
        FitnessGoal goal = FitnessGoal.of(user, "목표1", "desc", GoalType.WEIGHT_LOSS,
            80.0, 75.0, LocalDate.now(), LocalDate.now().plusMonths(1));
        ReflectionTestUtils.setField(goal, "id", 10L);

        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(10L, 1L)).willReturn(goal);

        // when
        FitnessGoalResponseDto canceled = fitnessGoalService.cancelGoal(10L, 1L);

        // then
        assertEquals(GoalStatus.CANCELLED, canceled.goalStatus());
    }

    @Test
    void deleteGoal_성공() {
        // given
        FitnessGoal goal = FitnessGoal.of(user, "목표1", "desc", GoalType.WEIGHT_LOSS,
            80.0, 75.0, LocalDate.now(), LocalDate.now().plusMonths(1));
        ReflectionTestUtils.setField(goal, "id", 10L);

        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(10L, 1L)).willReturn(goal);

        // when
        fitnessGoalService.deleteGoal(10L, 1L);

        // then
        verify(fitnessGoalRepository).delete(goal);
    }

    @Test
    void deleteGoal_완료상태_예외() {
        // given
        FitnessGoal goal = FitnessGoal.of(user, "목표1", "desc", GoalType.WEIGHT_LOSS,
            80.0, 75.0, LocalDate.now(), LocalDate.now().plusMonths(1));
        ReflectionTestUtils.setField(goal, "id", 10L);
        ReflectionTestUtils.setField(goal, "goalStatus", GoalStatus.COMPLETED);

        given(fitnessGoalRepository.findByIdAndUserIdOrElseThrow(10L, 1L)).willReturn(goal);

        // when & then
        BaseException e = assertThrows(BaseException.class, () -> fitnessGoalService.deleteGoal(10L, 1L));
        assertEquals(ExceptionCode.FITNESS_GOAL_DELETE_NOT_ALLOWED, e.getErrorCode());
    }

    @Test
    void updateExpiredGoals_성공() {
        // given
        FitnessGoal goal1 = Mockito.mock(FitnessGoal.class);
        FitnessGoal goal2 = Mockito.mock(FitnessGoal.class);
        List<FitnessGoal> activeGoals = List.of(goal1, goal2);

        given(fitnessGoalRepository.findByGoalStatus(GoalStatus.ACTIVE)).willReturn(activeGoals);

        // when
        fitnessGoalService.updateExpiredGoals();

        // then
        verify(goal1).checkAndUpdateExpiredStatus();
        verify(goal2).checkAndUpdateExpiredStatus();
    }
}
