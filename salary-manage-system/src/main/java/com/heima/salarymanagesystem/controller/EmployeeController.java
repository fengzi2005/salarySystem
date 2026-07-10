package com.heima.salarymanagesystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.salarymanagesystem.common.Result;
import com.heima.salarymanagesystem.common.UserHolder;
import com.heima.salarymanagesystem.entity.Employee;
import com.heima.salarymanagesystem.entity.Position;
import com.heima.salarymanagesystem.entity.User;
import com.heima.salarymanagesystem.mapper.EmployeeMapper;
import com.heima.salarymanagesystem.mapper.PositionMapper;
import com.heima.salarymanagesystem.mapper.UserMapper;
import com.heima.salarymanagesystem.service.EmployeeService;
import com.heima.salarymanagesystem.service.impl.EmployeeServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final EmployeeMapper employeeMapper;
    private final UserMapper userMapper;
    private final PositionMapper positionMapper;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 分页查询员工列表（通过视图获取完整信息）
     * GET /api/employee/list?page=1&pageSize=10
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getEmployeeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        // MANAGER 角色：只看本部门
        if ("MANAGER".equals(UserHolder.getRoleCode())) {
            Long empId = employeeMapper.selectEmployeeIdByUserId(UserHolder.getUserId());
            if (empId != null) {
                Map<String, Object> empInfo = employeeMapper.selectEmployeeInfoById(empId);
                Long deptId = empInfo != null ? (Long) empInfo.get("dept_id") : null;
                if (deptId != null) {
                    result.put("records", ((EmployeeServiceImpl) employeeService).getEmployeeListByDept(page, pageSize, deptId));
                    result.put("total", ((EmployeeServiceImpl) employeeService).getEmployeeCountByDept(deptId));
                    return Result.ok(result);
                }
            }
        }
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
        List<Map<String, Object>> list = employeeService.getActiveEmployees();
        if ("MANAGER".equals(UserHolder.getRoleCode())) {
            Long deptId = getManagerDeptId();
            if (deptId != null) {
                list = list.stream().filter(e -> deptId.equals(e.get("department_id"))).collect(java.util.stream.Collectors.toList());
            }
        }
        return Result.ok(list);
    }

    private Long getManagerDeptId() {
        Long empId = employeeMapper.selectEmployeeIdByUserId(UserHolder.getUserId());
        if (empId != null) {
            Map<String, Object> empInfo = employeeMapper.selectEmployeeInfoById(empId);
            return empInfo != null ? (Long) empInfo.get("dept_id") : null;
        }
        return null;
    }

    /** 中文转拼音（小写无空格） */
    private String toPinyin(String chinese) {
        StringBuilder sb = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            String[] arr = net.sourceforge.pinyin4j.PinyinHelper.toHanyuPinyinStringArray(c);
            if (arr != null && arr.length > 0) {
                sb.append(arr[0].replaceAll("\\d", ""));
            } else {
                sb.append(c);
            }
        }
        return sb.toString().toLowerCase().replaceAll("\\s+", "");
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
        // 检查工号是否重复
        Employee exist = employeeService.getOne(new LambdaQueryWrapper<Employee>().eq(Employee::getEmpNo, employee.getEmpNo()));
        if (exist != null) {
            return Result.fail("工号 " + employee.getEmpNo() + " 已存在，请使用其他工号");
        }
        boolean saved = employeeService.save(employee);
        if (saved) {
            // 自动创建登录账号：默认用户名为员工姓名拼音，重复则用工号
            String username = toPinyin(employee.getName());
            User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
            String msg = null;
            if (existUser != null) {
                username = employee.getEmpNo();
                msg = "因用户名重复，该员工账号用户名为其工号 " + username;
            }
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword("123456");
            newUser.setEmployeeId(employee.getId());
            newUser.setStatus(1);
            userMapper.insert(newUser);
            // 判断岗位类型分配角色
            String roleCode = "EMPLOYEE";
            if (employee.getPositionId() != null) {
                Position pos = positionMapper.selectById(employee.getPositionId());
                if (pos != null && "MANAGEMENT".equals(pos.getPositionType())) {
                    roleCode = "MANAGER";
                }
            }
            jdbcTemplate.update("INSERT INTO sys_user_role (user_id, role_id) SELECT ?, id FROM sys_role WHERE role_code = ?", newUser.getId(), roleCode);
            if (msg != null) {
                return Result.ok(msg, true);
            }
        }
        return Result.ok(saved);
    }

    /**
     * 更新员工
     * PUT /api/employee
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Employee employee) {
        // 检查工号是否被其他员工使用
        Employee exist = employeeService.getOne(new LambdaQueryWrapper<Employee>().eq(Employee::getEmpNo, employee.getEmpNo()).ne(Employee::getId, employee.getId()));
        if (exist != null) {
            return Result.fail("工号 " + employee.getEmpNo() + " 已被使用");
        }
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
