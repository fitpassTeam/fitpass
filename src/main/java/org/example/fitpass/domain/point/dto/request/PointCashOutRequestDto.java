package org.example.fitpass.domain.point.dto.request;

import lombok.Getter;

@Getter
public class PointCashOutRequestDto {
    // 현금화 할 포인트 양
    private int amount;

    // 현금화 사유
    private String description;
}
