package org.example.fitpass.domain.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;

@Getter
public class UserReservationResponseDto {
    private final Long reservationId;
    private final LocalDate reservationDate;
    private final LocalTime reservationTime;
    private final ReservationStatus status;
    private final LocalDateTime createdAt;

    private final GymInfo gym;
    private final TrainerInfo trainer;

    public UserReservationResponseDto(Long reservationId, LocalDate reservationDate,
        LocalTime reservationTime, ReservationStatus status, LocalDateTime createdAt, GymInfo gym,
        TrainerInfo trainer) {
        this.reservationId = reservationId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.status = status;
        this.createdAt = createdAt;
        this.gym = gym;
        this.trainer = trainer;
    }

    @Getter
    @AllArgsConstructor
    public static class GymInfo {
        private final Long gymId;
        private final String name;
        private final String address;
        private final String number;
    }

    @Getter
    @AllArgsConstructor
    public static class TrainerInfo {
        private final Long trainerId;
        private final String name;
        private final String content;
        private final int price;
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
