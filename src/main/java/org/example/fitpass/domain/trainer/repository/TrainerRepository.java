package org.example.fitpass.domain.trainer.repository;

import static org.example.fitpass.common.error.ExceptionCode.CANT_FIND_DATA;

import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    default Trainer getByIdOrThrow(Long trainerId) {
        return findById(trainerId).orElseThrow(() -> new BaseException(CANT_FIND_DATA));
    }

}
