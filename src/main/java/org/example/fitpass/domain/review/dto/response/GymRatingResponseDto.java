package org.example.fitpass.domain.review.dto.response;

public record GymRatingResponseDto(
    Long gymId,
    String gymName,
    Double averageGymRating,
    Long totalReviewCount
) {}
