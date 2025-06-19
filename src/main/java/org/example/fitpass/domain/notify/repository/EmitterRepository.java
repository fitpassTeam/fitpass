package org.example.fitpass.domain.notify.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class EmitterRepository {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

    public SseEmitter save(Long id, String key, SseEmitter sseEmitter) {
        emitters.put(key, sseEmitter);
        return sseEmitter;
    }

    // 변경: 이벤트 저장시에도 key 값을 넘깁니다.
    public void saveEventCache(Long id, String key, Object event) {
        eventCache.put(key, event);
    }

    public Map<String, SseEmitter> findAllEmittersById(Long id) {
        String keyPrefix = id + ":";
        return emitters.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(keyPrefix))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, Object> findAllEventCacheById(Long id) {
        String keyPrefix = id + ":";
        return eventCache.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(keyPrefix))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void deleteAllEmittersById(Long id) {
        String keyPrefix = id + ":";
        emitters.keySet().removeIf(key -> key.startsWith(keyPrefix));
    }

    public void deleteAllEventCacheById(Long id) {
        String keyPrefix = id + ":";
        eventCache.keySet().removeIf(key -> key.startsWith(keyPrefix));
    }

    public void deleteById(String key) {
        emitters.remove(key);
    }
}