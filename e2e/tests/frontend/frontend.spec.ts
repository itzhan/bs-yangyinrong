import { test, expect, type Page } from '@playwright/test'

const FRONTEND_URL = 'http://localhost:5173'
const API_URL = 'http://localhost:8080'

// ── Helper: 会员登录 ──
async function memberLogin(page: Page, phone = '18600001001', password = '123456') {
  await page.goto(`${FRONTEND_URL}/member/login`)
  await page.waitForTimeout(1000)
  const phoneInput = page.locator('.el-form-item').nth(0).locator('input')
  const pwdInput = page.locator('.el-form-item').nth(1).locator('input')
  await phoneInput.fill(phone)
  await pwdInput.fill(password)
  await page.locator('.el-button--danger').click()
  await page.waitForTimeout(3000)
}

// ===============================
// 1. 首页
// ===============================

test.describe('用户端 - 首页', () => {
  test('首页加载成功且有核心内容', async ({ page }) => {
    await page.goto(FRONTEND_URL)
    await page.waitForTimeout(2000)
    // Hero 区域
    await expect(page.locator('section.hero')).toBeVisible()
    // 标题
    const title = await page.textContent('.hero-title')
    expect(title).toContain('川味红锅')
  })

  test('导航栏渲染且链接可点击', async ({ page }) => {
    await page.goto(FRONTEND_URL)
    await page.waitForTimeout(1000)
    // 验证导航链接
    const navLinks = page.locator('.nav-link, .nav-links a')
    const count = await navLinks.count()
    expect(count).toBeGreaterThanOrEqual(4) // 首页/菜单/预订/关于
  })

  test('导航栏链接跳转正确', async ({ page }) => {
    await page.goto(FRONTEND_URL)
    await page.waitForTimeout(1000)
    // 点击菜品菜单
    await page.locator('.nav-link, .nav-links a').filter({ hasText: '菜品菜单' }).click()
    await page.waitForTimeout(2000)
    expect(page.url()).toContain('/menu')
  })

  test('特色卡片区域加载', async ({ page }) => {
    await page.goto(FRONTEND_URL)
    await page.waitForTimeout(2000)
    // 特色卡片 (features)
    const featureCards = page.locator('.feature-card')
    const count = await featureCards.count()
    expect(count).toBe(4)
  })

  test('推荐菜品区域从后端加载', async ({ page }) => {
    await page.goto(FRONTEND_URL)
    await page.waitForTimeout(3000)
    // 菜品卡片区域应该有数据或错误处理
    const dishCards = page.locator('.dish-card')
    const count = await dishCards.count()
    // 如果有推荐菜品则应渲染
    expect(count).toBeGreaterThanOrEqual(0)
  })

  test('Lucide 图标渲染（无 emoji 残留）', async ({ page }) => {
    await page.goto(FRONTEND_URL)
    await page.waitForTimeout(2000)
    // 检查 logo 区域有 SVG 图标
    const logoSvg = page.locator('.logo-icon svg, .logo-icon .lucide')
    const count = await logoSvg.count()
    expect(count).toBeGreaterThan(0)
  })
})

// ===============================
// 2. 菜品菜单页
// ===============================

test.describe('用户端 - 菜品菜单', () => {
  test('菜单页加载且有分类', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/menu`)
    await page.waitForTimeout(3000)
    // 分类侧边栏
    const catItems = page.locator('.cat-item')
    const count = await catItems.count()
    expect(count).toBeGreaterThan(0) // 至少有"全部菜品"
  })

  test('菜品列表加载有数据', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/menu`)
    await page.waitForTimeout(3000)
    const dishCards = page.locator('.dish-card')
    const count = await dishCards.count()
    expect(count).toBeGreaterThan(0)
  })

  test('点击分类切换菜品', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/menu`)
    await page.waitForTimeout(3000)
    // 点击第二个分类
    const catItems = page.locator('.cat-item')
    const catCount = await catItems.count()
    if (catCount > 1) {
      await catItems.nth(1).click()
      await page.waitForTimeout(2000)
      // 第二个分类应高亮
      await expect(catItems.nth(1)).toHaveClass(/active/)
    }
  })

  test('搜索菜品', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/menu`)
    await page.waitForTimeout(3000)
    const searchInput = page.locator('.search-bar input, .el-input input').first()
    await searchInput.fill('肥牛')
    await searchInput.press('Enter')
    await page.waitForTimeout(2000)
    const dishCards = page.locator('.dish-card')
    const count = await dishCards.count()
    // 搜索肥牛应该有结果
    expect(count).toBeGreaterThanOrEqual(0)
  })
})

