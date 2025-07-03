package org.example.fitpass.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 생성 요청 DTO")
public record ReviewCreateRequestDto(
    @Schema(description = "리뷰 내용", example = "시설이 깨끗하고 트레이너분이 친절합니다.")
    String content,
    
    @Schema(description = "체육관 평점 (1-5)", example = "5")
    int gymRating,
    
    @Schema(description = "트레이너 평점 (1-5)", example = "5")
    int trainerRating) {

    public ReviewCreateRequestDto {
        if (gymRating < 1 || gymRating > 5) {
            throw new IllegalArgumentException("체육관 평점은 1-5 사이여야 합니다");
        }
        if (trainerRating < 1 || trainerRating > 5) {
            throw new IllegalArgumentException("트레이너 평점은 1-5 사이여야 합니다");
        }
    }

}
