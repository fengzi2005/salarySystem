package com.heima.salarymanagesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 岗位实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("position")
public class Position {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 岗位名称 */
    private String positionName;

    /** 岗位类型：MANAGEMENT-管理岗, TECHNICAL-技术岗 */
    private String positionType;

    /** 岗位工资（统一标准） */
    private BigDecimal positionSalary;

    /** 岗位描述 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
