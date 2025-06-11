package org.example.fitpass.domain.fitnessGoal.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.fitnessGoal.dto.request.WeightRecordCreateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.response.WeightRecordResponseDto;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.entity.WeightRecord;
import org.example.fitpass.domain.fitnessGoal.enums.GoalStatus;
import org.example.fitpass.domain.fitnessGoal.repository.FitnessGoalRepository;
import org.example.fitpass.domain.fitnessGoal.repository.WeightRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeightRecordService {

    private final WeightRecordRepository weightRecordRepository;
    private final FitnessGoalRepository fitnessGoalRepository;

    // 체중 기록 생성
    @Transactional
    public WeightRecordResponseDto createWeightRecord (WeightRecordCreateRequestDto requestDto, Long userId) {
        // 목표 존재 여부 및 권한 확인
        FitnessGoal fitnessGoal = fitnessGoalRepository.findByIdAndUserIdOrElseThrow(requestDto.getFitnessGoalId(), userId);

        // 만료되거나 취소된 목표에는 체중 기록 생성 불가
        if (fitnessGoal.getGoalStatus() == GoalStatus.EXPIRED || 
            fitnessGoal.getGoalStatus() == GoalStatus.CANCELLED) {
            throw new BaseException(ExceptionCode.FITNESS_GOAL_WEIGHT_UPDATE_NOT_ALLOWED);
        }

        if(weightRecordRepository.existsByFitnessGoalIdAndRecordDate(
            requestDto.getFitnessGoalId(), requestDto.getRecordDate())) {
            throw new BaseException(ExceptionCode.WEIGHT_RECORD_ALREADY_EXISTS);
        }

        WeightRecord weightRecord = WeightRecord.of(
            fitnessGoal,
            requestDto.getWeight(),
            requestDto.getRecordDate(),
            requestDto.getMemo()
        );

        WeightRecord savedRecord = weightRecordRepository.save(weightRecord);

        // 현재 체중 업데이트 (목표 달성 체크 포함)
        fitnessGoal.updateCurrentWeight(requestDto.getWeight());
        
        return WeightRecordResponseDto.from(savedRecord);
    }

    // 체중 기록 목록 조회
    @Transactional
    public List<WeightRecordResponseDto> getWeightRecords (Long goalId, Long userId) {
        fitnessGoalRepository.findByIdAndUserIdOrElseThrow(goalId, userId);

        List<WeightRecord> weightRecords = weightRecordRepository.findByFitnessGoalIdOrderByRecordDateDesc(goalId);
        return weightRecords.stream().map(WeightRecordResponseDto::from).collect(Collectors.toList());
    }

    // 체중 기록 상세 조회
    @Transactional
    public WeightRecordResponseDto getWeightRecord(Long userId, Long recordId) {
        WeightRecord weightRecord = weightRecordRepository.findByIdOrElseThrow(recordId);

        if(!weightRecord.getFitnessGoal().getUser().getId().equals(userId)) {
            throw new BaseException(ExceptionCode.NOT_WEIGHT_RECORD_OWNER);
        }
        return WeightRecordResponseDto.from(weightRecord);
    }

    // 체중 기록 수정
    @Transactional
    public WeightRecordResponseDto updateWeightRecord(Long userId, Long recordId, WeightRecordCreateRequestDto requestDto) {
        WeightRecord weightRecord = weightRecordRepository.findByIdOrElseThrow(recordId);
        // 권한 확인
        if(!weightRecord.getFitnessGoal().getUser().getId().equals(userId)) {
            throw new BaseException(ExceptionCode.NOT_WEIGHT_RECORD_OWNER);
        }

        weightRecord.updateRecord(requestDto.getWeight(), requestDto.getMemo());

        updateCurrentWeightIfLatest(weightRecord);

        return WeightRecordResponseDto.from(weightRecord);
    }

    // 체중 기록 삭제
    @Transactional
    public void deleteWeightRecord (Long userId, Long recordId) {
        WeightRecord weightRecord = weightRecordRepository.findByIdOrElseThrow(recordId);
        // 권한 확인
        if(!weightRecord.getFitnessGoal().getUser().getId().equals(userId)) {
            throw new BaseException(ExceptionCode.NOT_WEIGHT_RECORD_OWNER);
        }

        Long fitnessGoalId = weightRecord.getFitnessGoal().getId();

        weightRecordRepository.delete(weightRecord);
        updateCurrentWeightToLatest(fitnessGoalId);
    }

    private void updateCurrentWeightIfLatest(WeightRecord record) {
        WeightRecord latestRecord = weightRecordRepository
            .findTopByFitnessGoalIdOrderByRecordDateDesc(record.getFitnessGoal().getId())
            .orElse(null);

        // 수정한 기록이 최신 기록이라면 목표의 현재 체중 업데이트
        if (latestRecord != null && latestRecord.getId().equals(record.getId())) {
            record.getFitnessGoal().updateCurrentWeight(record.getWeight());
        }
    }

    private void updateCurrentWeightToLatest(Long fitnessGoalId) {
        weightRecordRepository.findTopByFitnessGoalIdOrderByRecordDateDesc(fitnessGoalId)
            .ifPresentOrElse(
                latestRecord -> {
                    FitnessGoal goal = latestRecord.getFitnessGoal();
                    goal.updateCurrentWeight(latestRecord.getWeight());
                },
                () -> {
                    // 기록이 없으면 시작 체중으로 복원
                    FitnessGoal goal = fitnessGoalRepository.findById(fitnessGoalId)
                        .orElseThrow();
                    goal.updateCurrentWeight(goal.getStartWeight());
                }
            );
    }

}
