package org.example.fitpass.domain.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;

@Schema(description = "예약 수정 요청 DTO")
public record UpdateReservationRequestDto(
    @Schema(description = "수정할 예약 날짜", example = "2024-12-30")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "날짜를 입력해주세요.")
    @Future(message = "예약 날짜는 현재 날짜 이후여야 합니다.")
    LocalDate reservationDate,

    @Schema(description = "수정할 예약 시간", example = "16:00")
    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "시간을 입력해주세요.")
    LocalTime reservationTime,

    @Schema(description = "예약 상태", example = "PENDING")
    ReservationStatus reservationStatus
) {

    public UpdateReservationRequestDto(LocalDate reservationDate, LocalTime reservationTime,
        ReservationStatus reservationStatus) {
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.reservationStatus = reservationStatus;
    }
}
