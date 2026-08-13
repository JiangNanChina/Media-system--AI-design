<template>
  <div ref="landingRoot" :class="['landing-page', { 'is-ready': pageReady }]">
    <header class="landing-nav">
      <router-link to="/" class="brand" aria-label="返回校融媒体首页">
        <span class="brand-mark">
          <img v-if="setting('site.logo')" :src="logoUrl" alt="校融媒体中心标志" />
          <el-icon v-else><Camera /></el-icon>
        </span>
        <span>{{ brandTitle }}</span>
      </router-link>

      <nav aria-label="站点导航">
        <a href="#hero">{{ setting('landing.nav.home_label', '首页') }}</a>
        <a href="#features">{{ setting('landing.nav.features_label', '校园特色') }}</a>
        <a href="#showcase">{{ setting('landing.nav.showcase_label', '部门风采') }}</a>
        <router-link to="/submission">{{ setting('landing.nav.submission_label', '视频投稿') }}</router-link>
        <router-link to="/join-us">加入我们</router-link>
      </nav>

      <el-button class="nav-login" type="primary" @click="enterPlatform">
        <el-icon><Right /></el-icon>
        {{ userStore.isLoggedIn ? '进入管理平台' : '登录' }}
      </el-button>
    </header>

    <main>
      <section id="hero" class="hero" aria-labelledby="hero-title">
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
        <div class="hero-content">
          <div class="hero-pill">
            <el-icon><Camera /></el-icon>
            <span>{{ setting('landing.hero.badge', '校园官方新媒体平台') }}</span>
          </div>
          <h1 id="hero-title">{{ setting('landing.hero.title', '记录校园，让每一种声音被看见') }}</h1>
          <p>{{ setting('landing.hero.subtitle', '校融媒体中心连接校园现场、青年创作与公共表达') }}</p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="$router.push('/submission')">
              <el-icon><UploadFilled /></el-icon>
              {{ setting('landing.hero.primary_cta', '视频投稿') }}
            </el-button>
            <el-button size="large" plain @click="$router.push('/join-us')">
              <el-icon><UserFilled /></el-icon>
              加入我们
            </el-button>
            <el-button size="large" plain @click="scrollTo('features')">{{ setting('landing.hero.secondary_cta', '了解我们') }}</el-button>
          </div>
        </div>

        <button class="scroll-cue" type="button" aria-label="查看校园特色" @click="scrollTo('features')">
          <el-icon><ArrowDown /></el-icon>
        </button>
      </section>

      <section id="features" class="features-band" aria-labelledby="features-title">
        <div class="section-shell features-section">
          <div class="section-heading centered" data-reveal>
            <span>{{ setting('landing.features.eyebrow', '校园特色') }}</span>
            <h2 id="features-title">{{ setting('landing.features.title', '发现不一样的校园') }}</h2>
            <p>{{ setting('landing.features.description', '在这里，每一个角落都有故事，每一刻时光都值得被记录') }}</p>
          </div>

          <div class="feature-grid" data-ai-section-type="card-list">
            <article
              v-for="(item, index) in campusItems"
              :key="item.id || item.title"
              class="image-card feature-card"
              data-reveal
              :style="{ '--reveal-index': index }"
            >
              <div class="card-image">
                <img v-if="item.mediaUrl" :src="mediaUrl(item.mediaUrl)" :alt="item.title" />
                <div v-else class="card-media-fallback">
                  <el-icon><Picture /></el-icon>
                </div>
              </div>
              <div class="card-copy">
                <div class="card-icon">
                  <el-icon><component :is="featureIcon(index)" /></el-icon>
                </div>
                <h3>{{ item.title }}</h3>
                <p>{{ item.summary }}</p>
              </div>
            </article>
          </div>
        </div>
      </section>

      <section id="showcase" class="showcase-band" aria-labelledby="showcase-title">
        <div class="section-shell">
          <div class="section-heading centered" data-reveal>
            <span>{{ setting('landing.showcase.eyebrow', '部门风采') }}</span>
            <h2 id="showcase-title">{{ setting('landing.showcase.title', '我们的故事') }}</h2>
            <p>{{ setting('landing.showcase.description', '一群热爱影像与创作的年轻人，用镜头讲述校园里的每一个精彩瞬间。') }}</p>
          </div>

          <el-carousel
            v-if="carouselDepartments.length >= 2"
            class="showcase-carousel"
            data-reveal
            height="420px"
            indicator-position="none"
            arrow="always"
          >
            <el-carousel-item v-for="item in carouselDepartments" :key="item.id || item.title">
              <div class="showcase-slide">
                <img v-if="item.mediaUrl" :src="mediaUrl(item.mediaUrl)" :alt="item.title" />
                <div v-else class="slide-media-fallback">
                  <el-icon><OfficeBuilding /></el-icon>
                </div>
                <div class="slide-overlay"></div>
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
              class="image-card department-card"
              data-reveal
              :style="{ '--reveal-index': index }"
            >
              <div class="card-image">
                <img v-if="item.mediaUrl" :src="mediaUrl(item.mediaUrl)" :alt="item.title" />
                <div v-else class="card-media-fallback">
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
        <div class="submit-card" data-reveal>
          <div class="submit-copy">
            <div class="section-heading">
              <span>{{ setting('landing.submission.eyebrow', '视频投稿') }}</span>
              <h2 id="submission-title">{{ setting('landing.submission.title', '把你的校园故事交给我们') }}</h2>
              <p>{{ setting('landing.submission.description', '支持校园新闻、人物、活动与创意短视频投稿') }}</p>
            </div>
            <div class="submit-actions">
              <el-button size="large" type="primary" @click="$router.push('/submission')">
                <el-icon><VideoCamera /></el-icon>
                {{ setting('landing.submission.primary_cta', '开始投稿') }}
              </el-button>
              <el-button size="large" plain @click="scrollTo('showcase')">
                <el-icon><OfficeBuilding /></el-icon>
                {{ setting('landing.submission.secondary_cta', '部门风采') }}
              </el-button>
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
          </div>

          <aside class="submit-board" aria-label="投稿方向">
            <div class="submit-visual" aria-hidden="true">
              <video v-if="heroIsVideo && heroMedia" :src="heroMedia" autoplay muted loop playsinline />
              <img v-else-if="heroMedia" :src="heroMedia" alt="" />
              <div v-else class="submit-visual-fallback">
                <el-icon><Camera /></el-icon>
              </div>
              <div class="submit-visual-badge">
                <el-icon><Camera /></el-icon>
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
      <div class="footer-inner">
        <section class="footer-about" data-reveal>
          <div class="footer-brand">
            <span class="footer-brand-mark">
              <img :src="logoUrl" alt="" />
            </span>
            <strong>{{ brandTitle }}</strong>
          </div>
          <p>{{ setting('landing.footer.description', '用镜头记录青春，用创意点亮校园。我们是校园里的记录者，用影像传递温度与力量。') }}</p>
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
      <div class="footer-bottom">
        <small>© {{ currentYear }} {{ brandTitle }} {{ setting('landing.footer.copyright_suffix', '版权所有') }}</small>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, h, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowDown,
  Brush,
  Camera,
  Film,
  Location,
  MagicStick,
  Message,
  OfficeBuilding,
  Picture,
  Phone,
  Reading,
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
const pageReady = ref(false)

