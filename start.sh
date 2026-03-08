#!/bin/bash
# ============================================
# 🍲 火锅店管理系统 - 一键启动脚本 (Mac/Linux)
# 自动检测并安装: Java17, Maven, Node, pnpm, MySQL
# ============================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'
BOLD='\033[1m'

# 项目配置
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="hotpot_db"
DB_USER="root"
DB_PASS="ab123168"
BACKEND_PORT=8080
ADMIN_PORT=3006
FRONTEND_PORT=5173
LOG_DIR="$PROJECT_DIR/.logs"

banner() {
  echo ""
  echo -e "${RED}${BOLD}"
  echo "  ╔═══════════════════════════════════════╗"
  echo "  ║      🍲 火锅店管理系统 启动脚本       ║"
  echo "  ╚═══════════════════════════════════════╝"
  echo -e "${NC}"
}

info()  { echo -e "${GREEN}[✓]${NC} $1"; }
warn()  { echo -e "${YELLOW}[!]${NC} $1"; }
error() { echo -e "${RED}[✗]${NC} $1"; }
step()  { echo -e "\n${CYAN}${BOLD}$1${NC}"; }

# ============================================
# 清理函数 (Ctrl+C 停止所有服务)
# ============================================
cleanup() {
  echo ""
  warn "正在停止所有服务..."
  # 杀掉日志 tail 进程
  [ -f "$LOG_DIR/tail.pid" ] && {
    kill "$(cat "$LOG_DIR/tail.pid")" 2>/dev/null
  }
  # 杀掉各服务进程
  for pidfile in "$LOG_DIR"/backend.pid "$LOG_DIR"/admin.pid "$LOG_DIR"/frontend.pid; do
    [ -f "$pidfile" ] && {
      pid=$(cat "$pidfile")
      kill -- -"$pid" 2>/dev/null || kill "$pid" 2>/dev/null
    }
  done
  # 通过端口兜底清理
  lsof -ti ":${BACKEND_PORT}" | xargs kill -9 2>/dev/null || true
  lsof -ti ":${ADMIN_PORT}" | xargs kill -9 2>/dev/null || true
  lsof -ti ":${FRONTEND_PORT}" | xargs kill -9 2>/dev/null || true
  rm -f "$LOG_DIR"/*.pid
  info "所有服务已停止，再见！"
  exit 0
}

trap cleanup SIGINT SIGTERM

# ============================================
# 工具函数
# ============================================

# 检测命令是否存在
has_cmd() { command -v "$1" &>/dev/null; }

# 强制终止端口占用进程
kill_port() {
  local port=$1 name=$2
  if lsof -i ":$port" -sTCP:LISTEN &>/dev/null; then
    warn "端口 $port ($name) 被占用，正在强制释放..."
    lsof -ti ":$port" -sTCP:LISTEN | xargs kill -9 2>/dev/null
    sleep 1
    if lsof -i ":$port" -sTCP:LISTEN &>/dev/null; then
      error "无法释放端口 $port，请手动处理"
      exit 1
    fi
    info "端口 $port 已释放"
  fi
}

# 等待端口就绪
wait_port() {
  local port=$1 name=$2 timeout=${3:-120}
  local count=0
  local spin=('⠋' '⠙' '⠹' '⠸' '⠼' '⠴' '⠦' '⠧' '⠇' '⠏')
  while ! nc -z localhost "$port" 2>/dev/null; do
    count=$((count + 1))
    if [ $count -ge $timeout ]; then
      error "$name 启动超时（${timeout}秒），请检查日志: $LOG_DIR/"
      return 1
    fi
    printf "\r  ${spin[$((count % 10))]} 等待 $name 就绪... (${count}s)"
    sleep 1
  done
  printf "\r"
  info "$name 已就绪 ✓ (端口 $port, 耗时 ${count}s)"
}

# ============================================
# 主流程
# ============================================
banner

# ------------------------------------------
# [1/7] 检查并安装基础环境
# ------------------------------------------
step "[1/7] 🔍 检查基础环境..."

# 1.1 检查 Homebrew
if ! has_cmd brew; then
  warn "Homebrew 未安装，正在安装..."
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
  # 添加到 PATH (Apple Silicon)
  if [ -f "/opt/homebrew/bin/brew" ]; then
    eval "$(/opt/homebrew/bin/brew shellenv)"
  fi
  if ! has_cmd brew; then
    error "Homebrew 安装失败，请手动安装: https://brew.sh"
    exit 1
  fi
  info "Homebrew 安装成功"
else
  info "Homebrew ✓"
fi

# 1.2 检查 Java 17
NEED_JAVA=false
if ! has_cmd java; then
  NEED_JAVA=true
else
  # 检查是否为 Java 17
  JAVA_VER=$(java -version 2>&1 | head -n1)
  if ! echo "$JAVA_VER" | grep -q '"17\.' ; then
    warn "当前 Java 版本不是 17: $JAVA_VER"
    warn "将安装 OpenJDK 17 并设置为默认..."
    NEED_JAVA=true
  fi
fi
if [ "$NEED_JAVA" = true ]; then
  warn "正在通过 Homebrew 安装 OpenJDK 17..."
  brew install openjdk@17
  # 创建符号链接
  sudo ln -sfn "$(brew --prefix openjdk@17)/libexec/openjdk.jdk" /Library/Java/JavaVirtualMachines/openjdk-17.jdk 2>/dev/null
  export PATH="$(brew --prefix openjdk@17)/bin:$PATH"
  export JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
  if ! has_cmd java; then
    error "Java 安装失败"
    exit 1
  fi
  info "Java 17 安装成功"
else
  info "Java ✓ ($JAVA_VER)"
fi

# 1.3 检查 Maven
if ! has_cmd mvn; then
  warn "Maven 未安装，正在通过 Homebrew 安装..."
  brew install maven
  if ! has_cmd mvn; then
    error "Maven 安装失败"
    exit 1
  fi
  info "Maven 安装成功"
else
  info "Maven ✓ ($(mvn -version 2>&1 | head -n1))"
fi

# 1.4 检查 Node.js
if ! has_cmd node; then
  warn "Node.js 未安装，正在通过 Homebrew 安装..."
  brew install node
  if ! has_cmd node; then
    error "Node.js 安装失败"
    exit 1
  fi
  info "Node.js 安装成功"
else
  info "Node.js ✓ ($(node -v))"
fi

# 1.5 检查 pnpm
if ! has_cmd pnpm; then
  warn "pnpm 未安装，正在安装..."
  npm install -g pnpm
  if ! has_cmd pnpm; then
    error "pnpm 安装失败"
    exit 1
  fi
  info "pnpm 安装成功"
else
  info "pnpm ✓ ($(pnpm -v))"
fi

# 1.6 检查 MySQL
if ! has_cmd mysql; then
  warn "MySQL 未安装，正在通过 Homebrew 安装..."
  brew install mysql
  brew services start mysql
  sleep 3
  # 设置 root 密码
  mysqladmin -u root password "$DB_PASS" 2>/dev/null || true
  info "MySQL 安装成功"
else
  info "MySQL ✓"
fi

# ------------------------------------------
# [2/7] 检查 MySQL 数据库
# ------------------------------------------
step "[2/7] 🗄️  检查 MySQL 数据库..."

# 确保 MySQL 服务正在运行
if ! mysqladmin ping -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --silent 2>/dev/null; then
  warn "MySQL 未运行，尝试启动..."
  if [[ "$(uname)" == "Darwin" ]]; then
    brew services start mysql 2>/dev/null || true
  else
    sudo systemctl start mysql 2>/dev/null || sudo service mysql start 2>/dev/null || true
  fi
  sleep 3
  if ! mysqladmin ping -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --silent 2>/dev/null; then
    error "MySQL 无法启动，请手动检查"
    exit 1
  fi
fi
info "MySQL 服务运行中"

# 检查数据库是否存在
DB_EXISTS=$(mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 -N -e "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME='$DB_NAME'" 2>/dev/null || echo "0")
if [ "$DB_EXISTS" -eq 0 ]; then
  info "数据库 $DB_NAME 不存在，正在创建并导入数据..."
  mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 < "$PROJECT_DIR/sql/init.sql"
  if [ $? -ne 0 ]; then
    error "init.sql 导入失败"
    exit 1
  fi
  mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 < "$PROJECT_DIR/sql/data.sql"
  if [ $? -ne 0 ]; then
    error "data.sql 导入失败"
    exit 1
  fi
  info "数据库初始化完成 ✓"
else
  TABLE_COUNT=$(mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DB_NAME'" 2>/dev/null || echo "0")
  if [ "$TABLE_COUNT" -eq 0 ]; then
    info "数据库为空，正在导入..."
    mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 < "$PROJECT_DIR/sql/init.sql"
    mysql -h"$DB_HOST" -u"$DB_USER" -p"$DB_PASS" --default-character-set=utf8mb4 < "$PROJECT_DIR/sql/data.sql"
    info "数据库初始化完成 ✓"
  else
    info "数据库 $DB_NAME 已存在 ($TABLE_COUNT 张表)"
  fi
fi

# ------------------------------------------
# [3/7] 安装项目依赖
# ------------------------------------------
step "[3/7] 📦 安装项目依赖..."

# 后端编译
if [ ! -d "$PROJECT_DIR/backend/target/classes" ]; then
  info "编译后端项目..."
  cd "$PROJECT_DIR/backend" && mvn compile -q
  if [ $? -ne 0 ]; then
    error "后端编译失败，请检查错误信息"
    exit 1
  fi
  info "后端编译完成 ✓"
else
  info "后端已编译 ✓"
fi

# 管理端依赖
if [ ! -d "$PROJECT_DIR/admin/node_modules" ]; then
  info "安装管理端依赖..."
  cd "$PROJECT_DIR/admin" && pnpm install
  if [ $? -ne 0 ]; then
    error "管理端依赖安装失败"
    exit 1
  fi
fi
info "管理端依赖就绪 ✓"

# 用户端依赖
if [ ! -d "$PROJECT_DIR/frontend/node_modules" ]; then
  info "安装用户端依赖..."
  cd "$PROJECT_DIR/frontend" && pnpm install
  if [ $? -ne 0 ]; then
    error "用户端依赖安装失败"
    exit 1
  fi
fi
info "用户端依赖就绪 ✓"

# ------------------------------------------
# [4/7] 检查并清理端口
# ------------------------------------------
step "[4/7] 🔌 检查端口冲突..."

kill_port $BACKEND_PORT  "后端 Spring Boot"
kill_port $ADMIN_PORT    "管理端 Vite"
kill_port $FRONTEND_PORT "用户端 Vite"
info "所有端口可用 ✓"

# ------------------------------------------
# [5/7] 启动所有服务
# ------------------------------------------
step "[5/7] 🚀 启动所有服务..."
mkdir -p "$LOG_DIR"

# 启动后端
info "启动后端 (Spring Boot @ :$BACKEND_PORT)..."
cd "$PROJECT_DIR/backend"
nohup mvn spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &
echo $! > "$LOG_DIR/backend.pid"

# 启动管理端
info "启动管理端 (Vite @ :$ADMIN_PORT)..."
cd "$PROJECT_DIR/admin"
nohup pnpm dev > "$LOG_DIR/admin.log" 2>&1 &
echo $! > "$LOG_DIR/admin.pid"

# 启动用户端
info "启动用户端 (Vite @ :$FRONTEND_PORT)..."
cd "$PROJECT_DIR/frontend"
nohup pnpm dev > "$LOG_DIR/frontend.log" 2>&1 &
echo $! > "$LOG_DIR/frontend.pid"

# ------------------------------------------
# [6/7] 等待服务就绪
# ------------------------------------------
step "[6/7] ⏳ 等待服务就绪..."

wait_port $BACKEND_PORT  "后端"     120
wait_port $ADMIN_PORT    "管理端"   60
wait_port $FRONTEND_PORT "用户端"   60

# ------------------------------------------
# [7/7] 启动完成
# ------------------------------------------
step "[7/7] ✅ 启动完成！"

echo ""
echo -e "${GREEN}${BOLD}  ╔═══════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}${BOLD}  ║      🍲 火锅店管理系统 - 所有服务已启动       ║${NC}"
echo -e "${GREEN}${BOLD}  ╚═══════════════════════════════════════════════╝${NC}"
echo ""
echo -e "  ${BOLD}📊 管理端:${NC}   ${BLUE}http://localhost:${ADMIN_PORT}${NC}"
echo -e "  ${BOLD}🌐 用户端:${NC}   ${BLUE}http://localhost:${FRONTEND_PORT}${NC}"
echo -e "  ${BOLD}⚙️  后端API:${NC}  ${BLUE}http://localhost:${BACKEND_PORT}${NC}"
echo ""
echo -e "  ${BOLD}${CYAN}═══ 管理端测试账号 ═══${NC}"
echo -e "  ${BOLD}┌──────────┬────────────┬────────┐${NC}"
echo -e "  ${BOLD}│   角色   │   用户名   │  密码  │${NC}"
echo -e "  ${BOLD}├──────────┼────────────┼────────┤${NC}"
echo -e "  │ 🔴 店长  │ admin      │ 123456 │"
echo -e "  │ 🟢 收银员│ cashier1   │ 123456 │"
echo -e "  │ 🟡 后厨  │ kitchen1   │ 123456 │"
echo -e "  │ 🔵 库管员│ inventory1 │ 123456 │"
echo -e "  ${BOLD}└──────────┴────────────┴────────┘${NC}"
echo ""
echo -e "  ${BOLD}${CYAN}═══ 用户端会员测试账号 ═══${NC}"
echo -e "  ${BOLD}┌──────────┬─────────────┬────────┐${NC}"
echo -e "  ${BOLD}│   姓名   │   手机号    │  密码  │${NC}"
echo -e "  ${BOLD}├──────────┼─────────────┼────────┤${NC}"
echo -e "  │ 刘晓红   │ 18600001001 │ 001001 │"
echo -e "  │ 张明     │ 18600001002 │ 001002 │"
echo -e "  │ 王丽     │ 18600001003 │ 001003 │"
echo -e "  ${BOLD}└──────────┴─────────────┴────────┘${NC}"
echo -e "  ${YELLOW}提示: 会员密码为手机号后6位${NC}"
echo ""
echo -e "  ${YELLOW}${BOLD}💡 按 Ctrl+C 停止所有服务${NC}"
echo -e "  ${YELLOW}日志目录: $LOG_DIR/${NC}"
echo ""

# 实时显示所有日志（包括报错信息）
info "实时日志输出（所有报错将在此显示）："
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
tail -f "$LOG_DIR/backend.log" "$LOG_DIR/admin.log" "$LOG_DIR/frontend.log" 2>/dev/null &
echo $! > "$LOG_DIR/tail.pid"
wait
