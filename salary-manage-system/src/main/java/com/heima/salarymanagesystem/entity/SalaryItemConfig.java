package com.heima.salarymanagesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工资项配置实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("salary_item_config")
public class SalaryItemConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工资项名称 */
    private String itemName;

    /** 工资项编码 */
    private String itemCode;

    /** 类型：SYSTEM-系统自动计算, MANUAL-手动录入 */
    private String itemType;

    /** 是否启用：1-是, 0-否 */
    private Integer isActive;

    /** 排序号 */
    private Integer sortOrder;

    /** 描述 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
