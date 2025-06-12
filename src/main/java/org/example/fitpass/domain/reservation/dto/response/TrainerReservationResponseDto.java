package org.example.fitpass.domain.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;

public record TrainerReservationResponseDto(
    Long reservationId,
    LocalDate reservationDate,
    LocalTime reservationTime,
    ReservationStatus status,
    LocalDateTime createdAt,
    GymInfo gym,
    UserInfo user
) {


    public record GymInfo(
        Long gymId,
        String name,
        String address,
        String number) {

    }


    public record UserInfo(
        Long userId,
        String name,
        String email,
        String phone) {

    }

    // Entity -> Dto 변환 메서드
    public static TrainerReservationResponseDto from(Reservation reservation) {
        GymInfo gymInfo = new GymInfo(
            reservation.getGym().getId(),
            reservation.getGym().getName(),
            reservation.getGym().getAddress(),
            reservation.getGym().getNumber()
        );

        UserInfo userInfo = new UserInfo(
            reservation.getUser().getId(),
            reservation.getUser().getName(),
            reservation.getUser().getEmail(),
            reservation.getUser().getPhone()
        );

        return new TrainerReservationResponseDto(
            reservation.getId(),
            reservation.getReservationDate(),
            reservation.getReservationTime(),
            reservation.getReservationStatus(),
            reservation.getCreatedAt(),
            gymInfo,
            userInfo
        );
    }
}
