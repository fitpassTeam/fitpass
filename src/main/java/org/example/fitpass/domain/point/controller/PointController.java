package org.example.fitpass.domain.point.controller;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.point.dto.request.PointCashOutRequestDto;
import org.example.fitpass.domain.point.dto.request.PointChargeRequestDto;
import org.example.fitpass.domain.point.dto.request.PointUseRefundRequestDto;
import org.example.fitpass.domain.point.dto.response.PointCashOutResponseDto;
import org.example.fitpass.domain.point.dto.response.PointResponseDto;
import org.example.fitpass.domain.point.entity.Point;
import org.example.fitpass.domain.point.service.PointService;
import org.example.fitpass.domain.user.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("users/points")
public class PointController {

    private final PointService pointService;

    // 포인트 사용
    @PostMapping("/use")
    public ResponseEntity<ResponseMessage<Integer>> usePoint(
        @RequestBody PointUseRefundRequestDto pointUseRefundRequestDto,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        int newBalance = pointService.usePoint(user.getId(), pointUseRefundRequestDto);

        ResponseMessage<Integer> responseMessage =
            ResponseMessage.success(SuccessCode.POINT_USE_SUCCESS, newBalance);
        return ResponseEntity.status(SuccessCode.POINT_USE_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 포인트 100% 환불 (PT 취소)
    @PostMapping("/refund")
    public ResponseEntity<ResponseMessage<Integer>> refundPoint(
        @RequestBody PointUseRefundRequestDto pointUseRefundRequestDto,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        int newBalance = pointService.refundPoint(user.getId(), pointUseRefundRequestDto);

        ResponseMessage<Integer> responseMessage =
            ResponseMessage.success(SuccessCode.POINT_REFUND_SUCCESS, newBalance);
        return ResponseEntity.status(SuccessCode.POINT_REFUND_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 포인트 현금화 (90% 현금 환불)
    @PostMapping("/cashout")
    public ResponseEntity<ResponseMessage<PointCashOutResponseDto>> cashOutPoint(
        @RequestBody PointCashOutRequestDto pointCashOutRequestDto,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        PointCashOutResponseDto pointCashOutResponseDto = pointService.cashOutPoint(user.getId(), pointCashOutRequestDto);

        ResponseMessage<PointCashOutResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.POINT_CASH_OUT_SUCCESS, pointCashOutResponseDto);
        return ResponseEntity.status(SuccessCode.POINT_CASH_OUT_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 포인트 잔액 조회
    @GetMapping
    public ResponseEntity<ResponseMessage<Integer>> getPointBalance (
        @AuthenticationPrincipal CustomUserDetails user) {
        int balance = pointService.getPointBalance(user.getId());
        ResponseMessage<Integer> responseMessage =
            ResponseMessage.success(SuccessCode.POINT_BALANCE_GET_SUCCESS,balance);

        return ResponseEntity.status(SuccessCode.POINT_BALANCE_GET_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 포인트 이력 조회
    @GetMapping("/history")
    public ResponseEntity<ResponseMessage<List<PointResponseDto>>> getPointHistory (@AuthenticationPrincipal CustomUserDetails user) {
        List<Point> history = pointService.getPointHistory(user.getId());
        List<PointResponseDto> pointResponseDtos = history.stream()
            .map(PointResponseDto::from)
            .collect(Collectors.toList());

        ResponseMessage<List<PointResponseDto>> responseMessage =
            ResponseMessage.success(SuccessCode.POINT_HISTORY_GET_SUCCESS, pointResponseDtos);

        return ResponseEntity.status(SuccessCode.POINT_HISTORY_GET_SUCCESS.getHttpStatus()).body(responseMessage);
    }


}
