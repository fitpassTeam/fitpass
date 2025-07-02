package org.example.fitpass.domain.fitnessGoal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.fitnessGoal.entity.WeightRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeightRecordRepository extends JpaRepository<WeightRecord, Long> {

    Optional<WeightRecord> findById(Long recordId);

    default WeightRecord findByIdOrElseThrow (Long recordId) {
        WeightRecord weightRecord = findById(recordId)
            .orElseThrow(() -> new BaseException(ExceptionCode.WEIGHT_RECORD_NOT_FOUND));
        return weightRecord;
    }

    boolean existsByFitnessGoalIdAndRecordDate(Long fitnessGoalId, LocalDate recordDate);

    List<WeightRecord> findByFitnessGoalIdOrderByRecordDateDesc(Long fitnessGoalId);

    Optional<WeightRecord> findTopByFitnessGoalIdOrderByRecordDateDesc(Long fitnessGoalId);
}
