package org.example.fitpass.domain.review.dto.response;

import java.time.LocalDateTime;
import org.example.fitpass.domain.review.entity.Review;

public record ReviewDetailResponseDto(
    Long reviewId,
    String content,
    int gymRating,
    int trainerRating,
    Long gymId,
    String gymName,
    Long trainerId,
    String trainerName,
    String userName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ReviewDetailResponseDto from(Review review) {
        return new ReviewDetailResponseDto(
            review.getId(),
            review.getContent(),
            review.getGymRating(),
            review.getTrainerRating(),
            review.getReservation().getGym().getId(),
            review.getReservation().getGym().getName(),
            review.getReservation().getTrainer().getId(),
            review.getReservation().getTrainer().getName(),
            review.getUser().getName(),
            review.getCreatedAt(),
            review.getUpdatedAt()
        );
    }


}