let revealObserver = null
const revealCompleteTimers = new Set()
let revealFallbackTimer = null

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

const WeChatIcon = createBrandIcon('WeChatIcon', 'M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213c0 .163.13.295.29.295a.33.33 0 0 0 .167-.054l1.903-1.114a.86.86 0 0 1 .717-.098a10.2 10.2 0 0 0 2.837.403c.276 0 .543-.027.811-.05c-.857-2.578.157-4.972 1.932-6.446c1.703-1.415 3.882-1.98 5.853-1.838c-.576-3.583-4.196-6.348-8.596-6.348M5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178a1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18m5.34 2.867c-1.797-.052-3.746.512-5.28 1.786c-1.72 1.428-2.687 3.72-1.78 6.22c.942 2.453 3.666 4.229 6.884 4.229c.826 0 1.622-.12 2.361-.336a.72.72 0 0 1 .598.082l1.584.926a.3.3 0 0 0 .14.047c.134 0 .24-.111.24-.247c0-.06-.023-.12-.038-.177l-.327-1.233a.6.6 0 0 1-.023-.156a.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.01-.27-.027-.407-.03zm-2.53 3.274c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983a.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983a.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982')
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
const featureIcons = [Brush, Reading, MagicStick, Picture]
const featureIcon = index => featureIcons[index % featureIcons.length]
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
const footerContacts = computed(() => {
  return [
    { key: 'email', icon: Message, value: footerEmail.value, href: footerEmail.value ? `mailto:${footerEmail.value}` : '' },
    { key: 'phone', icon: Phone, value: footerPhone.value, href: footerPhone.value ? `tel:${footerPhone.value.replace(/[^\d+]/g, '')}` : '' },
    { key: 'address', icon: Location, value: footerAddress.value, href: '' }
  ].filter(item => item.value)
})

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

const scrollTo = id => {
  document.getElementById(id)?.scrollIntoView({
    behavior: prefersReducedMotion() ? 'auto' : 'smooth',
    block: 'start'
  })
}
const enterPlatform = () => router.push(userStore.isLoggedIn ? '/dashboard' : '/login')

onMounted(async () => {
  pageReady.value = true
  await nextTick()
  setupRevealObserver()

  const response = await request.get('/landing/public', { silent: true })
  Object.assign(content, response.data || {})
  document.title = brandTitle.value
  await nextTick()
  setupRevealObserver()
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
})
</script>

<style scoped>
.landing-page {
  --landing-ease: cubic-bezier(0.16, 1, 0.3, 1);
  --landing-ease-soft: cubic-bezier(0.22, 1, 0.36, 1);
  min-height: 100vh;
  color: #12384f;
  background: #f5fbff;
  position: relative;
  z-index: 2;
  overflow-x: hidden;
}

