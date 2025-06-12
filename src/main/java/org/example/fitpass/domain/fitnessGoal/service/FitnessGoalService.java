package org.example.fitpass.domain.fitnessGoal.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.fitnessGoal.dto.request.FitnessGoalCreateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.request.FitnessGoalUpdateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.response.FitnessGoalListResponseDto;
import org.example.fitpass.domain.fitnessGoal.dto.response.FitnessGoalResponseDto;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.enums.GoalStatus;
import org.example.fitpass.domain.fitnessGoal.repository.FitnessGoalRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FitnessGoalService {

    private final FitnessGoalRepository fitnessGoalRepository;
    private final UserRepository userRepository;

    // 목표 생성
    @Transactional
    public FitnessGoalResponseDto createGoal(Long userId, FitnessGoalCreateRequestDto requestDto) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);

        FitnessGoal fitnessGoal = FitnessGoal.of(
            user,
            requestDto.title(),
            requestDto.description(),
            requestDto.goalType(),
            requestDto.startWeight(),
            requestDto.targetWeight(),
            requestDto.startDate(),
            requestDto.endDate());

        FitnessGoal savedGoal = fitnessGoalRepository.save(fitnessGoal);
        return FitnessGoalResponseDto.from(savedGoal);
    }

    // 내 목표 목록 조회 (만료 상태 체크 포함)
    @Transactional
    public List<FitnessGoalListResponseDto> getMyGoals(Long userId) {
        List<FitnessGoal> goals = fitnessGoalRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        // 만료 상태 체크 및 업데이트
        goals.forEach(FitnessGoal::checkAndUpdateExpiredStatus);

        return goals.stream().map(FitnessGoalListResponseDto::from).collect(Collectors.toList());
    }

    // 목표 상세 조회 (만료 상태 체크 포함)
    @Transactional
    public FitnessGoalResponseDto getGoal(Long userId, Long goalId) {
        FitnessGoal fitnessGoal = fitnessGoalRepository.findByIdAndUserIdOrElseThrow(goalId, userId);
        
        // 만료 상태 체크 및 업데이트
        fitnessGoal.checkAndUpdateExpiredStatus();

        return FitnessGoalResponseDto.from(fitnessGoal);
    }

    // 목표 수정
    @Transactional
    public FitnessGoalResponseDto updateGoal(Long goalId, FitnessGoalUpdateRequestDto requestDto,
        Long userId) {
        FitnessGoal fitnessGoal = fitnessGoalRepository.findByIdAndUserIdOrElseThrow(goalId,
            userId);

        // 완료되거나 취소된 목표는 수정 불가
        if (fitnessGoal.getGoalStatus() == GoalStatus.COMPLETED) {
            throw new BaseException(ExceptionCode.FITNESS_GOAL_ALREADY_COMPLETED);
        }
        if (fitnessGoal.getGoalStatus() == GoalStatus.CANCELLED) {
            throw new BaseException(ExceptionCode.FITNESS_GOAL_ALREADY_CANCELLED);
        }

        fitnessGoal.updateGoal(requestDto.title(), requestDto.description(),
            requestDto.targetWeight(), requestDto.endDate());
        return FitnessGoalResponseDto.from(fitnessGoal);
    }

    // 목표 취소
    @Transactional
    public FitnessGoalResponseDto cancelGoal(Long goalId, Long userId) {
        FitnessGoal fitnessGoal = fitnessGoalRepository.findByIdAndUserIdOrElseThrow(goalId, userId);
        
        fitnessGoal.cancelGoal();
        
        return FitnessGoalResponseDto.from(fitnessGoal);
    }

    // 목표 삭제
    @Transactional
    public void deleteGoal(Long goalId, Long userId) {
        FitnessGoal fitnessGoal = fitnessGoalRepository.findByIdAndUserIdOrElseThrow(goalId, userId);
        
        // 완료된 목표는 삭제 불가 (기록 보존을 위해)
        if (fitnessGoal.getGoalStatus() == GoalStatus.COMPLETED) {
            throw new BaseException(ExceptionCode.FITNESS_GOAL_DELETE_NOT_ALLOWED);
        }
        
        fitnessGoalRepository.delete(fitnessGoal);
    }

    // 활성 목표들의 만료 상태 일괄 업데이트 (스케줄러용)
    @Transactional
    public void updateExpiredGoals() {
        List<FitnessGoal> activeGoals = fitnessGoalRepository.findByGoalStatus(GoalStatus.ACTIVE);
        activeGoals.forEach(FitnessGoal::checkAndUpdateExpiredStatus);
    }

}
