<template>
  <div class="recovery-page">
    <header class="recovery-nav">
      <router-link to="/" class="recovery-home-link" aria-label="返回公开站点">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回公开站点</span>
      </router-link>

      <router-link to="/" class="recovery-brand" aria-label="返回融媒体中心首页">
        <span class="recovery-brand-mark">
          <img v-if="siteConfig.siteLogo" :src="siteConfig.siteLogo" :alt="siteConfig.siteTitle || '网站LOGO'" />
          <el-icon v-else><VideoCamera /></el-icon>
        </span>
        <span>
          <strong>{{ siteConfig.siteTitle || '融媒体中心' }}</strong>
          <small>Credential recovery</small>
        </span>
      </router-link>

      <div class="recovery-nav-status" aria-label="凭证恢复服务正常">
        <i></i>
        <span>RECOVERY ONLINE</span>
      </div>
    </header>

    <main class="recovery-main">
      <div class="recovery-shell">
        <aside class="recovery-archive">
          <div class="recovery-kicker">
            <span>SECURITY ARCHIVE</span>
            <span>CASE / 03</span>
          </div>

          <div class="recovery-copy">
            <span>CREDENTIAL RECOVERY</span>
            <h1>找回密码</h1>
            <p>通过账号绑定的 QQ 邮箱验证身份，重置后所有已登录设备将退出。</p>
          </div>

          <div class="recovery-frame" aria-hidden="true">
            <div class="recovery-frame-meta">
              <span>REC</span>
              <span>ID / RESET</span>
            </div>
            <div class="recovery-lock-mark">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="recovery-focus"></div>
            <span class="recovery-timecode">CASE 00:00:03</span>
          </div>

          <ol class="recovery-sequence" aria-label="密码重置流程">
            <li>
              <span>01</span>
              <div><strong>核验邮箱</strong><small>确认账号归属</small></div>
            </li>
            <li>
              <span>02</span>
              <div><strong>验证身份</strong><small>校验六位验证码</small></div>
            </li>
            <li>
              <span>03</span>
              <div><strong>更新凭证</strong><small>注销旧设备会话</small></div>
            </li>
          </ol>

          <div class="recovery-archive-meta">
            <span><small>CHANNEL</small><strong>QQ 邮箱</strong></span>
            <span><small>POLICY</small><strong>全端退出</strong></span>
            <span><small>STATUS</small><strong><i></i>服务正常</strong></span>
          </div>
        </aside>

        <section class="recovery-workbench">
          <header class="recovery-heading">
            <div>
              <span>Security desk</span>
              <h2>重置访问凭证</h2>
            </div>
            <span class="recovery-index">03</span>
          </header>

          <div class="recovery-form-area">
            <div class="recovery-form-meta" aria-hidden="true">
              <span>RESET REQUEST</span>
              <span>DEVICE SIGN-OUT / ON</span>
            </div>

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
              class="recovery-form"
              @keyup.enter="submit"
            >
              <el-form-item prop="email">
                <template #label>
                  <span class="recovery-field-label"><b>01</b><span>QQ邮箱</span></span>
                </template>
                <el-input
                  v-model.trim="form.email"
                  type="email"
                  size="large"
                  placeholder="请输入账号绑定的 QQ 邮箱"
                  clearable
                >
                  <template #prefix><el-icon><Message /></el-icon></template>
                </el-input>
              </el-form-item>

              <el-form-item prop="code">
                <template #label>
                  <span class="recovery-field-label"><b>02</b><span>验证码</span></span>
                </template>
                <div class="recovery-code-row">
                  <el-input
                    v-model.trim="form.code"
                    maxlength="6"
                    size="large"
                    placeholder="请输入6位验证码"
                    clearable
                  >
                    <template #prefix><el-icon><Message /></el-icon></template>
                  </el-input>
                  <el-button
                    size="large"
                    class="recovery-code-button"
                    :disabled="countdown > 0"
                    :loading="sending"
                    @click="sendCode"
                  >
                    <el-icon><Message /></el-icon>
                    <span>{{ countdown ? `${countdown}s` : '发送验证码' }}</span>
                  </el-button>
                </div>
              </el-form-item>

              <div class="recovery-password-grid">
                <el-form-item prop="newPassword">
                  <template #label>
                    <span class="recovery-field-label"><b>03</b><span>新密码</span></span>
                  </template>
                  <el-input
                    v-model="form.newPassword"
                    type="password"
                    show-password
                    size="large"
                    placeholder="请输入8-72位新密码"
                  >
                    <template #prefix><el-icon><Lock /></el-icon></template>
                  </el-input>
                </el-form-item>

                <el-form-item prop="confirmPassword">
                  <template #label>
                    <span class="recovery-field-label"><b>04</b><span>确认新密码</span></span>
                  </template>
                  <el-input
                    v-model="form.confirmPassword"
                    type="password"
                    show-password
                    size="large"
                    placeholder="请再次输入新密码"
                  >
                    <template #prefix><el-icon><Lock /></el-icon></template>
                  </el-input>
                </el-form-item>
              </div>

              <el-button
                type="primary"
                size="large"
                class="recovery-submit"
                :loading="submitting"
                @click="submit"
              >
                <span>{{ submitting ? '重置中...' : '重置密码' }}</span>
                <el-icon><Lock /></el-icon>
              </el-button>
            </el-form>

            <footer class="recovery-footer">
              <span>完成后需使用新密码重新登录</span>
              <router-link to="/login">
                <el-icon><ArrowLeft /></el-icon>
                <span>返回登录</span>
              </router-link>
            </footer>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Lock, Message, VideoCamera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getSiteImageUrl } from '@/utils/imageUrl'

const router = useRouter()
const formRef = ref()
const sending = ref(false)
const submitting = ref(false)
const countdown = ref(0)
let timer

const siteConfig = reactive({
  siteLogo: '',
  siteTitle: localStorage.getItem('siteTitle') || ''
})

const form = reactive({ email: '', code: '', newPassword: '', confirmPassword: '' })
const rules = {
  email: [{ required: true, type: 'email', message: '请输入有效QQ邮箱', trigger: 'blur' }],
  code: [{ required: true, pattern: /^\d{6}$/, message: '请输入6位验证码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 8, max: 72, message: '密码长度为8-72位', trigger: 'blur' }],
  confirmPassword: [{ validator: (_r, value, callback) => value === form.newPassword ? callback() : callback(new Error('两次密码不一致')), trigger: 'blur' }]
}

const loadSiteConfig = async () => {
  try {
    const response = await request.get('/site-config/public', { silent: true })
    if (response.success && response.data) {
      siteConfig.siteLogo = getSiteImageUrl(response.data['site.logo'])
      siteConfig.siteTitle = response.data['site.title'] || siteConfig.siteTitle
    }
  } catch (error) {
    console.warn('加载站点配置失败，使用默认站点信息:', error)
  }
}

const sendCode = async () => {
  await formRef.value.validateField('email')
  sending.value = true
  try {
    await request.post('/auth/password-reset/email-code', { email: form.email })
    ElMessage.success('如邮箱已注册，验证码将发送到邮箱')
    countdown.value = 60
    timer = setInterval(() => {
      if (--countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } finally {
    sending.value = false
  }
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    await request.post('/auth/password-reset', { email: form.email, code: form.code, newPassword: form.newPassword })
    ElMessage.success('密码已重置')
    router.push('/login')
  } finally {
    submitting.value = false
  }
}

onMounted(loadSiteConfig)
onBeforeUnmount(() => clearInterval(timer))
</script>

<style src="../styles/forgot-password-archive.css"></style>
