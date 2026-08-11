<template>
  <div class="maintenance-page">
    <div class="grain-layer" aria-hidden="true"></div>
    <div class="light light-a" aria-hidden="true"></div>
    <div class="light light-b" aria-hidden="true"></div>

    <router-link to="/" class="public-link">
      <el-icon><ArrowLeft /></el-icon>
      <span>浏览公开站点</span>
    </router-link>

    <section class="maintenance-shell" aria-label="维护访问">
      <aside class="visual-panel" aria-hidden="true">
        <div class="lens-mark">
          <el-icon><Camera /></el-icon>
        </div>
        <div class="photo-frame">
          <div class="frame-top">
            <span>STUDIO</span>
            <span>PRIVATE</span>
          </div>
          <span class="track-glow"></span>
          <div class="focus-ring">
            <span class="focus-dot"></span>
          </div>
          <div class="scan-line"></div>
        </div>
        <div class="panel-status">
          <span class="pulse-dot"></span>
          <span>公开作品集保持在线</span>
        </div>
      </aside>

      <main class="access-card">
        <div class="status-header">
          <div class="status-icon"><el-icon><Tools /></el-icon></div>
          <div>
            <span>MAINTENANCE ACCESS</span>
            <strong>临时维护入口</strong>
          </div>
        </div>
        <h1>{{ status.title || '系统维护中' }}</h1>
        <p>{{ status.message || '管理平台正在维护，公开站点仍可正常浏览。' }}</p>
        <div class="quick-notes">
          <div>
            <el-icon><CircleCheck /></el-icon>
            <span>公开站点可访问</span>
          </div>
          <div>
            <el-icon><Timer /></el-icon>
            <span>维护完成后恢复登录</span>
          </div>
        </div>
        <el-alert class="maintenance-alert" title="维护密码仅提供临时通行，进入后仍需使用个人账号登录。" type="info" :closable="false" show-icon />
        <el-form class="unlock-form" @submit.prevent="unlock">
          <el-input v-model="password" type="password" show-password size="large" placeholder="输入维护密码" @keyup.enter="unlock">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
          <el-button type="primary" size="large" :loading="loading" :disabled="password.length < 8" @click="unlock">验证并继续</el-button>
        </el-form>
      </main>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Camera, CircleCheck, Lock, Timer, Tools } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const status = reactive({ enabled: true, title: '', message: '', unlocked: false })
const password = ref('')
const loading = ref(false)
const target = () => typeof route.query.redirect === 'string' ? route.query.redirect : '/login'
const unlock = async () => {
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
  const response = await request.get('/maintenance/public/status', { silent: true })
  Object.assign(status, response.data || {})
  if (!status.enabled || status.unlocked) router.replace(target())
})
</script>

<style scoped>
.maintenance-page {
  position: relative;
  z-index: 2;
  min-height: 100vh;
  display: grid;
  place-items: center;
  overflow: hidden;
  padding: 88px 24px 40px;
  color: #112533;
  background:
    radial-gradient(circle at 14% 16%, rgba(100, 200, 232, .28), transparent 30%),
    radial-gradient(circle at 88% 78%, rgba(230, 204, 155, .26), transparent 27%),
    linear-gradient(135deg, #071923 0%, #0e3340 48%, #172119 100%);
}

.grain-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: .32;
  background-image:
    linear-gradient(rgba(255, 255, 255, .045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, .035) 1px, transparent 1px);
  background-size: 58px 58px;
  mask-image: radial-gradient(circle at center, black 0%, transparent 76%);
}

.light {
  position: absolute;
  width: 380px;
  height: 380px;
  border-radius: 50%;
  filter: blur(18px);
  opacity: .58;
  pointer-events: none;
}

.light-a {
  top: 11%;
  left: 8%;
  background: radial-gradient(circle, rgba(80, 183, 213, .36), transparent 66%);
}

.light-b {
  right: 9%;
  bottom: 8%;
  background: radial-gradient(circle, rgba(255, 215, 150, .28), transparent 68%);
}

.public-link {
  position: fixed;
  top: 26px;
  left: 30px;
  z-index: 3;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 38px;
  padding: 0 14px;
  color: rgba(232, 247, 249, .88);
  text-decoration: none;
  border: 1px solid rgba(255, 255, 255, .16);
  border-radius: 999px;
  background: rgba(255, 255, 255, .08);
  backdrop-filter: blur(16px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, .16);
  transition: color .2s ease, border-color .2s ease, background .2s ease;
}

.public-link:hover,
.public-link:focus-visible {
  color: #ffffff;
  border-color: rgba(169, 226, 240, .55);
  background: rgba(255, 255, 255, .13);
}

.maintenance-shell {
  position: relative;
  z-index: 1;
  width: min(960px, 100%);
  display: grid;
  grid-template-columns: minmax(280px, .86fr) minmax(400px, 1fr);
  border: 1px solid rgba(218, 237, 239, .2);
  border-radius: 8px;
  overflow: hidden;
  background: rgba(246, 251, 250, .94);
  box-shadow: 0 30px 90px rgba(0, 0, 0, .34);
}

