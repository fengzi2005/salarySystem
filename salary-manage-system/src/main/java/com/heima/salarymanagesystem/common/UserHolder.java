package com.heima.salarymanagesystem.common;

/**
 * 用户上下文持有者
 * 基于 ThreadLocal 实现，用于在同一线程中传递当前登录用户信息
 * 每次请求结束后必须在拦截器中调用 remove() 清除，防止内存泄漏
 */
public class UserHolder {

    private static final ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 存储当前登录用户信息到 ThreadLocal
     */
    public static void set(Long userId, String username, String roleCode) {
        USER_THREAD_LOCAL.set(userId);
        USERNAME_THREAD_LOCAL.set(username);
        ROLE_THREAD_LOCAL.set(roleCode);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        return USERNAME_THREAD_LOCAL.get();
    }

    /**
     * 获取当前用户角色编码
     */
    public static String getRoleCode() {
        return ROLE_THREAD_LOCAL.get();
    }

    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        return "ADMIN".equals(ROLE_THREAD_LOCAL.get());
    }

    /**
     * 判断当前用户是否为管理人员
     */
    public static boolean isManager() {
        return "MANAGER".equals(ROLE_THREAD_LOCAL.get());
    }

    /**
     * 判断当前用户是否已登录
     */
    public static boolean isLogin() {
        return USER_THREAD_LOCAL.get() != null;
    }

    /**
     * 清除 ThreadLocal（防止内存泄漏，必须在请求结束后调用）
     */
    public static void remove() {
        USER_THREAD_LOCAL.remove();
        USERNAME_THREAD_LOCAL.remove();
        ROLE_THREAD_LOCAL.remove();
    }
}
