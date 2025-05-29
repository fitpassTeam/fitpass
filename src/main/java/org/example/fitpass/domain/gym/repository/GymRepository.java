package org.example.fitpass.domain.gym.repository;

import java.util.Optional;
import org.example.fitpass.domain.gym.entity.Gym;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymRepository extends JpaRepository<Gym, Long> {

    Optional<Gym> findById(Long gymId);

    default Gym findByIdOrElseThrow(Long gymId) {
        Gym gym = findById(gymId).orElseThrow(
                () -> new RuntimeException("존재하지 않은 체육관 id입니다."));
        return gym;
    }

    Optional<Gym> findByIdAndIsDeletedFalse(Long gymId);

    Page<Gym> findAllAndIsDeletedFalse(Pageable pageable);

}
