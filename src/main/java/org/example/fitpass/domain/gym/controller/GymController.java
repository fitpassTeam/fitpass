package org.example.fitpass.domain.gym.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.dto.request.GymRequestDto;
import org.example.fitpass.domain.gym.dto.response.GymDetailResponDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.gym.service.GymService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/gym")
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;
    private final GymRepository gymRepository;

    @PostMapping
    public ResponseEntity<GymResponseDto> postGym(
        @RequestBody GymRequestDto request){
        GymResponseDto response = gymService.post(
            request.getAddress(),
            request.getName(),
            request.getContent(),
            request.getNumber(),
            request.getGymImage(),
            request.getOpenTime(),
            request.getCloseTime()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gymId}")
    public ResponseEntity<GymDetailResponDto> getGym(@PathVariable Long gymId){
        GymDetailResponDto response = gymService.getGym(gymId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<GymResponseDto>> getAllGyms(
        @PageableDefault(page = 0, size = 10) Pageable pageable
    ){
        Page<GymResponseDto> response = gymService.getAllGyms(pageable);
        return ResponseEntity.ok(response);
    }
}
