package org.example.fitpass.domain.membership.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record MembershipRequestDto(
    @NotBlank String name,
    @Min(10000) int price,
    @NotBlank String content,
    @Min(1) int durationInDays
) {

}
