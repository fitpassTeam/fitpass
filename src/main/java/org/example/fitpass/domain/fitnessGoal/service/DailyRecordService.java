package org.example.fitpass.domain.fitnessGoal.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.s3.service.S3Service;
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
@Slf4j
public class DailyRecordService {

    private final DailyRecordRepository dailyRecordRepository;
    private final FitnessGoalRepository fitnessGoalRepository;
    private final S3Service s3Service;

    // 일일 기록 생성
    @Transactional
    public DailyRecordResponseDto createDailyRecord (DailyRecordCreateRequestDto requestDto, Long userId) {
        FitnessGoal fitnessGoal = fitnessGoalRepository.findByIdAndUserIdOrElseThrow(requestDto.fitnessGoalId(), userId);

        if(dailyRecordRepository.existsByFitnessGoalIdAndRecordDate(requestDto.fitnessGoalId(), requestDto.recordDate())) {
            throw new BaseException(ExceptionCode.DAILY_RECORD_ALREADY_EXISTS);
        }

        DailyRecord dailyRecord = DailyRecord.of(
            fitnessGoal,
            requestDto.recordDate(),
            requestDto.memo());
        DailyRecord savedRecord = dailyRecordRepository.save(dailyRecord);
        // 이미지 S3 업로드
        if(requestDto.imageUrls() != null && !requestDto.imageUrls().isEmpty()) {
            processImages(requestDto.imageUrls(), savedRecord);
        }

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
        // S3에서 이미지 파일들 삭제
        deleteImagesFromS3(dailyRecord.getImages());

        dailyRecordRepository.delete(dailyRecord);
    }

    // 이미지 S3 저장 메소드
    private void processImages (List<String> imageFiles, DailyRecord dailyRecord) {
        for(String imageFile : imageFiles) {

            Image image = new Image(imageFile);
            image.assignToDailyRecord(dailyRecord);

            // DailyRecord에 이미지 추가
            dailyRecord.getImages().add(image);
        }
    }
    // 이미지 S3 삭제 메서드
    private void deleteImagesFromS3(List<Image> images) {
        for (Image image : images) {
            try {
                s3Service.deleteFileFromS3(image.getUrl());
            } catch (Exception e) {
                log.warn("S3 이미지 삭제 실패: {}", image.getUrl(), e);
                // 이미지 삭제 실패해도 레코드는 삭제 진행
            }
        }
    }


}
