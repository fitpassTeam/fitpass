package org.example.fitpass.domain.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.user.entity.User;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reservations")
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate reservationDate;

    @Column(nullable = false, columnDefinition = "TIME")
    private LocalTime reservationTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus reservationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    public Reservation(LocalDate reservationDate, LocalTime reservationTime, ReservationStatus reservationStatus, User user,  Gym gym, Trainer trainer) {
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.reservationStatus = reservationStatus;
        this.user = user;
        this.gym = gym;
        this.trainer = trainer;
    }

    // 수정 기능
    public void updateReservation(LocalDate reservationDate, LocalTime reservationTime, ReservationStatus reservationStatus) {
       this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.reservationStatus = reservationStatus;
    }

    // 취소 상태 변환
    public void cancelReservation() {
        if (this.reservationStatus == ReservationStatus.PENDING
            || this.reservationStatus == ReservationStatus.CONFIRMED) {
            this.reservationStatus = ReservationStatus.CANCELLED;
        } else {
            throw new IllegalStateException("대기 중이거나 확정된 예약만 취소할 수 있습니다.");
        }
    }
}
