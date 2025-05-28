package org.example.fitpass.domain.trainer.repository;

import java.util.Optional;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findById(Long trainerId);

    default Trainer findByIdOrElseThrow(Long trainerId) {
        Trainer trainer = findById(trainerId).orElseThrow(
            () -> new RuntimeException("존재하지 않는 트레이너 id입니다.")
        );
        return trainer;
    }

}
