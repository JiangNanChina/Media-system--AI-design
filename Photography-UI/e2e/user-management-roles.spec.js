import { expect, test } from '@playwright/test'

const tokenPayload = Buffer.from(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 })).toString('base64url')
const token = `header.${tokenPayload}.signature`

const users = [
  {
    id: 1,
    username: 'root',
    realName: '范宸宇',
    email: 'root@example.com',
    roles: ['SUPER_ADMIN', 'ADVISOR', 'MEMBER'],
    role: 'SUPER_ADMIN',
    departmentId: 1,
    departmentName: '审核部',
    enabled: true,
    createdAt: '2026-10-09T12:00:00'
  },
  {
    id: 2,
    username: 'editor',
    realName: '彭相',
    email: 'editor@example.com',
    roles: ['MINISTER', 'ADVISOR', 'MEMBER'],
    role: 'MINISTER',
    departmentId: 2,
    departmentName: '摄影部',
    enabled: true,
    createdAt: '2026-03-09T19:42:47'
  },
  {
    id: 3,
    username: 'director',
    realName: '郑亚坤',
    email: 'director@example.com',
    roles: ['DIRECTOR', 'ADVISOR'],
    role: 'DIRECTOR',
    departmentId: 3,
    departmentName: '运营部',
    enabled: true,
    createdAt: '2025-11-03T21:02:30'
  }
]

const watchRuntimeErrors = page => {
  const errors = []
  page.on('console', message => {
    if (message.type() === 'error') errors.push(message.text())
  })
  page.on('pageerror', error => errors.push(error.message))
  return errors
}

test.beforeEach(async ({ page }) => {
  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname
    const method = route.request().method()
    let data = []

    if (path.endsWith('/auth/refresh')) {
      data = {
        token,
        userId: 99,
        username: 'admin',
        realName: '超级管理员',
        role: 'SUPER_ADMIN',
        roles: ['SUPER_ADMIN']
      }
    } else if (path.endsWith('/maintenance/public/status')) {
      data = { enabled: false, unlocked: true }
    } else if (path.endsWith('/site-config/public')) {
      data = { siteTitle: '融媒体管理系统' }
    } else if (path.endsWith('/departments/list')) {
      data = [{ id: 1, name: '审核部' }, { id: 2, name: '摄影部' }, { id: 3, name: '运营部' }]
    } else if (path.endsWith('/users') && method === 'GET') {
      data = { content: users, totalElements: users.length }
    } else if (/\/users\/\d+$/.test(path) && method === 'PUT') {
      data = users[1]
    } else if (/\/accounts\/admin\/\d+\/role$/.test(path) && method === 'PUT') {
      data = { ...users[1], roles: route.request().postDataJSON().roles }
    }

    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({ success: true, data })
    })
  })
})

test('shows all roles, restores the edit selection and submits roles', async ({ page }, testInfo) => {
  const runtimeErrors = watchRuntimeErrors(page)
  await page.setViewportSize({ width: 1440, height: 1000 })
  await page.goto('/user/list', { waitUntil: 'networkidle' })

  const rows = page.locator('.el-table__body tr')
  await expect(rows).toHaveCount(3)
  await expect(rows.nth(1).locator('.role-tag')).toHaveCount(3)
  await expect(rows.nth(1).locator('.highest-role-marker')).toHaveCount(2)
  await expect(rows.nth(2).locator('.highest-role-marker')).toHaveCount(1)

  const statusHeader = page.getByRole('columnheader', { name: '状态' })
  const createdHeader = page.getByRole('columnheader', { name: '创建时间' })
  const operationHeader = page.getByRole('columnheader', { name: '操作' })
  const [statusBox, createdBox, operationBox] = await Promise.all([
    statusHeader.boundingBox(),
    createdHeader.boundingBox(),
    operationHeader.boundingBox()
  ])
  expect(statusBox.x + statusBox.width).toBeLessThanOrEqual(createdBox.x + 1)
  expect(createdBox.x + createdBox.width).toBeLessThanOrEqual(operationBox.x + 1)

  const fixedCell = rows.first().locator('td.el-table-fixed-column--right')
  await expect(fixedCell).toHaveCSS('background-color', 'rgb(255, 255, 255)')
  await page.screenshot({
    path: 'C:/Users/Administrator/.codex/visualizations/2026/09/29/01a0ecdd-3aac-7f00-9d21-5b41c0fcd045/user-roles-desktop-fixed.png',
    fullPage: false
  })

  await rows.nth(1).getByRole('button', { name: '操作' }).click()
  await page.getByRole('menuitem', { name: /编辑/ }).click()

  const dialog = page.getByRole('dialog', { name: '编辑用户' })
  await expect(dialog).toBeVisible()
  await expect(dialog).toHaveCSS('opacity', '1')
  const roleSelect = dialog.getByLabel('用户角色，可多选，按最高角色级别授权')
  await expect(roleSelect).toHaveValue('')
  await page.screenshot({
    path: 'C:/Users/Administrator/.codex/visualizations/2026/09/29/01a0ecdd-3aac-7f00-9d21-5b41c0fcd045/user-roles-dialog-fixed.png',
    fullPage: false
  })
  await dialog.locator('.el-select__wrapper').first().click()
  const selectedOptions = page.locator('.el-select-dropdown__item.is-selected:visible')
  await expect(selectedOptions).toHaveCount(3)
  await expect(selectedOptions).toContainText(['部员', '部长', '指导老师'])
  await page.locator('.el-select-dropdown__item:visible', { hasText: '主任' }).click()
  await dialog.getByText('编辑用户', { exact: true }).click()
  await expect(page.locator('.el-select-dropdown:visible')).toHaveCount(0)

  const roleRequest = page.waitForRequest(request =>
    request.method() === 'PUT' && /\/accounts\/admin\/2\/role$/.test(new URL(request.url()).pathname)
  )
  await dialog.getByRole('button', { name: '更新' }).click()
  const request = await roleRequest
  expect(request.postDataJSON()).toEqual({ roles: ['DIRECTOR', 'MINISTER', 'ADVISOR', 'MEMBER'] })

  await page.screenshot({ path: testInfo.outputPath('user-roles-updated.png'), fullPage: false })
  expect(runtimeErrors).toEqual([])
})

test('keeps the user-management page within a narrow viewport', async ({ page }) => {
  const runtimeErrors = watchRuntimeErrors(page)
  await page.setViewportSize({ width: 375, height: 812 })
  await page.goto('/user/list', { waitUntil: 'networkidle' })
  await expect(page.locator('.el-table__body tr')).toHaveCount(3)

  const overflow = await page.evaluate(() => document.documentElement.scrollWidth - document.documentElement.clientWidth)
  expect(overflow).toBeLessThanOrEqual(1)
  await page.screenshot({
    path: 'C:/Users/Administrator/.codex/visualizations/2026/09/29/01a0ecdd-3aac-7f00-9d21-5b41c0fcd045/user-roles-mobile-fixed.png',
    fullPage: false
  })
  expect(runtimeErrors).toEqual([])
})
