package com.heima.salarymanagesystem.controller;

import com.heima.salarymanagesystem.common.Result;
import com.heima.salarymanagesystem.entity.Attendance;
import com.heima.salarymanagesystem.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 考勤管理控制器
 * 考勤数据的扣款/奖金由数据库触发器自动计算
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * 查询某月所有员工考勤记录
     * GET /api/attendance/month?year=2025&month=6
     */
    @GetMapping("/month")
    public Result<List<Map<String, Object>>> getMonthlyAttendance(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        return Result.ok(attendanceService.getMonthlyAttendance(year, month));
    }

    /**
     * 查询所有考勤记录
     * GET /api/attendance
     */
    @GetMapping
    public Result<List<Attendance>> listAll() {
        return Result.ok(attendanceService.list());
    }

    /**
     * 根据ID查询考勤
     * GET /api/attendance/{id}
     */
    @GetMapping("/{id}")
    public Result<Attendance> getById(@PathVariable Long id) {
        return Result.ok(attendanceService.getById(id));
    }

    /**
     * 新增考勤记录（触发器自动计算扣款/奖金）
     * POST /api/attendance
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody Attendance attendance) {
        return Result.ok(attendanceService.save(attendance));
    }

    /**
     * 更新考勤记录（触发器自动重新计算）
     * PUT /api/attendance
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Attendance attendance) {
        return Result.ok(attendanceService.updateById(attendance));
    }

    /**
     * 删除考勤记录
     * DELETE /api/attendance/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(attendanceService.removeById(id));
    }

    /**
     * 批量导入考勤数据
     * POST /api/attendance/batch
     */
    @PostMapping("/batch")
    public Result<Void> batchImport(@RequestBody List<Attendance> attendanceList) {
        attendanceService.batchImport(attendanceList);
        return Result.ok("批量导入成功", null);
    }
}
