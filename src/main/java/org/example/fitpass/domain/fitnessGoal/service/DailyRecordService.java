package org.example.fitpass.domain.fitnessGoal.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.fitnessGoal.dto.request.DailyRecordCreateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.response.DailyRecordResponseDto;
import org.example.fitpass.domain.fitnessGoal.entity.DailyRecord;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.repository.DailyRecordRepository;
import org.example.fitpass.domain.fitnessGoal.repository.FitnessGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyRecordService {

    private final DailyRecordRepository dailyRecordRepository;
    private final FitnessGoalRepository fitnessGoalRepository;

    // 일일 기록 생성
    @Transactional
    public DailyRecordResponseDto createDailyRecord (DailyRecordCreateRequestDto requestDto, Long userId) {
        FitnessGoal fitnessGoal = fitnessGoalRepository.findByIdAndUserIdOrElseThrow(requestDto.getFitnessGoalId(), userId);

        if(dailyRecordRepository.existsByFitnessGoalIdAndRecordDate(requestDto.getFitnessGoalId(), requestDto.getRecordDate())) {
            throw new BaseException(ExceptionCode.DAILY_RECORD_ALREADY_EXISTS);
        }

        DailyRecord dailyRecord = DailyRecord.of(
            fitnessGoal,
            requestDto.getRecordType(),
            requestDto.getRecordDate(),
            requestDto.getMemo());
        DailyRecord savedRecord = dailyRecordRepository.save(dailyRecord);

        return DailyRecordResponseDto.from(savedRecord);
    }

    // 특정 목표의 일일 기록 목록 조회
    @Transactional(readOnly = true)
    public List<DailyRecordResponseDto> getDailyRecords (Long fitnessGoalId, Long userId) {
        FitnessGoal fitnessGoal = fitnessGoalRepository.findByIdAndUserIdOrElseThrow(fitnessGoalId, userId);

        List<DailyRecord> dailyRecords = dailyRecordRepository.findByFitnessGoalIdOrderByRecordDateDesc(fitnessGoalId);

        return dailyRecords.stream().map(DailyRecordResponseDto::from).collect(Collectors.toList());
    }

    // 일일 기록 상세 조회
    @Transactional(readOnly = true)
    public DailyRecordResponseDto getDailyRecord (Long recordId, Long userId) {
        DailyRecord dailyRecord = dailyRecordRepository.findByIdOrElseThrow(recordId);

        if (!dailyRecord.getFitnessGoal().getUser().getId().equals(userId)) {
            throw new BaseException(ExceptionCode.NOT_DAILY_RECORD_OWNER);
        }
        return DailyRecordResponseDto.from(dailyRecord);
    }

    // 일일 기록 삭제
    @Transactional(readOnly = true)
    public void deleteDailyRecord (Long recordId, Long userId) {
        DailyRecord dailyRecord = dailyRecordRepository.findByIdOrElseThrow(recordId);
        if (!dailyRecord.getFitnessGoal().getUser().getId().equals(userId)) {
            throw new BaseException(ExceptionCode.NOT_DAILY_RECORD_OWNER);
        }
        dailyRecordRepository.delete(dailyRecord);
    }

}
