package com.heima.salarymanagesystem.interceptor;

import com.heima.salarymanagesystem.common.JwtUtils;
import com.heima.salarymanagesystem.common.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT Token 拦截器
 * 优先级最高（order=1），在每个请求到达 Controller 之前执行
 * 职责：从请求头中提取 Token，解析用户信息，存入 ThreadLocal
 *
 * 执行流程：
 * 1. 从 Authorization 请求头获取 Token
 * 2. 如果 Token 为空，直接放行（由 LoginInterceptor 判断是否需要登录）
 * 3. 如果 Token 不为空，解析 Token 获取用户信息
 * 4. 验证 Redis 中 Token 是否有效
 * 5. 将用户信息存入 UserHolder（ThreadLocal）
 */
@Component
@RequiredArgsConstructor
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    /** Redis 中 Token 的 Key 前缀 */
    private static final String TOKEN_PREFIX = "login:token:";

    /** 请求头中的 Authorization 字段名 */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 从请求头获取 Token
        String authorization = request.getHeader(AUTHORIZATION_HEADER);

        // 2. Token 为空，直接放行（LoginInterceptor 会处理未登录情况）
        if (authorization == null || authorization.isEmpty()) {
            return true;
        }

        // 3. 支持 "Bearer <token>" 格式
        String token = authorization;
        if (authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }

        // 4. 解析 Token
        if (jwtUtils.isTokenExpired(token)) {
            return true; // Token 过期，放行由 LoginInterceptor 返回401
        }

        String username = jwtUtils.getUsername(token);
        String roleCode = jwtUtils.getRoleCode(token);
        Long userId = jwtUtils.getUserId(token);

        // 5. 验证 Redis 中 Token 是否有效（是否被主动登出）
        String redisKey = TOKEN_PREFIX + token;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
            return true; // Redis 中不存在，可能已过期或已登出
        }

        // 6. 将用户信息存入 ThreadLocal
        UserHolder.set(userId, username, roleCode);

        return true;
    }

    /**
     * 请求完成后清除 ThreadLocal，防止内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        UserHolder.remove();
    }
}
