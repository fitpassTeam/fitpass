package org.example.fitpass.domain.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentRequestDto(
    @NotNull(message = "충전할 포인트 금액은 필수입니다")
    @Min(value = 1000, message = "최소 충전 금액은 1,000원입니다")
    Integer amount,
    
    String orderName
) {
    public PaymentRequestDto {
        if (orderName == null || orderName.trim().isEmpty()) {
            orderName = "포인트 충전";
        }
    }
}
