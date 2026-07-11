package com.heima.salarymanagesystem.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 启动预热：提前初始化数据库连接池和 Redis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupWarmer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.info("数据库连接池预热完成");
        } catch (Exception e) {
            log.warn("数据库预热失败: {}", e.getMessage());
        }
    }
}
