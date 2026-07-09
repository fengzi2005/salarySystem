package com.heima.salarymanagesystem.controller;

import com.heima.salarymanagesystem.common.Result;
import com.heima.salarymanagesystem.entity.Employee;
import com.heima.salarymanagesystem.service.EmployeeService;
import com.heima.salarymanagesystem.service.impl.EmployeeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工管理控制器
 * 支持员工信息 CRUD、查询、岗位/职称变更
 */
@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 分页查询员工列表（通过视图获取完整信息）
     * GET /api/employee/list?page=1&pageSize=10
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getEmployeeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("records", employeeService.getEmployeeList(page, pageSize));
        result.put("total", ((EmployeeServiceImpl) employeeService).getEmployeeCount());
        return Result.ok(result);
    }

    /**
     * 查询员工完整信息（通过视图）
     * GET /api/employee/info/{id}
     */
    @GetMapping("/info/{id}")
    public Result<Map<String, Object>> getEmployeeInfo(@PathVariable Long id) {
        return Result.ok(employeeService.getEmployeeInfo(id));
    }

    /**
     * 查询在职员工列表
     * GET /api/employee/active
     */
    @GetMapping("/active")
    public Result<List<Map<String, Object>>> getActiveEmployees() {
        return Result.ok(employeeService.getActiveEmployees());
    }

    /**
     * 搜索员工
     * GET /api/employee/search?keyword=张三&deptId=1&positionId=2
     */
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long positionId) {
        return Result.ok(employeeService.searchEmployees(keyword, deptId, positionId));
    }

    /**
     * 根据ID查询员工
     * GET /api/employee/{id}
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        return Result.ok(employeeService.getById(id));
    }

    /**
     * 新增员工
     * POST /api/employee
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody Employee employee) {
        return Result.ok(employeeService.save(employee));
    }

    /**
     * 更新员工
     * PUT /api/employee
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Employee employee) {
        return Result.ok(employeeService.updateById(employee));
    }

    /**
     * 删除员工
     * DELETE /api/employee/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(employeeService.removeById(id));
    }

    /**
     * 变更员工岗位或职称
     * POST /api/employee/change
     * 会同时记录变更历史日志
     */
    @PostMapping("/change")
    public Result<Void> changePositionOrTitle(@RequestBody Map<String, String> changeData) {
        try {
            employeeService.changePositionOrTitle(
                    Long.valueOf(changeData.get("employeeId")),
                    changeData.get("changeType"),
                    changeData.get("oldValueId") != null ? Long.valueOf(changeData.get("oldValueId")) : null,
                    Long.valueOf(changeData.get("newValueId")),
                    changeData.get("oldValueName"),
                    changeData.get("newValueName"),
                    changeData.get("changeDate"),
                    changeData.get("effectiveDate"),
                    changeData.get("remark")
            );
            return Result.ok("变更成功", null);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
