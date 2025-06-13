package org.example.fitpass.domain.review.dto.response;

public record TrainerRatingResponseDto(
    Long trainerId,
    String trainerName,
    double averageTrainerRating,
    int totalReviewCount
) {}
