package com.heima.salarymanagesystem.controller;

import com.heima.salarymanagesystem.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 * 管理定时任务、系统参数等
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemConfigController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取定时任务配置
     * GET /api/system/schedule
     */
    @GetMapping("/schedule")
    public Result<Map<String, Object>> getSchedule() {
        Map<String, Object> result = new HashMap<>();

        // 查询定时事件详情和状态
        String day = "10";
        String time = "06:00:00";
        String eventStatus = "DISABLED";
        try {
            List<Map<String, Object>> events = jdbcTemplate.queryForList(
                "SELECT STARTS, STATUS FROM information_schema.events " +
                "WHERE event_name = 'evt_monthly_salary_init' AND event_schema = 'salary_system'");
            if (!events.isEmpty()) {
                Object starts = events.get(0).get("STARTS");
                if (starts != null) {
                    String startsStr = starts.toString();
                    // STARTS 格式: "2026-08-10 06:00:00"
                    day = startsStr.substring(8, 10);  // 提取日期
                    if (day.startsWith("0")) day = day.substring(1); // "06" → "6"
                    time = startsStr.substring(11, 19); // 提取时间
                }
                Object st = events.get(0).get("STATUS");
                if (st != null) eventStatus = st.toString();
            }
        } catch (Exception ignored) {}

        result.put("schedulerOn", "ENABLED".equalsIgnoreCase(eventStatus));
        result.put("day", Integer.parseInt(day));
        result.put("time", time);
        return Result.ok(result);
    }

    /**
     * 更新定时任务配置
     * POST /api/system/schedule
     * 请求体: { "day": 5, "time": "03:00:00" }
     */
    @PostMapping("/schedule")
    public Result<Void> updateSchedule(@RequestBody Map<String, Object> config) {
        int day = Integer.parseInt(config.get("day").toString());
        String time = config.get("time").toString();
        String starts = String.format("TIMESTAMP('%s-%02d %s')",
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")),
                day, time);

        try {
            jdbcTemplate.execute("ALTER EVENT evt_monthly_salary_init " +
                    "ON SCHEDULE EVERY 1 MONTH STARTS " + starts + " ON COMPLETION PRESERVE ENABLE");
            return Result.ok("设置成功", null);
        } catch (Exception e) {
            return Result.fail("设置失败: " + e.getMessage());
        }
    }

    /**
     * 开关定时任务
     * POST /api/system/schedule/toggle
     */
    @PostMapping("/schedule/toggle")
    public Result<Void> toggleSchedule(@RequestBody Map<String, Object> body) {
        boolean enable = Boolean.parseBoolean(body.get("enable").toString());
        try {
            jdbcTemplate.execute("ALTER EVENT evt_monthly_salary_init " + (enable ? "ENABLE" : "DISABLE"));
            return Result.ok(enable ? "定时任务已开启" : "定时任务已关闭", null);
        } catch (Exception e) {
            return Result.fail("操作失败: " + e.getMessage());
        }
    }
}
