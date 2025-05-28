package org.example.fitpass.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.reservation.dto.ReservationRequestDto;
import org.example.fitpass.domain.reservation.dto.ReservationResponseDto;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.repository.ReservationRepository;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final TrainerRepository trainerRepository;

    // 예약 가능 시간 조회

    // 예약 생성
    @Transactional
    public ReservationResponseDto createReservation (ReservationRequestDto reservationRequestDto, User user, Long gymId, Long trainerId) {
        // 사용자 조회
        User findUser = userRepository.findByIdOrElseThrow(user.getId());
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 트레이너 조회
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);

        Reservation reservation = new Reservation(
            reservationRequestDto.getReservationDate(),
            reservationRequestDto.getReservationTime(),
            reservationRequestDto.getReservationStatus(),
            findUser, gym, trainer);

        Reservation createReservation = reservationRepository.save(reservation);

        return ReservationResponseDto.from(createReservation);
    }

    // 예약 수정

}
