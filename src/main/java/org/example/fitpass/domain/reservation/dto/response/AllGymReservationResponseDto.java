package org.example.fitpass.domain.reservation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;

@Schema(description = "체육관 전체 예약 응답 DTO")
public record AllGymReservationResponseDto(
    @Schema(description = "예약자 이름", example = "김핏패스")
    String userName,
    
    @Schema(description = "예약 ID", example = "1")
    Long reservationId,
    
    @Schema(description = "트레이너 이름", example = "김트레이너")
    String trainerName,
    
    @Schema(description = "예약 날짜", example = "2024-12-25")
    LocalDate reservationDate,
    
    @Schema(description = "예약 시간", example = "14:00")
    LocalTime reservationTime,
    
    @Schema(description = "예약 상태", example = "CONFIRMED")
    ReservationStatus status,
    
    @Schema(description = "트레이너 ID", example = "1")
    Long trainerId
) {

    public static AllGymReservationResponseDto from(Reservation reservation) {
        return new AllGymReservationResponseDto(
            reservation.getUser().getName(),
            reservation.getId(),
            reservation.getTrainer().getName(),
            reservation.getReservationDate(),
            reservation.getReservationTime(),
            reservation.getReservationStatus(),
            reservation.getTrainer().getId()
        );
    }
}
