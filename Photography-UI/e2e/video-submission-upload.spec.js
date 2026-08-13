import { expect, test } from '@playwright/test'

const makeVideoFile = name => ({
  name,
  mimeType: 'video/mp4',
  buffer: Buffer.alloc(1024, 1)
})

test('video master picker keeps one clear file state on mobile', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/submission', { waitUntil: 'networkidle' })

  const uploadItem = page.locator('.upload-item')
  const input = uploadItem.locator('input[type="file"]')
  const firstFileName = '校园迎新晚会最终审核版本-2026.mp4'
  const replacementFileName = 'campus-story-revised.mp4'

  await expect(uploadItem.locator('.upload-empty')).toBeVisible()
  await expect(uploadItem.locator('.el-upload-list')).toHaveCount(0)
  await uploadItem.scrollIntoViewIfNeeded()
  await page.screenshot({ path: testInfo.outputPath('video-upload-empty-mobile.png'), fullPage: true })

  await input.setInputFiles(makeVideoFile(firstFileName))
  await expect(uploadItem.getByText(firstFileName, { exact: true })).toHaveCount(1)
  await expect(uploadItem.getByRole('button', { name: '替换' })).toBeVisible()
  await expect(uploadItem.getByRole('button', { name: '移除视频' })).toBeVisible()

  const replacementChooser = page.waitForEvent('filechooser')
  await uploadItem.getByRole('button', { name: '替换' }).click()
  await (await replacementChooser).setFiles(makeVideoFile(replacementFileName))
  await expect(uploadItem.getByText(replacementFileName, { exact: true })).toHaveCount(1)
  await expect(uploadItem.getByText(firstFileName, { exact: true })).toHaveCount(0)

  const overflow = await page.evaluate(() => ({
    body: document.body.scrollWidth - document.body.clientWidth,
    root: document.documentElement.scrollWidth - document.documentElement.clientWidth
  }))
  expect(overflow.body).toBeLessThanOrEqual(1)
  expect(overflow.root).toBeLessThanOrEqual(1)
  await page.screenshot({ path: testInfo.outputPath('video-upload-selected-mobile.png'), fullPage: true })

  await uploadItem.getByRole('button', { name: '移除视频' }).click()
  await expect(uploadItem.locator('.upload-empty')).toBeVisible()
  await expect(uploadItem.getByText(replacementFileName, { exact: true })).toHaveCount(0)
})
