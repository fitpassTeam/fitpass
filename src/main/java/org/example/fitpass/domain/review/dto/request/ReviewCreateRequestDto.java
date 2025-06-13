package org.example.fitpass.domain.review.dto.request;

import lombok.Getter;

public record ReviewCreateRequestDto(
    String content,
    int gymRating,
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
