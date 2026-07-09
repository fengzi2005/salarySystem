@echo off
title Salary Management System

echo ============================================
echo   Salary Management System - Starting...
echo ============================================

echo.
echo [1/2] Starting Backend (Spring Boot :8080)...
start "SalaryBackend" cmd /c "cd /d d:\salarySystem\salary-manage-system && mvnw spring-boot:run"

echo [2/2] Starting Frontend (Vue :5173)...
start "SalaryFrontend" cmd /c "cd /d d:\salarySystem\salary-web && npm run dev"

echo.
echo ============================================
echo   Please wait 10-15 seconds, then visit:
echo   http://localhost:5173
echo.
echo   Demo accounts:
echo     Admin  : admin / 123456
echo     Staff  : zhangsan / 123456
echo ============================================
echo.
echo You can close this window (services will keep running).
pause
