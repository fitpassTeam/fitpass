package org.example.fitpass.domain.review.dto.response;

import java.time.LocalDateTime;
import org.example.fitpass.domain.review.entity.Review;

public record ReviewResponseDto(
    Long reviewId,
    String content,
    int gymRating,
    int trainerRating,
    String gymName,
    String trainerName,
    String userName,
    LocalDateTime createdAt
) {
    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(
            review.getId(),
            review.getContent(),
            review.getGymRating(),
            review.getTrainerRating(),
            review.getReservation().getGym().getName(),
            review.getReservation().getTrainer().getName(),
            review.getUser().getName(),
            review.getCreatedAt()
        );
    }
}
