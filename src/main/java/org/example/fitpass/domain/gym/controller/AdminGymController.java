package org.example.fitpass.domain.gym.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.service.GymService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "GYM Admin API", description = "관리자가 체육관 승인/거절을 수행하는 API입니다.")
public class AdminGymController {

    private final GymService gymService;

    @Operation(
        summary = "승인 대기 체육관 목록 조회",
        description = "관리자가 승인 대기 중인 체육관 요청 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "승인 대기 체육관 목록 조회 성공")
    })
    @GetMapping("/admin/pending-gym-requests")
    public ResponseEntity<ResponseMessage<List<GymResponseDto>>> getPendingGymRequests() {
        List<GymResponseDto> response = gymService.getPendingGymRequests();
        return ResponseEntity.ok()
            .body(ResponseMessage.success(SuccessCode.PENDING_GYM_REQUESTS_GET_SUCCESS, response));
    }


    @Operation(
        summary = "체육관 승인",
        description = "관리자가 특정 체육관을 승인 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "체육관 승인 성공"),
        @ApiResponse(responseCode = "404", description = "체육관을 찾을 수 없음")
    })
    @PatchMapping("/admin/approve-gym/{gymId}")
    public ResponseEntity<ResponseMessage<GymResponseDto>> approveGym(
        @Parameter(description = "승인할 체육관 ID", required = true)
        @PathVariable Long gymId) {

        GymResponseDto response = gymService.approveGym(gymId);
        return ResponseEntity.status(SuccessCode.GYM_APPROVE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_APPROVE_SUCCESS, response));
    }

    @Operation(
        summary = "체육관 거절",
        description = "관리자가 특정 체육관을 거절 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "체육관 거절 성공"),
        @ApiResponse(responseCode = "404", description = "체육관을 찾을 수 없음")
    })
    @PatchMapping("/admin/reject-gym/{gymId}")
    public ResponseEntity<ResponseMessage<GymResponseDto>> rejectGym(
        @Parameter(description = "거절할 체육관 ID", required = true)
        @PathVariable Long gymId) {
        GymResponseDto response = gymService.rejectGym(gymId);
        return ResponseEntity.status(SuccessCode.GYM_REJECT_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_REJECT_SUCCESS, response));
    }

}
