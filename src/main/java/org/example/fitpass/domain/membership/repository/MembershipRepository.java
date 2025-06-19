package org.example.fitpass.domain.membership.repository;

import static org.example.fitpass.common.error.ExceptionCode.MEMBERSHIP_NOT_FOUND;

import java.util.List;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.membership.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository <Membership, Long> {
    List<Membership> findAllByGym(Gym gym);

    default Membership findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(() -> new BaseException(MEMBERSHIP_NOT_FOUND));
    }
}
