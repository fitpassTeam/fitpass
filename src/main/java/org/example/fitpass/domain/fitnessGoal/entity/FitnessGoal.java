package org.example.fitpass.domain.fitnessGoal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.fitnessGoal.enums.GoalStatus;
import org.example.fitpass.domain.fitnessGoal.enums.GoalType;
import org.example.fitpass.domain.user.entity.User;

@Entity
@Getter
@Table(name = "fitness_goals")
@NoArgsConstructor
public class FitnessGoal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 목표 제목
    @Column(nullable = false)
    private String title;

    // 설명
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private GoalType goalType;

    @Enumerated(EnumType.STRING)
    private GoalStatus goalStatus;

    // 시작 체중
    @Column(nullable = false)
    private Double startWeight;

    // 목표 체중
    @Column(nullable = false)
    private Double targetWeight;

    // 현재 체중
    private Double currentWeight;

    // 목표 시작일
    @Column(nullable = false)
    private LocalDate startDate;

    // 목표 종료일
    @Column(nullable = false)
    private LocalDate endDate;

    // 달성 일시
    private LocalDateTime achievementDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public FitnessGoal(User user, String title, String description, GoalType goalType, double startWeight, double targetWeight, LocalDate startDate,
        LocalDate endDate) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.goalType = goalType;
        this.startWeight = startWeight;
        this.targetWeight = targetWeight;
        this.startDate = startDate;
        this.endDate = endDate;
        this.initializeStatus(); // 상태 초기화
    }

    public static FitnessGoal of(User user, String title, String description, GoalType goalType,
        double startWeight, double targetWeight, LocalDate startDate, LocalDate endDate) {
        return new FitnessGoal(user, title, description, goalType, startWeight, targetWeight, startDate, endDate);
    }

    public void updateCurrentWeight(Double newWeight) {
        this.currentWeight = newWeight;
        if (checkGoalAchievement()) {
            this.goalStatus = GoalStatus.COMPLETED;
            this.achievementDate = LocalDateTime.now();
        }
    }

    public void updateGoal(String title, String description, double targetWeight, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.targetWeight = targetWeight;
        this.endDate = endDate;
    }

    private boolean checkGoalAchievement() {
        if (currentWeight == null) return false;

        if (goalType == GoalType.WEIGHT_LOSS) {
            return currentWeight <= targetWeight;
        } else if (goalType == GoalType.WEIGHT_GAIN) {
            return currentWeight >= targetWeight;
        } else if (goalType == GoalType.WEIGHT_MAINTAIN) {
            // 목표 체중 ±1kg 범위 내면 달성
            return Math.abs(currentWeight - targetWeight) <= 1.0;
        }
        return false;
    }

    // 목표 만료 체크
    public void checkAndUpdateExpiredStatus() {
        if (goalStatus == GoalStatus.ACTIVE && LocalDate.now().isAfter(endDate)) {
            this.goalStatus = GoalStatus.EXPIRED;
        }
    }

    // 목표 상태 초기화 (생성 시)
    public void initializeStatus() {
        if (LocalDate.now().isAfter(endDate)) {
            this.goalStatus = GoalStatus.EXPIRED;
        } else {
            this.goalStatus = GoalStatus.ACTIVE;
        }
    }

    // 목표 취소 (상태를 CANCELLED로 변경)
    public void cancelGoal() {
        if (goalStatus == GoalStatus.COMPLETED) {
            throw new BaseException(ExceptionCode.FITNESS_GOAL_CANCEL_NOT_ALLOWED);
        }
        if (goalStatus == GoalStatus.CANCELLED) {
            throw new BaseException(ExceptionCode.FITNESS_GOAL_ALREADY_CANCELLED);
        }
        this.goalStatus = GoalStatus.CANCELLED;
    }

    // 목표 달성률 계산
    public double calculateProgressRate() {
        if (startWeight == null || targetWeight == null || currentWeight == null) {
            return 0.0;
        }

        // 목표 체중과 시작 체중이 같으면 100% (이미 달성)
        if (startWeight.equals(targetWeight)) {
            return 100.0;
        }

        double totalWeightDiff = Math.abs(targetWeight - startWeight);
        double currentWeightDiff = Math.abs(startWeight - currentWeight);

        // 감량 목표인지 증량 목표인지 확인
        if (goalType == GoalType.WEIGHT_LOSS) {
            // 감량: 현재가 목표보다 적게 나가야 함
            if (currentWeight <= targetWeight) {
                return 100.0; // 목표 달성
            }
            return Math.min(currentWeightDiff / totalWeightDiff * 100, 100.0);

        } else if (goalType == GoalType.WEIGHT_GAIN) {
            // 증량: 현재가 목표보다 많이 나가야 함
            if (currentWeight >= targetWeight) {
                return 100.0; // 목표 달성
            }
            return Math.min(currentWeightDiff / totalWeightDiff * 100, 100.0);

        } else {
            // 유지: 목표 체중 ±1kg 범위 내면 100%
            double diff = Math.abs(currentWeight - targetWeight);
            if (diff <= 1.0) {
                return 100.0;
            }
            return Math.max(0, 100 - (diff * 10)); // 1kg당 10%씩 감점
        }
    }





}
