package org.example.fitpass.domain.reservation.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;

@Getter
public class UpdateReservationRequestDto {

    @NotBlank(message = "날짜를 입력해주세요.")
    @Future(message = "예약 날짜는 현재 날짜 이후여야 합니다.")
    private LocalDate reservationDate;

    @NotBlank(message = "시간을 입력해주세요.")
    private LocalTime reservationTime;

    private ReservationStatus reservationStatus;
}
