package top.naccl.dwz.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.dwz.common.dto.ApiResponse;
import top.naccl.dwz.common.dto.request.ShortenRequest;
import top.naccl.dwz.common.dto.response.ShortenResponse;
import top.naccl.dwz.common.util.UrlUtils;
import top.naccl.dwz.core.service.UrlMappingService;

@RestController
@RequestMapping("/api/v1")
public class ShortenController {
    @Autowired
    private UrlMappingService urlMappingService;
    @Value("${short-url.host}")
    private String host;

    @PostMapping("/shorten")
    public ApiResponse<ShortenResponse> shorten(@RequestBody ShortenRequest request) {
        String longUrl = request.getLongUrl();
        if (!UrlUtils.checkURL(longUrl)) {
            return ApiResponse.fail(400, "URL 格式有误");
        }
        if (!longUrl.startsWith("http")) {
            longUrl = "http://" + longUrl;
        }
        String shortCode = urlMappingService.createShortUrl(longUrl, null, request.getCustomCode());
        return ApiResponse.ok(new ShortenResponse(shortCode, host + shortCode, longUrl));
    }
}
