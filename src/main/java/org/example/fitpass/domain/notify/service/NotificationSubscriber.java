package org.example.fitpass.domain.notify.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.example.fitpass.domain.notify.dto.NotificationEvent;
import org.example.fitpass.domain.notify.dto.NotifyDto;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.notify.repository.EmitterRepository;
import org.example.fitpass.domain.notify.repository.NotifyRepository;
import org.example.fitpass.domain.trainer.entity.Trainer;
import org.example.fitpass.domain.trainer.repository.TrainerRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.domain.user.repository.UserRepository;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationSubscriber implements MessageListener {

    private final EmitterRepository emitterRepository;
    private final NotifyRepository notifyRepository;
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            NotificationEvent event = mapper.readValue(message.getBody(), NotificationEvent.class);

            // DB에 저장
            Notify notification = createAndSaveNotification(event);

            // SSE로 실시간 전송
            sendToSseEmitters(event.getReceiverId(), event.getReceiverType(), notification);

        } catch (Exception e) {
            // 에러 처리
            e.printStackTrace();
        }
    }

    private Notify createAndSaveNotification(NotificationEvent event) {
        Notify notification;

        if ("USER".equals(event.getReceiverType())) {
            User user = userRepository.findById(event.getReceiverId()).orElseThrow();
            notification = new Notify(user, event.getNotificationType(),
                    event.getContent(), event.getUrl(), false);
        } else {
            Trainer trainer = trainerRepository.findById(event.getReceiverId()).orElseThrow();
            notification = new Notify(trainer, event.getNotificationType(),
                    event.getContent(), event.getUrl(), false);
        }

        return notifyRepository.save(notification);
    }

    private void sendToSseEmitters(Long receiverId, String receiverType, Notify notification) {
        // 현재 Notify 엔티티의 헬퍼 메소드 활용
        Long actualReceiverId = notification.getReceiverId();

        Map<String, SseEmitter> emitters = emitterRepository.findAllEmittersById(actualReceiverId);

        emitters.forEach((key, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(key)
                        .name("notification")
                        .data(NotifyDto.Response.createResponse(notification)));
            } catch (IOException e) {
                emitterRepository.deleteById(key);
            }
        });
    }
}