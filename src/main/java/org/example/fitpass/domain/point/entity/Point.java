package org.example.fitpass.domain.point.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.domain.point.enums.PointStatus;
import org.example.fitpass.domain.point.enums.PointType;
import org.example.fitpass.domain.user.entity.User;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "points")
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int balance; // 거래 후 잔액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointType pointType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointStatus pointStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 포인트 생성 생성자
    public Point(User user, int amount, String description, int balance, PointType pointType) {
        this.user = user;
        this.amount = amount;
        this.description = description;
        this.balance = balance;
        this.pointType = pointType;
        this.pointStatus = pointStatus.COMPLETED;
    }

    // 포인트 상태 변경
    public void updateStatus(PointStatus pointStatus) {
        this.pointStatus = pointStatus;
    }
}
