package org.example.fitpass.domain.membership.dto.response;

import java.time.LocalDateTime;
import org.example.fitpass.domain.membership.entity.MembershipPurchase;

public record MembershipPurchaseResponseDto(
    Long id,
    String membershipName,
    int price,
    int durationInDays,
    LocalDateTime startDate,
    LocalDateTime endDate,
    boolean isActive

) {
    public static MembershipPurchaseResponseDto from(MembershipPurchase purchase) {
        return new MembershipPurchaseResponseDto(
            purchase.getId(),
            purchase.getMembership().getName(),
            purchase.getMembership().getPrice(),
            purchase.getMembership().getDurationInDays(),
            purchase.getStartDate(),
            purchase.getEndDate(),
            purchase.isActive(LocalDateTime.now())
        );
    }
}
