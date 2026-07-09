package com.heima.salarymanagesystem.config;

import com.heima.salarymanagesystem.interceptor.JwtTokenInterceptor;
import com.heima.salarymanagesystem.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册拦截器、配置跨域
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtTokenInterceptor jwtTokenInterceptor;
    private final LoginInterceptor loginInterceptor;

    /**
     * 注册拦截器
     * JwtTokenInterceptor：解析Token并设置用户上下文（所有请求）
     * LoginInterceptor：校验登录状态（除了登录接口外的所有接口）
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT Token 拦截器：优先级最高，负责解析 Token 并存入 ThreadLocal
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")
                .order(1);

        // 登录拦截器：校验是否已登录，未登录返回401
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/auth/login",          // 登录接口
                        "/api/auth/logout",          // 登出接口
                        "/error",                    // 错误页面
                        "/doc.html",                 // API文档
                        "/webjars/**",
                        "/v3/**",
                        "/swagger-ui/**"
                )
                .order(2);
    }

    /**
     * 跨域配置（CORS）
     * 允许前端开发服务器（localhost:5173）访问后端接口
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
