package org.example.fitpass.domain.gym.service;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.common.s3.service.S3Service;
import org.example.fitpass.domain.gym.dto.response.GymDetailResponDto;
import org.example.fitpass.domain.gym.dto.response.GymResDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.likes.LikeType;
import org.example.fitpass.domain.likes.repository.LikeRepository;
import org.example.fitpass.domain.likes.service.LikeService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GymRepository gymRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final LikeRepository likeRepository;

    @Transactional
    public GymResDto post(String address, String name, String content, String number, List<String> gymImage, LocalTime openTime, LocalTime closeTime, Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Gym gym = Gym.of(gymImage,name,number,content,address,openTime,closeTime,user);
        gymRepository.save(gym);
        return GymResDto.of(
            gym.getName(),
            gym.getNumber(),
            gym.getContent(),
            gym.getAddress(),
            gym.getOpenTime(),
            gym.getCloseTime(),
            gym.getId()
        );
    }

    @Transactional(readOnly = true)
    public GymDetailResponDto getGym(Long gymId) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        return GymDetailResponDto.from(
            gym.getName(),
            gym.getNumber(),
            gym.getContent(),
            gym.getAddress(),
            gym.getOpenTime(),
            gym.getCloseTime(),
            gym.getImages().stream().map(Image::getUrl).toList(),
            gym.getTrainers().stream().map(trainer -> trainer.getName()).toList(),
            gym.getGymStatus()
        );
    }

    @Transactional(readOnly = true)
    public Page<GymResponseDto> getAllGyms(Pageable pageable, Long userId) {
        Page<Gym> gyms = gymRepository.findAll(pageable);
        Set<Long> likedGymIds = (userId != null) // null 값이 들어와도 에러가 발생하지 않고 사용하기 위하여 Set 사용
            ? likeRepository.findTargetIdsByUserIdAndLikeType(userId, LikeType.GYM)
            : Collections.emptySet();
        return gyms.map(gym -> {
            boolean isLiked = likedGymIds.contains(gym.getId());
            return GymResponseDto.from(gym, isLiked);
        });
    }

    @Transactional
    public List<String> updatePhoto(List<MultipartFile> files, Long gymId, Long userId) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        gym.isOwner(userId);
        for (Image image : gym.getImages()) {
            s3Service.deleteFileFromS3(image.getUrl());
        }
        List<String> imageUrls = s3Service.uploadFiles(files);
        gym.updatePhoto(imageUrls, gym);
        gymRepository.save(gym);
        return imageUrls;
    }

    @Transactional
    public GymResDto updateGym(String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, Long gymId, Long userId){
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        gym.isOwner(userId);
        gym.update(name,number,content,address,openTime,closeTime);
        gymRepository.save(gym);
        return GymResDto.of(
            gym.getName(),
            gym.getNumber(),
            gym.getContent(),
            gym.getAddress(),
            gym.getOpenTime(),
            gym.getCloseTime(),
            gym.getId()
        );
    }

    @Transactional
    public void delete(Long gymId, Long userId) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        gym.isOwner(userId);
        gymRepository.delete(gym);
    }
}
