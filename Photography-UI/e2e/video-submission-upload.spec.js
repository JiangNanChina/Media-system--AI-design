import { expect, test } from '@playwright/test'
import { mkdir, mkdtemp, rm, writeFile } from 'node:fs/promises'
import { tmpdir } from 'node:os'
import { join } from 'node:path'

const makeVideoFile = name => ({
  name,
  mimeType: 'video/mp4',
  buffer: Buffer.alloc(1024, 1)
})

const makeImageFile = name => ({
  name,
  mimeType: 'image/png',
  buffer: Buffer.alloc(1024, 1)
})

test('portfolio picker adds multiple files and supports per-file removal on mobile', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/join-us', { waitUntil: 'networkidle' })

  const portfolioItem = page.locator('section[aria-labelledby="portfolio-title"]')
  const input = portfolioItem.locator('input[type="file"]').first()
  const firstFileName = '融媒体中心招新作品最终版本-2026.png'
  const secondFileName = 'portfolio-revised.png'

  await expect(portfolioItem.locator('.upload-empty')).toBeVisible()
  await portfolioItem.scrollIntoViewIfNeeded()
  await page.screenshot({ path: testInfo.outputPath('portfolio-upload-empty-mobile.png'), fullPage: true })

  await input.setInputFiles(makeImageFile(firstFileName))
  await expect(portfolioItem.locator('.file-manifest__info strong', { hasText: firstFileName })).toHaveCount(1)
  await expect(portfolioItem.getByText('WORK READY · PNG', { exact: true })).toBeVisible()
  await expect(portfolioItem.getByRole('button', { name: '添加文件', exact: true })).toBeVisible()
  await expect(portfolioItem.getByRole('button', { name: '添加文件夹', exact: true })).toBeVisible()

  const secondChooser = page.waitForEvent('filechooser')
  await portfolioItem.getByRole('button', { name: '添加文件', exact: true }).click()
  await (await secondChooser).setFiles(makeImageFile(secondFileName))
  await expect(portfolioItem.getByText(secondFileName, { exact: true })).toHaveCount(1)
  await expect(portfolioItem.locator('.file-manifest__info strong', { hasText: firstFileName })).toHaveCount(1)
  await expect(portfolioItem.getByText('2 个作品将合并为 ZIP 作品包')).toBeVisible()

  await portfolioItem.getByRole('button', { name: `移除 ${secondFileName}` }).click()
  await expect(portfolioItem.getByText(secondFileName, { exact: true })).toHaveCount(0)
  await expect(portfolioItem.locator('.file-manifest__info strong', { hasText: firstFileName })).toHaveCount(1)

  const overflow = await page.evaluate(() => ({
    body: document.body.scrollWidth - document.body.clientWidth,
    root: document.documentElement.scrollWidth - document.documentElement.clientWidth
  }))
  expect(overflow.body).toBeLessThanOrEqual(1)
  expect(overflow.root).toBeLessThanOrEqual(1)
  await page.screenshot({ path: testInfo.outputPath('portfolio-upload-selected-mobile.png'), fullPage: true })

  await portfolioItem.getByRole('button', { name: '清空全部文件' }).click()
  await expect(portfolioItem.locator('.upload-empty')).toBeVisible()
  await expect(portfolioItem.getByText(firstFileName, { exact: true })).toHaveCount(0)
})

