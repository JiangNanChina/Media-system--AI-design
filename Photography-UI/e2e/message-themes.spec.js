import { expect, test } from '@playwright/test'

const showMessage = (page, type, message) => page.evaluate(async ({ messageType, messageText }) => {
  const { ElMessage } = await import('/node_modules/.vite/deps/element-plus.js')
  ElMessage({ type: messageType, message: messageText, duration: 0, showClose: true })
}, { messageType: type, messageText: message })

const closeMessages = page => page.evaluate(async () => {
  const { ElMessage } = await import('/node_modules/.vite/deps/element-plus.js')
  ElMessage.closeAll()
})

test.beforeEach(async ({ page }) => {
  await page.route('**/api/maintenance/public/status', route => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({ success: true, data: { enabled: false, unlocked: false } })
  }))
})

test('messages follow public and admin route themes', async ({ page }) => {
  const cases = [
    ['/', 'landing', '2px'],
    ['/join-us', 'join', '2px'],
    ['/submission', 'submission', '2px'],
    ['/login', 'auth', '2px'],
    ['/register', 'auth', '2px'],
    ['/forgot-password', 'recovery', '2px'],
    ['/login-test', 'admin', '14px']
  ]

  for (const [path, theme, radius] of cases) {
    await page.goto(path, { waitUntil: 'networkidle' })
    await expect(page.locator('body')).toHaveAttribute('data-message-theme', theme)
    await showMessage(page, 'success', `${theme} message`)

    const toast = page.locator('.el-message').last()
    await expect(toast).toBeVisible()
    await expect(toast).toHaveCSS('border-radius', radius)
    await expect(toast.locator('.el-message__icon')).toHaveCSS('width', theme === 'admin' ? '30px' : '32px')

    await closeMessages(page)
  }
})

test('mobile messages wrap long content without leaving the viewport', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 812 })
  await page.goto('/join-us', { waitUntil: 'networkidle' })
  await showMessage(page, 'error', '作品上传失败，请检查网络连接后重新提交申请。文件仍已保留。')

  const toast = page.locator('.el-message').last()
  await expect(toast).toBeVisible()
  await expect(toast).toHaveCSS('width', '351px')
  await expect(toast.locator('.el-message__closeBtn')).toHaveCSS('width', '44px')
  await expect(toast.locator('.el-message__closeBtn')).toHaveCSS('height', '44px')

  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth)
  expect(overflow).toBeLessThanOrEqual(1)
})
