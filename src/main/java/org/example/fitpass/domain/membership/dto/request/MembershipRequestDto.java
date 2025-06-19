package org.example.fitpass.domain.membership.dto.request;

public record MembershipRequestDto(
    String name,
    int price,
    String content
) {

}
