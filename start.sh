#!/bin/bash
# ============================================
# 🍲 火锅店管理系统 - Docker 一键启动 (Mac/Linux)
# ============================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'
BOLD='\033[1m'

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

echo ""
echo -e "${RED}${BOLD}"
echo "  ╔═══════════════════════════════════════╗"
echo "  ║      🍲 火锅店管理系统 Docker 启动     ║"
echo "  ╚═══════════════════════════════════════╝"
echo -e "${NC}"

# ---- 检查 Docker ----
if ! command -v docker &>/dev/null; then
  echo -e "${RED}[✗] Docker 未安装，请先安装 Docker Desktop 或 OrbStack${NC}"
  echo -e "    下载: https://www.docker.com/products/docker-desktop/"
  exit 1
fi

if ! docker info &>/dev/null; then
  echo -e "${RED}[✗] Docker 未运行，请先启动 Docker${NC}"
  exit 1
fi

echo -e "${GREEN}[✓]${NC} Docker 已就绪"



# ---- 启动服务 ----
echo ""
echo -e "${CYAN}${BOLD}▶ 启动 Docker 容器...${NC}"
DOCKER_BUILDKIT=0 docker compose up -d 2>&1

if [ $? -ne 0 ]; then
  echo -e "${RED}[✗] Docker 启动失败，请检查错误信息${NC}"
  exit 1
fi

# ---- 等待后端就绪 ----
echo ""
echo -ne "${CYAN}⏳ 等待后端启动"
TIMEOUT=60
COUNT=0
while ! curl -s -o /dev/null -w "%{http_code}" http://localhost/api/dish/category/list 2>/dev/null | grep -q "200\|401\|403"; do
  COUNT=$((COUNT + 1))
  if [ $COUNT -ge $TIMEOUT ]; then
    echo ""
    echo -e "${YELLOW}[!]${NC} 后端启动较慢，可能仍在初始化中"
    echo -e "    可运行 ${CYAN}docker compose logs -f backend${NC} 查看日志"
    break
  fi
  echo -ne "."
  sleep 2
done
echo ""
echo -e "${GREEN}[✓]${NC} 所有服务已启动"

# ---- 打印信息 ----
echo ""
echo -e "${GREEN}${BOLD}  ╔═══════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}${BOLD}  ║      🍲 火锅店管理系统 - 所有服务已启动       ║${NC}"
echo -e "${GREEN}${BOLD}  ╚═══════════════════════════════════════════════╝${NC}"
echo ""
echo -e "  ${BOLD}📊 管理后台:${NC}   ${BLUE}http://localhost/admin${NC}"
echo -e "  ${BOLD}🌐 用户前台:${NC}   ${BLUE}http://localhost${NC}"
echo -e "  ${BOLD}⚙️  后端 API:${NC}   ${BLUE}http://localhost/api${NC}"
echo ""
echo -e "  ${BOLD}${CYAN}═══ 管理端测试账号 ═══${NC}"
echo -e "  ${BOLD}┌──────────┬────────────┬────────┐${NC}"
echo -e "  ${BOLD}│   角色   │   用户名   │  密码  │${NC}"
echo -e "  ${BOLD}├──────────┼────────────┼────────┤${NC}"
echo -e "  │ 🔴 店长  │ admin      │ 123456 │"
echo -e "  │ 🟢 收银员│ cashier1   │ 123456 │"
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
echo -e "  ${BOLD}常用命令:${NC}"
echo -e "    查看日志:  ${CYAN}docker compose logs -f${NC}"
echo -e "    停止服务:  ${CYAN}docker compose down${NC}"
echo -e "    重建数据:  ${CYAN}docker compose down -v && DOCKER_BUILDKIT=0 docker compose up -d${NC}"
echo ""
echo -e "  ${YELLOW}${BOLD}💡 按 Ctrl+C 退出日志，服务将保持后台运行${NC}"
echo ""

# ---- 显示实时日志 ----
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BOLD}📺 实时日志 (Ctrl+C 退出日志，服务保持运行)${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
docker compose logs -f
