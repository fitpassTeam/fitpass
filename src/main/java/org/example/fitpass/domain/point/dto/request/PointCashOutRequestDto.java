package org.example.fitpass.domain.point.dto.request;

import lombok.Getter;

public record PointCashOutRequestDto(
    int amount, // 현금화 할 포인트 양
    String description // 현금화 사유
) {

}
