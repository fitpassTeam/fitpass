package org.example.fitpass.domain.gym.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.example.fitpass.common.Image.entity.Image;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.common.s3.service.S3Service;
import org.example.fitpass.domain.gym.dto.response.GymDetailResponDto;
import org.example.fitpass.domain.gym.dto.response.GymResDto;
import org.example.fitpass.domain.gym.dto.response.GymResponseDto;
import org.example.fitpass.domain.gym.dto.response.GymStatusResponseDto;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.enums.GymPostStatus;
import org.example.fitpass.domain.gym.enums.GymStatus;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.likes.LikeType;
import org.example.fitpass.domain.likes.repository.LikeRepository;
import org.example.fitpass.domain.review.dto.response.GymRatingResponseDto;
import org.example.fitpass.domain.review.repository.ReviewRepository;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class GymServiceTest {

    @InjectMocks
    private GymService gymService;

    @Mock
    private GymRepository gymRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private MultipartFile multipartFile;

    private User ownerUser;
    private User normalUser;
    private Gym gym;
    private GymRatingResponseDto ratingResponse;

    @BeforeEach
    void setUp() {
        // Owner 사용자 생성
        ownerUser = new User(
            "owner@test.com",
            "profile.jpg",
            "password123",
            "김오너",
            "010-1234-5678",
            30,
            "서울시 강남구",
            Gender.MAN,
            UserRole.OWNER
        );

        // 일반 사용자 생성
        normalUser = new User(
            "user@test.com",
            "profile.jpg",
            "password123",
            "김유저",
            "010-9876-5432",
            25,
            "서울시 서초구",
            Gender.WOMAN,
            UserRole.USER
        );

        // 체육관 생성
        gym = Gym.of(
            List.of("image1.jpg", "image2.jpg"),
            "헬스클럽ABC",
            "02-123-4567",
            "최고의 헬스장입니다.",
            "서울",
            "강남구",
            "테헤란로 123",
            LocalTime.of(6, 0),
            LocalTime.of(23, 0),
            "24시간 운영하는 프리미엄 헬스장",
            ownerUser
        );

        // 평점 응답 생성 (4개 파라미터: gymId, gymName, averageGymRating, totalReviewCount)
        ratingResponse = new GymRatingResponseDto(1L, "헬스클럽ABC", 4.5, 10L);
    }

    @Test
    @DisplayName("체육관 등록 성공 테스트")
    void postGym_Success() {
        // given
        Long ownerId = 1L;
        given(userRepository.findByIdOrElseThrow(ownerId)).willReturn(ownerUser);
        given(gymRepository.save(any(Gym.class))).willReturn(gym);

        // when
        GymStatusResponseDto result = gymService.postGym(
            "서울", "강남구", "테헤란로 123", 
            "헬스클럽ABC", "최고의 헬스장입니다.", "02-123-4567",
            List.of("image1.jpg", "image2.jpg"),
            LocalTime.of(6, 0), LocalTime.of(23, 0),
            "24시간 운영하는 프리미엄 헬스장", ownerId
        );

        // then
        assertThat(result.name()).isEqualTo("헬스클럽ABC");
        assertThat(result.number()).isEqualTo("02-123-4567");
        assertThat(result.content()).isEqualTo("최고의 헬스장입니다.");
        assertThat(result.address()).isEqualTo("서울 강남구 테헤란로 123");
        assertThat(result.gymPostStatus()).isEqualTo(GymPostStatus.PENDING);
        verify(gymRepository).save(any(Gym.class));
    }

    @Test
    @DisplayName("체육관 등록 실패 - OWNER 권한이 아닌 사용자")
    void postGym_Fail_NotOwner() {
        // given
        Long userId = 1L;
        given(userRepository.findByIdOrElseThrow(userId)).willReturn(normalUser);

        // when & then
        assertThatThrownBy(() -> gymService.postGym(
            "서울", "강남구", "테헤란로 123",
            "헬스클럽ABC", "최고의 헬스장입니다.", "02-123-4567",
            List.of("image1.jpg", "image2.jpg"),
            LocalTime.of(6, 0), LocalTime.of(23, 0),
            "24시간 운영하는 프리미엄 헬스장", userId
        )).isInstanceOf(BaseException.class)
          .hasMessageContaining(ExceptionCode.NOT_GYM_OWNER.getMessage());
    }

    @Test
    @DisplayName("체육관 상세 조회 성공 테스트")
    void getGym_Success() {
        // given
        Long gymId = 1L;
        gym.approveGym(); // 승인 상태로 변경
        
        given(gymRepository.findByIdAndGymPostStatusOrElseThrow(gymId, GymPostStatus.APPROVED))
            .willReturn(gym);
        given(reviewRepository.findGymRatingByGymIdOrElseThrow(gymId))
            .willReturn(ratingResponse);

        // when
        GymDetailResponDto result = gymService.getGym(gymId);

        // then
        assertThat(result.name()).isEqualTo("헬스클럽ABC");
        assertThat(result.number()).isEqualTo("02-123-4567");
        assertThat(result.content()).isEqualTo("최고의 헬스장입니다.");
        assertThat(result.fullAddress()).isEqualTo("서울 강남구 테헤란로 123");
        assertThat(result.averageGymRating()).isEqualTo(4.5);
        assertThat(result.totalReviewCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("체육관 목록 조회 성공 테스트 - 로그인한 사용자")
    void getAllGyms_Success_WithUser() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Gym> gymPage = new PageImpl<>(List.of(gym));
        Set<Long> likedGymIds = Set.of(1L); // gym.getId() 대신 직접 1L 사용

        // Mock gym ID 설정 (Reflection 사용)
        try {
            java.lang.reflect.Field idField = Gym.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(gym, 1L);
        } catch (Exception e) {
            // 테스트에서는 무시
        }

        given(gymRepository.findAllByGymPostStatus(GymPostStatus.APPROVED, pageable))
            .willReturn(gymPage);
        given(likeRepository.findTargetIdsByUserIdAndLikeType(userId, LikeType.GYM))
            .willReturn(likedGymIds);

        // when
        Page<GymResponseDto> result = gymService.getAllGyms(pageable, userId);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("헬스클럽ABC");
        assertThat(result.getContent().get(0).isLiked()).isTrue();
    }

    @Test
    @DisplayName("체육관 목록 조회 성공 테스트 - 비로그인 사용자")
    void getAllGyms_Success_WithoutUser() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Gym> gymPage = new PageImpl<>(List.of(gym));

        given(gymRepository.findAllByGymPostStatus(GymPostStatus.APPROVED, pageable))
            .willReturn(gymPage);

        // when
        Page<GymResponseDto> result = gymService.getAllGyms(pageable, null);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("헬스클럽ABC");
        assertThat(result.getContent().get(0).isLiked()).isFalse();
    }

    @Test
    @DisplayName("체육관 사진 업데이트 성공 테스트")
    void updatePhoto_Success() {
        // given
        Long gymId = 1L;
        Long ownerId = 1L;
        List<MultipartFile> files = List.of(multipartFile);
        List<String> newImageUrls = List.of("new_image1.jpg", "new_image2.jpg");

        given(userRepository.findByIdOrElseThrow(ownerId)).willReturn(ownerUser);
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);
        given(s3Service.uploadFiles(files)).willReturn(newImageUrls);
        given(gymRepository.save(any(Gym.class))).willReturn(gym);

        // when
        List<String> result = gymService.updatePhoto(files, gymId, ownerId);

        // then
        assertThat(result).containsExactlyElementsOf(newImageUrls);
        verify(s3Service).uploadFiles(files);
        verify(gymRepository).save(gym);
    }

    @Test
    @DisplayName("체육관 정보 업데이트 성공 테스트")
    void updateGym_Success() {
        // given
        Long gymId = 1L;
        Long ownerId = 1L;
        List<String> newImages = List.of("new_image.jpg");

        given(userRepository.findByIdOrElseThrow(ownerId)).willReturn(ownerUser);
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);
        given(gymRepository.save(any(Gym.class))).willReturn(gym);

        // when
        GymResDto result = gymService.updateGym(
            "새로운 헬스장", "02-987-6543", "업데이트된 내용",
            "경기", "성남시", "분당구 판교로 123",
            LocalTime.of(5, 0), LocalTime.of(23, 59),
            "새로운 요약", newImages, gymId, ownerId
        );

        // then
        assertThat(result).isNotNull();
        verify(gymRepository).save(gym);
    }

    @Test
    @DisplayName("체육관 삭제 성공 테스트")
    void deleteGym_Success() {
        // given
        Long gymId = 1L;
        Long ownerId = 1L;

        given(userRepository.findByIdOrElseThrow(ownerId)).willReturn(ownerUser);
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);

        // when
        gymService.deleteGym(gymId, ownerId);

        // then
        verify(gymRepository).delete(gym);
    }

    @Test
    @DisplayName("체육관 삭제 실패 - OWNER 권한이 아닌 사용자")
    void deleteGym_Fail_NotOwner() {
        // given
        Long gymId = 1L;
        Long userId = 1L;

        given(userRepository.findByIdOrElseThrow(userId)).willReturn(normalUser);
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);

        // when & then
        assertThatThrownBy(() -> gymService.deleteGym(gymId, userId))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining(ExceptionCode.NOT_GYM_OWNER.getMessage());
    }

    @Test
    @DisplayName("체육관 평점 조회 성공 테스트")
    void getGymRating_Success() {
        // given
        Long gymId = 1L;
        Long userId = 1L;

        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);
        given(userRepository.findByIdOrElseThrow(userId)).willReturn(ownerUser);
        given(reviewRepository.findGymRatingByGymIdOrElseThrow(gymId)).willReturn(ratingResponse);

        // when
        GymRatingResponseDto result = gymService.getGymRating(gymId, userId);

        // then
        assertThat(result.averageGymRating()).isEqualTo(4.5);
        assertThat(result.totalReviewCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("승인 대기 체육관 목록 조회 성공 테스트")
    void getPendingGymRequests_Success() {
        // given
        List<Gym> pendingGyms = List.of(gym);
        given(gymRepository.findByGymPostStatus(GymPostStatus.PENDING)).willReturn(pendingGyms);

        // when
        List<GymResponseDto> result = gymService.getPendingGymRequests();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("헬스클럽ABC");
        assertThat(result.get(0).isLiked()).isFalse();
    }

    @Test
    @DisplayName("체육관 승인 성공 테스트")
    void approveGym_Success() {
        // given
        Long gymId = 1L;
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);
        given(gymRepository.save(any(Gym.class))).willReturn(gym);

        // when
        GymResponseDto result = gymService.approveGym(gymId);

        // then
        assertThat(result.name()).isEqualTo("헬스클럽ABC");
        verify(gymRepository).save(gym);
    }

    @Test
    @DisplayName("체육관 승인 실패 - PENDING 상태가 아님")
    void approveGym_Fail_InvalidStatus() {
        // given
        Long gymId = 1L;
        gym.approveGym(); // 이미 승인된 상태로 변경
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);

        // when & then
        assertThatThrownBy(() -> gymService.approveGym(gymId))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining(ExceptionCode.INVALID_GYM_APPROVAL_REQUEST.getMessage());
    }

    @Test
    @DisplayName("체육관 거절 성공 테스트")
    void rejectGym_Success() {
        // given
        Long gymId = 1L;
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);
        given(gymRepository.save(any(Gym.class))).willReturn(gym);

        // when
        GymResponseDto result = gymService.rejectGym(gymId);

        // then
        assertThat(result.name()).isEqualTo("헬스클럽ABC");
        verify(gymRepository).save(gym);
    }

    @Test
    @DisplayName("체육관 거절 실패 - PENDING 상태가 아님")
    void rejectGym_Fail_InvalidStatus() {
        // given
        Long gymId = 1L;
        gym.rejectGym(); // 이미 거절된 상태로 변경
        given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(gym);

        // when & then
        assertThatThrownBy(() -> gymService.rejectGym(gymId))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining(ExceptionCode.INVALID_GYM_REJECTION_REQUEST.getMessage());
    }

    @Test
    @DisplayName("내 체육관 목록 조회 성공 테스트")
    void getAllMyGyms_Success() {
        // given
        Long ownerId = 1L;
        List<Gym> myGyms = List.of(gym);

        given(userRepository.findByIdOrElseThrow(ownerId)).willReturn(ownerUser);
        given(gymRepository.findByUserId(ownerId)).willReturn(myGyms);

        // when
        List<GymResponseDto> result = gymService.getAllMyGyms(ownerId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("헬스클럽ABC");
        assertThat(result.get(0).isLiked()).isFalse();
    }

    @Test
    @DisplayName("내 체육관 목록 조회 실패 - OWNER 권한이 아닌 사용자")
    void getAllMyGyms_Fail_NotOwner() {
        // given
        Long userId = 1L;
        given(userRepository.findByIdOrElseThrow(userId)).willReturn(normalUser);

        // when & then
        assertThatThrownBy(() -> gymService.getAllMyGyms(userId))
            .isInstanceOf(BaseException.class)
            .hasMessageContaining(ExceptionCode.NOT_GYM_OWNER.getMessage());
    }
}
