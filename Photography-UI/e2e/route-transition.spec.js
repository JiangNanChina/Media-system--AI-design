import { expect, test } from '@playwright/test'

const mockMaintenanceDisabled = page => page.route('**/api/maintenance/public/status', route => route.fulfill({
  contentType: 'application/json',
  body: JSON.stringify({ success: true, data: { enabled: false, unlocked: false } })
}))

test('public route navigation keeps the archive loading transition', async ({ page }) => {
  await mockMaintenanceDisabled(page)
  await page.setViewportSize({ width: 1440, height: 900 })
  await page.goto('/')

  await expect(page.getByTestId('route-transition')).toBeHidden()
  await page.locator('.nav-login').click()

  const transition = page.getByTestId('route-transition')
  await expect(transition).toBeVisible()
  await expect(transition).toHaveAttribute('data-transition-theme', 'archive')
  await expect(transition).toHaveCSS('background-color', 'rgb(17, 16, 14)')
  await expect(transition).toHaveCSS('opacity', '1')
  await expect(transition.getByText('正在切换画面')).toBeVisible()
  await expect(transition.getByText(/登录 \/ PLEASE STAND BY/)).toBeVisible()

  await expect(page.locator('.auth-container')).toBeVisible()
  await expect(transition).toBeHidden()
  await expect(page).toHaveURL(/\/login$/)
})

test('other routes use the admin loading transition', async ({ page }) => {
  await mockMaintenanceDisabled(page)
  await page.goto('/login-test')

  const transition = page.getByTestId('route-transition')
  await expect(transition).toBeVisible()
  await expect(transition).toHaveAttribute('data-transition-theme', 'admin')
  await expect(transition).toHaveCSS('background-color', 'rgba(240, 249, 253, 0.86)')
  await expect(transition.getByText('页面加载中，请稍候')).toBeVisible()
  await expect(transition).toBeHidden()
})

test('maintenance page uses the same archive transition as the landing page', async ({ page }) => {
  await page.route('**/api/maintenance/public/status', route => route.fulfill({
    contentType: 'application/json',
    body: JSON.stringify({
      success: true,
      data: {
        enabled: true,
        unlocked: false,
        title: '系统维护中',
        message: '系统正在维护，请稍后再试。'
      }
    })
  }))
  await page.route('**/api/landing/public', route => route.fulfill({
    contentType: 'application/json',
    body: JSON.stringify({
      success: true,
      data: { settings: { 'landing.brand.title': '融媒体中心' } }
    })
  }))

  await page.goto('/maintenance')

  const transition = page.getByTestId('route-transition')
  await expect(transition).toBeVisible()
  await expect(transition).toHaveAttribute('data-transition-theme', 'archive')
  await expect(transition).toHaveCSS('background-color', 'rgb(17, 16, 14)')
  await expect(transition.getByText(/系统维护 \/ PLEASE STAND BY/)).toBeVisible()
  await expect(page.locator('.maintenance-page')).toBeVisible()
  await expect(transition).toBeHidden()
})
