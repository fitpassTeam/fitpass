package org.example.fitpass.domain.point.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.point.dto.request.PointChargeRequestDto;
import org.example.fitpass.domain.point.dto.response.PointBalanceResponseDto;
import org.example.fitpass.domain.point.service.PointService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "관리자 포인트 관리", description = "관리자 권한으로 포인트를 관리하는 API")
public class AdminPointController {

    private final PointService pointService;

    // 포인트 충전 - 관리자만 충전할 수 있게
    @Operation(
        summary = "관리자 포인트 충전",
        description = "관리자가 특정 사용자에게 포인트를 충전합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포인트 충전 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/admin/users/{targetUserId}/charge")
    public ResponseEntity<ResponseMessage<PointBalanceResponseDto>> chargePoint(
        @PathVariable Long targetUserId,
        @RequestBody PointChargeRequestDto pointChargeRequestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PointBalanceResponseDto responseDto = pointService.chargePoint(targetUserId,
            pointChargeRequestDto.amount(),
            "포인트 충전");
        return ResponseEntity.status(SuccessCode.POINT_CHARGE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POINT_CHARGE_SUCCESS, responseDto));
    }

}
