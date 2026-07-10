-- ============================================================
-- 第二部分：视图
-- ============================================================

-- 视图1：员工信息视图（关联部门、岗位、职称）
CREATE OR REPLACE VIEW v_employee_info AS
SELECT
    e.id AS employee_id,
    e.emp_no,
    e.name AS employee_name,
    e.gender,
    e.entry_date,
    e.base_salary,
    e.phone,
    e.email,
    e.status,
    d.id AS dept_id,
    d.dept_name,
    d.dept_level,
    d.parent_id AS dept_parent_id,
    p.id AS position_id,
    p.position_name,
    p.position_type,
    p.position_salary,
    t.id AS title_id,
    t.title_name,
    t.title_salary,
    TIMESTAMPDIFF(YEAR, e.entry_date, CURDATE()) AS service_years
FROM employee e
LEFT JOIN department d ON e.department_id = d.id
LEFT JOIN position p ON e.position_id = p.id
LEFT JOIN title t ON e.title_id = t.id;

-- 视图2：部门信息视图（含负责人姓名、下属员工数量）
CREATE OR REPLACE VIEW v_department_info AS
SELECT
    d.id AS dept_id,
    d.dept_name,
    d.parent_id,
    d.dept_level,
    d.sort_order,
    d.description,
    e.name AS manager_name,
    COUNT(emp.id) AS employee_count
FROM department d
LEFT JOIN employee e ON d.manager_id = e.id
LEFT JOIN employee emp ON emp.department_id = d.id AND emp.status = 1
GROUP BY d.id, d.dept_name, d.parent_id, d.dept_level, d.sort_order, d.description, e.name;

-- ============================================================
-- 第三部分：触发器
-- ============================================================

DELIMITER //

-- 触发器：考勤数据变更时自动计算缺勤扣款和全勤奖金
-- 扣款标准：事假1天扣100元，迟到1次扣50元，旷工1天扣300元，全勤奖300元
DROP TRIGGER IF EXISTS trg_attendance_before_update//
CREATE TRIGGER trg_attendance_before_update
BEFORE UPDATE ON attendance
FOR EACH ROW
BEGIN
    -- 计算事假扣款：事假1天 = 100元
    SET NEW.leave_deduction = NEW.leave_days * 100.00;

    -- 计算迟到扣款：迟到1次 = 50元
    SET NEW.late_deduction = NEW.late_times * 50.00;

    -- 计算旷工扣款：旷工1天 = 300元
    SET NEW.absent_deduction = NEW.absent_days * 300.00;

    -- 判断是否全勤并计算全勤奖：无事假、无迟到、无旷工 = 全勤
    IF NEW.leave_days = 0 AND NEW.late_times = 0 AND NEW.absent_days = 0 THEN
        SET NEW.is_full_attendance = 1;
        SET NEW.full_attendance_bonus = 300.00;
    ELSE
        SET NEW.is_full_attendance = 0;
        SET NEW.full_attendance_bonus = 0.00;
    END IF;
END//

-- 触发器：考勤数据插入时自动计算缺勤扣款和全勤奖金
DROP TRIGGER IF EXISTS trg_attendance_before_insert//
CREATE TRIGGER trg_attendance_before_insert
BEFORE INSERT ON attendance
FOR EACH ROW
BEGIN
    -- 计算事假扣款
    SET NEW.leave_deduction = NEW.leave_days * 100.00;

    -- 计算迟到扣款
    SET NEW.late_deduction = NEW.late_times * 50.00;

    -- 计算旷工扣款
    SET NEW.absent_deduction = NEW.absent_days * 300.00;

    -- 判断全勤
    IF NEW.leave_days = 0 AND NEW.late_times = 0 AND NEW.absent_days = 0 THEN
        SET NEW.is_full_attendance = 1;
        SET NEW.full_attendance_bonus = 300.00;
    ELSE
        SET NEW.is_full_attendance = 0;
        SET NEW.full_attendance_bonus = 0.00;
    END IF;
