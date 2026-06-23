package com.algaworks.algashop.product.catalog.infrastructure.cache;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCacheConfig implements CachingConfigurer {

    @Autowired
    private ResilienceCacheErrorHandler resilienceCacheErrorHandler;

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(c -> c + ":")
                .entryTtl(Duration.ofMinutes(1));
        return builder -> builder.cacheDefaults(redisCacheConfiguration)
                .withCacheConfiguration("algashop:products:v1", redisCacheConfiguration.disableCachingNullValues().entryTtl(Duration.ofMinutes(1)));
    }

    @Bean
    @Override
    public @Nullable CacheErrorHandler errorHandler() {
        return resilienceCacheErrorHandler;
    }
}

