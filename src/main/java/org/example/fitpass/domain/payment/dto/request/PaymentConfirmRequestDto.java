package org.example.fitpass.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentConfirmRequestDto(
    @NotBlank(message = "paymentKey는 필수입니다")
    String paymentKey,
    
    @NotBlank(message = "orderId는 필수입니다")
    String orderId,
    
    @NotNull(message = "amount는 필수입니다")
    Integer amount
) {
}
