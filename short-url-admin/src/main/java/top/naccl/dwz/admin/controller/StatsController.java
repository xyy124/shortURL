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
        boolean admin = isAdmin(userDetails);
        Long userId = Long.valueOf(userDetails.getUsername());
        StatsOverviewVO vo = new StatsOverviewVO();
        if (admin) {
            vo.setTotalUrls(urlMapper.selectCount(
                    new LambdaQueryWrapper<UrlMap>().eq(UrlMap::getIsActive, true)));
            vo.setTotalViews(urlMapper.selectList(new LambdaQueryWrapper<UrlMap>())
                    .stream().mapToInt(UrlMap::getViews).sum());
            vo.setTodayNewUrls(urlMapper.selectCount(
                    new LambdaQueryWrapper<UrlMap>().apply("date(create_time) = curdate()")));
            vo.setTodayViews(dailyStatsMapper.selectList(
                    new LambdaQueryWrapper<DailyStats>()
                            .eq(DailyStats::getStatsDate, LocalDate.now()))
                    .stream().mapToInt(DailyStats::getPv).sum());
        } else {
            vo.setTotalUrls(urlMapper.selectCount(
                    new LambdaQueryWrapper<UrlMap>().eq(UrlMap::getUserId, userId)
                            .eq(UrlMap::getIsActive, true)));
            vo.setTotalViews(urlMapper.selectList(
                    new LambdaQueryWrapper<UrlMap>().eq(UrlMap::getUserId, userId))
                    .stream().mapToInt(UrlMap::getViews).sum());
            vo.setTodayNewUrls(urlMapper.selectCount(
                    new LambdaQueryWrapper<UrlMap>().eq(UrlMap::getUserId, userId)
                            .apply("date(create_time) = curdate()")));
            List<String> myShortCodes = urlMapper.selectList(
                    new LambdaQueryWrapper<UrlMap>().eq(UrlMap::getUserId, userId)
                            .select(UrlMap::getShortCode))
                    .stream().map(UrlMap::getShortCode).collect(Collectors.toList());
            vo.setTodayViews(myShortCodes.isEmpty() ? 0 : dailyStatsMapper.selectList(
                    new LambdaQueryWrapper<DailyStats>()
                            .in(DailyStats::getShortCode, myShortCodes)
                            .eq(DailyStats::getStatsDate, LocalDate.now()))
                    .stream().mapToInt(DailyStats::getPv).sum());
        }
        return ApiResponse.ok(vo);
    }

    @GetMapping("/daily")
    public ApiResponse<List<DailyStats>> daily(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String shortCode,
            @RequestParam(defaultValue = "7") int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        LambdaQueryWrapper<DailyStats> wrapper = new LambdaQueryWrapper<DailyStats>()
                .eq(shortCode != null, DailyStats::getShortCode, shortCode)
                .ge(DailyStats::getStatsDate, since);
        if (!isAdmin(userDetails)) {
            Long userId = Long.valueOf(userDetails.getUsername());
            List<String> myShortCodes = urlMapper.selectList(
                    new LambdaQueryWrapper<UrlMap>().eq(UrlMap::getUserId, userId)
                            .select(UrlMap::getShortCode))
                    .stream().map(UrlMap::getShortCode).collect(Collectors.toList());
            wrapper.in(!myShortCodes.isEmpty(), DailyStats::getShortCode, myShortCodes);
            if (myShortCodes.isEmpty()) {
                return ApiResponse.ok(List.of());
            }
        }
        wrapper.orderByAsc(DailyStats::getStatsDate);
        List<DailyStats> stats = dailyStatsMapper.selectList(wrapper);
        return ApiResponse.ok(stats);
    }

    private boolean isAdmin(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
