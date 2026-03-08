#!/bin/bash
# ============================================
# 🍲 火锅店管理系统 - 全面功能测试脚本
# 测试所有 API 接口 + 前端/管理端页面
# 使用方法: bash test.sh
# ============================================

# 颜色
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[0;33m'
CYAN='\033[0;36m'; NC='\033[0m'; BOLD='\033[1m'

# 配置
BASE="http://localhost:8080"
ADMIN_URL="http://localhost:3006"
FRONTEND_URL="http://localhost:5173"

# 统计
PASS=0; FAIL=0; SKIP=0; ERRORS=()

pass() { echo -e "  ${GREEN}✓${NC} $1"; PASS=$((PASS+1)); }
fail() { echo -e "  ${RED}✗${NC} $1 — $2"; FAIL=$((FAIL+1)); ERRORS+=("$1: $2"); }
skip() { echo -e "  ${YELLOW}⊘${NC} $1"; SKIP=$((SKIP+1)); }
section() { echo -e "\n${CYAN}${BOLD}━━━ $1 ━━━${NC}"; }

# RESP_BODY 和 RESP_CODE 全局变量
RESP_BODY=""
RESP_CODE=""
TOKEN=""

# 发起 HTTP 请求，结果存入 RESP_BODY / RESP_CODE
do_request() {
  local method="$1" path="$2" data="$3"
  local url="${BASE}${path}"
  local args=(-s -w "\n%{http_code}" -X "$method" -H "Content-Type: application/json")
  [ -n "$TOKEN" ] && args+=(-H "Authorization: Bearer $TOKEN")
  [ -n "$data" ] && args+=(-d "$data")
  local raw
  raw=$(curl "${args[@]}" "$url" 2>/dev/null)
  RESP_CODE=$(echo "$raw" | tail -1)
  RESP_BODY=$(echo "$raw" | sed '$d')
}

# 测试 API: method path data expectedCode desc
api_test() {
  local method="$1" path="$2" data="$3" expected="${4:-200}" desc="$5"
  do_request "$method" "$path" "$data"
  if [ "$RESP_CODE" = "$expected" ]; then
    pass "$desc (HTTP $RESP_CODE)"
    return 0
  else
    fail "$desc" "期望 $expected, 实际 $RESP_CODE"
    return 1
  fi
}

# 从 RESP_BODY 提取 JSON 字段
json_get() {
  echo "$RESP_BODY" | python3 -c "
import sys,json
try:
  d=json.load(sys.stdin)
  keys='$1'.split('.')
  for k in keys:
    if isinstance(d,dict): d=d.get(k,'')
    else: d=''
  print(d if d is not None else '')
except: print('')
" 2>/dev/null
}

# 检查页面是否可访问
page_test() {
  local code
  code=$(curl -s -o /dev/null -w "%{http_code}" "$1" 2>/dev/null)
  [ "$code" = "200" ] && pass "$2 (HTTP 200)" || fail "$2" "HTTP $code"
}

# 检查页面内容包含关键词
page_has() {
  curl -s "$1" 2>/dev/null | grep -q "$2" && pass "$3" || fail "$3" "未找到 '$2'"
}

# ============================================
echo ""
echo -e "${RED}${BOLD}"
echo "  ╔═══════════════════════════════════════════╗"
echo "  ║  🍲 火锅店管理系统 - 全面功能测试         ║"
echo "  ╚═══════════════════════════════════════════╝"
echo -e "${NC}"

# ============================================
section "前置检查: 服务可用性"
for svc in "后端:${BASE}" "管理端:${ADMIN_URL}" "用户端:${FRONTEND_URL}"; do
  name="${svc%%:*}"; url="${svc#*:}"
  curl -s -o /dev/null --max-time 3 "$url" 2>/dev/null && pass "$name ($url)" || fail "$name" "$url 不可访问"
done

# ============================================
section "1. 认证模块"

# 1.1 管理员登录
api_test POST "/api/auth/login" '{"username":"admin","password":"123456"}' 200 "管理员登录(admin)"
TOKEN=$(json_get "data.token")
[ -n "$TOKEN" ] && pass "获取 Token (${TOKEN:0:20}...)" || fail "Token 解析" "为空"

# 1.2 获取用户信息
api_test GET "/api/user/info" "" 200 "获取当前用户信息"

# 1.3 收银员登录
SAVE_TOKEN=$TOKEN
api_test POST "/api/auth/login" '{"username":"cashier1","password":"123456"}' 200 "收银员登录"
CASHIER_TOKEN=$(json_get "data.token")
[ -n "$CASHIER_TOKEN" ] && pass "收银员 Token ✓" || skip "收银员 Token"

# 1.4 后厨登录
api_test POST "/api/auth/login" '{"username":"kitchen1","password":"123456"}' 200 "后厨登录"
KITCHEN_TOKEN=$(json_get "data.token")

# 1.5 库管登录
api_test POST "/api/auth/login" '{"username":"inventory1","password":"123456"}' 200 "库管员登录"
INVENTORY_TOKEN=$(json_get "data.token")

TOKEN=$SAVE_TOKEN

# ============================================
section "2. 员工管理 (ADMIN)"

api_test GET "/api/admin/employees?page=1&size=10" "" 200 "员工列表"
EMP_COUNT=$(json_get "data.total")
[ -n "$EMP_COUNT" ] && echo -e "    员工总数: $EMP_COUNT"

api_test GET "/api/admin/employees/1" "" 200 "员工详情(ID=1)"

# CRUD
api_test POST "/api/admin/employees" '{"username":"_test_e2e_","password":"123456","realName":"E2E测试","phone":"13800000099","role":"CASHIER"}' 200 "新增员工"
NEW_ID=$(json_get "data.id")
[ -z "$NEW_ID" ] && NEW_ID=$(json_get "data")
if [ -n "$NEW_ID" ] && [ "$NEW_ID" != "" ]; then
  api_test PUT "/api/admin/employees/$NEW_ID" '{"username":"_test_e2e_","realName":"修改后","phone":"13800000098","role":"CASHIER","status":1}' 200 "修改员工"
  api_test PUT "/api/admin/employees/$NEW_ID/password" '{"password":"654321"}' 200 "重置密码"
  api_test DELETE "/api/admin/employees/$NEW_ID" "" 200 "删除员工"
else
  skip "员工 CRUD 后续操作"
fi

# ============================================
section "3. 菜品分类管理"

api_test GET "/api/admin/categories" "" 200 "分类列表"
api_test GET "/api/admin/categories/1" "" 200 "分类详情"

api_test POST "/api/admin/categories" '{"name":"_E2E测试分类_","sortOrder":99,"status":1}' 200 "新增分类"
CAT_ID=$(json_get "data.id")
[ -z "$CAT_ID" ] && CAT_ID=$(json_get "data")
if [ -n "$CAT_ID" ] && [ "$CAT_ID" != "" ]; then
  api_test PUT "/api/admin/categories/$CAT_ID" '{"name":"_E2E改名_","sortOrder":99,"status":1}' 200 "修改分类"
  api_test DELETE "/api/admin/categories/$CAT_ID" "" 200 "删除分类"
else
  skip "分类 CRUD 后续操作"
fi

# ============================================
section "4. 菜品管理"

api_test GET "/api/admin/dishes?page=1&size=10" "" 200 "菜品列表"
DISH_COUNT=$(json_get "data.total")
[ -n "$DISH_COUNT" ] && echo -e "    菜品总数: $DISH_COUNT"

api_test GET "/api/admin/dishes?page=1&size=10&categoryId=1" "" 200 "按分类筛选(锅底)"
api_test GET "/api/admin/dishes/1" "" 200 "菜品详情(ID=1)"
api_test PUT "/api/admin/dishes/1/status/1" "" 200 "菜品状态→在售"

# ============================================
section "5. 桌台管理"

api_test GET "/api/admin/tables" "" 200 "桌台列表"
api_test GET "/api/admin/tables/1" "" 200 "桌台详情(ID=1)"

# ============================================
section "6. 预订管理"

api_test GET "/api/admin/reservations?page=1&size=10" "" 200 "预订列表"
api_test GET "/api/admin/reservations/1" "" 200 "预订详情(ID=1)"

# ============================================
section "7. 订单管理"

api_test GET "/api/admin/orders?page=1&size=10" "" 200 "订单列表"
ORDER_COUNT=$(json_get "data.total")
[ -n "$ORDER_COUNT" ] && echo -e "    订单总数: $ORDER_COUNT"

api_test GET "/api/admin/orders?page=1&size=10&orderStatus=3" "" 200 "已结算订单"
api_test GET "/api/admin/orders/1" "" 200 "订单详情(ID=1)"
ORDER_NO=$(json_get "data.orderNo")
ITEMS_RAW=$(echo "$RESP_BODY" | python3 -c "import sys,json;d=json.load(sys.stdin);print(len(d.get('data',{}).get('items',[])))" 2>/dev/null)
[ -n "$ORDER_NO" ] && echo -e "    订单号: $ORDER_NO | 订单项: ${ITEMS_RAW:-?}个"

# ============================================
section "8. 收银结算"

api_test GET "/api/admin/payments?page=1&size=10" "" 200 "支付记录列表"
api_test GET "/api/admin/payments/1" "" 200 "支付详情(ID=1)"

# ============================================
section "9. 库存管理"

api_test GET "/api/admin/ingredients?page=1&size=10" "" 200 "食材列表"
api_test GET "/api/admin/ingredients/1" "" 200 "食材详情(ID=1)"
api_test GET "/api/admin/ingredients/warning" "" 200 "低库存预警"
api_test GET "/api/admin/ingredients/records?page=1&size=10" "" 200 "出入库记录"

# ============================================
section "10. 会员管理"

api_test GET "/api/admin/members?page=1&size=10" "" 200 "会员列表"
api_test GET "/api/admin/members/1" "" 200 "会员详情(ID=1)"
api_test GET "/api/admin/members/levels" "" 200 "会员等级列表"
api_test GET "/api/admin/members/recharges?page=1&size=10" "" 200 "充值记录"

# ============================================
section "11. 仪表盘 & 日志"

api_test GET "/api/admin/dashboard" "" 200 "仪表盘数据"
REVENUE=$(json_get "data.todayRevenue")
ORDERS=$(json_get "data.todayOrders")
MEMBERS=$(json_get "data.totalMembers")
echo -e "    营收: $REVENUE | 订单: $ORDERS | 会员: $MEMBERS"

api_test GET "/api/admin/logs?page=1&size=10" "" 200 "操作日志"

# ============================================
section "12. 后厨端接口"

TOKEN=$KITCHEN_TOKEN
api_test GET "/api/kitchen/orders?page=1&size=20" "" 200 "后厨订单列表"
TOKEN=$SAVE_TOKEN

# ============================================
section "13. 库管端接口"

TOKEN=$INVENTORY_TOKEN
api_test GET "/api/inventory/ingredients?page=1&size=10" "" 200 "库管食材列表"
api_test GET "/api/inventory/warning" "" 200 "库管低库存预警"
api_test GET "/api/inventory/records?page=1&size=10" "" 200 "库管出入库记录"
TOKEN=$SAVE_TOKEN

# ============================================
section "14. 公开接口 (无需认证)"

TOKEN=""
api_test GET "/api/public/categories" "" 200 "公开-菜品分类"
PUB_CATS=$(echo "$RESP_BODY" | python3 -c "import sys,json;print(len(json.load(sys.stdin).get('data',[])))" 2>/dev/null)
echo -e "    分类数: ${PUB_CATS:-?}"

api_test GET "/api/public/dishes?page=1&size=10" "" 200 "公开-菜品列表"
api_test GET "/api/public/dishes/1" "" 200 "公开-菜品详情"
api_test GET "/api/public/tables" "" 200 "公开-可预订桌台"

# 公开预订
api_test POST "/api/public/reservations" '{"tableId":1,"customerName":"E2E测试","customerPhone":"13900000099","reservationTime":"2026-12-31T18:00:00","guestCount":2,"remark":"自动测试"}' 200 "公开-提交预订"

TOKEN=$SAVE_TOKEN

# ============================================
section "15. 会员接口"

TOKEN=""
# 会员注册
RAND_PHONE="139$(( RANDOM % 90000000 + 10000000 ))"
api_test POST "/api/member/register" "{\"name\":\"E2E测试\",\"phone\":\"$RAND_PHONE\"}" 200 "会员注册"

# 会员登录 (使用已有会员)
api_test POST "/api/member/login" '{"phone":"18600001001","password":"001001"}' 200 "会员登录(老会员)"
MEMBER_TOKEN=$(json_get "data.token")
if [ -n "$MEMBER_TOKEN" ]; then
  TOKEN=$MEMBER_TOKEN
  api_test GET "/api/member/info" "" 200 "获取会员信息"
  MEM_NAME=$(json_get "data.name")
  MEM_BALANCE=$(json_get "data.balance")
  echo -e "    会员: $MEM_NAME | 余额: $MEM_BALANCE"

  api_test GET "/api/member/orders?page=1&size=10" "" 200 "会员消费记录"
  api_test GET "/api/member/recharges?page=1&size=10" "" 200 "会员充值记录"
else
  skip "会员信息/记录 (登录失败)"
fi

TOKEN=$SAVE_TOKEN

# ============================================
section "16. 用户端页面渲染"

page_test "$FRONTEND_URL/" "首页"
page_has "$FRONTEND_URL/" "川味红锅" "首页包含品牌名"
page_test "$FRONTEND_URL/menu" "菜品菜单"
page_test "$FRONTEND_URL/reservation" "在线预订"
page_test "$FRONTEND_URL/about" "关于我们"
page_test "$FRONTEND_URL/member/login" "会员登录"
page_test "$FRONTEND_URL/member/register" "会员注册"

# ============================================
section "17. 管理端页面渲染"

page_test "$ADMIN_URL/" "管理端首页"

# ============================================
section "18. 数据一致性"

TOKEN=$SAVE_TOKEN
# 仪表盘会员数 vs 会员列表总数
api_test GET "/api/admin/dashboard" "" 200 "仪表盘(复查)" >/dev/null
DASH_M=$(json_get "data.totalMembers")
api_test GET "/api/admin/members?page=1&size=1" "" 200 "会员列表(复查)" >/dev/null
LIST_M=$(json_get "data.total")
if [ -n "$DASH_M" ] && [ -n "$LIST_M" ]; then
  DIFF=$(( ${LIST_M:-0} - ${DASH_M:-0} ))
  [ "$DIFF" -le 1 ] && [ "$DIFF" -ge -1 ] && pass "会员数一致: 仪表盘=$DASH_M 列表=$LIST_M" || fail "会员数偏差" "仪表盘=$DASH_M 列表=$LIST_M"
fi

# ============================================
# 汇总
# ============================================
echo ""
echo -e "${BOLD}═══════════════════════════════════════════${NC}"
echo -e "${BOLD}  📊 测试结果汇总${NC}"
echo -e "${BOLD}═══════════════════════════════════════════${NC}"
echo ""
TOTAL=$((PASS + FAIL + SKIP))
echo -e "  总计: ${BOLD}$TOTAL${NC}"
echo -e "  ${GREEN}✓ 通过: $PASS${NC}"
echo -e "  ${RED}✗ 失败: $FAIL${NC}"
echo -e "  ${YELLOW}⊘ 跳过: $SKIP${NC}"
echo ""

if [ $FAIL -gt 0 ]; then
  echo -e "${RED}  失败详情:${NC}"
  for e in "${ERRORS[@]}"; do echo -e "  ${RED}  • $e${NC}"; done
  echo ""
fi

[ $FAIL -eq 0 ] && echo -e "${GREEN}${BOLD}  🎉 全部通过！${NC}" || echo -e "${YELLOW}${BOLD}  ⚠️ $FAIL 项未通过${NC}"
echo ""
exit $FAIL
