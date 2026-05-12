package top.naccl.dwz.common.dto.response;

import lombok.Data;

@Data
public class StatsOverviewVO {
    private long totalUrls;
    private long totalViews;
    private long todayNewUrls;
    private long todayViews;
}
