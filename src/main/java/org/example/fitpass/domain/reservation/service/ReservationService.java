package org.example.fitpass.domain.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.point.dto.request.PointUseRefundRequestDto;
import org.example.fitpass.domain.point.service.PointService;
import org.example.fitpass.domain.reservation.dto.request.ReservationRequestDto;
import org.example.fitpass.domain.reservation.dto.request.UpdateReservationRequestDto;
import org.example.fitpass.domain.reservation.dto.response.GetReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.response.ReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.response.TrainerReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.response.UpdateReservationResponseDto;
import org.example.fitpass.domain.reservation.dto.response.UserReservationResponseDto;
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
    private final PointService pointService;

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
    public ReservationResponseDto createReservation (ReservationRequestDto reservationRequestDto, Long userId, Long gymId, Long trainerId) {
        // 사용자 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 트레이너 조회
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);

        // 중복 예약 검증
        boolean isDuplicate = reservationRepository.existsByTrainerAndReservationDateAndReservationTime(
            trainer,
            reservationRequestDto.getReservationDate(),
            reservationRequestDto.getReservationTime()
        );
        if (isDuplicate) {
            throw new IllegalStateException("해당 시간에 이미 예약이 있습니다.");
        }

        // 포인트 사용
        PointUseRefundRequestDto pointUseRefundRequestDto = new PointUseRefundRequestDto();
        pointUseRefundRequestDto.setAmount(trainer.getPrice()); // 트레이너 이용료
        pointUseRefundRequestDto.setDescription("PT 예약 - " + trainer.getName());

        int newBalance = pointService.usePoint(userId, pointUseRefundRequestDto);

        Reservation reservation = new Reservation(
            reservationRequestDto.getReservationDate(),
            reservationRequestDto.getReservationTime(),
            reservationRequestDto.getReservationStatus(),
            user, gym, trainer);

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
        if(!reservation.getReservationStatus().equals(ReservationStatus.PENDING)){
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
    public void cancelReservation(Long userId, Long gymId, Long trainerId, Long reservationId) {
        // 사용자 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 트레이너 조회
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);
        // 예약 조회
        Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);

        // 본인의 예약인지 확인
        if (!Objects.equals(reservation.getUser().getId(), userId)) {
            throw new IllegalStateException("본인의 예약만 취소할 수 있습니다.");
        }

        // PENDING 또는 CONFIRMED 상태만 취소 가능
        if (!reservation.getReservationStatus().equals(ReservationStatus.PENDING) &&
            !reservation.getReservationStatus().equals(ReservationStatus.CONFIRMED)) {
            throw new IllegalStateException("대기 중이거나 확정된 예약만 취소할 수 있습니다.");
        }

        // 취소 기한 확인 (예: 2일 전까지만 취소 가능)
        LocalDate today = LocalDate.now();
        LocalDate reservationDate = reservation.getReservationDate();

        if (ChronoUnit.DAYS.between(today, reservationDate) < 2) {
            throw new IllegalStateException("예약 2일 전까지만 취소가 가능합니다.");
        }

        // 포인트 환불
        PointUseRefundRequestDto pointRefundRequestDto = new PointUseRefundRequestDto();
        pointRefundRequestDto.setAmount(trainer.getPrice());
        pointRefundRequestDto.setDescription("PT 예약 취소 환불 - " + trainer.getName());
        
        pointService.refundPoint(userId, pointRefundRequestDto);

        // 예약 상태를 취소로 변경
        reservation.cancelReservation();
    }

    // 트레이너별 예약 목록 조회
    @Transactional(readOnly = true)
    public List<TrainerReservationResponseDto> getTrainerReservation(Long gymId, Long trainerId) {
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 트레이너 조회
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);
        // 트레이너의 모든 예약 조회
        List<Reservation> reservations = reservationRepository.findByTrainerOrderByReservationDateDescReservationTimeDesc(trainer);

        return reservations.stream()
            .map(TrainerReservationResponseDto::from)
            .collect(Collectors.toList());
    }

    // 사용자별 예약 목록 조회
    @Transactional(readOnly = true)
    public List<UserReservationResponseDto> getUserReservations(Long userId) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        List<Reservation> reservations = reservationRepository.findByUserOrderByReservationDateDescReservationTimeDesc(user);

        return reservations.stream()
            .map(UserReservationResponseDto::from)
            .collect(Collectors.toList());
    }

    // 예약 단건 조회
    @Transactional(readOnly = true)
    public GetReservationResponseDto getReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);
        return GetReservationResponseDto.from(reservation);
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
