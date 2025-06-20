package org.example.fitpass.domain.fitnessGoal.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.fitnessGoal.dto.request.WeightRecordCreateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.response.WeightRecordResponseDto;
import org.example.fitpass.domain.fitnessGoal.service.WeightRecordService;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fitness-goals/{fitnessGoalId}/weight-records")
@RequiredArgsConstructor
public class WeightRecordController {

    private final WeightRecordService weightRecordService;

    // 체중 기록 생성
    @PostMapping
    public ResponseEntity<ResponseMessage<WeightRecordResponseDto>> createWeightRecord(
        @Valid @RequestBody WeightRecordCreateRequestDto requestDto,
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WeightRecordResponseDto recordResponseDto = weightRecordService.createWeightRecord(
            fitnessGoalId,
            requestDto.weight(),
            requestDto.recordDate(),
            requestDto.memo(),
            userDetails.getId());
        ResponseMessage<WeightRecordResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.FITNESSGOAL_WEIGHTRECORD_CREATE_SUCCESS, recordResponseDto);
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_WEIGHTRECORD_CREATE_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 특정 목표의 체중 기록 목록 조회
    @GetMapping
    public ResponseEntity<ResponseMessage<List<WeightRecordResponseDto>>> getWeightRecords (
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<WeightRecordResponseDto> responseDtos = weightRecordService.getWeightRecords(fitnessGoalId, userDetails.getId());
        ResponseMessage<List<WeightRecordResponseDto>> responseMessage =
            ResponseMessage.success(SuccessCode.FITNESSGOAL_WEIGHTRECORD_LIST_SUCCESS, responseDtos);
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_WEIGHTRECORD_LIST_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 체중 기록 상세 조회
    @GetMapping("/{weightRecordId}")
    public ResponseEntity<ResponseMessage<WeightRecordResponseDto>> getWeightRecord (
        @PathVariable Long weightRecordId,
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WeightRecordResponseDto responseDto = weightRecordService.getWeightRecord(userDetails.getId(), fitnessGoalId, weightRecordId);
        ResponseMessage<WeightRecordResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.FITNESSGOAL_WEIGHTRECORD_GET_SUCCESS, responseDto);
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_WEIGHTRECORD_GET_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 체중 기록 수정
    @PutMapping("/{weightRecordId}")
    public ResponseEntity<ResponseMessage<WeightRecordResponseDto>> updateWeightRecord (
        @PathVariable Long weightRecordId,
        @PathVariable Long fitnessGoalId,
        @Valid @RequestBody WeightRecordCreateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WeightRecordResponseDto responseDto = weightRecordService.updateWeightRecord(
            userDetails.getId(),
            weightRecordId,
            fitnessGoalId,
            requestDto.weight(),
            requestDto.recordDate(),
            requestDto.memo());
        ResponseMessage<WeightRecordResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.FITNESSGOAL_WEIGHTRECORD_UPDATE_SUCCESS, responseDto);
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_WEIGHTRECORD_UPDATE_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 체중 기록 삭제
    @DeleteMapping("/{weightRecordId}")
    public ResponseEntity<ResponseMessage<Void>> deleteWeightRecord (
        @PathVariable Long weightRecordId,
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
        ) {
        weightRecordService.deleteWeightRecord(userDetails.getId(), weightRecordId, fitnessGoalId);
        ResponseMessage<Void> responseMessage =
            ResponseMessage.success(SuccessCode.FITNESSGOAL_WEIGHTRECORD_DELETE_SUCCESS);
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_WEIGHTRECORD_DELETE_SUCCESS.getHttpStatus()).body(responseMessage);
    }

}
