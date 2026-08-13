<template>
  <div
    ref="landingRoot"
    :class="['landing-page', { 'is-ready': pageReady, 'menu-is-open': menuOpen }]"
    @pointermove="handleSurfacePointer"
    @pointerout="handleSurfacePointerOut"
  >
    <header class="landing-nav">
      <router-link to="/" class="brand" aria-label="返回校融媒体首页" @click="menuOpen = false">
        <span class="brand-mark">
          <img v-if="setting('site.logo')" :src="logoUrl" alt="校融媒体中心标志" />
          <el-icon v-else><Camera /></el-icon>
        </span>
        <span class="brand-copy">
          <strong>{{ brandTitle }}</strong>
          <small>Campus media center</small>
        </span>
      </router-link>

      <div id="landing-navigation" class="nav-panel">
        <nav aria-label="站点导航">
          <a href="#hero" @click="menuOpen = false">{{ setting('landing.nav.home_label', '首页') }}</a>
          <a href="#features" @click="menuOpen = false">{{ setting('landing.nav.features_label', '校园特色') }}</a>
          <a href="#showcase" @click="menuOpen = false">{{ setting('landing.nav.showcase_label', '部门风采') }}</a>
          <router-link to="/submission" @click="menuOpen = false">{{ setting('landing.nav.submission_label', '视频投稿') }}</router-link>
          <router-link to="/join-us" @click="menuOpen = false">加入我们</router-link>
        </nav>

        <el-button class="nav-login" type="primary" @click="enterPlatform">
          {{ userStore.isLoggedIn ? '进入管理平台' : '登录' }}
          <el-icon><Right /></el-icon>
        </el-button>
      </div>

      <button
        class="nav-menu-button"
        type="button"
        :aria-expanded="menuOpen"
        aria-controls="landing-navigation"
        :aria-label="menuOpen ? '关闭导航菜单' : '打开导航菜单'"
        @click="menuOpen = !menuOpen"
      >
        <span class="menu-icon-stack" aria-hidden="true">
          <el-icon :class="{ 'is-visible': menuOpen }"><Close /></el-icon>
          <el-icon :class="{ 'is-visible': !menuOpen }"><Menu /></el-icon>
        </span>
      </button>

      <div class="landing-progress" aria-hidden="true"><span></span></div>
    </header>

    <main>
      <section
        id="hero"
        ref="heroSection"
        :class="['hero', { 'is-tracking': heroTracking }]"
        aria-labelledby="hero-title"
        @pointerenter="startHeroTracking"
        @pointermove="trackHeroPointer"
        @pointerleave="stopHeroTracking"
      >
        <video
          v-if="heroIsVideo && heroMedia"
          class="hero-media"
          :src="heroMedia"
          autoplay
          muted
          loop
          playsinline
        />
        <img
          v-else-if="heroMedia"
          class="hero-media"
          :src="heroMedia"
          alt="校园融媒体中心展示画面"
        />
        <div v-else class="hero-fallback" aria-hidden="true">
          <img :src="logoUrl" alt="" />
        </div>

        <div class="hero-overlay"></div>
        <div class="hero-frame" aria-hidden="true">
          <span>REC</span>
          <span>FRAME 001</span>
        </div>
        <div class="hero-focus" aria-hidden="true"><span></span></div>

        <div class="hero-content">
          <div class="hero-kicker">
            <span class="record-dot"></span>
            {{ setting('landing.hero.badge', '校园官方新媒体平台') }}
          </div>
          <h1 id="hero-title">{{ brandTitle }}</h1>
          <p class="hero-statement">{{ setting('landing.hero.title', '记录校园，让每一种声音被看见') }}</p>
          <p class="hero-subtitle">{{ setting('landing.hero.subtitle', '校融媒体中心连接校园现场、青年创作与公共表达') }}</p>
          <div class="hero-actions">
            <el-button class="primary-action" type="primary" size="large" @click="$router.push('/submission')">
              <el-icon><UploadFilled /></el-icon>
              {{ setting('landing.hero.primary_cta', '视频投稿') }}
            </el-button>
            <el-button class="secondary-action" size="large" @click="$router.push('/join-us')">
              <el-icon><UserFilled /></el-icon>
              加入我们
            </el-button>
            <button class="text-action" type="button" @click="scrollTo('features')">
              {{ setting('landing.hero.secondary_cta', '了解我们') }}
              <el-icon><ArrowDown /></el-icon>
            </button>
          </div>
        </div>

        <button class="scroll-cue" type="button" aria-label="查看校园特色" @click="scrollTo('features')">
          <span>向下浏览</span>
          <el-icon><ArrowDown /></el-icon>
        </button>

        <div class="film-edge" aria-hidden="true"></div>
      </section>

      <section id="features" class="features-band" aria-labelledby="features-title">
        <div class="section-shell features-section">
          <div class="section-heading split-heading" data-reveal>
            <div class="section-label">
              <span>CONTACT SHEET</span>
              <small>校园影像 / {{ String(campusItems.length).padStart(2, '0') }} 帧</small>
            </div>
            <div class="section-title-block">
              <p>{{ setting('landing.features.eyebrow', '校园特色') }}</p>
              <h2 id="features-title">{{ setting('landing.features.title', '发现不一样的校园') }}</h2>
              <div class="heading-note">{{ setting('landing.features.description', '在这里，每一个角落都有故事，每一刻时光都值得被记录') }}</div>
            </div>
          </div>

          <div class="feature-grid" data-ai-section-type="card-list">
            <article
              v-for="(item, index) in campusItems"
              :key="item.id || item.title"
              class="image-card feature-card motion-surface"
              data-reveal
              :style="{ '--reveal-index': index }"
            >
              <div class="card-image">
                <img v-if="item.mediaUrl" :src="mediaUrl(item.mediaUrl)" :alt="item.title" />
                <div v-else class="card-media-fallback">
                  <el-icon><Picture /></el-icon>
                </div>
                <span class="frame-number">{{ String(index + 1).padStart(2, '0') }}</span>
              </div>
              <div class="card-copy">
                <span class="card-rule" aria-hidden="true"></span>
                <h3>{{ item.title }}</h3>
                <p>{{ item.summary }}</p>
              </div>
            </article>
          </div>
        </div>
      </section>

      <section id="showcase" class="showcase-band" aria-labelledby="showcase-title">
        <div class="film-edge film-edge-top" aria-hidden="true"></div>
        <div class="section-shell showcase-section">
          <div class="section-heading showcase-heading" data-reveal>
            <div class="section-label section-label-light">
              <span>NOW SHOWING</span>
              <small>创作团队 / {{ String(departmentItems.length).padStart(2, '0') }} 组</small>
            </div>
            <div class="section-title-block">
              <p>{{ setting('landing.showcase.eyebrow', '部门风采') }}</p>
              <h2 id="showcase-title">{{ setting('landing.showcase.title', '我们的故事') }}</h2>
              <div class="heading-note">{{ setting('landing.showcase.description', '一群热爱影像与创作的年轻人，用镜头讲述校园里的每一个精彩瞬间。') }}</div>
            </div>
          </div>

          <el-carousel
            v-if="carouselDepartments.length >= 2"
            class="showcase-carousel"
            data-reveal
            height="520px"
            indicator-position="outside"
            arrow="always"
          >
            <el-carousel-item v-for="(item, index) in carouselDepartments" :key="item.id || item.title">
              <div class="showcase-slide">
                <img v-if="item.mediaUrl" :src="mediaUrl(item.mediaUrl)" :alt="item.title" />
                <div v-else class="slide-media-fallback">
                  <el-icon><OfficeBuilding /></el-icon>
                </div>
                <div class="slide-overlay"></div>
                <span class="slide-counter">FRAME {{ String(index + 1).padStart(2, '0') }} / {{ String(carouselDepartments.length).padStart(2, '0') }}</span>
                <div class="slide-copy">
                  <h3>{{ item.title }}</h3>
                  <p>{{ item.summary }}</p>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>

          <div class="department-grid" data-ai-section-type="card-list">
            <article
              v-for="(item, index) in departmentItems"
              :key="item.id || item.title"
              class="department-card motion-surface"
              data-reveal
              :style="{ '--reveal-index': index }"
            >
              <div class="department-index">{{ String(index + 1).padStart(2, '0') }}</div>
              <div class="card-image">
                <img v-if="item.mediaUrl" :src="mediaUrl(item.mediaUrl)" :alt="item.title" />
                <div v-else class="card-media-fallback dark">
                  <el-icon><OfficeBuilding /></el-icon>
                </div>
              </div>
              <div class="card-copy">
                <h3>{{ item.title }}</h3>
                <p>{{ item.summary }}</p>
              </div>
            </article>
          </div>
        </div>
      </section>

      <section class="submit-section" aria-labelledby="submission-title">
        <div class="submit-card section-shell" data-reveal>
          <div class="submit-copy">
            <div class="section-label">
              <span>OPEN CALL</span>
              <small>{{ setting('landing.submission.eyebrow', '视频投稿') }}</small>
            </div>
            <div class="section-heading">
              <h2 id="submission-title">{{ setting('landing.submission.title', '把你的校园故事交给我们') }}</h2>
              <p>{{ setting('landing.submission.description', '支持校园新闻、人物、活动与创意短视频投稿') }}</p>
            </div>

            <div class="submit-flow" aria-label="投稿流程">
              <div
                v-for="(step, index) in submitSteps"
                :key="step.title"
                class="submit-step"
                :style="{ '--reveal-index': index }"
              >
                <span>{{ step.index }}</span>
                <strong>{{ step.title }}</strong>
              </div>
            </div>

            <div class="submit-actions">
              <el-button size="large" type="primary" @click="$router.push('/submission')">
                <el-icon><VideoCamera /></el-icon>
                {{ setting('landing.submission.primary_cta', '开始投稿') }}
              </el-button>
              <el-button size="large" @click="scrollTo('showcase')">
                <el-icon><OfficeBuilding /></el-icon>
                {{ setting('landing.submission.secondary_cta', '部门风采') }}
              </el-button>
            </div>
          </div>

          <aside class="submit-board motion-surface" aria-label="投稿方向">
            <div class="submit-visual" aria-hidden="true">
              <video v-if="heroIsVideo && heroMedia" :src="heroMedia" autoplay muted loop playsinline />
              <img v-else-if="heroMedia" :src="heroMedia" alt="" />
              <div v-else class="submit-visual-fallback">
                <el-icon><Camera /></el-icon>
              </div>
              <div class="submit-visual-badge">
                <span class="record-dot"></span>
                <span>{{ setting('landing.submission.visual_badge', '校园影像库') }}</span>
              </div>
            </div>
            <div class="submit-topic-list">
              <div
                v-for="(topic, index) in submitTopics"
                :key="topic.title"
                class="submit-topic"
                :style="{ '--reveal-index': index }"
              >
                <span class="topic-index">{{ String(index + 1).padStart(2, '0') }}</span>
                <el-icon><component :is="topic.icon" /></el-icon>
                <span>
                  <strong>{{ topic.title }}</strong>
                  <small>{{ topic.summary }}</small>
                </span>
              </div>
            </div>
          </aside>
        </div>
      </section>
    </main>

    <footer class="landing-footer" aria-label="站点页尾">
      <div class="footer-inner section-shell">
        <section class="footer-about" data-reveal>
          <div class="footer-brand">
            <span class="footer-brand-mark">
              <img :src="logoUrl" alt="" />
            </span>
            <strong>{{ brandTitle }}</strong>
          </div>
          <p>{{ setting('landing.footer.description', '用镜头记录青春，用创意点亮校园。我们是校园里的记录者，用影像传递温度与力量。') }}</p>
          <router-link class="footer-join-link" to="/join-us">
            加入我们
            <el-icon><Right /></el-icon>
          </router-link>
        </section>

        <section class="footer-social" data-reveal>
          <h2>{{ setting('landing.footer.social_title', '关注我们') }}</h2>
          <div class="social-links">
            <template v-for="item in footerSocialLinks" :key="item.key">
              <router-link
                v-if="item.to"
                :to="item.to"
                :class="['social-link', `is-${item.key}`]"
                :aria-label="item.label"
                :title="item.tooltip"
                :data-tooltip="item.tooltip"
              >
                <el-icon><component :is="item.icon" /></el-icon>
                <span class="social-tooltip" aria-hidden="true">{{ item.tooltip }}</span>
              </router-link>
              <a
                v-else
                :href="item.href"
                :class="['social-link', `is-${item.key}`]"
                :target="item.external ? '_blank' : undefined"
                :rel="item.external ? 'noopener' : undefined"
                :aria-label="item.label"
                :title="item.tooltip"
                :data-tooltip="item.tooltip"
              >
                <el-icon><component :is="item.icon" /></el-icon>
                <span class="social-tooltip" aria-hidden="true">{{ item.tooltip }}</span>
              </a>
            </template>
          </div>
        </section>

        <section class="footer-contact" data-reveal>
          <h2>{{ setting('landing.footer.contact_title', '联系我们') }}</h2>
          <div class="contact-list">
            <component
              :is="item.href ? 'a' : 'p'"
              v-for="item in footerContacts"
              :key="item.key"
              class="contact-item"
              v-bind="item.href ? { href: item.href } : {}"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.value }}</span>
            </component>
          </div>
          <img
            v-if="setting('landing.social.wechat_qr')"
            id="wechat-qr"
            class="wechat-qr"
            :src="mediaUrl(setting('landing.social.wechat_qr'))"
            alt="微信公众号二维码"
          />
        </section>
      </div>
      <div class="footer-bottom section-shell">
        <small>© {{ currentYear }} {{ brandTitle }} {{ setting('landing.footer.copyright_suffix', '版权所有') }}</small>
        <span>KEEP THE CAMERA ROLLING</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, h, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowDown,
  Camera,
  Close,
  Film,
  Location,
  Menu,
  Message,
  OfficeBuilding,
  Picture,
  Phone,
  Right,
  School,
  UploadFilled,
  UserFilled,
  VideoCamera
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getSiteImageUrl } from '@/utils/imageUrl'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const currentYear = new Date().getFullYear()
const landingRoot = ref(null)
const heroSection = ref(null)
const pageReady = ref(false)
const menuOpen = ref(false)
const heroTracking = ref(false)

