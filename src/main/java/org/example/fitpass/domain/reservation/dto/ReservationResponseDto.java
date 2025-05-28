package org.example.fitpass.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.reservation.ReservationStatus;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.user.entity.User;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationResponseDto {

    private Long reservationId;
    private Long userId;
    private Long gymId;
    private Long trainerId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime reservationTime;

    private ReservationStatus reservationStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static ReservationResponseDto from(Reservation reservation) {
        return ReservationResponseDto.builder()
            .reservationId(reservation.getId())
            .userId(reservation.getUser().getId())
            .gymId(reservation.getGym().getId())
            .trainerId(reservation.getTrainer().getId())
            .reservationDate(reservation.getReservationDate())
            .reservationTime(reservation.getReservationTime())
            .reservationStatus(reservation.getReservationStatus())
            .createdAt(reservation.getCreatedAt())
            .build();
    }

}
