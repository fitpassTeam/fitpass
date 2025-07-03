package org.example.fitpass.domain.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "결제 승인 요청 DTO")
public record PaymentConfirmRequestDto(
    @Schema(description = "토스페이먼츠 결제 키", example = "tgen_20241201123456_abcd1234")
    @NotBlank(message = "paymentKey는 필수입니다")
    String paymentKey,
    
    @Schema(description = "주문 ID", example = "order_20241201_123456")
    @NotBlank(message = "orderId는 필수입니다")
    String orderId,
    
    @Schema(description = "결제 금액", example = "10000")
    @NotNull(message = "amount는 필수입니다")
    Integer amount
) {
}
