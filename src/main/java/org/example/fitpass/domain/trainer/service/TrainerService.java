package org.example.fitpass.domain.trainer.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.common.s3.service.S3Service;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.trainer.dto.response.TrainerDetailResponseDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final GymRepository gymRepository;
    private final S3Service s3Service;

    @Transactional
    public TrainerResponseDto createTrainer(Long gymId, String name, int price, String content,
        List<String> trainerImage, String experience) {

        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Trainer trainer = Trainer.of(trainerImage, name, price, content, experience);
        trainer.assignToGym(gym);

        trainerRepository.save(trainer);
        return TrainerResponseDto.of(
            trainer.getId(),
            trainer.getName(),
            trainer.getPrice(),
            trainer.getContent(),
            trainer.getTrainerStatus(),
            trainer.getExperience(),
            trainer.getImages().stream()
                .map(Image::getUrl)
                .toList()
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

        trainer.validateTrainerBelongsToGym(trainer, gym);

        return TrainerDetailResponseDto.from(
            trainer.getName(),
            trainer.getPrice(),
            trainer.getContent(),
            trainer.getTrainerStatus(),
            trainer.getImages().stream()
                .map(Image::getUrl)
                .toList(),
            trainer.getCreatedAt()
        );
    }

    @Transactional
    public List<String> updatePhoto(List<MultipartFile> files, Long gymId, Long id) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Trainer trainer = trainerRepository.findByIdOrElseThrow(id);

        trainer.validateTrainerBelongsToGym(trainer, gym);

        for (Image image : trainer.getImages()) {
            s3Service.deleteFileFromS3(image.getUrl());
        }

        List<String> imageUrls = s3Service.uploadFiles(files);
        trainer.updatePhoto(imageUrls, trainer);
        trainerRepository.save(trainer);
        return imageUrls;
    }

    @Transactional
    public TrainerResponseDto updateTrainer(Long gymId, Long id, String name, int price,
        String content, TrainerStatus trainerStatus, String experience, List<String> imgs) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Trainer trainer = trainerRepository.findByIdOrElseThrow(id);
        trainer.validateTrainerBelongsToGym(trainer, gym);
        trainer.getImages().clear();
        trainer.update(name, price, content, trainerStatus, experience, imgs);
        trainerRepository.save(trainer);
        return TrainerResponseDto.of(
            trainer.getId(),
            trainer.getName(),
            trainer.getPrice(),
            trainer.getContent(),
            trainer.getTrainerStatus(),
            trainer.getExperience(),
            trainer.getImages().stream()
                .map(Image::getUrl)
                .toList()
        );
    }

    @Transactional
    public void deleteTrainer(Long gymId, Long id) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Trainer trainer = trainerRepository.findByIdOrElseThrow(id);
        trainer.validateTrainerBelongsToGym(trainer, gym);
        trainerRepository.delete(trainer);
    }

}