END//

DELIMITER ;

-- ============================================================
-- 第四部分：存储过程
-- ============================================================

DELIMITER //

-- 存储过程1：月度工资自动计算
-- 根据年份、月份，自动计算所有在职员工的月度工资
DROP PROCEDURE IF EXISTS sp_calc_monthly_salary//
CREATE PROCEDURE sp_calc_monthly_salary(
    IN p_year INT,
    IN p_month INT
)
BEGIN
    DECLARE v_employee_id BIGINT;
    DECLARE v_base_salary DECIMAL(10,2);
    DECLARE v_position_salary DECIMAL(10,2);
    DECLARE v_title_salary DECIMAL(10,2);
    DECLARE v_entry_date DATE;
    DECLARE v_service_years INT;
    DECLARE v_seniority_amount DECIMAL(10,2);
    DECLARE v_leave_deduction DECIMAL(10,2);
    DECLARE v_late_deduction DECIMAL(10,2);
    DECLARE v_absent_deduction DECIMAL(10,2);
    DECLARE v_full_attendance_bonus DECIMAL(10,2);
    DECLARE v_project_bonus DECIMAL(10,2);
    DECLARE v_tax_deduction DECIMAL(10,2);
    DECLARE v_other_additions DECIMAL(10,2);
    DECLARE v_other_deductions DECIMAL(10,2);
    DECLARE v_gross_salary DECIMAL(10,2);
    DECLARE v_total_deduction DECIMAL(10,2);
    DECLARE v_net_salary DECIMAL(10,2);
    DECLARE v_salary_id BIGINT;
    DECLARE v_rule_id BIGINT;
    DECLARE v_seniority_years INT;
    DECLARE v_first_year_amount DECIMAL(10,2);
    DECLARE v_increment DECIMAL(10,2);
    DECLARE v_effective_date DATE;
    DECLARE v_done INT DEFAULT 0;

    -- 游标：遍历所有在职员工（入职日期在计算月份之前或当月入职）
    DECLARE cur_employee CURSOR FOR
        SELECT e.id, e.base_salary, IFNULL(p.position_salary, 0),
               IFNULL(t.title_salary, 0), e.entry_date
        FROM employee e
        LEFT JOIN position p ON e.position_id = p.id
        LEFT JOIN title t ON e.title_id = t.id
        WHERE e.status = 1
          AND e.entry_date <= LAST_DAY(CONCAT(p_year, '-', LPAD(p_month, 2, '0'), '-01'));

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    -- 获取当前生效的工龄工资规则（优先用新规则）
    SELECT id, first_year_amount, increment_per_year, effective_date
    INTO v_rule_id, v_first_year_amount, v_increment, v_effective_date
    FROM seniority_pay_rule
    WHERE is_active = 1
    ORDER BY rule_type DESC, effective_date DESC
    LIMIT 1;

    -- 如果没有规则，使用默认值
    IF v_rule_id IS NULL THEN
        SET v_first_year_amount = 300.00;
        SET v_increment = 300.00;
        SET v_effective_date = '2000-01-01';
    END IF;

    OPEN cur_employee;

    read_loop: LOOP
        FETCH cur_employee INTO v_employee_id, v_base_salary, v_position_salary, v_title_salary, v_entry_date;
        IF v_done THEN
            LEAVE read_loop;
        END IF;

        -- 计算工龄工资
        -- 工龄 = 当前计算月份 - 入职月份 + 1（如果过了入职月份）
        SET v_seniority_years = TIMESTAMPDIFF(YEAR, v_entry_date, CONCAT(p_year, '-', LPAD(p_month, 2, '0'), '-01'));
        -- 不足一年按0年算
        IF v_seniority_years < 1 THEN
            SET v_seniority_years = 0;
        END IF;

        -- 判断使用旧规则还是新规则
        -- 根据工资发放日期与规则生效日期比较
        IF STR_TO_DATE(CONCAT(p_year, '-', LPAD(p_month, 2, '0'), '-01'), '%Y-%m-%d') >= v_effective_date THEN
            -- 新规则：第一年300，后续每年+100
            IF v_seniority_years >= 1 THEN
                SET v_seniority_amount = v_first_year_amount + (v_seniority_years - 1) * v_increment;
            ELSE
                SET v_seniority_amount = 0;
            END IF;
        ELSE
            -- 旧规则：每年300（1年300, 2年600, 3年900...）
            SET v_seniority_amount = v_seniority_years * v_first_year_amount;
        END IF;

        -- 获取考勤数据（使用 SET + 子查询避免触发外层 NOT FOUND handler）
        SET v_leave_deduction = (SELECT IFNULL(leave_deduction, 0) FROM attendance WHERE employee_id = v_employee_id AND attendance_year = p_year AND attendance_month = p_month);
        SET v_late_deduction = (SELECT IFNULL(late_deduction, 0) FROM attendance WHERE employee_id = v_employee_id AND attendance_year = p_year AND attendance_month = p_month);
        SET v_absent_deduction = (SELECT IFNULL(absent_deduction, 0) FROM attendance WHERE employee_id = v_employee_id AND attendance_year = p_year AND attendance_month = p_month);
        SET v_full_attendance_bonus = (SELECT IFNULL(full_attendance_bonus, 0) FROM attendance WHERE employee_id = v_employee_id AND attendance_year = p_year AND attendance_month = p_month);

        -- 如果没有考勤记录，默认全部为0
        IF v_leave_deduction IS NULL THEN
            SET v_leave_deduction = 0;
            SET v_late_deduction = 0;
            SET v_absent_deduction = 0;
            SET v_full_attendance_bonus = 0;
        END IF;

        -- 获取手动录入项（使用 SET + 子查询避免触发外层 NOT FOUND handler）
        SET v_project_bonus = (SELECT IFNULL(project_bonus, 0) FROM monthly_salary WHERE employee_id = v_employee_id AND salary_year = p_year AND salary_month = p_month AND status = 'DRAFT');
        SET v_tax_deduction = (SELECT IFNULL(tax_deduction, 0) FROM monthly_salary WHERE employee_id = v_employee_id AND salary_year = p_year AND salary_month = p_month AND status = 'DRAFT');
        SET v_other_additions = (SELECT IFNULL(other_additions, 0) FROM monthly_salary WHERE employee_id = v_employee_id AND salary_year = p_year AND salary_month = p_month AND status = 'DRAFT');
        SET v_other_deductions = (SELECT IFNULL(other_deductions, 0) FROM monthly_salary WHERE employee_id = v_employee_id AND salary_year = p_year AND salary_month = p_month AND status = 'DRAFT');

        IF v_project_bonus IS NULL THEN
            SET v_project_bonus = 0;
            SET v_tax_deduction = 0;
            SET v_other_additions = 0;
            SET v_other_deductions = 0;
        END IF;

        -- 计算汇总
        -- 应发 = 基本工资 + 岗位工资 + 职级工资 + 工龄工资 + 全勤奖 + 项目奖金 + 其他加项
        SET v_gross_salary = v_base_salary + v_position_salary + v_title_salary
                           + v_seniority_amount + v_full_attendance_bonus
                           + v_project_bonus + v_other_additions;

        -- 扣款 = 事假扣款 + 迟到扣款 + 旷工扣款 + 个税 + 其他减项
        SET v_total_deduction = v_leave_deduction + v_late_deduction + v_absent_deduction
                              + v_tax_deduction + v_other_deductions;

        -- 实发 = 应发 - 扣款
        SET v_net_salary = v_gross_salary - v_total_deduction;

        -- 插入或更新月度工资记录
        INSERT INTO monthly_salary (
            employee_id, salary_year, salary_month,
            base_salary, position_salary, title_salary, seniority_pay,
            full_attendance_bonus, leave_deduction, late_deduction, absent_deduction,
            project_bonus, tax_deduction, other_additions, other_deductions,
            gross_salary, total_deduction, net_salary, status
        ) VALUES (
            v_employee_id, p_year, p_month,
            v_base_salary, v_position_salary, v_title_salary, v_seniority_amount,
            v_full_attendance_bonus, v_leave_deduction, v_late_deduction, v_absent_deduction,
            v_project_bonus, v_tax_deduction, v_other_additions, v_other_deductions,
            v_gross_salary, v_total_deduction, v_net_salary, 'DRAFT'
        ) ON DUPLICATE KEY UPDATE
            base_salary = VALUES(base_salary),
            position_salary = VALUES(position_salary),
            title_salary = VALUES(title_salary),
            seniority_pay = VALUES(seniority_pay),
            full_attendance_bonus = VALUES(full_attendance_bonus),
            leave_deduction = VALUES(leave_deduction),
            late_deduction = VALUES(late_deduction),
            absent_deduction = VALUES(absent_deduction),
            gross_salary = VALUES(gross_salary),
            total_deduction = VALUES(total_deduction),
            net_salary = VALUES(net_salary),
            update_time = NOW();

        -- 记录工龄工资明细
        INSERT INTO seniority_pay_record (
            employee_id, salary_year, salary_month, seniority_years, seniority_amount, rule_id
        ) VALUES (
            v_employee_id, p_year, p_month, v_seniority_years, v_seniority_amount, v_rule_id
        ) ON DUPLICATE KEY UPDATE
            seniority_years = VALUES(seniority_years),
            seniority_amount = VALUES(seniority_amount),
            rule_id = VALUES(rule_id);

    END LOOP;

    CLOSE cur_employee;

    -- 记录执行日志
    INSERT INTO schedule_job_log (job_name, execute_time, status, message)
    VALUES ('sp_calc_monthly_salary', NOW(), 'SUCCESS',
            CONCAT('成功计算 ', p_year, '年', p_month, '月工资数据'));

