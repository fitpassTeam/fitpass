package org.example.fitpass.domain.payment.dto.response;

import java.time.LocalDateTime;

public record PaymentResponseDto(
    String paymentKey,
    String orderId,
    String orderName,
    Integer amount,
    String status,
    LocalDateTime approvedAt,
    String method
) {
}
