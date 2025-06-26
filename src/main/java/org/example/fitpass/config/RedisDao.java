package org.example.fitpass.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.fitpass.domain.notify.entity.Notify;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class RedisDao {


    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, List<Notify>> redisTemplateForNotify;

    public RedisDao(RedisTemplate<String, String> redisTemplate, RedisTemplate<String, List<Notify>> redisTemplateForNotify) {
        this.redisTemplate = redisTemplate;
        this.redisTemplateForNotify = redisTemplateForNotify;
    }

    public void setValues(String key, String data) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, data);
    }

    public void setValues(String key, String data, Duration duration) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    public String getValues(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return values.get(key);
    }


    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    public void setValuesForNotification(String key) {
        ValueOperations<String, List<Notify>> values = redisTemplateForNotify.opsForValue();
        List<Notify> emptyList = new ArrayList<>();
        values.set(key, emptyList);
    }

    public List<Notify> getValuesForNotification(String key) {
        ValueOperations<String, List<Notify>> values = redisTemplateForNotify.opsForValue();
        return values.get(key);
    }

    public void updateValuesForNotification(String key, Notify notification) {
        ValueOperations<String, List<Notify>> values = redisTemplateForNotify.opsForValue();
        Object value = values.get(key);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Notify> notifications = objectMapper.convertValue(value, new TypeReference<List<Notify>>() {});
        notifications.add(notification);
        values.set(key, notifications);
    }

    public void deleteValuesForNotification(String key) {
        ValueOperations<String, List<Notify>> values = redisTemplateForNotify.opsForValue();
        Object value = values.get(key);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Notify> notifications = objectMapper.convertValue(value, new TypeReference<List<Notify>>() {});
        notifications.clear();
        values.set(key, notifications);
    }

    public void saveNotifyToRedis(Long memberId, Notify notify) {
        String key = "notify:" + memberId;
        List<Notify> list = getValuesForNotification(key);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(notify);
        redisTemplateForNotify.opsForValue().set(key, list);
    }
}