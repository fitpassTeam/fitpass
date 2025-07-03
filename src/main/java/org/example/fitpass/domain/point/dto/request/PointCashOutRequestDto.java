package org.example.fitpass.domain.point.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 현금화 요청 DTO")
public record PointCashOutRequestDto(
    @Schema(description = "현금화할 포인트 금액", example = "10000")
    int amount, // 현금화 할 포인트 양
    
    @Schema(description = "현금화 사유", example = "급한 용돈 필요")
    String description // 현금화 사유
) {

}
