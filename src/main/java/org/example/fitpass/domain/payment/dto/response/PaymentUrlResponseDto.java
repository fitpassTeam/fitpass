package org.example.fitpass.domain.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 URL 응답 DTO")
public record PaymentUrlResponseDto(
    @Schema(description = "주문 ID", example = "order_20241201_123456")
    String orderId,
    
    @Schema(description = "결제 금액", example = "10000")
    Integer amount,
    
    @Schema(description = "주문명", example = "포인트 충전")
    String orderName,
    
    @Schema(description = "고객 이메일", example = "customer@example.com")
    String customerEmail,
    
    @Schema(description = "고객 이름", example = "김핏패스")
    String customerName,
    
    @Schema(description = "결제 성공 시 리다이렉트 URL")
    String successUrl,
    
    @Schema(description = "결제 실패 시 리다이렉트 URL")
    String failUrl
) {
}
