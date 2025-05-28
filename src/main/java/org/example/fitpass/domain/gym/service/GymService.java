package org.example.fitpass.domain.gym.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GymRepository gymRepository;

    public GymResponseDto post(String address, String name, String content, String number, String gymImage, LocalTime openTime, LocalTime closeTime, Long userId) {
        User user = User.of(userId,"www.google.js", Gender.MAN, UserRole.USER);
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
}
