package org.example.fitpass.domain.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.user.entity.User;

@Schema(description = "예약 생성 요청 DTO")
public record ReservationRequestDto(
    @Schema(description = "예약 날짜", example = "2024-12-25")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "날짜를 입력해주세요.")
    @Future(message = "예약 날짜는 현재 날짜 이후여야 합니다.")
    LocalDate reservationDate,

    @Schema(description = "예약 시간", example = "14:00")
    @JsonFormat(pattern = "HH:mm")
    @NotNull(message = "시간을 입력해주세요.")
    LocalTime reservationTime
) {
    public static Reservation from(LocalDate reservationDate, LocalTime reservationTime, ReservationStatus status, User user, Gym gym, Trainer trainer) {
        return new Reservation(
            reservationDate,
            reservationTime,
            ReservationStatus.PENDING,
            user,
            gym,
            trainer
        );
    }
}
