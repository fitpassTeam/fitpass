package org.example.fitpass.domain.trainer.repository;

import org.example.fitpass.domain.trainer.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

}
