package org.example.fitpass.domain.trainer.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.trainer.dto.TrainerReqeustDto;
import org.example.fitpass.domain.trainer.dto.TrainerResponseDto;
import org.example.fitpass.domain.trainer.service.TrainerService;
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

    @PostMapping
    public ResponseEntity<TrainerResponseDto> createTrainer(
        @RequestBody TrainerReqeustDto reqeustDto) {
        TrainerResponseDto responseDto = trainerService.createTrainer(reqeustDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<TrainerResponseDto>> getAllTrainer() {
        List<TrainerResponseDto> response = trainerService.getAllTrainers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainerResponseDto> getTrainerById(
        @PathVariable("id") long id) {
        TrainerResponseDto responseDto = trainerService.findById(id);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TrainerResponseDto> updateTrainer(
        @PathVariable("id") Long id,
        @RequestBody TrainerReqeustDto dto) {
        TrainerResponseDto response = trainerService.updateItem(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainer(
        @PathVariable("id") Long id) {
        trainerService.deleteItem(id);
        return ResponseEntity.ok().build();
    }
}
