import { expect, test } from '@playwright/test'

const pages = [
  { path: '/', marker: '.landing-page' },
  { path: '/login', marker: '.auth-container' },
  { path: '/submission', marker: '.public-form-page' },
  { path: '/maintenance', marker: '.maintenance-page' }
]

for (const width of [375, 768, 1024, 1440]) {
  for (const target of pages) {
    test(`${target.path} has no overflow or text overlap at ${width}px`, async ({ page }) => {
      await page.setViewportSize({ width, height: width === 375 ? 812 : 900 })
      await page.goto(target.path, { waitUntil: 'networkidle' })
      await expect(page.locator(target.marker)).toBeVisible()

      const overflow = await page.evaluate(() => ({
        body: document.body.scrollWidth - document.body.clientWidth,
        root: document.documentElement.scrollWidth - document.documentElement.clientWidth
      }))
      expect(overflow.body).toBeLessThanOrEqual(1)
      expect(overflow.root).toBeLessThanOrEqual(1)

      const overlaps = await page.evaluate(() => {
        const selectors = 'h1,h2,h3,p,a,button,label,.el-form-item__label'
        const elements = [...document.querySelectorAll(selectors)].filter(element => {
          const style = getComputedStyle(element)
          const rect = element.getBoundingClientRect()
          return style.visibility !== 'hidden' && style.display !== 'none' && rect.width > 1 && rect.height > 1
        })
        const collisions = []
        for (let i = 0; i < elements.length; i++) {
          for (let j = i + 1; j < elements.length; j++) {
            const first = elements[i]
            const second = elements[j]
            if (first.contains(second) || second.contains(first)) continue
            const a = first.getBoundingClientRect()
            const b = second.getBoundingClientRect()
            const intersectionWidth = Math.min(a.right, b.right) - Math.max(a.left, b.left)
            const intersectionHeight = Math.min(a.bottom, b.bottom) - Math.max(a.top, b.top)
            if (intersectionWidth > 3 && intersectionHeight > 3) {
              collisions.push(`${first.tagName}:${first.textContent?.trim().slice(0, 20)} <> ${second.tagName}:${second.textContent?.trim().slice(0, 20)}`)
            }
          }
        }
        return collisions
      })
      expect(overlaps).toEqual([])
    })
  }
}
