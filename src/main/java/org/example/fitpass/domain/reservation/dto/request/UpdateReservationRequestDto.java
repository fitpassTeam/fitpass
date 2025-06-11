package org.example.fitpass.domain.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;

@Getter
public class UpdateReservationRequestDto {

    @NotNull(message = "날짜를 입력해주세요.")
    @Future(message = "예약 날짜는 현재 날짜 이후여야 합니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private final LocalDate reservationDate;

    @NotNull(message = "시간을 입력해주세요.")
    @JsonFormat(pattern = "HH:mm")
    private final LocalTime reservationTime;

    private final ReservationStatus reservationStatus;

    public UpdateReservationRequestDto(LocalDate reservationDate, LocalTime reservationTime,
        ReservationStatus reservationStatus) {
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.reservationStatus = reservationStatus;
    }
}
