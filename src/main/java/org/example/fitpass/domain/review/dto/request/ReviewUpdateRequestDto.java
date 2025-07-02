package org.example.fitpass.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리뷰 수정 요청 DTO")
public record ReviewUpdateRequestDto(
    @Schema(description = "수정할 리뷰 내용", example = "시설이 더욱 깨끗해졌고 트레이너분도 매우 친절합니다.")
    String content,
    
    @Schema(description = "수정할 체육관 평점 (1-5)", example = "5")
    int gymRating,
    
    @Schema(description = "수정할 트레이너 평점 (1-5)", example = "5")
    int trainerRating
) {}
