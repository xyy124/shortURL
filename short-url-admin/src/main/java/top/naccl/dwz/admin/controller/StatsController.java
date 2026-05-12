package top.naccl.dwz.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.dwz.common.dto.ApiResponse;
import top.naccl.dwz.common.dto.response.StatsOverviewVO;
import top.naccl.dwz.common.entity.DailyStats;
import top.naccl.dwz.common.entity.UrlMap;
import top.naccl.dwz.core.mapper.DailyStatsMapper;
import top.naccl.dwz.core.mapper.UrlMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/stats")
public class StatsController {
    @Autowired
    private UrlMapper urlMapper;
    @Autowired
    private DailyStatsMapper dailyStatsMapper;

    @GetMapping("/overview")
    public ApiResponse<StatsOverviewVO> overview(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.valueOf(userDetails.getUsername());
        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setTotalUrls(urlMapper.selectCount(
                new LambdaQueryWrapper<UrlMap>().eq(UrlMap::getUserId, userId).or().isNull(UrlMap::getUserId)));
        vo.setTotalViews(urlMapper.selectList(
                new LambdaQueryWrapper<UrlMap>().eq(UrlMap::getUserId, userId).or().isNull(UrlMap::getUserId))
                .stream().mapToInt(UrlMap::getViews).sum());
        vo.setTodayNewUrls(urlMapper.selectCount(
                new LambdaQueryWrapper<UrlMap>().eq(UrlMap::getUserId, userId).or().isNull(UrlMap::getUserId)
                        .apply("date(create_time) = curdate()")));
        List<String> myShortCodes = urlMapper.selectList(
                new LambdaQueryWrapper<UrlMap>()
                        .eq(UrlMap::getUserId, userId).or().isNull(UrlMap::getUserId)
                        .select(UrlMap::getShortCode))
                .stream().map(UrlMap::getShortCode).collect(Collectors.toList());
        vo.setTodayViews(myShortCodes.isEmpty() ? 0 : dailyStatsMapper.selectList(
                new LambdaQueryWrapper<DailyStats>()
                        .in(DailyStats::getShortCode, myShortCodes)
                        .eq(DailyStats::getStatsDate, LocalDate.now()))
                .stream().mapToInt(DailyStats::getPv).sum());
        return ApiResponse.ok(vo);
    }

    @GetMapping("/daily")
    public ApiResponse<List<DailyStats>> daily(
            @RequestParam(required = false) String shortCode,
            @RequestParam(defaultValue = "7") int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        List<DailyStats> stats = dailyStatsMapper.selectList(
                new LambdaQueryWrapper<DailyStats>()
                        .eq(shortCode != null, DailyStats::getShortCode, shortCode)
                        .ge(DailyStats::getStatsDate, since)
                        .orderByAsc(DailyStats::getStatsDate));
        return ApiResponse.ok(stats);
    }
}
