<template>
  <div :class="['maintenance-page', { 'is-ready': pageReady }]">
    <header class="maintenance-header">
      <router-link to="/" class="maintenance-brand" aria-label="返回公开站点">
        <span class="brand-mark">
          <img v-if="siteLogo" :src="siteLogo" alt="" />
          <el-icon v-else><Camera /></el-icon>
        </span>
        <span class="brand-copy">
          <strong>{{ brandTitle }}</strong>
          <small>{{ brandSubtitle }}</small>
        </span>
      </router-link>

      <div class="header-status" aria-label="管理平台当前状态">
        <span class="status-light"></span>
        <span>管理平台维护中</span>
      </div>

      <router-link to="/" class="public-link">
        <el-icon><View /></el-icon>
        <span>浏览公开站点</span>
        <el-icon class="public-link-arrow"><Right /></el-icon>
      </router-link>
    </header>

    <main class="maintenance-main">
      <section class="maintenance-content" aria-labelledby="maintenance-title">
        <div class="service-reference" aria-hidden="true">
          <span>SERVICE NOTICE</span>
          <strong>503</strong>
        </div>

        <div class="notice-heading">
          <span class="notice-label">管理平台维护窗口</span>
          <h1 id="maintenance-title">{{ status.title || '系统维护中' }}</h1>
          <p class="maintenance-message">
            {{ status.message || '管理平台正在维护，公开站点仍可正常浏览。' }}
          </p>
        </div>

        <div class="service-board" aria-label="服务状态">
          <div class="service-row is-online">
            <span class="service-icon"><el-icon><CircleCheck /></el-icon></span>
            <span class="service-copy">
              <strong>公开站点</strong>
              <small>作品展示与公开内容可正常访问</small>
            </span>
            <span class="service-state">运行正常</span>
          </div>
          <div class="service-row is-paused">
            <span class="service-icon"><el-icon><Tools /></el-icon></span>
            <span class="service-copy">
              <strong>管理平台</strong>
              <small>登录与管理功能暂时停止服务</small>
            </span>
            <span class="service-state">维护中</span>
          </div>
        </div>

        <div class="maintenance-progress" aria-label="维护进度">
          <div class="progress-track" aria-hidden="true"><span></span></div>
          <div class="progress-labels">
            <span class="is-complete"><i></i>维护已开始</span>
            <span class="is-current"><i></i>服务检查中</span>
            <span><i></i>恢复访问</span>
          </div>
        </div>
      </section>

      <aside class="access-panel" aria-labelledby="access-title">
        <div class="access-panel-head">
          <span class="access-icon"><el-icon><Lock /></el-icon></span>
          <span class="access-number">ACCESS / 01</span>
        </div>

        <div class="access-copy">
          <p>临时通行</p>
          <h2 id="access-title">维护人员入口</h2>
          <span>拥有维护密码的人员可临时进入，随后仍需使用个人账号登录。</span>
        </div>

        <form class="unlock-form" @submit.prevent="unlock">
          <label for="maintenance-password">维护密码</label>
          <div class="unlock-control">
            <el-input
              id="maintenance-password"
              v-model="password"
              type="password"
              show-password
              autocomplete="current-password"
              placeholder="至少输入 8 位密码"
              aria-describedby="maintenance-password-hint"
            >
              <template #prefix><el-icon><Key /></el-icon></template>
            </el-input>
            <el-button
              type="primary"
              native-type="submit"
              :loading="loading"
              :disabled="password.length < 8"
            >
              验证并继续
              <el-icon><Right /></el-icon>
            </el-button>
          </div>
          <p id="maintenance-password-hint" class="unlock-hint">
            维护密码不会替代个人账号验证。
          </p>
        </form>

        <div class="access-foot">
          <el-icon><InfoFilled /></el-icon>
          <span>无维护权限时，请等待服务恢复后再登录。</span>
        </div>
      </aside>
    </main>

    <footer class="maintenance-footer">
      <span>STATUS / TEMPORARILY UNAVAILABLE</span>
      <span>公开内容保持在线</span>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Camera,
  CircleCheck,
  InfoFilled,
  Key,
  Lock,
  Right,
  Tools,
  View
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getSiteImageUrl } from '@/utils/imageUrl'

