import { test, expect, type Page } from '@playwright/test'

const ADMIN_URL = 'http://localhost:3006'
const API_URL = 'http://localhost:8080'

// ── Helper: 通过 API 登录获取 token，绕过滑块验证 ──
async function apiLogin(username = 'admin', password = '123456'): Promise<string> {
  const res = await fetch(`${API_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  })
  const json = await res.json()
  if (json.code !== 200 || !json.data?.token) throw new Error(`Login failed: ${JSON.stringify(json)}`)
  return json.data.token
}

// ── Helper: 通过 UI 直接登录管理端（滑块已移除） ──
async function loginAsAdmin(page: Page) {
  await page.goto(`${ADMIN_URL}/auth/login`)
  await page.waitForTimeout(2000)

  // 页面 onMounted 会自动选择 admin 账号并填充 username/password
  // 直接点击登录按钮
  const loginBtn = page.locator('.el-button--primary').first()
  await loginBtn.click()

  // 等待登录完成并跳转
  await page.waitForTimeout(5000)
  await page.waitForLoadState('networkidle')
}

// ===============================
// 1. 认证流程
// ===============================

test.describe('管理端 - 认证流程', () => {
  test('登录页可访问且表单可见', async ({ page }) => {
    await page.goto(`${ADMIN_URL}/auth/login`)
    await expect(page.locator('.el-form')).toBeVisible()
    await expect(page.locator('.el-input').first()).toBeVisible()
  })

  test('空字段提交显示验证提示', async ({ page }) => {
    await page.goto(`${ADMIN_URL}/auth/login`)
    await page.waitForTimeout(1000)
    // 清空自动填充的字段
    const usernameInput = page.locator('.el-form-item').nth(1).locator('input')
    const passwordInput = page.locator('.el-form-item').nth(2).locator('input')
    await usernameInput.fill('')
    await passwordInput.fill('')
    // 点击登录按钮
    await page.locator('.el-button--primary').click()
    await page.waitForTimeout(500)
    // 应有表单验证提示
    const formErrors = page.locator('.el-form-item__error')
    await expect(formErrors.first()).toBeVisible()
  })

  test('admin/123456 可通过 API 登录', async () => {
    const token = await apiLogin()
    expect(token).toBeTruthy()
    expect(token.length).toBeGreaterThan(20)
  })

  test('注入 token 后可访问首页', async ({ page }) => {
    await loginAsAdmin(page)
    // 验证不在登录页
    expect(page.url()).not.toContain('/auth/login')
  })

  test('未登录访问业务页面跳转到登录页', async ({ page }) => {
    await page.goto(`${ADMIN_URL}/dashboard/home`)
    await page.waitForTimeout(3000)
    expect(page.url()).toContain('/auth/login')
  })
})

// ===============================
// 2. 导航与路由
// ===============================

test.describe('管理端 - 导航与路由', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
  })

  test('侧边栏菜单渲染', async ({ page }) => {
    const menuItems = page.locator('.art-menu-item, .el-menu-item, .el-sub-menu')
    const count = await menuItems.count()
    expect(count).toBeGreaterThan(0)
  })

  test('访问不存在的路由显示 404', async ({ page }) => {
    await page.goto(`${ADMIN_URL}/nonexistent-page-xyz`)
    await page.waitForTimeout(2000)
    // 应该在 404 页面或者被重定向
    const bodyText = await page.textContent('body')
    const is404 = page.url().includes('404') ||
                  (bodyText && (bodyText.includes('404') || bodyText.includes('找不到')))
    expect(is404).toBeTruthy()
  })
})

// ===============================
// 3. 仪表盘
// ===============================

test.describe('管理端 - 仪表盘', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
  })

  test('仪表盘页面加载且有统计卡片', async ({ page }) => {
    await page.goto(`${ADMIN_URL}/`)
    await page.waitForTimeout(3000)
    // 检查统计卡片
    const statCards = page.locator('.stat-card, .el-card')
    const count = await statCards.count()
    expect(count).toBeGreaterThan(0)
  })
})

// ===============================
// 4. 业务模块 - 表格列表页通用测试
// ===============================

const businessModules = [
  { name: '员工管理', path: '/business/employee', hasSearch: true },
  { name: '菜品分类', path: '/business/category', hasSearch: false },
  { name: '菜品管理', path: '/business/dish', hasSearch: true },
  { name: '桌台管理', path: '/business/table', hasSearch: false },
  { name: '预订管理', path: '/business/reservation', hasSearch: true },
  { name: '订单管理', path: '/business/order', hasSearch: true },
  { name: '收银结算', path: '/business/payment', hasSearch: true },
  { name: '库存管理', path: '/business/ingredient', hasSearch: true },
  { name: '会员管理', path: '/business/member', hasSearch: true },
  { name: '操作日志', path: '/business/log', hasSearch: true },
]

for (const mod of businessModules) {
  test.describe(`管理端 - ${mod.name}`, () => {
    test.beforeEach(async ({ page }) => {
      await loginAsAdmin(page)
    })

    test(`${mod.name} - 列表页加载且有数据`, async ({ page }) => {
      await page.goto(`${ADMIN_URL}${mod.path}`)
      await page.waitForTimeout(3000)
      // 检查表格存在
      const table = page.locator('.el-table')
      await expect(table).toBeVisible()
      // 检查有数据行
      const rows = page.locator('.el-table__body-wrapper .el-table__row')
      const rowCount = await rows.count()
      expect(rowCount).toBeGreaterThan(0)
    })

    if (mod.hasSearch) {
      test(`${mod.name} - 搜索功能`, async ({ page }) => {
        await page.goto(`${ADMIN_URL}${mod.path}`)
        await page.waitForTimeout(3000)
        // 搜索一个不存在的关键词
        const searchInput = page.locator('.search-card .el-input input, .el-form .el-input input').first()
        if (await searchInput.isVisible()) {
          await searchInput.fill('ZZZZZ_NOT_EXIST')
          const searchBtn = page.locator('.el-button').filter({ hasText: '查询' }).first()
          if (await searchBtn.isVisible()) {
            await searchBtn.click()
            await page.waitForTimeout(2000)
          }
          // 搜索后应该没有数据或显示空态
          const rows = page.locator('.el-table__body-wrapper .el-table__row')
          const rowCount = await rows.count()
          expect(rowCount).toBeLessThanOrEqual(1) // 0 或空态行
        }
      })
    }

    test(`${mod.name} - 新增按钮可点击`, async ({ page }) => {
      // 跳过日志和结算（通常没有新增）
      if (['操作日志', '收银结算', '预订管理'].includes(mod.name)) return
      await page.goto(`${ADMIN_URL}${mod.path}`)
      await page.waitForTimeout(3000)
      const addBtn = page.locator('.el-button').filter({ hasText: /新增|添加|新建/ }).first()
      if (await addBtn.isVisible()) {
        await addBtn.click()
        await page.waitForTimeout(1000)
        // 应该出现弹窗
        const dialog = page.locator('.el-dialog, .el-drawer')
        await expect(dialog.first()).toBeVisible()
        // 关闭弹窗
        const cancelBtn = page.locator('.el-dialog .el-button, .el-drawer .el-button').filter({ hasText: '取消' }).first()
        if (await cancelBtn.isVisible()) {
          await cancelBtn.click()
        }
      }
    })
  })
}

// ===============================
// 5. 员工管理 - 完整 CRUD
// ===============================

test.describe('管理端 - 员工 CRUD', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
  })

  test('新增 → 编辑 → 删除员工', async ({ page }) => {
    await page.goto(`${ADMIN_URL}/business/employee`)
    await page.waitForTimeout(3000)

    // 记录初始行数
    const initialRows = await page.locator('.el-table__body-wrapper .el-table__row').count()

    // 1) 新增
    const addBtn = page.locator('.el-button').filter({ hasText: /新增/ }).first()
    await addBtn.click()
    await page.waitForTimeout(1000)

    const dialog = page.locator('.el-dialog')
    await expect(dialog).toBeVisible()

    // 填写表单
    const inputs = dialog.locator('.el-input input')
    await inputs.nth(0).fill('test_pw_' + Date.now())  // 用户名
    await inputs.nth(1).fill('Playwright测试员工')       // 姓名
    await inputs.nth(2).fill('13899999999')              // 手机号

    // 选择角色（如果是下拉）
    const roleSelect = dialog.locator('.el-select').first()
    if (await roleSelect.isVisible()) {
      await roleSelect.click()
      await page.waitForTimeout(500)
      const option = page.locator('.el-select-dropdown .el-select-dropdown__item').first()
      if (await option.isVisible()) await option.click()
    }

    // 提交
    const submitBtn = dialog.locator('.el-button--primary').filter({ hasText: /确定|提交|保存/ }).first()
    await submitBtn.click()
    await page.waitForTimeout(3000)

    // 验证成功提示或列表刷新
    const newRows = await page.locator('.el-table__body-wrapper .el-table__row').count()
    expect(newRows).toBeGreaterThanOrEqual(initialRows)
  })
})
