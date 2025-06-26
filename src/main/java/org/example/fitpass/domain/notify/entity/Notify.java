package org.example.fitpass.domain.notify.entity;

import com.querydsl.core.BooleanBuilder;
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
import javax.management.Notification;
import lombok.Getter;
import org.example.fitpass.common.BaseEntity;
import org.example.fitpass.domain.notify.NotificationType;
import org.example.fitpass.domain.notify.ReceiverType;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.user.entity.User;

@Entity
@Getter
public class Notify extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    private String url;

    private Boolean isRead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    // 수신자 타입을 구분하는 필드 추가
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceiverType receiverType;

    public Notify(User receiver, NotificationType notificationType, String content, String url, Boolean isRead) {
        this.receiver = receiver;
        this.notificationType = notificationType;
        this.content = content;
        this.url = url;
        this.isRead = isRead;
        this.receiverType = ReceiverType.USER;
    }

    public Notify(Trainer trainer, NotificationType notificationType, String content, String url, Boolean isRead) {
        this.trainer = trainer;
        this.notificationType = notificationType;
        this.content = content;
        this.url = url;
        this.isRead = isRead;
        this.receiverType = ReceiverType.TRAINER;
    }

    public Notify() {
    }

    // 수신자 ID를 반환하는 헬퍼 메소드
    public Long getReceiverId() {
        return receiverType == ReceiverType.USER ?
                (receiver != null ? receiver.getId() : null) :
                (trainer != null ? trainer.getId() : null);
    }

    // 수신자 이름을 반환하는 헬퍼 메소드
    public String getReceiverName() {
        return receiverType == ReceiverType.USER ?
                (receiver != null ? receiver.getName() : null) :
                (trainer != null ? trainer.getName() : null);
    }
}
