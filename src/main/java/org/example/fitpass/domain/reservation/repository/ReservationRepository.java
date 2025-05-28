package org.example.fitpass.domain.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.example.fitpass.domain.reservation.ReservationStatus;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 트레이너의 특정 날짜 예약된 시간들 조회
    @Query("SELECT r.reservationTime FROM Reservation r WHERE r.trainer = :trainer AND r.reservationDate = :date AND r.reservationStatus != :status")
    List<LocalTime> findReservedTimesByTrainerAndDate(
        @Param("trainer") Trainer trainer,
        @Param("date") LocalDate date,
        @Param("status") ReservationStatus status
    );

    // 간단한 버전 (취소된 예약 제외)
    default List<LocalTime> findReservedTimesByTrainerAndDate(Trainer trainer, LocalDate date) {
        return findReservedTimesByTrainerAndDate(trainer, date, ReservationStatus.CANCELLED);
    }

}
