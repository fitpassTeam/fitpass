package org.example.fitpass.domain.point.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
public class PointUseRefundRequestDto {

    // 사용할 포인트 양
    private final int amount;

    // 사용 목적
    private final String description;

    public PointUseRefundRequestDto(int amount, String description) {
        this.amount = amount;
        this.description = description;
    }
}
