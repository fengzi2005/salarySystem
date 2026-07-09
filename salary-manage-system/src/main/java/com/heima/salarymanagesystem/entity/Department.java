package com.heima.salarymanagesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 部门实体（多级组织架构）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("department")
public class Department {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 部门名称 */
    private String deptName;

    /** 上级部门ID，0表示总部 */
    private Long parentId;

    /** 层级：1-总部, 2-一级部门, 3-分部 */
    private Integer deptLevel;

    /** 部门负责人ID（关联employee.id） */
    private Long managerId;

    /** 排序号 */
    private Integer sortOrder;

    /** 部门描述 */
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
