package top.naccl.dwz.core.visit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import top.naccl.dwz.common.entity.VisitLog;
import top.naccl.dwz.common.util.IpAddressUtils;
import top.naccl.dwz.core.mapper.DailyStatsMapper;
import top.naccl.dwz.core.mapper.VisitLogMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
public class VisitLogger {
    @Autowired
    private VisitLogMapper visitLogMapper;
    @Autowired
    private DailyStatsMapper dailyStatsMapper;

    @Async
    public void logVisit(String shortCode, HttpServletRequest request) {
        try {
            VisitLog visitLog = new VisitLog();
            visitLog.setShortCode(shortCode);
            visitLog.setVisitTime(LocalDateTime.now());
            visitLog.setIp(IpAddressUtils.getIpAddress(request));
            String ua = request.getHeader("User-Agent");
            visitLog.setUserAgent(ua);
            visitLog.setReferer(request.getHeader("Referer"));
            if (ua != null) {
                visitLog.setDeviceType(ua.contains("Mobile") || ua.contains("Android") ? "MOBILE" : "PC");
                visitLog.setBrowser(ua.contains("Chrome") ? "Chrome" :
                        ua.contains("Firefox") ? "Firefox" :
                        ua.contains("Safari") ? "Safari" : "Other");
                visitLog.setOs(ua.contains("Windows") ? "Windows" :
                        ua.contains("Mac") ? "macOS" :
                        ua.contains("Linux") ? "Linux" : "Other");
            }
            visitLogMapper.insert(visitLog);
            dailyStatsMapper.upsertPv(shortCode, LocalDate.now());
        } catch (Exception e) {
            log.error("记录访问日志失败: shortCode={}", shortCode, e);
        }
    }
}
