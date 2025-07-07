package org.example.fitpass.common.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary // 여러 개의 CacheManager가 존재할 때, 기본값으로 사용할 것을 명시
    public CacheManager cacheManager() {
        // ConcurrentMapCacheManager 메모리 내에서 캐시를 관리하는 기본 구현체, 애플리케이션이 꺼지면 캐시도 사라지는 메모리 기반 캐시
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        // 사용할 캐시 이름들을 등록, @Cacheable(value = "storeSearch") 처럼 사용
        cacheManager.setCacheNames(java.util.Arrays.asList("gymSearch", "postSearch", "trainerSearch","popularKeywords"));
        return cacheManager;
    }

    @Bean("customKeyGenerator")
    public KeyGenerator customKeyGenerator() {
        return (target, method, params) -> {
            String keyword = (String) params[0];
            Pageable pageable = (Pageable) params[1];

            return "keyword:" + keyword +
                    ":page:" + pageable.getPageNumber() +
                    ":size:" + pageable.getPageSize();
        };
    }

}