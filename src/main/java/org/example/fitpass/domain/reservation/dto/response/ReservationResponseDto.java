package org.example.fitpass.domain.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
import org.example.fitpass.domain.reservation.entity.Reservation;

@Getter
@AllArgsConstructor
@NoArgsConstructor

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
