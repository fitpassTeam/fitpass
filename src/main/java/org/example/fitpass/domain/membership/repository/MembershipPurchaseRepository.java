package org.example.fitpass.domain.membership.repository;

import static org.example.fitpass.common.error.ExceptionCode.NOT_FOUND_PURCHASE;

import java.time.LocalDateTime;
import java.util.List;
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

    @Query("""
    SELECT mp FROM MembershipPurchase mp
    WHERE mp.startDate IS NOT NULL 
      AND mp.endDate BETWEEN :startDate AND :endDate
    """)
    List<MembershipPurchase> findActiveMembershipsExpiringBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("""
    SELECT mp FROM MembershipPurchase mp
    WHERE mp.scheduledStartDate BETWEEN :startOfDay AND :endOfDay
      AND mp.startDate IS NULL
    """)
    List<MembershipPurchase> findScheduledForActivation(
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay);
}
