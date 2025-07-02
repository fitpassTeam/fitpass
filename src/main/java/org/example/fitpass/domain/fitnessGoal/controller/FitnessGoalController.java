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
import org.example.fitpass.domain.fitnessGoal.dto.request.FitnessGoalCreateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.request.FitnessGoalUpdateRequestDto;
import org.example.fitpass.domain.fitnessGoal.dto.response.FitnessGoalListResponseDto;
import org.example.fitpass.domain.fitnessGoal.dto.response.FitnessGoalResponseDto;
import org.example.fitpass.domain.fitnessGoal.service.FitnessGoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fitness-goals")
@RequiredArgsConstructor
@Tag(name = "피트니스 목표 관리", description = "피트니스 목표 생성, 조회, 수정, 삭제 관련 API")
public class FitnessGoalController {

    private final FitnessGoalService fitnessGoalService;

    // 목표 생성
    @Operation(
        summary = "피트니스 목표 생성",
        description = "새로운 피트니스 목표를 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "목표 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<ResponseMessage<FitnessGoalResponseDto>> createGoal (
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @Valid @RequestBody FitnessGoalCreateRequestDto fitnessGoalCreateRequestDto) {
        FitnessGoalResponseDto responseDto = fitnessGoalService.createGoal(
            userDetails.getId(),
            fitnessGoalCreateRequestDto.title(),
            fitnessGoalCreateRequestDto.description(),
            fitnessGoalCreateRequestDto.goalType(),
            fitnessGoalCreateRequestDto.startWeight(),
            fitnessGoalCreateRequestDto.targetWeight(),
            fitnessGoalCreateRequestDto.startDate(),
            fitnessGoalCreateRequestDto.endDate());

        return ResponseEntity.status(SuccessCode.FITNESSGOAL_CREATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_CREATE_SUCCESS, responseDto));
    }

    // 내 목표 목록 조회
    @Operation(
        summary = "내 목표 목록 조회",
        description = "로그인한 사용자의 피트니스 목표 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "목표 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<ResponseMessage<List<FitnessGoalListResponseDto>>> getMyGoals(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<FitnessGoalListResponseDto> responseDtos = fitnessGoalService.getMyGoals(userDetails.getId());
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_LIST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_LIST_SUCCESS, responseDtos));
    }

    // 목표 상세 조회
    @Operation(
        summary = "목표 상세 조회",
        description = "특정 피트니스 목표의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "목표 상세 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "목표를 찾을 수 없음")
    })
    @GetMapping("/{fitnessGoalId}")
    public ResponseEntity<ResponseMessage<FitnessGoalResponseDto>> getGoal (
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FitnessGoalResponseDto responseDto = fitnessGoalService.getGoal(userDetails.getId(), fitnessGoalId);

        return ResponseEntity.status(SuccessCode.FITNESSGOAL_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_GET_SUCCESS, responseDto));
    }

    // 목표 수정
    @Operation(
        summary = "목표 수정",
        description = "피트니스 목표 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "목표 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "목표를 찾을 수 없음")
    })
    @PutMapping("/{fitnessGoalId}")
    public ResponseEntity<ResponseMessage<FitnessGoalResponseDto>> updateGoal (
        @PathVariable Long fitnessGoalId,
        @Valid @RequestBody FitnessGoalUpdateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FitnessGoalResponseDto responseDto = fitnessGoalService.updateGoal(
            fitnessGoalId,
            requestDto.title(),
            requestDto.description(),
            requestDto.targetWeight(),
            requestDto.endDate(),
            userDetails.getId());

        return ResponseEntity.status(SuccessCode.FITNESSGOAL_UPDATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_UPDATE_SUCCESS, responseDto));
    }

    // 목표 취소
    @Operation(
        summary = "목표 취소",
        description = "진행 중인 피트니스 목표를 취소합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "목표 취소 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "목표를 찾을 수 없음")
    })
    @PatchMapping("/{fitnessGoalId}/cancel")
    public ResponseEntity<ResponseMessage<FitnessGoalResponseDto>> cancelGoal (
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FitnessGoalResponseDto responseDto = fitnessGoalService.cancelGoal(fitnessGoalId, userDetails.getId());
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_CANCEL_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_CANCEL_SUCCESS, responseDto));
    }

    // 목표 삭제
    @Operation(
        summary = "목표 삭제",
        description = "피트니스 목표를 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "목표 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
        @ApiResponse(responseCode = "404", description = "목표를 찾을 수 없음")
    })
    @DeleteMapping("/{fitnessGoalId}")
    public ResponseEntity<ResponseMessage<Void>> deleteGoal (
        @PathVariable Long fitnessGoalId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        fitnessGoalService.deleteGoal(fitnessGoalId, userDetails.getId());
        return ResponseEntity.status(SuccessCode.FITNESSGOAL_DELETE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.FITNESSGOAL_DELETE_SUCCESS));
    }
}