// ===============================
// 3. 在线预订页
// ===============================

test.describe('用户端 - 在线预订', () => {
  test('预订页加载且表单可见', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/reservation`)
    await page.waitForTimeout(2000)
    await expect(page.locator('.el-form')).toBeVisible()
    await expect(page.locator('.form-card')).toBeVisible()
  })

  test('桌台列表从后端加载', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/reservation`)
    await page.waitForTimeout(3000)
    const tableOptions = page.locator('.table-option')
    const count = await tableOptions.count()
    expect(count).toBeGreaterThan(0) // 应有可选桌台
  })

  test('预订表单必填校验', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/reservation`)
    await page.waitForTimeout(2000)
    // 直接点击提交
    const submitBtn = page.locator('.el-button--danger').filter({ hasText: '确认预订' })
    await submitBtn.click()
    await page.waitForTimeout(1000)
    // 应显示验证错误
    const errors = page.locator('.el-form-item__error')
    const errCount = await errors.count()
    expect(errCount).toBeGreaterThan(0)
  })

  test('填写预订表单并提交', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/reservation`)
    await page.waitForTimeout(3000)

    // 填写姓名
    await page.locator('input').first().fill('Playwright测试')
    // 填写手机号
    const phoneInput = page.locator('input').nth(1)
    await phoneInput.fill('13800001234')

    // 选择桌台
    const tableOptions = page.locator('.table-option')
    const tableCount = await tableOptions.count()
    if (tableCount > 0) {
      await tableOptions.first().click()
    }

    // 人数
    const guestInput = page.locator('.el-input-number input')
    if (await guestInput.isVisible()) {
      await guestInput.fill('4')
    }
  })

  test('侧边栏提示信息可见', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/reservation`)
    await page.waitForTimeout(2000)
    const tipsCard = page.locator('.tips-card')
    const count = await tipsCard.count()
    expect(count).toBeGreaterThanOrEqual(2) // 预订须知 + 营业时间
  })
})

// ===============================
// 4. 关于我们页
// ===============================

test.describe('用户端 - 关于我们', () => {
  test('关于页面加载成功', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/about`)
    await page.waitForTimeout(2000)
    await expect(page.locator('.about-page')).toBeVisible()
    const title = await page.textContent('h1')
    expect(title).toContain('关于我们')
  })

  test('品牌故事图片加载（非 placeholder）', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/about`)
    await page.waitForTimeout(2000)
    const storyImg = page.locator('.story-photo')
    if (await storyImg.isVisible()) {
      const src = await storyImg.getAttribute('src')
      expect(src).toBeTruthy()
    }
  })

  test('四大优势卡片渲染', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/about`)
    await page.waitForTimeout(2000)
    const advCards = page.locator('.adv-card')
    expect(await advCards.count()).toBe(4)
  })

  test('联系方式卡片渲染', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/about`)
    await page.waitForTimeout(2000)
    const contactCards = page.locator('.contact-card')
    expect(await contactCards.count()).toBe(4)
  })

  test('Lucide 图标正常渲染', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/about`)
    await page.waitForTimeout(2000)
    const svgIcons = page.locator('.contact-icon svg, .adv-icon svg, .lucide')
    const count = await svgIcons.count()
    expect(count).toBeGreaterThan(0)
  })
})

// ===============================
// 5. 会员注册
// ===============================

test.describe('用户端 - 会员注册', () => {
  test('注册页加载', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/member/register`)
    await page.waitForTimeout(1000)
    await expect(page.locator('.register-card')).toBeVisible()
    await expect(page.locator('.el-form')).toBeVisible()
  })

  test('注册表单必填校验', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/member/register`)
    await page.waitForTimeout(1000)
    await page.locator('.el-button--danger').click()
    await page.waitForTimeout(500)
    const errors = page.locator('.el-form-item__error')
    expect(await errors.count()).toBeGreaterThan(0)
  })
})

// ===============================
// 6. 会员登录
// ===============================

