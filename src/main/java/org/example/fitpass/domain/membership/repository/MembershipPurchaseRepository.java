package org.example.fitpass.domain.membership.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.fitpass.domain.membership.entity.MembershipPurchase;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MembershipPurchaseRepository extends JpaRepository<MembershipPurchase, Long>{

    List<MembershipPurchase> findAllByUser(User user);

    @Query("SELECT m FROM MembershipPurchase m WHERE m.user = :user AND :now BETWEEN m.startDate AND m.endDate")
    Optional<MembershipPurchase> findActiveByUser(@Param("user") User user, @Param("now") LocalDateTime now);


}
