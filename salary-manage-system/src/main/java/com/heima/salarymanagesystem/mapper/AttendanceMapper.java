package com.heima.salarymanagesystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.salarymanagesystem.entity.Attendance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤 Mapper
 */
@Mapper
public interface AttendanceMapper extends BaseMapper<Attendance> {
}
