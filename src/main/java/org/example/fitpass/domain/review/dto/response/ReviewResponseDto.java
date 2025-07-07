package org.example.fitpass.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.example.fitpass.domain.review.entity.Review;

@Schema(description = "리뷰 응답 DTO")
public record ReviewResponseDto(
    @Schema(description = "리뷰 ID", example = "1")
    Long reviewId,
    
    @Schema(description = "리뷰 내용", example = "시설이 깨끗하고 트레이너분이 친절합니다.")
    String content,
    
    @Schema(description = "체육관 평점", example = "5")
    int gymRating,
    
    @Schema(description = "트레이너 평점", example = "5")
    int trainerRating,
    
    @Schema(description = "체육관 이름", example = "헬스핏짐")
    String gymName,
    
    @Schema(description = "트레이너 이름", example = "김트레이너")
    String trainerName,
    
    @Schema(description = "작성자 이름", example = "김핏패스")
    String userName,
    
    @Schema(description = "작성 시간", example = "2024-12-01T14:30:00")
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
