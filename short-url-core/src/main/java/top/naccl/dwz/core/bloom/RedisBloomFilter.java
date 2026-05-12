package top.naccl.dwz.core.bloom;

import cn.hutool.core.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.naccl.dwz.common.constant.CacheConstants;
import java.nio.charset.StandardCharsets;

@Component
public class RedisBloomFilter {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String BLOOM_KEY = CacheConstants.BLOOM_FILTER_KEY;
    private static final long BIT_SIZE = 1L << 24;
    private static final int[] SEEDS = {1, 3, 5, 7, 11};

    public boolean add(String key) {
        boolean existed = true;
        for (int seed : SEEDS) {
            long index = hash(key, seed) % BIT_SIZE;
            if (Boolean.FALSE.equals(redisTemplate.opsForValue().setBit(BLOOM_KEY, index, true))) {
                existed = false;
            }
        }
        return existed;
    }

    public boolean mightContain(String key) {
        for (int seed : SEEDS) {
            long index = hash(key, seed) % BIT_SIZE;
            if (Boolean.FALSE.equals(redisTemplate.opsForValue().getBit(BLOOM_KEY, index))) {
                return false;
            }
        }
        return true;
    }

    private long hash(String key, int seed) {
        return HashUtil.murmur32((key + seed).getBytes(StandardCharsets.UTF_8)) & 0xFFFFFFFFL;
    }
}