.landing-nav {
  position: fixed;
  z-index: 30;
  top: 18px;
  left: 50%;
  width: min(1240px, calc(100% - 40px));
  min-height: 72px;
  padding: 10px 18px;
  display: grid;
  grid-template-columns: minmax(210px, 1fr) auto minmax(180px, 1fr);
  align-items: center;
  gap: 18px;
  color: #12384f;
  background: linear-gradient(135deg, rgba(245, 253, 255, 0.42), rgba(224, 242, 245, 0.24));
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 20px;
  box-shadow: 0 14px 32px rgba(0, 20, 32, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(22px) saturate(1.12);
  -webkit-backdrop-filter: blur(22px) saturate(1.12);
  opacity: 0;
  transform: translate3d(-50%, -14px, 0);
  transition:
    opacity 520ms var(--landing-ease),
    transform 520ms var(--landing-ease),
    background 240ms ease,
    box-shadow 240ms ease,
    border-color 240ms ease;
}

.landing-page.is-ready .landing-nav {
  opacity: 1;
  transform: translate3d(-50%, 0, 0);
}

.brand,
.footer-brand,
nav,
.social-links,
.contact-item,
.hero-pill,
.hero-actions,
.submit-actions {
  display: flex;
  align-items: center;
}

.brand {
  min-width: 0;
  justify-self: start;
  gap: 12px;
  color: #0d2d40;
  font-size: clamp(19px, 1.55vw, 24px);
  font-weight: 800;
  text-decoration: none;
}

.brand-mark {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  color: #fff;
  font-size: 23px;
  background: linear-gradient(135deg, #34c3d6, #24b8d7);
  border: 1px solid rgba(255, 255, 255, 0.58);
  border-radius: 999px;
  overflow: hidden;
  box-shadow: 0 12px 28px rgba(36, 184, 215, 0.22);
  transition: transform 220ms var(--landing-ease), box-shadow 220ms ease;
}

.brand:hover .brand-mark,
.brand:focus-visible .brand-mark {
  transform: translateY(-1px);
  box-shadow: 0 16px 34px rgba(36, 184, 215, 0.28);
}

.brand-mark img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
  border-radius: 999px;
}

.brand > span:not(.brand-mark) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

nav {
  justify-self: center;
  gap: 8px;
}

nav a {
  min-height: 42px;
  padding: 0 clamp(14px, 1.45vw, 22px);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #12afc9;
  font-size: clamp(14px, 1.05vw, 17px);
  font-weight: 600;
  text-decoration: none;
  white-space: nowrap;
  background: rgba(228, 250, 252, 0.38);
  border: 1px solid rgba(255, 255, 255, 0.34);
  border-radius: 999px;
  transition:
    color 200ms ease,
    transform 200ms var(--landing-ease),
    background 200ms ease,
    border-color 200ms ease,
    box-shadow 200ms ease;
}

nav a:hover,
nav a:focus-visible {
  color: #0695b1;
  background: rgba(224, 249, 252, 0.62);
  border-color: rgba(255, 255, 255, 0.54);
  box-shadow: 0 10px 22px rgba(14, 84, 105, 0.12);
  transform: translateY(-1px);
}

.nav-login {
  justify-self: end;
}

#app .landing-page .landing-nav :deep(.nav-login.el-button.el-button--primary) {
  min-width: 128px;
  min-height: 42px;
  padding: 0 22px;
  color: #fff !important;
  font-size: 15px;
  font-weight: 700 !important;
  background: linear-gradient(135deg, #35bdd4, #23b5d3) !important;
  border-color: rgba(255, 255, 255, 0.42) !important;
  border-radius: 999px !important;
  box-shadow: 0 14px 30px rgba(35, 181, 211, 0.22) !important;
}

#app .landing-page .landing-nav :deep(.nav-login.el-button.el-button--primary:hover),
#app .landing-page .landing-nav :deep(.nav-login.el-button.el-button--primary:focus-visible) {
  color: #fff !important;
  background: linear-gradient(135deg, #43c8dc, #12a9ca) !important;
  box-shadow: 0 18px 36px rgba(35, 181, 211, 0.3) !important;
}

.hero {
  position: relative;
  min-height: calc(100svh - 16px);
  display: grid;
  place-items: center;
  overflow: hidden;
  background: #092d3d;
}

.hero-media,
.hero-fallback {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transform: scale(1.035);
  transition: opacity 900ms ease, transform 1300ms var(--landing-ease-soft);
}

.landing-page.is-ready .hero-media,
.landing-page.is-ready .hero-fallback {
  opacity: 1;
  transform: scale(1);
}

.hero-fallback {
  display: grid;
  place-items: center;
  background:
    linear-gradient(rgba(10, 47, 64, 0.18) 1px, transparent 1px),
    linear-gradient(90deg, rgba(10, 47, 64, 0.18) 1px, transparent 1px),
    #0c3a4f;
  background-size: 34px 34px;
}

.hero-fallback img {
  width: min(320px, 48vw);
  aspect-ratio: 1;
  max-height: 42%;
  object-fit: contain;
  border-radius: 999px;
  opacity: 0.5;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 50% 42%, rgba(37, 184, 242, 0.18), transparent 34%),
    linear-gradient(180deg, rgba(4, 24, 34, 0.28), rgba(4, 24, 34, 0.68)),
    rgba(0, 0, 0, 0.28);
  opacity: 0;
  transition: opacity 900ms ease 100ms;
}

.landing-page.is-ready .hero-overlay {
  opacity: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  width: min(920px, calc(100% - 40px));
  padding: 128px 0 96px;
  color: #fff;
  text-align: center;
}

.hero-pill,
.hero h1,
.hero-content > p,
.hero-actions {
  opacity: 0;
  transform: translate3d(0, 20px, 0);
}

.landing-page.is-ready .hero-pill {
  animation: landing-rise 680ms var(--landing-ease) 160ms both;
}

.landing-page.is-ready .hero h1 {
  animation: landing-rise 720ms var(--landing-ease) 260ms both;
}

.landing-page.is-ready .hero-content > p {
  animation: landing-rise 700ms var(--landing-ease) 390ms both;
}

.landing-page.is-ready .hero-actions {
  animation: landing-rise 660ms var(--landing-ease) 520ms both;
}

.hero-pill {
  width: max-content;
  max-width: 100%;
  gap: 8px;
  margin: 0 auto 24px;
  padding: 9px 15px;
  color: rgba(255, 255, 255, 0.92);
  font-size: 14px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 999px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.hero h1 {
  margin: 0 auto 22px;
  max-width: 860px;
  color: #fff;
  font-size: clamp(42px, 7vw, 76px);
  line-height: 1.08;
  font-weight: 800;
  letter-spacing: 0;
  text-shadow: 0 3px 16px rgba(0, 0, 0, 0.35);
}

.hero-content > p {
  max-width: 660px;
  margin: 0 auto;
  color: rgba(255, 255, 255, 0.88);
  font-size: clamp(17px, 2.2vw, 23px);
  font-weight: 400;
  line-height: 1.75;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.3);
}

.hero-actions {
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 34px;
}

.hero-actions :deep(.el-button) {
  min-width: 132px;
  min-height: 46px;
  transition:
    transform 200ms var(--landing-ease),
    box-shadow 200ms ease,
    background 200ms ease,
    border-color 200ms ease,
    color 200ms ease;
}

.hero-actions :deep(.el-button.is-plain) {
  color: #fff !important;
  background: rgba(255, 255, 255, 0.13) !important;
  border-color: rgba(255, 255, 255, 0.5) !important;
}

.hero-actions :deep(.el-button.is-plain:hover),
.hero-actions :deep(.el-button.is-plain:focus-visible) {
  color: #075985 !important;
  background: rgba(255, 255, 255, 0.88) !important;
}

.scroll-cue {
  position: absolute;
  z-index: 3;
  bottom: 28px;
  left: 50%;
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.36);
  border-radius: 999px;
  opacity: 0;
  transform: translate3d(-50%, 10px, 0);
  transition: color 200ms ease, background 200ms ease, border-color 200ms ease;
}

.landing-page.is-ready .scroll-cue {
  animation:
    scroll-cue-enter 520ms var(--landing-ease) 760ms both,
    cue-bounce 2.2s ease-in-out 1.35s infinite;
}