const route = useRoute()
const router = useRouter()
const status = reactive({ enabled: true, title: '', message: '', unlocked: false })
const landingSettings = reactive({})
const password = ref('')
const loading = ref(false)
const pageReady = ref(false)

const setting = (key, fallback = '') => landingSettings[key] || fallback
const siteTitle = computed(() => setting('site.title', '融媒体中心'))
const brandTitle = computed(() => {
  const configuredTitle = setting('landing.brand.title')
  if (configuredTitle) return configuredTitle
  return /融媒体.*管理系统/.test(siteTitle.value) ? '融媒体中心' : siteTitle.value
})
const brandSubtitle = computed(() => setting('site.subtitle', 'Campus media center'))
const siteLogo = computed(() => getSiteImageUrl(setting('site.logo')))

const target = () => typeof route.query.redirect === 'string' ? route.query.redirect : '/login'

const unlock = async () => {
  if (loading.value || password.value.length < 8) return
  loading.value = true
  try {
    await request.post('/maintenance/public/unlock', { password: password.value }, { skipAuthRefresh: true })
    ElMessage.success('维护通行验证成功')
    router.replace(target())
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const [statusResult, landingResult] = await Promise.allSettled([
    request.get('/maintenance/public/status', { silent: true }),
    request.get('/landing/public', { silent: true })
  ])

  if (landingResult.status === 'fulfilled') {
    Object.assign(landingSettings, landingResult.value.data?.settings || {})
  }

  if (statusResult.status === 'fulfilled') {
    Object.assign(status, statusResult.value.data || {})
    if (!status.enabled || status.unlocked) {
      router.replace(target())
      return
    }
  }

  pageReady.value = true
})
</script>

<style scoped>
.maintenance-page {
  --page-bg: #edf0ed;
  --page-surface: #f8faf7;
  --page-ink: #15201d;
  --page-muted: #66716d;
  --page-line: #c9d0cc;
  --page-dark: #17221f;
  --page-green: #28725c;
  --page-green-soft: #dceae4;
  --page-amber: #d7922c;
  --page-amber-soft: #f3e5cb;
  --page-body: "Noto Sans CJK SC", "Source Han Sans SC", "Microsoft YaHei", sans-serif;
  --page-display: "Arial Black", "PingFang SC", "Microsoft YaHei", sans-serif;
  --page-mono: "Cascadia Mono", "SFMono-Regular", Consolas, monospace;
  position: relative;
  z-index: 2;
  min-height: 100svh;
  overflow-x: hidden;
  color: var(--page-ink);
  background:
    linear-gradient(rgba(21, 32, 29, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(21, 32, 29, 0.035) 1px, transparent 1px),
    var(--page-bg);
  background-size: 48px 48px;
  font-family: var(--page-body);
  -webkit-font-smoothing: antialiased;
}

.maintenance-page *,
.maintenance-page *::before,
.maintenance-page *::after {
  box-sizing: border-box;
}

.maintenance-page :is(h1, h2, p) {
  margin: 0;
}

.maintenance-page::before {
  position: fixed;
  z-index: 0;
  top: 84px;
  bottom: 54px;
  left: 0;
  width: min(24vw, 360px);
  content: "";
  pointer-events: none;
  background: linear-gradient(90deg, transparent, rgba(215, 146, 44, 0.055), transparent);
  transform: translateX(-120%);
  animation: background-diagnostic-sweep 13s ease-in-out infinite;
  will-change: transform;
}

.maintenance-header {
  position: relative;
  z-index: 2;
  min-height: 84px;
  padding: 16px clamp(24px, 4vw, 64px);
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 24px;
  background: rgba(248, 250, 247, 0.92);
  border-top: 4px solid var(--page-amber);
  border-bottom: 1px solid var(--page-line);
}

.maintenance-brand {
  min-width: 0;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  justify-self: start;
  color: inherit;
  text-decoration: none;
}

.brand-mark {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  overflow: hidden;
  color: #ffffff;
  background: var(--page-dark);
  border-radius: 50%;
}

.brand-mark img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  outline: 1px solid oklch(0 0 0 / 0.1);
  outline-offset: -1px;
}

