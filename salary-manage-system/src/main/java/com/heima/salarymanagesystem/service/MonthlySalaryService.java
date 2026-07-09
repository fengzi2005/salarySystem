package com.heima.salarymanagesystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.salarymanagesystem.entity.MonthlySalary;

import java.util.List;
import java.util.Map;

/**
 * 月度工资服务接口
 */
public interface MonthlySalaryService extends IService<MonthlySalary> {

    /**
     * 调用存储过程 - 自动计算月度工资
     */
    void calculateMonthlySalary(Integer year, Integer month);

    /**
     * 调用存储过程 - 确认工资（锁定数据）
     */
    void confirmSalary(Integer year, Integer month);

    /**
     * 按员工查询工资明细（权限：管理员看全部，员工看自己，管理者看本部门）
     */
    List<Map<String, Object>> queryEmployeeSalary(Long employeeId, Integer year, Integer month);

    /**
     * 按部门查询工资汇总
     */
    List<Map<String, Object>> queryDepartmentSalary(Long departmentId, Integer year, Integer month);

    /**
     * 薪资结构分析报表
     */
    List<Map<String, Object>> salaryStructureReport(Integer year, Integer month);

    /**
     * 部门薪资统计报表
     */
    List<Map<String, Object>> departmentSalaryStats(Integer year, Integer month);

    /**
     * 员工年度薪资统计
     */
    List<Map<String, Object>> employeeAnnualStats(Long employeeId, Integer year);

    /**
     * 通用薪资查询（多条件筛选）
     */
    List<Map<String, Object>> salaryQuery(Integer year, Integer month, Long deptId,
                                           String employeeName, String empNo);

    /**
     * 更新手动录入项（仅草稿状态可修改，已确认不可修改）
     */
    void updateManualItems(Long salaryId, Map<String, Object> manualItems);
}
