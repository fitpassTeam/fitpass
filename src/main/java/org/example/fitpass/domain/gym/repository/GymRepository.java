package org.example.fitpass.domain.gym.repository;

import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.entity.Gym;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymRepository extends JpaRepository<Gym, Long> {

    Optional<Gym> findById(Long gymId);

    default Gym findByIdOrElseThrow(Long gymId) {
        Gym gym = findById(gymId).orElseThrow(
                () -> new BaseException(ExceptionCode.GYM_NOT_FOUND));
        return gym;
    }

    Optional<Gym> findByIdAndIsDeletedFalse(Long gymId);

    Page<Gym> findAllByIsDeletedFalse(Pageable pageable);

}