.brand-mark .el-icon {
  font-size: 21px;
}

.brand-copy {
  min-width: 0;
  display: grid;
  gap: 3px;
}

.brand-copy strong {
  overflow: hidden;
  font-family: var(--page-display);
  font-size: 16px;
  line-height: 1.1;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.brand-copy small,
.service-reference,
.access-number,
.maintenance-footer {
  font-family: var(--page-mono);
}

.brand-copy small {
  overflow: hidden;
  color: var(--page-muted);
  font-size: 9px;
  line-height: 1;
  text-overflow: ellipsis;
  text-transform: uppercase;
  white-space: nowrap;
}

.header-status {
  min-height: 34px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  gap: 9px;
  color: #86591b;
  background: var(--page-amber-soft);
  border: 1px solid #dfc18d;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
}

.status-light {
  position: relative;
  width: 8px;
  height: 8px;
  flex: 0 0 auto;
  background: var(--page-amber);
  border-radius: 50%;
  box-shadow: 0 0 0 4px rgba(215, 146, 44, 0.16);
}

.status-light::after {
  position: absolute;
  inset: -6px;
  content: "";
  border: 1px solid rgba(215, 146, 44, 0.58);
  border-radius: 50%;
  animation: status-signal 2.4s ease-out infinite;
}

.public-link {
  min-height: 42px;
  padding: 0 15px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  justify-self: end;
  color: var(--page-ink);
  background: transparent;
  border: 1px solid #9fa9a4;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 700;
  text-decoration: none;
  transition: color 150ms ease-out, background-color 150ms ease-out, border-color 150ms ease-out, transform 150ms ease-out;
}

.public-link .el-icon {
  font-size: 16px;
}

.public-link-arrow {
  color: var(--page-amber);
  transition: transform 150ms ease-out;
}

.public-link:hover,
.public-link:focus-visible {
  color: #ffffff;
  background: var(--page-dark);
  border-color: var(--page-dark);
  outline: none;
  transform: translateY(-1px);
}

.public-link:hover .public-link-arrow,
.public-link:focus-visible .public-link-arrow {
  transform: translateX(3px);
}

.public-link:active {
  transform: scale(0.96);
}

.maintenance-main {
  position: relative;
  z-index: 1;
  width: min(calc(100% - 64px), 1280px);
  min-height: calc(100svh - 138px);
  margin-inline: auto;
  padding: clamp(56px, 8vh, 96px) 0;
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(360px, 0.65fr);
  align-items: center;
  gap: clamp(56px, 8vw, 120px);
}

.maintenance-content {
  min-width: 0;
}

.service-reference,
.notice-heading,
.service-board,
.maintenance-progress,
.access-panel {
  opacity: 0;
  filter: blur(4px);
  transform: translateY(18px);
  transition: opacity 460ms ease-out, filter 460ms ease-out, transform 560ms cubic-bezier(0.16, 1, 0.3, 1);
}

.access-panel {
  transform: translateX(24px);
}

.is-ready :is(.service-reference, .notice-heading, .service-board, .maintenance-progress, .access-panel) {
  opacity: 1;
  filter: blur(0);
  transform: translate(0, 0);
}

.is-ready .notice-heading { transition-delay: 70ms; }
.is-ready .service-board { transition-delay: 140ms; }
.is-ready .maintenance-progress { transition-delay: 210ms; }
.is-ready .access-panel { transition-delay: 120ms; }

.service-reference {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--page-line);
  color: var(--page-muted);
  font-size: 10px;
  font-weight: 700;
}

.service-reference strong {
  color: #aab2ae;
  font-size: 42px;
  line-height: 0.78;
  font-variant-numeric: tabular-nums;
  animation: service-code-active 3.6s ease-in-out infinite;
}

.notice-heading {
  padding: 34px 0 38px;
}

.notice-label {
  display: inline-block;
  margin-bottom: 15px;
  color: var(--page-green);
  font-size: 12px;
  font-weight: 800;
}

