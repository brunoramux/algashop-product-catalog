package com.algaworks.algashop.product.catalog.infrastructure.cache;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class ResilienceCacheErrorHandler implements CacheErrorHandler {
    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Error while accessing cache {} for key {}: {}", cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, @Nullable Object value) {
        log.warn("Error while putting value {} into cache {} for key {}: {}", value, cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Error while evicting key {} from cache {}: {}", key, cache.getName(), exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("Error while clearing cache {}: {}", cache.getName(), exception.getMessage());
    }
}