.scroll-cue:hover,
.scroll-cue:focus-visible {
  color: #fff;
  background: rgba(255, 255, 255, 0.22);
  border-color: rgba(255, 255, 255, 0.7);
}

.section-shell {
  width: min(1180px, calc(100% - 40px));
  margin: 0 auto;
  padding: 88px 0;
  scroll-margin-top: 96px;
}

.features-band {
  background: #f7fbfc;
  border-top: 1px solid rgba(15, 57, 72, 0.08);
  border-bottom: 1px solid rgba(15, 57, 72, 0.08);
  scroll-margin-top: 96px;
}

.features-section {
  width: min(1320px, calc(100% - 96px));
  padding: 82px 0 86px;
}

[data-reveal] {
  opacity: 0;
  filter: blur(8px);
  transform: translate3d(0, 26px, 0);
  transition:
    opacity 720ms var(--landing-ease-soft),
    transform 720ms var(--landing-ease-soft),
    filter 720ms ease;
  transition-delay: calc(var(--reveal-index, 0) * 70ms);
  will-change: opacity, transform, filter;
}

[data-reveal].is-visible {
  opacity: 1;
  filter: blur(0);
  transform: translate3d(0, 0, 0);
}

[data-reveal].is-reveal-complete {
  transition-delay: 0ms;
  will-change: auto;
}

.section-heading {
  margin-bottom: 42px;
}

.section-heading.centered {
  text-align: center;
}

.section-heading span {
  display: inline-flex;
  align-items: center;
  padding: 7px 14px;
  color: #087fc4;
  font-size: 13px;
  font-weight: 800;
  line-height: 1;
  background: rgba(21, 158, 229, 0.1);
  border-radius: 999px;
}

.section-heading h2 {
  margin: 18px 0 12px;
  color: #12384f;
  font-size: clamp(30px, 4vw, 42px);
  line-height: 1.18;
  font-weight: 800;
  letter-spacing: 0;
}

.section-heading p {
  max-width: 660px;
  margin: 0 auto;
  color: #4f6d82;
  font-size: 16px;
  line-height: 1.75;
}

.features-section .section-heading {
  margin-bottom: 50px;
}

.features-section .section-heading span {
  padding: 10px 22px;
  color: #19b8d2;
  font-size: 16px;
  font-weight: 500;
  background: #e8f8fb;
}

.features-section .section-heading h2 {
  margin: 22px 0 14px;
  color: #172933;
  font-size: 40px;
  line-height: 1.16;
  font-weight: 900;
}

.features-section .section-heading p {
  max-width: none;
  color: #607b92;
  font-size: 18px;
  line-height: 1.7;
}

.feature-grid,
.department-grid {
  display: grid;
  gap: 24px;
}

.feature-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 32px;
}

.department-grid {
  width: min(100%, 1320px);
  grid-template-columns: repeat(auto-fit, minmax(220px, 300px));
  justify-content: center;
  gap: 28px;
  margin: 58px auto 0;
}

.image-card {
  overflow: hidden;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(135, 185, 214, 0.22);
  border-radius: 8px;
  box-shadow: 0 10px 28px rgba(21, 122, 177, 0.08);
  transition:
    opacity 720ms var(--landing-ease-soft),
    filter 720ms ease,
    transform 240ms var(--landing-ease),
    box-shadow 240ms ease,
    border-color 240ms ease,
    background 240ms ease;
}

.image-card:hover,
.image-card:focus-within {
  transform: translateY(-4px);
  border-color: rgba(24, 185, 236, 0.32);
  box-shadow: 0 22px 54px rgba(21, 122, 177, 0.15);
}

.card-image {
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: #e8f5fa;
}

.card-image img,
.showcase-slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-image img {
  animation: media-soft-in 620ms var(--landing-ease-soft) both;
  transition: transform 560ms var(--landing-ease-soft), filter 260ms ease;
}

.image-card:hover .card-image img {
  transform: scale(1.055);
  filter: saturate(1.05) contrast(1.03);
}

.card-media-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: #087fc4;
  font-size: 34px;
  background:
    linear-gradient(135deg, rgba(242, 251, 255, 0.95), rgba(231, 255, 248, 0.86)),
    #f2fbff;
}

.card-copy {
  padding: 24px;
}

.feature-card {
  display: flex;
  min-height: 100%;
  flex-direction: column;
  background: #fff;
  border-color: rgba(15, 57, 72, 0.1);
  box-shadow: 0 3px 10px rgba(12, 40, 52, 0.08);
}

.feature-card .card-image {
  aspect-ratio: 16 / 9;
  background: #edf6f8;
}

.feature-card .card-copy {
  min-height: 196px;
  padding: 28px 30px 30px;
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: flex-start;
}

.card-icon {
  width: 42px;
  height: 42px;
  margin-bottom: 16px;
  display: grid;
  place-items: center;
  color: #087fc4;
  background: rgba(21, 158, 229, 0.1);
  border-radius: 8px;
}

.feature-card .card-icon {
  width: 50px;
  height: 50px;
  margin-bottom: 22px;
  color: #24bed7;
  font-size: 24px;
  background: #e8f8fb;
  border-radius: 999px;
}

.card-copy h3 {
  margin: 0 0 10px;
  color: #12384f;
  font-size: 20px;
  line-height: 1.3;
  font-weight: 800;
  letter-spacing: 0;
}

.feature-card .card-copy h3 {
  margin-bottom: 10px;
  color: #071e2c;
  font-size: 24px;
  line-height: 1.24;
  font-weight: 900;
}

.card-copy p {
  margin: 0;
  color: #4f6d82;
  font-size: 14px;
  line-height: 1.75;
}

.feature-card .card-copy p {
  color: #5f788f;
  font-size: 16px;
  line-height: 1.68;
}

