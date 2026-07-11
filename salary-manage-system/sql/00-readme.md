# 工资管理系统 - 数据库说明文档

## 一、环境要求

| 项目 | 版本/配置 |
|------|----------|
| MySQL | 8.0+ |
| 字符集 | utf8mb4 / utf8mb4_unicode_ci |
| 存储引擎 | InnoDB |
| 最低内存 | 2GB |
| 端口 | 3306 |

## 二、快速部署

### 2.1 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS salary_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 2.2 导入脚本（按顺序执行）

```bash
# 方式一：命令行
mysql -h 主机 -u 用户名 -p --default-character-set=utf8mb4 < 01-schema.sql
mysql -h 主机 -u 用户名 -p --default-character-set=utf8mb4 salary_system < 02-views-triggers-procedures.sql
mysql -h 主机 -u 用户名 -p --default-character-set=utf8mb4 salary_system < 03-init-data.sql

# 方式二：Windows 用户双击
init-db.bat

# 方式三：Linux/Mac 用户运行
bash init-db.sh
```

### 2.3 开启定时任务（可选）

```sql
SET GLOBAL event_scheduler = ON;
SHOW VARIABLES LIKE 'event_scheduler';  -- 确认 ON
```

## 三、数据表清单（15 张）

| 序号 | 表名 | 中文名 | 用途 | 数据量(演示) |
|:--:|------|--------|------|:--:|
| 1 | sys_role | 角色表 | 系统角色定义 | 3 |
| 2 | sys_user | 用户表 | 登录账号 | 26 |
| 3 | sys_user_role | 用户角色关联 | 多对多关联 | 26 |
| 4 | department | 部门表 | 三级树形组织架构 | 8 |
| 5 | position | 岗位表 | 管理岗/技术岗定义 | 10 |
| 6 | title | 职称表 | 职级工资标准 | 5 |
| 7 | employee | 员工表 | 核心人事数据 | 27 |
| 8 | attendance | 考勤表 | 月度考勤+扣款 | 60+ |
| 9 | salary_item_config | 工资项配置 | 工资项启停管理 | 12 |
| 10 | monthly_salary | 月度工资主表 | 每月每人一条 | 400+ |
| 11 | monthly_salary_detail | 工资明细表 | 主表拆分 | — |
| 12 | seniority_pay_rule | 工龄工资规则 | 新旧规则定义 | 2 |
| 13 | seniority_pay_record | 工龄工资记录 | 每人每月工龄 | 400+ |
| 14 | employee_change_log | 员工变更日志 | 岗位/职称变动 | — |
| 15 | schedule_job_log | 定时任务日志 | 自动计算记录 | — |

## 四、各表字段说明

### 4.1 sys_role（角色表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK AUTO_INCREMENT | 主键 |
| role_code | varchar(50) | UNIQUE NOT NULL | 角色编码：ADMIN/MANAGER/EMPLOYEE |
| role_name | varchar(50) | NOT NULL | 角色名称 |
| description | varchar(255) | | 描述 |

### 4.2 sys_user（用户表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK AUTO_INCREMENT | 主键 |
| username | varchar(50) | UNIQUE NOT NULL | 登录名（员工姓名拼音） |
| password | varchar(255) | NOT NULL | 密码 |
| employee_id | bigint | FK→employee.id | 关联员工 |
| status | tinyint | DEFAULT 1 | 1启用 0禁用 |

### 4.3 department（部门表，自关联树形结构）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK AUTO_INCREMENT | 主键 |
| dept_name | varchar(100) | NOT NULL | 部门名称 |
| parent_id | bigint | FK→department.id | 上级部门，0=根 |
| dept_level | tinyint | NOT NULL | 1总部 2一级部门 3分部 |
| manager_id | bigint | FK→employee.id | 部门负责人 |
| sort_order | int | DEFAULT 0 | 排序号 |
| description | varchar(255) | | 描述 |

### 4.4 position（岗位表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK AUTO_INCREMENT | 主键 |
| position_name | varchar(100) | NOT NULL | 岗位名称 |
| position_type | varchar(20) | NOT NULL | MANAGEMENT管理岗 / TECHNICAL技术岗 |
| position_salary | decimal(10,2) | NOT NULL | 岗位工资（统一标准） |
| description | varchar(255) | | 描述 |

### 4.5 title（职称表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK AUTO_INCREMENT | 主键 |
| title_name | varchar(100) | NOT NULL | 正高级/副高级/中级/初级/员级 |
| title_salary | decimal(10,2) | NOT NULL | 职级工资（统一标准） |
| description | varchar(255) | | 描述 |

### 4.6 employee（员工表）⭐核心

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK AUTO_INCREMENT | 主键 |
| emp_no | varchar(20) | UNIQUE NOT NULL | 工号，FS开头+7位数字 |
| name | varchar(50) | NOT NULL | 姓名 |
| gender | tinyint | DEFAULT 1 | 1男 2女 |
| department_id | bigint | FK→department.id | 所属部门 |
| position_id | bigint | FK→position.id | 岗位 |
| title_id | bigint | FK→title.id | 职称 |
| entry_date | date | NOT NULL | **入职日期（工龄计算唯一依据）** |
| base_salary | decimal(10,2) | NOT NULL | **基本工资（个人独立，≠岗位工资）** |
| phone | varchar(20) | | 联系电话 |
| email | varchar(100) | | 邮箱 |
| status | tinyint | DEFAULT 1 | 1在职 0离职 |
| create_time | datetime | | 创建时间 |
| update_time | datetime | | 更新时间（自动） |

