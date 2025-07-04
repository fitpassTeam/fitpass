package org.example.fitpass.domain.membership.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.domain.gym.entity.Gym;
import org.example.fitpass.domain.user.entity.User;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "membership_purchase")
public class MembershipPurchase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membership_id", nullable = false)
    private Membership membership;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id", nullable = false)
    private Gym gym;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    // 예약 활성화 날짜 추가
    @Column(name = "scheduled_start_date")
    private LocalDateTime scheduledStartDate;

    public MembershipPurchase(Membership membership, Gym gym, User user, LocalDateTime now) {
        this.membership = membership;
        this.gym = gym;
        this.user = user;
        this.purchaseDate = now;
    }

    public MembershipPurchase(Membership membership, Gym gym, User user, LocalDateTime purchaseTime, LocalDateTime scheduledStartTime) {
        this.membership = membership;
        this.gym = gym;
        this.user = user;
        this.purchaseDate = purchaseTime;
        this.scheduledStartDate = scheduledStartTime;
    }

    public void activate(LocalDateTime now){
        this.startDate = now;
        this.endDate = now.plusDays(membership.getDurationInDays());
    }

    public boolean isActive() {
        return startDate != null && endDate != null
            && LocalDateTime.now().isAfter(startDate)
            && LocalDateTime.now().isBefore(endDate);
    }

    public boolean isNotStarted(){
        return startDate == null;
    }
}
