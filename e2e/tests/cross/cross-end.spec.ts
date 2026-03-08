import { test, expect, type Page } from '@playwright/test'

/**
 * 跨端一致性验证
 * 管理端操作 → 用户端验证，用户端操作 → 管理端验证
 */

const ADMIN_URL = 'http://localhost:3006'
const FRONTEND_URL = 'http://localhost:5173'
const API_URL = 'http://localhost:8080'

// ── Helper: API 辅助函数 ──
async function apiRequest(method: string, path: string, token: string, body?: any) {
  const res = await fetch(`${API_URL}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: body ? JSON.stringify(body) : undefined,
  })
  return res.json()
}

async function getAdminToken(): Promise<string> {
  const res = await fetch(`${API_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: '123456' }),
  })
  const json = await res.json()
  return json.data.token
}

// ===============================
// 1. 管理端新增菜品 → 用户端可见
// ===============================

test.describe('跨端一致性', () => {
  test('管理端新增菜品 → 用户端菜单可见', async ({ browser }) => {
    const token = await getAdminToken()
    const testDishName = `跨端测试菜品_${Date.now()}`

    // 通过 API 创建菜品（管理端操作）
    const createRes = await apiRequest('POST', '/api/admin/dishes', token, {
      name: testDishName,
      categoryId: 1,
      price: 99.99,
      spicyLevel: 2,
      description: '跨端测试',
      status: 1,
    })
    expect(createRes.code).toBe(200)

    // 在用户端验证菜品出现
    const context = await browser.newContext()
    const page = await context.newPage()
    await page.goto(`${FRONTEND_URL}/menu`)
    await page.waitForTimeout(3000)

    // 搜索该菜品
    const searchInput = page.locator('.search-bar input, .el-input input').first()
    await searchInput.fill(testDishName)
    await searchInput.press('Enter')
    await page.waitForTimeout(2000)

    const bodyText = await page.textContent('body')
    expect(bodyText).toContain(testDishName)

    // 清理：通过 API 删除
    const listRes = await apiRequest('GET', `/api/admin/dishes?page=1&size=100&keyword=${encodeURIComponent(testDishName)}`, token)
    const records = listRes.data?.records || listRes.data || []
    for (const dish of records) {
      if (dish.name === testDishName) {
        await apiRequest('DELETE', `/api/admin/dishes/${dish.id}`, token)
      }
    }

    await context.close()
  })

  test('管理端删除菜品 → 用户端不可见', async ({ browser }) => {
    const token = await getAdminToken()
    const testDishName = `跨端删除测试_${Date.now()}`

    // 创建
    await apiRequest('POST', '/api/admin/dishes', token, {
      name: testDishName,
      categoryId: 1,
      price: 50,
      status: 1,
    })

    // 查找并删除
    const listRes = await apiRequest('GET', `/api/admin/dishes?page=1&size=100&keyword=${encodeURIComponent(testDishName)}`, token)
    const records = listRes.data?.records || listRes.data || []
    const dish = records.find((d: any) => d.name === testDishName)
    if (dish) {
      await apiRequest('DELETE', `/api/admin/dishes/${dish.id}`, token)
    }

    // 用户端验证不可见
    const context = await browser.newContext()
    const page = await context.newPage()
    await page.goto(`${FRONTEND_URL}/menu`)
    await page.waitForTimeout(3000)

    const searchInput = page.locator('.search-bar input, .el-input input').first()
    await searchInput.fill(testDishName)
    await searchInput.press('Enter')
    await page.waitForTimeout(2000)

    const bodyText = await page.textContent('body')
    expect(bodyText).not.toContain(testDishName)

    await context.close()
  })

  test('用户端预订 → 管理端可见', async ({ browser }) => {
    const token = await getAdminToken()

    // 先获取可用桌台
    const tablesRes = await fetch(`${API_URL}/api/public/tables`)
    const tablesJson = await tablesRes.json()
    const tables = tablesJson.data || []
    if (tables.length === 0) {
      test.skip()
      return
    }

    // 用户端提交预订
    const testName = `跨端预订_${Date.now()}`
    const futureTime = new Date(Date.now() + 2 * 86400000).toISOString().replace(/\.\d+Z/, '')
    const submitRes = await fetch(`${API_URL}/api/public/reservations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        customerName: testName,
        customerPhone: '13800009999',
        reservationTime: futureTime,
        guestCount: 4,
        tableId: tables[0].id,
        remark: '跨端测试',
      }),
    })
    const submitJson = await submitRes.json()
    expect(submitJson.code).toBe(200)

    // 管理端验证预订出现
    const reserveRes = await apiRequest(
      'GET',
      `/api/admin/reservations?page=1&size=100`,
      token
    )
    const reservations = reserveRes.data?.records || reserveRes.data || []
    const found = reservations.some((r: any) => r.customerName === testName)
    expect(found).toBeTruthy()
  })
})
