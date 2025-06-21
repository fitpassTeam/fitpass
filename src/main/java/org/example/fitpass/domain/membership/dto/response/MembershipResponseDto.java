package org.example.fitpass.domain.membership.dto.response;

import org.example.fitpass.domain.membership.entity.Membership;

public record MembershipResponseDto (
    Long id,
    String name,
    int price,
    String content,
    int durationInDays
) {
    public static MembershipResponseDto fromEntity(long id, String name, int price, String content, int durationInDays) {
        return new MembershipResponseDto(id, name, price, content, durationInDays);
    }

    public static MembershipResponseDto of(Membership membership){
        return new MembershipResponseDto(
        membership.getId(),
        membership.getName(),
        membership.getPrice(),
        membership.getContent(),
        membership.getDurationInDays());
    }

}
