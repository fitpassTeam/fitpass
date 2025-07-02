package org.example.fitpass.domain.point.controller;

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
import org.example.fitpass.domain.payment.dto.request.PaymentRequestDto;
import org.example.fitpass.domain.payment.dto.response.PaymentUrlResponseDto;
import org.example.fitpass.domain.payment.service.PaymentService;
import org.example.fitpass.domain.point.dto.request.PointCashOutRequestDto;
import org.example.fitpass.domain.point.dto.request.PointUseRefundRequestDto;
import org.example.fitpass.domain.point.dto.response.PointBalanceResponseDto;
import org.example.fitpass.domain.point.dto.response.PointCashOutResponseDto;
import org.example.fitpass.domain.point.dto.response.PointResponseDto;
import org.example.fitpass.domain.point.service.PointService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("users/points")
@Tag(name = "포인트 관리", description = "포인트 충전, 사용, 환불, 현금화 등 포인트 관련 API")
public class PointController {

    private final PointService pointService;
    private final PaymentService paymentService;

    // 포인트 충전 결제 준비 (토스페이먼츠)
    @Operation(
        summary = "포인트 충전 결제 준비",
        description = "토스페이먼츠를 통한 포인트 충전 결제를 준비합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 준비 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/charge/prepare")
    public ResponseEntity<ResponseMessage<PaymentUrlResponseDto>> preparePointCharge(
        @Valid @RequestBody PaymentRequestDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PaymentUrlResponseDto responseDto = paymentService.preparePayment(userDetails.getId(),
            request.amount(), request.orderName());
        return ResponseEntity.status(SuccessCode.PAYMENT_PREPARE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.PAYMENT_PREPARE_SUCCESS, responseDto));
    }

    // 포인트 사용
    @Operation(
        summary = "포인트 사용",
        description = "사용자의 포인트를 사용합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포인트 사용 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "409", description = "포인트 잔액 부족")
    })
    @PostMapping("/use")
    public ResponseEntity<ResponseMessage<PointBalanceResponseDto>> usePoint(
        @RequestBody PointUseRefundRequestDto pointUseRefundRequestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PointBalanceResponseDto responseDto = pointService.usePoint(userDetails.getId(),
            pointUseRefundRequestDto.amount(),
            pointUseRefundRequestDto.description());
        return ResponseEntity.status(SuccessCode.POINT_USE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POINT_USE_SUCCESS, responseDto));
    }

    // 포인트 100% 환불 (PT 취소)
    @Operation(
        summary = "포인트 환불",
        description = "PT 취소 등으로 인한 포인트 100% 환불을 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포인트 환불 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/refund")
    public ResponseEntity<ResponseMessage<PointBalanceResponseDto>> refundPoint(
        @RequestBody PointUseRefundRequestDto pointUseRefundRequestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PointBalanceResponseDto responseDto = pointService.refundPoint(
            userDetails.getId(),
            pointUseRefundRequestDto.amount(),
            pointUseRefundRequestDto.description());

        return ResponseEntity.status(SuccessCode.POINT_REFUND_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POINT_REFUND_SUCCESS, responseDto));
    }

    // 포인트 현금화 (90% 현금 환불)
    @Operation(
        summary = "포인트 현금화",
        description = "포인트를 현금으로 전환합니다 (90% 현금 환불)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포인트 현금화 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "409", description = "포인트 잔액 부족")
    })
    @PostMapping("/cashout")
    public ResponseEntity<ResponseMessage<PointCashOutResponseDto>> cashOutPoint(
        @RequestBody PointCashOutRequestDto pointCashOutRequestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PointCashOutResponseDto pointCashOutResponseDto = pointService.cashOutPoint(
            userDetails.getId(),
            pointCashOutRequestDto.amount(),
            pointCashOutRequestDto.description());
        return ResponseEntity.status(SuccessCode.POINT_CASH_OUT_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POINT_CASH_OUT_SUCCESS, pointCashOutResponseDto));
    }

    // 포인트 잔액 조회
    @Operation(
        summary = "포인트 잔액 조회",
        description = "사용자의 현재 포인트 잔액을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포인트 잔액 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<ResponseMessage<PointBalanceResponseDto>> getPointBalance (
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PointBalanceResponseDto responseDto = pointService.getPointBalance(userDetails.getId());

        return ResponseEntity.status(SuccessCode.POINT_BALANCE_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POINT_BALANCE_GET_SUCCESS,responseDto));
    }

    // 포인트 이력 조회
    @Operation(
        summary = "포인트 이력 조회",
        description = "사용자의 포인트 사용 이력을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "포인트 이력 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/history")
    public ResponseEntity<ResponseMessage<List<PointResponseDto>>> getPointHistory (@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<PointResponseDto> pointResponseDtos = pointService.getPointHistory(userDetails.getId());

        return ResponseEntity.status(SuccessCode.POINT_HISTORY_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POINT_HISTORY_GET_SUCCESS, pointResponseDtos));
    }


}
