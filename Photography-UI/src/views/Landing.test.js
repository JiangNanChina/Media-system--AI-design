import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import Landing from './Landing.vue'

const { push, request, userStore } = vi.hoisted(() => ({
  push: vi.fn(),
  request: { get: vi.fn() },
  userStore: { isLoggedIn: false }
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push })
}))

vi.mock('@/utils/request', () => ({ default: request }))
vi.mock('@/utils/imageUrl', () => ({ getSiteImageUrl: value => value || '' }))
vi.mock('@/stores/user', () => ({ useUserStore: () => userStore }))

const RouterLinkStub = {
  props: ['to'],
  template: '<a :href="to"><slot /></a>'
}

const ElButtonStub = {
  template: '<button type="button" @click="$emit(\'click\')"><slot /></button>'
}

const mountLanding = () => mount(Landing, {
  global: {
    mocks: {
      $router: { push }
    },
    stubs: {
      RouterLink: RouterLinkStub,
      'router-link': RouterLinkStub,
      ElButton: ElButtonStub,
      ElIcon: { template: '<span class="icon"><slot /></span>' },
      ElCarousel: { template: '<div class="carousel"><slot /></div>' },
      ElCarouselItem: { template: '<div class="carousel-item"><slot /></div>' },
      ArrowDown: true,
      Camera: true,
      ChatRound: true,
      Film: true,
      LinkIcon: true,
      Location: true,
      Message: true,
      OfficeBuilding: true,
      Picture: true,
      Phone: true,
      Right: true,
      UploadFilled: true,
      User: true,
      VideoCamera: true
    }
  }
})

describe('Landing public page', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    userStore.isLoggedIn = false
    request.get.mockResolvedValue({
      data: {
        settings: {
          'site.title': '测试融媒体中心',
          'site.logo': '/uploads/logo.png',
          'landing.hero.media': '/uploads/hero.jpg',
          'landing.hero.title': '记录校园精彩瞬间',
          'landing.hero.subtitle': '用镜头连接青春现场',
          'landing.submission.title': '分享你的校园故事',
          'landing.submission.description': '优秀作品将在官方平台展示',
          'landing.contact': '行政楼 203 室',
          'landing.social.douyin_url': 'https://example.com/douyin',
          'landing.social.wechat_url': 'https://example.com/wechat',
          'landing.social.website_url': 'https://school.example.edu'
        },
        campusFeatures: [
          { id: 1, title: '大美校园', summary: '记录四季风景', mediaUrl: '/uploads/campus.jpg' },
          { id: 2, title: '学术沃土', summary: '记录学术现场', mediaUrl: '/uploads/academic.jpg' },
          { id: 3, title: '缤纷活动', summary: '记录校园活动', mediaUrl: '/uploads/activity.jpg' }
        ],
        departmentShowcases: [
          { id: 4, title: '摄影组', summary: '捕捉校园瞬间', mediaUrl: '/uploads/photo.jpg' },
          { id: 5, title: '视频组', summary: '剪辑精彩画面', mediaUrl: '/uploads/video.jpg' }
        ]
      }
    })
  })

  it('renders configured hero, cards, showcase, submission CTA and footer content', async () => {
    const wrapper = mountLanding()
    await flushPromises()

    expect(request.get).toHaveBeenCalledWith('/landing/public', { silent: true })
    expect(wrapper.text()).toContain('测试融媒体中心')
    expect(wrapper.text()).toContain('记录校园精彩瞬间')
    expect(wrapper.text()).toContain('用镜头连接青春现场')
    expect(wrapper.text()).toContain('大美校园')
    expect(wrapper.text()).toContain('摄影组')
    expect(wrapper.find('.showcase-carousel').exists()).toBe(true)
    expect(wrapper.text()).toContain('分享你的校园故事')
    expect(wrapper.text()).toContain('校园新闻')
    expect(wrapper.text()).toContain('上传素材')
    expect(wrapper.text()).toContain('media@campus.edu.cn')
    expect(wrapper.text()).toContain('010-12345678')
    expect(wrapper.text()).toContain('行政楼 203 室')
    expect(wrapper.findAll('.brand-social-icon')).toHaveLength(2)
    expect(wrapper.findAll('.social-links a')[0].attributes('title')).toBe('微信公众号')
    expect(wrapper.findAll('.social-links a')[1].attributes('title')).toBe('抖音官方号')
    expect(wrapper.findAll('.social-links a')[2].attributes('title')).toBe('学校官网')
    expect(wrapper.findAll('.social-links a')[2].attributes('href')).toBe('https://school.example.edu')
  })

  it('routes submission and platform actions through the existing public routes', async () => {
    const wrapper = mountLanding()
    await flushPromises()

    await wrapper.find('.submit-actions button').trigger('click')
    expect(push).toHaveBeenCalledWith('/submission')

    await wrapper.find('.landing-nav button').trigger('click')
    expect(push).toHaveBeenCalledWith('/login')

    userStore.isLoggedIn = true
    await wrapper.find('.landing-nav button').trigger('click')
    expect(push).toHaveBeenCalledWith('/dashboard')
  })

  it('opens and closes the mobile navigation without changing routes', async () => {
    const wrapper = mountLanding()
    await flushPromises()

    const menuButton = wrapper.find('.nav-menu-button')
    expect(menuButton.attributes('aria-expanded')).toBe('false')

    await menuButton.trigger('click')
    expect(wrapper.classes()).toContain('menu-is-open')
    expect(menuButton.attributes('aria-expanded')).toBe('true')

    await menuButton.trigger('click')
    expect(wrapper.classes()).not.toContain('menu-is-open')
    expect(push).not.toHaveBeenCalled()
  })

  it('uses the public landing brand name instead of the management system title', async () => {
    request.get.mockResolvedValueOnce({
      data: {
        settings: { 'site.title': '融媒体管理系统' },
        campusFeatures: [],
        departmentShowcases: []
      }
    })

    const wrapper = mountLanding()
    await flushPromises()

    expect(wrapper.find('.brand').text()).toContain('融媒体中心')
    expect(wrapper.find('.footer-brand').text()).toContain('融媒体中心')
    expect(wrapper.find('.footer-bottom').text()).toContain(`© ${new Date().getFullYear()} 融媒体中心 版权所有`)
  })

  it('shows the showcase carousel with placeholder media when departments have no images', async () => {
    request.get.mockResolvedValueOnce({
      data: {
        settings: { 'site.title': '测试融媒体中心' },
        campusFeatures: [],
        departmentShowcases: [
          { id: 8, title: '摄影部', summary: '记录校园活动' },
          { id: 9, title: '审核部', summary: '负责稿件审核' }
        ]
      }
    })

    const wrapper = mountLanding()
    await flushPromises()

    expect(wrapper.find('.showcase-carousel').exists()).toBe(true)
    expect(wrapper.find('.slide-media-fallback').exists()).toBe(true)
    expect(wrapper.text()).toContain('摄影部')
  })

  it('hides the showcase carousel when fewer than two departments are configured', async () => {
    request.get.mockResolvedValueOnce({
      data: {
        settings: { 'site.title': '测试融媒体中心' },
        campusFeatures: [],
        departmentShowcases: [
          { id: 8, title: '采编组', summary: '记录校园声音', mediaUrl: '/uploads/news.jpg' }
        ]
      }
    })

    const wrapper = mountLanding()
    await flushPromises()

    expect(wrapper.find('.showcase-carousel').exists()).toBe(false)
    expect(wrapper.text()).toContain('采编组')
  })
})
