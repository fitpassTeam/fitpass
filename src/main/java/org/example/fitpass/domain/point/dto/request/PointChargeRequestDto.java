package org.example.fitpass.domain.point.dto.request;

import lombok.Getter;

@Getter
public class PointChargeRequestDto {
    private final int amount;

    public PointChargeRequestDto(int amount) {
        this.amount = amount;
    }
}
