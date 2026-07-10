package com.heima.salarymanagesystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.salarymanagesystem.entity.Attendance;

import java.util.List;
import java.util.Map;

/**
 * 考勤服务接口
 */
public interface AttendanceService extends IService<Attendance> {

    /**
     * 查询某月所有员工考勤记录
     */
    List<Map<String, Object>> getMonthlyAttendance(Integer year, Integer month);

    /**
     * 查询时间范围内考勤记录
     */
    List<Map<String, Object>> getMonthlyAttendanceRange(Integer startYear, Integer startMonth,
                                                          Integer endYear, Integer endMonth);

    /**
     * 批量导入考勤数据
     */
    void batchImport(List<Attendance> attendanceList);
}