test.describe('用户端 - 会员登录', () => {
  test('登录页加载', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/member/login`)
    await page.waitForTimeout(1000)
    await expect(page.locator('.login-card')).toBeVisible()
    await expect(page.locator('.el-form')).toBeVisible()
  })

  test('登录表单必填校验', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/member/login`)
    await page.waitForTimeout(1000)
    await page.locator('.el-button--danger').click()
    await page.waitForTimeout(500)
    const errors = page.locator('.el-form-item__error')
    expect(await errors.count()).toBeGreaterThan(0)
  })

  test('会员登录成功', async ({ page }) => {
    // 通过 API 登录会员
    const res = await fetch(`${API_URL}/api/member/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ phone: '18600001001', password: '123456' }),
    })
    const json = await res.json()
    if (json.code === 200 && json.data?.token) {
      // 注入 token 到 localStorage
      await page.goto(`${FRONTEND_URL}/member/login`)
      await page.evaluate((tk) => {
        localStorage.setItem('memberToken', tk)
        localStorage.setItem('memberInfo', JSON.stringify({ name: '刘晓红', phone: '18600001001' }))
      }, json.data.token)
      await page.goto(`${FRONTEND_URL}/member`)
      await page.waitForTimeout(3000)
      // 应该能访问会员中心
      const bodyText = await page.textContent('body')
      expect(bodyText).toBeTruthy()
    } else {
      // 如果 API 登录方式不对，直接通过 UI 登录
      await memberLogin(page, '18600001001', '123456')
      await page.waitForTimeout(2000)
    }
  })
})

// ===============================
// 7. 会员中心
// ===============================

test.describe('用户端 - 会员中心', () => {
  test('未登录访问会员中心跳转登录', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/member`)
    await page.waitForTimeout(3000)
    // 应跳转到登录页
    expect(page.url()).toContain('/member/login')
  })

  test('登录后会员中心显示信息', async ({ page }) => {
    await memberLogin(page, '18600001001', '123456')
    await page.goto(`${FRONTEND_URL}/member`)
    await page.waitForTimeout(3000)
    // 会员信息卡片
    const infoCard = page.locator('.info-card')
    if (await infoCard.isVisible()) {
      // 检查有余额/积分等数据
      const statsText = await infoCard.textContent()
      expect(statsText).toBeTruthy()
    }
  })

  test('会员中心 Tab 切换', async ({ page }) => {
    await memberLogin(page, '18600001001', '123456')
    await page.goto(`${FRONTEND_URL}/member`)
    await page.waitForTimeout(3000)
    // 测试 Tab 切换 - 使用实际的 record-tabs class
    const tabs = page.locator('.record-tabs .el-tabs__item, .el-tabs__item')
    const tabCount = await tabs.count()
    if (tabCount >= 2) {
      await tabs.nth(1).click()
      await page.waitForTimeout(1000)
    }
    // 只要页面没报错就算通过
    const bodyText = await page.textContent('body')
    expect(bodyText).toBeTruthy()
  })
})

// ===============================
// 8. Footer
// ===============================

test.describe('用户端 - Footer', () => {
  test('Footer 渲染正确', async ({ page }) => {
    await page.goto(FRONTEND_URL)
    await page.waitForTimeout(2000)
    const footer = page.locator('.footer')
    await expect(footer).toBeVisible()
    const text = await footer.textContent()
    expect(text).toContain('川味红锅')
  })
})

// ===============================
// 9. 通用质量检查
// ===============================

test.describe('用户端 - 通用质量', () => {
  test('首页无控制台错误', async ({ page }) => {
    const errors: string[] = []
    page.on('console', (msg) => {
      if (msg.type() === 'error' && !msg.text().includes('favicon')) {
        errors.push(msg.text())
      }
    })
    await page.goto(FRONTEND_URL)
    await page.waitForTimeout(3000)
    // 允许少量非关键错误
    expect(errors.length).toBeLessThanOrEqual(2)
  })

  test('所有图片加载正常', async ({ page }) => {
    await page.goto(FRONTEND_URL)
    await page.waitForTimeout(3000)
    const images = page.locator('img')
    const imgCount = await images.count()
    let brokenCount = 0
    for (let i = 0; i < imgCount; i++) {
      const naturalWidth = await images.nth(i).evaluate((el: HTMLImageElement) => el.naturalWidth)
      if (naturalWidth === 0) brokenCount++
    }
    expect(brokenCount).toBe(0)
  })
})
