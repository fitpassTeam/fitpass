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
    @Query("SELECT r.reservationTime FROM Reservation r WHERE r.trainer = :trainer AND r.reservationDate = :date AND r.reservationStatus != :status")
    List<LocalTime> findReservationTimeByTrainerAndReservationDateAndReservationStatusNot(
        @Param("trainer") Trainer trainer,
        @Param("date") LocalDate date,
        @Param("status") ReservationStatus status
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

    // 만료된 확정 예약 조회
    @Query("SELECT r FROM Reservation r WHERE r.reservationStatus = 'CONFIRMED' " +
        "AND (r.reservationDate < :today OR " +
        "(r.reservationDate = :today AND r.reservationTime < :currentTime))")
    List<Reservation> findExpiredConfirmedReservations(
        @Param("today") LocalDate today,
        @Param("currentTime") LocalTime currentTime
    );

    // 24시간 이상 PENDING 상태인 예약 조회
    @Query("SELECT r FROM Reservation r WHERE r.reservationStatus = 'PENDING' " +
        "AND r.createdAt < :cutoffDate")
    List<Reservation> findLongPendingReservations(@Param("cutoffDate") LocalDate cutoffDate);

    // 특정 날짜의 확정된 예약들 조회
    List<Reservation> findByReservationDateAndReservationStatus(
        LocalDate reservationDate, ReservationStatus reservationStatus);

    // 특정 날짜, 시간의 확정된 예약들 조회
    List<Reservation> findByReservationDateAndReservationTimeAndReservationStatus(
        LocalDate reservationDate, LocalTime reservationTime, ReservationStatus reservationStatus);
}
