package top.naccl.dwz.core.shortcode;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.naccl.dwz.common.constant.ShortUrlConstants;
import top.naccl.dwz.common.util.HashUtils;
import top.naccl.dwz.core.bloom.RedisBloomFilter;

@Component
public class HashShortCodeGenerator implements ShortCodeGenerator {
    @Autowired
    private RedisBloomFilter bloomFilter;

    @Override
    public String generate(String longUrl) {
        return generateWithRetry(longUrl, 0);
    }

    private String generateWithRetry(String longUrl, int retryCount) {
        String salt = retryCount > 0 ? String.valueOf(retryCount) : "";
        String hash = HashUtils.murmurHash32ToBase62(longUrl + salt);
        String shortCode = StringUtils.leftPad(hash, ShortUrlConstants.TARGET_LENGTH, '0');

        if (retryCount < ShortUrlConstants.MAX_RETRY && bloomFilter.mightContain(shortCode)) {
            return generateWithRetry(longUrl, retryCount + 1);
        }
        return shortCode;
    }
}
