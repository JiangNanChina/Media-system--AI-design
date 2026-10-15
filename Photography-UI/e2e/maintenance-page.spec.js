import { expect, test } from '@playwright/test'

const maintenanceStatus = {
  enabled: true,
  unlocked: false,
  title: '影像系统维护中',
  message: '我们正在整理校园影像档案，管理平台将于维护结束后恢复开放。'
}

const landingSettings = {
  'landing.brand.title': '校园融媒体中心',
  'site.subtitle': 'CAMPUS MEDIA CENTER',
  'site.logo': '/logo.svg',
  'landing.hero.media': '',
  'landing.hero.media_type': 'image'
}

const mockMaintenanceApis = async (page, state = { unlocked: false }) => {
  await page.route('**/api/maintenance/public/status', route => route.fulfill({
    contentType: 'application/json',
    body: JSON.stringify({ success: true, data: { ...maintenanceStatus, unlocked: state.unlocked } })
  }))
  await page.route('**/api/landing/public', route => route.fulfill({
    contentType: 'application/json',
    body: JSON.stringify({ success: true, data: { settings: landingSettings } })
  }))
}

test('maintenance page keeps site branding while using its own service-status layout', async ({ page }, testInfo) => {
  const state = { unlocked: false }
  await mockMaintenanceApis(page, state)

  let unlockPayload
  await page.route('**/api/maintenance/public/unlock', async route => {
    unlockPayload = route.request().postDataJSON()
    state.unlocked = true
    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({ success: true, data: { unlocked: true } })
    })
  })

  await page.setViewportSize({ width: 1440, height: 900 })
  await page.goto('/maintenance?redirect=/dashboard', { waitUntil: 'networkidle' })

  await expect(page.locator('.maintenance-page')).toHaveClass(/is-ready/)
  await expect(page.locator('.maintenance-brand')).toContainText('校园融媒体中心')
  await expect(page.locator('.maintenance-brand')).toContainText('CAMPUS MEDIA CENTER')
  await expect(page.locator('.brand-mark img')).toHaveAttribute('src', '/logo.svg')
  await expect(page.locator('#maintenance-title')).toHaveText(maintenanceStatus.title)
  await expect(page.locator('.maintenance-message')).toHaveText(maintenanceStatus.message)
  await expect(page.locator('.service-board')).toContainText('公开站点')
  await expect(page.locator('.service-board')).toContainText('管理平台')
  await expect(page.locator('.access-panel')).toContainText('维护人员入口')
  await expect(page.locator('.maintenance-media, .viewfinder-frame, .film-edge')).toHaveCount(0)
  await expect(page.locator('.unlock-control .el-input__wrapper')).toHaveCSS('background-color', 'rgba(255, 255, 255, 0.08)')
  await expect(page.getByRole('button', { name: '验证并继续' })).toHaveCSS('background-color', 'rgba(255, 255, 255, 0.1)')
  await page.screenshot({ path: testInfo.outputPath('maintenance-desktop.png'), fullPage: true })

  const passwordInput = page.locator('#maintenance-password')
  const submitButton = page.getByRole('button', { name: '验证并继续' })
  await expect(submitButton).toBeDisabled()
  await passwordInput.fill('1234567')
  await expect(submitButton).toBeDisabled()
  await passwordInput.fill('temporary-pass')
  await expect(submitButton).toBeEnabled()
  await submitButton.click()

  await expect.poll(() => unlockPayload).toEqual({ password: 'temporary-pass' })
  await expect(page).toHaveURL(/\/login\?redirect=\/dashboard$|\/dashboard$/)
})

test('maintenance page remains readable without overflow on mobile', async ({ page }, testInfo) => {
  await mockMaintenanceApis(page)
  await page.setViewportSize({ width: 375, height: 812 })
  await page.goto('/maintenance', { waitUntil: 'networkidle' })

  await expect(page.locator('.maintenance-page')).toBeVisible()
  await expect(page.locator('.maintenance-content')).toBeInViewport()
  await expect(page.locator('.unlock-form')).toBeVisible()

  const overflow = await page.evaluate(() => ({
    body: document.body.scrollWidth - document.body.clientWidth,
    root: document.documentElement.scrollWidth - document.documentElement.clientWidth
  }))
  expect(overflow.body).toBeLessThanOrEqual(1)
  expect(overflow.root).toBeLessThanOrEqual(1)

  const contentGeometry = await page.locator('.maintenance-content').evaluate(element => {
    const rect = element.getBoundingClientRect()
    return { left: rect.left, right: rect.right, viewportWidth: window.innerWidth }
  })
  expect(contentGeometry.left).toBeGreaterThanOrEqual(0)
  expect(contentGeometry.right).toBeLessThanOrEqual(contentGeometry.viewportWidth)

  await page.screenshot({ path: testInfo.outputPath('maintenance-mobile.png'), fullPage: true })
})

test('maintenance status motion runs and honors reduced-motion preferences', async ({ page }) => {
  await mockMaintenanceApis(page)
  await page.emulateMedia({ reducedMotion: 'no-preference' })
  await page.goto('/maintenance', { waitUntil: 'networkidle' })

  const runningAnimations = await page.locator('.maintenance-page').evaluate(element => {
    return element.getAnimations({ subtree: true })
      .map(animation => animation.animationName)
      .filter(Boolean)
  })
  const expectedAnimations = [
    'background-diagnostic-sweep',
    'status-signal',
    'maintenance-gear',
    'progress-sweep',
    'panel-diagnostic-scan'
  ]
  expectedAnimations.forEach(name => {
    expect(runningAnimations.some(animationName => animationName.startsWith(name))).toBe(true)
  })

  await page.emulateMedia({ reducedMotion: 'reduce' })
  const reducedAnimations = await page.locator('.maintenance-page').evaluate(element => {
    return element.getAnimations({ subtree: true })
      .map(animation => animation.animationName)
      .filter(Boolean)
  })
  expect(reducedAnimations).toEqual([])
})
