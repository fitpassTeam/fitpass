package org.example.fitpass.domain.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;

public record ReservationResponseDto(
    Long reservationId,
    Long userId,
    Long gymId,
    Long trainerId,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate reservationDate,

    @JsonFormat(pattern = "HH:mm")
    LocalTime reservationTime,

    ReservationStatus reservationStatus,

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt
) {

    public ReservationResponseDto(Long reservationId, Long userId, Long gymId, Long trainerId,
        LocalDate reservationDate, LocalTime reservationTime, ReservationStatus reservationStatus,
        LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.gymId = gymId;
        this.trainerId = trainerId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.reservationStatus = reservationStatus;
        this.createdAt = createdAt;
    }

    public static ReservationResponseDto from(Reservation reservation) {
        return new ReservationResponseDto(
            reservation.getId(),
            reservation.getUser().getId(),
            reservation.getGym().getId(),
            reservation.getTrainer().getId(),
            reservation.getReservationDate(),
            reservation.getReservationTime(),
            reservation.getReservationStatus(),
            reservation.getCreatedAt()
        );

    }

}
