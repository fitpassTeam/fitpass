package org.example.fitpass.domain.gym.repository;

import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.common.error.ExceptionCode;
import org.example.fitpass.domain.gym.entity.Gym;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GymRepository extends JpaRepository<Gym, Long> {

    Optional<Gym> findById(Long gymId);

    default Gym findByIdOrElseThrow(Long gymId) {
        Gym gym = findById(gymId).orElseThrow(
                () -> new BaseException(ExceptionCode.GYM_NOT_FOUND));
        return gym;
    }

//    Optional<Gym> findByIdAndIsDeletedFalse(Long gymId);
//
//    default Gym findByIdAndIsDeletedFalseOrElseThrow(Long gymId){
//        return findByIdAndIsDeletedFalse(gymId)
//            .orElseThrow(() -> new BaseException(ExceptionCode.GYM_NOT_FOUND));
//    }

    Page<Gym> findAll(Pageable pageable);

//    @Query("SELECT g FROM Gym g WHERE g.name LIKE %:keyword% AND g.deletedAt IS NULL")
//    Page<Gym> findByGymNameContaining(@Param("keyword") String keyword, Pageable pageable);

    Page<Gym> findByNameContaining(@Param("keyword") String keyword, Pageable pageable);
}
