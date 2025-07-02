package org.example.fitpass.domain.notify.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.fitpass.domain.notify.NotificationType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private Long receiverId;
    private String receiverType; // "USER" 또는 "TRAINER"
    private NotificationType notificationType;
    private String content;
    private String url;
    private LocalDateTime createdAt;
}