.showcase-band {
  background: linear-gradient(180deg, #fbfdff 0%, #f5fbff 100%);
  border-top: 1px solid rgba(135, 185, 214, 0.18);
  border-bottom: 1px solid rgba(135, 185, 214, 0.18);
  scroll-margin-top: 96px;
}

.showcase-band .section-shell {
  width: min(1360px, calc(100% - 64px));
  padding: 88px 0 96px;
}

.showcase-band .section-heading {
  margin-bottom: 48px;
}

.department-card {
  background: #fff;
  border-color: rgba(122, 171, 202, 0.18);
  box-shadow: 0 16px 42px rgba(12, 62, 82, 0.1);
}

.department-card .card-image {
  aspect-ratio: 4 / 3;
}

.department-card .card-copy {
  min-height: 116px;
  padding: 22px 24px 24px;
}

.department-card .card-copy h3 {
  margin-bottom: 8px;
  color: #102d42;
  font-size: 18px;
  line-height: 1.32;
  font-weight: 800;
}

.department-card .card-copy p {
  color: #5d778d;
  font-size: 15px;
  line-height: 1.65;
}

.showcase-carousel {
  width: min(100%, 1240px);
  margin: 0 auto;
}

.showcase-carousel :deep(.el-carousel__container) {
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 28px 70px rgba(12, 62, 82, 0.16);
}

.showcase-carousel :deep(.el-carousel__item) {
  transition:
    transform 760ms var(--landing-ease-soft),
    opacity 760ms ease !important;
}

.showcase-carousel :deep(.el-carousel__arrow) {
  color: #12384f;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(135, 185, 214, 0.2);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  transition:
    color 200ms ease,
    transform 200ms var(--landing-ease),
    background 200ms ease,
    border-color 200ms ease,
    box-shadow 200ms ease;
}

.showcase-carousel :deep(.el-carousel__arrow:hover),
.showcase-carousel :deep(.el-carousel__arrow:focus-visible) {
  transform: translateY(-50%) scale(1.04);
  background: rgba(255, 255, 255, 0.96);
  border-color: rgba(35, 181, 211, 0.34);
  box-shadow: 0 12px 26px rgba(12, 62, 82, 0.14);
}

.showcase-slide {
  position: relative;
  height: 100%;
  overflow: hidden;
}

.showcase-slide img {
  transform: scale(1.035);
  transition: transform 900ms var(--landing-ease-soft);
}

.showcase-carousel :deep(.el-carousel__item.is-active) .showcase-slide img {
  transform: scale(1);
}

.slide-media-fallback {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: rgba(255, 255, 255, 0.38);
  font-size: clamp(54px, 8vw, 96px);
  background:
    radial-gradient(circle at 78% 18%, rgba(255, 255, 255, 0.2), transparent 24%),
    linear-gradient(135deg, rgba(18, 64, 84, 0.92), rgba(6, 28, 39, 0.98) 58%, rgba(8, 44, 54, 0.92));
}

.slide-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(4, 24, 34, 0.04) 0%, rgba(4, 24, 34, 0.18) 42%, rgba(4, 24, 34, 0.82) 100%),
    linear-gradient(90deg, rgba(4, 24, 34, 0.56), transparent 46%);
}

.slide-copy {
  position: absolute;
  left: clamp(26px, 4vw, 44px);
  right: clamp(26px, 4vw, 44px);
  bottom: clamp(24px, 4vw, 36px);
  color: #fff;
  opacity: 0;
  transform: translate3d(0, 18px, 0);
  transition: opacity 520ms ease, transform 520ms var(--landing-ease);
}

.showcase-carousel :deep(.el-carousel__item.is-active) .slide-copy {
  opacity: 1;
  transform: translate3d(0, 0, 0);
  transition-delay: 180ms;
}

.slide-copy h3 {
  margin: 0 0 8px;
  font-size: clamp(24px, 3vw, 32px);
  line-height: 1.18;
  font-weight: 800;
  letter-spacing: 0;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.32);
}

.slide-copy p {
  max-width: 560px;
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 16px;
  line-height: 1.7;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.28);
}

.submit-section {
  width: min(1180px, calc(100% - 48px));
  margin: 0 auto;
  padding: 88px 0 96px;
}

.submit-card {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1.04fr) minmax(340px, 0.86fr);
  gap: clamp(28px, 4vw, 46px);
  align-items: center;
  overflow: hidden;
  padding: clamp(30px, 5vw, 54px);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(255, 255, 255, 0.98) 58%, rgba(236, 251, 255, 0.96) 100%);
  border: 1px solid rgba(135, 185, 214, 0.24);
  border-radius: 8px;
  box-shadow: 0 22px 58px rgba(21, 122, 177, 0.12);
  transition:
    opacity 720ms var(--landing-ease-soft),
    filter 720ms ease,
    transform 720ms var(--landing-ease-soft),
    border-color 240ms ease,
    box-shadow 240ms ease;
}

