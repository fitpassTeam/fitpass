package org.example.fitpass.domain.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.user.entity.User;

public record ReservationRequestDto(
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "날짜를 입력해주세요.")
    @Future(message = "예약 날짜는 현재 날짜 이후여야 합니다.")
    LocalDate reservationDate,

    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "시간을 입력해주세요.")
    LocalTime reservationTime,

    ReservationStatus reservationStatus) {

    public ReservationRequestDto(LocalDate reservationDate, LocalTime reservationTime,
        ReservationStatus reservationStatus) {
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.reservationStatus = reservationStatus;
    }

    public static Reservation from(ReservationRequestDto dto, User user, Gym gym, Trainer trainer) {
        return new Reservation(
            dto.reservationDate,
            dto.reservationTime,
            dto.reservationStatus != null ? dto.reservationStatus : ReservationStatus.PENDING,
            user,
            gym,
            trainer
        );
    }

}