END//

-- 存储过程2：工资确认（锁定工资数据）
DROP PROCEDURE IF EXISTS sp_confirm_salary//
CREATE PROCEDURE sp_confirm_salary(
    IN p_year INT,
    IN p_month INT
)
BEGIN
    UPDATE monthly_salary
    SET status = 'CONFIRMED', confirm_time = NOW(), update_time = NOW()
    WHERE salary_year = p_year
      AND salary_month = p_month
      AND status = 'DRAFT';

    INSERT INTO schedule_job_log (job_name, execute_time, status, message)
    VALUES ('sp_confirm_salary', NOW(), 'SUCCESS',
            CONCAT('已确认 ', p_year, '年', p_month, '月工资数据，共 ', ROW_COUNT(), ' 条'));
END//

-- 存储过程3：按员工查询工资（支持权限过滤 - 管理员看全部，普通员工只看自己，管理者看本部门）
DROP PROCEDURE IF EXISTS sp_query_employee_salary//
CREATE PROCEDURE sp_query_employee_salary(
    IN p_employee_id BIGINT,
    IN p_year INT,
    IN p_month INT
)
BEGIN
    SELECT
        ms.id,
        ms.employee_id,
        e.name AS employee_name,
        e.emp_no,
        d.dept_name,
        ms.salary_year,
        ms.salary_month,
        ms.base_salary,
        ms.position_salary,
        ms.title_salary,
        ms.seniority_pay,
        ms.full_attendance_bonus,
        ms.leave_deduction,
        ms.late_deduction,
        ms.absent_deduction,
        ms.project_bonus,
        ms.tax_deduction,
        ms.other_additions,
        ms.other_deductions,
        ms.gross_salary,
        ms.total_deduction,
        ms.net_salary,
        ms.status,
        ms.confirm_time
    FROM monthly_salary ms
    LEFT JOIN employee e ON ms.employee_id = e.id
    LEFT JOIN department d ON e.department_id = d.id
    WHERE ms.employee_id = p_employee_id
      AND (p_year IS NULL OR ms.salary_year = p_year)
      AND (p_month IS NULL OR ms.salary_month = p_month)
    ORDER BY ms.salary_year DESC, ms.salary_month DESC;
