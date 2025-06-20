package org.example.fitpass.domain.point.controller;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.point.dto.request.PointChargeRequestDto;
import org.example.fitpass.domain.point.dto.response.PointBalanceResponseDto;
import org.example.fitpass.domain.point.service.PointService;
import org.example.fitpass.domain.user.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminPointController {

    private final PointService pointService;

    // 포인트 충전 - 관리자만 충전할 수 있게
    @PostMapping("/admin/users/{targetUserId}/charge")
    public ResponseEntity<ResponseMessage<PointBalanceResponseDto>> chargePoint(
        @PathVariable Long targetUserId,
        @RequestBody PointChargeRequestDto pointChargeRequestDto,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        PointBalanceResponseDto responseDto = pointService.chargePoint(targetUserId,
            pointChargeRequestDto.amount(),
            "포인트 충전");

        ResponseMessage<PointBalanceResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.POINT_CHARGE_SUCCESS, responseDto);
        return ResponseEntity.status(SuccessCode.POINT_CHARGE_SUCCESS.getHttpStatus()).body(responseMessage);
    }

}
