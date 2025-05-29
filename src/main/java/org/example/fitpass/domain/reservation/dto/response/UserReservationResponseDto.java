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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReservationResponseDto {
    private Long reservationId;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    private GymInfo gym;
    private TrainerInfo trainer;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GymInfo {
        private Long gymId;
        private String name;
        private String address;
        private String number;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrainerInfo {
        private Long trainerId;
        private String name;
        private String content;
        private int price;
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
