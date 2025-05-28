package org.example.fitpass.domain.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.reservation.dto.ReservationRequestDto;
import org.example.fitpass.domain.reservation.dto.ReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.UpdateReservationRequestDto;
import org.example.fitpass.domain.reservation.dto.UpdateReservationResponseDto;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
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
    public List<LocalTime> getAvailableTimes(Long gymId, Long trainerId, LocalDate date) {
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);

        // 체육관 운영 시간 (체육관 엔티티에서 가져오기)
        List<LocalTime> possibleTimes = generateTimeSlots(
            gym.getOpenTime().toLocalTime(),
            gym.getCloseTime().toLocalTime(),
            60
        );
        // 해당 날짜에 이미 예약된 시간들 제외
        List<LocalTime> reservedTimes = reservationRepository.findReservedTimesByTrainerAndDate(trainer, date);

        // 예약 가능한 시간만 반환
        return possibleTimes.stream()
            .filter(time -> !reservedTimes.contains(time))
            .collect(Collectors.toList());

    }

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
    @Transactional
    public UpdateReservationResponseDto updateReservation (UpdateReservationRequestDto updateReservationRequestDto, Long gymId, Long trainerId, Long reservationId){
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 트레이너 조회
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);
        // 예약 조회
        Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);

        // PENDING 상태만 수정 가능
        if(!updateReservationRequestDto.getReservationStatus().equals(ReservationStatus.PENDING)){
            throw new IllegalStateException("대기 상태의 예약만 변경이 가능합니다.");
        }

        // 2일 전까지만 변경 가능
        LocalDate today = LocalDate.now();
        LocalDate reservationDate = reservation.getReservationDate();
        if (ChronoUnit.DAYS.between(today, reservationDate) < 2) {
            throw new IllegalStateException("예약 2일 전까지만 변경이 가능합니다.");
        }

        // 새로운 예약 날짜도 2일 후부터 가능한지 검증
        LocalDate newReservationDate = updateReservationRequestDto.getReservationDate();
        if (ChronoUnit.DAYS.between(today, newReservationDate) < 2) {
            throw new IllegalStateException("예약은 2일 후부터 가능합니다.");
        }

        // 새로운 예약이 이미 예약이 있는지 확인 (중복 예약 방지)
        boolean isDuplicate = reservationRepository.existsByTrainerAndReservationDateAndReservationTimeAndIdNot(
            trainer,
            newReservationDate,
            updateReservationRequestDto.getReservationTime(),
            reservationId  // 현재 예약은 제외
        );
        if (isDuplicate) {
            throw new IllegalStateException("해당 시간에 이미 다른 예약이 있습니다.");
        }
        // 예약 정보 업데이트
        reservation.updateReservation(
            updateReservationRequestDto.getReservationDate(),
            updateReservationRequestDto.getReservationTime(),
            updateReservationRequestDto.getReservationStatus());

        Reservation updateReservation = reservationRepository.save(reservation);

        return UpdateReservationResponseDto.from(updateReservation);
    }

    // 예약 취소
    @Transactional
    public void cancelReservation(Long gymId, Long trainerId, Long reservationId) {
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 트레이너 조회
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);
        // 예약 조회
        Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);

        // PENDING 상태만 수정 가능
        if(!reservation.getReservationStatus().equals(ReservationStatus.PENDING)){
            throw new IllegalStateException("대기 상태의 예약만 변경이 가능합니다.");
        }

        // 취소 기한 확인 추가 (예: 2일 전까지만 취소 가능)
        LocalDate today = LocalDate.now();
        LocalDate reservationDate = reservation.getReservationDate();

        if (ChronoUnit.DAYS.between(today, reservationDate) < 2) {
            throw new IllegalStateException("예약 2일 전까지만 취소가 가능합니다.");
        }

        reservation.cancelReservation();
    }


    // 시간 슬롯 생성 유틸리티 메서드
    private List<LocalTime> generateTimeSlots(LocalTime start, LocalTime end, int intervalMinutes) {
        List<LocalTime> timeSlots = new ArrayList<>();
        LocalTime current = start;

        while (!current.isAfter(end.minusMinutes(intervalMinutes))) {
            timeSlots.add(current);
            current = current.plusMinutes(intervalMinutes);
        }

        return timeSlots;
    }
}
