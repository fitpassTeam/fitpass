package org.example.fitpass.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.payment.client.TossPaymentClient;
import org.example.fitpass.domain.payment.config.TossPaymentConfig;
import org.example.fitpass.domain.payment.dto.request.PaymentConfirmRequestDto;
import org.example.fitpass.domain.payment.dto.request.PaymentRequestDto;
import org.example.fitpass.domain.payment.dto.response.PaymentCancelResponseDto;
import org.example.fitpass.domain.payment.dto.response.PaymentResponseDto;
import org.example.fitpass.domain.payment.dto.response.PaymentUrlResponseDto;
import org.example.fitpass.domain.payment.entity.Payment;
import org.example.fitpass.domain.payment.enums.PaymentStatus;
import org.example.fitpass.domain.payment.repository.PaymentRepository;
import org.example.fitpass.domain.point.service.PointService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PointService pointService;
    private final TossPaymentClient tossPaymentClient;
    private final TossPaymentConfig tossPaymentConfig;
    
    // 결제 준비 - 토스페이먼츠 결제 페이지로 리다이렉트할 정보 생성
    @Transactional
    public PaymentUrlResponseDto preparePayment(Long userId, Integer amount, String orderName) {
        User user = userRepository.findByIdOrElseThrow(userId);
        
        // 고유한 주문 ID 생성 (UUID + timestamp)
        String orderId = generateOrderId();
        
        // 결제 정보 저장 (PENDING 상태)
        Payment payment = Payment.builder()
            .orderId(orderId)
            .orderName(orderName)
            .amount(amount)
            .status(PaymentStatus.PENDING)
            .user(user)
            .build();
        
        paymentRepository.save(payment);
        
        return new PaymentUrlResponseDto(
            orderId,
            amount,
            orderName,
            user.getEmail(),
            user.getName(),
            tossPaymentConfig.getSuccessUrl(),
            tossPaymentConfig.getFailUrl()
        );
    }
    
    // 결제 승인 처리
    @Transactional
    public PaymentResponseDto confirmPayment(String paymentKey, String orderId, Integer amount) {
        // 1. 주문 정보 조회
        Payment payment = paymentRepository.findByIdOrElseThrow(orderId);
        
        // 2. 결제 금액 검증
        if (!payment.getAmount().equals(amount)) {
            throw new BaseException(ExceptionCode.PAYMENT_AMOUNT_MISMATCH);
        }
        
        try {
            // 3. 토스페이먼츠 결제 승인 요청
            PaymentResponseDto tossResponse = tossPaymentClient.confirmPayment(paymentKey, orderId, amount);
            
            // 4. 결제 정보 업데이트
            payment.updatePaymentKey(paymentKey);
            payment.updateStatus(PaymentStatus.CONFIRMED);
            payment.updateMethod(tossResponse.method());
            payment.updateApprovedAt(tossResponse.approvedAt());
            
            // 5. 포인트 충전
            pointService.chargePoint(
                payment.getUser().getId(), 
                payment.getAmount(), 
                "토스페이먼츠 충전 - " + payment.getOrderName()
            );
            
            log.info("결제 승인 완료 - orderId: {}, amount: {}", orderId, amount);
            
            return tossResponse;
            
        } catch (BaseException e) {
            // BaseException인 경우 그대로 다시 던지기 (토스 API 에러 등)
            payment.updateStatus(PaymentStatus.FAILED);
            payment.updateFailureReason(e.getMessage());
            throw e;
            
        } catch (Exception e) {
            // 그 외 예상치 못한 예외
            payment.updateStatus(PaymentStatus.FAILED);
            payment.updateFailureReason(e.getMessage());
            
            log.error("결제 승인 실패 - orderId: {}", orderId, e);
            throw new BaseException(ExceptionCode.TOSS_PAYMENT_CONFIRM_FAILED);
        }
    }
    
    // 결제 실패 처리
    @Transactional
    public void failPayment(String orderId, String failureReason) {
        Payment payment = paymentRepository.findByIdOrElseThrow(orderId);
        
        payment.updateStatus(PaymentStatus.FAILED);
        payment.updateFailureReason(failureReason);
        
        log.info("결제 실패 처리 완료 - orderId: {}, reason: {}", orderId, failureReason);
    }
    
    // 사용자의 결제 내역 조회
    @Transactional(readOnly = true)
    public java.util.List<Payment> getPaymentHistory(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    // 결제 상태 조회 (토스페이먼츠에서 실제 상태 확인)
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentStatus(String paymentKey) {
        try {
            // 토스페이먼츠에서 결제 상태 조회
            PaymentResponseDto tossResponse = tossPaymentClient.getPayment(paymentKey);
            
            log.info("결제 상태 조회 완료 - paymentKey: {}, status: {}", paymentKey, tossResponse.status());
            
            return tossResponse;
            
        } catch (BaseException e) {
            throw e;
            
        } catch (Exception e) {
            log.error("결제 상태 조회 실패 - paymentKey: {}", paymentKey, e);
            throw new BaseException(ExceptionCode.TOSS_PAYMENT_STATUS_FAILED);
        }
    }
    
    // 결제 취소 처리
    @Transactional
    public PaymentCancelResponseDto cancelPayment(String orderId, String cancelReason) {
        // 1. 주문 정보 조회
        Payment payment = paymentRepository.findByIdOrElseThrow(orderId);
        
        // 2. 취소 가능한 상태인지 확인
        if (payment.getStatus() != PaymentStatus.CONFIRMED) {
            throw new BaseException(ExceptionCode.PAYMENT_NOT_CANCELLABLE);
        }
        
        if (payment.getPaymentKey() == null) {
            throw new BaseException(ExceptionCode.PAYMENT_KEY_NOT_FOUND);
        }
        
        try {
            // 3. 토스페이먼츠 결제 취소 요청
            PaymentCancelResponseDto tossResponse = tossPaymentClient.cancelPayment(payment.getPaymentKey(), cancelReason);
            
            // 4. 결제 정보 업데이트 (취소 상태로 변경)
            payment.updateStatus(PaymentStatus.CANCELLED);
            payment.updateFailureReason(cancelReason);
            payment.updateCancelledAt(tossResponse.cancelledAt());
            
            // 5. 포인트 차감 (충전했던 포인트 되돌리기)
            pointService.usePoint(
                payment.getUser().getId(), 
                payment.getAmount(), 
                "결제 취소로 인한 포인트 차감 - " + payment.getOrderName()
            );
            
            log.info("결제 취소 완료 - orderId: {}, reason: {}", orderId, cancelReason);
            
            return tossResponse;
            
        } catch (BaseException e) {
            // BaseException인 경우 그대로 다시 던지기
            throw e;
            
        } catch (Exception e) {
            log.error("결제 취소 실패 - orderId: {}", orderId, e);
            throw new BaseException(ExceptionCode.TOSS_PAYMENT_CANCEL_FAILED);
        }
    }
    
    private String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "ORDER_" + timestamp + "_" + uuid;
    }
}
