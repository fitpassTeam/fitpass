package org.example.fitpass.domain.fitnessGoal.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.fitnessGoal.dto.request.FitnessGoalCreateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.request.FitnessGoalUpdateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.response.FitnessGoalListResponseDto;
import org.example.fitpass.domain.fitnessGoal.dto.response.FitnessGoalResponseDto;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
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
            requestDto.getTitle(),
            requestDto.getDescription(),
            requestDto.getGoalType(),
            requestDto.getStartWeight(),
            requestDto.getTargetWeight(),
            requestDto.getStartDate(),
            requestDto.getEndDate());

        FitnessGoal savedGoal = fitnessGoalRepository.save(fitnessGoal);
        return FitnessGoalResponseDto.from(savedGoal);
    }

    // 내 목표 목록 조회
    public List<FitnessGoalListResponseDto> getMyGoals(Long userId) {
        List<FitnessGoal> goals = fitnessGoalRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return goals.stream().map(FitnessGoalListResponseDto::from).collect(Collectors.toList());
    }

    // 목표 상세 조회
    public FitnessGoalResponseDto getGoal(Long userId, Long goalId) {
        FitnessGoal fitnessGoal = fitnessGoalRepository.findByIdAndUserIdOrElseThrow(goalId,
            userId);

        return FitnessGoalResponseDto.from(fitnessGoal);
    }

    // 목표 수정
    @Transactional
    public FitnessGoalResponseDto updateGoal(Long goalId, FitnessGoalUpdateRequestDto requestDto,
        Long userId) {
        FitnessGoal fitnessGoal = fitnessGoalRepository.findByIdAndUserIdOrElseThrow(goalId,
            userId);

        fitnessGoal.updateGoal(requestDto.getTitle(), requestDto.getDescription(),
            requestDto.getTargetWeight(), requestDto.getEndDate());
        return FitnessGoalResponseDto.from(fitnessGoal);
    }

    // 목표 삭제
    @Transactional
    public void deleteGoal(Long goalId, Long userId) {
        FitnessGoal fitnessGoal = fitnessGoalRepository.findByIdAndUserIdOrElseThrow(goalId, userId);
        fitnessGoalRepository.delete(fitnessGoal);
    }

}
