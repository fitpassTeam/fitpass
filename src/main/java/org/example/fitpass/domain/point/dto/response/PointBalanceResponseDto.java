package org.example.fitpass.domain.point.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 잔액 응답 DTO")
public record PointBalanceResponseDto(
    @Schema(description = "현재 포인트 잔액", example = "15000")
    int balance
) {

}
