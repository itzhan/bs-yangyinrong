#!/bin/bash
# ============================================================================
# 火锅店管理系统 - API 接口测试脚本
# 覆盖：认证、管理端业务（10个模块）、用户端公开接口、会员接口
# 用法：bash test_api.sh [后端地址，默认 http://localhost:8080]
# ============================================================================

BASE_URL="${1:-http://localhost:8080}"
PASS=0
FAIL=0
TOTAL=0

# 颜色
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# ---- 工具函数 ----

# 发送请求并检查结果
# $1=方法 $2=路径 $3=描述 $4=token $5=body
api_test() {
  local method="$1"
  local path="$2"
  local desc="$3"
  local token="$4"
  local body="$5"

  TOTAL=$((TOTAL + 1))

  local curl_args=(-s -w "\n%{http_code}" -X "$method" "${BASE_URL}${path}")
  curl_args+=(-H "Content-Type: application/json")

  if [ -n "$token" ]; then
    curl_args+=(-H "Authorization: Bearer ${token}")
  fi

  if [ -n "$body" ]; then
    curl_args+=(-d "$body")
  fi

  local response
  response=$(curl "${curl_args[@]}" 2>/dev/null)
  local http_code
  http_code=$(echo "$response" | tail -1)
  local resp_body
  resp_body=$(echo "$response" | sed '$d')

  # 检查 HTTP 状态 2xx
  if [[ "$http_code" =~ ^2 ]]; then
    # 检查业务状态码（如果是JSON）
    local biz_code
    biz_code=$(echo "$resp_body" | grep -o '"code":[0-9]*' | head -1 | grep -o '[0-9]*')
    if [ -n "$biz_code" ] && [ "$biz_code" != "200" ]; then
      local biz_msg
      biz_msg=$(echo "$resp_body" | grep -o '"msg":"[^"]*"' | head -1)
      echo -e "  ${RED}✗ FAIL${NC} [${method} ${path}] ${desc} — HTTP ${http_code} 但业务码=${biz_code} ${biz_msg}"
      FAIL=$((FAIL + 1))
      LAST_BODY="$resp_body"
      return 1
    fi
    echo -e "  ${GREEN}✓ PASS${NC} [${method} ${path}] ${desc}"
    PASS=$((PASS + 1))
    LAST_BODY="$resp_body"
    return 0
  else
    local biz_msg
    biz_msg=$(echo "$resp_body" | grep -o '"msg":"[^"]*"' | head -1)
    echo -e "  ${RED}✗ FAIL${NC} [${method} ${path}] ${desc} — HTTP ${http_code} ${biz_msg}"
    FAIL=$((FAIL + 1))
    LAST_BODY="$resp_body"
    return 1
  fi
}

# 从 JSON 中提取字段值（简单提取）
json_val() {
  echo "$1" | grep -o "\"$2\":[^,}]*" | head -1 | sed "s/\"$2\"://" | tr -d '"' | tr -d ' '
}

# 从 JSON 中提取 data 中的 token
extract_token() {
  echo "$1" | grep -o '"token":"[^"]*"' | head -1 | sed 's/"token":"//' | sed 's/"//'
}

# 从 JSON 中提取数组第一个元素的 id
extract_first_id() {
  echo "$1" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*'
}

# 分隔线
section() {
  echo ""
  echo -e "${CYAN}${BOLD}━━━ $1 ━━━${NC}"
}

# ============================================================================
# 开始测试
# ============================================================================

echo ""
echo -e "${BOLD}╔══════════════════════════════════════════════╗${NC}"
echo -e "${BOLD}║    🍲 火锅店管理系统 - API 接口测试          ║${NC}"
echo -e "${BOLD}║    后端地址: ${BASE_URL}              ║${NC}"
echo -e "${BOLD}╚══════════════════════════════════════════════╝${NC}"

# ---- 0. 连接检查 ----
section "0. 连接检查"
if ! curl -s --connect-timeout 5 "${BASE_URL}/api/auth/login" > /dev/null 2>&1; then
  echo -e "${RED}无法连接到后端 ${BASE_URL}，请确保后端已启动${NC}"
  exit 1
fi
echo -e "  ${GREEN}✓${NC} 后端已连接"

# ---- 1. 认证模块 ----
section "1. 认证模块"

