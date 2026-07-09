package com.heima.salarymanagesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.salarymanagesystem.entity.Attendance;
import com.heima.salarymanagesystem.mapper.AttendanceMapper;
import com.heima.salarymanagesystem.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 考勤服务实现
 * 考勤数据的扣款/奖金由数据库触发器自动计算
 */
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl extends ServiceImpl<AttendanceMapper, Attendance> implements AttendanceService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getMonthlyAttendance(Integer year, Integer month) {
        String sql = "SELECT a.*, e.name AS employee_name, e.emp_no, d.dept_name " +
                     "FROM attendance a " +
                     "LEFT JOIN employee e ON a.employee_id = e.id " +
                     "LEFT JOIN department d ON e.department_id = d.id " +
                     "WHERE a.attendance_year = ? AND a.attendance_month = ? " +
                     "ORDER BY d.id, e.id";
        return jdbcTemplate.queryForList(sql, year, month);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchImport(List<Attendance> attendanceList) {
        // 逐条插入，触发器会自动计算扣款
        for (Attendance attendance : attendanceList) {
            this.save(attendance);
        }
    }
}
