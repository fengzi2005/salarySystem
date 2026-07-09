package com.heima.salarymanagesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.salarymanagesystem.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 员工 Mapper
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

    /**
     * 通过视图查询员工完整信息（含部门、岗位、职称）
     */
    @Select("SELECT * FROM v_employee_info WHERE employee_id = #{employeeId}")
    Map<String, Object> selectEmployeeInfoById(Long employeeId);

    /**
     * 根据用户名/工号查询员工
     */
    @Select("SELECT * FROM employee WHERE emp_no = #{username} AND status = 1")
    Employee selectByEmpNo(String username);

    /**
     * 查询用户的员工ID
     */
    @Select("SELECT employee_id FROM sys_user WHERE id = #{userId}")
    Long selectEmployeeIdByUserId(Long userId);
}
