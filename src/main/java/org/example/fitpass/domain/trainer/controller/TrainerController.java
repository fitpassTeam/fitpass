package org.example.fitpass.domain.trainer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.SuccessCode;
import org.example.fitpass.common.response.ResponseMessage;
import org.example.fitpass.common.security.CustomUserDetails;
import org.example.fitpass.domain.trainer.dto.reqeust.TrainerRequestDto;
import org.example.fitpass.domain.trainer.dto.reqeust.TrainerUpdateRequestDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerDetailResponseDto;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Trainer API", description = "트레이너 관리에 대한 설명입니다.")
@RequestMapping("/gyms/{gymId}/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;

    @Operation(summary = "트레이너 등록",
        description = "필요 파라미터 : 체육관 ID, 이름, PT 비용, 트레이너 정보, 경력, 트레이너 사진")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "트레이너 등록 성공"),
        @ApiResponse(responseCode = "404", description = "체육관을 찾을 수 없음")
    })
    @Parameter(name = "gymId", description = "체육관 ID")
    @Parameter(name = "name", description = "트레이너 이름")
    @Parameter(name = "price", description = "트레이너 PT 비용")
    @Parameter(name = "content", description = "트레이너 정보")
    @Parameter(name = "experience", description = "트레이너 경력")
    @Parameter(name = "trainerImage", description = "트레이너 사진")
    @PostMapping
    public ResponseEntity<ResponseMessage<TrainerResponseDto>> createTrainer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("gymId") Long gymId,
        @Valid @RequestBody TrainerRequestDto dto) {
        TrainerResponseDto response = trainerService.createTrainer(
            userDetails.getId(),
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

    @Operation(summary = "트레이너 전체 조회", description = "체육관에 속한 트레이너를 조회하는 기능입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "트레이너 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "체육관을 찾을 수 없음")
    })
    @Parameter(name = "gymId", description = "체육관 ID")
    @Parameter(name = "page", description = "페이지 번호 (0부터 시작)")
    @Parameter(name = "size", description = "페이지 크기")
    @GetMapping
    public ResponseEntity<ResponseMessage<Page<TrainerResponseDto>>> getAllTrainer(
        @PathVariable("gymId") Long gymId,
        @PageableDefault Pageable pageable
    ) {
        Page<TrainerResponseDto> response = trainerService.getAllTrainer(gymId, pageable);
        return ResponseEntity.status(SuccessCode.GET_TRAINER_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_TRAINER_SUCCESS, response));
    }

    @Operation(summary = "트레이너 단일 조회", description = "체육관에 속한 트레이너를 단일 조회하는 기능입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "트레이너 상세 조회 성공"),
        @ApiResponse(responseCode = "404", description = "체육관 또는 트레이너를 찾을 수 없음")
    })
    @Parameter(name = "gymId", description = "체육관 ID")
    @Parameter(name = "trainerId", description = "트레이너 ID")
    @GetMapping("/{trainerId}")
    public ResponseEntity<ResponseMessage<TrainerDetailResponseDto>> getTrainerById(
        @PathVariable("gymId") Long gymId,
        @PathVariable("trainerId") Long trainerId) {
        TrainerDetailResponseDto response = trainerService.getTrainerById(gymId, trainerId);
        return ResponseEntity.status(SuccessCode.GET_TRAINER_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.GET_TRAINER_SUCCESS, response));
    }

    @Operation(summary = "트레이너 정보 수정", description = "체육관에 속한 트레이너의 정보를 수정하는 기능입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "트레이너 정보 수정 성공"),
        @ApiResponse(responseCode = "404", description = "체육관 또는 트레이너를 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "수정 권한이 없음")
    })
    @Parameter(name = "gymId", description = "체육관 ID")
    @Parameter(name = "trainerId", description = "트레이너 ID")
    @PatchMapping("/{trainerId}")
    public ResponseEntity<ResponseMessage<TrainerResponseDto>> updateTrainer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("gymId") Long gymId,
        @PathVariable("trainerId") Long trainerId,
        @Valid @RequestBody TrainerUpdateRequestDto dto) {
        TrainerResponseDto response = trainerService.updateTrainer(
            userDetails.getId(),
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


    @Operation(summary = "트레이너 삭제", description = "체육관에 속한 트레이너를 삭제하는 기능입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "트레이너 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "체육관 또는 트레이너를 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "삭제 권한이 없음")
    })
    @Parameter(name = "gymId", description = "체육관 ID")
    @Parameter(name = "trainerId", description = "트레이너 ID")
    @DeleteMapping("/{trainerId}")
    public ResponseEntity<ResponseMessage<Void>> deleteTrainer(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("gymId") Long gymId,
        @PathVariable("trainerId") Long trainerId) {
        trainerService.deleteTrainer(userDetails.getId(), gymId, trainerId);
        return ResponseEntity.status(SuccessCode.DELETE_TRAINER_SUCCESS.getHttpStatus())
            .body(ResponseMessage.success(SuccessCode.DELETE_TRAINER_SUCCESS));
    }
}
