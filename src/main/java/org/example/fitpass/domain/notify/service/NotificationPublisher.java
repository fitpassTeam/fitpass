package org.example.fitpass.domain.notify.service;


import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.notify.NotificationType;
import org.example.fitpass.domain.notify.dto.request.NotificationEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String NOTIFICATION_CHANNEL = "notification:";

    // User에게 알림
    public void publishNotificationToUser(Long userId, NotificationType type, String content, String url) {
        NotificationEvent event = new NotificationEvent(
                userId, "USER", type, content, url, LocalDateTime.now()
        );

        String channel = NOTIFICATION_CHANNEL + "USER:" + userId;
        redisTemplate.convertAndSend(channel, event);
    }

    // Trainer에게 알림
    public void publishNotificationToTrainer(Long trainerId, NotificationType type, String content, String url) {
        NotificationEvent event = new NotificationEvent(
                trainerId, "TRAINER", type, content, url, LocalDateTime.now()
        );

        String channel = NOTIFICATION_CHANNEL + "TRAINER:" + trainerId;
        redisTemplate.convertAndSend(channel, event);
    }
}