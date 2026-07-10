package com.heima.salarymanagesystem.controller;

import com.heima.salarymanagesystem.common.Result;
import com.heima.salarymanagesystem.common.UserHolder;
import com.heima.salarymanagesystem.mapper.EmployeeMapper;
import com.heima.salarymanagesystem.service.MonthlySalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 月度工资控制器
 * 核心薪资计算通过数据库存储过程实现
 * 权限控制：
 *   ADMIN - 查看全部
 *   MANAGER - 查看本部门
 *   EMPLOYEE - 仅查看自己
 */
@RestController
@RequestMapping("/api/salary")
@RequiredArgsConstructor
public class MonthlySalaryController {

    private final MonthlySalaryService monthlySalaryService;
    private final EmployeeMapper employeeMapper;

    /**
     * 自动计算月度工资（调用存储过程）
     * POST /api/salary/calculate?year=2025&month=6
     */
    @PostMapping("/calculate")
    public Result<Void> calculate(@RequestParam Integer year, @RequestParam Integer month) {
        try {
            monthlySalaryService.calculateMonthlySalary(year, month);
            return Result.ok("月度工资计算完成", null);
        } catch (Exception e) {
            return Result.fail("计算失败: " + e.getMessage());
        }
    }

    /**
     * 确认工资（锁定数据，不可再修改）
     * POST /api/salary/confirm?year=2025&month=6
     */
    @PostMapping("/confirm")
    public Result<Void> confirm(@RequestParam Integer year, @RequestParam Integer month) {
        try {
            monthlySalaryService.confirmSalary(year, month);
            return Result.ok("工资已确认", null);
        } catch (Exception e) {
            return Result.fail("确认失败: " + e.getMessage());
        }
    }

    /**
     * 按员工查询工资（权限过滤）
     * GET /api/salary/employee/{employeeId}?year=2025&month=6
     * - 管理员：可查看任意员工
     * - 管理人员：只能查看本部门员工（需前端传 correct employeeId）
     * - 普通员工：只能查看自己
     */
    @GetMapping("/employee/{employeeId}")
    public Result<List<Map<String, Object>>> queryEmployeeSalary(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        // 权限校验：普通员工只能查看自己的
        if (UserHolder.isAdmin() || UserHolder.isManager()) {
            // 管理员和管理人员可查看
        } else {
            // 普通员工：获取自己关联的员工ID
            Long currentEmployeeId = employeeMapper.selectEmployeeIdByUserId(UserHolder.getUserId());
            if (currentEmployeeId == null || !currentEmployeeId.equals(employeeId)) {
                return Result.fail(403, "无权查看其他员工的工资数据");
            }
        }
        return Result.ok(monthlySalaryService.queryEmployeeSalary(employeeId, year, month));
    }

    /**
     * 按部门查询工资汇总
     * GET /api/salary/department/{deptId}?year=2025&month=6
     */
    @GetMapping("/department/{deptId}")
    public Result<List<Map<String, Object>>> queryDepartmentSalary(
            @PathVariable Long deptId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        return Result.ok(monthlySalaryService.queryDepartmentSalary(deptId, year, month));
    }

    /**
     * 通用薪资查询（多条件筛选）
     * GET /api/salary/query?year=2025&month=6&deptId=1&employeeName=张三&empNo=FS2020001
     */
    @GetMapping("/query")
    public Result<List<Map<String, Object>>> salaryQuery(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String empNo) {
        return Result.ok(monthlySalaryService.salaryQuery(year, month, deptId, employeeName, empNo));
    }

    /**
     * 查询我的工资（当前登录用户）
     * 按角色自动区分：
     *   ADMIN → 全部员工工资
     *   MANAGER → 本部门全员（含自己，自己的置顶）
     *   EMPLOYEE → 仅自己
     * GET /api/salary/my?year=2025&month=6
     */
    @GetMapping("/my")
    public Result<Map<String, Object>> queryMySalary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        Long currentEmployeeId = employeeMapper.selectEmployeeIdByUserId(UserHolder.getUserId());
        if (currentEmployeeId == null) {
            return Result.fail("未关联员工信息");
        }

        String roleCode = UserHolder.getRoleCode();
        Map<String, Object> result = new HashMap<>();

        if ("ADMIN".equals(roleCode)) {
            // 管理员：查看全部
            result.put("scope", "all");
            result.put("records", monthlySalaryService.salaryQuery(year, month, null, null, null));
        } else if ("MANAGER".equals(roleCode)) {
            // 管理人员：仅看自己
            result.put("scope", "self");
            result.put("records", monthlySalaryService.queryEmployeeSalary(currentEmployeeId, year, month));
        } else {
            // 普通员工：仅自己
            result.put("scope", "self");
            result.put("records", monthlySalaryService.queryEmployeeSalary(currentEmployeeId, year, month));
        }

        return Result.ok(result);
    }

    /**
     * 更新手动录入项（仅草稿状态可修改）
     * PUT /api/salary/manual/{salaryId}
     * 请求体：{ "projectBonus": 1000, "taxDeduction": 200, ... }
     */
    @PutMapping("/manual/{salaryId}")
    public Result<Void> updateManualItems(@PathVariable Long salaryId,
                                           @RequestBody Map<String, Object> manualItems) {
        try {
            monthlySalaryService.updateManualItems(salaryId, manualItems);
            return Result.ok("更新成功", null);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    // ===== 报表接口 =====

    /**
     * 薪资结构分析报表
     * GET /api/salary/report/structure?year=2025&month=6
     */
    @GetMapping("/report/structure")
    public Result<List<Map<String, Object>>> structureReport(
            @RequestParam Integer year, @RequestParam Integer month) {
        return Result.ok(monthlySalaryService.salaryStructureReport(year, month));
    }

    /**
     * 部门薪资统计报表（最高/最低/平均工资）
     * GET /api/salary/report/dept-stats?year=2025&month=6
     */
    @GetMapping("/report/dept-stats")
    public Result<List<Map<String, Object>>> departmentSalaryStats(
            @RequestParam Integer year, @RequestParam Integer month) {
        return Result.ok(monthlySalaryService.departmentSalaryStats(year, month));
    }

    /**
     * 员工年度薪资统计
     * GET /api/salary/report/annual/1?year=2025
     */
    @GetMapping("/report/annual/{employeeId}")
    public Result<List<Map<String, Object>>> employeeAnnualStats(
            @PathVariable Long employeeId, @RequestParam Integer year) {
        return Result.ok(monthlySalaryService.employeeAnnualStats(employeeId, year));
    }
}
