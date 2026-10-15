import { expect, test } from '@playwright/test'

test('landing preview renders unsaved management settings with the public page', async ({ page }, testInfo) => {
  let publicContentRequests = 0
  page.on('request', request => {
    if (request.url().includes('/api/landing/public')) publicContentRequests += 1
  })

  await page.setViewportSize({ width: 1080, height: 900 })
  await page.goto('/?landingPreview=1#hero', { waitUntil: 'networkidle' })
  await expect(page.locator('.landing-page')).toBeVisible()

  await page.evaluate(() => {
    window.postMessage({
      type: 'landing-preview-update',
      section: 'hero',
      content: {
        settings: {
          'site.subtitle': '实时预览副标题',
          'landing.brand.title': '实时预览品牌',
          'landing.nav.join_label': '加入创作团队',
          'landing.hero.badge': 'PREVIEW / LIVE',
          'landing.hero.title': '未保存内容也应立即可见',
          'landing.hero.subtitle': '此画面与公开落地页使用同一组件和同一套样式。',
          'landing.hero.primary_cta': '提交作品',
          'landing.hero.join_cta': '现在加入',
          'landing.hero.secondary_cta': '浏览栏目'
        },
        campusFeatures: [],
        departmentShowcases: []
      }
    }, window.location.origin)
  })

  await expect(page.locator('#hero-title')).toHaveText('实时预览品牌')
  await expect(page.locator('.brand-copy small')).toHaveText('实时预览副标题')
  await expect(page.locator('.hero-statement')).toHaveText('未保存内容也应立即可见')
  await expect(page.locator('.hero-actions')).toContainText('现在加入')
  await expect(page.locator('.nav-panel')).toContainText('加入创作团队')
  expect(publicContentRequests).toBe(0)

  await page.screenshot({ path: testInfo.outputPath('landing-live-preview.png'), fullPage: false })
})

test('management page keeps its scroll position while the live preview updates during typing', async ({ page }, testInfo) => {
  const tokenPayload = Buffer.from(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 })).toString('base64url')
  const token = `header.${tokenPayload}.signature`
  const settings = {
    'site.title': '融媒体管理系统',
    'site.subtitle': 'Campus media center',
    'landing.brand.title': '融媒体中心',
    'landing.nav.home_label': '首页',
    'landing.nav.features_label': '校园特色',
    'landing.nav.showcase_label': '部门风采',
    'landing.nav.submission_label': '视频投稿',
    'landing.nav.join_label': '加入我们',
    'landing.hero.badge': '校园官方新媒体平台',
    'landing.hero.title': '记录校园，让每一种声音被看见',
    'landing.hero.subtitle': '连接校园现场、青年创作与公共表达',
    'landing.hero.primary_cta': '视频投稿',
    'landing.hero.join_cta': '加入我们',
    'landing.hero.secondary_cta': '了解我们',
    'landing.hero.media_type': 'image'
  }

  await page.route('**/api/**', async route => {
    const path = new URL(route.request().url()).pathname
    let data = []
    if (path.endsWith('/auth/refresh')) {
      data = { token, userId: 1, username: 'preview-admin', realName: '预览管理员', role: 'SUPER_ADMIN' }
    } else if (path.endsWith('/maintenance/public/status')) {
      data = { enabled: false, unlocked: true }
    } else if (path.endsWith('/landing/public')) {
      data = { settings, campusFeatures: [], departmentShowcases: [] }
    } else if (path.endsWith('/landing/admin/items')) {
      data = []
    } else if (path.endsWith('/site-config/public')) {
      data = { siteTitle: '融媒体管理系统' }
    } else if (path.includes('/announcements/')) {
      data = { content: [], totalElements: 0 }
    }
    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({ success: true, data })
    })
  })

  await page.setViewportSize({ width: 1600, height: 1000 })
  await page.goto('/devices/landing-config', { waitUntil: 'networkidle' })
  await expect(page.locator('.landing-management')).toBeVisible()
  await expect(page.locator('.landing-live-preview iframe')).toBeVisible()

  const preview = page.frameLocator('.landing-live-preview iframe')
  await expect(preview.locator('#hero-title')).toHaveText('融媒体中心')

  const titleInput = page.locator('.el-form-item').filter({ hasText: '首屏主张文案' }).locator('input')
  await titleInput.fill('编辑中的内容与真实页面同步')
  await expect(preview.locator('.hero-statement')).toHaveText('编辑中的内容与真实页面同步')

  const mainContent = page.locator('.main-content')
  await mainContent.evaluate(element => {
    element.scrollTop = Math.min(360, element.scrollHeight - element.clientHeight)
  })
  await titleInput.scrollIntoViewIfNeeded()
  await titleInput.click()
  const scrollTopBeforeTyping = await mainContent.evaluate(element => element.scrollTop)
  expect(scrollTopBeforeTyping).toBeGreaterThan(100)
  await titleInput.press('End')
  await titleInput.type('!')
  await expect(preview.locator('.hero-statement')).toHaveText('编辑中的内容与真实页面同步!')
  await expect.poll(() => mainContent.evaluate(element => element.scrollTop))
    .toBe(scrollTopBeforeTyping)

  const geometry = await page.evaluate(() => {
    const viewport = document.querySelector('.preview-viewport')?.getBoundingClientRect()
    const frame = document.querySelector('.preview-viewport iframe')?.getBoundingClientRect()
    return viewport && frame
      ? {
          horizontalOverflow: Math.max(0, frame.right - viewport.right),
          verticalOverflow: Math.max(0, frame.bottom - viewport.bottom),
          frameWidth: frame.width,
          frameHeight: frame.height
        }
      : null
  })
  expect(geometry).not.toBeNull()
  expect(geometry.horizontalOverflow).toBeLessThanOrEqual(1)
  expect(geometry.verticalOverflow).toBeLessThanOrEqual(1)
  expect(geometry.frameWidth).toBeGreaterThan(360)
  expect(geometry.frameHeight).toBeGreaterThan(360)

  await page.screenshot({ path: testInfo.outputPath('landing-management-real-preview.png'), fullPage: false })
})
