package com.heima.salarymanagesystem.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.salarymanagesystem.common.Result;
import com.heima.salarymanagesystem.common.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 登录拦截器
 * 优先级低于 JwtTokenInterceptor（order=2）
 * 职责：校验用户是否已登录，未登录返回 401 状态码
 *
 * 前端接收到 401 响应后，应重定向到登录页面
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 检查 ThreadLocal 中是否有用户信息
        if (!UserHolder.isLogin()) {
            // 未登录，返回 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            Result<Object> result = Result.unauthorized("未登录或登录已过期，请重新登录");
            String json = new ObjectMapper().writeValueAsString(result);
            response.getWriter().write(json);
            return false;
        }
        return true;
    }
}
