package org.example.fitpass.domain.membership.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.domain.membership.entity.MembershipPurchase;
import org.example.fitpass.domain.membership.repository.MembershipPurchaseRepository;
import org.example.fitpass.domain.notify.NotificationType;
import org.example.fitpass.domain.notify.service.NotifyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipScheduler {

    private final MembershipPurchaseRepository membershipPurchaseRepository;
    private final NotifyService notifyService;

    // 매일 오전 9시 실행 - 멤버십 만료 3일 전 알림
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendThreeDaysBeforeExpirationNotice() {
        LocalDateTime threeDaysLater = LocalDateTime.now().plusDays(3);
        LocalDateTime startOfDay = threeDaysLater.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = threeDaysLater.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        List<MembershipPurchase> expiringMemberships = membershipPurchaseRepository
            .findActiveMembershipsExpiringBetween(startOfDay, endOfDay);

        expiringMemberships.forEach(purchase -> {
            String content = purchase.getMembership().getName() + " 이용권이 3일 후 만료됩니다!";
            String url = "/memberships/purchases/me";

            notifyService.send(purchase.getUser(), NotificationType.MEMBERSHIP, content, url);
        });
    }

    // 매일 오전 9시 실행 - 멤버십 만료 1일 전 알림
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendOneDayBeforeExpirationNotice() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime startOfDay = tomorrow.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = tomorrow.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        List<MembershipPurchase> expiringMemberships = membershipPurchaseRepository
            .findActiveMembershipsExpiringBetween(startOfDay, endOfDay);

        expiringMemberships.forEach(purchase -> {
            String content = "내일 " + purchase.getMembership().getName() + " 이용권이 만료됩니다";
            String url = "/gyms/" + purchase.getGym().getId() + "/memberships";

            notifyService.send(purchase.getUser(), NotificationType.MEMBERSHIP, content, url);
        });
    }

    // 매일 오전 9시 실행 - 오늘 만료되는 멤버십 알림
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendTodayExpirationNotice() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfDay = today.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = today.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        List<MembershipPurchase> expiringToday = membershipPurchaseRepository
            .findActiveMembershipsExpiringBetween(startOfDay, endOfDay);

        expiringToday.forEach(purchase -> {
            String content = "오늘 " + purchase.getMembership().getName() + " 이용권이 만료됩니다!";
            String url = "/memberships/purchases/me";

            notifyService.send(purchase.getUser(), NotificationType.MEMBERSHIP, content, url);
        });
    }

    // 매일 오전 6시 실행 - 오늘 예약된 이용권 자동 활성화
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void activateScheduledMemberships() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        List<MembershipPurchase> scheduledMemberships = membershipPurchaseRepository
            .findScheduledForActivation(startOfDay, endOfDay);

        log.info("오늘 자동 활성화될 이용권 {}개를 처리합니다.", scheduledMemberships.size());

        scheduledMemberships.forEach(purchase -> {
            // 자동 활성화
            purchase.activate(LocalDateTime.now());

            // 활성화 알림 발송
            String content = purchase.getMembership().getName() + " 이용권이 활성화되었습니다!";
            String url = "/memberships/purchases/active";
            notifyService.send(purchase.getUser(), NotificationType.MEMBERSHIP, content, url);

            log.info("사용자 ID: {}, 멤버십: {} 자동 활성화 완료",
                purchase.getUser().getId(), purchase.getMembership().getName());
        });
    }

}
