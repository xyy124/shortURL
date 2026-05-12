package top.naccl.dwz.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.dwz.common.dto.ApiResponse;
import top.naccl.dwz.common.dto.PageResult;
import top.naccl.dwz.common.dto.request.CustomShortenRequest;
import top.naccl.dwz.common.dto.response.ShortenResponse;
import top.naccl.dwz.common.entity.UrlMap;
import top.naccl.dwz.common.enums.ResultCode;
import top.naccl.dwz.common.exception.ApiException;
import top.naccl.dwz.core.cache.MultiLevelCacheService;
import top.naccl.dwz.core.mapper.UrlMapper;
import top.naccl.dwz.core.service.UrlMappingService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/urls")
public class UrlManageController {
    @Autowired
    private UrlMappingService urlMappingService;
    @Autowired
    private UrlMapper urlMapper;
    @Autowired
    private MultiLevelCacheService cacheService;
    @Value("${short-url.host}")
    private String host;

    @GetMapping
    public ApiResponse<PageResult<UrlMap>> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = Long.valueOf(userDetails.getUsername());
        Page<UrlMap> result = urlMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UrlMap>()
                        .eq(UrlMap::getUserId, userId)
                        .orderByDesc(UrlMap::getCreateTime));
        return ApiResponse.ok(new PageResult<>(result.getTotal(), page, size, result.getRecords()));
    }

    @PostMapping
    public ApiResponse<ShortenResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CustomShortenRequest request) {
        String shortCode = urlMappingService.createShortUrl(
                request.getLongUrl(), Long.valueOf(userDetails.getUsername()), request.getCustomCode());
        return ApiResponse.ok(new ShortenResponse(shortCode, host + "/" + shortCode, request.getLongUrl()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        UrlMap urlMap = urlMapper.selectById(id);
        if (urlMap == null) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
        urlMapper.deleteById(id);
        cacheService.evict(urlMap.getShortCode());
        return ApiResponse.ok(null);
    }

    @PutMapping("/{id}/toggle")
    public ApiResponse<Void> toggleActive(@PathVariable Long id) {
        UrlMap urlMap = urlMapper.selectById(id);
        if (urlMap == null) {
            throw new ApiException(ResultCode.NOT_FOUND);
        }
        urlMap.setIsActive(!urlMap.getIsActive());
        urlMap.setUpdateTime(LocalDateTime.now());
        urlMapper.updateById(urlMap);
        cacheService.evict(urlMap.getShortCode());
        return ApiResponse.ok(null);
    }

    @GetMapping("/all")
    public ApiResponse<List<UrlMap>> allUrls() {
        List<UrlMap> list = urlMapper.selectList(
                new LambdaQueryWrapper<UrlMap>().orderByDesc(UrlMap::getCreateTime));
        return ApiResponse.ok(list);
    }
}
