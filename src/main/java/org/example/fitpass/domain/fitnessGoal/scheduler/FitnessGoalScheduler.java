package org.example.fitpass.domain.fitnessGoal.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.fitpass.domain.fitnessGoal.service.FitnessGoalService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FitnessGoalScheduler {

    private final FitnessGoalService fitnessGoalService;

    // 매일 자정에 만료된 목표들 상태 업데이트
    @Scheduled(cron = "0 0 0 * * *")
    public void updateExpiredGoals() {
        log.info("만료된 피트니스 목표들 상태 업데이트 시작");
        try {
            fitnessGoalService.updateExpiredGoals();
            log.info("만료된 피트니스 목표들 상태 업데이트 완료");
        } catch (Exception e) {
            log.error("만료된 피트니스 목표들 상태 업데이트 중 오류 발생", e);
        }
    }
}
