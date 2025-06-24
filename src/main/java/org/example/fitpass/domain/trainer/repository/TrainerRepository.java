package org.example.fitpass.domain.trainer.repository;

import static org.example.fitpass.common.error.ExceptionCode.TRAINER_NOT_FOUND;

import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    default Trainer findByIdOrElseThrow(Long trainerId) {
        return findById(trainerId).orElseThrow(() -> new BaseException(TRAINER_NOT_FOUND));
    }

    Page<Trainer> findAllByGym(Gym gym, Pageable pageable);

    Page<Trainer> findByNameContaining(@Param("keyword") String keyword, Pageable pageable);
}
