-- ============================================================
-- 工资管理系统 - 数据库初始化脚本
-- 版本：1.0.0
-- 说明：包含所有建表语句、视图、触发器、存储过程、定时任务
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS salary_system
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE salary_system;

-- ============================================================
-- 第一部分：建表语句
-- ============================================================

-- 1. 系统角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码：ADMIN-管理员, MANAGER-管理人员, EMPLOYEE-普通员工',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='系统角色表';

-- 2. 系统用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    employee_id BIGINT COMMENT '关联员工ID',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用, 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='系统用户表';

-- 3. 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- 4. 部门表（多级组织架构：总部-一级部门-分部）
DROP TABLE IF EXISTS department;
CREATE TABLE department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '上级部门ID，0表示总部',
    dept_level TINYINT NOT NULL DEFAULT 1 COMMENT '层级：1-总部, 2-一级部门, 3-分部',
    manager_id BIGINT COMMENT '部门负责人ID（关联employee.id）',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    description VARCHAR(255) COMMENT '部门描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_dept_level (dept_level)
) ENGINE=InnoDB COMMENT='部门表（多级组织架构）';

-- 5. 岗位表
DROP TABLE IF EXISTS position;
CREATE TABLE position (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '岗位ID',
    position_name VARCHAR(100) NOT NULL COMMENT '岗位名称',
    position_type VARCHAR(20) NOT NULL COMMENT '岗位类型：MANAGEMENT-管理岗, TECHNICAL-技术岗',
    position_salary DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '岗位工资（统一标准）',
    description VARCHAR(255) COMMENT '岗位描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='岗位表';

-- 6. 职称表（职级）
DROP TABLE IF EXISTS title;
CREATE TABLE title (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '职称ID',
    title_name VARCHAR(100) NOT NULL COMMENT '职称名称',
    title_salary DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '职级工资（统一标准）',
    description VARCHAR(255) COMMENT '职称描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='职称表';

-- 7. 员工表
DROP TABLE IF EXISTS employee;
CREATE TABLE employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '员工ID',
    emp_no VARCHAR(20) NOT NULL UNIQUE COMMENT '员工编号',
    name VARCHAR(50) NOT NULL COMMENT '员工姓名',
    gender TINYINT DEFAULT 1 COMMENT '性别：1-男, 2-女',
    department_id BIGINT COMMENT '所属部门ID',
    position_id BIGINT COMMENT '岗位ID',
    title_id BIGINT COMMENT '职称ID',
    entry_date DATE NOT NULL COMMENT '入职日期（工龄工资核算依据）',
    base_salary DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '基本工资（员工个人专属基础底薪，独立核算）',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：1-在职, 0-离职',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_department (department_id),
    INDEX idx_position (position_id),
    INDEX idx_title (title_id),
    INDEX idx_entry_date (entry_date)
) ENGINE=InnoDB COMMENT='员工表';

-- 8. 员工岗位/职称变更记录表
DROP TABLE IF EXISTS employee_change_log;
CREATE TABLE employee_change_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型：POSITION-岗位变更, TITLE-职称变更',
    old_value_id BIGINT COMMENT '变更前ID',
    new_value_id BIGINT COMMENT '变更后ID',
    old_value_name VARCHAR(100) COMMENT '变更前名称',
    new_value_name VARCHAR(100) COMMENT '变更后名称',
    change_date DATE NOT NULL COMMENT '变更日期',
    effective_date DATE NOT NULL COMMENT '薪资格式生效日期',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_employee (employee_id),
    INDEX idx_change_date (change_date)
) ENGINE=InnoDB COMMENT='员工岗位/职称变更记录表';

-- 9. 考勤表
DROP TABLE IF EXISTS attendance;
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    attendance_year INT NOT NULL COMMENT '年份',
    attendance_month INT NOT NULL COMMENT '月份',
    leave_days INT DEFAULT 0 COMMENT '事假天数',
    late_times INT DEFAULT 0 COMMENT '迟到次数',
    absent_days INT DEFAULT 0 COMMENT '旷工天数',
    is_full_attendance TINYINT DEFAULT 1 COMMENT '是否全勤：1-是, 0-否',
    leave_deduction DECIMAL(10,2) DEFAULT 0.00 COMMENT '事假扣款（自动计算）',
    late_deduction DECIMAL(10,2) DEFAULT 0.00 COMMENT '迟到扣款（自动计算）',
    absent_deduction DECIMAL(10,2) DEFAULT 0.00 COMMENT '旷工扣款（自动计算）',
    full_attendance_bonus DECIMAL(10,2) DEFAULT 0.00 COMMENT '全勤奖金（自动计算）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_emp_year_month (employee_id, attendance_year, attendance_month),
    INDEX idx_year_month (attendance_year, attendance_month)
) ENGINE=InnoDB COMMENT='考勤表';