### 4.7 attendance（考勤表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK AUTO_INCREMENT | 主键 |
| employee_id | bigint | FK→employee.id | 员工 |
| attendance_year | int | NOT NULL | 年份 |
| attendance_month | int | NOT NULL | 月份 |
| leave_days | int | DEFAULT 0 | 事假天数 |
| late_times | int | DEFAULT 0 | 迟到次数 |
| absent_days | int | DEFAULT 0 | 旷工天数 |
| is_full_attendance | tinyint | DEFAULT 1 | 是否全勤（触发器计算） |
| leave_deduction | decimal(10,2) | DEFAULT 0 | 事假扣款 100元/天（触发器计算） |
| late_deduction | decimal(10,2) | DEFAULT 0 | 迟到扣款 50元/次（触发器计算） |
| absent_deduction | decimal(10,2) | DEFAULT 0 | 旷工扣款 300元/天（触发器计算） |
| full_attendance_bonus | decimal(10,2) | DEFAULT 0 | 全勤奖金 300元/月（触发器计算） |
| UNIQUE | | (employee_id, year, month) | 每人每月唯一 |

### 4.8 salary_item_config（工资项配置表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK AUTO_INCREMENT | 主键 |
| item_name | varchar(100) | NOT NULL | 工资项名称 |
| item_code | varchar(50) | UNIQUE NOT NULL | 系统编码 |
| item_type | varchar(20) | NOT NULL | SYSTEM系统自动 / MANUAL手动录入 |
| is_active | tinyint | DEFAULT 1 | **启停开关** |

### 4.9 monthly_salary（月度工资主表）⭐核心

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint PK | 主键 |
| employee_id | bigint FK | 员工 |
| salary_year | int | 年份 |
| salary_month | int | 月份 |
| **系统自动计算项** | | |
| base_salary | decimal | ①基本工资 |
| position_salary | decimal | ②岗位工资 |
| title_salary | decimal | ③职级工资 |
| seniority_pay | decimal | ④工龄工资 |
| full_attendance_bonus | decimal | ⑤全勤奖金 |
| leave_deduction | decimal | ⑥事假扣款 |
| late_deduction | decimal | ⑦迟到扣款 |
| absent_deduction | decimal | ⑧旷工扣款 |
| **手动录入项** | | |
| project_bonus | decimal | ⑨项目奖金 |
| tax_deduction | decimal | ⑩个税扣除 |
| other_additions | decimal | ⑪其他加项 |
| other_deductions | decimal | ⑫其他减项 |
| **汇总** | | |
| gross_salary | decimal | 应发 = ①+②+③+④+⑤+⑨+⑪ |
| total_deduction | decimal | 扣款 = ⑥+⑦+⑧+⑩+⑫ |
| net_salary | decimal | **实发 = 应发 - 扣款** |
| status | varchar(20) | DRAFT草稿 / CONFIRMED已确认(锁定) |
| UNIQUE | | (employee_id, year, month) |

### 4.10 seniority_pay_rule（工龄工资规则表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK AUTO_INCREMENT | 主键 |
| rule_name | varchar(100) | NOT NULL | 旧/新工龄工资规则 |
| rule_type | varchar(20) | | OLD旧规则(每年+300) / NEW新规则(第2年+100) |
| first_year_amount | decimal(10,2) | | 第一年工龄工资金额 |
| increment_per_year | decimal(10,2) | | 每年递增金额 |
| effective_date | date | | 生效日期 |
| expire_date | date | | 失效日期 |
| is_active | tinyint | | **当前启用（同时只有一个）** |

## 五、索引设计

详见 `04-database-design-doc.md`

## 六、视图

### v_employee_info
关联 employee + department + position + title，一次性获取员工完整信息。

### v_department_info
关联 department + employee，包含负责人姓名和下属员工数量统计。

## 七、触发器

### trg_attendance_before_insert / trg_attendance_before_update
考勤数据写入/修改前自动计算扣款和全勤奖金。

## 八、存储过程（8个）

| 过程名 | 功能 |
|------|------|
| sp_calc_monthly_salary | 月度工资自动计算 |
| sp_confirm_salary | 确认工资（锁定） |
| sp_query_employee_salary | 按员工查询工资 |
| sp_query_department_salary | 按部门查询工资 |
| sp_salary_structure_report | 薪资结构分析报表 |
| sp_department_salary_stats | 部门薪资统计 |
| sp_employee_annual_stats | 员工年度统计 |
| sp_salary_query | 多条件通用查询 |

## 九、演示数据

| 数据类别 | 数量 | 说明 |
|---------|:--:|------|
| 角色 | 3 | 管理员/管理人员/普通员工 |
| 用户 | 26 | 含 admin 和各岗位人员 |
| 部门 | 8 | 1总部 + 3一级部门 + 4分部 |
| 员工 | 27 | 在职，分布在各层级 |
| 岗位 | 10 | 管理岗4 + 技术岗6 |
| 职称 | 5 | 正高级→员级 |
| 考勤 | 60+ | 跨多月份 |

**演示登录：**
- admin / 123456（系统管理员）
- sunqi / 123456（总经理）
- lisi / 123456（分部经理）
- zhoumingyuan / 123456（普通员工）
