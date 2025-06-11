package org.example.fitpass.domain.point.dto.request;

import lombok.Getter;

@Getter
public class PointCashOutRequestDto {
    // 현금화 할 포인트 양
    private final int amount;

    // 현금화 사유
    private final String description;

    public PointCashOutRequestDto(int amount, String description) {
        this.amount = amount;
        this.description = description;
    }
}
