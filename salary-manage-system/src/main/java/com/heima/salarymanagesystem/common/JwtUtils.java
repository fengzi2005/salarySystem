package com.heima.salarymanagesystem.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和解析 JWT Token
 * Token 有效期1小时，存储在 Redis 中实现主动失效
 */
@Component
public class JwtUtils {

    /** JWT 签名密钥（至少256位） */
    private static final String SECRET_KEY = "salary-system-secret-key-2025-foshan-university-very-long-key!!";

    /** Token 有效期：1小时（毫秒） */
    private static final long EXPIRE_TIME = 60 * 60 * 1000;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roleCode 角色编码
     * @return JWT Token 字符串
     */
    public String generateToken(Long userId, String username, String roleCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roleCode", roleCode);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 JWT Token，获取 Claims
     *
     * @param token JWT Token
     * @return Claims 对象，解析失败返回 null
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 Token 中获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        return claims.getSubject();
    }

    /**
     * 从 Token 中获取角色编码
     */
    public String getRoleCode(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        return claims.get("roleCode", String.class);
    }

    /**
     * 判断 Token 是否已过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return true;
        return claims.getExpiration().before(new Date());
    }

    /**
     * 获取 Token 剩余有效时间（毫秒）
     */
    public long getRemainingTime(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return 0;
        return Math.max(0, claims.getExpiration().getTime() - System.currentTimeMillis());
    }
}
