#!/bin/bash
# ============================================
# 佛山大学 工资管理系统 - 一键启动脚本
# 用法: bash start.sh
# ============================================

set -e

echo "============================================"
echo "   佛山大学 工资管理系统 v1.0"
echo "============================================"
echo ""

# [1/4] 检查 MySQL 连接
echo "[1/4] 检查 MySQL 数据库连接..."
if mysql -h 192.168.100.128 -P 3306 -u root -p123 --protocol=tcp --default-character-set=utf8mb4 -e "SELECT 1" salary_system >/dev/null 2>&1; then
    echo "       数据库连接正常 ✓"
else
    echo "[错误] MySQL 数据库连接失败！"
    exit 1
fi

# [2/4] 检查 Redis 连接
echo "[2/4] 检查 Redis 连接..."
if command -v redis-cli &>/dev/null; then
    if redis-cli -h 192.168.100.128 -p 6379 -a 123456 ping >/dev/null 2>&1; then
        echo "       Redis 连接正常 ✓"
    else
        echo "[警告] Redis 连接失败，登录功能可能不可用"
    fi
else
    echo "[警告] 未安装 redis-cli，跳过 Redis 检查"
fi

# [3/4] 启动后端
echo "[3/4] 启动后端服务 (Spring Boot :8080)..."
cd "$(dirname "$0")/salary-manage-system"
./mvnw spring-boot:run &
BACKEND_PID=$!
echo "       后端 PID: $BACKEND_PID"

# [4/4] 启动前端
echo "[4/4] 启动前端服务 (Vue + Vite :5173)..."
cd "$(dirname "$0")/salary-web"
npm run dev &
FRONTEND_PID=$!
echo "       前端 PID: $FRONTEND_PID"

echo ""
echo "============================================"
echo "   启动完成！请等待服务就绪后访问："
echo "   前端地址：http://localhost:5173"
echo "   后端地址：http://localhost:8080"
echo ""
echo "   演示账号："
echo "     管理员 - admin / 123456"
echo "     员工   - zhangsan / 123456"
echo ""
echo "   按 Ctrl+C 停止所有服务"
echo "============================================"

# 捕获退出信号，停止所有子进程
cleanup() {
    echo ""
    echo "正在停止所有服务..."
    kill $BACKEND_PID 2>/dev/null
    kill $FRONTEND_PID 2>/dev/null
    echo "服务已停止"
    exit 0
}

trap cleanup SIGINT SIGTERM

# 等待子进程
wait
