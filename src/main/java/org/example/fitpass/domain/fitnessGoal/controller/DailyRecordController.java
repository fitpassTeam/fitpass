package org.example.fitpass.domain.fitnessGoal.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.fitnessGoal.dto.request.DailyRecordCreateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.response.DailyRecordResponseDto;
import org.example.fitpass.domain.fitnessGoal.service.DailyRecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fitness-goals/{fitnessGoalId}/daily-records")
@RequiredArgsConstructor
public class DailyRecordController {

    private final DailyRecordService dailyRecordService;

    // 일일 기록 생성 (운동 사진 등)
    @PostMapping
    public ResponseEntity<ResponseMessage<DailyRecordResponseDto>> createDailyRecord (
        @Valid @RequestBody DailyRecordCreateRequestDto requestDto,
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        DailyRecordResponseDto responseDto = dailyRecordService.createDailyRecord(
            fitnessGoalId,
            requestDto.imageUrls(),
            requestDto.memo(),
            requestDto.recordDate(),
            userDetails.getId());
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_DAILYRECORD_CREATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_DAILYRECORD_CREATE_SUCCESS, responseDto));
    }

    // 특정 목표의 일일 기록 목록 조회
    @GetMapping
    public ResponseEntity<ResponseMessage<List<DailyRecordResponseDto>>> getDailyRecords(
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<DailyRecordResponseDto> responseDtos = dailyRecordService.getDailyRecords(fitnessGoalId, userDetails.getId());
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_DAILYRECORD_LIST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_DAILYRECORD_LIST_SUCCESS, responseDtos));
    }

    // 일일 기록 상세 조회
    @GetMapping("/{dailyRecordId}")
    public ResponseEntity<ResponseMessage<DailyRecordResponseDto>> getDailyRecord (
        @PathVariable Long dailyRecordId,
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        DailyRecordResponseDto responseDto = dailyRecordService.getDailyRecord(dailyRecordId, fitnessGoalId, userDetails.getId());
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_DAILYRECORD_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_DAILYRECORD_GET_SUCCESS, responseDto));
    }

    // 일일 기록 삭제
    @DeleteMapping("/{dailyRecordId}")
    public ResponseEntity<ResponseMessage<Void>> deleteDailyRecord (
        @PathVariable Long dailyRecordId,
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        dailyRecordService.deleteDailyRecord(dailyRecordId, fitnessGoalId, userDetails.getId());
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_DAILYRECORD_DELETE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_DAILYRECORD_DELETE_SUCCESS));
    }

}
