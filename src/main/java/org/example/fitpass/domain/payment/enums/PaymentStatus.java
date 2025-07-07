package org.example.fitpass.domain.payment.enums;

public enum PaymentStatus {
    PENDING,        // 결제 대기
    CONFIRMED,      // 결제 승인
    FAILED,         // 결제 실패
    CANCELLED,      // 결제 취소
    REFUNDED        // 환불 완료
}