let revealObserver = null
const revealCompleteTimers = new Set()
let revealFallbackTimer = null
let pointerFrame = null
let scrollFrame = null
let pendingHeroPointer = null

const content = reactive({
  settings: {},
  campusFeatures: [],
  departmentShowcases: []
})

const fallbackCampusItems = [
  {
    id: 'fallback-campus',
    title: '大美校园',
    summary: '四季流转的校园风景，用镜头定格每一个值得停留的瞬间。'
  },
  {
    id: 'fallback-academic',
    title: '学术沃土',
    summary: '记录讲座、竞赛与学术现场，让思想与成长被更多人看见。'
  },
  {
    id: 'fallback-activity',
    title: '缤纷活动',
    summary: '捕捉社团、晚会和志愿服务中的热烈时刻，呈现校园生活的多面。'
  }
]

const fallbackDepartmentItems = [
  {
    id: 'fallback-photo',
    title: '校园摄影组',
    summary: '用镜头捕捉校园最美的瞬间。'
  },
  {
    id: 'fallback-video',
    title: '视频创作组',
    summary: '拍摄、剪辑并发布每一帧精彩画面。'
  },
  {
    id: 'fallback-news',
    title: '新闻采编组',
    summary: '记录校园真实的声音和正在发生的故事。'
  },
  {
    id: 'fallback-operation',
    title: '平台运营组',
    summary: '让优秀作品在更多官方平台被看见。'
  }
]