END//

-- 存储过程4：按部门查询工资汇总
DROP PROCEDURE IF EXISTS sp_query_department_salary//
CREATE PROCEDURE sp_query_department_salary(
    IN p_department_id BIGINT,
    IN p_year INT,
    IN p_month INT
)
BEGIN
    SELECT
        ms.id,
        ms.employee_id,
        e.name AS employee_name,
        e.emp_no,
        d.dept_name,
        p.position_name,
        t.title_name,
        ms.salary_year,
        ms.salary_month,
        ms.base_salary,
        ms.position_salary,
        ms.title_salary,
        ms.seniority_pay,
        ms.full_attendance_bonus,
        ms.project_bonus,
        ms.gross_salary,
        ms.total_deduction,
        ms.net_salary,
        ms.status
    FROM monthly_salary ms
    LEFT JOIN employee e ON ms.employee_id = e.id
    LEFT JOIN department d ON e.department_id = d.id
    LEFT JOIN position p ON e.position_id = p.id
    LEFT JOIN title t ON e.title_id = t.id
    WHERE e.department_id = p_department_id
      AND ms.salary_year = p_year
      AND ms.salary_month = p_month
    ORDER BY ms.net_salary DESC;
END//

-- 存储过程5：薪资结构分析报表（各部门各工资项汇总及占比）
DROP PROCEDURE IF EXISTS sp_salary_structure_report//
CREATE PROCEDURE sp_salary_structure_report(
    IN p_year INT,
    IN p_month INT
)
BEGIN
    SELECT
        d.id AS dept_id,
        d.dept_name,
        COUNT(ms.id) AS employee_count,
        SUM(ms.base_salary) AS total_base_salary,
        SUM(ms.position_salary) AS total_position_salary,
        SUM(ms.title_salary) AS total_title_salary,
        SUM(ms.seniority_pay) AS total_seniority_pay,
        SUM(ms.full_attendance_bonus) AS total_attendance_bonus,
        SUM(ms.project_bonus) AS total_project_bonus,
        SUM(ms.gross_salary) AS total_gross_salary,
        SUM(ms.total_deduction) AS total_deduction,
        SUM(ms.net_salary) AS total_net_salary,
        ROUND(SUM(ms.net_salary) / (SELECT SUM(net_salary) FROM monthly_salary WHERE salary_year = p_year AND salary_month = p_month) * 100, 2) AS salary_ratio
    FROM monthly_salary ms
    LEFT JOIN employee e ON ms.employee_id = e.id
    LEFT JOIN department d ON e.department_id = d.id
    WHERE ms.salary_year = p_year AND ms.salary_month = p_month
    GROUP BY d.id, d.dept_name
    ORDER BY total_net_salary DESC;
