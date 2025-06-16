package org.example.fitpass.domain.review.controller;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.review.dto.request.ReviewCreateRequestDto;
import org.example.fitpass.domain.review.dto.request.ReviewUpdateRequestDto;
import org.example.fitpass.domain.review.dto.response.ReviewResponseDto;
import org.example.fitpass.domain.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/reservations/{reservationId}/reviews")
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 생성
    @PostMapping
    public ResponseEntity<ResponseMessage<ReviewResponseDto>> createReview (
        @PathVariable Long reservationId,
        @RequestBody ReviewCreateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        ReviewResponseDto responseDto = reviewService.createReview(
            reservationId,
            user.getId(),
            requestDto.content(),
            requestDto.gymRating(),
            requestDto.trainerRating());
        ResponseMessage<ReviewResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.REVIEW_CREATE_SUCCESS,responseDto);
        return ResponseEntity.status(SuccessCode.REVIEW_CREATE_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<ResponseMessage<ReviewResponseDto>> updateReview (
        @PathVariable Long reservationId,
        @PathVariable Long reviewId,
        @RequestBody ReviewUpdateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        ReviewResponseDto responseDto = reviewService.updateReview(
            reservationId,
            reviewId,
            requestDto.content(),
            requestDto.gymRating(),
            requestDto.trainerRating(),
            user.getId());
        ResponseMessage<ReviewResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.REVIEW_UPDATE_SUCCESS, responseDto);
        return ResponseEntity.status(SuccessCode.REVIEW_UPDATE_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ResponseMessage<Void>> deleteReview (
        @PathVariable Long reservationId,
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        reviewService.deleteReview(reservationId, reviewId, user.getId());
        ResponseMessage<Void> responseMessage =
            ResponseMessage.success(SuccessCode.REVIEW_DELETE_SUCCESS, null);
        return ResponseEntity.status(SuccessCode.REVIEW_DELETE_SUCCESS.getHttpStatus()).body(responseMessage);
    }

}
