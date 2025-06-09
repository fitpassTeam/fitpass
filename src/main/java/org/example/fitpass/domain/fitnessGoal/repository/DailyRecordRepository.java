package org.example.fitpass.domain.fitnessGoal.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.fitnessGoal.entity.DailyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyRecordRepository extends JpaRepository<DailyRecord, Long> {

    boolean existsByFitnessGoalIdAndRecordDate(Long fitnessGoalId, LocalDate recordDate);

    List<DailyRecord> findByFitnessGoalIdOrderByRecordDateDesc(Long fitnessGoalId);

    Optional<DailyRecord> findById (Long recordId);

    default DailyRecord findByIdOrElseThrow (Long recordId) {
        DailyRecord dailyRecord = findById(recordId)
            .orElseThrow(() -> new BaseException(ExceptionCode.DAILY_RECORD_NOT_FOUND));
        return dailyRecord;
    }
}
