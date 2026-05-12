package top.naccl.dwz.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.naccl.dwz.common.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("select * from user where username = #{username}")
    User findByUsername(@Param("username") String username);

    @Select("select count(1) from user where username = #{username}")
    boolean existsByUsername(@Param("username") String username);
}
