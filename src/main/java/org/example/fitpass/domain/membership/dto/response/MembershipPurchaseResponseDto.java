package org.example.fitpass.domain.membership.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.example.fitpass.domain.membership.entity.MembershipPurchase;

@Schema(description = "이용권 구매 정보 응답 DTO")
public record MembershipPurchaseResponseDto(
    @Schema(description = "구매 ID", example = "1")
    Long id,
    
    @Schema(description = "이용권 이름", example = "1개월 자유 이용권")
    String membershipName,
    
    @Schema(description = "이용권 가격 (원 단위)", example = "80000")
    int price,
    
    @Schema(description = "이용 가능 일수", example = "30")
    int durationInDays,
    
    @Schema(description = "이용 시작 날짜", example = "2025-07-03T00:00:00")
    LocalDateTime startDate,
    
    @Schema(description = "이용 종료 날짜", example = "2025-08-02T23:59:59")
    LocalDateTime endDate,
    
    @Schema(description = "현재 활성화 상태", example = "true")
    boolean isActive

) {
    public static MembershipPurchaseResponseDto from(MembershipPurchase purchase) {
        boolean isActive = purchase.getStartDate() != null && purchase.getEndDate() != null && purchase.isActive();

        return new MembershipPurchaseResponseDto(
            purchase.getId(),
            purchase.getMembership().getName(),
            purchase.getMembership().getPrice(),
            purchase.getMembership().getDurationInDays(),
            purchase.getStartDate(),
            purchase.getEndDate(),
            isActive
        );
    }
}
