package org.example.fitpass.domain.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class GetReservationResponseDto {
    private final Long reservationId;
    private final Long userId;
    private final Long gymId;
    private final Long trainerId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate reservationDate;

    @JsonFormat(pattern = "HH:mm")
    private final LocalTime reservationTime;

    private final ReservationStatus reservationStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime updatedAt;

    public GetReservationResponseDto(Long reservationId, Long userId, Long gymId, Long trainerId,
        LocalDate reservationDate, LocalTime reservationTime, ReservationStatus reservationStatus,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.gymId = gymId;
        this.trainerId = trainerId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.reservationStatus = reservationStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static GetReservationResponseDto from(Reservation reservation) {
        return new GetReservationResponseDto(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getGym().getId(),
                reservation.getTrainer().getId(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getReservationStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }

}
