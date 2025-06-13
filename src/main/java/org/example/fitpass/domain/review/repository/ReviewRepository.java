package org.example.fitpass.domain.review.repository;

import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.review.entity.Review;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 예약 별 리뷰 존재 여부 확인
    boolean existsByReservationId(Long reservationId);

    default Review findByIdOrElseThrow (Long reveiwId) {
        Review review = findById(reveiwId).orElseThrow(
            () -> new BaseException(ExceptionCode.REVIEW_NOT_FOUND)
        );
        return review;
    }

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Review> findByGymIdOrderByCreatedAtDesc(Long gymId);

    List<Review> findByTrainerIdOrderByCreatedAtDesc(Long trainerId);

}
