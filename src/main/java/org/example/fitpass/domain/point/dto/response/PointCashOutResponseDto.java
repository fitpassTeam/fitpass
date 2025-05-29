package org.example.fitpass.domain.point.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PointCashOutResponseDto {
    // 요청한 포인트
    private int requestedAmount;

    // 실제 현금화 금액 (90%)
    private int cashAmount;

    // 남은 포인트 잔액
    private int newBalance;
}
