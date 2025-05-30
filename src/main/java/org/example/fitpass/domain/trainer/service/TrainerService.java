package org.example.fitpass.domain.trainer.service;

import static org.example.fitpass.common.error.ExceptionCode.CANT_FIND_DATA;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.Image;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.trainer.dto.response.TrainerDetailResponseDto;
import org.example.fitpass.domain.trainer.dto.reqeust.TrainerRequestDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerRepository trainerRepository;

    public TrainerResponseDto createTrainer(String name, int price, String content, TrainerStatus trainerStatus, List<Image> trainerImage) {
        Trainer trainer = Trainer.of(trainerImage, name, price,content, trainerStatus);
        trainerRepository.save(trainer);
        return TrainerResponseDto.of(
            trainer.getName(),
            trainer.getPrice(),
            trainer.getContent(),
            trainer.getTrainerStatus()
        );
    }

    @Transactional(readOnly = true)
    public Page<TrainerResponseDto> getAllTrainers(Pageable pageable) {
        Page<Trainer> trainers = trainerRepository.findAll(pageable);
        return trainers.map(TrainerResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public TrainerDetailResponseDto findById(Long id) {
        Trainer trainer = trainerRepository.findById(id)
            .orElseThrow(() -> new BaseException(CANT_FIND_DATA));
        return TrainerDetailResponseDto.from(
            trainer.getName(),
            trainer.getPrice(),
            trainer.getContent(),
            trainer.getTrainerStatus(),
            trainer.getImages(),
            trainer.getCreatedAt()
        );
    }

    @Transactional
    public void updatePhoto(List<String> imageUrls, Long id) {
        Trainer trainer = trainerRepository.findByIdOrElseThrow(id);
        trainer.updatePhoto(imageUrls, trainer);
        trainerRepository.save(trainer);
    }

    @Transactional
    public TrainerResponseDto updateTrainer(Long id, TrainerRequestDto dto) {
        Trainer trainer = trainerRepository.findById(id)
            .orElseThrow(() -> new BaseException(CANT_FIND_DATA));
        trainer.update(dto.getTrainerImage(), dto.getName(), dto.getPrice(), dto.getContent(),
            dto.getTrainerStatus());
        return TrainerResponseDto.fromEntity(trainer);
    }

    @Transactional
    public void deleteItem(Long id) {
        Trainer trainer = trainerRepository.findById(id)
            .orElseThrow(() -> new BaseException(CANT_FIND_DATA));
        trainerRepository.delete(trainer);
    }

}
