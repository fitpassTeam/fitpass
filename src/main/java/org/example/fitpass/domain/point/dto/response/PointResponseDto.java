package org.example.fitpass.domain.point.dto.response;

import java.time.LocalDateTime;
import org.example.fitpass.domain.point.entity.Point;
import org.example.fitpass.domain.point.enums.PointType;

public record PointResponseDto(
    Long pointId,
    int amount,
    String description,
    int balance,
    PointType pointType,
    LocalDateTime createdAt
) {

    public static PointResponseDto from(Point point) {
        return new PointResponseDto(
            point.getId(),
            point.getAmount(),
            point.getDescription(),
            point.getBalance(),
            point.getPointType(),
            point.getCreatedAt()
        );
    }
}
