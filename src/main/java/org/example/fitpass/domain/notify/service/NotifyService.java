package org.example.fitpass.domain.notify.service;

import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.config.RedisDao;
import org.example.fitpass.domain.notify.NotificationType;
import org.example.fitpass.domain.notify.dto.NotifyDto;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.notify.repository.EmitterRepository;
import org.example.fitpass.domain.notify.repository.NotifyRepository;
import org.example.fitpass.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Service
public class NotifyService {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final EmitterRepository emitterRepository;
    private final NotifyRepository notifyRepository;
    private final RedisDao redisDao;

    @Transactional(readOnly = true)
    public SseEmitter subscribe(Long id, String lastEventId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        // 한 곳에서 key 생성
        String key = id + ":" + System.currentTimeMillis();

        // 해당 key 를 그대로 저장
        emitterRepository.save(id, key, emitter);
        emitter.onCompletion(() -> emitterRepository.deleteById(key));
        emitter.onTimeout(() -> emitterRepository.deleteById(key));

        sendNotification(emitter, key, "Event Stream Established.");

        if (hasLostData(lastEventId)) {
            sendLostData(id, lastEventId, key, emitter);
        }
        return emitter;
    }

    private void sendLostData(Long id, String lastEventId, String key, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheById(id);
        eventCaches.entrySet().stream()
            .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
            .forEach(entry -> sendNotification(emitter, entry.getKey(), entry.getValue()));
    }


    @Transactional
    public void send(User receiver, NotificationType notificationType, String content, String url) {
        Notify notification = notifyRepository.save(createNotification(receiver, notificationType, content, url));

        Long receiverId = receiver.getId();
        // 한 유저의 연결들 가져온 후 하나 하나 메시지 전달
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmittersById(receiverId);

        if (emitters.isEmpty()) {
            redisDao.saveNotifyToRedis(receiverId, notification); // 변경된 부분
            return;
        }

        emitters.forEach((key, emitter) -> {
            sendNotification(emitter, key, NotifyDto.Response.createResponse(notification));
        });
    }

    private void sendNotification(SseEmitter emitter, String eventId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                .id(eventId)
                .name("sse")
                .data(data));
        } catch (IOException e) {
            // 만료시 해당 연결 삭제
        }
    }

    private boolean hasLostData(String lastEventId) {
        return lastEventId != null && !lastEventId.isEmpty();
    }

    private Notify createNotification(User receiver, NotificationType notificationType, String content, String url) {
        return new Notify(receiver, notificationType, content, url, false);
    }
}