test('video picker adds multiple files without horizontal overflow on mobile', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/submission', { waitUntil: 'networkidle' })

  const uploadItem = page.locator('.upload-item')
  const input = uploadItem.locator('input[type="file"]').first()
  const firstFileName = '校园迎新晚会最终审核版本-2026.mp4'
  const secondFileName = 'campus-story-revised.mp4'

  await expect(uploadItem.locator('.upload-empty')).toBeVisible()
  await uploadItem.scrollIntoViewIfNeeded()
  await page.screenshot({ path: testInfo.outputPath('video-upload-empty-mobile.png'), fullPage: true })

  await input.setInputFiles(makeVideoFile(firstFileName))
  await expect(uploadItem.locator('.file-manifest__info strong', { hasText: firstFileName })).toHaveCount(1)
  await expect(uploadItem.getByRole('button', { name: '添加文件', exact: true })).toBeVisible()

  const secondChooser = page.waitForEvent('filechooser')
  await uploadItem.getByRole('button', { name: '添加文件', exact: true }).click()
  await (await secondChooser).setFiles(makeVideoFile(secondFileName))
  await expect(uploadItem.getByText(secondFileName, { exact: true })).toHaveCount(1)
  await expect(uploadItem.getByText(firstFileName, { exact: true })).toHaveCount(1)
  await expect(uploadItem.getByText('2 个视频将合并为 ZIP 素材包')).toBeVisible()

  const overflow = await page.evaluate(() => ({
    body: document.body.scrollWidth - document.body.clientWidth,
    root: document.documentElement.scrollWidth - document.documentElement.clientWidth
  }))
  expect(overflow.body).toBeLessThanOrEqual(1)
  expect(overflow.root).toBeLessThanOrEqual(1)
  await page.screenshot({ path: testInfo.outputPath('video-upload-selected-mobile.png'), fullPage: true })

  await uploadItem.getByRole('button', { name: `移除 ${secondFileName}` }).click()
  await expect(uploadItem.getByText(secondFileName, { exact: true })).toHaveCount(0)
  await uploadItem.getByRole('button', { name: '清空全部文件' }).click()
  await expect(uploadItem.locator('.upload-empty')).toBeVisible()
  await expect(uploadItem.getByText(firstFileName, { exact: true })).toHaveCount(0)
})

test('folder selection preserves relative paths in the uploaded zip', async ({ page }) => {
  const root = await mkdtemp(join(tmpdir(), 'photography-upload-'))
  const folderName = root.split(/[\\/]/).pop()
  const rawDirectory = join(root, 'campaign', 'raw')
  const finalDirectory = join(root, 'campaign', 'final')
  await mkdir(rawDirectory, { recursive: true })
  await mkdir(finalDirectory, { recursive: true })
  await writeFile(join(rawDirectory, 'first.mp4'), Buffer.alloc(1024, 1))
  await writeFile(join(finalDirectory, 'second.webm'), Buffer.alloc(1024, 2))

  let uploadBody
  await page.route('**/api/submissions/public', async route => {
    uploadBody = route.request().postDataBuffer()
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true, data: { submissionNumber: 'TEST-FOLDER-001' } })
    })
  })

  try {
    await page.goto('/submission', { waitUntil: 'networkidle' })
    await page.getByPlaceholder('请输入视频标题').fill('文件夹投稿测试')
    await page.getByPlaceholder('请输入真实姓名').fill('测试用户')
    await page.getByPlaceholder('请输入手机号').fill('13800138000')
    await page.getByPlaceholder('请输入QQ邮箱').fill('10000@qq.com')
    await page.getByPlaceholder('请输入6位验证码').fill('123456')
    await page.locator('.upload-item input[webkitdirectory]').setInputFiles(root)
    await expect(page.getByText('2 个视频将合并为 ZIP 素材包')).toBeVisible()
    await page.locator('.consent-row').click()
    await page.getByRole('button', { name: '提交视频' }).click()
    await expect(page.getByRole('dialog')).toContainText('TEST-FOLDER-001')

    expect(uploadBody).toBeTruthy()
    expect(uploadBody.toString('latin1')).toMatch(/filename="video-submission-\d{8}T\d{6}\.zip"/)
    expect(uploadBody.indexOf(Buffer.from(`${folderName}/campaign/raw/first.mp4`))).toBeGreaterThan(-1)
    expect(uploadBody.indexOf(Buffer.from(`${folderName}/campaign/final/second.webm`))).toBeGreaterThan(-1)
  } finally {
    await rm(root, { recursive: true, force: true })
  }
})
