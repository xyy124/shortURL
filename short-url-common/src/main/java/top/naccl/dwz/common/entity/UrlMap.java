package top.naccl.dwz.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlMap {
    private Long id;
    private String shortCode;
    private String longUrl;
    private Long userId;
    private String title;
    private Boolean isCustom;
    private Boolean isActive;
    private LocalDateTime expireTime;
    private Integer views;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public UrlMap(String shortCode, String longUrl) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.views = 0;
        this.isCustom = false;
        this.isActive = true;
    }
}
