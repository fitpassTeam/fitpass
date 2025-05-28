package org.example.fitpass.domain.point.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.example.fitpass.domain.point.entity.Point;
import org.example.fitpass.domain.point.enums.PointType;

@Getter
@Builder
public class PointResponseDto {

    private Long id;
    private int amount;
    private String description;
    private int balance;
    private PointType pointType;
    private LocalDateTime createdAt;

    public static PointResponseDto from(Point point) {
        return PointResponseDto.builder()
            .id(point.getId())
            .amount(point.getAmount())
            .description(point.getDescription())
            .balance(point.getBalance())
            .pointType(point.getPointType())
            .createdAt(point.getCreatedAt())
            .build();
    }
}
