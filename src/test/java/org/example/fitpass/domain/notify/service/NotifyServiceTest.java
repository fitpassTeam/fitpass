package org.example.fitpass.domain.notify.service;

import org.example.fitpass.domain.notify.NotificationType;
import org.example.fitpass.domain.notify.entity.Notify;
import org.example.fitpass.domain.notify.repository.EmitterRepository;
import org.example.fitpass.domain.notify.repository.NotifyRepository;
import org.example.fitpass.domain.user.entity.User;
import org.example.fitpass.common.config.RedisDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotifyServiceTest {
    @Mock
    private EmitterRepository emitterRepository;
    @Mock
    private NotifyRepository notifyRepository;
    @Mock
    private RedisDao redisDao;

    @InjectMocks
    private NotifyService notifyService;

    private User user;
    private Notify mockNotify;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("test@email.com", "testUser", "LOCAL");
        
        // 리플렉션으로 id 세팅 (실제 DB 사용 안하므로)
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Mock Notify 객체 생성 및 설정
        mockNotify = mock(Notify.class);
        when(mockNotify.getId()).thenReturn(1L);
        when(mockNotify.getReceiver()).thenReturn(user);
        when(mockNotify.getContent()).thenReturn("메시지");
        when(mockNotify.getNotificationType()).thenReturn(NotificationType.CHAT);
        when(mockNotify.getCreatedAt()).thenReturn(LocalDateTime.now());
    }

    @Test
    void send_알림_연결없으면_redis저장() {
        // given
        when(emitterRepository.findAllEmittersById(anyLong())).thenReturn(Collections.emptyMap());
        when(notifyRepository.save(any(Notify.class))).thenReturn(mockNotify);

        // when
        notifyService.send(user, NotificationType.CHAT, "메시지", "/chat/1");

        // then
        verify(redisDao, times(1)).saveNotifyToRedis(eq(1L), any(Notify.class));
        verify(emitterRepository, times(1)).findAllEmittersById(1L);
        verify(notifyRepository, times(1)).save(any(Notify.class));
    }

    @Test
    void send_알림_연결있으면_emitter로_전송() {
        // given
        SseEmitter emitter = mock(SseEmitter.class);
        Map<String, SseEmitter> emitters = new HashMap<>();
        emitters.put("1:123456", emitter);
        when(emitterRepository.findAllEmittersById(anyLong())).thenReturn(emitters);
        when(notifyRepository.save(any(Notify.class))).thenReturn(mockNotify);

        // when
        notifyService.send(user, NotificationType.CHAT, "메시지", "/chat/1");

        // then
        verify(emitterRepository, times(1)).findAllEmittersById(1L);
        verify(notifyRepository, times(1)).save(any(Notify.class));
        verify(redisDao, never()).saveNotifyToRedis(anyLong(), any(Notify.class));
        // emitter.send는 내부적으로 예외처리되어 호출여부만 검증 불가
    }
}
