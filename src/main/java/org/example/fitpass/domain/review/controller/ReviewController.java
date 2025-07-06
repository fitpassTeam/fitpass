package org.example.fitpass.domain.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Review API", description = "헬스장 및 트레이너 리뷰 작성, 수정, 삭제")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 생성
    @Operation(
        summary = "리뷰 작성",
        description = "완료된 예약에 대해 헬스장과 트레이너 리뷰를 작성합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "403", description = "리뷰 작성 권한 없음"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "이미 리뷰가 존재함")
    })
    @PostMapping
    public ResponseEntity<ResponseMessage<ReviewResponseDto>> createReview (
        @PathVariable Long reservationId,
        @RequestBody ReviewCreateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ReviewResponseDto responseDto = reviewService.createReview(
            reservationId,
            userDetails.getId(),
            requestDto.content(),
            requestDto.gymRating(),
            requestDto.trainerRating());
        return ResponseEntity.status(SuccessCode.REVIEW_CREATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.REVIEW_CREATE_SUCCESS,responseDto));
    }

    // 리뷰 수정
    @Operation(
        summary = "리뷰 수정",
        description = "작성한 리뷰의 내용과 평점을 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "403", description = "리뷰 수정 권한 없음"),
        @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    @PutMapping("/{reviewId}")
    public ResponseEntity<ResponseMessage<ReviewResponseDto>> updateReview (
        @PathVariable Long reservationId,
        @PathVariable Long reviewId,
        @RequestBody ReviewUpdateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        ReviewResponseDto responseDto = reviewService.updateReview(
            reservationId,
            reviewId,
            requestDto.content(),
            requestDto.gymRating(),
            requestDto.trainerRating(),
            userDetails.getId());
        return ResponseEntity.status(SuccessCode.REVIEW_UPDATE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.REVIEW_UPDATE_SUCCESS, responseDto));
    }

    // 리뷰 삭제
    @Operation(
        summary = "리뷰 삭제",
        description = "작성한 리뷰를 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ResponseMessage<Void>> deleteReview (
        @PathVariable Long reservationId,
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        reviewService.deleteReview(reservationId, reviewId, userDetails.getId());
        return ResponseEntity.status(SuccessCode.REVIEW_DELETE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.REVIEW_DELETE_SUCCESS, null));
    }
}