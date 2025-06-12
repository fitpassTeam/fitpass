package org.example.fitpass.domain.point.dto.response;

public record PointCashOutResponseDto(
    int requestedAmount, // 요청한 포인트
    int cashAmount, // 실제 현금화 금액(90%)
    int newBalance // 남은 포인트 잔액
) {

}
