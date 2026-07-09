package com.heima.salarymanagesystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.salarymanagesystem.entity.Employee;

import java.util.List;
import java.util.Map;

/**
 * 员工服务接口
 */
public interface EmployeeService extends IService<Employee> {

    /**
     * 通过视图查询员工完整信息
     */
    Map<String, Object> getEmployeeInfo(Long employeeId);

    /**
     * 分页查询员工列表（含部门、岗位、职称信息）
     */
    List<Map<String, Object>> getEmployeeList(Integer page, Integer pageSize);

    /**
     * 根据条件搜索员工
     */
    List<Map<String, Object>> searchEmployees(String keyword, Long deptId, Long positionId);

    /**
     * 查询所有在职员工
     */
    List<Map<String, Object>> getActiveEmployees();

    /**
     * 变更员工岗位或职称（含历史记录）
     */
    void changePositionOrTitle(Long employeeId, String changeType,
                               Long oldValueId, Long newValueId,
                               String oldValueName, String newValueName,
                               String changeDate, String effectiveDate, String remark);
}
