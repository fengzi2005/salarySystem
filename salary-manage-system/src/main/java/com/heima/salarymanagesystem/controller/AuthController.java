package com.heima.salarymanagesystem.controller;

import com.heima.salarymanagesystem.common.Result;
import com.heima.salarymanagesystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、登出请求
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * POST /api/auth/login
     * 请求体：{ "username": "admin", "password": "123456" }
     * 返回：{ code: 200, message: "...", data: { token: "...", username: "...", roleCode: "..." } }
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");

            if (username == null || password == null) {
                return Result.fail("用户名和密码不能为空");
            }

            Map<String, Object> result = authService.login(username, password);
            return Result.ok("登录成功", result);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 用户登出
     * POST /api/auth/logout
     * 请求头：Authorization: Bearer <token>
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization;
        if (authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        authService.logout(token);
        return Result.ok("已安全退出", null);
    }
}
