package org.example.fitpass.domain.fitnessGoal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.fitnessGoal.dto.request.WeightRecordCreateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.response.WeightRecordResponseDto;
import org.example.fitpass.domain.fitnessGoal.service.WeightRecordService;
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
@Tag(name = "WeightRecord API", description = "피트니스 목표의 체중 기록 관리 API")
public class WeightRecordController {

    private final WeightRecordService weightRecordService;

    // 체중 기록 생성
    @Operation(
        summary = "체중 기록 생성",
        description = "피트니스 목표에 대한 체중 기록을 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "체중 기록 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "피트니스 목표를 찾을 수 없음")
    })
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
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_WEIGHTRECORD_CREATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_WEIGHTRECORD_CREATE_SUCCESS, recordResponseDto));
    }

    // 특정 목표의 체중 기록 목록 조회
    @Operation(
        summary = "체중 기록 목록 조회",
        description = "특정 피트니스 목표의 체중 기록 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "체중 기록 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "피트니스 목표를 찾을 수 없음")
    })
    @GetMapping
    public ResponseEntity<ResponseMessage<List<WeightRecordResponseDto>>> getWeightRecords (
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<WeightRecordResponseDto> responseDtos = weightRecordService.getWeightRecords(fitnessGoalId, userDetails.getId());
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_WEIGHTRECORD_LIST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_WEIGHTRECORD_LIST_SUCCESS, responseDtos));
    }

    // 체중 기록 상세 조회
    @Operation(
        summary = "체중 기록 상세 조회",
        description = "특정 체중 기록의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "체중 기록 상세 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "체중 기록을 찾을 수 없음")
    })
    @GetMapping("/{weightRecordId}")
    public ResponseEntity<ResponseMessage<WeightRecordResponseDto>> getWeightRecord (
        @PathVariable Long weightRecordId,
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        WeightRecordResponseDto responseDto = weightRecordService.getWeightRecord(userDetails.getId(), fitnessGoalId, weightRecordId);
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_WEIGHTRECORD_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_WEIGHTRECORD_GET_SUCCESS, responseDto));
    }

    // 체중 기록 수정
    @Operation(
        summary = "체중 기록 수정",
        description = "체중 기록 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "체중 기록 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "체중 기록을 찾을 수 없음")
    })
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
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_WEIGHTRECORD_UPDATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_WEIGHTRECORD_UPDATE_SUCCESS, responseDto));
    }

    // 체중 기록 삭제
    @Operation(
        summary = "체중 기록 삭제",
        description = "체중 기록을 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "체중 기록 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "체중 기록을 찾을 수 없음")
    })
    @DeleteMapping("/{weightRecordId}")
    public ResponseEntity<ResponseMessage<Void>> deleteWeightRecord (
        @PathVariable Long weightRecordId,
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
        ) {
        weightRecordService.deleteWeightRecord(userDetails.getId(), weightRecordId, fitnessGoalId);
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_WEIGHTRECORD_DELETE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_WEIGHTRECORD_DELETE_SUCCESS));
    }

}
