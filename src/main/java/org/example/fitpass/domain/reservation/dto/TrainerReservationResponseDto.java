package org.example.fitpass.domain.reservation.dto;

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
        return TrainerReservationResponseDto.builder()
            .reservationId(reservation.getId())
            .reservationDate(reservation.getReservationDate())
            .reservationTime(reservation.getReservationTime())
            .status(reservation.getReservationStatus())
            .createdAt(reservation.getCreatedAt())
            .gym(GymInfo.builder()
                .gymId(reservation.getGym().getId())
                .name(reservation.getGym().getName())
                .address(reservation.getGym().getAddress())
                .number(reservation.getGym().getNumber()).build())
            .user(UserInfo.builder()
                .userId(reservation.getUser().getId())
                .name(reservation.getUser().getName())
                .email(reservation.getUser().getEmail())
                .phone(reservation.getUser().getPhone())
                .build())
            .build();
    }
}
