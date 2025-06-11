package org.example.fitpass.domain.point.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.example.fitpass.domain.point.entity.Point;
import org.example.fitpass.domain.point.enums.PointType;

@Getter
public class PointResponseDto {

    private final Long pointId;
    private final int amount;
    private final String description;
    private final int balance;
    private final PointType pointType;
    private final LocalDateTime createdAt;

    public PointResponseDto(Long pointId, int amount, String description, int balance,
        PointType pointType, LocalDateTime createdAt) {
        this.pointId = pointId;
        this.amount = amount;
        this.description = description;
        this.balance = balance;
        this.pointType = pointType;
        this.createdAt = createdAt;
    }

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
