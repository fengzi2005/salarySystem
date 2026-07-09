package com.heima.salarymanagesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 月度工资主表实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("monthly_salary")
public class MonthlySalary {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 员工ID */
    private Long employeeId;

    /** 年份 */
    private Integer salaryYear;

    /** 月份 */
    private Integer salaryMonth;

    /** 基本工资（员工个人底薪） */
    private BigDecimal baseSalary;

    /** 岗位工资 */
    private BigDecimal positionSalary;

    /** 职级工资 */
    private BigDecimal titleSalary;

    /** 工龄工资 */
    private BigDecimal seniorityPay;

    /** 全勤奖金 */
    private BigDecimal fullAttendanceBonus;

    /** 事假扣款 */
    private BigDecimal leaveDeduction;

    /** 迟到扣款 */
    private BigDecimal lateDeduction;

    /** 旷工扣款 */
    private BigDecimal absentDeduction;

    /** 项目奖金 */
    private BigDecimal projectBonus;

    /** 个税扣除 */
    private BigDecimal taxDeduction;

    /** 其他加项 */
    private BigDecimal otherAdditions;

    /** 其他减项 */
    private BigDecimal otherDeductions;

    /** 应发工资合计 */
    private BigDecimal grossSalary;

    /** 扣款合计 */
    private BigDecimal totalDeduction;

    /** 实发工资 */
    private BigDecimal netSalary;

    /** 状态：DRAFT-草稿, CONFIRMED-已确认 */
    private String status;

    /** 确认时间 */
    private LocalDateTime confirmTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
