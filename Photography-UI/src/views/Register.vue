<template>
  <div class="register-container" :style="containerStyle">
    <div class="register-form">
      <div class="register-header">
        <!-- 动态LOGO -->
        <div v-if="siteConfig.siteLogo" class="logo-container animate-logo">
          <img :src="siteConfig.siteLogo" :alt="siteConfig.siteTitle || '网站LOGO'" class="site-logo" />
        </div>
        <h2 class="register-title animate-title">用户注册</h2>
        <p class="register-subtitle animate-subtitle">创建您的融媒体管理系统账户</p>
      </div>
      
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form-content animate-form"
        @keyup.enter="handleRegister"
      >
        <!-- 注册类型选择 -->
        <el-form-item prop="role">
          <template #label>
            <span class="form-label">注册类型</span>
          </template>
          <el-radio-group v-model="registerForm.role" @change="handleRoleChange">
            <el-radio value="MEMBER">
              <div class="role-option">
                <el-icon><User /></el-icon>
                <span>成员注册</span>
              </div>
            </el-radio>
            <el-radio v-if="false" value="ADMIN">
              <div class="role-option">
                <el-icon><Setting /></el-icon>
                <span>管理员注册</span>
              </div>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 管理员密钥（仅管理员注册时显示） -->
        <transition name="admin-key-slide" appear>
          <div v-if="false" class="admin-key-section">
            <el-form-item prop="adminSecretKey">
              <template #label>
                <span class="form-label">管理员密钥</span>
              </template>
              <el-input
                v-model="registerForm.adminSecretKey"
                type="password"
                placeholder="请输入管理员注册密钥"
                size="large"
                show-password
                clearable
                @blur="validateAdminKey"
              >
                <template #prefix>
                  <el-icon><Key /></el-icon>
                </template>
              </el-input>
              <transition name="validation-fade" mode="out-in">
                <div v-if="adminKeyValidated" key="success" class="validation-success">
                  <el-icon class="success-icon"><CircleCheck /></el-icon>
                  <span class="success-text">密钥验证通过</span>
                </div>
                <div v-else-if="adminKeyError" key="error" class="validation-error">
                  <el-icon class="error-icon"><CircleClose /></el-icon>
                  <span class="error-text">{{ adminKeyError }}</span>
                </div>
              </transition>
            </el-form-item>
            
            <el-divider class="admin-divider" />
          </div>
        </transition>
        
        <!-- 基本信息 -->
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
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
                @input="handleUsernameInput"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
              <!-- 静态提示文字 -->
              <div class="input-hint">
                <el-icon><InfoFilled /></el-icon>
                <span>只能包含字母、数字、下划线，不能包含中文</span>
              </div>
              <!-- 用户名格式错误提示 -->
              <div v-if="usernameFormatError" class="validation-error">
                <el-icon><CircleClose /></el-icon>
                <span>{{ usernameFormatError }}</span>
              </div>
              <!-- 动态验证结果 -->
              <transition name="validation-fade" mode="out-in">
                <div v-if="usernameChecked && !usernameExists && !usernameFormatError" key="success" class="validation-success">
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
          
          <el-col :xs="24" :md="12">
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
        
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
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
                @input="handleEmailInput"
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
          
          <el-col :xs="24" :md="12">
            <el-form-item prop="departmentId">
              <template #label>
                <span class="form-label">所属部门</span>
              </template>
              <el-select
                v-model="registerForm.departmentId"
                placeholder="请选择所属部门"
                size="large"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="department in departments"
                  :key="department.id || department.name"
                  :label="department.name || '未知部门'"
                  :value="department.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <el-form-item prop="emailCode">
              <template #label>
                <span class="form-label">邮箱验证码</span>
              </template>
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
                <template #append>
                  <el-button
                    :loading="emailCodeSending"
                    :disabled="!canSendEmailCode"
                    @click="sendEmailCode"
                  >
                    {{ emailCodeCountdown > 0 ? `${emailCodeCountdown}s` : '发送验证码' }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
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
          
          <el-col :xs="24" :md="12">
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
        
        <!-- 密码强度指示器 -->
        <div class="password-strength" v-if="registerForm.password">
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
        
        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-btn"
            :loading="registering"
            :disabled="!canRegister"
            @click="handleRegister"
          >
            {{ registering ? '注册中...' : '立即注册' }}
          </el-button>
        </el-form-item>
        
        <!-- 登录链接 -->
        <div class="login-link">
          <span>已有账户？</span>
          <router-link to="/login" class="link">立即登录</router-link>
        </div>
      </el-form>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, 
  UserFilled, 
  Message, 
  Lock, 
  Key, 
  CircleCheck, 
  CircleClose,
  Setting,
  InfoFilled
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getSiteImageUrl } from '@/utils/imageUrl'

