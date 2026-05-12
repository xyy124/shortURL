package top.naccl.dwz.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VisitLog {
    private Long id;
    private String shortCode;
    private LocalDateTime visitTime;
    private String ip;
    private String userAgent;
    private String referer;
    private String country;
    private String city;
    private String deviceType;
    private String browser;
    private String os;
}