ADMIN_TOKEN=""
CASHIER_TOKEN=""
KITCHEN_TOKEN=""
INVENTORY_TOKEN=""

# Admin 登录
api_test POST "/api/auth/login" "管理员登录 (admin/123456)" "" '{"username":"admin","password":"123456"}'
if [ $? -eq 0 ]; then
  ADMIN_TOKEN=$(extract_token "$LAST_BODY")
  echo -e "     ${YELLOW}→ Token: ${ADMIN_TOKEN:0:20}...${NC}"
fi

# Cashier 登录
api_test POST "/api/auth/login" "收银员登录 (cashier1/123456)" "" '{"username":"cashier1","password":"123456"}'
if [ $? -eq 0 ]; then
  CASHIER_TOKEN=$(extract_token "$LAST_BODY")
fi

# Kitchen 登录
api_test POST "/api/auth/login" "后厨登录 (kitchen1/123456)" "" '{"username":"kitchen1","password":"123456"}'
if [ $? -eq 0 ]; then
  KITCHEN_TOKEN=$(extract_token "$LAST_BODY")
fi

# Inventory 登录
api_test POST "/api/auth/login" "库管员登录 (inventory1/123456)" "" '{"username":"inventory1","password":"123456"}'
if [ $? -eq 0 ]; then
  INVENTORY_TOKEN=$(extract_token "$LAST_BODY")
fi

# 获取用户信息
api_test GET "/api/user/info" "获取当前用户信息" "$ADMIN_TOKEN"

# 无 token 应返回 401
TOTAL=$((TOTAL + 1))
resp=$(curl -s -w "\n%{http_code}" "${BASE_URL}/api/user/info" -H "Content-Type: application/json" 2>/dev/null)
code=$(echo "$resp" | tail -1)
if [ "$code" = "401" ]; then
  echo -e "  ${GREEN}✓ PASS${NC} [GET /api/user/info] 无 token 返回 401"
  PASS=$((PASS + 1))
else
  echo -e "  ${RED}✗ FAIL${NC} [GET /api/user/info] 无 token 期望 401 但得到 ${code}"
  FAIL=$((FAIL + 1))
fi

if [ -z "$ADMIN_TOKEN" ]; then
  echo -e "${RED}管理员登录失败，无法继续后续测试${NC}"
  echo ""
  echo -e "${BOLD}测试结果: ${GREEN}${PASS} PASS${NC} / ${RED}${FAIL} FAIL${NC} / ${TOTAL} TOTAL${NC}"
  exit 1
fi

# ---- 2. 管理端 - 仪表盘 ----
section "2. 仪表盘"
api_test GET "/api/admin/dashboard" "获取仪表盘数据" "$ADMIN_TOKEN"

# ---- 3. 管理端 - 员工管理 ----
section "3. 员工管理"
api_test GET "/api/admin/employees?page=1&size=10" "查询员工列表" "$ADMIN_TOKEN"

# 创建测试员工
api_test POST "/api/admin/employees" "新增员工" "$ADMIN_TOKEN" \
  '{"username":"test_emp_001","password":"123456","realName":"测试员工","phone":"13800000001","role":"CASHIER","status":1}'
TEST_EMP_ID=$(extract_first_id "$LAST_BODY")
# 如果创建成功但没有返回 ID，尝试从列表获取
if [ -z "$TEST_EMP_ID" ]; then
  api_test GET "/api/admin/employees?page=1&size=100&keyword=test_emp_001" "查找新建员工" "$ADMIN_TOKEN"
  TEST_EMP_ID=$(extract_first_id "$LAST_BODY")
fi

if [ -n "$TEST_EMP_ID" ]; then
  api_test PUT "/api/admin/employees/${TEST_EMP_ID}" "修改员工信息" "$ADMIN_TOKEN" \
    '{"realName":"测试员工-已修改","phone":"13800000002","role":"CASHIER","status":1}'
  api_test PUT "/api/admin/employees/${TEST_EMP_ID}/password" "重置员工密码" "$ADMIN_TOKEN" \
    '{"password":"654321"}'
  api_test DELETE "/api/admin/employees/${TEST_EMP_ID}" "删除测试员工" "$ADMIN_TOKEN"
fi

# ---- 4. 管理端 - 菜品分类 ----
section "4. 菜品分类"
api_test GET "/api/admin/categories" "查询分类列表" "$ADMIN_TOKEN"

