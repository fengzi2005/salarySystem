package com.heima.salarymanagesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考勤实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("attendance")
public class Attendance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 员工ID */
    private Long employeeId;

    /** 年份 */
    private Integer attendanceYear;

    /** 月份 */
    private Integer attendanceMonth;

    /** 事假天数 */
    private Integer leaveDays;

    /** 迟到次数 */
    private Integer lateTimes;

    /** 旷工天数 */
    private Integer absentDays;

    /** 是否全勤：1-是, 0-否 */
    private Integer isFullAttendance;

    /** 事假扣款（触发器自动计算） */
    private BigDecimal leaveDeduction;

    /** 迟到扣款（触发器自动计算） */
    private BigDecimal lateDeduction;

    /** 旷工扣款（触发器自动计算） */
    private BigDecimal absentDeduction;

    /** 全勤奖金（触发器自动计算） */
    private BigDecimal fullAttendanceBonus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