const router = useRouter()

// 站点配置数据
const siteConfig = reactive({
  siteLogo: '',
  loginBackground: '',
  siteTitle: '',
  loginTitle: '',
  loginWelcome: '',
  primaryColor: ''
})

// 响应式数据
const registering = ref(false)
const showSuccessDialog = ref(false)
const successMessage = ref('')
const departments = ref([])
const usernameChecked = ref(false)
const usernameExists = ref(false)
const usernameFormatError = ref('')
const emailChecked = ref(false)
const emailExists = ref(false)
const adminKeyValidated = ref(false)
const adminKeyError = ref('')
const emailCodeSending = ref(false)
const emailCodeCountdown = ref(0)
let emailCodeTimer = null

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  email: '',
  emailCode: '',
  role: 'MEMBER',
  departmentId: '',
  adminSecretKey: ''
})

// 容器样式计算
const containerStyle = computed(() => {
  return {
    background: siteConfig.loginBackground 
      ? `url(${siteConfig.loginBackground}) center center / cover no-repeat, var(--gradient-background)`
      : 'var(--gradient-background)'
  }
})

// 表单验证规则
const registerRules = {
  role: [
    { required: true, message: '请选择注册类型', trigger: 'change' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
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
    { min: 6, max: 50, message: '密码长度在 6 到 50 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  adminSecretKey: [
    {
      validator: (rule, value, callback) => {
        if (registerForm.role === 'ADMIN' && !value) {
          callback(new Error('请输入管理员密钥'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const registerFormRef = ref()

// 计算属性
const canRegister = computed(() => {
  const basicValid = registerForm.username && 
                    registerForm.password && 
                    registerForm.confirmPassword && 
                    registerForm.realName && 
                    registerForm.email &&
                    registerForm.emailCode &&
                    registerForm.password === registerForm.confirmPassword &&
                    !usernameExists.value &&
                    !usernameFormatError.value &&
                    !emailExists.value

  if (registerForm.role === 'ADMIN') {
    return basicValid && adminKeyValidated.value
  }
  
  return basicValid
})

const canSendEmailCode = computed(() => {
  return !emailCodeSending.value &&
    emailCodeCountdown.value === 0 &&
    registerForm.email &&
    isValidEmail(registerForm.email) &&
    !emailExists.value
})

// 密码强度计算
const passwordStrength = computed(() => {
  const password = registerForm.password
  if (!password) return 0
  
  let strength = 0
  
  // 长度检查
  if (password.length >= 8) strength += 1
  if (password.length >= 12) strength += 1
  
  // 字符类型检查
  if (/[a-z]/.test(password)) strength += 1
  if (/[A-Z]/.test(password)) strength += 1
  if (/[0-9]/.test(password)) strength += 1
  if (/[^a-zA-Z0-9]/.test(password)) strength += 1
  
  return Math.min(strength, 4)
})

const passwordStrengthWidth = computed(() => {
  return `${(passwordStrength.value / 4) * 100}%`
})

const passwordStrengthClass = computed(() => {
  const strength = passwordStrength.value
  if (strength <= 1) return 'weak'
  if (strength <= 2) return 'medium'
  if (strength <= 3) return 'strong'
  return 'very-strong'
})

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength <= 1) return '弱'
  if (strength <= 2) return '中等'
  if (strength <= 3) return '强'
  return '很强'
})

// 获取站点配置
const fetchSiteConfig = async () => {
  try {
    const response = await request.get('/site-config/public')
    if (response.data) {
      const configData = response.data
      siteConfig.siteLogo = getSiteImageUrl(configData['site.logo'])
      siteConfig.loginBackground = getSiteImageUrl(configData['login.background'])
      siteConfig.siteTitle = configData['site.title'] || ''
      siteConfig.loginTitle = configData['login.title'] || ''
      siteConfig.loginWelcome = configData['login.welcome'] || ''
      siteConfig.primaryColor = configData['theme.primary_color'] || ''
    }
  } catch (error) {
    console.log('获取站点配置失败，使用默认配置')
  }
}

// 获取部门列表
const fetchDepartments = async () => {
  try {
    console.log('开始获取部门列表...')
    console.log('请求URL: /departments/list')
    
    const response = await request.get('/departments/list')
    console.log('部门API响应:', response)
    
    if (response && response.data) {
      // 确保数据是数组格式
      if (Array.isArray(response.data)) {
        departments.value = response.data
        console.log('部门列表设置成功:', departments.value)
      } else {
        console.warn('部门数据格式不正确，期望数组格式:', response.data)
        setDefaultDepartments()
      }
    } else {
      // API返回了错误，使用默认部门
      console.warn('部门接口返回错误，使用默认部门列表:', response)
      setDefaultDepartments()
    }
  } catch (error) {
    console.error('获取部门列表失败:', error)
    console.log('错误类型:', error.constructor.name)
    console.log('错误消息:', error.message)
    
    // 网络错误或其他异常，使用默认部门
    setDefaultDepartments()
    
    // 显示友好的错误提示
    ElMessage.warning('无法获取部门列表，已使用默认部门选项')
  }
}

// 设置默认部门列表
const setDefaultDepartments = () => {
  departments.value = [
    { id: 1, name: '摄影部' },
    { id: 2, name: '采编部' },
    { id: 3, name: '审核部' },
    { id: 4, name: '宣传部' }
  ]
}

// 角色变化处理
const handleRoleChange = () => {
  adminKeyValidated.value = false
  adminKeyError.value = ''
  registerForm.adminSecretKey = ''
}

// 验证管理员密钥
const validateAdminKey = async () => {
  adminKeyValidated.value = false
  adminKeyError.value = '系统不支持管理员自助注册'
}

// 处理用户名输入：禁止中文，只允许字母、数字和下划线
const handleUsernameInput = (value) => {
  const hasChinese = /[\u4e00-\u9fa5]/.test(value)
  const filtered = value.replace(/[^a-zA-Z0-9_]/g, '')

  if (hasChinese) {
    usernameFormatError.value = '用户名不能包含中文'
    ElMessage.error('用户名不能包含中文')
  } else if (filtered !== value) {
    usernameFormatError.value = '用户名只能包含字母、数字和下划线'
    ElMessage.warning('用户名只能包含字母、数字和下划线')
  } else {
    usernameFormatError.value = ''
  }

  if (filtered !== registerForm.username) {
    registerForm.username = filtered
  }
}

const handleEmailInput = () => {
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

// 检查用户名是否存在
const checkUsername = async () => {
  if (!registerForm.username) {
    usernameChecked.value = false
    usernameExists.value = false
    return
  }
  
  try {
    // 若存在格式错误（包含中文或非法字符），提示并中断检查
    if (usernameFormatError.value) {
      ElMessage.error(usernameFormatError.value)
      usernameChecked.value = false
      return
    }
    // 这里可以调用后端接口检查用户名
    // 暂时使用模拟检查
    usernameChecked.value = true
    usernameExists.value = false // 假设不存在
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
}

// 检查邮箱是否存在
const checkEmail = async () => {
  if (!registerForm.email) {
    emailChecked.value = false
    emailExists.value = false
    return
  }

  if (!isValidEmail(registerForm.email)) {
    emailChecked.value = false
    emailExists.value = false
    return
  }
  
  try {
    const response = await request.get(`/auth/check-email?email=${registerForm.email}`)
    emailExists.value = response.data === true
    emailChecked.value = true
  } catch (error) {
    console.error('检查邮箱失败:', error)
    emailChecked.value = false
    emailExists.value = false
  }
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

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    registering.value = true
    
    const response = await request.post('/auth/register', registerForm)
    
    if (response.success !== false && response.data) {
      successMessage.value = `欢迎加入融媒体管理系统！您的${registerForm.role === 'ADMIN' ? '管理员' : '成员'}账户已创建成功。`
      showSuccessDialog.value = true
    } else {
      ElMessage.error(response.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败')
  } finally {
    registering.value = false
  }
}

// 跳转到登录页
const goToLogin = () => {
  showSuccessDialog.value = false
  router.push('/login')
}

// 组件挂载时获取配置和部门列表
onMounted(() => {
  fetchSiteConfig()
  fetchDepartments()
})

onUnmounted(() => {
  resetEmailCodeState()
})
</script>
<style scoped>
/* 现代化注册容器 */
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-5);
  position: relative;
  z-index: 1;
  background: var(--gradient-background);
  
  /* 背景优化 */
  background-attachment: fixed;
  background-size: cover;
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

/* 现代化注册表单 - 磨玻璃效果 */
.register-form {
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: var(--radius-xl);
  padding: var(--spacing-10);
  width: 100%;
  max-width: 650px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 2px 16px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  position: relative;
  overflow: hidden;
  animation: slideUp var(--duration-slow) var(--easing-spring);
}

.register-form::before {
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

.register-form::after {
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

.register-header {
  text-align: center;
  margin-bottom: var(--spacing-8);
}

.logo-container {
  display: flex;
  justify-content: center;
  margin-bottom: var(--spacing-5);
}

.site-logo {
  max-width: 120px;
  max-height: 80px;
  object-fit: contain;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-base);
}

/* 现代化标题样式 - 纯白色字体 */
.register-title {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: #ffffff;
  margin-bottom: var(--spacing-2);
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.register-subtitle {
  color: #ffffff;
  font-size: var(--font-size-base);
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

/* 现代化表单样式 */
.register-form-content {
  margin-bottom: var(--spacing-5);
}

.register-form-content .el-form-item {
  margin-bottom: var(--spacing-5);
}

.register-form-content :deep(.el-form-item__label) {
  color: #ffffff !important;
  font-weight: var(--font-weight-semibold);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  margin-bottom: var(--spacing-2) !important;
}

.form-label {
  color: #ffffff;
  font-weight: var(--font-weight-semibold);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.register-form-content :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: var(--radius-md);
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  transition: all var(--duration-normal) var(--easing-ease);
}

.register-form-content :deep(.el-input__wrapper:hover) {
  border-color: rgba(64, 158, 255, 0.6);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transform: translateY(-1px);
}

.register-form-content :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(64, 158, 255, 0.8);
  box-shadow: 
    0 0 0 3px rgba(64, 158, 255, 0.2),
    0 4px 12px rgba(64, 158, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.register-form-content :deep(.el-input__inner) {
  background: transparent;
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
}

.register-form-content :deep(.el-input__inner::placeholder) {
  color: var(--color-text-placeholder);
}

.register-form-content :deep(.el-input__prefix-inner) {
  color: var(--color-text-secondary);
}

/* 现代化选择框样式 */
.register-form-content :deep(.el-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: var(--radius-md);
  box-shadow: 
    0 2px 8px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  transition: all var(--duration-normal) var(--easing-ease);
}

.register-form-content :deep(.el-select .el-input__wrapper:hover) {
  border-color: rgba(64, 158, 255, 0.6);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transform: translateY(-1px);
}

.register-form-content :deep(.el-select .el-input__wrapper.is-focus) {
  border-color: rgba(64, 158, 255, 0.8);
  box-shadow: 
    0 0 0 3px rgba(64, 158, 255, 0.2),
    0 4px 12px rgba(64, 158, 255, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

/* 角色选择样式 */
.register-form-content :deep(.el-radio-group) {
  display: flex;
  gap: var(--spacing-4);
  flex-wrap: wrap;
}

.register-form-content :deep(.el-radio) {
  flex: 1;
  margin-right: 0 !important;
  min-width: 150px;
}

.role-option {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  padding: var(--spacing-3) var(--spacing-4);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all var(--duration-normal) var(--easing-ease);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.role-option::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.5s ease;
}

.role-option:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 255, 255, 0.1);
}

.role-option:hover::before {
  left: 100%;
}

.register-form-content :deep(.el-radio.is-checked) .role-option {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.15);
}

.role-option .el-icon {
  font-size: 18px;
  color: #ffffff;
  transition: all var(--duration-normal) var(--easing-ease);
}

.role-option:hover .el-icon {
  transform: scale(1.1);
}

/* 管理员密钥区域动画 */
.admin-key-section {
  overflow: hidden;
}

.admin-key-slide-enter-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.admin-key-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.admin-key-slide-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
  max-height: 0;
}

.admin-key-slide-enter-to {
  opacity: 1;
  transform: translateY(0) scale(1);
  max-height: 200px;
}

.admin-key-slide-leave-from {
  opacity: 1;
  transform: translateY(0) scale(1);
  max-height: 200px;
}

.admin-key-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
  max-height: 0;
}

/* 输入提示样式 */
.input-hint {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-size: var(--font-size-sm);
  margin-top: var(--spacing-2);
  padding: var(--spacing-1) 0;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.input-hint .el-icon {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

/* 验证状态样式 */
.validation-success,
.validation-error {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  font-size: var(--font-size-sm);
  margin-top: var(--spacing-2);
  padding: var(--spacing-1) 0;
}

.validation-success {
  color: var(--color-success);
}

.validation-error {
  color: var(--color-error);
}

.validation-fade-enter-active,
.validation-fade-leave-active {
  transition: all var(--duration-normal) var(--easing-ease);
}

.validation-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.9);
}

.validation-fade-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.9);
}

/* 密码强度指示器 */
.password-strength {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  margin-bottom: var(--spacing-5);
  font-size: var(--font-size-sm);
  padding: var(--spacing-2) 0;
}

.strength-label {
  color: #ffffff;
  white-space: nowrap;
  font-weight: var(--font-weight-medium);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.strength-bar {
  flex: 1;
  height: 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-sm);
  overflow: hidden;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.1);
}

.strength-fill {
  height: 100%;
  border-radius: var(--radius-sm);
  transition: all var(--duration-normal) var(--easing-ease);
}

.strength-fill.weak {
  background: var(--color-error);
}

.strength-fill.medium {
  background: var(--color-warning);
}

.strength-fill.strong {
  background: var(--color-info);
}

.strength-fill.very-strong {
  background: var(--color-success);
}

.strength-text {
  color: #ffffff;
  font-weight: var(--font-weight-medium);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.strength-text.weak {
  color: var(--color-error);
}

.strength-text.medium {
  color: var(--color-warning);
}

.strength-text.strong {
  color: var(--color-info);
}

.strength-text.very-strong {
  color: var(--color-success);
}

/* 现代化注册按钮 - 磨玻璃风格 */
.register-btn {
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

.register-btn:hover {
  background: linear-gradient(135deg, 
    rgba(64, 158, 255, 1) 0%, 
    rgba(102, 177, 255, 1) 100%
  );
  transform: translateY(-2px);
  box-shadow: 
    0 8px 24px rgba(64, 158, 255, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

/* 现代化登录链接 - 纯白色字体 */
.login-link {
  text-align: center;
  margin-top: var(--spacing-5);
  color: #ffffff;
  font-size: var(--font-size-sm);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.login-link .link {
  color: #ffffff;
  text-decoration: none;
  font-weight: var(--font-weight-semibold);
  margin-left: var(--spacing-1);
  transition: all var(--duration-normal) var(--easing-ease);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
}

.login-link .link:hover {
  color: #ffffff;
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.8);
  border-bottom-color: rgba(255, 255, 255, 1);
}

/* 分割线样式 */
.register-form-content :deep(.el-divider) {
  border-color: rgba(255, 255, 255, 0.2);
  margin: var(--spacing-5) 0;
}

/* 成功对话框样式 */
.success-content {
  text-align: center;
}

/* 动画定义 */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-container {
    padding: var(--spacing-3);
    align-items: flex-start;
    padding-top: var(--spacing-5);
  }
  
  .register-form {
    padding: var(--spacing-6);
    max-width: 100%;
    border-radius: var(--radius-lg);
  }
  
  .register-header {
    margin-bottom: var(--spacing-6);
  }
  
  .register-title {
    font-size: var(--font-size-2xl);
  }
  
  .register-subtitle {
    font-size: var(--font-size-sm);
  }
  
  .register-form-content .el-form-item {
    margin-bottom: var(--spacing-4);
  }
  
  .register-form-content :deep(.el-radio-group) {
    flex-direction: column;
  }
  
  .role-option {
    padding: var(--spacing-3);
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

@media (max-width: 480px) {
  .register-container {
    padding: var(--spacing-2);
    padding-top: var(--spacing-3);
  }
  
  .register-form {
    padding: var(--spacing-4);
    border-radius: var(--radius-md);
  }
  
  .register-header {
    margin-bottom: var(--spacing-4);
  }
  
  .register-title {
    font-size: var(--font-size-xl);
  }
  
  .register-form-content .el-form-item {
    margin-bottom: var(--spacing-3);
  }
  
  .register-btn {
    height: 44px;
    font-size: var(--font-size-base);
  }
}
</style>
