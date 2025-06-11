package org.example.fitpass.domain.point.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PointCashOutResponseDto {
    // 요청한 포인트
    private final int requestedAmount;

    // 실제 현금화 금액 (90%)
    private final int cashAmount;

    // 남은 포인트 잔액
    private final int newBalance;

    public PointCashOutResponseDto(int requestedAmount, int cashAmount, int newBalance) {
        this.requestedAmount = requestedAmount;
        this.cashAmount = cashAmount;
        this.newBalance = newBalance;
    }
}
