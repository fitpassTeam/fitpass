package org.example.fitpass.domain.point.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointUseRefundRequestDto {

    // 사용할 포인트 양
    private int amount;

    // 사용 목적
    private String description;

}