api_test POST "/api/admin/categories" "新增分类" "$ADMIN_TOKEN" \
  '{"name":"测试分类_API","sortOrder":99}'
# 获取分类列表并找到测试分类
api_test GET "/api/admin/categories" "查询分类-获取测试分类ID" "$ADMIN_TOKEN"
TEST_CAT_ID=$(echo "$LAST_BODY" | grep -o '"id":[0-9]*[^}]*"name":"测试分类_API"' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [ -z "$TEST_CAT_ID" ]; then
  # 尝试简单匹配
  TEST_CAT_ID=$(echo "$LAST_BODY" | grep -o '"测试分类_API"' > /dev/null && extract_first_id "$LAST_BODY")
fi

if [ -n "$TEST_CAT_ID" ]; then
  api_test PUT "/api/admin/categories/${TEST_CAT_ID}" "修改分类" "$ADMIN_TOKEN" \
    '{"name":"测试分类_修改","sortOrder":98}'
  api_test DELETE "/api/admin/categories/${TEST_CAT_ID}" "删除测试分类" "$ADMIN_TOKEN"
fi

# ---- 5. 管理端 - 菜品管理 ----
section "5. 菜品管理"
api_test GET "/api/admin/dishes?page=1&size=10" "查询菜品列表" "$ADMIN_TOKEN"

# 获取一个分类 ID 用于创建菜品
api_test GET "/api/admin/categories" "获取分类（用于菜品）" "$ADMIN_TOKEN"
FIRST_CAT_ID=$(extract_first_id "$LAST_BODY")

if [ -n "$FIRST_CAT_ID" ]; then
  api_test POST "/api/admin/dishes" "新增菜品" "$ADMIN_TOKEN" \
    "{\"name\":\"测试菜品_API\",\"categoryId\":${FIRST_CAT_ID},\"price\":88.00,\"spicyLevel\":3,\"description\":\"API测试菜品\",\"status\":1}"

  api_test GET "/api/admin/dishes?page=1&size=100&keyword=测试菜品_API" "查找测试菜品" "$ADMIN_TOKEN"
  TEST_DISH_ID=$(extract_first_id "$LAST_BODY")

  if [ -n "$TEST_DISH_ID" ]; then
    api_test GET "/api/admin/dishes/${TEST_DISH_ID}" "查询菜品详情" "$ADMIN_TOKEN"
    api_test PUT "/api/admin/dishes/${TEST_DISH_ID}" "修改菜品" "$ADMIN_TOKEN" \
      "{\"name\":\"测试菜品_修改\",\"categoryId\":${FIRST_CAT_ID},\"price\":99.00,\"spicyLevel\":2,\"status\":1}"
    api_test PUT "/api/admin/dishes/${TEST_DISH_ID}/status/0" "下架菜品" "$ADMIN_TOKEN"
    api_test PUT "/api/admin/dishes/${TEST_DISH_ID}/status/1" "上架菜品" "$ADMIN_TOKEN"
    api_test DELETE "/api/admin/dishes/${TEST_DISH_ID}" "删除测试菜品" "$ADMIN_TOKEN"
  fi
fi

# ---- 6. 管理端 - 桌台管理 ----
section "6. 桌台管理"
api_test GET "/api/admin/tables" "查询桌台列表" "$ADMIN_TOKEN"

api_test POST "/api/admin/tables" "新增桌台" "$ADMIN_TOKEN" \
  '{"tableName":"测试桌A99","capacity":6,"status":0}'

api_test GET "/api/admin/tables" "查询桌台-获取测试桌台ID" "$ADMIN_TOKEN"
TEST_TABLE_ID=$(echo "$LAST_BODY" | grep -o '"id":[0-9]*' | tail -1 | grep -o '[0-9]*')

if [ -n "$TEST_TABLE_ID" ]; then
  api_test PUT "/api/admin/tables/${TEST_TABLE_ID}" "修改桌台" "$ADMIN_TOKEN" \
    '{"tableName":"测试桌A99-修改","capacity":8}'
  api_test PUT "/api/admin/tables/${TEST_TABLE_ID}/status/1" "桌台设为占用" "$ADMIN_TOKEN"
  api_test PUT "/api/admin/tables/${TEST_TABLE_ID}/status/0" "桌台设为空闲" "$ADMIN_TOKEN"
  api_test DELETE "/api/admin/tables/${TEST_TABLE_ID}" "删除测试桌台" "$ADMIN_TOKEN"
fi

# ---- 7. 管理端 - 预订管理 ----
section "7. 预订管理"
api_test GET "/api/admin/reservations?page=1&size=10" "查询预订列表" "$ADMIN_TOKEN"

# ---- 8. 管理端 - 订单管理 ----
section "8. 订单管理"
api_test GET "/api/admin/orders?page=1&size=10" "查询订单列表" "$ADMIN_TOKEN"

# ---- 9. 管理端 - 收银结算 ----
section "9. 收银结算"
api_test GET "/api/admin/payments?page=1&size=10" "查询结算列表" "$ADMIN_TOKEN"

# ---- 10. 管理端 - 库存管理 ----
section "10. 库存管理"
api_test GET "/api/admin/ingredients?page=1&size=10" "查询食材列表" "$ADMIN_TOKEN"

api_test POST "/api/admin/ingredients" "新增食材" "$ADMIN_TOKEN" \
  '{"name":"测试食材_API","unit":"kg","stock":100,"warningStock":10,"price":25.5}'

api_test GET "/api/admin/ingredients?page=1&size=100&keyword=测试食材_API" "查找测试食材" "$ADMIN_TOKEN"
TEST_ING_ID=$(extract_first_id "$LAST_BODY")

if [ -n "$TEST_ING_ID" ]; then
  api_test PUT "/api/admin/ingredients/${TEST_ING_ID}" "修改食材" "$ADMIN_TOKEN" \
    '{"name":"测试食材_修改","unit":"kg","stock":200,"warningStock":20,"price":30.0}'

  api_test POST "/api/admin/ingredients/operate" "入库操作" "$ADMIN_TOKEN" \
    "{\"ingredientId\":${TEST_ING_ID},\"type\":\"IN\",\"quantity\":50,\"remark\":\"API测试入库\"}"

  api_test DELETE "/api/admin/ingredients/${TEST_ING_ID}" "删除测试食材" "$ADMIN_TOKEN"
fi

api_test GET "/api/admin/ingredients/warning" "库存预警查询" "$ADMIN_TOKEN"
api_test GET "/api/admin/ingredients/records?page=1&size=10" "库存操作记录" "$ADMIN_TOKEN"

# ---- 11. 管理端 - 会员管理 ----
section "11. 会员管理"
api_test GET "/api/admin/members?page=1&size=10" "查询会员列表" "$ADMIN_TOKEN"
api_test GET "/api/admin/members/levels" "查询会员等级" "$ADMIN_TOKEN"

api_test POST "/api/admin/members" "新增会员" "$ADMIN_TOKEN" \
  '{"name":"测试会员","phone":"13900000099","gender":1}'

api_test GET "/api/admin/members?page=1&size=100&keyword=13900000099" "查找测试会员" "$ADMIN_TOKEN"
TEST_MEM_ID=$(extract_first_id "$LAST_BODY")

if [ -n "$TEST_MEM_ID" ]; then
  api_test PUT "/api/admin/members/${TEST_MEM_ID}" "修改会员信息" "$ADMIN_TOKEN" \
    '{"name":"测试会员-修改","phone":"13900000099","gender":0}'

  api_test POST "/api/admin/members/recharge" "会员充值" "$ADMIN_TOKEN" \
    "{\"memberId\":${TEST_MEM_ID},\"amount\":500}"

  api_test GET "/api/admin/members/${TEST_MEM_ID}" "查询会员详情" "$ADMIN_TOKEN"
  api_test GET "/api/admin/members/recharges?page=1&size=10" "查询充值记录" "$ADMIN_TOKEN"

  api_test DELETE "/api/admin/members/${TEST_MEM_ID}" "删除测试会员" "$ADMIN_TOKEN"
fi

# ---- 12. 管理端 - 操作日志 ----
section "12. 操作日志"
api_test GET "/api/admin/logs?page=1&size=10" "查询操作日志" "$ADMIN_TOKEN"

# ---- 13. 管理端 - 权限测试 ----
section "13. 权限测试"
# 非管理员访问管理端接口应被拒绝
TOTAL=$((TOTAL + 1))
resp=$(curl -s -w "\n%{http_code}" "${BASE_URL}/api/admin/dashboard" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${CASHIER_TOKEN}" 2>/dev/null)
code=$(echo "$resp" | tail -1)
if [ "$code" = "403" ]; then
  echo -e "  ${GREEN}✓ PASS${NC} [GET /api/admin/dashboard] 收银员访问管理端返回 403"
  PASS=$((PASS + 1))
else
  echo -e "  ${RED}✗ FAIL${NC} [GET /api/admin/dashboard] 收银员期望 403 但得到 ${code}"
  FAIL=$((FAIL + 1))
fi

# ---- 14. 用户端 - 公开接口 ----
section "14. 用户端公开接口"
api_test GET "/api/public/categories" "获取菜品分类（公开）"
api_test GET "/api/public/dishes?page=1&size=10" "获取菜品列表（公开）"
api_test GET "/api/public/tables" "获取可用桌台（公开）"

# 获取第一个桌台 ID 用于预订
api_test GET "/api/public/tables" "获取桌台ID" ""
PUB_TABLE_ID=$(extract_first_id "$LAST_BODY")

if [ -n "$PUB_TABLE_ID" ]; then
  FUTURE_TIME=$(date -v+2d '+%Y-%m-%dT18:00:00' 2>/dev/null || date -d '+2 days' '+%Y-%m-%dT18:00:00' 2>/dev/null || echo "2026-03-05T18:00:00")
  api_test POST "/api/public/reservations" "提交预订（公开）" "" \
    "{\"customerName\":\"API测试\",\"customerPhone\":\"13800001111\",\"reservationTime\":\"${FUTURE_TIME}\",\"guestCount\":4,\"tableId\":${PUB_TABLE_ID},\"remark\":\"API测试预订\"}"
fi

# ---- 15. 用户端 - 会员接口 ----
section "15. 用户端会员接口"

# 注册
api_test POST "/api/member/register" "会员注册" "" \
  '{"name":"API测试会员","phone":"13700007777","gender":1}'

# 登录
api_test POST "/api/member/login" "会员登录" "" \
  '{"phone":"13700007777","password":"007777"}'
MEMBER_TOKEN=""
if [ $? -eq 0 ]; then
  MEMBER_TOKEN=$(extract_token "$LAST_BODY")
fi

if [ -n "$MEMBER_TOKEN" ]; then
  api_test GET "/api/member/info" "获取会员信息" "$MEMBER_TOKEN"
  api_test GET "/api/member/orders?page=1&size=10" "获取我的订单" "$MEMBER_TOKEN"
  api_test GET "/api/member/recharges?page=1&size=10" "获取充值记录" "$MEMBER_TOKEN"
  api_test GET "/api/member/reservations?page=1&size=10" "获取预订记录" "$MEMBER_TOKEN"
fi

# ---- 清理测试会员 ----
if [ -n "$ADMIN_TOKEN" ]; then
  api_test GET "/api/admin/members?page=1&size=100&keyword=13700007777" "查找并清理测试会员" "$ADMIN_TOKEN"
  CLEANUP_ID=$(extract_first_id "$LAST_BODY")
  if [ -n "$CLEANUP_ID" ]; then
    api_test DELETE "/api/admin/members/${CLEANUP_ID}" "清理: 删除测试会员" "$ADMIN_TOKEN"
  fi
fi

# ============================================================================
# 测试结果汇总
# ============================================================================
echo ""
echo -e "${BOLD}══════════════════════════════════════════════${NC}"
echo -e "${BOLD}  测试结果汇总${NC}"
echo -e "${BOLD}══════════════════════════════════════════════${NC}"
echo -e "  总计:  ${BOLD}${TOTAL}${NC} 个测试用例"
echo -e "  通过:  ${GREEN}${BOLD}${PASS}${NC} ${GREEN}PASS${NC}"
echo -e "  失败:  ${RED}${BOLD}${FAIL}${NC} ${RED}FAIL${NC}"

if [ "$FAIL" -eq 0 ]; then
  echo ""
  echo -e "  ${GREEN}${BOLD}✓ 全部通过！${NC}"
else
  echo ""
  echo -e "  ${RED}${BOLD}✗ 有 ${FAIL} 个测试失败，请检查上方输出${NC}"
fi

echo ""
exit $FAIL
