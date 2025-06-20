package org.example.fitpass.domain.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.notify.NotificationType;
import org.example.fitpass.domain.notify.service.NotifyService;
import org.example.fitpass.domain.user.UserRole;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.gym.repository.GymRepository;
import org.example.fitpass.domain.point.dto.request.PointUseRefundRequestDto;
import org.example.fitpass.domain.point.dto.response.PointBalanceResponseDto;
import org.example.fitpass.domain.point.service.PointService;
import org.example.fitpass.domain.reservation.dto.request.ReservationRequestDto;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final TrainerRepository trainerRepository;
    private final PointService pointService;
    private final RedissonClient redissonClient; // ⭐ Redis 분산 락용
    private final NotifyService notifyService;

    // 예약 가능 시간 조회
    @Transactional
    public List<LocalTime> getAvailableTimes(Long userId, Long gymId, Long trainerId,
        LocalDate date) {
        User user = userRepository.findByIdOrElseThrow(userId);
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);

        // 체육관 운영 시간 (체육관 엔티티에서 가져오기)
        List<LocalTime> possibleTimes = generateTimeSlots(
            gym.getOpenTime(),
            gym.getCloseTime(),
            60
        );
        // 해당 날짜에 이미 예약된 시간들 제외
        List<LocalTime> reservedTimes = reservationRepository.findReservedTimesByTrainerAndDate(
            trainer, date);

        // 예약 가능한 시간만 반환
        return possibleTimes.stream()
            .filter(time -> !reservedTimes.contains(time))
            .collect(Collectors.toList());

    }

    // 예약 생성
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservationResponseDto createReservation(
        LocalDate reservationDate, LocalTime reservationTime, ReservationStatus status,
        Long userId, Long gymId, Long trainerId) {
        // 사용자 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        // 트레이너 조회
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);

        // Redis 분산 락 키 생성
        String lockKey = String.format("reservation:lock:%d:%s:%s",
            trainerId, reservationDate,
            reservationTime);

            RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도 (10초 대기, 30초 후 자동 해제)
            if (!lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                throw new BaseException(ExceptionCode.RESERVATION_ALREADY_EXISTS);
            }
            // 중복 예약 확인 (Redis 락 내에서)
            boolean alreadyExists = reservationRepository.existsByTrainerAndReservationDateAndReservationTime(
                trainer, reservationDate,
                reservationTime
            );

                if (alreadyExists) {
                    throw new BaseException(ExceptionCode.RESERVATION_ALREADY_EXISTS);
                }

                // 포인트 사용
                String description = "PT 예약 - " + trainer.getName();
                PointUseRefundRequestDto pointUseRefundRequestDto = new PointUseRefundRequestDto(trainer.getPrice(), description);// 트레이너 이용료

            PointBalanceResponseDto dto = pointService.usePoint(userId, pointUseRefundRequestDto.amount(), pointUseRefundRequestDto.description());
            int newBalance = dto.balance();
            // 예약 생성 및 저장
            Reservation reservation = ReservationRequestDto.from(
                reservationDate,
                reservationTime,
                status,
                user,
                gym,
                trainer);
            Reservation createReservation = reservationRepository.save(reservation);

                String url = "/gyms/" + gymId + "/trainers/" + trainerId + "/reservations/" + createReservation.getId();

                String content = user.getName() + "님의 예약이 완료되었습니다." + "예약 날짜는 " + reservation.getReservationDate() +" "+ reservation.getReservationTime() + " 입니다. ";
                notifyService.send(user, NotificationType.RESERVATION, content, url);
                notifyService.send(trainer.getGym().getOwner(), NotificationType.RESERVATION, content, url);

                return ReservationResponseDto.from(createReservation);
            } catch (InterruptedException e) {
                // 플래그 복원 (다시 설정)
                Thread.currentThread().interrupt();
                throw new BaseException(ExceptionCode.RESERVATION_INTERRUPTED);
            } finally {
                // 안전한 락 해제
                if(lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

    // 예약 수정
    @Transactional
    public UpdateReservationResponseDto updateReservation(
        LocalDate reservationDate, LocalTime reservationTime, ReservationStatus status,
        Long userId, Long gymId, Long trainerId, Long reservationId) {
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
            throw new BaseException(ExceptionCode.NOT_RESERVATION_OWNER);
        }

        // PENDING 상태만 수정 가능
        if (!reservation.getReservationStatus().equals(ReservationStatus.PENDING)) {
            throw new BaseException(ExceptionCode.RESERVATION_STATUS_NOT_CHANGEABLE);
        }

        // 2일 전까지만 변경 가능
        LocalDate today = LocalDate.now();
        LocalDate reservationDates = reservation.getReservationDate();
        if (ChronoUnit.DAYS.between(today, reservationDates) < 2) {
            throw new BaseException(ExceptionCode.RESERVATION_CHANGE_DEADLINE_PASSED);
        }

        // 새로운 예약 날짜도 2일 후부터 가능한지 검증
        LocalDate newReservationDate = reservationDate;
        if (ChronoUnit.DAYS.between(today, newReservationDate) < 2) {
            throw new BaseException(ExceptionCode.RESERVATION_TOO_EARLY);
        }

        // 새로운 예약이 이미 예약이 있는지 확인 (중복 예약 방지)
        boolean isDuplicate = reservationRepository.existsByTrainerAndReservationDateAndReservationTimeAndIdNot(
            trainer,
            newReservationDate,
            reservationTime,
            reservationId  // 현재 예약은 제외
        );
        if (isDuplicate) {
            throw new BaseException(ExceptionCode.RESERVATION_TIME_CONFLICT);
        }
        // 예약 정보 업데이트
        reservation.updateReservation(
            reservationDate,
            reservationTime,
            status);

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
            throw new BaseException(ExceptionCode.NOT_RESERVATION_OWNER);
        }

        // PENDING 또는 CONFIRMED 상태만 취소 가능
        if (!reservation.getReservationStatus().equals(ReservationStatus.PENDING) &&
            !reservation.getReservationStatus().equals(ReservationStatus.CONFIRMED)) {
            throw new BaseException(ExceptionCode.RESERVATION_STATUS_NOT_CANCELLABLE);
        }

        // 취소 기한 확인 (예: 2일 전까지만 취소 가능)
        LocalDate today = LocalDate.now();
        LocalDate reservationDate = reservation.getReservationDate();

        if (ChronoUnit.DAYS.between(today, reservationDate) < 2) {
            throw new BaseException(ExceptionCode.RESERVATION_CANCEL_DEADLINE_PASSED);
        }

        // 포인트 환불
        String description = "PT 예약 취소 환불 - " + trainer.getName();
        PointUseRefundRequestDto pointRefundRequestDto = new PointUseRefundRequestDto(trainer.getPrice(), description);

        pointService.refundPoint(userId, pointRefundRequestDto.amount(), pointRefundRequestDto.description());

        // 예약 상태를 취소로 변경
        reservation.cancelReservation();
    }

    // 트레이너별 예약 목록 조회
    @Transactional(readOnly = true)
    public List<TrainerReservationResponseDto> getTrainerReservation(Long userId, Long gymId,
        Long trainerId) {
        // 사용자 조회
        User user = userRepository.findByIdOrElseThrow(userId);

        // OWNER 권한 확인
        if (!user.getUserRole().equals(UserRole.OWNER)) {
            throw new BaseException(ExceptionCode.NO_OWNER_AUTHORITY);
        }

        // 체육관 조회
        Gym gym = gymRepository.findByIdOrElseThrow(gymId);
        gym.isOwner(userId);
        // 트레이너 조회
        Trainer trainer = trainerRepository.findByIdOrElseThrow(trainerId);
        // 트레이너가 해당 체육관 소속인지 확인
        if (!trainer.getGym().getId().equals(gymId)) {
            throw new BaseException(ExceptionCode.NOT_GYM_OWNER);
        }

        // 트레이너의 모든 예약 조회
        List<Reservation> reservations = reservationRepository.findByTrainerOrderByReservationDateDescReservationTimeDesc(
            trainer);

        return reservations.stream()
            .map(TrainerReservationResponseDto::from)
            .collect(Collectors.toList());
    }

    // 사용자별 예약 목록 조회
    @Transactional(readOnly = true)
    public List<UserReservationResponseDto> getUserReservations(Long userId) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        List<Reservation> reservations = reservationRepository.findByUserOrderByReservationDateDescReservationTimeDesc(
            user);

        return reservations.stream()
            .map(UserReservationResponseDto::from)
            .collect(Collectors.toList());
    }

    // 예약 단건 조회
    @Transactional(readOnly = true)
    public GetReservationResponseDto getReservation(Long userId, Long reservationId) {
        // 유저 조회
        User user = userRepository.findByIdOrElseThrow(userId);
        Reservation reservation = reservationRepository.findByIdOrElseThrow(reservationId);

        // 본인의 예약인지 확인
        if (!Objects.equals(reservation.getUser().getId(), userId)) {
            throw new BaseException(ExceptionCode.NOT_RESERVATION_OWNER);
        }

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
