package com.heima.salarymanagesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.salarymanagesystem.entity.EmployeeChangeLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工变更日志 Mapper
 */
@Mapper
public interface EmployeeChangeLogMapper extends BaseMapper<EmployeeChangeLog> {
}
