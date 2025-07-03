package org.example.fitpass.domain.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "결제 요청 DTO")
public record PaymentRequestDto(
    @Schema(description = "결제 금액 (최소 1,000원)", example = "10000")
    @NotNull(message = "충전할 포인트 금액은 필수입니다")
    @Min(value = 1000, message = "최소 충전 금액은 1,000원입니다")
    Integer amount,
    
    @Schema(description = "주문명", example = "포인트 충전")
    String orderName
) {
    public PaymentRequestDto {
        if (orderName == null || orderName.trim().isEmpty()) {
            orderName = "포인트 충전";
        }
    }
}
