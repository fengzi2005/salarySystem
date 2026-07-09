package com.heima.salarymanagesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工龄工资规则实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("seniority_pay_rule")
public class SeniorityPayRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则名称 */
    private String ruleName;

    /** 规则类型：OLD-旧规则, NEW-新规则 */
    private String ruleType;

    /** 第一年工龄工资金额 */
    private BigDecimal firstYearAmount;

    /** 每年递增金额 */
    private BigDecimal incrementPerYear;

    /** 生效日期 */
    private LocalDate effectiveDate;

    /** 失效日期（NULL表示永久有效） */
    private LocalDate expireDate;

    /** 是否启用：1-是, 0-否 */
    private Integer isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
