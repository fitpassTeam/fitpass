package org.example.fitpass.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public void setRefreshToken(String email, String token, long durationMs) {
        redisTemplate.opsForValue().set(email, token, Duration.ofMillis(durationMs));
    }

    public void setBlackList(String token, String value, long expirationMillis) {
        redisTemplate.opsForValue().set(token, value, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }

    public void deleteRefreshToken(String email) {
        redisTemplate.delete(email);
    }
}
