package org.example.fitpass.domain.gym.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.domain.gym.dto.request.GymPhotoUpdateRequestDto;
import org.example.fitpass.domain.gym.dto.request.GymRequestDto;
import org.example.fitpass.domain.gym.dto.response.GymDetailResponDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.service.GymService;
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
@RequestMapping("/gym")
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

    @PostMapping
    public ResponseEntity<ResponseMessage<GymResponseDto>> postGym(
        @Valid @RequestBody GymRequestDto request){
        GymResponseDto response = gymService.post(
            request.getAddress(),
            request.getName(),
            request.getContent(),
            request.getNumber(),
            request.getGymImage(),
            request.getOpenTime(),
            request.getCloseTime()
        );
        ResponseMessage<GymResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.GYM_POST_SUCCESS,response);
        return ResponseEntity.status(SuccessCode.GYM_POST_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    @GetMapping("/{gymId}")
    public ResponseEntity<ResponseMessage<GymDetailResponDto>> getGym(@PathVariable Long gymId){
        GymDetailResponDto response = gymService.getGym(gymId);
        ResponseMessage<GymDetailResponDto> responseMessage =
            ResponseMessage.success(SuccessCode.GYM_FIND_SUCCESS,response);
        return ResponseEntity.status(SuccessCode.GYM_FIND_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    @GetMapping
    public ResponseEntity<ResponseMessage<Page<GymResponseDto>>> getAllGyms(
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ){
        Page<GymResponseDto> response = gymService.getAllGyms(pageable);
        ResponseMessage<Page<GymResponseDto>> responseMessage =
            ResponseMessage.success(SuccessCode.GYM_FIND_ALL_SUCCESS,response);
        return ResponseEntity.status(SuccessCode.GYM_FIND_ALL_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    @PutMapping("/{gymId}/photo")
    public ResponseEntity<ResponseMessage<Void>> updatePhoto(
        @Valid @RequestBody GymPhotoUpdateRequestDto request,
        @PathVariable Long gymId){
        gymService.updatePhoto(request.getPhotoUrls(), gymId);
        ResponseMessage<Void> responseMessage =
            ResponseMessage.success(SuccessCode.GYM_EDIT_PHOTO_SUCCESS);
        return ResponseEntity.status(SuccessCode.GYM_EDIT_PHOTO_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    @PatchMapping("/{gymId}")
    public ResponseEntity<ResponseMessage<GymResponseDto>> updateGym(
        @RequestBody GymRequestDto request,
        @PathVariable Long gymId){
        GymResponseDto response = gymService.updateGym(
            request.getName(),
            request.getNumber(),
            request.getContent(),
            request.getAddress(),
            request.getOpenTime(),
            request.getCloseTime(),
            gymId
        );
        ResponseMessage<GymResponseDto> responseMessage =
            ResponseMessage.success(SuccessCode.GYM_EDIT_INFO_SUCCESS, response);
        return ResponseEntity.status(SuccessCode.GYM_EDIT_INFO_SUCCESS.getHttpStatus()).body(responseMessage);
    }

    @DeleteMapping("/{gymId}")
    public ResponseEntity<ResponseMessage<Void>> deleteGym(@PathVariable Long gymId){
        gymService.delete(gymId);
        ResponseMessage<Void> responseMessage =
            ResponseMessage.success(SuccessCode.GYM_DELETE_SUCCESS);
        return ResponseEntity.status(SuccessCode.GYM_DELETE_SUCCESS.getHttpStatus()).body(responseMessage);
    }
}
