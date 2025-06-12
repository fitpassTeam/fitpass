package org.example.fitpass.domain.trainer.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.trainer.dto.reqeust.TrainerUpdateRequestDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerDetailResponseDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final GymRepository gymRepository;

    public TrainerResponseDto createTrainer(Long gymId, String name, int price, String content,
        List<Image> trainerImage) {

        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        Trainer trainer = Trainer.of(trainerImage, name, price, content);

        trainer.assignToGym(gym);

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
        Trainer trainer = trainerRepository.getByIdOrThrow(id);
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
        Trainer trainer = trainerRepository.getByIdOrThrow(id);
        trainer.updatePhoto(imageUrls, trainer);
        trainerRepository.save(trainer);
    }

    @Transactional
    public TrainerResponseDto updateTrainer(Long id, TrainerUpdateRequestDto dto) {
        Trainer trainer = trainerRepository.getByIdOrThrow(id);
        trainer.update(dto.getTrainerImage(), dto.getName(), dto.getPrice(), dto.getContent(),
            dto.getTrainerStatus());
        return TrainerResponseDto.of(
            trainer.getName(),
            trainer.getPrice(),
            trainer.getContent(),
            trainer.getTrainerStatus()
        );
    }

    @Transactional
    public void deleteItem(Long id) {
        Trainer trainer = trainerRepository.getByIdOrThrow(id);
        trainerRepository.delete(trainer);
    }

}
