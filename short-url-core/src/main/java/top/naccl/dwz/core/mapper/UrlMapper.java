package top.naccl.dwz.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.naccl.dwz.common.entity.UrlMap;

@Mapper
public interface UrlMapper extends BaseMapper<UrlMap> {
    @Select("select lurl from url_map where short_code = #{shortCode} and is_active = 1")
    String getLongUrlByShortUrl(@Param("shortCode") String shortCode);

    @Update("update url_map set views = views + 1 where short_code = #{shortCode}")
    void incrementViews(@Param("shortCode") String shortCode);
}
