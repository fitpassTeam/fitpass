package org.example.fitpass.domain.gym.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.Image;
import org.example.fitpass.domain.gym.dto.response.GymDetailResponDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GymRepository gymRepository;

    @Transactional
    public GymResponseDto post(String address, String name, String content, String number, List<Image> gymImage, LocalTime openTime, LocalTime closeTime) {
        Long userId = 1L;
        User user = User.of(userId,"image.js", Gender.MAN, UserRole.USER);
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

    public GymDetailResponDto getGym(Long gymId) {
        Gym gym = gymRepository.findByIdAndIsDeletedFalse(gymId)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 가게입니다."));
        return GymDetailResponDto.from(
            gym.getName(),
            gym.getNumber(),
            gym.getContent(),
            gym.getAddress(),
            gym.getOpenTime(),
            gym.getCloseTime(),
            gym.getImages(),
            gym.getTrainers().stream().map(trainer -> trainer.getName()).toList(),
            gym.getGymStatus()
        );
    }

    public Page<GymResponseDto> getAllGyms(Pageable pageable) {
        Page<Gym> gyms = gymRepository.findAllAndIsDeletedFalse(pageable);
        return gyms.map(GymResponseDto::from);
    }
}
