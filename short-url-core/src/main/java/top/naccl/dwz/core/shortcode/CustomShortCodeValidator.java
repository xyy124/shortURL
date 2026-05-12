package top.naccl.dwz.core.shortcode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.naccl.dwz.common.enums.ResultCode;
import top.naccl.dwz.common.exception.ApiException;
import top.naccl.dwz.common.util.UrlUtils;
import top.naccl.dwz.core.bloom.RedisBloomFilter;

@Component
public class CustomShortCodeValidator {
    @Autowired
    private RedisBloomFilter bloomFilter;

    public void validate(String code) {
        if (!UrlUtils.validateCustomCode(code)) {
            throw new ApiException(ResultCode.BAD_REQUEST.getCode(), "自定义短链必须以字母开头，4-16位字母数字");
        }
        if (bloomFilter.mightContain(code)) {
            throw new ApiException(ResultCode.CONFLICT);
        }
    }
}
