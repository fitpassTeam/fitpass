package org.example.fitpass.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "체육관 평점 응답 DTO")
public record GymRatingResponseDto(
    @Schema(description = "체육관 ID", example = "1")
    Long gymId,
    
    @Schema(description = "체육관 이름", example = "헬스핏짐")
    String gymName,
    
    @Schema(description = "평균 체육관 평점", example = "4.5")
    Double averageGymRating,
    
    @Schema(description = "총 리뷰 개수", example = "25")
    Long totalReviewCount
) {}