const setting = (key, fallback = '') => content.settings?.[key] || fallback
const mediaUrl = value => getSiteImageUrl(value)

const createBrandIcon = (name, path) => ({
  name,
  render() {
    return h('svg', {
      class: 'brand-social-icon',
      viewBox: '0 0 24 24',
      role: 'img',
      'aria-hidden': 'true',
      focusable: 'false'
    }, [
      h('path', { fill: 'currentColor', d: path })
    ])
  }
})

const WeChatIcon = createBrandIcon('WeChatIcon', 'M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213c0 .163.13.295.29.295a.33.33 0 0 0 .167-.054l1.903-1.114a.86.86 0 0 1 .717-.098a10.2 10.2 0 0 0 2.837.403c.276 0 .543-.027.811-.05c-.857-2.578.157-4.972 1.932-6.446c1.703-1.415 3.882-1.98 5.853-1.838c-.576-3.583-4.196-6.348-8.596-6.348M5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178a1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18m5.34 2.867c-1.797-.052-3.746.512-5.28 1.786c-1.72 1.428-2.687 3.72-1.78 6.22c.942 2.453 3.666 4.229 6.884 4.229c.826 0 1.622-.12 2.361-.336a.72.72 0 0 1 .598.082l1.584.926a.3.3 0 0 0 .14.047c.134 0 .24-.111.24-.247c0-.06-.023-.12-.038-.177l-.327-1.233a.6.6 0 0 1-.023-.156a.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-6.088-6.656-6.088V8.89c-.135-.01-.27-.027-.407-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983a.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983a.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982')
const DouyinIcon = createBrandIcon('DouyinIcon', 'M12.525.02c1.31-.02 2.61-.01 3.91-.02c.08 1.53.63 3.09 1.75 4.17c1.12 1.11 2.7 1.62 4.24 1.79v4.03c-1.44-.05-2.89-.35-4.2-.97c-.57-.26-1.1-.59-1.62-.93c-.01 2.92.01 5.84-.02 8.75c-.08 1.4-.54 2.79-1.35 3.94c-1.31 1.92-3.58 3.17-5.91 3.21c-1.43.08-2.86-.31-4.08-1.03c-2.02-1.19-3.44-3.37-3.65-5.71c-.02-.5-.03-1-.01-1.49c.18-1.9 1.12-3.72 2.58-4.96c1.66-1.44 3.98-2.13 6.15-1.72c.02 1.48-.04 2.96-.04 4.44c-.99-.32-2.15-.23-3.02.37c-.63.41-1.11 1.04-1.36 1.75c-.21.51-.15 1.07-.14 1.61c.24 1.64 1.82 3.02 3.5 2.87c1.12-.01 2.19-.66 2.77-1.61c.19-.33.4-.67.41-1.06c.1-1.79.06-3.57.07-5.36c.01-4.03-.01-8.05.02-12.07')

