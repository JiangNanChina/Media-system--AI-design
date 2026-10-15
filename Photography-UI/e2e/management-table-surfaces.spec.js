import { expect, test } from '@playwright/test'

const tokenPayload = Buffer.from(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 })).toString('base64url')
const token = `header.${tokenPayload}.signature`

const submission = {
  id: 1,
  submissionNumber: 'SUB202608140012363166',
  title: '校园短片',
  submitterName: 'JiangNan',
  organization: '未填写单位',
  qqEmail: '2332542730@qq.com',
  originalFilename: '11月30日.mp4',
  fileSize: 26843545,
  status: 'APPROVED'
}

const joinApplication = {
  id: 2,
  applicationNumber: 'JOIN202608140015400070',
  realName: 'JiangNan',
  qqEmail: '2332542730@qq.com',
  phone: '18682734814',
  genderDescription: '男',
  enrollmentYear: 2024,
  college: '信息工程学院',
  major: '大数据技术',
  hasWork: true,
  status: 'INTERVIEW',
  notificationSent: true
}

test.beforeEach(async ({ page }) => {
  let submissionRows = [submission]
  let joinApplicationRows = [joinApplication]

  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname
    const method = route.request().method()
    let data = []

    if (path.endsWith('/auth/refresh')) {
      data = { token, userId: 1, username: 'admin', realName: '测试管理员', role: 'SUPER_ADMIN' }
    } else if (path.endsWith('/maintenance/public/status')) {
      data = { enabled: false, unlocked: true }
    } else if (path.endsWith('/site-config/public')) {
      data = { siteTitle: '融媒体管理系统' }
    } else if (method === 'DELETE' && /\/submissions\/\d+$/.test(path)) {
      submissionRows = submissionRows.filter(item => !path.endsWith(`/${item.id}`))
      data = null
    } else if (method === 'DELETE' && /\/join-applications\/\d+$/.test(path)) {
      joinApplicationRows = joinApplicationRows.filter(item => !path.endsWith(`/${item.id}`))
      data = null
    } else if (path.endsWith('/join-applications/admin/interview-group')) {
      data = { qqGroupNumber: '123456781212' }
    } else if (path.endsWith('/join-applications')) {
      data = { content: joinApplicationRows, totalElements: joinApplicationRows.length }
    } else if (path.endsWith('/submissions')) {
      data = { content: submissionRows, totalElements: submissionRows.length }
    } else if (path.includes('/announcements/')) {
      data = { content: [], totalElements: 0 }
    }

    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({ success: true, data })
    })
  })
})

test('submission and join tables use an opaque management surface', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 1600, height: 1000 })

  for (const [name, path] of [['submission', '/submission-management'], ['join', '/join-applications']]) {
    await page.goto(path, { waitUntil: 'networkidle' })

    const panel = page.locator('.management-table-panel')
    const firstCell = panel.locator('.el-table__body td.el-table__cell').first()
    const footer = panel.locator('.management-table-footer')
    await expect(panel).toBeVisible()
    await expect(panel).toHaveCSS('background-color', 'rgba(255, 255, 255, 0.9)')
    await expect(panel).toHaveCSS('border-radius', '20px')
    await expect(firstCell).toHaveCSS('background-color', 'rgba(255, 255, 255, 0.72)')
    await expect(footer).toHaveCSS('background-color', 'rgba(246, 251, 255, 0.9)')

    await panel.locator('.el-table__body tr').first().hover()
    await expect(firstCell).toHaveCSS('background-color', 'rgba(226, 247, 255, 0.92)')
    await expect(panel.locator('td.el-table-fixed-column--right').first()).toHaveCSS('background-color', 'rgb(226, 247, 255)')
    await expect(panel.getByRole('button', { name: '删除', exact: true })).toHaveCSS('color', 'rgb(198, 69, 88)')
    await page.screenshot({ path: testInfo.outputPath(`${name}-table-surface.png`), fullPage: false })
  }
})

test('management table panel stays within a narrow viewport', async ({ page }) => {
  await page.setViewportSize({ width: 768, height: 900 })
  await page.goto('/join-applications', { waitUntil: 'networkidle' })
  await expect(page.locator('.management-table-panel')).toBeVisible()
  await expect(page.locator('.management-table-footer')).toBeVisible()

  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth)
  expect(overflow).toBeLessThanOrEqual(1)
})

test('super admin can cancel and confirm permanent deletion in both lists', async ({ page }, testInfo) => {
  for (const [path, recordText, warningText] of [
    ['/submission-management', submission.submissionNumber, '数据库记录和服务器中的视频文件将一并删除'],
    ['/join-applications', joinApplication.applicationNumber, '数据库记录和服务器中的作品文件将一并删除']
  ]) {
    await page.goto(path, { waitUntil: 'networkidle' })

    const panel = page.locator('.management-table-panel')
    const deleteButton = panel.getByRole('button', { name: '删除', exact: true })
    await expect(deleteButton).toBeVisible()

    await deleteButton.click()
    await expect(page.getByText(warningText, { exact: false })).toBeVisible()
    await page.waitForTimeout(300)
    await page.screenshot({ path: testInfo.outputPath(`${path.includes('submission') ? 'submission' : 'join'}-delete-confirm.png`) })
    await page.getByRole('button', { name: '取消', exact: true }).click()
    await expect(panel.getByText(recordText)).toBeVisible()

    await deleteButton.click()
    const deleteRequest = page.waitForRequest(request => request.method() === 'DELETE')
    await page.getByRole('button', { name: '永久删除', exact: true }).click()
    await deleteRequest

    await expect(panel.getByText(recordText)).toHaveCount(0)
    await expect(page.locator('.el-message--success')).toBeVisible()
  }
})