.visual-panel {
  position: relative;
  min-height: 520px;
  padding: 36px;
  color: #eefafa;
  background:
    linear-gradient(180deg, rgba(7, 25, 35, .08), rgba(7, 25, 35, .72)),
    radial-gradient(circle at 40% 36%, rgba(119, 209, 226, .34), transparent 32%),
    linear-gradient(145deg, #102b34 0%, #183d40 52%, #202315 100%);
}

.visual-panel::before {
  position: absolute;
  inset: 24px;
  content: "";
  border: 1px solid rgba(255, 255, 255, .16);
  border-radius: 6px;
}

.lens-mark {
  position: relative;
  z-index: 1;
  width: 56px;
  height: 56px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, .24);
  border-radius: 50%;
  color: #bff4ff;
  background: rgba(255, 255, 255, .1);
  box-shadow: inset 0 0 28px rgba(255, 255, 255, .08);
  font-size: 25px;
}

.photo-frame {
  position: absolute;
  left: 36px;
  right: 36px;
  bottom: 96px;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, .18);
  background:
    linear-gradient(rgba(255, 255, 255, .11) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, .11) 1px, transparent 1px),
    linear-gradient(135deg, rgba(23, 79, 91, .45), rgba(7, 18, 26, .88));
  background-size: 76px 76px, 76px 76px, auto;
  box-shadow: inset 0 0 70px rgba(0, 0, 0, .34);
}

.frame-top {
  position: absolute;
  top: 18px;
  left: 20px;
  right: 20px;
  display: flex;
  justify-content: space-between;
  color: rgba(239, 250, 250, .72);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .14em;
  z-index: 3;
}

.track-glow {
  position: absolute;
  left: 50%;
  top: 50%;
  z-index: 1;
  width: 92px;
  height: 92px;
  border-radius: 50%;
  pointer-events: none;
  opacity: .88;
  transform: translate(-50%, -50%) translate(-58px, -24px) scale(.92);
  background:
    radial-gradient(circle at 50% 50%, rgba(255, 246, 219, .96) 0 8%, rgba(248, 220, 168, .58) 14%, rgba(248, 220, 168, .18) 36%, transparent 67%);
  filter: blur(1.5px);
  mix-blend-mode: screen;
}

.focus-ring {
  position: absolute;
  inset: 86px 76px;
  z-index: 2;
  border: 1px solid rgba(255, 255, 255, .58);
  border-radius: 50%;
  box-shadow: 0 0 0 18px rgba(255, 255, 255, .03), 0 0 55px rgba(157, 235, 246, .28);
}

.focus-ring::before {
  position: absolute;
  left: 50%;
  top: 50%;
  content: "";
  transform: translate(-50%, -50%);
  border-radius: 50%;
}

.focus-ring::before {
  width: 46%;
  height: 46%;
  border: 1px solid rgba(255, 255, 255, .34);
}

.focus-dot {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 8px;
  height: 8px;
  transform: translate(-50%, -50%);
  background: #f8dca8;
  border-radius: 50%;
  box-shadow: 0 0 22px rgba(248, 220, 168, .9);
}

.focus-dot::before {
  position: absolute;
  inset: -8px;
  content: "";
  border-radius: 50%;
  background: radial-gradient(circle, rgba(248, 220, 168, .34), transparent 66%);
}

.scan-line {
  position: absolute;
  left: 0;
  right: 0;
  top: 48%;
  z-index: 3;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(173, 240, 248, .8), transparent);
}

.panel-status {
  position: absolute;
  left: 36px;
  right: 36px;
  bottom: 36px;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 40px;
  padding: 0 14px;
  border-radius: 999px;
  color: rgba(239, 250, 250, .86);
  background: rgba(7, 20, 29, .38);
  border: 1px solid rgba(255, 255, 255, .14);
  backdrop-filter: blur(14px);
}

.pulse-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #8df0bf;
  box-shadow: 0 0 0 7px rgba(141, 240, 191, .14), 0 0 22px rgba(141, 240, 191, .68);
}

.access-card {
  padding: 52px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, .94), rgba(245, 250, 249, .94)),
    radial-gradient(circle at 88% 12%, rgba(42, 147, 181, .12), transparent 34%);
}

.status-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
}