const siteTitle = computed(() => setting('site.title', '融媒体中心'))
const brandTitle = computed(() => {
  const configuredTitle = setting('landing.brand.title')
  if (configuredTitle) return configuredTitle
  return /融媒体.*管理系统/.test(siteTitle.value) ? '融媒体中心' : siteTitle.value
})
const logoUrl = computed(() => mediaUrl(setting('site.logo')) || '/logo.svg')
const heroMedia = computed(() => mediaUrl(setting('landing.hero.media') || setting('login.background')))
const heroIsVideo = computed(() => setting('landing.hero.media_type') === 'video' || /\.(mp4|mov|webm)(\?|$)/i.test(heroMedia.value))
const campusItems = computed(() => content.campusFeatures?.length ? content.campusFeatures : fallbackCampusItems)
const departmentItems = computed(() => content.departmentShowcases?.length ? content.departmentShowcases : fallbackDepartmentItems)
const carouselDepartments = computed(() => departmentItems.value)
const submitTopics = computed(() => [
  {
    title: setting('landing.submission.topic_one.title', '校园新闻'),
    summary: setting('landing.submission.topic_one.summary', '记录现场与公共议题'),
    icon: VideoCamera
  },
  {
    title: setting('landing.submission.topic_two.title', '人物故事'),
    summary: setting('landing.submission.topic_two.summary', '呈现青春里的闪光时刻'),
    icon: Camera
  },
  {
    title: setting('landing.submission.topic_three.title', '活动创意'),
    summary: setting('landing.submission.topic_three.summary', '捕捉舞台、社团与灵感'),
    icon: Film
  }
])
const submitSteps = computed(() => [
  { index: '01', title: setting('landing.submission.step_one', '上传素材') },
  { index: '02', title: setting('landing.submission.step_two', '填写信息') },
  { index: '03', title: setting('landing.submission.step_three', '等待审核') }
])
const footerEmail = computed(() => setting('landing.contact.email', 'media@campus.edu.cn'))
const footerPhone = computed(() => setting('landing.contact.phone', '010-12345678'))
const footerAddress = computed(() => setting('landing.contact.address', setting('landing.contact', '行政楼 203 室')))
const schoolWebsiteUrl = computed(() => setting('landing.social.website_url', 'https://www.campus.edu.cn'))
const footerSocialLinks = computed(() => {
  const douyinUrl = setting('landing.social.douyin_url')
  const wechatUrl = setting('landing.social.wechat_url')

  return [
    {
      key: 'wechat',
      label: '微信公众号',
      tooltip: '微信公众号',
      icon: WeChatIcon,
      href: wechatUrl || (setting('landing.social.wechat_qr') ? '#wechat-qr' : '#showcase'),
      external: Boolean(wechatUrl)
    },
    {
      key: 'douyin',
      label: '抖音官方号',
      tooltip: '抖音官方号',
      icon: DouyinIcon,
      href: douyinUrl || '#showcase',
      external: Boolean(douyinUrl)
    },
    {
      key: 'school',
      label: '学校官网',
      tooltip: '学校官网',
      icon: School,
      href: schoolWebsiteUrl.value,
      external: true
    }
  ]
})
const footerContacts = computed(() => [
  { key: 'email', icon: Message, value: footerEmail.value, href: footerEmail.value ? `mailto:${footerEmail.value}` : '' },
  { key: 'phone', icon: Phone, value: footerPhone.value, href: footerPhone.value ? `tel:${footerPhone.value.replace(/[^\d+]/g, '')}` : '' },
  { key: 'address', icon: Location, value: footerAddress.value, href: '' }
].filter(item => item.value))

