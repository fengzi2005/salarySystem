package com.heima.salarymanagesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.salarymanagesystem.entity.Employee;
import com.heima.salarymanagesystem.entity.EmployeeChangeLog;
import com.heima.salarymanagesystem.mapper.EmployeeChangeLogMapper;
import com.heima.salarymanagesystem.mapper.EmployeeMapper;
import com.heima.salarymanagesystem.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工服务实现
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private final JdbcTemplate jdbcTemplate;
    private final EmployeeChangeLogMapper changeLogMapper;

    @Override
    public Map<String, Object> getEmployeeInfo(Long employeeId) {
        return baseMapper.selectEmployeeInfoById(employeeId);
    }

    @Override
    public List<Map<String, Object>> getEmployeeList(Integer page, Integer pageSize) {
        String sql = "SELECT * FROM v_employee_info ORDER BY dept_id, employee_id LIMIT ? OFFSET ?";
        int offset = (page - 1) * pageSize;
        return jdbcTemplate.queryForList(sql, pageSize, offset);
    }

    /**
     * 获取员工总数
     */
    public long getEmployeeCount() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM v_employee_info", Long.class);
        return count != null ? count : 0;
    }

    /**
     * 按部门分页查询员工（含子部门）
     */
    public List<Map<String, Object>> getEmployeeListByDept(Integer page, Integer pageSize, Long deptId) {
        List<Long> deptIds = getAllSubDeptIds(deptId);
        deptIds.add(deptId);
        String placeholders = deptIds.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("?");
        String sql = "SELECT * FROM v_employee_info WHERE dept_id IN (" + placeholders + ") ORDER BY dept_id, employee_id LIMIT ? OFFSET ?";
        int offset = (page - 1) * pageSize;
        Object[] params = new Object[deptIds.size() + 2];
        for (int i = 0; i < deptIds.size(); i++) params[i] = deptIds.get(i);
        params[deptIds.size()] = pageSize;
        params[deptIds.size() + 1] = offset;
        return jdbcTemplate.queryForList(sql, params);
    }

    public long getEmployeeCountByDept(Long deptId) {
        List<Long> deptIds = getAllSubDeptIds(deptId);
        deptIds.add(deptId);
        String placeholders = deptIds.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("?");
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM v_employee_info WHERE dept_id IN (" + placeholders + ")", Long.class, deptIds.toArray());
        return count != null ? count : 0;
    }

    private List<Long> getAllSubDeptIds(Long parentId) {
        List<Long> ids = new java.util.ArrayList<>();
        String sql = "SELECT id FROM department WHERE parent_id = ?";
        List<Map<String, Object>> children = jdbcTemplate.queryForList(sql, parentId);
        for (Map<String, Object> child : children) {
            Long childId = ((Number) child.get("id")).longValue();
            ids.add(childId);
            ids.addAll(getAllSubDeptIds(childId));
        }
        return ids;
    }

    @Override
    public List<Map<String, Object>> searchEmployees(String keyword, Long deptId, Long positionId) {
        StringBuilder sql = new StringBuilder("SELECT * FROM v_employee_info WHERE 1=1 ");
        List<Object> params = new java.util.ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (employee_name LIKE ? OR emp_no LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (deptId != null) {
            sql.append("AND dept_id = ? ");
            params.add(deptId);
        }
        if (positionId != null) {
            sql.append("AND position_id = ? ");
            params.add(positionId);
        }

        sql.append("ORDER BY dept_id, employee_id");
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }

    @Override
    public List<Map<String, Object>> getActiveEmployees() {
        return jdbcTemplate.queryForList(
                "SELECT id, emp_no, name, department_id, position_id, title_id FROM employee WHERE status = 1 ORDER BY id");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(java.io.Serializable id) {
        Long empId = (Long) id;
        // 级联删除关联数据，防止孤儿记录
        jdbcTemplate.update("DELETE FROM attendance WHERE employee_id = ?", empId);
        jdbcTemplate.update("DELETE FROM monthly_salary WHERE employee_id = ?", empId);
        jdbcTemplate.update("DELETE FROM seniority_pay_record WHERE employee_id = ?", empId);
        jdbcTemplate.update("DELETE FROM employee_change_log WHERE employee_id = ?", empId);
        jdbcTemplate.update("DELETE FROM sys_user WHERE employee_id = ?", empId);
        return super.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePositionOrTitle(Long employeeId, String changeType,
                                       Long oldValueId, Long newValueId,
                                       String oldValueName, String newValueName,
                                       String changeDate, String effectiveDate, String remark) {
        // 更新员工信息
        Employee employee = this.getById(employeeId);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }

        if ("POSITION".equals(changeType)) {
            employee.setPositionId(newValueId);
        } else if ("TITLE".equals(changeType)) {
            employee.setTitleId(newValueId);
        }
        this.updateById(employee);

        // 记录变更日志
        EmployeeChangeLog log = new EmployeeChangeLog();
        log.setEmployeeId(employeeId);
        log.setChangeType(changeType);
        log.setOldValueId(oldValueId);
        log.setNewValueId(newValueId);
        log.setOldValueName(oldValueName);
        log.setNewValueName(newValueName);
        log.setChangeDate(LocalDate.parse(changeDate));
        log.setEffectiveDate(LocalDate.parse(effectiveDate));
        log.setRemark(remark);
        changeLogMapper.insert(log);
    }
}