.notice-heading h1 {
  max-width: 720px;
  font-family: var(--page-display);
  font-size: clamp(48px, 6vw, 78px);
  font-weight: 900;
  line-height: 1.03;
  letter-spacing: 0;
  overflow-wrap: anywhere;
  text-wrap: balance;
}

.maintenance-message {
  max-width: 680px;
  margin-top: 20px !important;
  color: var(--page-muted);
  font-size: 16px;
  line-height: 1.85;
  text-wrap: pretty;
}

.service-board {
  border-top: 1px solid var(--page-line);
}

.service-row {
  min-height: 76px;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  border-bottom: 1px solid var(--page-line);
}

.service-icon {
  position: relative;
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  font-size: 17px;
}

.is-paused .service-icon .el-icon {
  animation: maintenance-gear 7s linear infinite;
}

.is-online .service-icon {
  color: var(--page-green);
  background: var(--page-green-soft);
}

.is-paused .service-icon {
  color: #8b5e1d;
  background: var(--page-amber-soft);
}

.service-copy {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.service-copy strong {
  font-size: 14px;
}

.service-copy small {
  overflow: hidden;
  color: var(--page-muted);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.service-state {
  padding: 5px 8px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 800;
}

.is-online .service-state {
  color: var(--page-green);
  background: var(--page-green-soft);
}

.is-paused .service-state {
  color: #8b5e1d;
  background: var(--page-amber-soft);
}

.maintenance-progress {
  margin-top: 34px;
}

.progress-track {
  height: 3px;
  overflow: hidden;
  background: #d5dad7;
}

.progress-track span {
  position: relative;
  display: block;
  width: 58%;
  height: 100%;
  background: var(--page-amber);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 900ms cubic-bezier(0.16, 1, 0.3, 1) 260ms;
}

.progress-track span::after {
  position: absolute;
  inset: 0 auto 0 0;
  width: 34%;
  content: "";
  background: rgba(255, 255, 255, 0.76);
  transform: translateX(-120%);
  animation: progress-sweep 2.8s ease-in-out infinite;
  will-change: transform;
}

.is-ready .progress-track span {
  transform: scaleX(1);
}

.progress-labels {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  color: #909895;
  font-size: 11px;
  font-weight: 700;
}

.progress-labels span {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.progress-labels span:nth-child(2) {
  justify-content: center;
}

.progress-labels span:last-child {
  justify-content: flex-end;
}

.progress-labels i {
  width: 6px;
  height: 6px;
  background: currentColor;
  border-radius: 50%;
}

.progress-labels .is-complete {
  color: var(--page-green);
}

.progress-labels .is-current {
  color: #9a671d;
}

.access-panel {
  position: relative;
  min-width: 0;
  padding: 32px;
  overflow: hidden;
  isolation: isolate;
  color: #ffffff;
  background: var(--page-dark);
  border-radius: 6px;
  box-shadow: 0 24px 70px rgba(21, 32, 29, 0.2);
  transition-property: opacity, filter, transform, box-shadow;
}

.access-panel::before {
  position: absolute;
  z-index: -1;
  top: -34%;
  right: 0;
  left: 0;
  height: 26%;
  content: "";
  pointer-events: none;
  background: linear-gradient(180deg, transparent, rgba(215, 146, 44, 0.075), transparent);
  transform: translateY(0);
  animation: panel-diagnostic-scan 5.6s ease-in-out infinite;
  will-change: transform;
}

.access-panel:focus-within {
  box-shadow: 0 28px 80px rgba(21, 32, 29, 0.25), 0 0 0 1px rgba(215, 146, 44, 0.34);
  transition-delay: 0ms;
  transition-duration: 150ms;
}

.access-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 26px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.14);
}

.access-icon {
  position: relative;
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  color: #191d1b;
  background: var(--page-amber);
  border-radius: 50%;
  font-size: 19px;
}

.access-icon::after {
  position: absolute;
  inset: -1px;
  content: "";
  border: 1px solid rgba(215, 146, 44, 0.7);
  border-radius: 50%;
  animation: access-signal 2.8s ease-out infinite;
}

.access-number {
  color: rgba(255, 255, 255, 0.42);
  font-size: 9px;
  font-weight: 700;
}

.access-copy {
  padding-top: 28px;
}

.access-copy > p {
  color: var(--page-amber);
  font-size: 11px;
  font-weight: 800;
}

.access-copy h2 {
  margin-top: 8px !important;
  font-family: var(--page-display);
  font-size: 28px;
  line-height: 1.15;
  text-wrap: balance;
}

.access-copy > span {
  display: block;
  margin-top: 13px;
  color: rgba(255, 255, 255, 0.62);
  font-size: 13px;
  line-height: 1.75;
  text-wrap: pretty;
}

.unlock-form {
  margin-top: 30px;
}

.unlock-form > label {
  display: block;
  margin-bottom: 9px;
  color: rgba(255, 255, 255, 0.88);
  font-size: 12px;
  font-weight: 700;
}

.unlock-control {
  display: grid;
  gap: 10px;
}

#app .maintenance-page .unlock-control :deep(.el-input__wrapper) {
  min-height: 50px;
  padding-inline: 15px;
  background: rgba(255, 255, 255, 0.08) !important;
  border-radius: 3px !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.26) inset !important;
  transition: background-color 150ms ease-out, box-shadow 150ms ease-out;
}

