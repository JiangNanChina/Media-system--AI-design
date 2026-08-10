<template>
  <div class="auth-container" :style="containerStyle">
    <router-link to="/" class="home-link" aria-label="返回首页">
      <el-icon><House /></el-icon>
      <span>返回首页</span>
    </router-link>

    <div class="auth-form">
      <div class="auth-header">
        <h2 class="auth-title animate-title">
          {{ isLoginMode ? (siteConfig.loginTitle || siteConfig.siteTitle || '融媒体管理系统') : '用户注册' }}
        </h2>
        <p class="auth-subtitle animate-subtitle">
          {{ isLoginMode ? (siteConfig.loginWelcome || '欢迎回来，请登录您的账户') : '创建您的融媒体管理系统账户' }}
        </p>
        <!-- 动态LOGO -->
        <div v-if="siteConfig.siteLogo" class="logo-container animate-logo">
          <img :src="siteConfig.siteLogo" :alt="siteConfig.siteTitle || '网站LOGO'" class="site-logo" />
        </div>
      </div>
      
      <!-- 模式切换标签 -->
      <div class="mode-tabs">
        <div 
          class="mode-tab" 
          :class="{ active: isLoginMode }"
          @click="switchToLogin"
        >
          <el-icon><User /></el-icon>
          <span>账户登录</span>
        </div>
        <div 
          class="mode-tab" 
          :class="{ active: !isLoginMode }"
          @click="switchToRegister"
        >
          <el-icon><UserFilled /></el-icon>
          <span>用户注册</span>
        </div>
        <div class="tab-indicator" :class="{ 'move-right': !isLoginMode }"></div>
      </div>
      
      <!-- 表单切换容器 -->
      <div class="form-container">
        <transition name="form-slide" mode="out-in">
          <!-- 登录表单 -->
      <el-form
            v-if="isLoginMode"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
            class="auth-form-content"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
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
        
        <el-form-item prop="password">
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
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
          <!-- 注册表单 -->
          <el-form
            v-else
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
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
                {{ registering ? '注册中...' : '立即注册' }}
              </el-button>
            </el-form-item>
          </el-form>
        </transition>
      </div>
    </div>
    
    <!-- 注册成功对话框 -->
    <el-dialog
      v-model="showSuccessDialog"
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
const switchToLogin = () => {
  isLoginMode.value = true
  
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
  isLoginMode.value = false
  
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

<style scoped>
.password-actions {
  display: flex;
  justify-content: flex-end;
  margin: -8px 0 16px;
  font-size: 14px;
  color: var(--el-color-primary);
}

.password-actions a:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 3px;
}
/* 现代化认证容器 */
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-5);
  position: relative;
  z-index: 1;
  background: var(--gradient-background);
  
  /* 背景优化 - 不随内容放大 */
  background-attachment: fixed;
  background-size: 100% 100%;  /* 🎨 背景固定尺寸，不随内容放大 */
  background-position: center center;
  background-repeat: no-repeat;
  
  /* 硬件加速优化 */
  transform: translateZ(0);
  backface-visibility: hidden;
  perspective: 1000px;
  
  /* 字体渲染优化 */
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  
  /* 超清显示滤镜优化 */
  filter: contrast(1.02) saturate(1.05) brightness(1.01);
  -webkit-filter: contrast(1.02) saturate(1.05) brightness(1.01);
}

/* 现代化认证表单 - 磨玻璃效果 */
.auth-form {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: var(--radius-xl);
  padding: var(--spacing-6);  /* 🎨 减小内边距，使表单更紧凑 */
  width: 100%;
  max-width: 420px;  /* 🎨 登录表单宽度 */
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 2px 16px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  position: relative;
  overflow: hidden;
  animation: slideUp var(--duration-slow) var(--easing-spring);
}

/* 🎨 注册表单宽度自适应 - 支持双列布局 */
.auth-form:has(.register-form) {
  max-width: 680px;  /* 🎨 注册表单更宽，支持双列布局 */
}

.auth-form::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, 
    transparent, 
    rgba(255, 255, 255, 0.8) 20%, 
    rgba(255, 255, 255, 0.8) 80%, 
    transparent
  );
}

.auth-form::after {
  content: '';
  position: absolute;
  top: -1px;
  left: -1px;
  right: -1px;
  bottom: -1px;
  background: linear-gradient(45deg, 
    rgba(255, 255, 255, 0.2) 0%, 
    rgba(255, 255, 255, 0.1) 50%, 
    rgba(255, 255, 255, 0.2) 100%
  );
  border-radius: var(--radius-xl);
  z-index: -1;
}

.auth-header {
  text-align: center;
  margin-bottom: var(--spacing-5);  /* 🎨 增加标题下方间距，避免遮挡 */
  position: relative;
  z-index: 2;  /* 确保头部在表单上方 */
}

/* 模式切换标签 */
.mode-tabs {
  display: flex;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  padding: var(--spacing-1);
  margin-bottom: var(--spacing-4);  /* 🎨 减小标签下方间距 */
  position: relative;
  z-index: 1;  /* 确保标签在表单上方，但在头部下方 */
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.mode-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-3) var(--spacing-4);
  border-radius: var(--radius-md);
  cursor: pointer;
  color: rgba(255, 255, 255, 0.7);
  font-weight: var(--font-weight-medium);
  transition: all var(--duration-normal) var(--easing-ease);
  z-index: 2;
  position: relative;
  gap: var(--spacing-2);
}

.mode-tab.active {
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.mode-tab:hover {
  color: rgba(255, 255, 255, 0.9);
}

.tab-indicator {
  position: absolute;
  top: var(--spacing-1);
  left: var(--spacing-1);
  width: calc(50% - var(--spacing-1));
  height: calc(100% - var(--spacing-2));
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-md);
  transition: transform var(--duration-normal) var(--easing-spring);
  z-index: 1;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(255, 255, 255, 0.1);
}

.tab-indicator.move-right {
  transform: translateX(100%);
}

/* 表单容器 */
.form-container {
  position: relative;
  overflow: hidden;
  z-index: 1;  /* 确保表单容器在头部下方 */
}

/* 表单切换动画 */
.form-slide-enter-active,
.form-slide-leave-active {
  transition: all var(--duration-normal) var(--easing-ease);
}

.form-slide-enter-from {
  opacity: 0;
  transform: translateX(30px) scale(0.95);
}

.form-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px) scale(0.95);
}

.login-header {
  text-align: center;
  margin-bottom: 16px;  /* 🎨 减小间距 */
}

.logo-container {
  display: flex;
  justify-content: center;
  margin-bottom: 12px;  /* 🎨 减小间距 */
}

.site-logo {
  max-width: 90px;  /* 🎨 减小logo尺寸 */
  max-height: 60px;  /* 🎨 减小logo尺寸 */
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 现代化标题样式 - 纯白色字体 */
.auth-title {
  font-size: var(--font-size-2xl);  /* 🎨 减小字体大小 */
  font-weight: var(--font-weight-bold);
  color: #ffffff;
  margin-bottom: var(--spacing-1);  /* 🎨 减小间距 */
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.auth-subtitle {
  color: #ffffff;
  font-size: var(--font-size-sm);  /* 🎨 减小字体 */
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
  margin-bottom: var(--spacing-3);  /* 确保与下方元素有足够间距 */
  position: relative;
  z-index: 1;  /* 确保提示词在输入框上方 */
}

/* 现代化表单样式 */
.auth-form-content {
  margin-bottom: var(--spacing-3);  /* 🎨 减小间距 */
}

/* 🎨 表单布局优化 - 整齐对齐 */
.auth-form-content :deep(.el-form-item) {
  margin-bottom: var(--spacing-2);  /* 🎨 进一步减小表单项间距 */
  width: 100% !important;
}

.auth-form-content :deep(.el-form-item__content) {
  width: 100% !important;
  display: block !important;
}

/* 🎨 双列布局优化 - 注册表单 */
.register-form :deep(.el-row) {
  margin-bottom: 0 !important;
  margin-left: -6px !important;  /* 抵消gutter的一半 */
  margin-right: -6px !important;  /* 抵消gutter的一半 */
}

.register-form :deep(.el-col) {
  margin-bottom: 0 !important;
  padding-left: 6px !important;  /* gutter的一半 */
  padding-right: 6px !important;  /* gutter的一半 */
}

/* 全宽项目（注册类型、管理员密钥等） */
.full-width-item {
  width: 100% !important;
  grid-column: 1 / -1 !important;
}

/* 响应式：小屏幕自动变为单列 */
@media (max-width: 640px) {
  .auth-form:has(.register-form) {
    max-width: 420px;  /* 小屏幕时恢复单列宽度 */
  }
}

/* 🎨 表单标签统一对齐 */
.auth-form-content :deep(.el-form-item__label) {
  color: #ffffff !important;
  font-weight: var(--font-weight-semibold) !important;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.4) !important;
  font-size: var(--font-size-xs) !important;  /* 🎨 减小标签字体 */
  margin-bottom: var(--spacing-1) !important;  /* 🎨 减小标签间距 */
  width: 100% !important;
  text-align: left !important;
  justify-content: flex-start !important;
  padding: 0 !important;
  line-height: 1.2 !important;  /* 🎨 减小行高 */
}

.form-label {
  color: #ffffff;
  font-weight: var(--font-weight-semibold);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
  font-size: var(--font-size-xs);  /* 🎨 减小字体 */
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-1);
  margin-bottom: var(--spacing-1);  /* 🎨 减小间距 */
  width: 100%;
  line-height: 1.2;  /* 🎨 减小行高 */
}

