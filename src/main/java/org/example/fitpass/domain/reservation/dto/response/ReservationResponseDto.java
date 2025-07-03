package org.example.fitpass.domain.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;

@Schema(description = "예약 응답 DTO")
public record ReservationResponseDto(
    @Schema(description = "예약 ID", example = "1")
    Long reservationId,
    
    @Schema(description = "사용자 ID", example = "1")
    Long userId,
    
    @Schema(description = "체육관 ID", example = "1")
    Long gymId,
    
    @Schema(description = "트레이너 ID", example = "1")
    Long trainerId,

    @Schema(description = "예약 날짜", example = "2024-12-25")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate reservationDate,

    @Schema(description = "예약 시간", example = "14:00")
    @JsonFormat(pattern = "HH:mm")
    LocalTime reservationTime,

    @Schema(description = "예약 상태", example = "PENDING")
    ReservationStatus reservationStatus,

    @Schema(description = "예약 생성 시간", example = "2024-12-01 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt
) {

    public ReservationResponseDto(Long reservationId, Long userId, Long gymId, Long trainerId,
        LocalDate reservationDate, LocalTime reservationTime, ReservationStatus reservationStatus,
        LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.gymId = gymId;
        this.trainerId = trainerId;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.reservationStatus = reservationStatus;
        this.createdAt = createdAt;
    }

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
