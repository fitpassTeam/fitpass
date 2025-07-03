package org.example.fitpass.domain.point.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포인트 충전 요청 DTO")
public record PointChargeRequestDto(
    @Schema(description = "충전할 포인트 금액", example = "10000")
    int amount
) {

}