/* 移除装饰线条，保持简洁 */
.form-label::before {
  display: none;
}

/* 🔧 确保表单元素可见性 */
.auth-form-content :deep(*) {
  visibility: visible !important;
  opacity: 1 !important;
}

.auth-form-content :deep(.el-form-item),
.auth-form-content :deep(.el-input),
.auth-form-content :deep(.el-select),
.auth-form-content :deep(.el-radio-group),
.auth-form-content :deep(.el-input__wrapper) {
  display: block !important;
  visibility: visible !important;
  opacity: 1 !important;
}

.auth-form-content :deep(.el-radio-group) {
  display: flex !important;
}

/* 🎨 输入框简化设计 - 整齐统一 */
.auth-form-content :deep(.el-input),
.auth-form-content :deep(.el-select) {
  width: 100% !important;
  display: block !important;
}

.auth-form-content :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.92) !important;
  backdrop-filter: blur(12px) !important;
  -webkit-backdrop-filter: blur(12px) !important;
  border: 1px solid rgba(255, 255, 255, 0.5) !important;
  border-radius: var(--radius-md) !important;
  min-height: 36px !important;  /* 🎨 进一步减小高度 */
  height: auto !important;
  padding: 0 var(--spacing-2) !important;  /* 🎨 进一步减小内边距 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
  transition: all 0.2s ease !important;
  display: flex !important;
  align-items: center !important;
}

.auth-form-content :deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.95) !important;
  border-color: rgba(64, 158, 255, 0.6) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
}

.auth-form-content :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.98) !important;
  border-color: rgba(64, 158, 255, 0.8) !important;
  box-shadow: 
    0 0 0 3px rgba(64, 158, 255, 0.2),
    0 4px 12px rgba(64, 158, 255, 0.3) !important;
}

.auth-form-content :deep(.el-input__inner) {
  font-size: var(--font-size-base) !important;
  line-height: 1.5 !important;
  font-weight: var(--font-weight-medium) !important;
  color: #303133 !important;
  background: transparent !important;
  height: 32px !important;
  flex: 1 !important;
}

.auth-form-content :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.65) !important;  /* 改为白色半透明，更清晰 */
  font-weight: var(--font-weight-normal) !important;
}

/* 兼容不同浏览器的 placeholder */
.auth-form-content :deep(.el-input__inner::-webkit-input-placeholder) {
  color: rgba(255, 255, 255, 0.65) !important;
  opacity: 1 !important;
}

.auth-form-content :deep(.el-input__inner::-moz-placeholder) {
  color: rgba(255, 255, 255, 0.65) !important;
  opacity: 1 !important;
}

.auth-form-content :deep(.el-input__inner:-ms-input-placeholder) {
  color: rgba(255, 255, 255, 0.65) !important;
  opacity: 1 !important;
}

.auth-form-content :deep(.el-input__prefix-inner) {
  color: rgba(64, 158, 255, 0.8) !important;
  margin-right: var(--spacing-2) !important;
}

/* 🎨 选择框样式统一 - 与输入框保持一致 */
.auth-form-content :deep(.el-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9) !important;
  backdrop-filter: blur(12px) !important;
  -webkit-backdrop-filter: blur(12px) !important;
  border: 1px solid rgba(255, 255, 255, 0.4) !important;
  border-radius: var(--radius-lg) !important;
  min-height: 48px !important;
  transition: all 0.2s ease !important;
}

.auth-form-content :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(64, 158, 255, 0.6) !important;
  background: rgba(255, 255, 255, 0.95) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
}

.auth-form-content :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: rgba(64, 158, 255, 0.8) !important;
  box-shadow: 
    0 0 0 3px rgba(64, 158, 255, 0.2),
    0 4px 12px rgba(64, 158, 255, 0.3) !important;
  background: rgba(255, 255, 255, 0.98) !important;
}

/* 下拉选项样式 */
.auth-form-content :deep(.el-select-dropdown) {
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(20px) !important;
  -webkit-backdrop-filter: blur(20px) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2) !important;
}

.auth-form-content :deep(.el-select-dropdown__item) {
  color: var(--color-text-primary) !important;
  font-weight: var(--font-weight-medium) !important;
  padding: var(--spacing-3) var(--spacing-4) !important;
}

.auth-form-content :deep(.el-select-dropdown__item:hover) {
  background: rgba(64, 158, 255, 0.1) !important;
  color: rgba(64, 158, 255, 0.9) !important;
}

.auth-form-content :deep(.el-select-dropdown__item.is-selected) {
  background: rgba(64, 158, 255, 0.15) !important;
  color: rgba(64, 158, 255, 1) !important;
  font-weight: var(--font-weight-semibold) !important;
}

/* 🎨 单选按钮组样式 - 简洁整齐 */
.auth-form-content :deep(.el-radio-group) {
  display: flex;
  gap: var(--spacing-2);  /* 🎨 减小间距 */
  width: 100%;
  margin: var(--spacing-1) 0;  /* 🎨 减小间距 */
}

.auth-form-content :deep(.el-radio) {
  flex: 1;
  margin-right: 0 !important;
  border: 1px solid rgba(255, 255, 255, 0.4) !important;
  border-radius: var(--radius-md) !important;  /* 🎨 减小圆角 */
  padding: var(--spacing-2) var(--spacing-3) !important;  /* 🎨 减小内边距 */
  background: rgba(255, 255, 255, 0.15) !important;
  backdrop-filter: blur(10px) !important;
  -webkit-backdrop-filter: blur(10px) !important;
  transition: all 0.2s ease !important;
  cursor: pointer;
}

.auth-form-content :deep(.el-radio:hover) {
  border-color: rgba(64, 158, 255, 0.6) !important;
  background: rgba(255, 255, 255, 0.25) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1) !important;
}

.auth-form-content :deep(.el-radio.is-checked) {
  border-color: rgba(64, 158, 255, 0.8) !important;
  background: rgba(64, 158, 255, 0.2) !important;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3) !important;
}

.auth-form-content :deep(.el-radio__label) {
  color: #ffffff !important;
  font-weight: var(--font-weight-medium) !important;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3) !important;
  font-size: var(--font-size-sm) !important;
}

.role-option {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-size: var(--font-size-sm);
  justify-content: center;
}

