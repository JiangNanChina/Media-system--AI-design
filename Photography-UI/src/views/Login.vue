<template>
  <div class="auth-container" :style="containerStyle">
    <header class="auth-nav">
      <router-link to="/" class="home-link" aria-label="返回首页">
        <el-icon><House /></el-icon>
        <span>返回首页</span>
      </router-link>

      <router-link to="/" class="auth-nav-brand" aria-label="返回融媒体中心首页">
        <span class="auth-nav-mark">
          <img v-if="siteConfig.siteLogo" :src="siteConfig.siteLogo" :alt="siteConfig.siteTitle || '网站LOGO'" />
          <el-icon v-else><VideoCamera /></el-icon>
        </span>
        <span>
          <strong>{{ siteConfig.siteTitle || '融媒体中心' }}</strong>
          <small>Member access</small>
        </span>
      </router-link>

      <div class="auth-nav-status" aria-label="系统状态正常">
        <i></i>
        <span>SECURE ACCESS</span>
      </div>
    </header>

    <main class="auth-main">
      <div class="auth-form" :class="authFormClasses">
        <aside class="auth-header">
          <div class="archive-kicker">
            <span>MEMBER ARCHIVE</span>
            <span>{{ isLoginMode ? 'ACCESS / 01' : 'ENROLL / 02' }}</span>
          </div>

        <transition name="brand-fade">
          <div :key="isLoginMode ? 'login-brand' : 'register-brand'" class="auth-brand-copy">
            <h1 class="auth-title animate-title">
              {{ isLoginMode ? (siteConfig.loginTitle || siteConfig.siteTitle || '融媒体管理系统') : '用户注册' }}
            </h1>
            <p class="auth-subtitle animate-subtitle">
              {{ isLoginMode ? (siteConfig.loginWelcome || '欢迎回来，请登录您的账户') : '创建您的融媒体管理系统账户' }}
            </p>
          </div>
        </transition>

        <div class="identity-frame" aria-hidden="true">
          <div class="identity-frame-meta"><span>REC</span><span>ID / MEMBER</span></div>
          <div class="logo-container animate-logo">
            <img v-if="siteConfig.siteLogo" :src="siteConfig.siteLogo" :alt="siteConfig.siteTitle || '网站LOGO'" class="site-logo" />
            <el-icon v-else class="site-logo-fallback"><VideoCamera /></el-icon>
          </div>
          <div class="identity-focus"></div>
          <span class="identity-timecode">00:00:00:01</span>
        </div>

          <div class="archive-meta" aria-label="账号安全说明">
            <span><small>CHANNEL</small><strong>成员管理平台</strong></span>
            <span><small>VERIFY</small><strong>设备身份校验</strong></span>
            <span><small>STATUS</small><strong><i></i>认证服务正常</strong></span>
          </div>
        </aside>

        <section class="auth-workbench">
          <header class="workbench-heading">
            <div>
              <span>Authentication desk</span>
              <h2>{{ isLoginMode ? '身份核验' : '建立成员档案' }}</h2>
            </div>
            <span class="workbench-index">{{ isLoginMode ? '01' : '02' }}</span>
          </header>

          <div class="mode-tabs" role="tablist" aria-label="认证方式">
            <button
              type="button"
              role="tab"
              class="mode-tab"
              :class="{ active: isLoginMode }"
              :aria-selected="isLoginMode"
              @click="switchToLogin"
            >
              <el-icon><User /></el-icon>
              <span>账户登录</span>
            </button>
            <button
              type="button"
              role="tab"
              class="mode-tab"
              :class="{ active: !isLoginMode }"
              :aria-selected="!isLoginMode"
              @click="switchToRegister"
            >
              <el-icon><UserFilled /></el-icon>
              <span>用户注册</span>
            </button>
          </div>

          <div class="form-container">
            <transition :name="formTransitionName" @after-enter="handleAuthTransitionEnd">
              <el-form
            v-if="isLoginMode"
                key="login-form"
                ref="loginFormRef"
                :model="loginForm"
                :rules="loginRules"
                label-position="top"
                class="auth-form-content login-form"
                @keyup.enter="handleLogin"
              >
                <el-form-item prop="username" label="账号">
                  <el-input
                    v-model="loginForm.username"
                    placeholder="请输入用户名或QQ邮箱"
                    size="large"
                    clearable
                    @input="handleLoginUsernameInput"
                  >
                    <template #prefix>
                      <el-icon><User /></el-icon>
                    </template>
                  </el-input>
                  <div v-if="usernameFormatErrorLogin" class="validation-error">
                    <el-icon><CircleClose /></el-icon>
                    <span>{{ usernameFormatErrorLogin }}</span>
                  </div>
                </el-form-item>

                <el-form-item prop="password" label="密码">
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="请输入密码"
                    size="large"
                    show-password
                    clearable
                  >
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <div class="password-actions">
                  <span>通过已登记账号进入管理平台</span>
                  <router-link to="/forgot-password">忘记密码</router-link>
                </div>

                <el-form-item class="button-item">
                  <el-button
                    type="primary"
                    size="large"
                    class="auth-btn"
                    :loading="loading"
                    @click="handleLogin"
                  >
                    <span>{{ loading ? '登录中...' : '登 录' }}</span>
                    <el-icon><User /></el-icon>
                  </el-button>
                </el-form-item>
              </el-form>
      
          <!-- 注册表单 -->
          <el-form
            v-else
            key="register-form"
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-position="top"
            class="auth-form-content register-form"
            @keyup.enter="handleRegister"
          >
            <!-- 双列布局区域 -->
            <el-row :gutter="12">
              <!-- 左列 -->
              <el-col :xs="24" :sm="24">
                <!-- 用户名 -->
                <el-form-item prop="username">
                  <template #label>
                    <span class="form-label">用户名</span>
                  </template>
                  <el-input
                    v-model="registerForm.username"
                    placeholder="请输入用户名"
                    size="large"
                    clearable
                    @blur="checkUsername"
                    @input="handleRegisterUsernameInput"
                  >
                    <template #prefix>
                      <el-icon><User /></el-icon>
                    </template>
                  </el-input>
                  <div v-if="usernameFormatErrorRegister" class="validation-error">
                    <el-icon><CircleClose /></el-icon>
                    <span>{{ usernameFormatErrorRegister }}</span>
                  </div>
                  <transition name="validation-fade" mode="out-in">
                    <div v-if="usernameChecked && !usernameExists && !usernameFormatErrorRegister" key="success" class="validation-success">
                      <el-icon><CircleCheck /></el-icon>
                      <span>用户名可用</span>
                    </div>
                    <div v-else-if="usernameExists" key="error" class="validation-error">
                      <el-icon><CircleClose /></el-icon>
                      <span>用户名已存在</span>
                    </div>
                  </transition>
                </el-form-item>
              </el-col>
              
              <!-- 右列 -->
              <el-col :xs="24" :sm="12">
                <!-- 真实姓名 -->
                <el-form-item prop="realName">
                  <template #label>
                    <span class="form-label">真实姓名</span>
                  </template>
                  <el-input
                    v-model="registerForm.realName"
                    placeholder="请输入真实姓名"
                    size="large"
                    clearable
                  >
                    <template #prefix>
                      <el-icon><UserFilled /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="12">
              <!-- 左列 -->
              <el-col :xs="24" :sm="12">
                <!-- 邮箱地址 -->
                <el-form-item prop="email">
                  <template #label>
                    <span class="form-label">邮箱地址</span>
                  </template>
                  <el-input
                    v-model="registerForm.email"
                    type="email"
                    placeholder="请输入邮箱地址"
                    size="large"
                    clearable
                    @blur="checkEmail"
                    @input="handleRegisterEmailInput"
                  >
                    <template #prefix>
                      <el-icon><Message /></el-icon>
                    </template>
                  </el-input>
                  <transition name="validation-fade" mode="out-in">
                    <div v-if="emailChecked && !emailExists" key="success" class="validation-success">
                      <el-icon><CircleCheck /></el-icon>
                      <span>邮箱可用</span>
                    </div>
                    <div v-else-if="emailExists" key="error" class="validation-error">
                      <el-icon><CircleClose /></el-icon>
                      <span>邮箱已被使用</span>
                    </div>
                  </transition>
                </el-form-item>
              </el-col>
              
            </el-row>

            <el-row :gutter="12" class="email-code-row">
              <el-col :xs="24" :sm="24">
                <el-form-item prop="emailCode">
                  <template #label>
                    <span class="form-label">邮箱验证码</span>
                  </template>
                  <div class="email-code-control">
                    <el-input
                      v-model="registerForm.emailCode"
                      class="email-code-input"
                      placeholder="请输入6位验证码"
                      size="large"
                      maxlength="6"
                      clearable
                      @input="handleEmailCodeInput"
                    >
                      <template #prefix>
                        <el-icon><Message /></el-icon>
                      </template>
                    </el-input>
                    <el-button
                      class="email-code-button"
                      :loading="emailCodeSending"
                      :disabled="!canSendEmailCode"
                      @click="sendEmailCode"
                    >
                      <el-icon><Message /></el-icon>
                      <span>{{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s` : '发送验证码' }}</span>
                    </el-button>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="12">
              <!-- 左列 -->
              <el-col :xs="24" :sm="12">
                <!-- 密码 -->
                <el-form-item prop="password">
                  <template #label>
                    <span class="form-label">密码</span>
                  </template>
                  <el-input
                    v-model="registerForm.password"
                    type="password"
                    placeholder="请输入密码"
                    size="large"
                    show-password
                    clearable
                  >
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              
              <!-- 右列 -->
              <el-col :xs="24" :sm="12">
                <!-- 确认密码 -->
                <el-form-item prop="confirmPassword">
                  <template #label>
                    <span class="form-label">确认密码</span>
                  </template>
                  <el-input
                    v-model="registerForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入密码"
                    size="large"
                    show-password
                    clearable
                  >
                    <template #prefix>
                      <el-icon><Lock /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>
            
            <!-- 密码强度指示器 - 全宽 -->
            <div class="password-strength full-width-item" v-if="registerForm.password">
              <div class="strength-label">密码强度：</div>
              <div class="strength-bar">
                <div 
                  class="strength-fill" 
                  :class="passwordStrengthClass"
                  :style="{ width: passwordStrengthWidth }"
                ></div>
              </div>
              <div class="strength-text" :class="passwordStrengthClass">
                {{ passwordStrengthText }}
              </div>
            </div>
            
            <el-form-item class="button-item">
              <el-button
                type="primary"
                size="large"
                class="auth-btn"
                :loading="registering"
                :disabled="!canRegister"
                @click="handleRegister"
              >
                <span>{{ registering ? '注册中...' : '立即注册' }}</span>
                <el-icon><UserFilled /></el-icon>
              </el-button>
            </el-form-item>
              </el-form>
            </transition>
          </div>
        </section>
      </div>
    </main>
    
    <!-- 注册成功对话框 -->
    <el-dialog
      v-model="showSuccessDialog"
      class="auth-success-dialog"
      title="注册成功"
      width="500px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <div class="success-content">
        <el-result
          icon="success"
          title="账户创建成功！"
          :sub-title="successMessage"
        >
          <template #extra>
            <el-button type="primary" @click="goToLogin">
              立即登录
            </el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  User, 
  UserFilled, 
  Message, 
  Lock,
  House,
  VideoCamera,
  CircleCheck, 
  CircleClose
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { generateDeviceFingerprint } from '@/utils/deviceFingerprint'
import request from '@/utils/request'
import { getSiteImageUrl } from '@/utils/imageUrl'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 模式切换状态
const isLoginMode = ref(true)
const transitionDirection = ref('forward')
const isModeSwitching = ref(false)

const authFormClasses = computed(() => ({
  'auth-form-login': isLoginMode.value,
  'auth-form-register': !isLoginMode.value,
  'auth-form-switching': isModeSwitching.value
}))

const formTransitionName = computed(() => (
  transitionDirection.value === 'backward' ? 'auth-panel-backward' : 'auth-panel-forward'
))

// 登录和注册状态
const loading = ref(false)

// 站点配置数据
const siteConfig = reactive({
  siteLogo: '',
  loginBackground: '',
  siteTitle: '',
  loginTitle: '',
  loginWelcome: '',
  primaryColor: ''
})

// 注册相关状态
const registering = ref(false)
const showSuccessDialog = ref(false)
const successMessage = ref('')
const usernameChecked = ref(false)
const usernameExists = ref(false)
const usernameFormatErrorLogin = ref('')
const usernameFormatErrorRegister = ref('')
const emailChecked = ref(false)
const emailExists = ref(false)
const emailCodeSending = ref(false)
const emailCodeCountdown = ref(0)
let emailCodeTimer = null

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 注册表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  emailCode: '',
  role: 'MEMBER'
})

// 注册表单验证规则
const registerRules = {
  role: [
    { required: true, message: '请选择注册类型', trigger: 'change' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线，且不能包含中文', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '邮箱验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 72, message: '密码长度在 8 到 72 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' }
  ]
}

const loginUsernamePattern = /^[a-zA-Z0-9_]+$/
const loginEmailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const validateLoginAccount = (rule, value, callback) => {
  const account = String(value || '').trim()

  if (!account) {
    callback(new Error('请输入用户名或QQ邮箱'))
    return
  }

  if (/[\u4e00-\u9fa5]/.test(account)) {
    callback(new Error('账号不能包含中文'))
    return
  }

  if (account.includes('@')) {
    if (account.length > 100 || !loginEmailPattern.test(account)) {
      callback(new Error('请输入正确的邮箱格式'))
      return
    }
    callback()
    return
  }

  if (account.length < 2 || account.length > 50) {
    callback(new Error('用户名长度在 2 到 50 个字符'))
    return
  }

  if (!loginUsernamePattern.test(account)) {
    callback(new Error('用户名只能包含字母、数字和下划线，或输入邮箱地址'))
    return
  }

  callback()
}

// 表单验证规则
const loginRules = {
  username: [
    { validator: validateLoginAccount, trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度在 6 到 50 个字符', trigger: 'blur' }
  ]
}

const loginFormRef = ref()
const registerFormRef = ref()
const backgroundLoaded = ref(false) // 🔧 背景图片加载状态

// 密码强度计算
const passwordStrength = computed(() => {
  const password = registerForm.password
  if (!password) return 0
  
  let strength = 0
  
  // 长度加分
  if (password.length >= 8) strength += 25
  if (password.length >= 12) strength += 25
  
  // 包含小写字母
  if (/[a-z]/.test(password)) strength += 15
  
  // 包含大写字母
  if (/[A-Z]/.test(password)) strength += 15
  
  // 包含数字
  if (/[0-9]/.test(password)) strength += 10
  
  // 包含特殊字符
  if (/[^A-Za-z0-9]/.test(password)) strength += 10
  
  return strength
})

const passwordStrengthWidth = computed(() => `${passwordStrength.value}%`)

const passwordStrengthClass = computed(() => {
  const strength = passwordStrength.value
  if (strength >= 80) return 'strong'
  if (strength >= 50) return 'medium'
  return 'weak'
})

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength >= 80) return '强'
  if (strength >= 50) return '中'
  return '弱'
})

const canRegister = computed(() => {
  // 基本字段检查
  if (!registerForm.username || !registerForm.password || !registerForm.confirmPassword || !registerForm.realName || !registerForm.email || !registerForm.emailCode) {
    return false
  }
  
  // 角色检查
  if (!registerForm.role) {
    return false
  }
  
  // 密码一致性检查
  if (registerForm.password !== registerForm.confirmPassword) {
    return false
  }
  
  // 用户名检查
  if (usernameChecked.value && usernameExists.value) {
    return false
  }
  
  // 邮箱检查
  if (emailChecked.value && emailExists.value) {
    return false
  }
  
  // 🔓 密码强度检查已移除 - 允许任何强度的密码注册
  // 注意：密码强度指示器仍会显示，但不再阻止注册
  // if (passwordStrength.value < 50) {
  //   return false
  // }
  
  return true
})

const canSendEmailCode = computed(() => {
  return !emailCodeSending.value &&
    emailCodeCountdown.value === 0 &&
    registerForm.email &&
    isValidEmail(registerForm.email) &&
    !emailExists.value
})

// 🔧 图片预加载函数 - 确保背景图片完全加载后再显示
const preloadBackgroundImage = (imageUrl) => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    
    img.onload = () => {
      backgroundLoaded.value = true
      console.log('✅ 登录背景图片加载完成')
      resolve(img)
    }
    
    img.onerror = () => {
      console.warn('❌ 登录背景图片加载失败:', imageUrl)
      backgroundLoaded.value = true // 即使失败也标记为完成，避免页面卡住
      reject(new Error('背景图片加载失败'))
    }
    
    // 设置高质量加载选项
    img.crossOrigin = 'anonymous'
    img.decoding = 'async'
    img.loading = 'eager'
    img.src = imageUrl
  })
}

// 🔧 优化：动态背景样式 - 超清显示
const containerStyle = computed(() => {
  const style = {}
  
  console.log('🔍 containerStyle 计算:', {
    loginBackground: siteConfig.loginBackground,
    backgroundLoaded: backgroundLoaded.value
  })
  
  if (siteConfig.loginBackground && backgroundLoaded.value) {
    style.backgroundImage = `url(${siteConfig.loginBackground})`
    style.backgroundSize = 'cover'
    style.backgroundPosition = 'center center'
    style.backgroundRepeat = 'no-repeat'
    style.backgroundAttachment = 'fixed' // 固定背景，提升视觉效果
    
    // 🔧 高清显示优化
    style.imageRendering = 'high-quality' // 高质量渲染
    style.backfaceVisibility = 'hidden' // 优化性能
    style.transform = 'translateZ(0)' // 启用硬件加速
    style.willChange = 'transform' // 提示浏览器优化
    
    // 🔧 平滑过渡效果
    style.transition = 'background-image 0.5s ease-in-out'
    
    console.log('✅ 应用背景图片样式:', style.backgroundImage)
  } else if (!backgroundLoaded.value && siteConfig.loginBackground) {
    // 🔧 背景加载时的占位样式
    style.backgroundColor = '#1a1a2e'
    style.backgroundImage = 'linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)'
    console.log('⏳ 背景加载中，使用占位样式')
  } else {
    console.log('ℹ️ 使用默认渐变背景')
  }
  
  return style
})

// 🔧 优化：加载站点配置并预加载背景图片
const loadSiteConfig = async () => {
  try {
    const response = await request.get('/site-config/public', { silent: true })
    
    if (response.success && response.data) {
      // 映射配置键到本地数据
      const configData = response.data
      
      siteConfig.siteLogo = getSiteImageUrl(configData['site.logo'])
      const backgroundUrl = getSiteImageUrl(configData['login.background'])
      siteConfig.siteTitle = configData['site.title'] || ''
      siteConfig.loginTitle = configData['login.title'] || ''
      siteConfig.loginWelcome = configData['login.welcome'] || ''
      siteConfig.primaryColor = configData['theme.primary_color'] || ''
      
      // 🔧 预加载背景图片以确保超清显示
      if (backgroundUrl) {
        console.log('🖼️ 开始预加载登录背景图片...')
        try {
          await preloadBackgroundImage(backgroundUrl)
          siteConfig.loginBackground = backgroundUrl
          console.log('✅ 背景图片预加载完成，应用超清显示效果')
        } catch (error) {
          console.warn('背景图片预加载失败，使用默认背景:', error)
          siteConfig.loginBackground = backgroundUrl // 即使预加载失败也设置URL
          backgroundLoaded.value = true
        }
      } else {
        // 没有设置背景图片时，使用默认渐变背景
        backgroundLoaded.value = true
        console.log('ℹ️ 未设置登录背景图片，使用默认样式')
      }
      
      // 缓存站点标题到localStorage，供路由使用
      if (siteConfig.siteTitle) {
        localStorage.setItem('siteTitle', siteConfig.siteTitle)
      }
      
      // 如果有主题色，动态设置CSS变量
      if (siteConfig.primaryColor) {
        document.documentElement.style.setProperty('--el-color-primary', siteConfig.primaryColor)
      }
    }
  } catch (error) {
    console.warn('加载站点配置失败:', error)
    // 配置加载失败不影响登录功能，只是使用默认样式
    backgroundLoaded.value = true
  }
}

// 生命周期
onMounted(() => {
  loadSiteConfig()
})

// 登录处理
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    const loginAccount = String(loginForm.username || '').trim()
    loginForm.username = loginAccount

    // 先校验输入中是否包含中文
    if (/[\u4e00-\u9fa5]/.test(loginAccount)) {
      usernameFormatErrorLogin.value = '账号不能包含中文'
      ElMessage.error('账号不能包含中文')
      return
    }
    await loginFormRef.value.validate()
    loading.value = true
    
    // 生成设备指纹
    ElMessage.info('正在验证设备信息...')
    const deviceInfo = await generateDeviceFingerprint()
    
    // 构建登录请求数据
    const loginData = {
      username: loginAccount,
      password: loginForm.password,
      deviceInfo: deviceInfo
    }
    
    const result = await userStore.login(loginData)
    
    if (result.success) {
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    
    // 处理不同类型的错误
    if (error.message && error.message.includes('当前账号已在其他')) {
      // 设备绑定冲突的详细提示
      ElMessage({
        message: error.message,
        type: 'warning',
        duration: 8000,
        showClose: true
      })
    } else if (error.message && error.message.includes('设备')) {
      ElMessage.error(error.message)
    } else if (error.message && error.message.includes('作弊')) {
      ElMessage.error('检测到异常登录行为，请使用原设备登录')
    } else {
      ElMessage.error('登录失败，请检查账号和密码')
    }
  } finally {
    loading.value = false
  }
}

// 登录表单账号输入时校验：支持用户名或邮箱
const handleLoginUsernameInput = (value) => {
  const rawValue = String(value || '')
  const filtered = rawValue.replace(/\s/g, '')
  const hasChinese = /[\u4e00-\u9fa5]/.test(filtered)

  if (hasChinese) {
    usernameFormatErrorLogin.value = '账号不能包含中文'
  } else if (filtered.includes('@') && filtered && !loginEmailPattern.test(filtered)) {
    usernameFormatErrorLogin.value = '请输入正确的邮箱格式'
  } else if (!filtered.includes('@') && filtered && !loginUsernamePattern.test(filtered)) {
    usernameFormatErrorLogin.value = '用户名只能包含字母、数字和下划线'
  } else {
    usernameFormatErrorLogin.value = ''
  }
  if (filtered !== loginForm.username) {
    loginForm.username = filtered
  }
}

// 模式切换方法
const setAuthMode = (nextMode) => {
  const nextIsLoginMode = nextMode === 'login'

  if (isLoginMode.value === nextIsLoginMode) {
    return false
  }

  transitionDirection.value = nextIsLoginMode ? 'backward' : 'forward'
  isModeSwitching.value = true
  isLoginMode.value = nextIsLoginMode
  return true
}

const handleAuthTransitionEnd = () => {
  isModeSwitching.value = false
}

const switchToLogin = () => {
  const didSwitch = setAuthMode('login')

  if (!didSwitch) {
    return
  }
  
  // 清空登录表单数据
  loginForm.username = ''
  loginForm.password = ''
  usernameFormatErrorLogin.value = ''
  
  // 清除表单验证错误
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }
}

const switchToRegister = () => {
  const didSwitch = setAuthMode('register')

  if (!didSwitch) {
    return
  }
  
  // 清空注册表单数据
  registerForm.username = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.realName = ''
  registerForm.email = ''
  registerForm.emailCode = ''
  registerForm.role = 'MEMBER'
  resetEmailCodeState()
  
  // 重置验证状态
  usernameChecked.value = false
  usernameExists.value = false
  emailChecked.value = false
  emailExists.value = false
  
  // 清除表单验证错误
  if (registerFormRef.value) {
    registerFormRef.value.clearValidate()
  }
}

// 注册相关方法
const handleRegister = async () => {
  // 1. 表单验证
  if (!registerFormRef.value) {
    ElMessage.error('表单未初始化')
    return
  }
  
  try {
    // 2. 验证表单
    await registerFormRef.value.validate()
    
    // 3. 检查密码是否一致
    if (registerForm.password !== registerForm.confirmPassword) {
      ElMessage.error('两次输入的密码不一致')
      return
    }
    
    // 4. 检查用户名是否已存在
    if (usernameExists.value) {
      ElMessage.error('用户名已被使用，请更换用户名')
      return
    }
    
    // 5. 检查邮箱是否已存在
    if (emailExists.value) {
      ElMessage.error('邮箱已被注册，请更换邮箱')
      return
    }
    
    // 8. 开始注册
    registering.value = true
    
    // 9. 构建注册请求
    const registerData = {
      username: registerForm.username.trim(),
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      realName: registerForm.realName.trim(),
      email: registerForm.email.trim(),
      emailCode: registerForm.emailCode.trim(),
      role: 'MEMBER'
    }
    
    console.log('📤 发送注册请求:', { ...registerData, password: '******', confirmPassword: '******' })
    
    // 10. 调用注册API
    const response = await request.post('/auth/register', registerData)
    
    console.log('✅ 注册成功响应:', response)
    
    // 11. 显示成功消息
    if (response.data?.message) {
      successMessage.value = response.data.message
    } else {
      successMessage.value = `申请已提交，${registerForm.realName}，请等待管理员审核。`
    }
    
    // 12. 显示成功对话框
    showSuccessDialog.value = true
    
    // 13. 清空表单
    registerForm.username = ''
    registerForm.password = ''
    registerForm.confirmPassword = ''
    registerForm.realName = ''
    registerForm.email = ''
    registerForm.emailCode = ''
    registerForm.role = 'MEMBER'
    resetEmailCodeState()
    
    // 14. 重置验证状态
    usernameChecked.value = false
    usernameExists.value = false
    emailChecked.value = false
    emailExists.value = false
    
    // 15. 重置表单验证
    if (registerFormRef.value) {
      registerFormRef.value.resetFields()
    }
    
    ElMessage.success('注册成功！请使用新账号登录')
    
  } catch (error) {
    console.error('❌ 注册失败:', error)
    
    // 处理错误
    let errorMessage = '注册失败，请稍后重试'
    
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    registering.value = false
  }
}

const checkUsername = async () => {
  // 检查用户名是否已存在
  if (!registerForm.username || registerForm.username.length < 3) {
    usernameChecked.value = false
    usernameExists.value = false
    return
  }
  
  try {
    // 若存在格式错误（包含中文或非法字符），提示并中断检查
    if (usernameFormatErrorRegister.value) {
      ElMessage.error(usernameFormatErrorRegister.value)
      usernameChecked.value = false
      return
    }
    const response = await request.get(`/auth/check-username?username=${registerForm.username}`)
    usernameExists.value = response.data // true表示已存在
    usernameChecked.value = true
  } catch (error) {
    console.error('检查用户名失败:', error)
    usernameChecked.value = false
    usernameExists.value = false
  }
}

// 注册表单用户名输入时校验（禁止中文）
const handleRegisterUsernameInput = (value) => {
  const hasChinese = /[\u4e00-\u9fa5]/.test(value)
  const filtered = value.replace(/[^a-zA-Z0-9_]/g, '')
  if (hasChinese) {
    usernameFormatErrorRegister.value = '用户名不能包含中文'
    ElMessage.error('用户名不能包含中文')
  } else if (filtered !== value) {
    usernameFormatErrorRegister.value = '用户名只能包含字母、数字和下划线'
    ElMessage.warning('用户名只能包含字母、数字和下划线')
  } else {
    usernameFormatErrorRegister.value = ''
  }
  if (filtered !== registerForm.username) {
    registerForm.username = filtered
  }
}

const handleRegisterEmailInput = () => {
  emailChecked.value = false
  emailExists.value = false
  registerForm.emailCode = ''
  resetEmailCodeState()
}

const handleEmailCodeInput = (value) => {
  const filtered = String(value || '').replace(/\D/g, '').slice(0, 6)
  if (filtered !== registerForm.emailCode) {
    registerForm.emailCode = filtered
  }
}

const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(email || '').trim())

const startEmailCodeCountdown = () => {
  resetEmailCodeState()
  emailCodeCountdown.value = 60
  emailCodeTimer = window.setInterval(() => {
    emailCodeCountdown.value -= 1
    if (emailCodeCountdown.value <= 0) {
      resetEmailCodeState()
    }
  }, 1000)
}

const resetEmailCodeState = () => {
  if (emailCodeTimer) {
    window.clearInterval(emailCodeTimer)
    emailCodeTimer = null
  }
  emailCodeCountdown.value = 0
}

const sendEmailCode = async () => {
  if (!isValidEmail(registerForm.email)) {
    ElMessage.warning('请先输入正确的邮箱地址')
    return
  }

  try {
    await checkEmail()
    if (emailExists.value) {
      ElMessage.error('邮箱已被注册，请更换邮箱')
      return
    }

    emailCodeSending.value = true
    await request.post('/auth/email-code', { email: registerForm.email.trim() })
    ElMessage.success('验证码已发送，请查收邮箱')
    startEmailCodeCountdown()
  } catch (error) {
    console.error('发送邮箱验证码失败:', error)
    ElMessage.error(error.message || '验证码发送失败')
  } finally {
    emailCodeSending.value = false
  }
}

const checkEmail = async () => {
  // 检查邮箱是否已被使用
  if (!registerForm.email) {
    emailChecked.value = false
    emailExists.value = false
    return
  }
  
  // 简单的邮箱格式验证
  if (!isValidEmail(registerForm.email)) {
    emailChecked.value = false
    emailExists.value = false
    return
  }
  
  try {
    const response = await request.get(`/auth/check-email?email=${registerForm.email}`)
    emailExists.value = response.data // true表示已被使用
    emailChecked.value = true
  } catch (error) {
    console.error('检查邮箱失败:', error)
    emailChecked.value = false
    emailExists.value = false
  }
}

const goToLogin = () => {
  // 关闭成功对话框
  showSuccessDialog.value = false
  // 切换到登录模式
  switchToLogin()
  // 提示用户
  ElMessage.success('请使用新账号登录')
}

// 组件挂载时的处理
onMounted(() => {
  loadSiteConfig()
  
  // 检查路由参数，如果是从注册页面来的，切换到注册模式
  if (route.path === '/register' || route.query.mode === 'register' || route.meta?.mode === 'register') {
    isLoginMode.value = false
  }
})

onUnmounted(() => {
  resetEmailCodeState()
})
</script>

<style src="../styles/login-archive.css"></style>
