package top.naccl.dwz.web.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.naccl.dwz.common.constant.CacheConstants;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {
    @Bean
    public Cache<String, String> caffeineCache() {
        return Caffeine.newBuilder()
                .maximumSize(CacheConstants.CAFFEINE_MAX_SIZE)
                .expireAfterWrite(CacheConstants.CAFFEINE_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }
}