const prefersReducedMotion = () => {
  return typeof window !== 'undefined'
    && typeof window.matchMedia === 'function'
    && window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

const revealTarget = target => {
  target.classList.add('is-visible')

  if (typeof window === 'undefined' || prefersReducedMotion()) {
    target.classList.add('is-reveal-complete')
    return
  }

  const revealIndex = Number(target.style.getPropertyValue('--reveal-index')) || 0
  const timer = window.setTimeout(() => {
    target.classList.add('is-reveal-complete')
    revealCompleteTimers.delete(timer)
  }, Math.min(revealIndex * 70, 420) + 760)
  revealCompleteTimers.add(timer)
}

const setupRevealObserver = () => {
  const root = landingRoot.value
  if (!root) return

  const revealTargets = Array.from(root.querySelectorAll('[data-reveal]:not(.is-visible)'))
  if (!revealTargets.length) return

  if (prefersReducedMotion() || typeof window === 'undefined' || !('IntersectionObserver' in window)) {
    revealTargets.forEach(revealTarget)
    return
  }

  if (!revealObserver) {
    revealObserver = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return
        revealTarget(entry.target)
        revealObserver.unobserve(entry.target)
      })
    }, {
      rootMargin: '0px 0px -8% 0px',
      threshold: 0.16
    })
  }

  revealTargets.forEach(target => revealObserver.observe(target))

  if (revealFallbackTimer == null && typeof window !== 'undefined') {
    revealFallbackTimer = window.setTimeout(() => {
      root.querySelectorAll('[data-reveal]:not(.is-visible)').forEach(revealTarget)
      revealFallbackTimer = null
    }, 1800)
  }
}

