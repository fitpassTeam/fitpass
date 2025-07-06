package org.example.fitpass.trainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
import org.example.fitpass.domain.trainer.service.TrainerService;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.enums.Gender;
import org.example.fitpass.domain.user.enums.UserRole;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("TrainerService 단위 테스트")
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private GymRepository gymRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TrainerService trainerService;

    private User ownerUser;
    private User normalUser;
    private Gym testGym;
    private List<String> testImages;

    @BeforeEach
    void setUp() {
        // 실제 User 객체 생성 (생성자 사용)
        ownerUser = new User(
            "owner@test.com",
            null,
            "password123",
            "체육관사장",
            "010-1234-5678",
            30,
            "서울시 강남구",
            Gender.MAN,
            UserRole.OWNER,
            "LOCAL"
        );
        setId(ownerUser, 1L);

        normalUser = new User(
            "user@test.com",
            null,
            "password123",
            "일반사용자",
            "010-9876-5432",
            25,
            "서울시 서초구",
            Gender.WOMAN,
            UserRole.USER,
            "LOCAL"
        );
        setId(normalUser, 2L);

        // 실제 Gym 객체 생성 (정적 팩토리 메서드 사용)
        testGym = Gym.of(
            List.of("gym1.jpg", "gym2.jpg"),
            "테스트헬스장",
            "02-1234-5678",
            "최고의 헬스장입니다.",
            "서울",
            "강남구",
            "테헤란로 332",
            LocalTime.of(6, 0),
            LocalTime.of(23, 0),
            "깨끗하고 시설이 좋은 헬스장",
            ownerUser
        );
        setId(testGym, 1L);

        testImages = List.of("image1.jpg", "image2.jpg");
    }

    // 리플렉션으로 ID 설정
    private void setId(Object entity, Long id) {
        try {
            java.lang.reflect.Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            // ID 설정 실패 시 무시
        }
    }

    @Nested
    @DisplayName("트레이너 생성")
    class CreateTrainer {

        @Test
        @DisplayName("성공: 체육관 소유자가 트레이너 생성")
        void createTrainer_Success() {
            // given
            Long userId = 1L;
            Long gymId = 1L;
            String name = "김트레이너";
            int price = 50000;
            String content = "전문 트레이너입니다.";
            String experience = "5년 경력";
            List<String> trainerImages = testImages;

            given(userRepository.findByIdOrElseThrow(userId)).willReturn(ownerUser);
            given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(testGym);
            
            // ArgumentCaptor를 사용하여 실제 저장되는 Trainer 객체를 캡처
            given(trainerRepository.save(any(Trainer.class))).willAnswer(invocation -> {
                Trainer trainer = invocation.getArgument(0);
                // ID를 설정 (실제 DB에서는 자동으로 생성됨)
                setId(trainer, 1L);
                return trainer;
            });

            // when
            TrainerResponseDto result = trainerService.createTrainer(
                userId, gymId, name, price, content, experience, trainerImages);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("김트레이너");
            assertThat(result.price()).isEqualTo(50000);
            assertThat(result.content()).isEqualTo("전문 트레이너입니다.");
            assertThat(result.experience()).isEqualTo("5년 경력");
            assertThat(result.trainerStatus()).isEqualTo(TrainerStatus.ACTIVE);

            verify(userRepository).findByIdOrElseThrow(userId);
            verify(gymRepository).findByIdOrElseThrow(gymId);
            verify(trainerRepository).save(any(Trainer.class));
        }

        @Test
        @DisplayName("실패: 사용자가 체육관 소유자가 아님")
        void createTrainer_NotOwner() {
            // given
            Long userId = 2L;
            Long gymId = 1L;

            given(userRepository.findByIdOrElseThrow(userId)).willReturn(normalUser);

            // when & then
            assertThatThrownBy(() -> trainerService.createTrainer(
                userId, gymId, "김트레이너", 50000, "내용", "경력", testImages))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(ExceptionCode.NOT_GYM_OWNER);
                });

            verify(userRepository).findByIdOrElseThrow(userId);
            verify(gymRepository, never()).findByIdOrElseThrow(anyLong());
        }

        @Test
        @DisplayName("실패: 다른 사람의 체육관에 트레이너 생성 시도")
        void createTrainer_NotGymOwner() {
            // given
            Long userId = 1L;
            Long gymId = 1L;

            // 다른 소유자의 체육관 생성
            User anotherOwner = new User(
                "another@test.com",
                null,
                "password123",
                "다른사장",
                "010-5555-5555",
                35,
                "서울시 종로구",
                Gender.MAN,
                UserRole.OWNER,
                "LOCAL"
            );
            setId(anotherOwner, 3L);

            Gym anotherGym = Gym.of(
                List.of("gym1.jpg"),
                "다른헬스장",
                "02-0000-0000",
                "다른 체육관입니다.",
                "서울",
                "강남구",
                "테스트로 123",
                LocalTime.of(6, 0),
                LocalTime.of(23, 0),
                "다른 체육관",
                anotherOwner
            );
            setId(anotherGym, 1L);

            given(userRepository.findByIdOrElseThrow(userId)).willReturn(ownerUser);
            given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(anotherGym);

            // when & then
            assertThatThrownBy(() -> trainerService.createTrainer(
                userId, gymId, "김트레이너", 50000, "내용", "경력", testImages))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(ExceptionCode.NOT_GYM_OWNER);
                });
        }
    }

    @Nested
    @DisplayName("트레이너 조회")
    class GetTrainer {

        @Test
        @DisplayName("성공: 체육관별 트레이너 전체 조회")
        void getAllTrainer_Success() {
            // given
            Long gymId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            
            Trainer mockTrainer = mock(Trainer.class);
            given(mockTrainer.getId()).willReturn(1L);
            given(mockTrainer.getName()).willReturn("김트레이너");
            given(mockTrainer.getPrice()).willReturn(50000);
            given(mockTrainer.getContent()).willReturn("전문 트레이너입니다.");
            given(mockTrainer.getExperience()).willReturn("5년 경력");
            given(mockTrainer.getTrainerStatus()).willReturn(TrainerStatus.ACTIVE);
            given(mockTrainer.getImages()).willReturn(new ArrayList<>());
            
            List<Trainer> trainers = List.of(mockTrainer);
            Page<Trainer> trainerPage = new PageImpl<>(trainers, pageable, 1);

            given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(testGym);
            given(trainerRepository.findAllByGym(testGym, pageable)).willReturn(trainerPage);

            // when
            Page<TrainerResponseDto> result = trainerService.getAllTrainer(gymId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);

            verify(gymRepository).findByIdOrElseThrow(gymId);
            verify(trainerRepository).findAllByGym(testGym, pageable);
        }

        @Test
        @DisplayName("성공: 트레이너 상세 조회")
        void getTrainerById_Success() {
            // given
            Long gymId = 1L;
            Long trainerId = 1L;

            Trainer mockTrainer = mock(Trainer.class);
            given(mockTrainer.getName()).willReturn("김트레이너");
            given(mockTrainer.getPrice()).willReturn(50000);
            given(mockTrainer.getContent()).willReturn("전문 트레이너입니다.");
            given(mockTrainer.getExperience()).willReturn("5년 경력");
            given(mockTrainer.getTrainerStatus()).willReturn(TrainerStatus.ACTIVE);
            given(mockTrainer.getImages()).willReturn(new ArrayList<>());
            given(mockTrainer.getCreatedAt()).willReturn(LocalDateTime.now());
            willDoNothing().given(mockTrainer).validateTrainerBelongsToGym(mockTrainer, testGym);

            given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(testGym);
            given(trainerRepository.findByIdOrElseThrow(trainerId)).willReturn(mockTrainer);

            // when
            TrainerDetailResponseDto result = trainerService.getTrainerById(gymId, trainerId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("김트레이너");
            assertThat(result.price()).isEqualTo(50000);
            assertThat(result.content()).isEqualTo("전문 트레이너입니다.");
            assertThat(result.experience()).isEqualTo("5년 경력");
            assertThat(result.trainerStatus()).isEqualTo(TrainerStatus.ACTIVE);

            verify(gymRepository).findByIdOrElseThrow(gymId);
            verify(trainerRepository).findByIdOrElseThrow(trainerId);
            verify(mockTrainer).validateTrainerBelongsToGym(mockTrainer, testGym);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 체육관")
        void getTrainerById_GymNotFound() {
            // given
            Long gymId = 999L;
            Long trainerId = 1L;

            given(gymRepository.findByIdOrElseThrow(gymId))
                .willThrow(new BaseException(ExceptionCode.GYM_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> trainerService.getTrainerById(gymId, trainerId))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(ExceptionCode.GYM_NOT_FOUND);
                });

            verify(gymRepository).findByIdOrElseThrow(gymId);
            verify(trainerRepository, never()).findByIdOrElseThrow(anyLong());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 트레이너")
        void getTrainerById_TrainerNotFound() {
            // given
            Long gymId = 1L;
            Long trainerId = 999L;

            given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(testGym);
            given(trainerRepository.findByIdOrElseThrow(trainerId))
                .willThrow(new BaseException(ExceptionCode.TRAINER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> trainerService.getTrainerById(gymId, trainerId))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(ExceptionCode.TRAINER_NOT_FOUND);
                });
        }
    }

    @Nested
    @DisplayName("트레이너 수정")
    class UpdateTrainer {

        @Test
        @DisplayName("성공: 트레이너 정보 수정")
        void updateTrainer_Success() {
            // given
            Long userId = 1L;
            Long gymId = 1L;
            Long trainerId = 1L;
            String name = "수정된트레이너";
            int price = 60000;
            String content = "수정된 내용";
            String experience = "6년 경력";
            TrainerStatus status = TrainerStatus.ACTIVE;
            List<String> imgs = testImages;

            Trainer mockTrainer = mock(Trainer.class);
            given(mockTrainer.getId()).willReturn(trainerId);
            given(mockTrainer.getName()).willReturn(name);
            given(mockTrainer.getPrice()).willReturn(price);
            given(mockTrainer.getContent()).willReturn(content);
            given(mockTrainer.getExperience()).willReturn(experience);
            given(mockTrainer.getTrainerStatus()).willReturn(status);
            given(mockTrainer.getGym()).willReturn(testGym);
            given(mockTrainer.getImages()).willReturn(new ArrayList<>());
            willDoNothing().given(mockTrainer).validateTrainerBelongsToGym(mockTrainer, testGym);
            willDoNothing().given(mockTrainer).update(name, price, content, status, experience, imgs);

            given(userRepository.findByIdOrElseThrow(userId)).willReturn(ownerUser);
            given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(testGym);
            given(trainerRepository.findByIdOrElseThrow(trainerId)).willReturn(mockTrainer);
            given(trainerRepository.save(mockTrainer)).willReturn(mockTrainer);

            // when
            TrainerResponseDto result = trainerService.updateTrainer(
                userId, gymId, trainerId, name, price, content, experience, status, imgs);

            // then
            assertThat(result).isNotNull();

            verify(userRepository).findByIdOrElseThrow(userId);
            verify(gymRepository).findByIdOrElseThrow(gymId);
            verify(trainerRepository).findByIdOrElseThrow(trainerId);
            verify(mockTrainer).validateTrainerBelongsToGym(mockTrainer, testGym);
            verify(mockTrainer).update(name, price, content, status, experience, imgs);
            verify(trainerRepository).save(mockTrainer);
        }

        @Test
        @DisplayName("실패: 권한 없음 - 일반 사용자")
        void updateTrainer_NotOwner() {
            // given
            Long userId = 2L;
            Long gymId = 1L;
            Long trainerId = 1L;

            given(userRepository.findByIdOrElseThrow(userId)).willReturn(normalUser);

            // when & then
            assertThatThrownBy(() -> trainerService.updateTrainer(
                userId, gymId, trainerId, "이름", 50000, "내용", "경력", TrainerStatus.ACTIVE, testImages))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(ExceptionCode.NOT_GYM_OWNER);
                });
        }

        @Test
        @DisplayName("실패: 다른 체육관의 트레이너 수정 시도")
        void updateTrainer_DifferentGym() {
            // given
            Long userId = 1L;
            Long gymId = 1L;
            Long trainerId = 1L;

            // 다른 체육관 생성
            User anotherOwner = new User(
                "another@test.com", null, "password123", "다른사장",
                "010-5555-5555", 35, "서울시 종로구",
                Gender.MAN, UserRole.OWNER, "LOCAL"
            );
            setId(anotherOwner, 3L);

            Gym anotherGym = Gym.of(
                List.of("gym1.jpg"), "다른헬스장", "02-0000-0000", "다른 체육관입니다.",
                "서울", "강남구", "테스트로 123",
                LocalTime.of(6, 0), LocalTime.of(23, 0), "다른 체육관", anotherOwner
            );
            setId(anotherGym, 2L);

            Trainer mockTrainer = mock(Trainer.class);
            given(mockTrainer.getGym()).willReturn(anotherGym);

            given(userRepository.findByIdOrElseThrow(userId)).willReturn(ownerUser);
            given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(testGym);
            given(trainerRepository.findByIdOrElseThrow(trainerId)).willReturn(mockTrainer);

            // when & then
            assertThatThrownBy(() -> trainerService.updateTrainer(
                userId, gymId, trainerId, "이름", 50000, "내용", "경력", TrainerStatus.ACTIVE, testImages))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(ExceptionCode.NOT_GYM_OWNER);
                });
        }
    }


    @Nested
    @DisplayName("트레이너 삭제")
    class DeleteTrainer {

        @Test
        @DisplayName("성공: 트레이너 삭제")
        void deleteTrainer_Success() {
            // given
            Long userId = 1L;
            Long gymId = 1L;
            Long trainerId = 1L;

            Trainer mockTrainer = mock(Trainer.class);
            given(mockTrainer.getGym()).willReturn(testGym);
            willDoNothing().given(mockTrainer).validateTrainerBelongsToGym(mockTrainer, testGym);

            given(userRepository.findByIdOrElseThrow(userId)).willReturn(ownerUser);
            given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(testGym);
            given(trainerRepository.findByIdOrElseThrow(trainerId)).willReturn(mockTrainer);
            willDoNothing().given(trainerRepository).delete(mockTrainer);

            // when
            trainerService.deleteTrainer(userId, gymId, trainerId);

            // then
            verify(userRepository).findByIdOrElseThrow(userId);
            verify(gymRepository).findByIdOrElseThrow(gymId);
            verify(trainerRepository).findByIdOrElseThrow(trainerId);
            verify(mockTrainer).validateTrainerBelongsToGym(mockTrainer, testGym);
            verify(trainerRepository).delete(mockTrainer);
        }

        @Test
        @DisplayName("실패: 권한 없음")
        void deleteTrainer_NotOwner() {
            // given
            Long userId = 2L;

            given(userRepository.findByIdOrElseThrow(userId)).willReturn(normalUser);

            // when & then
            assertThatThrownBy(() -> trainerService.deleteTrainer(userId, 1L, 1L))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(ExceptionCode.NOT_GYM_OWNER);
                });

            verify(trainerRepository, never()).delete(any(Trainer.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 트레이너")
        void deleteTrainer_TrainerNotFound() {
            // given
            Long userId = 1L;
            Long gymId = 1L;
            Long trainerId = 999L;

            given(userRepository.findByIdOrElseThrow(userId)).willReturn(ownerUser);
            given(gymRepository.findByIdOrElseThrow(gymId)).willReturn(testGym);
            given(trainerRepository.findByIdOrElseThrow(trainerId))
                .willThrow(new BaseException(ExceptionCode.TRAINER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> trainerService.deleteTrainer(userId, gymId, trainerId))
                .isInstanceOf(BaseException.class)
                .satisfies(exception -> {
                    BaseException baseException = (BaseException) exception;
                    assertThat(baseException.getErrorCode()).isEqualTo(ExceptionCode.TRAINER_NOT_FOUND);
                });

            verify(trainerRepository, never()).delete(any(Trainer.class));
        }
    }
}
