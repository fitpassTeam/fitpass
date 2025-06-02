package org.example.fitpass.domain.gym.service;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.Image;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.dto.response.GymDetailResponDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GymRepository gymRepository;
    private final UserRepository userRepository;

    @Transactional
    public GymResponseDto post(String address, String name, String content, String number, List<Image> gymImage, LocalTime openTime, LocalTime closeTime, Long userId) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Gym gym = Gym.of(gymImage,name,number,content,address,openTime,closeTime,user);
        gymRepository.save(gym);
        return GymResponseDto.of(
            gym.getName(),
            gym.getNumber(),
            gym.getContent(),
            gym.getAddress(),
            gym.getOpenTime(),
            gym.getCloseTime()
        );
    }

    @Transactional(readOnly = true)
    public GymDetailResponDto getGym(Long gymId) {
        Gym gym = gymRepository.findByIdAndIsDeletedFalse(gymId)
            .orElseThrow(() -> new BaseException(ExceptionCode.GYM_NOT_FOUND));
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
    public Page<GymResponseDto> getAllGyms(Pageable pageable) {
        Page<Gym> gyms = gymRepository.findAllByIsDeletedFalse(pageable);
        return gyms.map(GymResponseDto::from);
    }

    @Transactional
    public void updatePhoto(List<String> imageUrls, Long gymId, Long userId) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        gym.isOwner(userId);
        gym.updatePhoto(imageUrls, gym);
        gymRepository.save(gym);
    }

    @Transactional
    public GymResponseDto updateGym(String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, Long gymId, Long userId){
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        gym.isOwner(userId);
        gym.update(name,number,content,address,openTime,closeTime);
        gymRepository.save(gym);
        return GymResponseDto.of(
            gym.getName(),
            gym.getNumber(),
            gym.getContent(),
            gym.getAddress(),
            gym.getOpenTime(),
            gym.getCloseTime()
        );
    }

    @Transactional
    public void delete(Long gymId, Long userId) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        gym.isOwner(userId);
        gym.delete();
        gymRepository.save(gym);
    }
}
