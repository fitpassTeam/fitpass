package org.example.fitpass.domain.trainer.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.ResponseMessage;
import org.example.fitpass.domain.trainer.dto.TrainerReqeustDto;
import org.example.fitpass.domain.trainer.dto.TrainerResponseDto;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.service.TrainerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
        @Valid @RequestBody TrainerReqeustDto reqeustDto) {
        TrainerResponseDto responseDto = trainerService.createTrainer(reqeustDto);

        ResponseMessage<TrainerResponseDto> responseMessage =
            ResponseMessage.<TrainerResponseDto>builder()
            .statusCode(HttpStatus.CREATED.value())
            .message("트레이너 생성이 되었습니다.")
            .data(responseDto)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseMessage);
    }

    //전체 조회
    @GetMapping
    public ResponseEntity<ResponseMessage<List<TrainerResponseDto>>> getAllTrainer() {
        List<TrainerResponseDto> responseDto = trainerService.getAllTrainers();

        ResponseMessage<List<TrainerResponseDto>> responseMessage =
            ResponseMessage.<List<TrainerResponseDto>>builder()
            .statusCode(HttpStatus.OK.value())
            .message("트레이너 생성이 되었습니다.")
            .data(responseDto)
            .build();

        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    //단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage<TrainerResponseDto>> getTrainerById(
        @PathVariable("id") long id) {
        TrainerResponseDto responseDto = trainerService.findById(id);

        ResponseMessage<TrainerResponseDto> responseMessage =
            ResponseMessage.<TrainerResponseDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("트레이너 생성이 되었습니다.")
                .data(responseDto)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    //수정
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseMessage<TrainerResponseDto>> updateTrainer(
        @PathVariable("id") Long id,
        @Valid @RequestBody TrainerReqeustDto dto) {
        TrainerResponseDto responseDto = trainerService.updateItem(id, dto);

        ResponseMessage<TrainerResponseDto> responseMessage =
            ResponseMessage.<TrainerResponseDto>builder()
                .statusCode(HttpStatus.OK.value())
                .message("트레이너 정보가 수정되었습니다.")
                .data(responseDto)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }

    //삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage<Void>> deleteTrainer(
        @PathVariable("id") Long id) {
        trainerService.deleteItem(id);

        ResponseMessage<Void> responseMessage =
            ResponseMessage.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("트레이너 정보가 삭제되었습니다.")
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(responseMessage);
    }
}
