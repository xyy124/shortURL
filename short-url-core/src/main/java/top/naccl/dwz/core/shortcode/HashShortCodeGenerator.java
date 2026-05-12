package top.naccl.dwz.core.shortcode;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.naccl.dwz.common.constant.ShortUrlConstants;
import top.naccl.dwz.common.exception.ApiException;
import top.naccl.dwz.common.enums.ResultCode;
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
        if (retryCount > ShortUrlConstants.MAX_RETRY) {
            throw new ApiException(ResultCode.ERROR.getCode(), "短码生成冲突过多，请稍后重试");
        }
        String salt = retryCount > 0 ? String.valueOf(retryCount) : "";
        String hash = HashUtils.murmurHash32ToBase62(longUrl + salt);
        String shortCode = StringUtils.leftPad(hash, ShortUrlConstants.TARGET_LENGTH, '0');

        if (bloomFilter.mightContain(shortCode)) {
            return generateWithRetry(longUrl, retryCount + 1);
        }
        return shortCode;
    }
}
