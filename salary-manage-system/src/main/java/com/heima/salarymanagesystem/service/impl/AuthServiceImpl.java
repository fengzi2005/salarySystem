package com.heima.salarymanagesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.salarymanagesystem.common.JwtUtils;
import com.heima.salarymanagesystem.entity.Employee;
import com.heima.salarymanagesystem.entity.User;
import com.heima.salarymanagesystem.mapper.EmployeeMapper;
import com.heima.salarymanagesystem.mapper.UserMapper;
import com.heima.salarymanagesystem.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 * 实现基于 JWT + Redis 的登录认证
 * Token 写入 Redis，有效期为1小时
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final EmployeeMapper employeeMapper;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    /** Redis中Token的Key前缀 */
    private static final String TOKEN_PREFIX = "login:token:";

    /** Token有效期（小时） */
    private static final long TOKEN_EXPIRE_HOURS = 1;

    @Override
    public Map<String, Object> login(String username, String password) {
        // 注意：此示例中明文比对密码，实际生产环境应使用 BCryptPasswordEncoder
        // 这里为了演示方便，直接使用明文比对
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getPassword, password)  // TODO: 生产环境改为 BCrypt 验证
                        .eq(User::getStatus, 1)
        );

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 查询用户角色
        String roleCode = userMapper.selectRoleCodeByUserId(user.getId());

        // 生成 JWT Token
        String token = jwtUtils.generateToken(user.getId(), username, roleCode);

        // Token 存入 Redis，有效期1小时
        String redisKey = TOKEN_PREFIX + token;
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("userId", user.getId());
        tokenInfo.put("username", username);
        tokenInfo.put("roleCode", roleCode);
        redisTemplate.opsForValue().set(redisKey, tokenInfo, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);

        // 查询关联的员工信息
        String employeeName = null;
        Long employeeId = user.getEmployeeId();
        String positionName = null;
        if (employeeId != null) {
            Map<String, Object> empInfo = employeeMapper.selectEmployeeInfoById(employeeId);
            if (empInfo != null) {
                employeeName = (String) empInfo.get("employee_name");
                positionName = (String) empInfo.get("position_name");
            }
        }

        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", username);
        result.put("roleCode", roleCode);
        result.put("employeeId", employeeId);
        result.put("employeeName", employeeName);
        result.put("positionName", positionName);
        return result;
    }

    @Override
    public void logout(String token) {
        String redisKey = TOKEN_PREFIX + token;
        redisTemplate.delete(redisKey);
    }
}