#app .maintenance-page .unlock-control :deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.11) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.48) inset !important;
}

#app .maintenance-page .unlock-control :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.12) !important;
  box-shadow: 0 0 0 2px var(--page-amber) inset !important;
}

#app .maintenance-page .unlock-control :deep(.el-input__inner),
#app .maintenance-page .unlock-control :deep(.el-input__prefix),
#app .maintenance-page .unlock-control :deep(.el-input__suffix) {
  color: #ffffff !important;
}

#app .maintenance-page .unlock-control :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.4) !important;
}

#app .maintenance-page .unlock-control :deep(.el-button) {
  min-height: 50px;
  margin: 0;
  gap: 8px;
  color: #17221f !important;
  background: var(--page-amber) !important;
  border-color: var(--page-amber) !important;
  border-radius: 3px !important;
  box-shadow: none !important;
  font-weight: 800;
  transition: background-color 150ms ease-out, border-color 150ms ease-out, transform 150ms ease-out, box-shadow 150ms ease-out;
}

#app .maintenance-page .unlock-control :deep(.el-button .el-icon) {
  transition: transform 150ms ease-out;
}

#app .maintenance-page .unlock-control :deep(.el-button:not(.is-disabled):hover),
#app .maintenance-page .unlock-control :deep(.el-button:not(.is-disabled):focus-visible) {
  background: #e5a845 !important;
  border-color: #e5a845 !important;
  box-shadow: 0 12px 30px rgba(215, 146, 44, 0.22) !important;
  transform: translateY(-1px);
}

#app .maintenance-page .unlock-control :deep(.el-button:not(.is-disabled):hover .el-icon),
#app .maintenance-page .unlock-control :deep(.el-button:not(.is-disabled):focus-visible .el-icon) {
  transform: translateX(3px);
}

#app .maintenance-page .unlock-control :deep(.el-button:not(.is-disabled):active) {
  transform: scale(0.96);
}

#app .maintenance-page .unlock-control :deep(.el-button.is-disabled) {
  color: rgba(255, 255, 255, 0.42) !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.14) !important;
}

.unlock-hint {
  margin-top: 9px !important;
  color: rgba(255, 255, 255, 0.4);
  font-size: 11px;
  line-height: 1.6;
}

.access-foot {
  margin-top: 30px;
  padding-top: 18px;
  display: flex;
  align-items: flex-start;
  gap: 9px;
  color: rgba(255, 255, 255, 0.5);
  border-top: 1px solid rgba(255, 255, 255, 0.14);
  font-size: 11px;
  line-height: 1.6;
}

.access-foot .el-icon {
  margin-top: 2px;
  flex: 0 0 auto;
  color: var(--page-amber);
  font-size: 14px;
}

