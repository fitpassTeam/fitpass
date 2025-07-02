package org.example.fitpass.domain.payment.controller;

import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    // 포인트 충전 결제 준비
    @PostMapping("/prepare")
    public ResponseEntity<ResponseMessage<PaymentUrlResponseDto>> preparePayment(
        @Valid @RequestBody PaymentRequestDto request,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        PaymentUrlResponseDto response = paymentService.preparePayment(user.getId(), request.amount(), request.orderName());
        
        return ResponseEntity.ok(
            ResponseMessage.success(SuccessCode.PAYMENT_PREPARE_SUCCESS, response)
        );
    }
    
    // 토스페이먼츠 결제 승인
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
    @GetMapping("/history")
    public ResponseEntity<ResponseMessage<List<Payment>>> getPaymentHistory(
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<Payment> payments = paymentService.getPaymentHistory(user.getId());
        
        return ResponseEntity.ok(
            ResponseMessage.success(SuccessCode.PAYMENT_HISTORY_GET_SUCCESS, payments)
        );
    }
    
    // 결제 상태 조회
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
