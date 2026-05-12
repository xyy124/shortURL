package top.naccl.dwz.core.service;

public interface UrlMappingService {
    String getLongUrlByShortCode(String shortCode);
    String createShortUrl(String longUrl, Long userId, String customCode);
    void updateViews(String shortCode);
}
