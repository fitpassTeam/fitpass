package org.example.fitpass.domain.trainer.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.trainer.dto.TrainerReqeustDto;
import org.example.fitpass.domain.trainer.dto.TrainerResponseDto;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerRepository trainerRepository;

    public TrainerResponseDto createTrainer(TrainerReqeustDto dto){
        Trainer trainer = Trainer.builder()
            .name(dto.getName())
            .price(dto.getPrice())
            .content(dto.getContent())
            .trainerStatus(dto.getTrainerStatus())
            .build();

        Trainer saved = trainerRepository.save(trainer);

        return TrainerResponseDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<TrainerResponseDto> getAllTrainers() {
        List<Trainer> trainers = trainerRepository.findAll();
        return trainers.stream()
            .map(TrainerResponseDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public TrainerResponseDto findById(long id) {
        Trainer trainer = trainerRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        return TrainerResponseDto.fromEntity(trainer);
    }

    @Transactional
    public TrainerResponseDto updateItem(long id, TrainerReqeustDto dto) {
        Trainer trainer = trainerRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        trainer.update(dto.getTrainerImage(), dto.getName(), dto.getPrice(), dto.getContent(),dto.getTrainerStatus());

        return TrainerResponseDto.fromEntity(trainer);
    }

    @Transactional
    public void deleteItem(long id) {
        Trainer trainer = trainerRepository.findById(id)
            .orElseThrow(() ->  new ResponseStatusException(HttpStatus.BAD_REQUEST));

        trainerRepository.delete(trainer);
    }

}
