import { expect, test } from '@playwright/test'

const waitForPage = async (page, path, marker) => {
  await page.goto(path, { waitUntil: 'networkidle' })
  await expect(page.locator(marker)).toBeVisible()
  await expect(page.getByTestId('route-transition')).toBeHidden()
}

test('public page typography and dynamic numbers use stable rendering', async ({ page }) => {
  const pages = [
    ['/', '.landing-page', '.frame-number'],
    ['/register', '.auth-container', '.email-code-button'],
    ['/forgot-password', '.recovery-page', '.recovery-code-button'],
    ['/join-us', '.join-page', '.visual-caption strong'],
    ['/submission', '.submission-page', '.code-row .el-button']
  ]

  for (const [path, marker, numberSelector] of pages) {
    await waitForPage(page, path, marker)
    await expect(page.locator('h1')).toHaveCSS('text-wrap', 'balance')
    await expect(page.locator(numberSelector).first()).toHaveCSS('font-variant-numeric', /tabular-nums/)
  }
})

test('mobile public controls keep touch targets and contextual icon endpoints', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 812 })
  await waitForPage(page, '/', '.landing-page')

  const menuButton = page.locator('.nav-menu-button')
  await expect(menuButton).toHaveCSS('width', '44px')
  await expect(menuButton).toHaveCSS('height', '44px')

  const menuIcons = page.locator('.menu-icon-stack .el-icon')
  await expect(menuIcons.nth(0)).toHaveCSS('scale', '0.25')
  await expect(menuIcons.nth(0)).toHaveCSS('filter', 'blur(4px)')
  await expect(menuIcons.nth(1)).toHaveCSS('scale', '1')

  await menuButton.click()
  await expect(menuIcons.nth(0)).toHaveCSS('scale', '1')
  await expect(menuIcons.nth(0)).toHaveCSS('filter', 'blur(0px)')
  await expect(menuIcons.nth(1)).toHaveCSS('scale', '0.25')

  for (const [path, marker] of [
    ['/join-us', '.join-page'],
    ['/submission', '.submission-page']
  ]) {
    await waitForPage(page, path, marker)
    const backButton = page.locator('.nav-link-back')
    await expect(backButton).toHaveCSS('width', '44px')
    await expect(backButton).toHaveCSS('height', '44px')
    await expect(backButton.locator('.el-icon')).not.toHaveCSS('transition-property', 'all')
  }
})

test('public buttons press to 0.96 and elevated shells use layered shadows', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 812 })
  await waitForPage(page, '/forgot-password', '.recovery-page')

  const sendButton = page.locator('.recovery-code-button')
  await sendButton.scrollIntoViewIfNeeded()
  const box = await sendButton.boundingBox()
  await page.mouse.move(box.x + box.width / 2, box.y + box.height / 2)
  await page.mouse.down()
  await expect(sendButton).toHaveCSS('transform', /matrix\(0\.96, 0, 0, 0\.96, 0, 0\)/)
  await page.mouse.up()

  const shellShadow = await page.locator('.recovery-shell').evaluate(element => getComputedStyle(element).boxShadow)
  expect(shellShadow).toContain('0px 0px 0px 1px')
  expect(shellShadow).toContain('0px 10px 24px -14px')
  expect(shellShadow).toContain('0px 28px 70px')
})
