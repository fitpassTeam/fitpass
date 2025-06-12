package org.example.fitpass.domain.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;

public record UserReservationResponseDto(
    Long reservationId,
    LocalDate reservationDate,
    LocalTime reservationTime,
    ReservationStatus status,
    LocalDateTime createdAt,
    GymInfo gym,
    TrainerInfo trainer) {


    public record GymInfo(
        Long gymId,
        String name,
        String address,
        String number) {

    }

    public record TrainerInfo(
        Long trainerId,
        String name,
        String content,
        int price) {

    }

    // Entity -> Dto 변환 메서드
    public static UserReservationResponseDto from(Reservation reservation) {
        GymInfo gymInfo = new GymInfo(
            reservation.getGym().getId(),
            reservation.getGym().getName(),
            reservation.getGym().getAddress(),
            reservation.getGym().getNumber()
        );

        TrainerInfo trainerInfo = new TrainerInfo(
            reservation.getTrainer().getId(),
            reservation.getTrainer().getName(),
            reservation.getTrainer().getContent(),
            reservation.getTrainer().getPrice()
        );

        return new UserReservationResponseDto(
            reservation.getId(),
            reservation.getReservationDate(),
            reservation.getReservationTime(),
            reservation.getReservationStatus(),
            reservation.getCreatedAt(),
            gymInfo,
            trainerInfo
        );
    }

}
