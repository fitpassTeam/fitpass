package org.example.fitpass.domain.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "결제 응답 DTO")
public record PaymentResponseDto(
    @Schema(description = "결제 키", example = "tgen_20241201123456_abcd1234")
    String paymentKey,
    
    @Schema(description = "주문 ID", example = "order_20241201_123456")
    String orderId,
    
    @Schema(description = "주문명", example = "포인트 충전")
    String orderName,
    
    @Schema(description = "결제 금액", example = "10000")
    Integer amount,
    
    @Schema(description = "결제 상태", example = "DONE")
    String status,
    
    @Schema(description = "결제 승인 시간", example = "2024-12-01T14:30:00")
    LocalDateTime approvedAt,
    
    @Schema(description = "결제 수단", example = "카드")
    String method
) {
}
