package org.example.fitpass.domain.trainer.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.trainer.dto.reqeust.TrainerUpdateRequestDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerDetailResponseDto;
import org.example.fitpass.domain.trainer.dto.reqeust.TrainerRequestDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
import org.example.fitpass.domain.trainer.service.TrainerService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/gyms/{gymId}/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    // 생성
    @PostMapping
    public ResponseEntity<ResponseMessage<TrainerResponseDto>> createTrainer(
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId,
        @Valid @RequestBody TrainerRequestDto dto) {
        TrainerResponseDto response = trainerService.createTrainer(
            user.getId(),
            gymId,
            dto.name(),
            dto.price(),
            dto.content(),
            dto.experience(),
            dto.trainerImage()
        );
        return ResponseEntity.status(SuccessCode.POST_TRAINER_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.POST_TRAINER_SUCCESS, response));
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<ResponseMessage<Page<TrainerResponseDto>>> getAllTrainer(
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId,
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        Page<TrainerResponseDto> response = trainerService.getAllTrainer(user.getId(), gymId, pageable);
        return ResponseEntity.status(SuccessCode.GET_TRAINER_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_TRAINER_SUCCESS, response));
    }

    // 단일 조회
    @GetMapping("/{trainerId}")
    public ResponseEntity<ResponseMessage<TrainerDetailResponseDto>> getTrainerById(
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId,
        @PathVariable("trainerId") Long trainerId) {
        TrainerDetailResponseDto response = trainerService.getTrainerById(user.getId(), gymId, trainerId);
        return ResponseEntity.status(SuccessCode.GET_TRAINER_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_TRAINER_SUCCESS, response));
    }

    // 수정
    @PatchMapping("/{trainerId}")
    public ResponseEntity<ResponseMessage<TrainerResponseDto>> updateTrainer(
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId,
        @PathVariable("trainerId") Long trainerId,
        @Valid @RequestBody TrainerUpdateRequestDto dto) {
        TrainerResponseDto response = trainerService.updateTrainer(
            user.getId(),
            gymId,
            trainerId,
            dto.name(),
            dto.price(),
            dto.content(),
            dto.experience(),
            dto.trainerStatus(),
            dto.trainerImage()
            );
        return ResponseEntity.status(SuccessCode.PATCH_TRAINER_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.PATCH_TRAINER_SUCCESS, response));
    }

    // 사진 수정
    @PatchMapping("/{trainerId}/photo")
    public ResponseEntity<ResponseMessage<List<String>>> updatePhoto(
        @AuthenticationPrincipal CustomUserDetails user,
        @RequestParam("images")List<MultipartFile> files,
        @PathVariable("gymId") Long gymId,
        @PathVariable("trainerId") Long trainerId) {
        List<String> response = trainerService.updatePhoto(user.getId(), files, gymId, trainerId);
        return ResponseEntity.status(SuccessCode.PATCH_TRAINER_IMAGE_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.PATCH_TRAINER_IMAGE_SUCCESS, response));
    }

    // 삭제
    @DeleteMapping("/{trainerId}")
    public ResponseEntity<ResponseMessage<Void>> deleteTrainer(
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable("gymId") Long gymId,
        @PathVariable("trainerId") Long trainerId) {
        trainerService.deleteTrainer(user.getId(), gymId, trainerId);
        return ResponseEntity.status(SuccessCode.DELETE_TRAINER_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.DELETE_TRAINER_SUCCESS));
    }
}
