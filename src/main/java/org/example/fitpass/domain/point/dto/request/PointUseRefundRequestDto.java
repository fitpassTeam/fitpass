package org.example.fitpass.domain.point.dto.request;


public record PointUseRefundRequestDto(
    int amount, // 사용할 포인트 양
    String description // 사용 목적 설명
) {

}
