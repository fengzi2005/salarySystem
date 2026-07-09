#!/bin/bash
# ============================================
# 数据库初始化脚本 (Linux/Mac/Git Bash)
# ============================================

MYSQL_HOST="192.168.100.128"
MYSQL_PORT="3306"
MYSQL_USER="root"
MYSQL_PASS="123"
DB_NAME="salary_system"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "============================================"
echo "   数据库初始化脚本"
echo "============================================"
echo ""
echo "目标: $MYSQL_HOST:$MYSQL_PORT / $MYSQL_USER"
echo "数据库: $DB_NAME"
echo ""

# 检查 mysql 客户端
if ! command -v mysql &>/dev/null; then
    echo "[错误] 未找到 mysql 客户端，请先安装 MySQL"
    exit 1
fi

MYSQL_CMD="mysql -h $MYSQL_HOST -P $MYSQL_PORT -u $MYSQL_USER -p$MYSQL_PASS --protocol=tcp --default-character-set=utf8mb4"

echo "[1/3] 创建表结构..."
$MYSQL_CMD < "$SCRIPT_DIR/salary-manage-system/sql/01-schema.sql"
echo "       表结构创建完成 ✓"

echo "[2/3] 创建视图、触发器、存储过程..."
$MYSQL_CMD $DB_NAME < "$SCRIPT_DIR/salary-manage-system/sql/02-views-triggers-procedures.sql"
echo "       视图/存储过程创建完成 ✓"

echo "[3/3] 导入初始数据..."
$MYSQL_CMD $DB_NAME < "$SCRIPT_DIR/salary-manage-system/sql/03-init-data.sql"
echo "       初始数据导入完成 ✓"

echo ""
echo "============================================"
echo "   数据库初始化完成！"
echo "============================================"