.status-icon {
  width: 58px;
  height: 58px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  background: linear-gradient(145deg, #e5f8fa, #f5fbfb);
  color: #0a7b99;
  border: 1px solid #d5eef3;
  border-radius: 8px;
  font-size: 27px;
  box-shadow: 0 14px 34px rgba(15, 125, 153, .13);
}

.status-header span {
  display: block;
  color: #0a7b99;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: .12em;
}

.status-header strong {
  display: block;
  margin-top: 5px;
  color: #263f49;
  font-size: 15px;
  font-weight: 700;
}

h1 {
  margin: 0 0 14px;
  color: #102936;
  font-size: clamp(34px, 5vw, 50px);
  line-height: 1.08;
  letter-spacing: 0;
}

.access-card > p {
  max-width: 460px;
  margin: 0 0 24px;
  color: #526c77;
  font-size: 16px;
  line-height: 1.8;
}

.quick-notes {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 24px;
}

.quick-notes div {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 42px;
  padding: 0 12px;
  color: #31505b;
  border: 1px solid #dcecef;
  border-radius: 8px;
  background: #f7fbfb;
  font-size: 13px;
  font-weight: 600;
}

.quick-notes .el-icon {
  color: #0a8e84;
  font-size: 16px;
}

.maintenance-alert {
  border: 1px solid #dcecf2;
  border-radius: 8px;
  background: #f3f8fa;
}

.unlock-form {
  margin-top: 24px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 138px;
  gap: 12px;
}

.unlock-form :deep(.el-input__wrapper) {
  min-height: 50px;
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d9e8ee inset;
  transition: box-shadow .2s ease, background .2s ease;
}

.unlock-form :deep(.el-input__wrapper:hover),
.unlock-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #8bd5e7 inset, 0 10px 28px rgba(12, 107, 138, .1);
}

.unlock-form :deep(.el-button) {
  min-height: 50px;
  border: 0;
  border-radius: 8px;
  font-weight: 700;
  background: linear-gradient(135deg, #0a88a8, #0f6f8c);
  box-shadow: 0 14px 28px rgba(12, 107, 138, .22);
}

.unlock-form :deep(.el-button:not(.is-disabled):hover),
.unlock-form :deep(.el-button:not(.is-disabled):focus-visible) {
  background: linear-gradient(135deg, #0b95b9, #107c9e);
}

.unlock-form :deep(.el-button.is-disabled) {
  color: rgba(255, 255, 255, .76);
  background: #93bfcc;
  box-shadow: none;
}

@media (prefers-reduced-motion: no-preference) {
  .scan-line {
    animation: scanMove 3.8s ease-in-out infinite;
  }

  .pulse-dot {
    animation: pulseGlow 2.2s ease-in-out infinite;
  }

  .track-glow {
    animation: trackGlowOrbit 6.4s cubic-bezier(.42, 0, .24, 1) infinite;
  }
}

@keyframes scanMove {
  0%, 100% {
    transform: translateY(-78px);
    opacity: .24;
  }

  50% {
    transform: translateY(78px);
    opacity: .88;
  }
}

@keyframes pulseGlow {
  0%, 100% {
    box-shadow: 0 0 0 7px rgba(141, 240, 191, .12), 0 0 20px rgba(141, 240, 191, .54);
  }

  50% {
    box-shadow: 0 0 0 11px rgba(141, 240, 191, .2), 0 0 32px rgba(141, 240, 191, .88);
  }
}

@keyframes trackGlowOrbit {
  0%, 100% {
    opacity: .72;
    transform: translate(-50%, -50%) translate(-76px, -34px) scale(.82);
  }

  20% {
    opacity: .95;
    transform: translate(-50%, -50%) translate(-18px, -55px) scale(1.02);
  }

  42% {
    opacity: .8;
    transform: translate(-50%, -50%) translate(72px, -12px) scale(.9);
  }

  64% {
    opacity: 1;
    transform: translate(-50%, -50%) translate(18px, 38px) scale(1.1);
  }

  84% {
    opacity: .86;
    transform: translate(-50%, -50%) translate(-64px, 16px) scale(.96);
  }
}

@media (max-width: 860px) {
  .maintenance-page {
    padding: 82px 18px 28px;
    overflow-y: auto;
  }

  .maintenance-shell {
    grid-template-columns: 1fr;
  }

  .visual-panel {
    min-height: 250px;
    padding: 26px;
  }

  .photo-frame {
    left: 26px;
    right: 26px;
    bottom: 78px;
    height: 145px;
  }

  .focus-ring {
    inset: 36px 36%;
  }

  .panel-status {
    left: 26px;
    right: 26px;
    bottom: 24px;
  }

  .access-card {
    padding: 34px;
  }
}

@media (max-width: 560px) {
  .maintenance-page {
    padding: 76px 14px 22px;
  }

  .public-link {
    top: 18px;
    left: 18px;
    min-height: 36px;
    padding: 0 12px;
  }

  .maintenance-shell {
    border-radius: 8px;
  }

  .visual-panel {
    display: none;
  }

  .access-card {
    padding: 28px 20px;
  }

  .status-header {
    margin-bottom: 22px;
  }

  .quick-notes,
  .unlock-form {
    grid-template-columns: 1fr;
  }

  .unlock-form :deep(.el-button) {
    width: 100%;
  }
}
</style>
