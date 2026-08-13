import { expect, test } from '@playwright/test'

test('route navigation uses the archive loading transition', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 })
  await page.goto('/')

  await expect(page.getByTestId('route-transition')).toBeHidden()
  await page.locator('.nav-login').click()

  const transition = page.getByTestId('route-transition')
  await expect(transition).toBeVisible()
  await expect(transition).toHaveCSS('background-color', 'rgb(17, 16, 14)')
  await expect(transition).toHaveCSS('opacity', '1')

  await expect(page.locator('.auth-container')).toBeVisible()
  await expect(transition).toBeHidden()
  await expect(page).toHaveURL(/\/login$/)
})