-- 10. 工资项配置表
DROP TABLE IF EXISTS salary_item_config;
CREATE TABLE salary_item_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    item_name VARCHAR(100) NOT NULL COMMENT '工资项名称',
    item_code VARCHAR(50) NOT NULL UNIQUE COMMENT '工资项编码',
    item_type VARCHAR(20) NOT NULL COMMENT '类型：SYSTEM-系统自动计算, MANUAL-手动录入',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用：1-是, 0-否',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    description VARCHAR(255) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='工资项配置表';

-- 11. 月度工资主表
DROP TABLE IF EXISTS monthly_salary;
CREATE TABLE monthly_salary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    salary_year INT NOT NULL COMMENT '年份',
    salary_month INT NOT NULL COMMENT '月份',
    -- 系统自动计算项
    base_salary DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '基本工资（员工个人底薪）',
    position_salary DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '岗位工资',
    title_salary DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '职级工资',
    seniority_pay DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '工龄工资',
    full_attendance_bonus DECIMAL(10,2) DEFAULT 0.00 COMMENT '全勤奖金',
    leave_deduction DECIMAL(10,2) DEFAULT 0.00 COMMENT '事假扣款',
    late_deduction DECIMAL(10,2) DEFAULT 0.00 COMMENT '迟到扣款',
    absent_deduction DECIMAL(10,2) DEFAULT 0.00 COMMENT '旷工扣款',
    -- 手动录入项
    project_bonus DECIMAL(10,2) DEFAULT 0.00 COMMENT '项目奖金',
    tax_deduction DECIMAL(10,2) DEFAULT 0.00 COMMENT '个税扣除',
    other_additions DECIMAL(10,2) DEFAULT 0.00 COMMENT '其他加项',
    other_deductions DECIMAL(10,2) DEFAULT 0.00 COMMENT '其他减项',
    -- 汇总
    gross_salary DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '应发工资合计',
    total_deduction DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '扣款合计',
    net_salary DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实发工资',
    -- 状态
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿, CONFIRMED-已确认(不可修改)',
    confirm_time DATETIME COMMENT '确认时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_emp_year_month (employee_id, salary_year, salary_month),
    INDEX idx_year_month (salary_year, salary_month),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='月度工资主表';

-- 12. 月度工资明细表
DROP TABLE IF EXISTS monthly_salary_detail;
CREATE TABLE monthly_salary_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    monthly_salary_id BIGINT NOT NULL COMMENT '月度工资主表ID',
    item_name VARCHAR(100) NOT NULL COMMENT '工资项名称',
    item_code VARCHAR(50) COMMENT '工资项编码',
    item_type VARCHAR(20) COMMENT '类型：SYSTEM/MANUAL',
    item_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_monthly_salary (monthly_salary_id)
) ENGINE=InnoDB COMMENT='月度工资明细表';

-- 13. 工龄工资规则表
DROP TABLE IF EXISTS seniority_pay_rule;
CREATE TABLE seniority_pay_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(20) NOT NULL DEFAULT 'OLD' COMMENT '规则类型：OLD-旧规则, NEW-新规则',
    first_year_amount DECIMAL(10,2) NOT NULL DEFAULT 300.00 COMMENT '第一年工龄工资金额',
    increment_per_year DECIMAL(10,2) NOT NULL DEFAULT 300.00 COMMENT '每年递增金额',
    effective_date DATE COMMENT '生效日期',
    expire_date DATE COMMENT '失效日期（NULL表示永久有效）',
    is_active TINYINT DEFAULT 1 COMMENT '当前是否启用：1-是, 0-否',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='工龄工资规则表';

-- 14. 工龄工资记录表
DROP TABLE IF EXISTS seniority_pay_record;
CREATE TABLE seniority_pay_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    salary_year INT NOT NULL COMMENT '年份',
    salary_month INT NOT NULL COMMENT '月份',
    seniority_years INT NOT NULL COMMENT '工龄（年）',
    seniority_amount DECIMAL(10,2) NOT NULL COMMENT '工龄工资金额',
    rule_id BIGINT COMMENT '使用的规则ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_emp_year_month (employee_id, salary_year, salary_month),
    INDEX idx_year_month (salary_year, salary_month)
) ENGINE=InnoDB COMMENT='工龄工资记录表';

-- 15. 定时任务执行日志表
DROP TABLE IF EXISTS schedule_job_log;
CREATE TABLE schedule_job_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    execute_time DATETIME NOT NULL COMMENT '执行时间',
    status VARCHAR(20) NOT NULL COMMENT '状态：SUCCESS-成功, FAIL-失败',
    message TEXT COMMENT '执行信息/异常信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_job_name (job_name),
    INDEX idx_execute_time (execute_time)
) ENGINE=InnoDB COMMENT='定时任务执行日志表';
