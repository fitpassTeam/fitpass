package org.example.fitpass.domain.gym.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.Image;
import org.example.fitpass.domain.gym.dto.request.GymPhotoUpdateRequestDto;
import org.example.fitpass.domain.gym.dto.request.GymRequestDto;
import org.example.fitpass.domain.gym.dto.request.GymUpdateRequestDto;
import org.example.fitpass.domain.gym.dto.response.GymDetailResponDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.user.Gender;
import org.example.fitpass.domain.user.UserRole;
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public Page<GymResponseDto> getAllGyms(Pageable pageable) {
        Page<Gym> gyms = gymRepository.findAllAndIsDeletedFalse(pageable);
        return gyms.map(GymResponseDto::from);
    }

    @Transactional
    public void updatePhoto(List<String> imageUrls, Long gymId) {
        Long userId = 1L; // 임시로 사용, 나중에 인증객체 받아와서 사용할 예정
        User user = userRepository.findByIdOrElseThrow(userId); // 임시로 사용, 나중에 인증객체 받아와서 사용할 예정
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        gym.isOwner(user.getId());
        gym.updatePhoto(imageUrls, gym);
        gymRepository.save(gym);
    }

    public GymResponseDto updateGym(String name, String number, String content, String address, LocalTime openTime, LocalTime closeTime, Long gymId){
        Long userId = 1L; // 임시로 사용, 나중에 인증객체 받아와서 사용할 예정
        User user = userRepository.findByIdOrElseThrow(userId); // 임시로 사용, 나중에 인증객체 받아와서 사용할 예정
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        gym.isOwner(user.getId());
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
    public void delete(Long gymId) {
        Long userId = 1L; // 임시로 사용, 나중에 인증객체 받아와서 사용할 예정
        User user = userRepository.findByIdOrElseThrow(userId); // 임시로 사용, 나중에 인증객체 받아와서 사용할 예정
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        gym.isOwner(user.getId());
        gym.delete();
        gymRepository.save(gym);
    }
}
