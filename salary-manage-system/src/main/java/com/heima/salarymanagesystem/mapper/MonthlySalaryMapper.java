package com.heima.salarymanagesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.salarymanagesystem.entity.MonthlySalary;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;
import java.util.Map;

/**
 * 月度工资 Mapper
 * 核心查询通过存储过程实现
 */
@Mapper
public interface MonthlySalaryMapper extends BaseMapper<MonthlySalary> {

    /**
     * 调用存储过程 - 月度工资自动计算
     */
    @Update("CALL sp_calc_monthly_salary(#{year}, #{month})")
    void callCalcMonthlySalary(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * 调用存储过程 - 工资确认（锁定）
     */
    @Update("CALL sp_confirm_salary(#{year}, #{month})")
    void callConfirmSalary(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * 调用存储过程 - 按员工查询工资
     */
    @Select("CALL sp_query_employee_salary(#{employeeId}, #{year}, #{month})")
    @Options(statementType = StatementType.CALLABLE)
    List<Map<String, Object>> callQueryEmployeeSalary(@Param("employeeId") Long employeeId,
                                                       @Param("year") Integer year,
                                                       @Param("month") Integer month);

    /**
     * 调用存储过程 - 按部门查询工资汇总
     */
    @Select("CALL sp_query_department_salary(#{departmentId}, #{year}, #{month})")
    @Options(statementType = StatementType.CALLABLE)
    List<Map<String, Object>> callQueryDepartmentSalary(@Param("departmentId") Long departmentId,
                                                         @Param("year") Integer year,
                                                         @Param("month") Integer month);

    /**
     * 调用存储过程 - 薪资结构报表
     */
    @Select("CALL sp_salary_structure_report(#{year}, #{month})")
    @Options(statementType = StatementType.CALLABLE)
    List<Map<String, Object>> callSalaryStructureReport(@Param("year") Integer year,
                                                         @Param("month") Integer month);

    /**
     * 调用存储过程 - 部门薪资统计
     */
    @Select("CALL sp_department_salary_stats(#{year}, #{month})")
    @Options(statementType = StatementType.CALLABLE)
    List<Map<String, Object>> callDepartmentSalaryStats(@Param("year") Integer year,
                                                         @Param("month") Integer month);

    /**
     * 调用存储过程 - 员工年度薪资统计
     */
    @Select("CALL sp_employee_annual_stats(#{employeeId}, #{year})")
    @Options(statementType = StatementType.CALLABLE)
    List<Map<String, Object>> callEmployeeAnnualStats(@Param("employeeId") Long employeeId,
                                                       @Param("year") Integer year);

    /**
     * 调用存储过程 - 通用薪资查询
     */
    @Select("CALL sp_salary_query(#{year}, #{month}, #{deptId}, #{employeeName}, #{empNo})")
    @Options(statementType = StatementType.CALLABLE)
    List<Map<String, Object>> callSalaryQuery(@Param("year") Integer year,
                                               @Param("month") Integer month,
                                               @Param("deptId") Long deptId,
                                               @Param("employeeName") String employeeName,
                                               @Param("empNo") String empNo);
}