END//

-- 存储过程6：部门薪资统计报表（最高工资、最低工资、平均工资）
DROP PROCEDURE IF EXISTS sp_department_salary_stats//
CREATE PROCEDURE sp_department_salary_stats(
    IN p_year INT,
    IN p_month INT
)
BEGIN
    SELECT
        d.id AS dept_id,
        d.dept_name,
        COUNT(ms.id) AS employee_count,
        MAX(ms.net_salary) AS max_salary,
        MIN(ms.net_salary) AS min_salary,
        ROUND(AVG(ms.net_salary), 2) AS avg_salary,
        SUM(ms.net_salary) AS total_salary
    FROM monthly_salary ms
    LEFT JOIN employee e ON ms.employee_id = e.id
    LEFT JOIN department d ON e.department_id = d.id
    WHERE ms.salary_year = p_year AND ms.salary_month = p_month
    GROUP BY d.id, d.dept_name
    ORDER BY avg_salary DESC;
END//

-- 存储过程7：员工年度薪资统计
DROP PROCEDURE IF EXISTS sp_employee_annual_stats//
CREATE PROCEDURE sp_employee_annual_stats(
    IN p_employee_id BIGINT,
    IN p_year INT
)
BEGIN
    SELECT
        e.name AS employee_name,
        e.emp_no,
        d.dept_name,
        ms.salary_year,
        COUNT(ms.id) AS month_count,
        SUM(ms.net_salary) AS total_annual_salary,
        ROUND(AVG(ms.net_salary), 2) AS avg_monthly_salary,
        MAX(ms.net_salary) AS max_monthly_salary,
        MIN(ms.net_salary) AS min_monthly_salary
    FROM monthly_salary ms
    LEFT JOIN employee e ON ms.employee_id = e.id
    LEFT JOIN department d ON e.department_id = d.id
    WHERE ms.employee_id = p_employee_id
      AND ms.salary_year = p_year
      AND ms.status = 'CONFIRMED'
    GROUP BY e.name, e.emp_no, d.dept_name, ms.salary_year;