const updateScrollProgress = () => {
  scrollFrame = null
  const root = landingRoot.value
  if (!root || typeof window === 'undefined') return

  const scrollRange = Math.max(document.documentElement.scrollHeight - window.innerHeight, 1)
  const progress = Math.min(Math.max(window.scrollY / scrollRange, 0), 1)
  root.style.setProperty('--landing-progress', progress.toFixed(4))
}

const scheduleScrollProgress = () => {
  if (scrollFrame != null || typeof window === 'undefined') return
  scrollFrame = window.requestAnimationFrame(updateScrollProgress)
}

const startHeroTracking = event => {
  if (event.pointerType === 'touch' || prefersReducedMotion()) return
  heroTracking.value = true
  trackHeroPointer(event)
}

const trackHeroPointer = event => {
  const hero = heroSection.value
  if (!hero || event.pointerType === 'touch' || prefersReducedMotion()) return

  pendingHeroPointer = { clientX: event.clientX, clientY: event.clientY }
  if (pointerFrame != null) return

  pointerFrame = window.requestAnimationFrame(() => {
    pointerFrame = null
    if (!pendingHeroPointer) return

    const bounds = hero.getBoundingClientRect()
    const x = Math.min(Math.max(pendingHeroPointer.clientX - bounds.left, 0), bounds.width)
    const y = Math.min(Math.max(pendingHeroPointer.clientY - bounds.top, 0), bounds.height)
    const normalizedX = bounds.width ? x / bounds.width - 0.5 : 0
    const normalizedY = bounds.height ? y / bounds.height - 0.5 : 0

    hero.style.setProperty('--hero-focus-x', `${x}px`)
    hero.style.setProperty('--hero-focus-y', `${y}px`)
    hero.style.setProperty('--hero-pan-x', `${(-normalizedX * 10).toFixed(2)}px`)
    hero.style.setProperty('--hero-pan-y', `${(-normalizedY * 7).toFixed(2)}px`)
  })
}

