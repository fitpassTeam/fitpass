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
public class TrainerReservationResponseDto {
    private Long reservationId;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private ReservationStatus status;
    private LocalDateTime createdAt;

    private GymInfo gym;
    private UserInfo user;

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
    public static class UserInfo {
        private Long userId;
        private String name;
        private String email;
        private String phone;
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
