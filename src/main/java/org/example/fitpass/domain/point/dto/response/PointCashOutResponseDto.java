package org.example.fitpass.domain.point.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 현금화 응답 DTO")
public record PointCashOutResponseDto(
    @Schema(description = "현금화 요청 포인트", example = "10000")
    int requestedAmount, // 요청한 포인트
    
    @Schema(description = "실제 현금화 금액 (90%)", example = "9000")
    int cashAmount, // 실제 현금화 금액(90%)
    
    @Schema(description = "현금화 후 포인트 잔액", example = "5000")
    int newBalance // 남은 포인트 잔액
) {

}
