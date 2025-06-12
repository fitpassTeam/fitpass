package org.example.fitpass.domain.trainer.service;

import static org.example.fitpass.common.error.ExceptionCode.INVALID_GYM_TRAINER_RELATION;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.entity.Image;
import org.example.fitpass.common.error.BaseException;
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
    public Page<TrainerResponseDto> getAllTrainersByGym(Long gymId, Pageable pageable) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Page<Trainer> trainers = trainerRepository.findAllByGym(gym, pageable);
        return trainers.map(TrainerResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public TrainerDetailResponseDto getTrainerByIdAndGym(Long gymId, Long id) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Trainer trainer = trainerRepository.findByIdOrElseThrow(id);

        validateTrainerBelongsToGym(trainer, gym);
        
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
    public void updatePhoto(Long gymId, List<String> imageUrls, Long id) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Trainer trainer = trainerRepository.findByIdOrElseThrow(id);

        validateTrainerBelongsToGym(trainer, gym);

        trainer.updatePhoto(imageUrls, trainer);
        trainerRepository.save(trainer);
    }

    @Transactional
    public TrainerResponseDto updateTrainer(Long gymId, Long id, TrainerUpdateRequestDto dto) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Trainer trainer = trainerRepository.findByIdOrElseThrow(id);

        validateTrainerBelongsToGym(trainer, gym);

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
    public void deleteItem(Long gymId, Long id) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Trainer trainer = trainerRepository.findByIdOrElseThrow(id);

        validateTrainerBelongsToGym(trainer, gym);
        trainerRepository.delete(trainer);
    }

    // 트레이너가 해당 체육관에 속해 있는지 검증
    private void validateTrainerBelongsToGym(Trainer trainer, Gym gym) {
        if (!trainer.getGym().getId().equals(gym.getId())) {
            throw new BaseException(INVALID_GYM_TRAINER_RELATION);
        }
    }

}
