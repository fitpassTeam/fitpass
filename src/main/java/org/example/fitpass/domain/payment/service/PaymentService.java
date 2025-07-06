package org.example.fitpass.domain.payment.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.payment.client.TossPaymentClient;
import org.example.fitpass.domain.payment.config.TossPaymentConfig;
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
        log.info("[PAYMENT PREPARE] 결제 준비 시작 - USER_ID: {}, AMOUNT: {}, ORDER_NAME: {}", 
                userId, amount, orderName);
                
        User user = userRepository.findByIdOrElseThrow(userId);
        
        // 고유한 주문 ID 생성 (UUID + timestamp)
        String orderId = generateOrderId();
        
        log.info("[PAYMENT ORDER_ID] 주문 ID 생성 완료 - ORDER_ID: {}, USER_ID: {}", orderId, userId);
        
        // 결제 정보 저장 (PENDING 상태)
        Payment payment = Payment.builder()
            .orderId(orderId)
            .orderName(orderName)
            .amount(amount)
            .status(PaymentStatus.PENDING)
            .user(user)
            .build();
        
        paymentRepository.save(payment);
        
        log.info("[PAYMENT PREPARE SUCCESS] 결제 준비 완료 - ORDER_ID: {}, USER: {}, AMOUNT: {}", 
                orderId, user.getEmail(), amount);
        
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
        log.info("[PAYMENT CONFIRM] 결제 승인 시작 - ORDER_ID: {}, PAYMENT_KEY: {}, AMOUNT: {}", 
                orderId, paymentKey, amount);
                
        // 1. 주문 정보 조회
        Payment payment = paymentRepository.findByIdOrElseThrow(orderId);

        log.info("[PAYMENT VALIDATION] 결제 금액 검증 - ORDER_ID: {}, DB_AMOUNT: {}, REQUEST_AMOUNT: {}", 
                orderId, payment.getAmount(), amount);

        // 2. 결제 금액 검증
        if (!payment.getAmount().equals(amount)) {
            log.warn("[PAYMENT CONFIRM FAILED] 결제 금액 불일치 - ORDER_ID: {}, EXPECTED: {}, ACTUAL: {}", 
                    orderId, payment.getAmount(), amount);
            throw new BaseException(ExceptionCode.PAYMENT_AMOUNT_MISMATCH);
        }

        try {
            log.info("[PAYMENT TOSS_API] 토스페이먼츠 승인 요청 시작 - ORDER_ID: {}", orderId);
            
            // 3. 토스페이먼츠 결제 승인 요청
            PaymentResponseDto tossResponse = tossPaymentClient.confirmPayment(paymentKey, orderId, amount);

            log.info("[PAYMENT TOSS_API SUCCESS] 토스페이먼츠 승인 성공 - ORDER_ID: {}, STATUS: {}", 
                    orderId, tossResponse.status());

            // 4. 결제 정보 업데이트
            payment.updatePaymentKey(paymentKey);
            payment.updateStatus(PaymentStatus.CONFIRMED);
            payment.updateMethod(tossResponse.method());
            payment.updateApprovedAt(tossResponse.approvedAt());

            log.info("[PAYMENT POINT_CHARGE] 포인트 충전 시작 - USER_ID: {}, AMOUNT: {}", 
                    payment.getUser().getId(), payment.getAmount());

            // 5. 포인트 충전
            pointService.chargePoint(
                payment.getUser().getId(),
                payment.getAmount(),
                "토스페이먼츠 충전 - " + payment.getOrderName()
            );

            log.info("[PAYMENT CONFIRM SUCCESS] 결제 승인 완료 - ORDER_ID: {}, USER: {}, AMOUNT: {}", 
                    orderId, payment.getUser().getEmail(), amount);

            return tossResponse;

        } catch (BaseException e) {
            // BaseException인 경우 그대로 다시 던지기 (토스 API 에러 등)
            payment.updateStatus(PaymentStatus.FAILED);
            payment.updateFailureReason(e.getMessage());
            
            log.error("[PAYMENT CONFIRM FAILED] 결제 승인 실패 (BaseException) - ORDER_ID: {}, ERROR: {}", 
                    orderId, e.getMessage());
            throw e;

        } catch (Exception e) {
            // 그 외 예상치 못한 예외
            payment.updateStatus(PaymentStatus.FAILED);
            payment.updateFailureReason(e.getMessage());

            log.error("[PAYMENT CONFIRM FAILED] 결제 승인 실패 (Exception) - ORDER_ID: {}, ERROR: {}", 
                    orderId, e.getMessage(), e);
            throw new BaseException(ExceptionCode.TOSS_PAYMENT_CONFIRM_FAILED);
        }
    }

    // 결제 실패 처리
    @Transactional
    public void failPayment(String orderId, String failureReason) {
        log.info("[PAYMENT FAIL] 결제 실패 처리 시작 - ORDER_ID: {}, REASON: {}", orderId, failureReason);
        
        Payment payment = paymentRepository.findByIdOrElseThrow(orderId);

        payment.updateStatus(PaymentStatus.FAILED);
        payment.updateFailureReason(failureReason);

        log.info("[PAYMENT FAIL SUCCESS] 결제 실패 처리 완료 - ORDER_ID: {}, USER: {}, REASON: {}", 
                orderId, payment.getUser().getEmail(), failureReason);
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
        log.info("[PAYMENT CANCEL] 결제 취소 시작 - ORDER_ID: {}, REASON: {}", orderId, cancelReason);
        
        // 1. 주문 정보 조회
        Payment payment = paymentRepository.findByIdOrElseThrow(orderId);
        
        log.info("[PAYMENT CANCEL_VALIDATION] 취소 가능 여부 확인 - ORDER_ID: {}, STATUS: {}, PAYMENT_KEY: {}", 
                orderId, payment.getStatus(), payment.getPaymentKey());
        
        // 2. 취소 가능한 상태인지 확인
        if (payment.getStatus() != PaymentStatus.CONFIRMED) {
            log.warn("[PAYMENT CANCEL FAILED] 취소 불가능한 상태 - ORDER_ID: {}, STATUS: {}", 
                    orderId, payment.getStatus());
            throw new BaseException(ExceptionCode.PAYMENT_NOT_CANCELLABLE);
        }
        
        if (payment.getPaymentKey() == null) {
            log.warn("[PAYMENT CANCEL FAILED] 결제키 없음 - ORDER_ID: {}", orderId);
            throw new BaseException(ExceptionCode.PAYMENT_KEY_NOT_FOUND);
        }
        
        try {
            log.info("[PAYMENT TOSS_CANCEL] 토스페이먼츠 취소 요청 시작 - ORDER_ID: {}, PAYMENT_KEY: {}", 
                    orderId, payment.getPaymentKey());
            
            // 3. 토스페이먼츠 결제 취소 요청
            PaymentCancelResponseDto tossResponse = tossPaymentClient.cancelPayment(payment.getPaymentKey(), cancelReason);
            
            log.info("[PAYMENT TOSS_CANCEL SUCCESS] 토스페이먼츠 취소 성공 - ORDER_ID: {}", orderId);
            
            // 4. 결제 정보 업데이트 (취소 상태로 변경)
            payment.updateStatus(PaymentStatus.CANCELLED);
            payment.updateFailureReason(cancelReason);
            payment.updateCancelledAt(tossResponse.cancelledAt());
            
            log.info("[PAYMENT POINT_DEDUCT] 포인트 차감 시작 - USER_ID: {}, AMOUNT: {}", 
                    payment.getUser().getId(), payment.getAmount());
            
            // 5. 포인트 차감 (충전했던 포인트 되돌리기)
            pointService.usePoint(
                payment.getUser().getId(), 
                payment.getAmount(), 
                "결제 취소로 인한 포인트 차감 - " + payment.getOrderName()
            );
            
            log.info("[PAYMENT CANCEL SUCCESS] 결제 취소 완료 - ORDER_ID: {}, USER: {}, AMOUNT: {}", 
                    orderId, payment.getUser().getEmail(), payment.getAmount());
            
            return tossResponse;
            
        } catch (BaseException e) {
            log.error("[PAYMENT CANCEL FAILED] 결제 취소 실패 (BaseException) - ORDER_ID: {}, ERROR: {}", 
                    orderId, e.getMessage());
            throw e;
            
        } catch (Exception e) {
            log.error("[PAYMENT CANCEL FAILED] 결제 취소 실패 (Exception) - ORDER_ID: {}, ERROR: {}", 
                    orderId, e.getMessage(), e);
            throw new BaseException(ExceptionCode.TOSS_PAYMENT_CANCEL_FAILED);
        }
    }
    
    private String generateOrderId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "ORDER_" + timestamp + "_" + uuid;
    }
}
