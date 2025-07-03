package org.example.fitpass.domain.fitnessGoal.repository;

import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.fitnessGoal.entity.FitnessGoal;
import org.example.fitpass.domain.fitnessGoal.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FitnessGoalRepository extends JpaRepository<FitnessGoal, Long> {

    // 특정 사용자의 목표들 조회
    List<FitnessGoal> findByUserIdOrderByCreatedAtDesc (Long userId);

    // 특정 상태의 목표들 조회
    List<FitnessGoal> findByGoalStatus(GoalStatus goalStatus);

    Optional<FitnessGoal> findByIdAndUserId(Long goalId, Long userId);

    default FitnessGoal findByIdAndUserIdOrElseThrow (Long fitnessGoalId, Long userId) {
        FitnessGoal fitnessGoal = findByIdAndUserId(fitnessGoalId, userId)
            .orElseThrow(() -> new BaseException(ExceptionCode.FITNESS_GOAL_NOT_FOUND));
        return fitnessGoal;
    }

}
