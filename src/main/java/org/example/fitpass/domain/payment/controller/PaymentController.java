package org.example.fitpass.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.payment.dto.request.PaymentConfirmRequestDto;
import org.example.fitpass.domain.payment.dto.request.PaymentRequestDto;
import org.example.fitpass.domain.payment.dto.response.PaymentCancelResponseDto;
import org.example.fitpass.domain.payment.dto.response.PaymentResponseDto;
import org.example.fitpass.domain.payment.dto.response.PaymentUrlResponseDto;
import org.example.fitpass.domain.payment.entity.Payment;
import org.example.fitpass.domain.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "결제 관리", description = "토스페이먼츠를 통한 결제 처리 API")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    // 포인트 충전 결제 준비
    @Operation(
        summary = "결제 준비",
        description = "포인트 충전을 위한 결제를 준비합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 준비 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/prepare")
    public ResponseEntity<ResponseMessage<PaymentUrlResponseDto>> preparePayment(
        @Valid @RequestBody PaymentRequestDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        PaymentUrlResponseDto response = paymentService.preparePayment(userDetails.getId(), request.amount(), request.orderName());
        
        return ResponseEntity.ok(
            ResponseMessage.success(SuccessCode.PAYMENT_PREPARE_SUCCESS, response)
        );
    }
    
    // 토스페이먼츠 결제 승인
    @Operation(
        summary = "결제 승인",
        description = "토스페이먼츠를 통한 결제를 승인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 승인 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
    })
    @PostMapping("/confirm")
    public ResponseEntity<ResponseMessage<PaymentResponseDto>> confirmPayment(
        @Valid @RequestBody PaymentConfirmRequestDto request
    ) {
        PaymentResponseDto response = paymentService.confirmPayment(request.paymentKey(), request.orderId(), request.amount());
        
        return ResponseEntity.ok(
            ResponseMessage.success(SuccessCode.PAYMENT_CONFIRM_SUCCESS, response)
        );
    }
    
    // 결제 실패 처리
    @Operation(
        summary = "결제 실패 처리",
        description = "결제 실패 시 처리를 수행합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 실패 처리 성공"),
        @ApiResponse(responseCode = "404", description = "주문 정보를 찾을 수 없음")
    })
    @PostMapping("/fail")
    public ResponseEntity<ResponseMessage<Void>> failPayment(
        @RequestParam String orderId,
        @RequestParam(required = false) String message
    ) {
        paymentService.failPayment(orderId, message);
        
        return ResponseEntity.ok(
            ResponseMessage.success(SuccessCode.PAYMENT_FAIL_SUCCESS, null)
        );
    }
    
    // 결제 내역 조회
    @Operation(
        summary = "결제 내역 조회",
        description = "사용자의 결제 내역을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 내역 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/history")
    public ResponseEntity<ResponseMessage<List<Payment>>> getPaymentHistory(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Payment> payments = paymentService.getPaymentHistory(userDetails.getId());
        
        return ResponseEntity.ok(
            ResponseMessage.success(SuccessCode.PAYMENT_HISTORY_GET_SUCCESS, payments)
        );
    }
    
    // 결제 상태 조회
    @Operation(
        summary = "결제 상태 조회",
        description = "결제 키를 통해 결제 상태를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 상태 조회 성공"),
        @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
    })
    @GetMapping("/status/{paymentKey}")
    public ResponseEntity<ResponseMessage<PaymentResponseDto>> getPaymentStatus(
        @PathVariable String paymentKey
    ) {
        PaymentResponseDto response = paymentService.getPaymentStatus(paymentKey);
        
        return ResponseEntity.ok(
            ResponseMessage.success(SuccessCode.PAYMENT_STATUS_GET_SUCCESS, response)
        );
    }
    
    // 결제 취소
    @Operation(
        summary = "결제 취소",
        description = "결제를 취소합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 취소 성공"),
        @ApiResponse(responseCode = "400", description = "취소할 수 없는 결제"),
        @ApiResponse(responseCode = "404", description = "결제 정보를 찾을 수 없음")
    })
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<ResponseMessage<PaymentCancelResponseDto>> cancelPayment(
        @PathVariable String orderId,
        @RequestParam String cancelReason
    ) {
        PaymentCancelResponseDto response = paymentService.cancelPayment(orderId, cancelReason);
        
        return ResponseEntity.ok(
            ResponseMessage.success(SuccessCode.PAYMENT_CANCEL_SUCCESS, response)
        );
    }
}
