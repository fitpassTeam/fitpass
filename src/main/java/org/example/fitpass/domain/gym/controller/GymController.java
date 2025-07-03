package org.example.fitpass.domain.gym.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.dto.PageResponse;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.gym.dto.request.GymRequestDto;
import org.example.fitpass.domain.gym.dto.response.GymDetailResponDto;
import org.example.fitpass.domain.gym.dto.response.GymResDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.dto.response.GymStatusResponseDto;
import org.example.fitpass.domain.gym.service.GymService;
import org.example.fitpass.domain.review.dto.response.GymRatingResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/gyms")
@Tag(name = "GYM API", description = "체육관 ")
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

    @Operation(
        summary = "체육관 생성",
        description = "요청 본문에 체육관 정보를 입력하여 새로운 체육관을 등록합니다. 인증된 사용자(트레이너)만 체육관을 등록할 수 있습니다."
    )
    @PostMapping
    public ResponseEntity<ResponseMessage<GymStatusResponseDto>> postGym(
        @Valid @RequestBody GymRequestDto request,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        GymStatusResponseDto response = gymService.postGym(
            request.city(),
            request.district(),
            request.detailAddress(),
            request.name(),
            request.content(),
            request.number(),
            request.gymImage(),
            request.openTime(),
            request.closeTime(),
            request.summary(),
            userDetails.getId()
        );
        return ResponseEntity.status(SuccessCode.GYM_REQUEST_POST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_REQUEST_POST_SUCCESS, response));
    }

    @Operation(
        summary = "체육관 상세 조회",
        description = "체육관 ID를 이용해 특정 체육관의 상세 정보를 조회합니다."
    )
    @GetMapping("/{gymId}")
    public ResponseEntity<ResponseMessage<GymDetailResponDto>> getGym(@PathVariable Long gymId) {
        GymDetailResponDto response = gymService.getGym(gymId);
        return ResponseEntity.status(SuccessCode.GYM_FIND_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_FIND_SUCCESS, response));
    }

    @Operation(
        summary = "전체 체육관 목록 조회",
        description = "페이지네이션을 사용하여 전체 체육관 리스트를 조회합니다. 로그인 여부에 따라 좋아요 여부도 함께 반환됩니다."
    )
    @GetMapping
    public ResponseEntity<ResponseMessage<PageResponse<GymResponseDto>>> getAllGyms(
        @PageableDefault(page = 0, size = 10) Pageable pageable,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        Page<GymResponseDto> response = gymService.getAllGyms(pageable, userId);
        PageResponse<GymResponseDto> pageResponse = new PageResponse<>(response);
        return ResponseEntity.status(SuccessCode.GYM_FIND_ALL_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_FIND_ALL_SUCCESS, pageResponse));
    }

    @Operation(
        summary = "체육관 정보 수정",
        description = "체육관 ID를 기준으로 체육관의 이름, 번호, 주소, 운영 시간 등의 정보를 수정합니다. 체육관 생성 당사자만 수정 가능합니다."
    )
    @PatchMapping("/{gymId}")
    public ResponseEntity<ResponseMessage<GymResDto>> updateGym(
        @RequestBody GymRequestDto request,
        @PathVariable Long gymId,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        GymResDto response = gymService.updateGym(
            request.name(),
            request.number(),
            request.content(),
            request.city(),
            request.district(),
            request.detailAddress(),
            request.openTime(),
            request.closeTime(),
            request.summary(),
            request.gymImage(),
            gymId,
            userDetails.getId()
        );
        return ResponseEntity.status(SuccessCode.GYM_EDIT_INFO_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_EDIT_INFO_SUCCESS, response));
    }

    @Operation(
        summary = "체육관 삭제",
        description = "해당 체육관 ID의 체육관을 삭제합니다. 체육관 생성 당사자만 삭제 가능합니다."
    )
    @DeleteMapping("/{gymId}")
    public ResponseEntity<ResponseMessage<Void>> deleteGym(
        @PathVariable Long gymId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        gymService.deleteGym(gymId, userDetails.getId());
        return ResponseEntity.status(SuccessCode.GYM_DELETE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_DELETE_SUCCESS));
    }

    @Operation(
        summary = "체육관 별점 조회",
        description = "해당 체육관의 평균 별점과 리뷰 수를 조회합니다. 로그인한 사용자 기준으로 즐겨찾기 여부도 함께 반환됩니다."
    )
    @GetMapping("/{gymId}/rating")
    public ResponseEntity<ResponseMessage<GymRatingResponseDto>> getGymRating(
        @PathVariable Long gymId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GymRatingResponseDto response = gymService.getGymRating(gymId, userDetails.getId());
        return ResponseEntity.status(SuccessCode.GYM_RATING_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_RATING_GET_SUCCESS, response));
    }
}
