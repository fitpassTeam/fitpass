
package org.example.fitpass.config;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.notify.service.NotificationSubscriber;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class NotificationConfig implements ApplicationListener<ApplicationReadyEvent> {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final NotificationSubscriber notificationSubscriber;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 애플리케이션이 완전히 준비된 후에 구독 설정
        redisMessageListenerContainer.addMessageListener(notificationSubscriber,
                new PatternTopic("notification:*"));
    }
}