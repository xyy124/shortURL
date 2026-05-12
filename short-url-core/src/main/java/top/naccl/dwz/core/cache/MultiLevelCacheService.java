package top.naccl.dwz.core.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.naccl.dwz.common.constant.CacheConstants;
import top.naccl.dwz.core.bloom.RedisBloomFilter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
public class MultiLevelCacheService {
    @Autowired
    private Cache<String, String> caffeineCache;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisBloomFilter bloomFilter;

    public String getWithProtection(String key, Supplier<String> loader) {
        // 1. 布隆过滤器前置拦截
        if (!bloomFilter.mightContain(key)) {
            return null;
        }

        // 2. L1 Caffeine
        String value = caffeineCache.getIfPresent(key);
        if (value != null) {
            return CacheConstants.EMPTY_VALUE.equals(value) ? null : value;
        }

        // 3. L2 Redis
        value = redisTemplate.opsForValue().get(CacheConstants.URL_MAPPING_KEY + key);
        if (value != null) {
            caffeineCache.put(key, value);
            return CacheConstants.EMPTY_VALUE.equals(value) ? null : value;
        }

        // 4. 互斥锁加载 DB（while 循环避避免递归栈溢出）
        RLock lock = redissonClient.getLock(CacheConstants.LOCK_KEY_PREFIX + key);
        try {
            while (true) {
                if (lock.tryLock(2, 10, TimeUnit.SECONDS)) {
                    try {
                        // Double-check
                        value = redisTemplate.opsForValue().get(CacheConstants.URL_MAPPING_KEY + key);
                        if (value != null) {
                            caffeineCache.put(key, value);
                            return CacheConstants.EMPTY_VALUE.equals(value) ? null : value;
                        }
                        value = loader.get();
                        long ttl = CacheConstants.REDIS_BASE_TTL_SECONDS
                                + ThreadLocalRandom.current().nextLong(CacheConstants.REDIS_TTL_RANDOM_RANGE);
                        if (value != null) {
                            redisTemplate.opsForValue().set(CacheConstants.URL_MAPPING_KEY + key, value, ttl, TimeUnit.SECONDS);
                        } else {
                            redisTemplate.opsForValue().set(CacheConstants.URL_MAPPING_KEY + key,
                                    CacheConstants.EMPTY_VALUE, CacheConstants.NULL_VALUE_TTL_SECONDS, TimeUnit.SECONDS);
                        }
                        caffeineCache.put(key, value != null ? value : CacheConstants.EMPTY_VALUE);
                        return value;
                    } finally {
                        lock.unlock();
                    }
                }
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void set(String key, String value) {
        long ttl = CacheConstants.REDIS_BASE_TTL_SECONDS
                + ThreadLocalRandom.current().nextLong(CacheConstants.REDIS_TTL_RANDOM_RANGE);
        redisTemplate.opsForValue().set(CacheConstants.URL_MAPPING_KEY + key, value, ttl, TimeUnit.SECONDS);
        caffeineCache.put(key, value);
    }

    public void evict(String key) {
        caffeineCache.invalidate(key);
        redisTemplate.delete(CacheConstants.URL_MAPPING_KEY + key);
    }
}
