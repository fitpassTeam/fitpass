package org.example.fitpass.domain.membership.dto.response;

import java.time.LocalDateTime;
import org.example.fitpass.domain.membership.entity.MembershipPurchase;

public record MembershipPurchaseResponseDto(
    String membershipName,
    int price,
    LocalDateTime startDate,
    LocalDateTime endDate
) {
    public static MembershipPurchaseResponseDto from(MembershipPurchase purchase) {
        return new MembershipPurchaseResponseDto(
            purchase.getMembership().getName(),
            purchase.getMembership().getPrice(),
            purchase.getStartDate(),
            purchase.getEndDate()
        );
    }
}
