package com.heima.salarymanagesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("employee")
public class Employee {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 员工编号 */
    private String empNo;

    /** 员工姓名 */
    private String name;

    /** 性别：1-男, 2-女 */
    private Integer gender;

    /** 所属部门ID */
    private Long departmentId;

    /** 岗位ID */
    private Long positionId;

    /** 职称ID */
    private Long titleId;

    /** 入职日期（工龄工资核算依据） */
    private LocalDate entryDate;

    /** 基本工资（员工个人专属基础底薪，独立核算） */
    private BigDecimal baseSalary;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态：1-在职, 0-离职 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
