package org.example.fitpass.domain.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;

public record AllGymReservationResponseDto(
    String userName,
    Long reservationId,
    String trainerName,
    LocalDate reservationDate,
    LocalTime reservationTime,
    ReservationStatus status,
    Long trainerId
) {

    public static AllGymReservationResponseDto from(Reservation reservation) {
        return new AllGymReservationResponseDto(
            reservation.getUser().getName(),
            reservation.getId(),
            reservation.getTrainer().getName(),
            reservation.getReservationDate(),
            reservation.getReservationTime(),
            reservation.getReservationStatus(),
            reservation.getTrainer().getId()
        );
    }
}
