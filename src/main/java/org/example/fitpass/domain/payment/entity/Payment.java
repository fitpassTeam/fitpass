package org.example.fitpass.domain.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.domain.payment.enums.PaymentStatus;
import org.example.fitpass.domain.user.entity.User;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String orderId;
    
    @Column(unique = true)
    private String paymentKey;
    
    @Column(nullable = false)
    private String orderName;
    
    @Column(nullable = false)
    private Integer amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    private String method;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "toss_order_id")
    private String tossOrderId;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    public void updatePaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }
    
    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public void updateMethod(String method) {
        this.method = method;
    }
    
    public void updateFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
