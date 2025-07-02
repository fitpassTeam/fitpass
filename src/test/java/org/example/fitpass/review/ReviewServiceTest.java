package org.example.fitpass.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
import org.example.fitpass.domain.reservation.repository.ReservationRepository;
import org.example.fitpass.domain.review.dto.response.ReviewResponseDto;
import org.example.fitpass.domain.review.entity.Review;
import org.example.fitpass.domain.review.repository.ReviewRepository;
import org.example.fitpass.domain.review.service.ReviewService;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymRepository gymRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private Reservation reservation;
    private Review review;

    @BeforeEach
    void setup() {
        Long userId = 1L;
        Long gymId = 1L;
        Long trainerId = 1L;
        Long reservationId = 1L;

        user = new User("test@test.com", "tester", "GOOGLE");
        ReflectionTestUtils.setField(user, "id", userId);

        Gym gym = Gym.of(List.of(), "헬스장", "1234", "내용", "서울", "강남", "주소",
            LocalTime.of(9, 0), LocalTime.of(22, 0), "요약", user);
        ReflectionTestUtils.setField(gym, "id", gymId);

        Trainer trainer = new Trainer(List.of(), "김트레이너", 50000, "PT 전문가", "5년 경력");
        trainer.assignToGym(gym);
        ReflectionTestUtils.setField(trainer, "id", trainerId);

        reservation = new Reservation(
            LocalDate.now(),           // 예약 날짜
            LocalTime.now(),           // 예약 시간
            ReservationStatus.COMPLETED, // 상태
            user,
            gym,
            trainer
        );
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        review = Review.of(reservation, "content", 5, 4, user);
        ReflectionTestUtils.setField(review, "id", 200L);
    }

    @Test
    void createReview_success() {
        given(userRepository.findByIdOrElseThrow(user.getId())).willReturn(user);
        given(reservationRepository.findByIdOrElseThrow(reservation.getId())).willReturn(reservation);
        given(reviewRepository.existsByReservationId(reservation.getId())).willReturn(false);
        given(reviewRepository.save(Mockito.any(Review.class))).willReturn(review);

        ReviewResponseDto dto = reviewService.createReview(
            reservation.getId(),
            user.getId(),
            "content",
            5,
            4
        );

        assertEquals(200L, dto.reviewId());
        verify(reviewRepository).save(Mockito.any(Review.class));
    }

    @Test
    void createReview_fail_notOwner() {
        // given
        Long userId = 1L;
        Long reservationId = 10L;

        // 실제 로그인한 유저 (리뷰 생성 시도자)
        User user = new User("test@test.com", "tester", "GOOGLE");
        ReflectionTestUtils.setField(user, "id", userId);

        // 예약에 연결된 다른 유저 (타인)
        User otherUser = new User("other@test.com", "other", "GOOGLE");
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        // 트레이너와 체육관은 mock 처리
        Gym gym = Gym.of(List.of(), "헬스장", "1234", "내용", "서울", "강남", "주소",
            LocalTime.of(9, 0), LocalTime.of(22, 0), "요약", otherUser);
        ReflectionTestUtils.setField(gym, "id", 1L);

        Trainer trainer = new Trainer(List.of(), "김트레이너", 50000, "PT 전문가", "5년 경력");
        trainer.assignToGym(gym);
        ReflectionTestUtils.setField(trainer, "id", 1L);

        // 예약 객체 (타인의 예약)
        Reservation reservation = new Reservation(
            LocalDate.now(),
            LocalTime.now(),
            ReservationStatus.COMPLETED,
            otherUser, // 주의: 다른 유저로 설정
            gym,
            trainer
        );
        ReflectionTestUtils.setField(reservation, "id", reservationId);

        // mocking
        given(userRepository.findByIdOrElseThrow(user.getId())).willReturn(user);
        given(reservationRepository.findByIdOrElseThrow(reservationId)).willReturn(reservation);

        // when & then
        BaseException ex = assertThrows(BaseException.class, () -> {
            reviewService.createReview(reservationId, user.getId(), "content", 5, 4);
        });

        assertEquals(ExceptionCode.NOT_RESERVATION_OWNER, ex.getErrorCode());
    }



    @Test
    void updateReview_success() {
        given(userRepository.findByIdOrElseThrow(user.getId())).willReturn(user);
        given(reservationRepository.findByIdOrElseThrow(reservation.getId())).willReturn(reservation);
        given(reviewRepository.findByIdOrElseThrow(review.getId())).willReturn(review);

        ReviewResponseDto dto = reviewService.updateReview(
            reservation.getId(),
            review.getId(),
            "updated content",
            4,
            3,
            user.getId()
        );

        assertEquals("updated content", dto.content());
    }

    @Test
    void deleteReview_success() {
        given(userRepository.findByIdOrElseThrow(user.getId())).willReturn(user);
        given(reservationRepository.findByIdOrElseThrow(reservation.getId())).willReturn(reservation);
        given(reviewRepository.findByIdOrElseThrow(review.getId())).willReturn(review);

        reviewService.deleteReview(reservation.getId(), review.getId(), user.getId());

        verify(reviewRepository).delete(review);
    }

}

