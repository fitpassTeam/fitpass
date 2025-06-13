package org.example.fitpass.domain.trainer.repository;

import static org.example.fitpass.common.error.ExceptionCode.CANT_FIND_DATA;

import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    default Trainer findByIdOrElseThrow(Long trainerId) {
        return findById(trainerId).orElseThrow(() -> new BaseException(CANT_FIND_DATA));
    }

    Page<Trainer> findAllByGym(Gym gym, Pageable pageable);

}