const stopHeroTracking = () => {
  heroTracking.value = false
  pendingHeroPointer = null
  heroSection.value?.style.setProperty('--hero-pan-x', '0px')
  heroSection.value?.style.setProperty('--hero-pan-y', '0px')
}

const getMotionSurface = event => {
  const target = event.target
  return target instanceof Element ? target.closest('.motion-surface') : null
}

const handleSurfacePointer = event => {
  if (event.pointerType === 'touch' || prefersReducedMotion()) return
  const surface = getMotionSurface(event)
  if (!surface || !landingRoot.value?.contains(surface)) return

  const bounds = surface.getBoundingClientRect()
  const x = Math.min(Math.max(event.clientX - bounds.left, 0), bounds.width)
  const y = Math.min(Math.max(event.clientY - bounds.top, 0), bounds.height)
  const normalizedX = bounds.width ? x / bounds.width - 0.5 : 0
  const normalizedY = bounds.height ? y / bounds.height - 0.5 : 0

  surface.style.setProperty('--pointer-x', `${x.toFixed(1)}px`)
  surface.style.setProperty('--pointer-y', `${y.toFixed(1)}px`)
  surface.style.setProperty('--media-shift-x', `${(normalizedX * 7).toFixed(2)}px`)
  surface.style.setProperty('--media-shift-y', `${(normalizedY * 5).toFixed(2)}px`)
}

const handleSurfacePointerOut = event => {
  const surface = getMotionSurface(event)
  if (!surface || (event.relatedTarget instanceof Node && surface.contains(event.relatedTarget))) return
  surface.style.setProperty('--media-shift-x', '0px')
  surface.style.setProperty('--media-shift-y', '0px')
}

const scrollTo = id => {
  menuOpen.value = false
  document.getElementById(id)?.scrollIntoView({
    behavior: prefersReducedMotion() ? 'auto' : 'smooth',
    block: 'start'
  })
}

const enterPlatform = () => {
  menuOpen.value = false
  router.push(userStore.isLoggedIn ? '/dashboard' : '/login')
}

onMounted(async () => {
  pageReady.value = true
  window.addEventListener('scroll', scheduleScrollProgress, { passive: true })
  window.addEventListener('resize', scheduleScrollProgress)
  updateScrollProgress()
  await nextTick()
  setupRevealObserver()

  try {
    const response = await request.get('/landing/public', { silent: true })
    Object.assign(content, response.data || {})
    document.title = brandTitle.value
    await nextTick()
    setupRevealObserver()
  } catch {
    document.title = brandTitle.value
  }
})

onBeforeUnmount(() => {
  revealObserver?.disconnect()
  revealObserver = null
  if (revealFallbackTimer != null) {
    window.clearTimeout(revealFallbackTimer)
    revealFallbackTimer = null
  }
  revealCompleteTimers.forEach(timer => window.clearTimeout(timer))
  revealCompleteTimers.clear()
  window.removeEventListener('scroll', scheduleScrollProgress)
  window.removeEventListener('resize', scheduleScrollProgress)
  if (pointerFrame != null) window.cancelAnimationFrame(pointerFrame)
  if (scrollFrame != null) window.cancelAnimationFrame(scrollFrame)
  pointerFrame = null
  scrollFrame = null
})
</script>

<style scoped src="../styles/landing.css"></style>