/* 🎨 验证反馈样式 - 增强可见性 */
.validation-success {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  color: #ffffff;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  margin-top: var(--spacing-2);
  padding: var(--spacing-2) var(--spacing-3);
  background: linear-gradient(135deg, rgba(82, 196, 26, 0.9), rgba(115, 209, 61, 0.9));
  border-radius: var(--radius-md);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 
    0 2px 8px rgba(82, 196, 26, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  animation: successPulse 0.5s ease-out;
}

.validation-success .success-icon,
.validation-success el-icon {
  font-size: var(--font-size-lg);
  color: #ffffff;
}

.validation-error {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  color: #ffffff;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  margin-top: var(--spacing-2);
  padding: var(--spacing-2) var(--spacing-3);
  background: linear-gradient(135deg, rgba(255, 77, 79, 0.9), rgba(255, 120, 117, 0.9));
  border-radius: var(--radius-md);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 
    0 2px 8px rgba(255, 77, 79, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  animation: errorShake 0.5s ease-out;
}

.validation-error .error-icon,
.validation-error el-icon {
  font-size: var(--font-size-lg);
  color: #ffffff;
}

/* 成功提示脉冲动画 */
@keyframes successPulse {
  0% {
    opacity: 0;
    transform: scale(0.9);
  }
  
  50% {
    transform: scale(1.05);
  }
  
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 错误提示抖动动画 */
@keyframes errorShake {
  0%, 100% {
    opacity: 1;
    transform: translateX(0);
  }
  
  10%, 30%, 50%, 70%, 90% {
    transform: translateX(-5px);
  }
  
  20%, 40%, 60%, 80% {
    transform: translateX(5px);
  }
}

/* 🎨 密码强度指示器 - 简洁实用 */
.password-strength {
  display: flex !important;
  visibility: visible !important;
  opacity: 1 !important;
  align-items: center;
  gap: var(--spacing-2);  /* 🎨 减小间距 */
  margin: var(--spacing-2) 0 var(--spacing-3);  /* 🎨 减小间距 */
  padding: var(--spacing-2) var(--spacing-3);  /* 🎨 减小内边距 */
  background: rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-md);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.strength-label {
  color: #ffffff !important;
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
  white-space: nowrap;
}

.strength-bar {
  flex: 1;
  height: 6px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 3px;
  overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.2);
}

.strength-fill {
  height: 100%;
  border-radius: 3px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 2px;
}

.strength-fill.weak {
  background: linear-gradient(90deg, #ff4d4f, #ff7875);
  box-shadow: 0 0 8px rgba(255, 77, 79, 0.5);
}

.strength-fill.medium {
  background: linear-gradient(90deg, #faad14, #ffc53d);
  box-shadow: 0 0 8px rgba(250, 173, 20, 0.5);
}

.strength-fill.strong {
  background: linear-gradient(90deg, #52c41a, #73d13d);
  box-shadow: 0 0 8px rgba(82, 196, 26, 0.5);
}

.strength-text {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-bold);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
  white-space: nowrap;
  min-width: 40px;
  text-align: center;
}

.strength-text.weak {
  color: #ff7875 !important;
}

.strength-text.medium {
  color: #ffc53d !important;
}

.strength-text.strong {
  color: #73d13d !important;
}

/* 管理员密钥区域 */
.admin-key-section {
  padding: var(--spacing-4);
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: var(--spacing-5);
}

.admin-divider {
  margin: var(--spacing-4) 0;
  border-color: rgba(255, 255, 255, 0.2);
}

/* 🎬 管理员密钥弹出动画 - 弹跳+发光效果 */
.admin-key-slide-enter-active {
  animation: 
    adminKeyPop 0.6s cubic-bezier(0.34, 1.56, 0.64, 1),
    adminKeyGlow 0.6s ease-out;
}

.admin-key-slide-leave-active {
  animation: adminKeyFadeOut 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 弹出动画 */
@keyframes adminKeyPop {
  0% {
    opacity: 0;
    transform: translateY(-30px) scale(0.8);
    max-height: 0;
  }
  
  60% {
    opacity: 1;
    transform: translateY(5px) scale(1.05);
  }
  
  80% {
    transform: translateY(-2px) scale(0.98);
  }
  
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
    max-height: 500px;
  }
}

/* 发光动画 */
@keyframes adminKeyGlow {
  0% {
    box-shadow: 
      0 0 0 rgba(64, 158, 255, 0),
      0 0 0 rgba(64, 158, 255, 0);
  }
  
  50% {
    box-shadow: 
      0 0 20px rgba(64, 158, 255, 0.6),
      0 0 40px rgba(64, 158, 255, 0.3),
      inset 0 0 20px rgba(64, 158, 255, 0.1);
  }
  
  100% {
    box-shadow: 
      0 0 0 rgba(64, 158, 255, 0),
      0 0 0 rgba(64, 158, 255, 0);
  }
}

/* 消失动画 */
@keyframes adminKeyFadeOut {
  0% {
    opacity: 1;
    transform: translateY(0) scale(1);
    max-height: 500px;
  }
  
  100% {
    opacity: 0;
    transform: translateY(-20px) scale(0.9);
    max-height: 0;
  }
}

/* 响应式优化 */
@media (max-width: 768px) {
  .auth-form-content .el-form-item {
    margin-bottom: var(--spacing-4);
  }
  
  .auth-form-content :deep(.el-radio-group) {
    flex-direction: column;
    gap: var(--spacing-2);
  }
  
  .password-strength {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-2);
  }
  
  .strength-bar {
    width: 100%;
  }
}

/* 现代化按钮 - 磨玻璃风格 */
.auth-btn {
  width: 100%;
  background: linear-gradient(135deg, 
    rgba(64, 158, 255, 0.9) 0%, 
    rgba(102, 177, 255, 0.9) 100%
  );
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(64, 158, 255, 0.4);
  border-radius: var(--radius-md);
  height: 40px;  /* 🎨 减小按钮高度 */
  font-size: var(--font-size-base);  /* 🎨 减小字体 */
  font-weight: var(--font-weight-semibold);
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  box-shadow: 
    0 4px 16px rgba(64, 158, 255, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition: all var(--duration-normal) var(--easing-ease);
  position: relative;
  overflow: hidden;
  margin: 0 auto;
  display: block;
}

.auth-btn:hover {
  background: linear-gradient(135deg, 
    rgba(64, 158, 255, 1) 0%, 
    rgba(102, 177, 255, 1) 100%
  );
  transform: translateY(-2px);
  box-shadow: 
    0 8px 24px rgba(64, 158, 255, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

/* 按钮容器居中 */
.auth-form-content .el-form-item:has(.auth-btn) {
  text-align: center;
}

/* 兼容性：如果不支持:has选择器，使用类名 */
.auth-form-content .el-form-item.button-item {
  text-align: center;
}

/* 现代化登录按钮 - 磨玻璃风格 */
.login-btn {
  width: 100%;
  background: linear-gradient(135deg, 
    rgba(64, 158, 255, 0.9) 0%, 
    rgba(102, 177, 255, 0.9) 100%
  );
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(64, 158, 255, 0.4);
  border-radius: var(--radius-md);
  height: 48px;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  box-shadow: 
    0 4px 16px rgba(64, 158, 255, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  transition: all var(--duration-normal) var(--easing-ease);
  position: relative;
  overflow: hidden;
}

.login-btn:hover {
  background: linear-gradient(135deg, 
    rgba(64, 158, 255, 1) 0%, 
    rgba(102, 177, 255, 1) 100%
  );
  transform: translateY(-2px);
  box-shadow: 
    0 8px 24px rgba(64, 158, 255, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

/* 现代化注册链接 - 纯白色字体 */
.register-link {
  text-align: center;
  margin-top: var(--spacing-5);
  color: #ffffff;
  font-size: var(--font-size-sm);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.register-link .link {
  color: #ffffff;
  text-decoration: none;
  font-weight: var(--font-weight-semibold);
  margin-left: var(--spacing-1);
  transition: all var(--duration-normal) var(--easing-ease);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
}

.register-link .link:hover {
  color: #ffffff;
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.8);
  border-bottom-color: rgba(255, 255, 255, 1);
}



/* 精细化内容动画 - 仅在直接访问时生效 */
.login-form:not(.transitioning) .animate-title {
  animation: slideInDown 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.1s both;
}

.login-form:not(.transitioning) .animate-logo {
  animation: fadeInScale 0.8s cubic-bezier(0.4, 0, 0.2, 1) 0.05s both;
}

.login-form:not(.transitioning) .animate-subtitle {
  animation: slideInDown 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.2s both;
}

.login-form:not(.transitioning) .animate-form {
  animation: slideInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.3s both;
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* 表单项精细动画 - 仅在直接访问时生效 */
.login-form:not(.transitioning) .login-form-content .el-form-item:nth-child(1) {
  animation: fadeInLeft 0.4s cubic-bezier(0.4, 0, 0.2, 1) 0.4s both;
}

.login-form:not(.transitioning) .login-form-content .el-form-item:nth-child(2) {
  animation: fadeInLeft 0.4s cubic-bezier(0.4, 0, 0.2, 1) 0.5s both;
}

.login-form:not(.transitioning) .login-btn {
  animation: fadeInUp 0.4s cubic-bezier(0.4, 0, 0.2, 1) 0.6s both;
}

.login-form:not(.transitioning) .register-link {
  animation: fadeIn 0.4s cubic-bezier(0.4, 0, 0.2, 1) 0.7s both;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-15px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 🔧 高分辨率设备优化 */
@media (-webkit-min-device-pixel-ratio: 2), (min-resolution: 192dpi), (min-resolution: 2dppx) {
  .login-container {
    /* Retina 显示器优化 */
    image-rendering: -webkit-optimize-contrast;
    image-rendering: optimize-contrast;
    
    /* 更精确的背景定位 */
    background-size: cover;
    background-position: center center;
  }
}

@media (-webkit-min-device-pixel-ratio: 3), (min-resolution: 288dpi), (min-resolution: 3dppx) {
  .login-container {
    /* 超高分辨率设备优化 */
    image-rendering: pixelated;
    image-rendering: -moz-crisp-edges;
    image-rendering: crisp-edges;
  }
}

/* 响应式设计 - 移动端紧凑优化 */
@media (max-width: 480px) {
  .auth-container {
    /* 移动端背景优化 */
    background-attachment: scroll; /* 移动端使用scroll以提高性能 */
    background-size: cover;
    padding: 15px;  /* 减小外边距 */
  }
  
  .auth-form {
    padding: 15px 8px !important;  /* 减小左右内边距，为输入框留出margin空间 */
    max-width: 100% !important;
    margin: 0 auto;
  }
  
  /* 表单内容区域 - 增加左右间距 */
  .auth-form-content {
    padding: 0 12px !important;  /* 给表单内容增加左右间距 */
  }
  
  /* Logo 紧凑化 */
  .site-logo {
    width: 55px !important;  /* 进一步缩小Logo */
    height: 55px !important;
    margin-bottom: 10px !important;
  }
  
  /* 标题紧凑化 */
  .auth-title {
    font-size: 18px !important;  /* 进一步减小标题字体 */
    margin-bottom: 5px !important;
  }
  
  .auth-subtitle {
    font-size: 11px !important;  /* 进一步减小副标题字体 */
    margin-bottom: 16px !important;  /* 增加底部间距，避免被遮挡 */
    z-index: 2 !important;  /* 确保在最上层 */
  }
  
  /* 模式切换标签紧凑化 */
  .mode-tabs {
    margin-bottom: 12px !important;  /* 进一步减小间距 */
    padding: 0 8px !important;  /* 增加左右间距，让标签更窄 */
  }
  
  .mode-tab {
    padding: 6px 12px !important;  /* 进一步减小内边距 */
    font-size: 12px !important;  /* 进一步减小字体 */
  }
  
  /* 表单项紧凑化 */
  .auth-form-content :deep(.el-form-item) {
    margin-bottom: 10px !important;  /* 进一步减小表单项间距 */
  }
  
  .auth-form-content :deep(.el-form-item__label) {
    font-size: 11px !important;  /* 进一步减小标签字体 */
    margin-bottom: 3px !important;
  }
  
  /* 输入框紧凑化 - 减小宽度 */
  .auth-form-content :deep(.el-input__wrapper) {
    min-height: 36px !important;  /* 进一步减小输入框高度 */
    padding: 5px 10px !important;  /* 进一步减小内边距 */
  }
  
  .auth-form-content :deep(.el-input__inner) {
    font-size: 13px !important;  /* 进一步减小输入文字 */
    line-height: 1.3 !important;
  }
  
  .auth-form-content :deep(.el-input__prefix) {
    font-size: 13px !important;  /* 减小图标 */
  }
  
  /* 选择框紧凑化 */
  .auth-form-content :deep(.el-select) {
    font-size: 13px !important;
  }
  
  .auth-form-content :deep(.el-select .el-input__wrapper) {
    padding: 5px 10px !important;
  }
  
  /* 单选按钮紧凑化 - 保持横向排列 */
  .auth-form-content :deep(.el-radio-group) {
    display: flex !important;  /* 强制横向排列 */
    flex-direction: row !important;  /* 覆盖768px的纵向设置 */
    gap: 8px !important;  /* 减小间距 */
    flex-wrap: nowrap !important;
  }
  
  .auth-form-content :deep(.el-radio) {
    flex: 1 !important;  /* 平分宽度 */
    padding: 6px 8px !important;  /* 进一步减小内边距 */
    font-size: 12px !important;  /* 进一步减小字体 */
    margin: 0 !important;
  }
  
  .auth-form-content :deep(.el-radio__label) {
    font-size: 12px !important;
  }
  
  .auth-form-content :deep(.el-radio__inner) {
    width: 12px !important;  /* 减小单选圆圈 */
    height: 12px !important;
  }
  
  /* 管理员密钥区域紧凑化 */
  .admin-key-section {
    padding: 10px !important;  /* 进一步减小内边距 */
    margin-bottom: 10px !important;  /* 减小底部间距 */
  }
  
  .admin-key-section :deep(.el-input__wrapper) {
    min-height: 36px !important;
    padding: 5px 10px !important;
  }
  
  .admin-key-section :deep(.el-button) {
    height: 32px !important;  /* 验证按钮也缩小 */
    padding: 0 12px !important;
    font-size: 12px !important;
  }
  
  /* 密码强度指示器紧凑化 */
  .password-strength {
    margin: 6px 0 !important;  /* 进一步减小间距 */
    padding: 6px 8px !important;  /* 进一步减小内边距 */
  }
  
  .strength-label,
  .strength-text {
    font-size: 10px !important;  /* 进一步减小字体 */
  }
  
  .strength-bar {
    height: 3px !important;  /* 进一步减小高度 */
  }
  
  /* 按钮紧凑化 */
  .auth-btn {
    height: 38px !important;  /* 进一步减小按钮高度 */
    font-size: 13px !important;  /* 进一步减小字体 */
    margin-top: 12px !important;
  }
  
  /* 验证反馈紧凑化 */
  .validation-success,
  .validation-error {
    font-size: 11px !important;  /* 进一步减小字体 */
    padding: 5px 8px !important;  /* 进一步减小内边距 */
    margin-top: 4px !important;
  }
  
  .validation-success .el-icon,
  .validation-error .el-icon {
    font-size: 12px !important;  /* 减小图标 */
  }
  
  /* 两列布局改为单列 */
  .register-form :deep(.el-row) {
    display: block !important;
  }
  
  .register-form :deep(.el-col) {
    max-width: 100% !important;
    flex: 0 0 100% !important;
  }
  
  /* 成功对话框紧凑化 */
  .el-dialog {
    width: 92% !important;  /* 进一步充分利用宽度 */
    margin: 0 auto !important;
  }
  
  .el-dialog__header {
    padding: 12px !important;
  }
  
  .el-dialog__body {
    padding: 12px !important;
  }
  
  .el-result__title {
    font-size: 15px !important;
  }
  
  .el-result__subtitle {
    font-size: 12px !important;
  }
  
  .el-result__extra {
    margin-top: 15px !important;
  }
  
  .el-result__extra .el-button {
    height: 36px !important;
    font-size: 13px !important;
  }
}

/* 🔧 超宽屏优化 */
@media (min-width: 1920px) {
  .login-container {
    /* 4K及以上分辨率优化 */
    background-size: cover;
    background-position: center center;
    image-rendering: high-quality;
  }
}

/* Glass bento auth redesign */
.auth-container {
  display: grid !important;
  place-items: center !important;
  padding: clamp(18px, 4vw, 52px) !important;
  background:
    radial-gradient(circle at 14% 16%, rgba(24, 193, 242, 0.2), transparent 30%),
    radial-gradient(circle at 82% 10%, rgba(75, 211, 180, 0.18), transparent 28%),
    linear-gradient(135deg, #f6fdff 0%, #e9f9ff 48%, #f9fff7 100%) !important;
  filter: none !important;
}

.auth-container::before {
  content: '';
  position: fixed;
  inset: 0;
  background:
    linear-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.08) 1px, transparent 1px);
  background-size: 36px 36px;
  opacity: 0.18;
  pointer-events: none;
}

.auth-form {
  display: grid !important;
  grid-template-columns: minmax(240px, 0.82fr) minmax(320px, 1.18fr);
  gap: clamp(18px, 4vw, 38px);
  width: min(100%, 1040px) !important;
  max-width: 1040px !important;
  padding: clamp(22px, 4vw, 42px) !important;
  background: rgba(255, 255, 255, 0.78) !important;
  border: 1px solid rgba(255, 255, 255, 0.7) !important;
  border-radius: 28px !important;
  box-shadow: 0 34px 90px rgba(18, 174, 231, 0.16) !important;
  backdrop-filter: blur(28px) saturate(1.16) !important;
  -webkit-backdrop-filter: blur(28px) saturate(1.16) !important;
}

.auth-form:has(.register-form) {
  width: min(100%, 1120px) !important;
  max-width: 1120px !important;
}

.auth-form::before,
.auth-form::after {
  display: none !important;
}

.auth-header {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: clamp(18px, 3vw, 34px);
  min-height: 360px;
  text-align: left !important;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.94), rgba(225, 249, 255, 0.82)),
    radial-gradient(circle at 20% 12%, rgba(24, 193, 242, 0.2), transparent 36%);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 24px;
  overflow: hidden;
}

.auth-header::after {
  content: '';
  width: 112px;
  height: 112px;
  margin-top: 28px;
  border-radius: 28px;
  background:
    linear-gradient(rgba(255, 255, 255, 0.14) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.14) 1px, transparent 1px);
  background-size: 28px 28px;
  opacity: 0.8;
}

.logo-container {
  justify-content: flex-start !important;
}

.site-logo {
  width: 68px !important;
  height: 68px !important;
  margin: 0 0 22px !important;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 18px !important;
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.18) !important;
}

.auth-title {
  color: #123044 !important;
  font-size: clamp(30px, 4vw, 46px) !important;
  line-height: 1.08 !important;
  letter-spacing: 0 !important;
  margin-bottom: 14px !important;
  text-shadow: none !important;
}

.auth-subtitle {
  max-width: 320px;
  color: #496579 !important;
  font-size: 15px !important;
  line-height: 1.7 !important;
  text-shadow: none !important;
}

.mode-tabs,
.form-container {
  grid-column: 2;
}

.mode-tabs {
  align-self: start;
  background: rgba(255, 255, 255, 0.72) !important;
  border: 1px solid rgba(148, 163, 184, 0.22) !important;
  border-radius: 999px !important;
  padding: 5px !important;
  margin: 0 0 22px !important;
  box-shadow: none !important;
}

.mode-tab {
  min-height: 40px;
  color: #475569 !important;
  border-radius: 999px !important;
  font-weight: 700 !important;
  transition: color 0.2s ease, background 0.2s ease !important;
}

.mode-tab.active {
  color: #ffffff !important;
}

.tab-indicator {
  inset: 5px auto 5px 5px !important;
  width: calc(50% - 5px) !important;
  height: auto !important;
  background: linear-gradient(135deg, #18c1f2, #16a7e5 48%, #33caa8) !important;
  border-radius: 999px !important;
  box-shadow: 0 10px 24px rgba(24, 193, 242, 0.22) !important;
}

.tab-indicator.move-right {
  transform: translateX(calc(100% - 5px)) !important;
}

.auth-form-content {
  margin: 0 !important;
}

.auth-form-content :deep(.el-form-item) {
  margin-bottom: 16px !important;
}

.auth-form-content :deep(.el-form-item__label),
.form-label,
.strength-label {
  color: #475569 !important;
  font-weight: 700 !important;
  text-shadow: none !important;
}

.auth-form-content :deep(.el-input__wrapper),
.auth-form-content :deep(.el-select .el-input__wrapper) {
  min-height: 44px !important;
  background: rgba(255, 255, 255, 0.84) !important;
  border: 1px solid rgba(148, 163, 184, 0.28) !important;
  border-radius: 14px !important;
  box-shadow: none !important;
}

.auth-form-content :deep(.el-input__wrapper:hover),
.auth-form-content :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(24, 185, 236, 0.36) !important;
  transform: none !important;
}

.auth-form-content :deep(.el-input__wrapper.is-focus),
.auth-form-content :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: rgba(24, 185, 236, 0.58) !important;
  box-shadow: 0 0 0 4px rgba(24, 185, 236, 0.14) !important;
}

.auth-form-content :deep(.el-input__inner) {
  color: #0f172a !important;
  text-shadow: none !important;
}

.auth-form-content :deep(.el-input__inner::placeholder) {
  color: #64748b !important;
}

.auth-form-content :deep(.el-input__prefix-inner) {
  color: #64748b !important;
}

.auth-form-content :deep(.el-radio-group) {
  gap: 10px !important;
}

.auth-form-content :deep(.el-radio) {
  background: rgba(255, 255, 255, 0.72) !important;
  border: 1px solid rgba(148, 163, 184, 0.28) !important;
  border-radius: 14px !important;
  box-shadow: none !important;
}

.auth-form-content :deep(.el-radio.is-checked) {
  background: rgba(219, 234, 254, 0.72) !important;
  border-color: rgba(24, 185, 236, 0.38) !important;
}

.auth-form-content :deep(.el-radio__label),
.role-option {
  color: #0f172a !important;
  text-shadow: none !important;
}

.admin-key-section,
.password-strength {
  background: rgba(248, 250, 252, 0.78) !important;
  border: 1px solid rgba(148, 163, 184, 0.24) !important;
  border-radius: 16px !important;
  box-shadow: none !important;
}

.auth-btn {
  height: 44px !important;
  margin-top: 6px !important;
  background: linear-gradient(135deg, #18c1f2, #16a7e5 48%, #33caa8) !important;
  border: none !important;
  border-radius: 14px !important;
  box-shadow: 0 18px 38px rgba(24, 193, 242, 0.22) !important;
  text-shadow: none !important;
}

.auth-btn:hover {
  transform: translateY(-1px) !important;
  box-shadow: 0 20px 44px rgba(24, 193, 242, 0.28) !important;
}

.validation-success,
.validation-error {
  color: #0f172a !important;
  text-shadow: none !important;
  border-radius: 12px !important;
  box-shadow: none !important;
  animation: none !important;
}

.validation-success {
  background: rgba(16, 185, 129, 0.12) !important;
  border: 1px solid rgba(16, 185, 129, 0.22) !important;
}

.validation-error {
  background: rgba(244, 63, 94, 0.12) !important;
  border: 1px solid rgba(244, 63, 94, 0.22) !important;
}

.strength-bar {
  background: rgba(148, 163, 184, 0.24) !important;
}

.strength-text {
  text-shadow: none !important;
}

@media (max-width: 900px) {
  .auth-form {
    grid-template-columns: 1fr;
    width: min(100%, 620px) !important;
  }

  .auth-header,
  .mode-tabs,
  .form-container {
    grid-column: 1;
  }

  .auth-header {
    min-height: auto;
    padding: 24px;
  }

  .auth-header::after {
    display: none;
  }
}

@media (max-width: 480px) {
  .auth-container {
    padding: 12px !important;
  }

  .auth-form {
    padding: 14px !important;
    border-radius: 22px !important;
  }

  .auth-header {
    padding: 18px !important;
    border-radius: 18px !important;
  }

  .auth-title {
    font-size: 24px !important;
  }

  .auth-subtitle {
    font-size: 13px !important;
  }

  .mode-tabs {
    margin-bottom: 14px !important;
    padding: 4px !important;
  }
}

/* Fresh QQ-mail style correction */
.auth-container {
  background:
    radial-gradient(circle at 9% 12%, rgba(24, 193, 242, 0.2), transparent 28%),
    radial-gradient(circle at 86% 14%, rgba(75, 211, 180, 0.18), transparent 28%),
    radial-gradient(circle at 80% 86%, rgba(255, 213, 106, 0.16), transparent 26%),
    linear-gradient(135deg, #f5fdff 0%, #e9f9ff 45%, #f9fff7 100%) !important;
}

.auth-container::before {
  background:
    linear-gradient(rgba(18, 174, 231, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(18, 174, 231, 0.08) 1px, transparent 1px) !important;
  opacity: 0.42 !important;
}

.auth-form {
  grid-template-columns: minmax(300px, 0.9fr) minmax(360px, 1.1fr) !important;
  grid-template-rows: auto 1fr !important;
  align-items: stretch !important;
  gap: 26px !important;
  width: min(100%, 960px) !important;
  min-height: 560px !important;
  padding: 26px !important;
  background: rgba(255, 255, 255, 0.7) !important;
  border: 1px solid rgba(255, 255, 255, 0.86) !important;
  box-shadow: 0 30px 90px rgba(18, 174, 231, 0.16) !important;
}

.auth-form:has(.register-form) {
  width: min(100%, 1120px) !important;
}

.auth-header {
  grid-column: 1 !important;
  grid-row: 1 / span 2 !important;
  justify-content: space-between !important;
  min-height: 508px !important;
  padding: 32px !important;
  background:
    radial-gradient(circle at 12% 10%, rgba(24, 193, 242, 0.22), transparent 34%),
    radial-gradient(circle at 86% 78%, rgba(75, 211, 180, 0.22), transparent 36%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.94), rgba(225, 249, 255, 0.82)) !important;
  border: 1px solid rgba(255, 255, 255, 0.86) !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9), 0 18px 44px rgba(18, 174, 231, 0.12) !important;
}

.auth-header::before {
  content: '';
  width: 76px;
  height: 5px;
  border-radius: 999px;
  background: linear-gradient(90deg, #18c1f2, #4bd3b4, #ffd56a);
  order: -1;
  margin-bottom: 24px;
}

.auth-header::after {
  width: 148px !important;
  height: 118px !important;
  margin-top: auto !important;
  background:
    linear-gradient(rgba(18, 174, 231, 0.16) 1px, transparent 1px),
    linear-gradient(90deg, rgba(18, 174, 231, 0.16) 1px, transparent 1px),
    linear-gradient(135deg, rgba(255, 255, 255, 0.68), rgba(216, 245, 255, 0.55)) !important;
  background-size: 26px 26px, 26px 26px, auto !important;
  border: 1px solid rgba(24, 185, 236, 0.18);
  opacity: 1 !important;
}

.site-logo {
  background: rgba(255, 255, 255, 0.84) !important;
  border: 1px solid rgba(24, 185, 236, 0.2) !important;
  box-shadow: 0 12px 28px rgba(18, 174, 231, 0.12) !important;
}

.auth-title {
  color: #123044 !important;
  font-size: clamp(30px, 3.4vw, 42px) !important;
  max-width: 320px;
}

.auth-subtitle {
  color: #496579 !important;
  max-width: 330px;
}

.mode-tabs {
  grid-column: 2 !important;
  grid-row: 1 !important;
  align-self: start !important;
  background: rgba(255, 255, 255, 0.74) !important;
  border: 1px solid rgba(98, 177, 210, 0.24) !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8), 0 10px 24px rgba(18, 174, 231, 0.08) !important;
}

.form-container {
  grid-column: 2 !important;
  grid-row: 2 !important;
  align-self: center !important;
  width: 100% !important;
}

.mode-tab {
  color: #496579 !important;
}

.mode-tab.active {
  color: #ffffff !important;
}

.tab-indicator {
  background: linear-gradient(135deg, #18c1f2, #16a7e5 48%, #33caa8) !important;
  box-shadow: 0 12px 28px rgba(24, 193, 242, 0.22) !important;
}

.auth-form-content :deep(.el-input__wrapper),
.auth-form-content :deep(.el-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.92) !important;
  border-color: rgba(98, 177, 210, 0.24) !important;
}

.auth-form-content :deep(.el-input__wrapper:hover),
.auth-form-content :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(24, 185, 236, 0.42) !important;
}

.auth-form-content :deep(.el-input__wrapper.is-focus),
.auth-form-content :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: rgba(24, 185, 236, 0.66) !important;
  box-shadow: 0 0 0 4px rgba(24, 185, 236, 0.13) !important;
}

.auth-btn {
  background: linear-gradient(135deg, #18c1f2, #16a7e5 48%, #33caa8) !important;
  box-shadow: 0 16px 34px rgba(24, 193, 242, 0.22) !important;
}

.auth-btn:hover {
  box-shadow: 0 18px 40px rgba(24, 193, 242, 0.28) !important;
}

@media (max-width: 900px) {
  .auth-form {
    grid-template-columns: 1fr !important;
    grid-template-rows: auto auto auto !important;
    min-height: auto !important;
  }

  .auth-header {
    grid-column: 1 !important;
    grid-row: 1 !important;
    min-height: 220px !important;
  }

  .mode-tabs {
    grid-column: 1 !important;
    grid-row: 2 !important;
  }

  .form-container {
    grid-column: 1 !important;
    grid-row: 3 !important;
  }
}

/* Compact auth layout final pass */
.auth-container {
  padding: clamp(18px, 5vw, 42px) !important;
}

.auth-form {
  grid-template-columns: minmax(320px, 0.9fr) minmax(360px, 1.1fr) !important;
  grid-template-rows: auto auto !important;
  align-items: stretch !important;
  align-content: center !important;
  column-gap: clamp(24px, 4vw, 34px) !important;
  row-gap: 22px !important;
  width: min(100%, 1080px) !important;
  min-height: auto !important;
  padding: clamp(22px, 3vw, 32px) !important;
}

.auth-form:has(.register-form) {
  width: min(100%, 1160px) !important;
}

.auth-header {
  min-height: 420px !important;
  padding: clamp(24px, 3vw, 34px) !important;
}

.auth-header::before {
  margin-bottom: 18px !important;
}

.auth-header::after {
  width: 132px !important;
  height: 106px !important;
}

.site-logo {
  width: 62px !important;
  height: 62px !important;
  margin-bottom: 18px !important;
}

.auth-title {
  font-size: clamp(30px, 3.1vw, 40px) !important;
  line-height: 1.12 !important;
}

.auth-subtitle {
  font-size: 15px !important;
  line-height: 1.55 !important;
}

.mode-tabs {
  align-self: end !important;
  margin: 0 !important;
}

.form-container {
  align-self: start !important;
  width: 100% !important;
  padding: clamp(10px, 2vw, 18px) 0 0 !important;
}

.auth-form-content {
  display: flex;
  flex-direction: column;
  gap: 0 !important;
}

.auth-form-content :deep(.el-form-item) {
  margin-bottom: 18px !important;
}

.auth-form-content :deep(.el-input__wrapper),
.auth-form-content :deep(.el-select .el-input__wrapper) {
  min-height: 48px !important;
  border-radius: 14px !important;
}

.auth-btn {
  height: 50px !important;
  margin-top: 2px !important;
}

.auth-form-content .el-form-item.button-item {
  margin-bottom: 0 !important;
}

.register-form {
  display: block !important;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 14px !important;
}

@media (min-width: 901px) {
  .auth-form:not(:has(.register-form)) .form-container {
    padding-top: 18px !important;
  }

  .auth-form:not(:has(.register-form)) .auth-form-content {
    max-width: 500px;
    margin: 0 auto !important;
  }
}

@media (max-width: 900px) {
  .auth-form {
    grid-template-columns: 1fr !important;
    grid-template-rows: auto auto auto !important;
    row-gap: 16px !important;
    width: min(100%, 640px) !important;
  }

  .auth-header {
    min-height: auto !important;
  }

  .mode-tabs {
    align-self: stretch !important;
  }

  .form-container {
    padding-top: 0 !important;
  }
}

/* Move the site logo into the bento visual block */
.auth-header::after {
  display: none !important;
}

.logo-container {
  order: 4 !important;
  width: 168px !important;
  height: 126px !important;
  margin: auto 0 0 !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  border-radius: 30px !important;
  background:
    linear-gradient(rgba(18, 174, 231, 0.15) 1px, transparent 1px),
    linear-gradient(90deg, rgba(18, 174, 231, 0.15) 1px, transparent 1px),
    linear-gradient(135deg, rgba(255, 255, 255, 0.74), rgba(216, 245, 255, 0.58)) !important;
  background-size: 28px 28px, 28px 28px, auto !important;
  border: 1px solid rgba(24, 185, 236, 0.22) !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.86), 0 18px 34px rgba(18, 174, 231, 0.12) !important;
  overflow: hidden !important;
}

.site-logo {
  width: 100% !important;
  height: 100% !important;
  max-width: none !important;
  max-height: none !important;
  margin: 0 !important;
  padding: 0 !important;
  object-fit: cover !important;
  border-radius: inherit !important;
  background: transparent !important;
  border: 0 !important;
  box-shadow: none !important;
}

@media (max-width: 900px) {
  .logo-container {
    width: 132px !important;
    height: 98px !important;
    margin-top: 18px !important;
  }

  .site-logo {
    width: 100% !important;
    height: 100% !important;
    max-width: none !important;
    max-height: none !important;
    padding: 0 !important;
  }
}

/* Register page layout refinement */
.auth-form:has(.register-form) {
  grid-template-columns: minmax(310px, 0.86fr) minmax(520px, 1.14fr) !important;
  column-gap: clamp(22px, 3vw, 30px) !important;
  width: min(100%, 1180px) !important;
  padding: clamp(18px, 2.4vw, 28px) !important;
  align-items: stretch !important;
}

.auth-form:has(.register-form) .auth-header {
  justify-content: flex-start !important;
  min-height: 100% !important;
  padding: clamp(28px, 3vw, 38px) !important;
}

.auth-form:has(.register-form) .auth-title {
  font-size: clamp(38px, 4.1vw, 52px) !important;
  margin-bottom: 16px !important;
}

.auth-form:has(.register-form) .auth-subtitle {
  margin-bottom: 0 !important;
}

.auth-form:has(.register-form) .logo-container {
  width: 230px !important;
  height: 140px !important;
  margin: clamp(42px, 6vh, 68px) 0 0 !important;
  border-radius: 32px !important;
}

.auth-form:has(.register-form) .mode-tabs {
  align-self: start !important;
  width: 100% !important;
}

.auth-form:has(.register-form) .form-container {
  align-self: start !important;
  padding: clamp(18px, 2.4vw, 28px) !important;
  overflow: visible !important;
  background: rgba(255, 255, 255, 0.78) !important;
  border: 1px solid rgba(255, 255, 255, 0.88) !important;
  border-radius: 28px !important;
  box-shadow: 0 22px 54px rgba(18, 174, 231, 0.1) !important;
}

.auth-form:has(.register-form) .register-form {
  display: block !important;
}

.auth-form:has(.register-form) .register-form :deep(.el-row) {
  margin-left: -8px !important;
  margin-right: -8px !important;
}

.auth-form:has(.register-form) .register-form :deep(.el-col) {
  padding-left: 8px !important;
  padding-right: 8px !important;
}

.auth-form:has(.register-form) .register-form :deep(.el-form-item) {
  margin-bottom: 12px !important;
}

.auth-form:has(.register-form) .register-form :deep(.el-form-item__label),
.auth-form:has(.register-form) .form-label {
  color: #31556e !important;
  font-size: 13px !important;
  font-weight: 750 !important;
  line-height: 1.2 !important;
  margin-bottom: 8px !important;
  text-shadow: none !important;
}

.auth-form:has(.register-form) .register-form :deep(.el-input__wrapper),
.auth-form:has(.register-form) .register-form :deep(.el-select .el-input__wrapper) {
  min-height: 44px !important;
  height: 44px !important;
  border-radius: 14px !important;
  background: rgba(255, 255, 255, 0.94) !important;
  border-color: rgba(98, 177, 210, 0.22) !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.86) !important;
}

.auth-form:has(.register-form) .register-form :deep(.el-input__inner) {
  height: 40px !important;
  color: #123044 !important;
}

.auth-form:has(.register-form) .register-form :deep(.el-input__inner::placeholder) {
  color: #7f9ab0 !important;
}

.auth-form:has(.register-form) .register-form :deep(.el-input__prefix-inner) {
  color: #5b7890 !important;
}

.auth-form:has(.register-form) .register-form :deep(.el-radio-group) {
  display: grid !important;
  grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  gap: 12px !important;
  width: 100% !important;
  margin: 0 !important;
}

.auth-form:has(.register-form) .register-form :deep(.el-radio) {
  height: 42px !important;
  min-width: 0 !important;
  padding: 0 14px !important;
  background: rgba(255, 255, 255, 0.86) !important;
  border: 1px solid rgba(98, 177, 210, 0.22) !important;
  border-radius: 16px !important;
  box-shadow: none !important;
}

.auth-form:has(.register-form) .register-form :deep(.el-radio.is-checked) {
  background: linear-gradient(135deg, rgba(225, 247, 255, 0.94), rgba(231, 255, 248, 0.92)) !important;
  border-color: rgba(24, 185, 236, 0.54) !important;
  box-shadow: 0 10px 22px rgba(24, 185, 236, 0.12) !important;
}

.auth-form:has(.register-form) .register-form :deep(.el-radio__label),
.auth-form:has(.register-form) .role-option {
  color: #123044 !important;
  font-weight: 750 !important;
  text-shadow: none !important;
}

.email-code-row {
  margin-bottom: 2px !important;
}

.email-code-control {
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) 138px !important;
  gap: 12px !important;
  width: 100% !important;
  align-items: stretch !important;
}

.email-code-button {
  height: 44px !important;
  min-width: 0 !important;
  padding: 0 14px !important;
  border-radius: 14px !important;
  color: #0f7596 !important;
  font-weight: 800 !important;
  background: linear-gradient(135deg, rgba(225, 247, 255, 0.96), rgba(230, 255, 247, 0.94)) !important;
  border: 1px solid rgba(24, 185, 236, 0.34) !important;
  box-shadow: 0 10px 24px rgba(18, 174, 231, 0.1) !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 6px !important;
}

.email-code-button:hover:not(.is-disabled) {
  color: #075f7d !important;
  border-color: rgba(24, 185, 236, 0.58) !important;
  box-shadow: 0 14px 30px rgba(18, 174, 231, 0.16) !important;
}

.email-code-button.is-disabled {
  color: #8aa4b8 !important;
  background: rgba(246, 251, 253, 0.88) !important;
  border-color: rgba(148, 163, 184, 0.22) !important;
  box-shadow: none !important;
}

.auth-form:has(.register-form) .password-strength {
  margin: 0 0 12px !important;
  padding: 10px 12px !important;
}

.auth-form:has(.register-form) .auth-btn {
  height: 48px !important;
  margin-top: 4px !important;
}

@media (max-width: 1100px) {
  .auth-form:has(.register-form) {
    grid-template-columns: minmax(280px, 0.8fr) minmax(480px, 1.2fr) !important;
  }

  .auth-form:has(.register-form) .logo-container {
    width: 190px !important;
    height: 118px !important;
  }
}

@media (max-width: 900px) {
  .auth-form:has(.register-form) {
    grid-template-columns: 1fr !important;
    width: min(100%, 680px) !important;
  }

  .auth-form:has(.register-form) .auth-header {
    min-height: auto !important;
  }

  .auth-form:has(.register-form) .logo-container {
    width: 160px !important;
    height: 100px !important;
    margin-top: 20px !important;
  }

  .auth-form:has(.register-form) .form-container {
    padding: 18px !important;
  }
}

@media (max-width: 640px) {
  .auth-form:has(.register-form) .register-form :deep(.el-radio-group),
  .email-code-control {
    grid-template-columns: 1fr !important;
  }

  .email-code-button {
    width: 100% !important;
  }
}

.home-link {
  position: fixed;
  z-index: 20;
  top: clamp(16px, 3vw, 28px);
  left: clamp(16px, 3vw, 32px);
  min-height: 42px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  color: #123044;
  font-size: 14px;
  font-weight: 800;
  line-height: 1;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(98, 177, 210, 0.22);
  border-radius: 999px;
  box-shadow: 0 14px 34px rgba(18, 174, 231, 0.12);
  backdrop-filter: blur(18px) saturate(1.1);
  -webkit-backdrop-filter: blur(18px) saturate(1.1);
  transition: color 180ms ease, background 180ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.home-link .el-icon {
  color: #18a9d0;
  font-size: 17px;
}

.home-link:hover,
.home-link:focus-visible {
  color: #075f7d;
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(24, 185, 236, 0.42);
  box-shadow: 0 18px 40px rgba(18, 174, 231, 0.18);
}

@media (max-width: 640px) {
  .home-link {
    top: 12px;
    left: 12px;
    min-height: 38px;
    padding: 0 13px;
    font-size: 13px;
  }
}

/* Auth harmony final pass */
:global(#app .auth-container) {
  min-height: 100vh !important;
  display: grid !important;
  place-items: center !important;
  padding: clamp(24px, 5vw, 52px) clamp(16px, 5vw, 48px) !important;
  overflow: auto !important;
  filter: none !important;
  background:
    radial-gradient(circle at 12% 14%, rgba(37, 184, 242, 0.18), transparent 30%),
    radial-gradient(circle at 84% 12%, rgba(98, 214, 189, 0.15), transparent 28%),
    radial-gradient(circle at 78% 88%, rgba(255, 210, 111, 0.12), transparent 30%),
    linear-gradient(135deg, #fbfdff 0%, #edf8ff 48%, #fbfff8 100%) !important;
}

:global(#app .auth-form) {
  display: grid !important;
  grid-template-columns: minmax(300px, 0.92fr) minmax(420px, 1.08fr) !important;
  grid-template-rows: auto 1fr !important;
  align-items: stretch !important;
  gap: clamp(20px, 3vw, 30px) !important;
  width: min(100%, 1060px) !important;
  max-width: none !important;
  max-height: none !important;
  overflow: visible !important;
  padding: clamp(20px, 3vw, 30px) !important;
  background: rgba(255, 255, 255, 0.78) !important;
  border: 1px solid rgba(255, 255, 255, 0.82) !important;
  border-radius: 28px !important;
  box-shadow: 0 34px 90px rgba(18, 85, 116, 0.14) !important;
  backdrop-filter: blur(28px) saturate(1.12) !important;
  -webkit-backdrop-filter: blur(28px) saturate(1.12) !important;
}

:global(#app .auth-form:has(.register-form)) {
  grid-template-columns: minmax(300px, 0.88fr) minmax(590px, 1.12fr) !important;
  width: min(100%, 1120px) !important;
}

:global(#app .auth-form::before),
:global(#app .auth-form::after) {
  display: none !important;
}

:global(#app .auth-header) {
  grid-column: 1 !important;
  grid-row: 1 / span 2 !important;
  display: flex !important;
  flex-direction: column !important;
  align-items: flex-start !important;
  justify-content: space-between !important;
  min-height: 430px !important;
  margin: 0 !important;
  padding: clamp(28px, 3.2vw, 38px) !important;
  text-align: left !important;
  overflow: hidden !important;
  background:
    radial-gradient(circle at 12% 10%, rgba(37, 184, 242, 0.18), transparent 36%),
    radial-gradient(circle at 88% 84%, rgba(98, 214, 189, 0.2), transparent 38%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.95), rgba(229, 248, 255, 0.8)) !important;
  border: 1px solid rgba(255, 255, 255, 0.9) !important;
  border-radius: 24px !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.92), 0 18px 44px rgba(18, 174, 231, 0.1) !important;
}

:global(#app .auth-header::before) {
  content: "" !important;
  display: block !important;
  flex: 0 0 auto !important;
  order: -1 !important;
  width: 86px !important;
  height: 5px !important;
  margin: 0 0 clamp(28px, 5vw, 54px) !important;
  border-radius: 999px !important;
  background: linear-gradient(90deg, #25b8f2, #62d6bd, #ffd26f) !important;
}

:global(#app .auth-header::after) {
  content: "" !important;
  display: block !important;
  flex: 0 0 auto !important;
  width: min(100%, 220px) !important;
  height: 132px !important;
  margin-top: auto !important;
  border: 1px solid rgba(37, 184, 242, 0.2) !important;
  border-radius: 24px !important;
  opacity: 1 !important;
  background:
    linear-gradient(rgba(18, 174, 231, 0.14) 1px, transparent 1px),
    linear-gradient(90deg, rgba(18, 174, 231, 0.14) 1px, transparent 1px),
    linear-gradient(135deg, rgba(255, 255, 255, 0.72), rgba(221, 247, 255, 0.58)) !important;
  background-size: 26px 26px, 26px 26px, auto !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.84), 0 18px 34px rgba(18, 174, 231, 0.1) !important;
}

:global(#app .auth-header:has(.logo-container)::after) {
  display: none !important;
}

:global(#app .auth-title) {
  max-width: 360px !important;
  margin: 0 0 16px !important;
  color: #12384f !important;
  font-size: clamp(32px, 4vw, 44px) !important;
  line-height: 1.12 !important;
  letter-spacing: 0 !important;
  text-shadow: none !important;
}

:global(#app .auth-subtitle) {
  max-width: 340px !important;
  margin: 0 !important;
  color: #4f6d82 !important;
  font-size: 15px !important;
  line-height: 1.65 !important;
  text-shadow: none !important;
}

:global(#app .logo-container) {
  order: 4 !important;
  width: min(100%, 220px) !important;
  height: 132px !important;
  margin: auto 0 0 !important;
  border-radius: 24px !important;
}

:global(#app .site-logo) {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
  border-radius: inherit !important;
}

:global(#app .mode-tabs) {
  grid-column: 2 !important;
  grid-row: 1 !important;
  align-self: end !important;
  width: min(100%, 540px) !important;
  height: 58px !important;
  margin: 0 auto !important;
  padding: 5px !important;
  background: rgba(255, 255, 255, 0.78) !important;
  border: 1px solid rgba(98, 177, 210, 0.24) !important;
  border-radius: 999px !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8), 0 12px 26px rgba(18, 174, 231, 0.08) !important;
}

:global(#app .mode-tab) {
  height: 48px !important;
  color: #4f6d82 !important;
  font-size: 15px !important;
  font-weight: 800 !important;
  border-radius: 999px !important;
  transition: color 180ms ease, background 180ms ease !important;
}

:global(#app .mode-tab.active) {
  color: #075985 !important;
  text-shadow: none !important;
}

:global(#app .tab-indicator) {
  inset: 5px auto 5px 5px !important;
  width: calc(50% - 5px) !important;
  height: auto !important;
  background: linear-gradient(135deg, #e2f8ff 0%, #c8efff 52%, #dcfff4 100%) !important;
  border: 1px solid rgba(37, 184, 242, 0.24) !important;
  border-radius: 999px !important;
  box-shadow: 0 10px 22px rgba(18, 174, 231, 0.12) !important;
}

:global(#app .tab-indicator.move-right) {
  transform: translateX(100%) !important;
}

:global(#app .form-container) {
  grid-column: 2 !important;
  grid-row: 2 !important;
  align-self: center !important;
  width: 100% !important;
  padding: 0 !important;
  overflow: visible !important;
  background: transparent !important;
  border: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
}

:global(#app .auth-form-content) {
  width: min(100%, 540px) !important;
  margin: 0 auto !important;
}

:global(#app .auth-form-content .el-form-item) {
  margin-bottom: 18px !important;
}

:global(#app .auth-form-content .el-input__wrapper),
:global(#app .auth-form-content .el-select .el-input__wrapper) {
  min-height: 50px !important;
  border-radius: 14px !important;
  background: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid rgba(98, 177, 210, 0.22) !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.86) !important;
}

:global(#app .auth-form-content .el-input__wrapper:hover),
:global(#app .auth-form-content .el-input__wrapper.is-focus) {
  border-color: rgba(37, 184, 242, 0.52) !important;
  box-shadow: 0 0 0 4px rgba(37, 184, 242, 0.12) !important;
}

:global(#app .password-actions) {
  margin: -4px 0 24px !important;
  color: #087fc4 !important;
  font-weight: 700 !important;
}

:global(#app .auth-btn) {
  width: 100% !important;
  height: 50px !important;
  margin-top: 2px !important;
  color: #075985 !important;
  font-size: 16px !important;
  font-weight: 850 !important;
  background: linear-gradient(135deg, #e2f8ff 0%, #c8efff 52%, #dcfff4 100%) !important;
  border: 1px solid rgba(37, 184, 242, 0.38) !important;
  border-radius: 14px !important;
  box-shadow: 0 14px 30px rgba(18, 174, 231, 0.14) !important;
}

:global(#app .auth-btn:hover),
:global(#app .auth-btn:focus-visible) {
  color: #056f9a !important;
  border-color: rgba(37, 184, 242, 0.54) !important;
  box-shadow: 0 18px 38px rgba(18, 174, 231, 0.18) !important;
}

:global(#app .auth-form:has(.register-form) .auth-header) {
  min-height: 430px !important;
}

:global(#app .auth-form:has(.register-form) .form-container) {
  align-self: center !important;
}

:global(#app .auth-form:has(.register-form) .auth-form-content) {
  width: 100% !important;
  max-width: none !important;
}

:global(#app .auth-form:has(.register-form) .register-form) {
  display: grid !important;
  grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  column-gap: 16px !important;
  row-gap: 0 !important;
  max-height: none !important;
  overflow: visible !important;
  padding: 0 !important;
  background: transparent !important;
  border: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  backdrop-filter: none !important;
  -webkit-backdrop-filter: none !important;
}

:global(#app .auth-form:has(.register-form) .register-form .el-row) {
  display: contents !important;
  margin-left: 0 !important;
  margin-right: 0 !important;
}

:global(#app .auth-form:has(.register-form) .register-form .el-col) {
  display: block !important;
  width: auto !important;
  max-width: none !important;
  flex: 0 0 auto !important;
  padding-left: 0 !important;
  padding-right: 0 !important;
}

:global(#app .auth-form:has(.register-form) .register-form .el-form-item) {
  margin-bottom: 14px !important;
}

:global(#app .auth-form:has(.register-form) .register-form .el-input__wrapper),
:global(#app .auth-form:has(.register-form) .register-form .el-select .el-input__wrapper) {
  min-height: 46px !important;
  height: 46px !important;
}

:global(#app .auth-form:has(.register-form) .register-form .full-width-item),
:global(#app .auth-form:has(.register-form) .register-form .button-item) {
  grid-column: 1 / -1 !important;
}

:global(#app .email-code-control) {
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) 128px !important;
  gap: 10px !important;
  width: 100% !important;
}

:global(#app .email-code-button) {
  width: 128px !important;
  height: 46px !important;
  padding: 0 10px !important;
  white-space: nowrap !important;
  border-radius: 14px !important;
}

:global(#app .email-code-button span) {
  overflow: hidden !important;
  text-overflow: ellipsis !important;
}

:global(#app .password-strength) {
  display: grid !important;
  grid-template-columns: auto minmax(0, 1fr) 32px !important;
  align-items: center !important;
  gap: 10px !important;
  margin: 0 0 14px !important;
  padding: 10px 12px !important;
  background: rgba(255, 255, 255, 0.72) !important;
  border: 1px solid rgba(98, 177, 210, 0.18) !important;
  border-radius: 14px !important;
}

@media (max-width: 980px) {
  :global(#app .auth-form),
  :global(#app .auth-form:has(.register-form)) {
    grid-template-columns: 1fr !important;
    grid-template-rows: auto auto auto !important;
    width: min(100%, 660px) !important;
  }

  :global(#app .auth-header),
  :global(#app .auth-form:has(.register-form) .auth-header) {
    grid-column: 1 !important;
    grid-row: 1 !important;
    min-height: auto !important;
  }

  :global(#app .auth-header::after),
  :global(#app .logo-container) {
    display: none !important;
  }

  :global(#app .mode-tabs) {
    grid-column: 1 !important;
    grid-row: 2 !important;
    width: 100% !important;
  }

  :global(#app .form-container) {
    grid-column: 1 !important;
    grid-row: 3 !important;
  }
}

@media (max-width: 680px) {
  :global(#app .auth-container) {
    place-items: start center !important;
    padding-top: 72px !important;
  }

  :global(#app .auth-form:has(.register-form) .register-form) {
    grid-template-columns: 1fr !important;
  }

  :global(#app .email-code-control) {
    grid-template-columns: 1fr !important;
  }

  :global(#app .email-code-button) {
    width: 100% !important;
  }

  :global(#app .password-strength) {
    grid-template-columns: 1fr !important;
    align-items: start !important;
  }
}
</style>
