package org.example.fitpass.domain.review.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.review.dto.response.ReviewDetailResponseDto;
import org.example.fitpass.domain.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/reviews")
@RestController
@RequiredArgsConstructor
public class ReviewGetController {

    private final ReviewService reviewService;

    // 리뷰 단건 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<ResponseMessage<ReviewDetailResponseDto>> getReview(
        @PathVariable Long reviewId
    ) {
        ReviewDetailResponseDto responseDto = reviewService.getReview(reviewId);
        ResponseMessage<ReviewDetailResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.REVIEW_GET_SUCCESS,responseDto);
        return ResponseEntity.status(SuccessCode.REVIEW_GET_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 사용자가 쓴 리뷰 조회
    @GetMapping("/my")
    public ResponseEntity<ResponseMessage<List<ReviewDetailResponseDto>>> getMyReviews(
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<ReviewDetailResponseDto> responseDtos = reviewService.getMyReviews(user.getId());
        ResponseMessage<List<ReviewDetailResponseDto>> responseMessage =
            ResponseMessage.success(SuccessCode.REVIEW_GET_SUCCESS, responseDtos);
        return ResponseEntity.status(SuccessCode.REVIEW_GET_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 체육관 리뷰들 조회
    @GetMapping("/gyms/{gymId}")
    public ResponseEntity<ResponseMessage<List<ReviewDetailResponseDto>>> getGymReviews(
        @PathVariable Long gymId
    ) {
        List<ReviewDetailResponseDto> responseDtos = reviewService.getGymReviews(gymId);
        ResponseMessage<List<ReviewDetailResponseDto>> responseMessage =
            ResponseMessage.success(SuccessCode.REVIEW_GET_SUCCESS, responseDtos);
        return ResponseEntity.status(SuccessCode.REVIEW_GET_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 트레이너 별 리뷰 조회
    @GetMapping("/trainers/{trainerId}")
    public ResponseEntity<ResponseMessage<List<ReviewDetailResponseDto>>> getTrainerReviews(
        @PathVariable Long trainerId
    ){
      List<ReviewDetailResponseDto> responseDtos = reviewService.getTrainerReviews(trainerId);
      ResponseMessage<List<ReviewDetailResponseDto>> responseMessage =
          ResponseMessage.success(SuccessCode.REVIEW_GET_SUCCESS, responseDtos);
      return ResponseEntity.status(SuccessCode.REVIEW_GET_SUCCESS.getHttpStatus()).body(responseMessage);
    }

}
