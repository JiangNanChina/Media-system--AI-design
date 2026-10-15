import { expect, test } from '@playwright/test'
import { createServer } from 'node:http'

const testFile = Buffer.alloc(8 * 1024 * 1024, 1)
let uploadServer
let uploadServerUrl

test.beforeAll(async () => {
  uploadServer = createServer((request, response) => {
    request.on('data', () => {
      request.pause()
      setTimeout(() => request.resume(), 20)
    })
    request.on('end', () => {
      const isJoinApplication = request.url.includes('join-applications')
      const data = isJoinApplication
        ? { applicationNumber: 'TEST-JOIN-001' }
        : { submissionNumber: 'TEST-VIDEO-001' }

      setTimeout(() => {
        response.writeHead(200, {
          'Access-Control-Allow-Origin': 'http://127.0.0.1:3000',
          'Access-Control-Allow-Credentials': 'true',
          'Content-Type': 'application/json'
        })
        response.end(JSON.stringify({ success: true, data }))
      }, 800)
    })
  })

  await new Promise(resolve => uploadServer.listen(0, '127.0.0.1', resolve))
  uploadServerUrl = `http://127.0.0.1:${uploadServer.address().port}`
})

test.afterAll(async () => {
  await new Promise(resolve => uploadServer.close(resolve))
})

const useSlowUploadReceiver = async (page, url) => {
  await page.route(url, route => route.continue({
    url: `${uploadServerUrl}${new URL(route.request().url()).pathname}`
  }))
}

const expectDialogWithinViewport = async (page, dialog) => {
  const box = await dialog.boundingBox()
  const viewport = page.viewportSize()
  expect(box).not.toBeNull()
  expect(box.x).toBeGreaterThanOrEqual(0)
  expect(box.y).toBeGreaterThanOrEqual(0)
  expect(box.x + box.width).toBeLessThanOrEqual(viewport.width)
  expect(box.y + box.height).toBeLessThanOrEqual(viewport.height)

  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth)
  expect(overflow).toBeLessThanOrEqual(1)
}

test('join application shows portfolio upload progress next to the selected file', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 375, height: 812 })
  await useSlowUploadReceiver(page, '**/api/join-applications/public')
  await page.route('**/api/colleges/list', route => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({ success: true, data: [{ id: 1, name: '新闻传播学院' }] })
  }))

  await page.goto('/join-us', { waitUntil: 'networkidle' })
  await page.getByPlaceholder('请输入真实姓名').fill('测试用户')
  await page.getByPlaceholder('example@qq.com').fill('10000@qq.com')
  await page.getByPlaceholder('请输入手机号').fill('13800138000')
  await page.getByText('其他', { exact: true }).click()
  await page.locator('.el-select').first().click()
  await page.locator('.el-select-dropdown__item').first().click()
  await page.getByPlaceholder('请输入专业').fill('数字媒体')
  await page.getByPlaceholder('请输入班级').fill('数字媒体一班')
  await page.getByPlaceholder('可以介绍你的兴趣方向、过往经历、想加入融媒体中心的原因').fill('端到端上传进度测试内容')
  await page.locator('input[type="file"]').first().setInputFiles({
    name: 'portfolio-preview.png',
    mimeType: 'image/png',
    buffer: testFile
  })
  await page.locator('.consent-row').click()
  await page.getByRole('button', { name: '提交申请' }).click()

  const progress = page.locator('.selected-file .file-upload-progress')
  await expect.poll(async () => Number(await progress.getAttribute('aria-label').then(label => label.match(/(\d+)%/)?.[1]))).toBeGreaterThan(0)
  await expect(progress).toContainText('作品上传完成')
  await expect(progress).toContainText('100%')
  await expect(progress).toContainText('正在等待服务器确认')
  await expect(page.locator('.upload-dropzone')).toHaveClass(/is-disabled/)
  await expect(page.locator('.selected-file')).toBeInViewport()
  await page.screenshot({ path: testInfo.outputPath('join-upload-progress.png'), fullPage: true })

  const dialog = page.getByRole('dialog')
  await expect(dialog).toContainText('申请已提交')
  await expect(dialog).toContainText('APPLICATION RECEIPT / RECEIVED')
  await expect(dialog).toContainText('TEST-JOIN-001')
  await dialog.getByRole('button', { name: '复制申请编号' }).click()
  await expect(dialog).toContainText('已复制')
  await expectDialogWithinViewport(page, dialog)
  await page.screenshot({ path: testInfo.outputPath('join-success-dialog.png') })
})

test('video submission shows master upload progress next to the selected file', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 1440, height: 900 })
  await useSlowUploadReceiver(page, '**/api/submissions/public')

  await page.goto('/submission', { waitUntil: 'networkidle' })
  await page.getByPlaceholder('请输入视频标题').fill('上传进度测试视频')
  await page.getByPlaceholder('请输入真实姓名').fill('测试用户')
  await page.getByPlaceholder('请输入手机号').fill('13800138000')
  await page.getByPlaceholder('请输入QQ邮箱').fill('10000@qq.com')
  await page.getByPlaceholder('请输入6位验证码').fill('123456')
  await page.locator('input[type="file"]').first().setInputFiles({
    name: 'campus-story.mp4',
    mimeType: 'video/mp4',
    buffer: testFile
  })
  await page.locator('.consent-row').click()
  await page.getByRole('button', { name: '提交视频' }).click()

  const progress = page.locator('.selected-file .file-upload-progress')
  await expect.poll(async () => Number(await progress.getAttribute('aria-label').then(label => label.match(/(\d+)%/)?.[1]))).toBeGreaterThan(0)
  await expect(progress).toContainText('视频上传完成')
  await expect(progress).toContainText('100%')
  await expect(progress).toContainText('正在等待服务器确认')
  await expect(page.locator('.upload-dropzone')).toHaveClass(/is-disabled/)
  await expect(page.locator('.selected-file')).toBeInViewport()
  await page.screenshot({ path: testInfo.outputPath('video-upload-progress.png'), fullPage: true })

  const dialog = page.getByRole('dialog')
  await expect(dialog).toContainText('投稿成功')
  await expect(dialog).toContainText('SUBMISSION RECEIPT / RECEIVED')
  await expect(dialog).toContainText('TEST-VIDEO-001')
  await dialog.getByRole('button', { name: '复制投稿编号' }).click()
  await expect(dialog).toContainText('已复制')
  await expectDialogWithinViewport(page, dialog)
  await page.screenshot({ path: testInfo.outputPath('submission-success-dialog.png') })
})
