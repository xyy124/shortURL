package top.naccl.dwz.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.naccl.dwz.common.entity.DailyStats;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyStatsMapper extends BaseMapper<DailyStats> {
    @Insert("insert into daily_stats(short_code, stats_date, pv, uv, ip_count) values(#{shortCode}, #{date}, 1, 0, 0) " +
            "on duplicate key update pv = pv + 1")
    void upsertPv(@Param("shortCode") String shortCode, @Param("date") LocalDate date);

    @Update("update daily_stats set uv = uv + 1 where short_code = #{shortCode} and stats_date = #{date}")
    void incrementUv(@Param("shortCode") String shortCode, @Param("date") LocalDate date);

    @Update("update daily_stats set ip_count = ip_count + 1 where short_code = #{shortCode} and stats_date = #{date}")
    void incrementIp(@Param("shortCode") String shortCode, @Param("date") LocalDate date);

    @Select("select * from daily_stats where short_code = #{shortCode} and stats_date >= #{since} order by stats_date")
    List<DailyStats> selectByCodeAndDate(@Param("shortCode") String shortCode, @Param("since") LocalDate since);
}
