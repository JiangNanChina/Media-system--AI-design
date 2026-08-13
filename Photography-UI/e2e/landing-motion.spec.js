import { expect, test } from '@playwright/test'

test('landing interactions respond to pointer and reading progress', async ({ page }, testInfo) => {
  await page.emulateMedia({ reducedMotion: 'no-preference' })
  await page.setViewportSize({ width: 1440, height: 900 })
  await page.goto('/', { waitUntil: 'networkidle' })

  const hero = page.locator('.hero')
  const heroBounds = await hero.boundingBox()
  await page.mouse.move(heroBounds.x + heroBounds.width * 0.78, heroBounds.y + heroBounds.height * 0.42)

  await expect(hero).toHaveClass(/is-tracking/)
  await expect.poll(() => hero.evaluate(element => element.style.getPropertyValue('--hero-focus-x'))).not.toBe('')
  await expect.poll(() => hero.evaluate(element => element.style.getPropertyValue('--hero-pan-x'))).not.toBe('0px')
  await page.screenshot({ path: testInfo.outputPath('landing-hero-focus.png') })

  const featureCard = page.locator('.feature-card').first()
  await featureCard.scrollIntoViewIfNeeded()
  await expect(featureCard).toHaveClass(/is-reveal-complete/)
  const cardBounds = await featureCard.boundingBox()
  await page.mouse.move(cardBounds.x + cardBounds.width * 0.72, cardBounds.y + cardBounds.height * 0.34)

  await expect.poll(() => featureCard.evaluate(element => element.style.getPropertyValue('--pointer-x'))).not.toBe('')
  await expect.poll(() => featureCard.evaluate(element => element.style.getPropertyValue('--media-shift-x'))).not.toBe('0px')
  await page.screenshot({ path: testInfo.outputPath('landing-card-inspection.png') })

  await page.evaluate(() => window.scrollTo(0, document.documentElement.scrollHeight * 0.7))
  await expect.poll(async () => Number(await page.locator('.landing-page').evaluate(element => element.style.getPropertyValue('--landing-progress')))).toBeGreaterThan(0.4)
})

test('landing interactions honor reduced motion', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' })
  await page.setViewportSize({ width: 1440, height: 900 })
  await page.goto('/', { waitUntil: 'networkidle' })

  const hero = page.locator('.hero')
  const heroBounds = await hero.boundingBox()
  await page.mouse.move(heroBounds.x + heroBounds.width * 0.8, heroBounds.y + heroBounds.height * 0.4)

  await expect(hero).not.toHaveClass(/is-tracking/)
  await expect(hero).toHaveCSS('--hero-pan-x', '0px')
  await expect(page.locator('.motion-surface').first(), 'pointer spotlight is disabled').toHaveCSS('--media-shift-x', '0px')
  const titleTransform = await page.locator('.hero-content > h1').evaluate(element => {
    const matrix = new DOMMatrixReadOnly(getComputedStyle(element).transform)
    return { scaleX: matrix.a, scaleY: matrix.d, translateX: matrix.e, translateY: matrix.f }
  })
  expect(titleTransform).toEqual({ scaleX: 1, scaleY: 1, translateX: 0, translateY: 0 })
})
