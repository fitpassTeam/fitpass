package org.example.fitpass.domain.gym.controller;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.dto.request.GymRequestDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.service.GymService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gym")
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

    @PostMapping
    ResponseEntity<GymResponseDto> postGym(@RequestBody GymRequestDto request, Long userId){
        GymResponseDto response = gymService.post(
            request.getAddress(),
            request.getName(),
            request.getContent(),
            request.getNumber(),
            request.getGymImage(),
            request.getOpenTime(),
            request.getCloseTime(),
            userId
        );
        return ResponseEntity.ok(response);
    }
}
