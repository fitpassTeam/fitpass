package org.example.fitpass.domain.point.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 사용/환불 요청 DTO")
public record PointUseRefundRequestDto(
    @Schema(description = "사용/환불할 포인트 금액", example = "5000")
    int amount, // 사용할 포인트 양
    
    @Schema(description = "사용/환불 목적 설명", example = "PT 수업료 결제")
    String description // 사용 목적 설명
) {

}
