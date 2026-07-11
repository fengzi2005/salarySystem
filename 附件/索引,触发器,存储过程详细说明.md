# 工资管理系统 - 数据库设计说明

## 一、索引设计

### 1.1 唯一索引（UNIQUE KEY）

| 表 | 索引列 | 原因 |
|------|------|------|
| employee | emp_no | 工号必须唯一，防止重复录入 |
| sys_user | username | 登录账号唯一 |
| sys_user_role | (user_id, role_id) | 同一用户不能重复分配同角色 |
| attendance | (employee_id, attendance_year, attendance_month) | 每人每月只有一条考勤记录 |
| monthly_salary | (employee_id, salary_year, salary_month) | 每人每月只有一条工资记录 |
| seniority_pay_record | (employee_id, salary_year, salary_month) | 每人每月一条工龄记录 |
| sys_role | role_code | 角色编码唯一 |
| salary_item_config | item_code | 工资项编码唯一 |

### 1.2 普通索引（KEY）

| 表 | 索引列 | 原因 |
|------|------|------|
| employee | department_id | 按部门查员工是最频繁的操作 |
| employee | position_id | 按岗位筛选 |
| employee | title_id | 按职称筛选 |
| employee | entry_date | 工龄计算依赖入职日期排序 |
| employee | name | 姓名搜索（高频） |
| employee | status | 在职/离职过滤（高频） |
| department | parent_id | 多级部门树递归查询 |
| department | dept_level | 按层级筛选 |
| attendance | (attendance_year, attendance_month) | 按月查考勤（高频） |
| monthly_salary | (salary_year, salary_month) | 按月查工资（高频） |
| monthly_salary | status | 区分草稿/已确认 |
| seniority_pay_record | (salary_year, salary_month) | 按月查工龄 |
| schedule_job_log | job_name | 按任务名查日志 |
| schedule_job_log | execute_time | 按时间排序 |
| sys_user | employee_id | 登录时关联员工信息 |
| employee_change_log | employee_id | 查某员工的变更记录 |
| employee_change_log | change_date | 按日期查变更 |
| monthly_salary_detail | monthly_salary_id | 关联查询工资明细 |

---

## 二、触发器设计

### 2.1 trg_attendance_before_insert / trg_attendance_before_update

**用途**：录入/修改考勤数据时，自动计算缺勤扣款和全勤奖金。

**触发时机**：INSERT 或 UPDATE 到 attendance 表之前。

**计算逻辑**：
```sql
事假扣款 = 事假天数 × 100元
迟到扣款 = 迟到次数 × 50元
旷工扣款 = 旷工天数 × 300元
全勤判定 = 无事假 AND 无迟到 AND 无旷工
全勤奖金 = 全勤 ? 300元 : 0元
```

**设计原因**：
- 计算规则固定，无需应用层参与，数据和计算在数据库端耦合，保证一致性
- 前端只需录入天数/次数，扣款金额自动呈现
- 修改考勤时自动重算，无需手动更新扣款

---

## 三、存储过程设计

### 3.1 sp_calc_monthly_salary(年, 月)

**用途**：根据所有在职员工的基础数据，自动计算该月的应发工资、扣款合计、实发工资，写入 monthly_salary 表。

**计算流程**：
```
遍历所有在职员工（入职日期 ≤ 该月最后一天）
  ├── 取基本工资、岗位工资、职级工资
  ├── 按员工入职日期和活跃的工龄规则计算工龄工资
  ├── 从考勤表取扣款和全勤奖金
  ├── 从工资项配置表读取启停状态，停用的项置零
  ├── 保留已存在的手动录入项（项目奖金、个税等）
  ├── 应发 = 基本+岗位+职级+工龄+全勤+项目奖金+其他加项
  ├── 扣款 = 事假+迟到+旷工+个税+其他减项
  └── 实发 = 应发 - 扣款
```

**设计原因**：
- 计算逻辑集中在数据库端，避免多次网络往返
- 26名员工的计算在存储过程中一次性完成，性能优于Java逐条处理
- 使用游标遍历 + ON DUPLICATE KEY UPDATE 实现幂等计算
- 工资项配置联动：停用的项目在计算时自动置零

**注意事项**：
- 已确认(CONFIRMED)的记录会被跳过，不会覆盖
- 工龄工资根据活跃规则自动切换新旧公式

### 3.2 sp_confirm_salary(年, 月)

**用途**：将该月所有草稿工资标记为已确认，锁定数据。

**设计原因**：确认后数据不可修改，保证财务数据的严肃性和可审计性。

### 3.3 sp_query_employee_salary(员工ID, 年, 月)

**用途**：按员工+月份查询工资明细，支持权限过滤。

### 3.4 sp_query_department_salary(部门ID, 年, 月)

**用途**：按部门查询该月所有员工工资列表。

### 3.5 sp_salary_query(年, 月, 部门ID, 姓名, 工号)

**用途**：多条件组合查询工资数据，支持灵活的筛选条件。

### 3.6 sp_salary_structure_report(年, 月)

**用途**：按部门汇总各项工资的合计及占比，生成薪资结构分析报表。

### 3.7 sp_department_salary_stats(年, 月)

**用途**：按部门统计最高工资、最低工资、平均工资、工资总额。

### 3.8 sp_employee_annual_stats(员工ID, 年)

**用途**：统计单个员工全年度的月均、最高、最低、总工资。

---

## 四、视图设计

### 4.1 v_employee_info

**用途**：关联员工、部门、岗位、职称，一键获取员工完整信息。

**设计原因**：
- 避免前端多次请求不同表的数据
- 计算工龄字段直接在视图中完成

### 4.2 v_department_info

**用途**：关联部门与负责人，统计各部下辖员工数。

---

## 五、定时事件

### evt_monthly_salary_init

**用途**：每月1号凌晨自动计算上月工资。

**默认配置**：每月10号 06:00（可通过工龄工资规则页面前端调整）

**设计原因**：
- 工资计算不宜频繁（存储过程执行时间约1-2秒）
- 凌晨执行避免影响用户操作
- 可通过前端界面灵活调整触发时间
- 支持开关控制（ENABLE/DISABLE）
