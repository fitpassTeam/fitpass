package org.example.fitpass.domain.trainer.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.gym.dto.request.GymPhotoUpdateRequestDto;
import org.example.fitpass.domain.trainer.dto.reqeust.TrainerUpdateRequestDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerDetailResponseDto;
import org.example.fitpass.domain.trainer.dto.reqeust.TrainerRequestDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
import org.example.fitpass.domain.trainer.service.TrainerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gyms/{gymId}/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    //생성
    @PostMapping
    public ResponseEntity<ResponseMessage<TrainerResponseDto>> createTrainer(
        @Valid @RequestBody TrainerRequestDto request) {
        TrainerResponseDto response = trainerService.createTrainer(
            request.getName(),
            request.getPrice(),
            request.getContent(),
            request.getTrainerImage()
        );
        ResponseMessage<TrainerResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.POST_TRAINER_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.POST_TRAINER_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    //전체 조회
    @GetMapping
    public ResponseEntity<ResponseMessage<Page<TrainerResponseDto>>> getAllTrainer(
        @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<TrainerResponseDto> response = trainerService.getAllTrainers(pageable);
        ResponseMessage<Page<TrainerResponseDto>> responseMessage =
            ResponseMessage.success(SuccessCode.GET_TRAINER_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.GET_TRAINER_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    //단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage<TrainerDetailResponseDto>> getTrainerById(
        @PathVariable("id") Long id) {
        TrainerDetailResponseDto response = trainerService.findById(id);
        ResponseMessage<TrainerDetailResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.GET_TRAINER_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.GET_TRAINER_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    //수정
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseMessage<TrainerResponseDto>> updateTrainer(
        @PathVariable("id") Long id,
        @Valid @RequestBody TrainerUpdateRequestDto dto) {
        TrainerResponseDto response = trainerService.updateTrainer(id, dto);
        ResponseMessage<TrainerResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.PATCH_TRAINER_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.PATCH_TRAINER_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    //사진 수정
    @PutMapping("/{id}/photo")
    public ResponseEntity<ResponseMessage<Void>> updatePhoto(
        @Valid @RequestBody GymPhotoUpdateRequestDto request,
        @PathVariable Long id) {
        trainerService.updatePhoto(request.getPhotoUrls(), id);
        ResponseMessage<Void> responseMessage =
            ResponseMessage.success(SuccessCode.PATCH_TRAINER_IMAGE_SUCCESS);
        return ResponseEntity.status(SuccessCode.PATCH_TRAINER_IMAGE_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }

    //삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage<Void>> deleteTrainer(
        @PathVariable("id") Long id) {
        trainerService.deleteItem(id);
        ResponseMessage<Void> responseMessage =
            ResponseMessage.success(SuccessCode.DELETE_TRAINER_SUCCESS);
        return ResponseEntity.status(SuccessCode.DELETE_TRAINER_SUCCESS.getHttpStatus())
            .body(responseMessage);
    }
}
