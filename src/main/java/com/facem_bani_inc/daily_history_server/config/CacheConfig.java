package com.facem_bani_inc.daily_history_server.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.facem_bani_inc.daily_history_server.utils.Constants.DAILY_CONTENT_BY_DATE;
import static com.facem_bani_inc.daily_history_server.utils.Constants.GAMIFICATION_BY_USER_ID;
import static com.facem_bani_inc.daily_history_server.utils.Constants.LEADERBOARD;
import static com.facem_bani_inc.daily_history_server.utils.Constants.PRO_DAILY_CONTENT_BY_DATE;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                new CaffeineCache(DAILY_CONTENT_BY_DATE,
                        Caffeine.newBuilder()
                                .maximumSize(20)
                                .expireAfterWrite(24, TimeUnit.HOURS)
                                .build()),
                new CaffeineCache(PRO_DAILY_CONTENT_BY_DATE,
                        Caffeine.newBuilder()
                                .maximumSize(20)
                                .expireAfterWrite(24, TimeUnit.HOURS)
                                .build()),
                new CaffeineCache(GAMIFICATION_BY_USER_ID,
                        Caffeine.newBuilder()
                                .maximumSize(2000)
                                .expireAfterWrite(30, TimeUnit.MINUTES)
                                .build()),
                new CaffeineCache(LEADERBOARD,
                        Caffeine.newBuilder()
                                .maximumSize(1)
                                .expireAfterWrite(5, TimeUnit.MINUTES)
                                .build())
        ));
        return cacheManager;
    }
}
