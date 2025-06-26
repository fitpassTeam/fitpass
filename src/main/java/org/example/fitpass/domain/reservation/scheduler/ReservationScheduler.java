package org.example.fitpass.domain.reservation.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.domain.notify.NotificationType;
import org.example.fitpass.domain.notify.service.NotifyService;
import org.example.fitpass.domain.reservation.entity.Reservation;
import org.example.fitpass.domain.reservation.enums.ReservationStatus;
import org.example.fitpass.domain.reservation.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;
    private final NotifyService notifyService;

    // 매 시간마다 실행 (0분에 실행)
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void completeExpiredReservations() {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // 현재 시간이 지난 CONFIRMED 상태의 예약들 조회
        List<Reservation> expiredReservations = reservationRepository
            .findExpiredConfirmedReservations(today, currentTime);

        log.info("만료된 예약 {}개를 COMPLETED로 변경합니다.", expiredReservations.size());

        // 상태를 COMPLETED로 변경
        expiredReservations.forEach(reservation -> {
            reservation.updateReservation(
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                ReservationStatus.COMPLETED
            );
            log.info("예약 ID {} 완료 처리됨", reservation.getId());
        });

        reservationRepository.saveAll(expiredReservations);
    }

    // PENDING 상태로 24시간 이상 있는 예약 자동 취소
    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시에 실행
    @Transactional
    public void cancelLongPendingReservations() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<Reservation> longPendingReservations = reservationRepository
            .findLongPendingReservations(yesterday);

        log.info("24시간 이상 대기 중인 예약 {}개를 취소합니다.", longPendingReservations.size());

        longPendingReservations.forEach(reservation -> {
            reservation.cancelReservation();
            log.info("예약 ID {} 자동 취소됨", reservation.getId());
        });

        reservationRepository.saveAll(longPendingReservations);
    }
    // // 매일 오전 9시에 내일 예약 발송
    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
    public void sendTomorrowReservationReminder() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Reservation> tomorrowReservations = reservationRepository
            .findByReservationDateAndReservationStatus(tomorrow, ReservationStatus.CONFIRMED);

        tomorrowReservations.forEach(reservation -> {
            String content = "내일 " + reservation.getReservationTime() + " PT 예약이 있습니다!";
            String url = "/reservations/" + reservation.getId();

            // 사용자에게
            notifyService.send(reservation.getUser(), NotificationType.RESERVATION, content, url);
            // 사장에게
            notifyService.send(reservation.getGym().getOwner(), NotificationType.RESERVATION, content, url);
        });
    }

    // 매시간 정각에 실행하여 2시간 후 예약 확인
    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    public void sendTwoHourBeforeReminder() {
        LocalDateTime twoHoursLater = LocalDateTime.now().plusHours(2);
        LocalDate targetDate = twoHoursLater.toLocalDate();
        LocalTime targetTime = twoHoursLater.toLocalTime();

        // 정확히 2시간 후에 시작하는 예약들 조회
        List<Reservation> upcomingReservations = reservationRepository
            .findByReservationDateAndReservationTimeAndReservationStatus(
                targetDate, targetTime, ReservationStatus.CONFIRMED);

        log.info("2시간 후({} {}) 시작하는 예약 {}건에 리마인더를 발송합니다.",
            targetDate, targetTime, upcomingReservations.size());

        upcomingReservations.forEach(reservation -> {
            String content = "2시간 후 PT 수업이 시작됩니다. 준비해주세요!";
            notifyService.send(reservation.getUser(), NotificationType.RESERVATION, content, "/reservations/" + reservation.getId());
            notifyService.send(reservation.getGym().getOwner(), NotificationType.RESERVATION, content, "/reservations/" + reservation.getId());
        });
    }
}