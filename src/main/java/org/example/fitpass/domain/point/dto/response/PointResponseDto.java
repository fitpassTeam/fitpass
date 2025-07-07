package org.example.fitpass.domain.point.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.example.fitpass.domain.point.entity.Point;
import org.example.fitpass.domain.point.enums.PointType;

@Schema(description = "포인트 이력 응답 DTO")
public record PointResponseDto(
    @Schema(description = "포인트 이력 ID", example = "1")
    Long pointId,
    
    @Schema(description = "포인트 금액 (음수는 사용, 양수는 충전)", example = "5000")
    int amount,
    
    @Schema(description = "포인트 사용/충전 설명", example = "PT 수업료 결제")
    String description,
    
    @Schema(description = "거래 후 잔액", example = "15000")
    int balance,
    
    @Schema(description = "포인트 거래 유형", example = "USE")
    PointType pointType,
    
    @Schema(description = "거래 시간", example = "2024-12-01T14:30:00")
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
