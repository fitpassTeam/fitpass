package org.example.fitpass.domain.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 트레이너의 특정 날짜 예약된 시간들 조회
    List<LocalTime> findReservationTimeByTrainerAndReservationDateAndReservationStatusNot(
        Trainer trainer,
        LocalDate date,
        ReservationStatus status
    );

    // 간단한 버전 (취소된 예약 제외)
    default List<LocalTime> findReservedTimesByTrainerAndDate(Trainer trainer, LocalDate date) {
        return findReservationTimeByTrainerAndReservationDateAndReservationStatusNot(trainer, date, ReservationStatus.CANCELLED);
    }

    Optional<Reservation> findById (Long reservationId);

    default Reservation findByIdOrElseThrow(Long reservationId) {
        Reservation reservation = findById(reservationId).orElseThrow(
            () -> new BaseException(ExceptionCode.RESERVATION_NOT_FOUND));
        return reservation;
    }

    // 중복 예약 체크 메서드
    boolean existsByTrainerAndReservationDateAndReservationTimeAndIdNot(
        Trainer trainer,
        LocalDate date,
        LocalTime time,
        Long excludeId
    );
    // 생성 시 중복 예약 체크 메서드
    boolean existsByTrainerAndReservationDateAndReservationTime(
        Trainer trainer,
        LocalDate reservationDate,
        LocalTime reservationTime
    );

    // 트레이너별 예약 목록 (최신순)
    List<Reservation> findByTrainerOrderByReservationDateDescReservationTimeDesc(Trainer trainer);

    // 사용자별 예약 목록 (최신순)
    List<Reservation> findByUserOrderByReservationDateDescReservationTimeDesc(User user);

}
