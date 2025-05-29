package org.example.fitpass.domain.reservation.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.user.entity.User;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationRequestDto {

    @NotBlank(message = "날짜를 입력해주세요.")
    @Future(message = "예약 날짜는 현재 날짜 이후여야 합니다.")
    private LocalDate reservationDate;

    @NotBlank(message = "시간을 입력해주세요.")
    private LocalTime reservationTime;

    private ReservationStatus reservationStatus;

    public Reservation toEntity(User user, Gym gym, Trainer trainer) {
        return Reservation.builder()
            .reservationDate(this.reservationDate)
            .reservationTime(this.reservationTime)
            .reservationStatus(this.reservationStatus !=null
                ? this.reservationStatus : ReservationStatus.PENDING) // 기본값 설정
            .user(user)
            .gym(gym)
            .trainer(trainer)
            .build();
    }

}
