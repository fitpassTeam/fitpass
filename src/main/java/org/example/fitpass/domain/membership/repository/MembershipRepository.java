package org.example.fitpass.domain.membership.repository;

import java.util.List;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.membership.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository <Membership, Long> {
    List<Membership> findAllByGym(Gym gym);

    Membership findByIdOrElseThrow(Long id);
}