.submit-card::before {
  content: '';
  position: absolute;
  inset: 0 0 auto;
  height: 5px;
  background: linear-gradient(90deg, #20b8d2, #f59e0b, #159ee5);
  transform: scaleX(0);
  transform-origin: left center;
  transition: transform 820ms var(--landing-ease) 220ms;
}

.submit-card.is-visible::before {
  transform: scaleX(1);
}

.submit-copy {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.submit-copy .section-heading {
  margin-bottom: 28px;
}

.submit-copy .section-heading p {
  max-width: 560px;
  margin: 0;
}

.submit-actions {
  justify-content: flex-start;
  flex-wrap: wrap;
  gap: 12px;
}

.submit-actions :deep(.el-button) {
  min-width: 148px;
  min-height: 46px;
  border-radius: 999px;
}

.submit-actions :deep(.el-button.el-button--primary) {
  color: #fff !important;
  background: linear-gradient(135deg, #f97316, #f59e0b) !important;
  border-color: rgba(249, 115, 22, 0.36) !important;
  box-shadow: 0 16px 32px rgba(249, 115, 22, 0.22) !important;
}

.submit-actions :deep(.el-button.el-button--primary:hover),
.submit-actions :deep(.el-button.el-button--primary:focus-visible) {
  background: linear-gradient(135deg, #fb923c, #f97316) !important;
  box-shadow: 0 18px 36px rgba(249, 115, 22, 0.28) !important;
}

.submit-actions :deep(.el-button.is-plain) {
  color: #0f6f84 !important;
  background: rgba(232, 248, 251, 0.7) !important;
  border-color: rgba(32, 184, 210, 0.22) !important;
}

.submit-actions :deep(.el-button.is-plain:hover),
.submit-actions :deep(.el-button.is-plain:focus-visible) {
  color: #075b6d !important;
  background: rgba(217, 247, 252, 0.96) !important;
  border-color: rgba(32, 184, 210, 0.42) !important;
}

.submit-flow {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-top: 34px;
  border-top: 1px solid rgba(18, 56, 79, 0.1);
  border-bottom: 1px solid rgba(18, 56, 79, 0.1);
}

.submit-step {
  min-width: 0;
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  padding: 17px 14px 17px 0;
  opacity: 0;
  transform: translate3d(0, 10px, 0);
  transition: opacity 460ms ease, transform 460ms var(--landing-ease);
  transition-delay: calc(260ms + var(--reveal-index, 0) * 80ms);
}

.submit-step + .submit-step {
  padding-left: 16px;
  border-left: 1px solid rgba(18, 56, 79, 0.1);
}

.submit-step span {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  color: #c75a0e;
  font-size: 12px;
  font-weight: 900;
  background: #fff7ed;
  border: 1px solid rgba(249, 115, 22, 0.18);
  border-radius: 999px;
}

.submit-step strong {
  overflow: hidden;
  color: #173f56;
  font-size: 15px;
  line-height: 1.4;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.submit-card.is-visible .submit-step,
.submit-card.is-visible .submit-topic {
  opacity: 1;
  transform: translate3d(0, 0, 0);
}

.submit-board {
  position: relative;
  z-index: 1;
  min-width: 0;
  padding: 18px;
  color: #fff;
  background: linear-gradient(155deg, #0b3a4f 0%, #062435 68%, #104c61 100%);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 8px;
  box-shadow: 0 24px 48px rgba(7, 46, 63, 0.18);
}

.submit-visual {
  position: relative;
  min-height: 222px;
  overflow: hidden;
  background: #0b4056;
  border-radius: 8px;
}

.submit-visual::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(5, 25, 35, 0.02), rgba(5, 25, 35, 0.62)),
    linear-gradient(90deg, rgba(5, 25, 35, 0.58), transparent 56%);
}

.submit-visual img,
.submit-visual video {
  width: 100%;
  height: 100%;
  min-height: 222px;
  display: block;
  object-fit: cover;
  animation: media-soft-in 620ms var(--landing-ease-soft) both;
  transition: transform 620ms var(--landing-ease-soft), filter 240ms ease;
}

.submit-board:hover .submit-visual img,
.submit-board:hover .submit-visual video {
  transform: scale(1.035);
  filter: saturate(1.06) contrast(1.04);
}

.submit-visual-fallback {
  min-height: 222px;
  display: grid;
  place-items: center;
  color: rgba(255, 255, 255, 0.48);
  font-size: 56px;
  background:
    linear-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.08) 1px, transparent 1px),
    #0b4056;
  background-size: 28px 28px;
}

.submit-visual-badge {
  position: absolute;
  z-index: 1;
  left: 16px;
  bottom: 16px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: calc(100% - 32px);
  padding: 8px 12px;
  color: rgba(255, 255, 255, 0.92);
  font-size: 13px;
  font-weight: 800;
  line-height: 1;
  background: rgba(5, 25, 35, 0.46);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 999px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  opacity: 0;
  transform: translate3d(0, 10px, 0);
  transition: opacity 480ms ease 260ms, transform 480ms var(--landing-ease) 260ms;
}

.submit-card.is-visible .submit-visual-badge {
  opacity: 1;
  transform: translate3d(0, 0, 0);
}

.submit-topic-list {
  display: grid;
  margin-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.submit-topic {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  min-width: 0;
  padding: 15px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  opacity: 0;
  transform: translate3d(0, 12px, 0);
  transition: opacity 460ms ease, transform 460ms var(--landing-ease);
  transition-delay: calc(320ms + var(--reveal-index, 0) * 80ms);
}

.submit-topic .el-icon {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  color: #fef3c7;
  font-size: 18px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 999px;
}

.submit-topic span {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.submit-topic strong {
  color: #fff;
  font-size: 15px;
  line-height: 1.25;
  font-weight: 800;
}

.submit-topic small {
  overflow: hidden;
  color: rgba(255, 255, 255, 0.68);
  font-size: 13px;
  line-height: 1.45;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.landing-footer {
  color: rgba(255, 255, 255, 0.74);
  background: #092d3d;
}

.footer-inner {
  width: min(1180px, calc(100% - 64px));
  margin: 0 auto;
  padding: 68px 0 58px;
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(180px, 0.7fr) minmax(220px, 0.92fr);
  gap: clamp(36px, 4vw, 88px);
  align-items: start;
}

.footer-about {
  min-width: 0;
}

.footer-brand {
  gap: 12px;
  margin-bottom: 18px;
  color: #fff;
}

.footer-brand-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  background: linear-gradient(135deg, #36c6d8, #1cb8d7);
  border-radius: 999px;
  overflow: hidden;
}

.footer-brand-mark img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
  border-radius: 999px;
}

.footer-brand strong {
  font-size: 20px;
  line-height: 1.2;
  font-weight: 800;
}

.landing-footer h2 {
  margin: 0 0 18px;
  color: #fff;
  font-size: 18px;
  line-height: 1.3;
  font-weight: 800;
  letter-spacing: 0;
}

.landing-footer p {
  max-width: 420px;
  margin: 0;
  color: rgba(220, 233, 238, 0.72);
  font-size: 15px;
  line-height: 1.9;
}

.footer-social {
  text-align: center;
}

.social-links {
  justify-content: center;
  gap: 14px;
}

.social-links a {
  position: relative;
  width: 50px;
  height: 50px;
  display: grid;
  place-items: center;
  color: rgba(241, 248, 250, 0.9);
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 999px;
  cursor: pointer;
  text-decoration: none;
  transition: color 200ms ease, background 200ms ease, border-color 200ms ease, box-shadow 200ms ease;
}

.social-links a :deep(.el-icon) {
  position: relative;
  z-index: 1;
  font-size: 20px;
}

.social-links a :deep(.brand-social-icon) {
  width: 1em;
  height: 1em;
  display: block;
}

.social-links a:hover,
.social-links a:focus-visible {
  color: #fff;
  background: rgba(31, 194, 212, 0.92);
  border-color: rgba(31, 194, 212, 0.28);
  box-shadow: 0 14px 28px rgba(31, 194, 212, 0.24);
}

.social-links a.is-wechat:hover,
.social-links a.is-wechat:focus-visible {
  background: #07c160;
  border-color: rgba(7, 193, 96, 0.5);
  box-shadow: 0 14px 28px rgba(7, 193, 96, 0.22);
}

.social-links a.is-douyin:hover,
.social-links a.is-douyin:focus-visible {
  background: #111827;
  border-color: rgba(255, 255, 255, 0.18);
  box-shadow: 0 14px 28px rgba(0, 0, 0, 0.24);
}

.social-tooltip {
  position: absolute;
  z-index: 4;
  top: calc(100% + 10px);
  left: 50%;
  padding: 7px 10px;
  color: #083447;
  font-size: 12px;
  font-weight: 800;
  line-height: 1;
  white-space: nowrap;
  pointer-events: none;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(180, 219, 231, 0.48);
  border-radius: 8px;
  box-shadow: 0 12px 26px rgba(0, 18, 30, 0.18);
  opacity: 0;
  transform: translate(-50%, -4px);
  transition: opacity 180ms ease, transform 180ms ease;
}

.social-tooltip::before {
  content: '';
  position: absolute;
  left: 50%;
  top: -5px;
  width: 9px;
  height: 9px;
  background: rgba(255, 255, 255, 0.96);
  border-top: 1px solid rgba(180, 219, 231, 0.48);
  border-left: 1px solid rgba(180, 219, 231, 0.48);
  transform: translateX(-50%) rotate(45deg);
}

.social-links a:hover .social-tooltip,
.social-links a:focus-visible .social-tooltip {
  opacity: 1;
  transform: translate(-50%, 0);
}

.footer-contact {
  justify-self: end;
  min-width: 0;
}

.contact-list {
  display: grid;
  gap: 12px;
}

.contact-item {
  min-width: 0;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: rgba(220, 233, 238, 0.84);
  font-size: 15px;
  line-height: 1.4;
  font-weight: 600;
  text-decoration: none;
}

.contact-item p {
  margin: 0;
}

.contact-item .el-icon {
  flex: 0 0 auto;
  color: #27c4d7;
  font-size: 18px;
}

.wechat-qr {
  width: 86px;
  height: 86px;
  margin-top: 16px;
  object-fit: cover;
  border: 4px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
}

.footer-bottom {
  width: min(1180px, calc(100% - 64px));
  margin: 0 auto;
  padding: 34px 0 42px;
  color: rgba(220, 233, 238, 0.42);
  text-align: center;
  border-top: 1px solid rgba(220, 233, 238, 0.11);
}

.footer-bottom small {
  font-size: 14px;
  line-height: 1.5;
  font-weight: 500;
}

@keyframes cue-bounce {
  0%,
  100% {
    translate: 0 0;
  }
  50% {
    translate: 0 8px;
  }
}

@keyframes landing-rise {
  from {
    opacity: 0;
    transform: translate3d(0, 18px, 0);
    filter: blur(6px);
  }

  to {
    opacity: 1;
    transform: translate3d(0, 0, 0);
    filter: blur(0);
  }
}

@keyframes scroll-cue-enter {
  from {
    opacity: 0;
    transform: translate3d(-50%, 18px, 0);
  }

  to {
    opacity: 1;
    transform: translate3d(-50%, 0, 0);
  }
}

@keyframes media-soft-in {
  from {
    opacity: 0;
    transform: scale(1.045);
    filter: blur(4px);
  }

  to {
    opacity: 1;
    transform: scale(1);
    filter: blur(0);
  }
}

@media (max-width: 1024px) {
  .landing-nav {
    top: 14px;
    width: min(100% - 28px, 980px);
    min-height: 64px;
    padding: 9px 14px;
    grid-template-columns: minmax(180px, 1fr) auto minmax(150px, 1fr);
    gap: 14px;
  }

  .brand-mark {
    width: 42px;
    height: 42px;
    font-size: 21px;
  }

  nav {
    gap: 6px;
  }

  nav a {
    min-height: 38px;
    padding: 0 13px;
    font-size: 14px;
  }

  #app .landing-page .landing-nav :deep(.nav-login.el-button.el-button--primary) {
    min-width: 104px;
    min-height: 38px;
    padding: 0 16px;
    font-size: 14px;
  }

  .feature-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 24px;
  }

  .features-section {
    width: min(100% - 48px, 920px);
    padding: 68px 0;
  }

  .features-section .section-heading {
    margin-bottom: 38px;
  }

  .features-section .section-heading h2 {
    font-size: 40px;
  }

  .features-section .section-heading p {
    font-size: 18px;
  }

  .feature-card .card-copy {
    min-height: 186px;
    padding: 24px;
  }

  .feature-card .card-icon {
    margin-bottom: 18px;
  }

  .feature-card .card-copy h3 {
    font-size: 22px;
  }

  .department-grid {
    width: min(100%, 640px);
    grid-template-columns: repeat(2, minmax(0, 300px));
    justify-content: center;
  }

  .showcase-carousel {
    width: 100%;
  }

  .footer-inner {
    width: min(100% - 48px, 1060px);
    padding: 60px 0 52px;
    gap: 42px;
  }

  .footer-bottom {
    width: min(100% - 48px, 1060px);
    padding: 30px 0 38px;
  }

  .footer-brand strong {
    font-size: 18px;
  }

  .landing-footer h2 {
    font-size: 16px;
  }

  .landing-footer p {
    font-size: 14px;
  }

  .submit-section {
    width: min(100% - 48px, 920px);
    padding: 72px 0 80px;
  }

  .submit-card {
    grid-template-columns: 1fr;
  }

  .submit-board {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(230px, 0.72fr);
    align-items: stretch;
    gap: 16px;
  }

  .submit-topic-list {
    margin-top: 0;
    border-top: 0;
  }

  .submit-visual,
  .submit-visual img,
  .submit-visual video,
  .submit-visual-fallback {
    min-height: 248px;
  }
}

@media (max-width: 760px) {
  .landing-nav {
    top: 10px;
    width: calc(100% - 20px);
    min-height: 56px;
    padding: 7px 10px;
    border-radius: 16px;
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 12px;
  }

  .landing-nav nav {
    display: none;
  }

  .brand {
    gap: 10px;
    font-size: 15px;
  }

  .brand-mark {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }

  .brand > span:not(.brand-mark) {
    max-width: 154px;
  }

  #app .landing-page .landing-nav :deep(.nav-login.el-button.el-button--primary) {
    min-width: 76px;
    min-height: 34px;
    padding: 0 12px;
    font-size: 13px;
  }

  .hero {
    min-height: 760px;
  }

  .hero-content {
    width: min(100% - 32px, 680px);
    padding: 110px 0 88px;
  }

  .hero h1 {
    font-size: clamp(36px, 12vw, 50px);
  }

  .hero-content > p {
    font-size: 17px;
  }

  .section-shell,
  .submit-section {
    width: min(100% - 32px, 680px);
    padding: 66px 0;
  }

  .submit-card {
    gap: 24px;
    padding: 24px;
  }

  .submit-copy .section-heading {
    text-align: center;
  }

  .submit-copy .section-heading p {
    margin: 0 auto;
  }

  .submit-actions {
    justify-content: center;
  }

  .submit-flow {
    grid-template-columns: 1fr;
    margin-top: 26px;
  }

  .submit-step {
    padding: 14px 0;
  }

  .submit-step + .submit-step {
    padding-left: 0;
    border-top: 1px solid rgba(18, 56, 79, 0.1);
    border-left: 0;
  }

  .submit-board {
    display: block;
    padding: 14px;
  }

  .submit-topic-list {
    margin-top: 14px;
    border-top: 1px solid rgba(255, 255, 255, 0.12);
  }

  .submit-visual,
  .submit-visual img,
  .submit-visual video,
  .submit-visual-fallback {
    min-height: 188px;
  }

  .features-section {
    width: min(100% - 32px, 680px);
    padding: 58px 0;
  }

  .section-heading {
    margin-bottom: 30px;
  }

  .features-section .section-heading {
    margin-bottom: 30px;
  }

  .features-section .section-heading span {
    padding: 9px 18px;
    font-size: 15px;
  }

  .features-section .section-heading h2 {
    margin: 20px 0 12px;
    font-size: 32px;
  }

  .features-section .section-heading p {
    font-size: 16px;
  }

  .feature-grid,
  .department-grid {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .department-grid {
    width: 100%;
  }

  .showcase-band .section-shell {
    width: min(100% - 32px, 680px);
    padding: 66px 0;
  }

  .showcase-band .section-heading {
    margin-bottom: 32px;
  }

  .department-card .card-copy {
    min-height: auto;
    padding: 20px 22px 24px;
  }

  .department-card .card-copy h3 {
    font-size: 18px;
  }

  .department-card .card-copy p {
    font-size: 14px;
  }

  .feature-card .card-image {
    aspect-ratio: 16 / 9;
  }

  .feature-card .card-copy {
    min-height: auto;
    padding: 24px 24px 26px;
  }

  .feature-card .card-icon {
    width: 48px;
    height: 48px;
    margin-bottom: 18px;
    font-size: 22px;
  }

  .feature-card .card-copy h3 {
    margin-bottom: 12px;
    font-size: 22px;
  }

  .feature-card .card-copy p {
    font-size: 16px;
  }

  .showcase-carousel :deep(.el-carousel__container) {
    height: 320px !important;
  }

  .footer-inner {
    width: min(100% - 32px, 680px);
    grid-template-columns: 1fr;
    gap: 30px;
    padding: 52px 0 42px;
  }

  .footer-bottom {
    width: min(100% - 32px, 680px);
    padding: 26px 0 34px;
  }

  .footer-social {
    text-align: left;
  }

  .social-links {
    justify-content: flex-start;
  }

  .footer-contact {
    justify-self: start;
  }

  .footer-brand {
    margin-bottom: 14px;
  }

  .footer-brand-mark {
    width: 38px;
    height: 38px;
  }

  .footer-brand-mark img {
    width: 100%;
    height: 100%;
  }
}

@media (max-width: 420px) {
  .features-section .section-heading h2 {
    font-size: 30px;
  }

  .hero-pill {
    white-space: normal;
    justify-content: center;
  }

  .hero-actions :deep(.el-button) {
    width: 100%;
  }

  .submit-actions :deep(.el-button) {
    width: 100%;
    min-width: 0;
  }

  .submit-topic small {
    white-space: normal;
  }
}

@media (prefers-reduced-motion: reduce) {
  .landing-page.is-ready .landing-nav,
  .landing-page.is-ready .hero-media,
  .landing-page.is-ready .hero-fallback,
  .landing-page.is-ready .hero-overlay,
  .landing-page.is-ready .hero-pill,
  .landing-page.is-ready .hero h1,
  .landing-page.is-ready .hero-content > p,
  .landing-page.is-ready .hero-actions,
  .landing-page.is-ready .scroll-cue,
  [data-reveal],
  [data-reveal].is-visible,
  .submit-card::before,
  .submit-card.is-visible .submit-step,
  .submit-card.is-visible .submit-topic,
  .submit-card.is-visible .submit-visual-badge {
    animation: none !important;
    transition: none !important;
    filter: none !important;
    opacity: 1 !important;
    transform: none !important;
  }

  .scroll-cue {
    animation: none;
  }

  .image-card,
  .card-image img,
  .scroll-cue,
  nav a,
  .social-links a,
  .social-tooltip {
    transition: none;
  }

  .image-card:hover,
  .image-card:focus-within,
  .image-card:hover .card-image img {
    transform: none;
  }

  .hero video {
    display: none;
  }

  .showcase-slide img,
  .submit-visual img,
  .submit-visual video {
    transform: none !important;
  }
}
</style>
