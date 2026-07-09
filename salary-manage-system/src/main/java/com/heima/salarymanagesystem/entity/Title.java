package com.heima.salarymanagesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 职称（职级）实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("title")
public class Title {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 职称名称 */
    private String titleName;

    /** 职级工资（统一标准） */
    private BigDecimal titleSalary;

    /** 职称描述 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
