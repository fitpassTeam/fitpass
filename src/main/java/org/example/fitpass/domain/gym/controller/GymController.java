package org.example.fitpass.domain.gym.controller;

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
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

    @PostMapping
    public ResponseEntity<ResponseMessage<GymStatusResponseDto>> postGym(
        @Valid @RequestBody GymRequestDto request,
        @AuthenticationPrincipal CustomUserDetails user) {
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
            user.getId()
        );
        return ResponseEntity.status(SuccessCode.GYM_REQUEST_POST_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_REQUEST_POST_SUCCESS, response));
    }

    @GetMapping("/{gymId}")
    public ResponseEntity<ResponseMessage<GymDetailResponDto>> getGym(@PathVariable Long gymId) {
        GymDetailResponDto response = gymService.getGym(gymId);
        return ResponseEntity.status(SuccessCode.GYM_FIND_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_FIND_SUCCESS, response));
    }

    @GetMapping
    public ResponseEntity<ResponseMessage<PageResponse<GymResponseDto>>> getAllGyms(
        @PageableDefault(page = 0, size = 10) Pageable pageable,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = (user != null) ? user.getId() : null;
        Page<GymResponseDto> response = gymService.getAllGyms(pageable, userId);
        PageResponse<GymResponseDto> pageResponse = new PageResponse<>(response);
        return ResponseEntity.status(SuccessCode.GYM_FIND_ALL_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_FIND_ALL_SUCCESS, pageResponse));
    }

    @PatchMapping("/{gymId}/photo")
    public ResponseEntity<ResponseMessage<List<String>>> updatePhoto(
        @RequestParam("images")List<MultipartFile> files,
        @PathVariable Long gymId,
        @AuthenticationPrincipal CustomUserDetails user) {
        List<String> updatedImageUrls = gymService.updatePhoto(files, gymId, user.getId());
        return ResponseEntity.status(SuccessCode.GYM_EDIT_PHOTO_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_EDIT_PHOTO_SUCCESS, updatedImageUrls));
    }

    @PatchMapping("/{gymId}")
    public ResponseEntity<ResponseMessage<GymResDto>> updateGym(
        @RequestBody GymRequestDto request,
        @PathVariable Long gymId,
        @AuthenticationPrincipal CustomUserDetails user) {
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
            user.getId()
        );
        return ResponseEntity.status(SuccessCode.GYM_EDIT_INFO_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_EDIT_INFO_SUCCESS, response));
    }

    @DeleteMapping("/{gymId}")
    public ResponseEntity<ResponseMessage<Void>> deleteGym(
        @PathVariable Long gymId,
        @AuthenticationPrincipal CustomUserDetails user) {
        gymService.deleteGym(gymId, user.getId());
        return ResponseEntity.status(SuccessCode.GYM_DELETE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_DELETE_SUCCESS));
    }

    @GetMapping("/{gymId}/rating")
    public ResponseEntity<ResponseMessage<GymRatingResponseDto>> getGymRating(
        @PathVariable Long gymId,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        GymRatingResponseDto response = gymService.getGymRating(gymId, user.getId());
        return ResponseEntity.status(SuccessCode.GYM_RATING_GET_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GYM_RATING_GET_SUCCESS, response));
    }
}
