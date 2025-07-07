package org.example.fitpass.domain.review.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
import org.example.fitpass.domain.reservation.repository.ReservationRepository;
import org.example.fitpass.domain.review.dto.response.ReviewDetailResponseDto;
import org.example.fitpass.domain.review.dto.response.ReviewResponseDto;
import org.example.fitpass.domain.review.entity.Review;
import org.example.fitpass.domain.review.repository.ReviewRepository;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final TrainerRepository trainerRepository;

    // 리뷰 생성
    @Transactional
    public ReviewResponseDto createReview(
        Long reservationId,
        Long userId,
        String content,
        int gymRating,
        int trainerRating) {
        // 사용자 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 예약 조회
        Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);
        // 예약 소유자 확인
        if(!reservation.getUser().getId().equals(userId)) {
            throw new BaseException(ExceptionCode.NOT_RESERVATION_OWNER);
        }
        // 예약 완료 상태 확인
        if(reservation.getReservationStatus() != ReservationStatus.COMPLETED) {
            throw new BaseException(ExceptionCode.RESERVATION_NOT_COMPLETED);
        }
        // 이미 리뷰 작성했는지 확인
        if (reviewRepository.existsByReservationId(reservationId)){
            throw new BaseException(ExceptionCode.REVIEW_ALREADY_EXISTS);
        }
        // 리뷰 저장
        Review review = Review.of(
            reservation,
            content,
            gymRating,
            trainerRating,
            user);
        Review savedReview = reviewRepository.save(review);

        return ReviewResponseDto.from(savedReview);
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponseDto updateReview(
        Long reservationId,
        Long reviewId,
        String content,
        int gymRating,
        int trainerRating,
        Long userId) {
        // 사용자 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 예약 조회
        Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);
        // 리뷰 조회
        Review review = reviewRepository.findByIdOrElseThrow(reviewId);

        // 리뷰 작성자 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new BaseException(ExceptionCode.NOT_REVIEW_OWNER);
        }
        review.update(content, gymRating, trainerRating);

        return ReviewResponseDto.from(review);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reservationId, Long reviewId, Long userId) {
        // 사용자 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 예약 조회
        Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);
        // 리뷰 조회
        Review review = reviewRepository.findByIdOrElseThrow(reviewId);
        // 리뷰 작성자 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new BaseException(ExceptionCode.NOT_REVIEW_OWNER);
        }
        reviewRepository.delete(review);
    }

    // 리뷰 단건 조회
    @Transactional(readOnly = true)
    public ReviewDetailResponseDto getReview(Long reviewId) {
        // 리뷰 조회
        Review review = reviewRepository.findByIdOrElseThrow(reviewId);

        return ReviewDetailResponseDto.from(review);
    }

    // 사용자가 쓴 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewDetailResponseDto> getMyReviews(Long userId) {
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return reviews.stream().map(ReviewDetailResponseDto::from).toList();
    }

    // 체육관 리뷰들 조회
    @Transactional(readOnly = true)
    public List<ReviewDetailResponseDto> getGymReviews(Long gymId) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        List<Review> reviews = reviewRepository.findByGymIdOrderByCreatedAtDesc(gymId);
        return reviews.stream().map(ReviewDetailResponseDto::from).toList();
    }

    // 트레이너별 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewDetailResponseDto> getTrainerReviews(Long trainerId) {
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);
        List<Review> reviews = reviewRepository.findByTrainerIdOrderByCreatedAtDesc(trainerId);
        return reviews.stream().map(ReviewDetailResponseDto::from).toList();
    }

}
