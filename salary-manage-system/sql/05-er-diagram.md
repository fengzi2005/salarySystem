# 工资管理系统 - E-R 概念模型

```mermaid
erDiagram
    sys_role ||--o{ sys_user_role : "1"
    sys_user ||--o{ sys_user_role : "1"
    sys_user_role }o--|| sys_role : ""
    sys_user_role }o--|| sys_user : ""

    sys_user }o--o| employee : "可关联"

    department ||--o{ department : "parent"
    department ||--o{ employee : ""

    position ||--o{ employee : ""
    title ||--o{ employee : ""

    employee ||--o{ monthly_salary : ""
    employee ||--o{ attendance : ""
    employee ||--o{ seniority_pay_record : ""
    employee ||--o{ employee_change_log : ""

    seniority_pay_rule ||--o{ seniority_pay_record : ""

    monthly_salary ||--o{ monthly_salary_detail : ""

    sys_role {
        bigint id PK "角色ID"
        varchar role_code UK "ADMIN|MANAGER|EMPLOYEE"
        varchar role_name "角色名称"
    }

    sys_user {
        bigint id PK "用户ID"
        varchar username UK "登录名"
        varchar password "密码"
        bigint employee_id FK "关联员工"
        tinyint status "1启用 0禁用"
    }

    sys_user_role {
        bigint user_id FK "用户ID"
        bigint role_id FK "角色ID"
    }

    department {
        bigint id PK "部门ID"
        varchar dept_name "部门名称"
        bigint parent_id FK "上级部门"
        tinyint dept_level "1总部 2一级 3分部"
        bigint manager_id FK "负责人"
    }

    position {
        bigint id PK "岗位ID"
        varchar position_name "岗位名称"
        varchar position_type "MANAGEMENT|TECHNICAL"
        decimal position_salary "岗位工资"
    }

    title {
        bigint id PK "职称ID"
        varchar title_name "职称名称"
        decimal title_salary "职级工资"
    }

    employee {
        bigint id PK "员工ID"
        varchar emp_no UK "工号 FS+7位"
        varchar name "姓名"
        tinyint gender "1男 2女"
        bigint department_id FK "部门"
        bigint position_id FK "岗位"
        bigint title_id FK "职称"
        date entry_date "入职日期"
        decimal base_salary "基本工资"
        varchar phone "电话"
        varchar email "邮箱"
        tinyint status "1在职 0离职"
    }

    attendance {
        bigint id PK "考勤ID"
        bigint employee_id FK "员工"
        int attendance_year "年份"
        int attendance_month "月份"
        int leave_days "事假天数"
        int late_times "迟到次数"
        int absent_days "旷工天数"
        tinyint is_full_attendance "是否全勤"
        decimal leave_deduction "事假扣款"
        decimal late_deduction "迟到扣款"
        decimal absent_deduction "旷工扣款"
        decimal full_attendance_bonus "全勤奖金"
    }

    salary_item_config {
        bigint id PK "配置ID"
        varchar item_name "工资项名称"
        varchar item_code UK "编码"
        varchar item_type "SYSTEM|MANUAL"
        tinyint is_active "启用开关"
    }

    monthly_salary {
        bigint id PK "工资ID"
        bigint employee_id FK "员工"
        int salary_year "年份"
        int salary_month "月份"
        decimal base_salary "基本工资"
        decimal position_salary "岗位工资"
        decimal title_salary "职级工资"
        decimal seniority_pay "工龄工资"
        decimal full_attendance_bonus "全勤奖金"
        decimal leave_deduction "事假扣款"
        decimal late_deduction "迟到扣款"
        decimal absent_deduction "旷工扣款"
        decimal project_bonus "项目奖金"
        decimal tax_deduction "个税扣除"
        decimal other_additions "其他加项"
        decimal other_deductions "其他减项"
        decimal gross_salary "应发合计"
        decimal total_deduction "扣款合计"
        decimal net_salary "实发工资"
        varchar status "DRAFT|CONFIRMED"
    }

    monthly_salary_detail {
        bigint id PK "明细ID"
        bigint monthly_salary_id FK "工资主表"
        varchar item_name "工资项名"
        decimal item_amount "金额"
    }

    seniority_pay_rule {
        bigint id PK "规则ID"
        varchar rule_name "规则名称"
        varchar rule_type "OLD|NEW"
        decimal first_year_amount "首年金"
        decimal increment_per_year "年递增"
        date effective_date "生效日期"
        tinyint is_active "是否启用"
    }

    seniority_pay_record {
        bigint id PK "工龄记录ID"
        bigint employee_id FK "员工"
        int salary_year "年份"
        int salary_month "月份"
        int seniority_years "工龄年数"
        decimal seniority_amount "工龄工资金额"
        bigint rule_id FK "使用规则"
    }

    employee_change_log {
        bigint id PK "日志ID"
        bigint employee_id FK "员工"
        varchar change_type "POSITION|TITLE"
        bigint old_value_id "变更前ID"
        bigint new_value_id "变更后ID"
        date change_date "变更日期"
    }

    schedule_job_log {
        bigint id PK "日志ID"
        varchar job_name "任务名称"
        datetime execute_time "执行时间"
        varchar status "SUCCESS|FAIL"
        text message "执行信息"
    }
```

## 实体关系说明

| 关系 | 类型 | 说明 |
|------|------|------|
| 用户 → 角色 | 多对多 | 一个用户可有多个角色 |
| 部门 → 部门 | 自引用一对多 | parent_id 自关联，支持多级树 |
| 部门 → 员工 | 一对多 | 一个部门有多个员工 |
| 岗位 → 员工 | 一对多 | 一个岗位可分配给多个员工 |
| 职称 → 员工 | 一对多 | 一个职称可分配给多个员工 |
| 员工 → 月度工资 | 一对多 | 一个员工每月一条工资记录 |
| 员工 → 考勤 | 一对多 | 一个员工每月一条考勤记录 |
| 员工 → 工龄工资 | 一对多 | 一个员工每月一条工龄记录 |
| 员工 → 变更日志 | 一对多 | 岗位/职称变更的历史记录 |
| 工龄规则 → 工龄记录 | 一对多 | 每次计算关联使用的规则 |
| 月度工资 → 工资明细 | 一对多 | 主表拆分明细项 |

## 业务约束

- **考勤唯一**：同一员工同一年月只能有一条考勤
- **工资唯一**：同一员工同一年月只能有一条工资记录
- **工龄唯一**：同一员工同一年月只能有一条工龄记录
- **已确认锁定**：工资确认后不可修改（存储过程跳过）
- **部门级联**：删除部门前检查子部门和员工
- **员工级联**：删除员工自动清理关联数据
