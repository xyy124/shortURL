package top.naccl.dwz.api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.naccl.dwz.common.util.IpAddressUtils;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = IpAddressUtils.getIpAddress(request);
        String key = "rate:shorten:" + ip;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, 10, TimeUnit.SECONDS);
        }
        if (count != null && count > 5) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":429,\"msg\":\"请求过于频繁，请10秒后再试\"}");
            return false;
        }
        return true;
    }
}
