package org.example.fitpass.domain.gym.service;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.s3.service.S3Service;
import org.example.fitpass.domain.gym.dto.response.GymDetailResponDto;
import org.example.fitpass.domain.gym.dto.response.GymResDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.enums.GymStatus;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.likes.LikeType;
import org.example.fitpass.domain.likes.repository.LikeRepository;
import org.example.fitpass.domain.likes.service.LikeService;
import org.example.fitpass.domain.review.dto.response.GymRatingResponseDto;
import org.example.fitpass.domain.review.repository.ReviewRepository;
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
public class GymService {

    private final GymRepository gymRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final S3Service s3Service;
    private final LikeRepository likeRepository;

    @Transactional
    public GymResDto postGym(String address, String name, String content, String number, List<String> gymImage, LocalTime openTime, LocalTime closeTime, Long userId) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 오너인지 확인 여부
        if (user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }

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
        GymRatingResponseDto rating = reviewRepository.findGymRatingByGymIdOrElseThrow(gymId);
        return GymDetailResponDto.from(
            gym.getName(),
            gym.getNumber(),
            gym.getContent(),
            gym.getAddress(),
            gym.getOpenTime(),
            gym.getCloseTime(),
            gym.getImages().stream().map(Image::getUrl).toList(),
            gym.getTrainers().stream().map(trainer -> trainer.getName()).toList(),
            gym.getGymStatus(),
            rating.averageGymRating(),
            rating.totalReviewCount().intValue()
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
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 오너인지 확인 여부
        if (user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }

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
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 오너인지 확인 여부
        if (user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
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
    public void deleteGym(Long gymId, Long userId) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 오너인지 확인 여부
        if (user.getUserRole() != UserRole.OWNER) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }
        gymRepository.delete(gym);
    }

    @Transactional(readOnly = true)
    public GymRatingResponseDto getGymRating(Long gymId, Long userId) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        User user = userRepository.findByIdOrElseThrow(userId);
        return reviewRepository.findGymRatingByGymIdOrElseThrow(gymId);
    }

    // Admin용 승인 대기 목록 조회
    @Transactional(readOnly = true)
    public List<GymResponseDto> getPendingGymRequests() {
        List<Gym> pendingGyms = gymRepository.findByGymStatus(GymStatus.PENDING);
        return pendingGyms.stream()
            .map(gym -> GymResponseDto.from(gym, false)) // 좋아요는 false로 설정
            .toList();
    }

    // Admin용 체육관 승인
    @Transactional
    public GymResponseDto approveGym(Long gymId) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        if (gym.getGymStatus() != GymStatus.PENDING) {
            throw new BaseException(ExceptionCode.INVALID_GYM_APPROVAL_REQUEST);
        }

        gym.approveGym();
        gymRepository.save(gym);
        return GymResponseDto.from(gym, false);
    }

    // Admin용 체육관 거절
    @Transactional
    public GymResponseDto rejectGym(Long gymId) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);

        if (gym.getGymStatus() != GymStatus.PENDING) {
            throw new BaseException(ExceptionCode.INVALID_GYM_REJECTION_REQUEST);
        }

        gym.rejectGym();
        gymRepository.save(gym);
        return GymResponseDto.from(gym, false);
    }
}
