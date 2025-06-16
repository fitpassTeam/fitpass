package org.example.fitpass.domain.review.dto.request;

public record ReviewUpdateRequestDto(
    String content,
    int gymRating,
    int trainerRating
) {}
