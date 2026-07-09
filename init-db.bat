@echo off
chcp 65001 >nul
title 工资管理系统 - 数据库初始化

echo ============================================
echo    数据库初始化脚本
echo ============================================
echo.
echo 目标: 192.168.100.128:3306 / root / 123
echo 数据库: salary_system
echo.
echo 按任意键开始初始化...
pause >nul

echo [1/3] 创建表结构...
mysql -h 192.168.100.128 -P 3306 -u root -p123 --protocol=tcp --default-character-set=utf8mb4 < "%~dp0salary-manage-system\sql\01-schema.sql"
if %errorlevel% neq 0 (
    echo [错误] 表结构创建失败！
    pause
    exit /b 1
)
echo        表结构创建完成 ✓

echo [2/3] 创建视图、触发器、存储过程...
mysql -h 192.168.100.128 -P 3306 -u root -p123 --protocol=tcp --default-character-set=utf8mb4 salary_system < "%~dp0salary-manage-system\sql\02-views-triggers-procedures.sql"
if %errorlevel% neq 0 (
    echo [错误] 视图/存储过程创建失败！
    pause
    exit /b 1
)
echo        视图/存储过程创建完成 ✓

echo [3/3] 导入初始数据...
mysql -h 192.168.100.128 -P 3306 -u root -p123 --protocol=tcp --default-character-set=utf8mb4 salary_system < "%~dp0salary-manage-system\sql\03-init-data.sql"
if %errorlevel% neq 0 (
    echo [错误] 初始数据导入失败！
    pause
    exit /b 1
)
echo        初始数据导入完成 ✓

echo.
echo ============================================
echo    数据库初始化完成！
echo.
echo    表: 15 张
echo    初始数据: 角色3 / 用户11 / 部门8 /
echo              岗位10 / 职称5 / 员工10 / 考勤10
echo ============================================
pause
