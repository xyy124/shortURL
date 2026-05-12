package top.naccl.dwz.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.naccl.dwz.common.constant.ShortUrlConstants;
import top.naccl.dwz.common.entity.UrlMap;
import top.naccl.dwz.common.enums.ResultCode;
import top.naccl.dwz.common.exception.ApiException;
import top.naccl.dwz.core.bloom.RedisBloomFilter;
import top.naccl.dwz.core.cache.MultiLevelCacheService;
import top.naccl.dwz.core.mapper.UrlMapper;
import top.naccl.dwz.core.service.UrlMappingService;
import top.naccl.dwz.core.shortcode.CustomShortCodeValidator;
import top.naccl.dwz.core.shortcode.HashShortCodeGenerator;

@Slf4j
@Service
public class UrlMappingServiceImpl implements UrlMappingService {
    @Autowired
    private UrlMapper urlMapper;
    @Autowired
    private MultiLevelCacheService cacheService;
    @Autowired
    private RedisBloomFilter bloomFilter;
    @Autowired
    private HashShortCodeGenerator codeGenerator;
    @Autowired
    private CustomShortCodeValidator customValidator;

    @Override
    public String getLongUrlByShortCode(String shortCode) {
        return cacheService.getWithProtection(shortCode, () -> {
            UrlMap urlMap = urlMapper.selectById(shortCode);
            if (urlMap == null) {
                urlMap = urlMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UrlMap>()
                        .eq(UrlMap::getShortCode, shortCode)
                        .eq(UrlMap::getIsActive, true));
            }
            return urlMap != null ? urlMap.getLongUrl() : null;
        });
    }

    @Override
    @Transactional
    public String createShortUrl(String longUrl, Long userId, String customCode) {
        if (StringUtils.isNotBlank(customCode)) {
            customValidator.validate(customCode);
            return saveUrlMap(customCode, longUrl, userId, true);
        }
        String shortCode = codeGenerator.generate(longUrl);
        return saveUrlMap(shortCode, longUrl, userId, false);
    }

    private String saveUrlMap(String shortCode, String longUrl, Long userId, boolean isCustom) {
        try {
            UrlMap urlMap = new UrlMap(shortCode, longUrl);
            urlMap.setUserId(userId);
            urlMap.setIsCustom(isCustom);
            urlMapper.insert(urlMap);
            bloomFilter.add(shortCode);
            cacheService.set(shortCode, longUrl);
            return shortCode;
        } catch (DuplicateKeyException e) {
            if (isCustom) {
                throw new ApiException(ResultCode.CONFLICT);
            }
            return createShortUrl(longUrl + ShortUrlConstants.DUPLICATE_SALT, userId, null);
        }
    }

    @Override
    public void updateViews(String shortCode) {
        urlMapper.incrementViews(shortCode);
    }
}
