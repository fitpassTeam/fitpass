package org.example.fitpass.domain.membership.dto.response;

import org.example.fitpass.domain.membership.entity.Membership;

public record MembershipResponseDto (
    String name,
    int price,
    String content
) {
    public static MembershipResponseDto fromEntity(Membership membership) {
        return new MembershipResponseDto(
            membership.getName(),
            membership.getPrice(),
            membership.getContent()
        );
    }

    public static MembershipResponseDto of(String name, int price, String content) {
        return new MembershipResponseDto(name, price, content);
    }
}
