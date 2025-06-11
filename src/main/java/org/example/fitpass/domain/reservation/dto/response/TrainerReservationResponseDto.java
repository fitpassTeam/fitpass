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
public class TrainerReservationResponseDto {
    private final Long reservationId;
    private final LocalDate reservationDate;
    private final LocalTime reservationTime;
    private final ReservationStatus status;
    private final LocalDateTime createdAt;

    private final GymInfo gym;
    private final UserInfo user;

    public TrainerReservationResponseDto(Long reservationId, LocalDate reservationDate,
        LocalTime reservationTime, ReservationStatus status, LocalDateTime createdAt, GymInfo gym,
        UserInfo user) {
        this.reservationId = reservationId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.status = status;
        this.createdAt = createdAt;
        this.gym = gym;
        this.user = user;
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
    public static class UserInfo {
        private final Long userId;
        private final String name;
        private final String email;
        private final String phone;
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
