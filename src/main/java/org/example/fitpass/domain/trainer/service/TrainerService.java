package org.example.fitpass.domain.trainer.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.s3.service.S3Service;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.trainer.dto.response.TrainerDetailResponseDto;
import org.example.fitpass.domain.trainer.dto.response.TrainerResponseDto;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.enums.TrainerStatus;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public TrainerResponseDto createTrainer(Long userId, Long gymId, String name, int price, String content,
        String experience, List<String> trainerImage) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 오너인지 확인 여부
        if (user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 이 체육관의 소유자인지 확인
        if (!Objects.equals(gym.getOwner().getId(), userId)) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
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
    public Page<TrainerResponseDto> getAllTrainer(Long userId, Long gymId, Pageable pageable) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Page<Trainer> trainers = trainerRepository.findAllByGym(gym, pageable);
        return trainers.map(TrainerResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public TrainerDetailResponseDto getTrainerById(Long userId, Long gymId, Long trainerId) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 트레이너 조회
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);

        trainer.validateTrainerBelongsToGym(trainer, gym);

        return TrainerDetailResponseDto.from(
            trainer.getName(),
            trainer.getPrice(),
            trainer.getContent(),
            trainer.getExperience(),
            trainer.getTrainerStatus(),
            trainer.getImages().stream()
                .map(Image::getUrl)
                .toList(),
            trainer.getCreatedAt()
        );
    }

    @Transactional
    public List<String> updatePhoto(Long userId, List<MultipartFile> files, Long gymId, Long trainerId) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 오너인지 확인 여부
        if (user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 이 체육관의 소유자인지 확인
        if (!Objects.equals(gym.getOwner().getId(), userId)) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);
        // 트레이너가 해당 체육관 소속인지 확인
        if (!trainer.getGym().getId().equals(gymId)) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
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
    public TrainerResponseDto updateTrainer(Long userId, Long gymId, Long trainerId, String name, int price,
        String content, String experience, TrainerStatus trainerStatus, List<String> imgs) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 오너인지 확인 여부
        if (user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 이 체육관의 소유자인지 확인
        if (!Objects.equals(gym.getOwner().getId(), userId)) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);
        // 트레이너가 해당 체육관 소속인지 확인
        if (!trainer.getGym().getId().equals(gymId)) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
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
    public void deleteTrainer(Long userId, Long gymId, Long trainerId) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 오너인지 확인 여부
        if (user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 이 체육관의 소유자인지 확인
        if (!Objects.equals(gym.getOwner().getId(), userId)) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);
        // 트레이너가 해당 체육관 소속인지 확인
        if (!trainer.getGym().getId().equals(gymId)) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        trainer.validateTrainerBelongsToGym(trainer, gym);
        trainerRepository.delete(trainer);
    }

}