.maintenance-footer {
  position: relative;
  z-index: 2;
  min-height: 54px;
  padding: 0 clamp(24px, 4vw, 64px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  color: var(--page-muted);
  background: var(--page-surface);
  border-top: 1px solid var(--page-line);
  font-size: 9px;
  font-weight: 700;
}

.maintenance-footer span:last-child {
  color: var(--page-green);
}

@keyframes background-diagnostic-sweep {
  0%, 14% { transform: translateX(-120%); }
  62%, 100% { transform: translateX(calc(100vw + 120%)); }
}

@keyframes status-signal {
  0% { opacity: 0.72; transform: scale(0.72); }
  62%, 100% { opacity: 0; transform: scale(1.72); }
}

@keyframes service-code-active {
  0%, 100% { opacity: 0.72; transform: translateY(0); }
  50% { opacity: 1; transform: translateY(-2px); }
}

@keyframes maintenance-gear {
  to { transform: rotate(360deg); }
}

@keyframes progress-sweep {
  0%, 18% { transform: translateX(-120%); }
  72%, 100% { transform: translateX(320%); }
}

@keyframes panel-diagnostic-scan {
  0%, 18% { opacity: 0; transform: translateY(0); }
  28% { opacity: 1; }
  66% { opacity: 0.7; }
  78%, 100% { opacity: 0; transform: translateY(520%); }
}

@keyframes access-signal {
  0%, 24% { opacity: 0.68; transform: scale(0.82); }
  72%, 100% { opacity: 0; transform: scale(1.46); }
}

@media (max-width: 980px) {
  .maintenance-header {
    grid-template-columns: 1fr auto;
  }

  .header-status {
    display: none;
  }

  .maintenance-main {
    width: min(calc(100% - 48px), 760px);
    grid-template-columns: 1fr;
    gap: 48px;
    padding: 56px 0 64px;
  }

  .access-panel {
    width: min(100%, 560px);
  }
}

@media (max-width: 620px) {
  .maintenance-header {
    min-height: 72px;
    padding: 12px 18px;
  }

  .brand-mark {
    width: 40px;
    height: 40px;
  }

  .brand-copy strong {
    max-width: 150px;
    font-size: 14px;
  }

  .public-link {
    width: 44px;
    min-height: 44px;
    padding: 0;
  }

  .public-link span,
  .public-link-arrow {
    position: absolute;
    width: 1px;
    height: 1px;
    overflow: hidden;
    clip-path: inset(50%);
  }

  .maintenance-main {
    width: calc(100% - 36px);
    min-height: auto;
    padding: 40px 0 52px;
    gap: 38px;
  }

  .service-reference strong {
    font-size: 34px;
  }

  .notice-heading {
    padding: 28px 0 30px;
  }

  .notice-heading h1 {
    font-size: clamp(38px, 12vw, 54px);
  }

  .maintenance-message {
    font-size: 14px;
    line-height: 1.75;
  }

  .service-row {
    min-height: 84px;
    grid-template-columns: 38px minmax(0, 1fr) auto;
    gap: 10px;
  }

  .service-copy small {
    white-space: normal;
  }

  .service-state {
    padding-inline: 6px;
    font-size: 10px;
  }

  .progress-labels {
    font-size: 9px;
  }

  .progress-labels i {
    display: none;
  }

  .access-panel {
    padding: 26px 22px;
  }

  .maintenance-footer {
    min-height: 48px;
    padding-inline: 18px;
  }

  .maintenance-footer span:first-child {
    display: none;
  }
}

@media (max-width: 380px) {
  .brand-copy small {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .maintenance-page::before,
  .status-light::after,
  .service-reference strong,
  .is-paused .service-icon .el-icon,
  .progress-track span::after,
  .access-panel::before,
  .access-icon::after {
    animation: none !important;
  }

  .maintenance-page::before,
  .status-light::after,
  .access-panel::before,
  .access-icon::after,
  .progress-track span::after {
    display: none;
  }

  .service-reference,
  .notice-heading,
  .service-board,
  .maintenance-progress,
  .access-panel,
  .progress-track span,
  .public-link,
  .public-link-arrow,
  .unlock-control :deep(.el-button) {
    transition: none !important;
  }
}
</style>
