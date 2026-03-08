#!/bin/bash
# ============================================
# 🍲 火锅店管理系统 - 停止脚本 (Mac/Linux)
# ============================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$PROJECT_DIR/.logs"

BACKEND_PORT=8080
ADMIN_PORT=3006
FRONTEND_PORT=5173

echo ""
echo -e "${RED}  ╔═══════════════════════════════════════╗${NC}"
echo -e "${RED}  ║      🍲 火锅店管理系统 停止脚本       ║${NC}"
echo -e "${RED}  ╚═══════════════════════════════════════╝${NC}"
echo ""

# 通过 PID 文件停止
for pidfile in "$LOG_DIR"/backend.pid "$LOG_DIR"/admin.pid "$LOG_DIR"/frontend.pid "$LOG_DIR"/tail.pid; do
  if [ -f "$pidfile" ]; then
    pid=$(cat "$pidfile")
    name=$(basename "$pidfile" .pid)
    if kill -0 "$pid" 2>/dev/null; then
      kill -- -"$pid" 2>/dev/null || kill "$pid" 2>/dev/null
      echo -e "${GREEN}[✓]${NC} 已停止 $name (PID: $pid)"
    fi
    rm -f "$pidfile"
  fi
done

# 通过端口兜底清理
for port in $BACKEND_PORT $ADMIN_PORT $FRONTEND_PORT; do
  pids=$(lsof -ti ":$port" 2>/dev/null)
  if [ -n "$pids" ]; then
    echo "$pids" | xargs kill -9 2>/dev/null
    echo -e "${YELLOW}[!]${NC} 已强制释放端口 $port"
  fi
done

echo ""
echo -e "${GREEN}所有服务已停止 ✓${NC}"
echo ""
