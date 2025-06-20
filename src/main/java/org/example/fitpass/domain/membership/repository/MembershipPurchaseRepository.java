package org.example.fitpass.domain.membership.repository;

import static org.example.fitpass.common.error.ExceptionCode.NOT_FOUND_PURCHASE;
import static org.example.fitpass.common.error.ExceptionCode.TRAINER_NOT_FOUND;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.fitpass.common.error.BaseException;
import org.example.fitpass.domain.membership.entity.MembershipPurchase;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MembershipPurchaseRepository extends JpaRepository<MembershipPurchase, Long>{

    List<MembershipPurchase> findAllByUser(User user);

    default MembershipPurchase findByIdOrElseThrow(Long purchaseId) {
        return findById(purchaseId).orElseThrow(() -> new BaseException(NOT_FOUND_PURCHASE));
    }

    @Query("""
    SELECT m FROM MembershipPurchase m
    WHERE m.user = :user
      AND :now BETWEEN m.startDate AND m.endDate
    ORDER BY m.startDate DESC
    """)
    List<MembershipPurchase> findAllActiveByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    @Query("""
    SELECT m FROM MembershipPurchase m
    WHERE m.user = :user
      AND m.startDate IS NULL
    ORDER BY m.purchaseDate DESC
    """)
    List<MembershipPurchase> findAllNotStartedByUser(@Param("user") User user);

}
