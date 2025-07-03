package org.example.fitpass.domain.review.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "리뷰 조회", description = "리뷰 조회 관련 API")
public class ReviewGetController {

    private final ReviewService reviewService;

    // 리뷰 단건 조회
    @Operation(
        summary = "리뷰 단건 조회",
        description = "특정 리뷰의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 조회 성공"),
        @ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    @GetMapping("/{reviewId}")
    public ResponseEntity<ResponseMessage<ReviewDetailResponseDto>> getReview(
        @PathVariable Long reviewId
    ) {
        ReviewDetailResponseDto responseDto = reviewService.getReview(reviewId);
        return ResponseEntity.status(SuccessCode.REVIEW_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.REVIEW_GET_SUCCESS,responseDto));
    }

    // 사용자가 쓴 리뷰 조회
    @Operation(
        summary = "내 리뷰 목록 조회",
        description = "로그인한 사용자가 작성한 리뷰 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/my")
    public ResponseEntity<ResponseMessage<List<ReviewDetailResponseDto>>> getMyReviews(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ReviewDetailResponseDto> responseDtos = reviewService.getMyReviews(userDetails.getId());
        return ResponseEntity.status(SuccessCode.REVIEW_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.REVIEW_GET_SUCCESS, responseDtos));
    }

    // 체육관 리뷰들 조회
    @Operation(
        summary = "체육관 리뷰 목록 조회",
        description = "특정 체육관에 대한 리뷰 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "체육관 리뷰 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "체육관을 찾을 수 없음")
    })
    @GetMapping("/gyms/{gymId}")
    public ResponseEntity<ResponseMessage<List<ReviewDetailResponseDto>>> getGymReviews(
        @PathVariable Long gymId
    ) {
        List<ReviewDetailResponseDto> responseDtos = reviewService.getGymReviews(gymId);
        return ResponseEntity.status(SuccessCode.REVIEW_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.REVIEW_GET_SUCCESS, responseDtos));
    }

    // 트레이너 별 리뷰 조회
    @Operation(
        summary = "트레이너 리뷰 목록 조회",
        description = "특정 트레이너에 대한 리뷰 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "트레이너 리뷰 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "트레이너를 찾을 수 없음")
    })
    @GetMapping("/trainers/{trainerId}")
    public ResponseEntity<ResponseMessage<List<ReviewDetailResponseDto>>> getTrainerReviews(
        @PathVariable Long trainerId
    ){
      List<ReviewDetailResponseDto> responseDtos = reviewService.getTrainerReviews(trainerId);
      return ResponseEntity.status(SuccessCode.REVIEW_GET_SUCCESS.getHttpStatus())
          .body(ResponseMessage.success(SuccessCode.REVIEW_GET_SUCCESS, responseDtos));
    }

}
