package org.example.fitpass.domain.payment.dto.response;

public record PaymentUrlResponseDto(
    String orderId,
    Integer amount,
    String orderName,
    String customerEmail,
    String customerName,
    String successUrl,
    String failUrl
) {
}