END//

-- 存储过程8：通用薪资查询（支持多条件筛选）
DROP PROCEDURE IF EXISTS sp_salary_query//
CREATE PROCEDURE sp_salary_query(
    IN p_year INT,
    IN p_month INT,
    IN p_dept_id BIGINT,
    IN p_employee_name VARCHAR(50),
    IN p_emp_no VARCHAR(20)
)
BEGIN
    SELECT
        ms.id,
        ms.employee_id,
        e.name AS employee_name,
        e.emp_no,
        d.id AS dept_id,
        d.dept_name,
        p.position_name,
        t.title_name,
        ms.salary_year,
        ms.salary_month,
        ms.base_salary,
        ms.position_salary,
        ms.title_salary,
        ms.seniority_pay,
        ms.full_attendance_bonus,
        ms.leave_deduction,
        ms.late_deduction,
        ms.absent_deduction,
        ms.project_bonus,
        ms.tax_deduction,
        ms.other_additions,
        ms.other_deductions,
        ms.gross_salary,
        ms.total_deduction,
        ms.net_salary,
        ms.status,
        ms.confirm_time
    FROM monthly_salary ms
    LEFT JOIN employee e ON ms.employee_id = e.id
    LEFT JOIN department d ON e.department_id = d.id
    LEFT JOIN position p ON e.position_id = p.id
    LEFT JOIN title t ON e.title_id = t.id
    WHERE (p_year IS NULL OR ms.salary_year = p_year)
      AND (p_month IS NULL OR ms.salary_month = p_month)
      AND (p_dept_id IS NULL OR d.id = p_dept_id)
      AND (p_employee_name IS NULL OR e.name LIKE CONCAT('%', p_employee_name, '%'))
      AND (p_emp_no IS NULL OR e.emp_no LIKE CONCAT('%', p_emp_no, '%'))
    ORDER BY ms.salary_year DESC, ms.salary_month DESC, d.id, ms.net_salary DESC;
END//

DELIMITER ;

-- ============================================================
-- 第五部分：MySQL 定时事件
-- ============================================================

-- 开启事件调度器（需要SUPER权限，如无权限需手动在MySQL配置中开启）
-- SET GLOBAL event_scheduler = ON;

-- 定时任务：每月1号凌晨1:00自动计算上月工资数据
DROP EVENT IF EXISTS evt_monthly_salary_init;
CREATE EVENT evt_monthly_salary_init
ON SCHEDULE EVERY 1 MONTH
STARTS TIMESTAMP(CONCAT(DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-'), '01 01:00:00'))
ON COMPLETION PRESERVE
ENABLE
COMMENT '每月1号凌晨1点自动计算上月工资'
DO
CALL sp_calc_monthly_salary(YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)));
