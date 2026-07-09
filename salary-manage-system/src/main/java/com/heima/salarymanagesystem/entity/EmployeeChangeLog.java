package com.heima.salarymanagesystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工岗位/职称变更记录实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("employee_change_log")
public class EmployeeChangeLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 员工ID */
    private Long employeeId;

    /** 变更类型：POSITION-岗位变更, TITLE-职称变更 */
    private String changeType;

    /** 变更前ID */
    private Long oldValueId;

    /** 变更后ID */
    private Long newValueId;

    /** 变更前名称 */
    private String oldValueName;

    /** 变更后名称 */
    private String newValueName;

    /** 变更日期 */
    private LocalDate changeDate;

    /** 生效日期 */
    private LocalDate effectiveDate;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
