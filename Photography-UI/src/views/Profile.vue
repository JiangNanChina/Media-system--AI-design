<template>
  <div class="profile">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><User /></el-icon>
            个人中心
          </h1>
          <p class="page-subtitle">管理您的个人信息和账户设置</p>
        </div>
      </div>
      
      <!-- 装饰元素 -->
      <div class="header-decoration">
        <div class="decoration-circle decoration-circle-1"></div>
        <div class="decoration-circle decoration-circle-2"></div>
        <div class="decoration-circle decoration-circle-3"></div>
      </div>
    </div>
    
    <el-row :gutter="isMobile ? 16 : 20" class="profile-content">
      <!-- 个人信息 -->
      <el-col :xs="24" :sm="24" :md="24" :lg="8" :xl="8" class="profile-col">
        <el-card class="profile-card modern-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><User /></el-icon>
                个人信息
              </span>
            </div>
          </template>
          
          <div class="profile-info">
            <div class="avatar-section">
              <div class="avatar-container">
                <el-avatar :size="isMobile ? 60 : 80" :src="avatarUrl" class="profile-avatar">
                  <el-icon :size="isMobile ? 30 : 40"><UserFilled /></el-icon>
                </el-avatar>
                <div class="avatar-overlay" @click="showAvatarUpload = true">
                  <el-icon><Edit /></el-icon>
                </div>
              </div>
              <el-button type="text" @click="showAvatarUpload = true" class="upload-btn">
                更换头像
              </el-button>
            </div>
            
            <div class="info-section">
              <div class="info-grid" v-if="isMobile">
                <div class="info-card">
                  <div class="info-label">用户名</div>
                  <div class="info-value">{{ userInfo.username }}</div>
                </div>
                <div class="info-card">
                  <div class="info-label">真实姓名</div>
                  <div class="info-value">{{ userInfo.realName || '未设置' }}</div>
                </div>
                <div class="info-card">
                  <div class="info-label">邮箱</div>
                  <div class="info-value">{{ userInfo.email || '未设置' }}</div>
                </div>
                <div class="info-card">
                  <div class="info-label">角色</div>
                  <div class="info-value">
                    <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
                      {{ userInfo.role === 'ADMIN' ? '管理员' : '成员' }}
                    </el-tag>
                  </div>
                </div>
                <div class="info-card">
                  <div class="info-label">部门</div>
                  <div class="info-value">{{ userInfo.department?.name || userInfo.departmentName || '未分配' }}</div>
                </div>
                <div class="info-card">
                  <div class="info-label">注册时间</div>
                  <div class="info-value">{{ formatTime(userInfo.createdAt) }}</div>
                </div>
              </div>
              
              <div class="info-list" v-else>
                <div class="info-item">
                  <label>用户名：</label>
                  <span>{{ userInfo.username }}</span>
                </div>
                <div class="info-item">
                  <label>真实姓名：</label>
                  <span>{{ userInfo.realName || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <label>邮箱：</label>
                  <span>{{ userInfo.email || '未设置' }}</span>
                </div>
                <div class="info-item">
                  <label>角色：</label>
                  <el-tag :type="userInfo.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
                    {{ userInfo.role === 'ADMIN' ? '管理员' : '成员' }}
                  </el-tag>
                </div>
                <div class="info-item">
                  <label>部门：</label>
                  <span>{{ userInfo.department?.name || userInfo.departmentName || '未分配' }}</span>
                </div>
                <div class="info-item">
                  <label>注册时间：</label>
                  <span>{{ formatTime(userInfo.createdAt) }}</span>
                </div>
              </div>
            </div>
            
            <div class="action-section">
              <el-row :gutter="8">
                <el-col :span="12">
                  <el-button type="primary" @click="openEditProfileDialog" :size="isMobile ? 'default' : 'default'" class="action-btn">
                    <el-icon><Edit /></el-icon>
                    <span v-if="!isMobile">编辑信息</span>
                    <span v-else>编辑</span>
                  </el-button>
                </el-col>
                <el-col :span="12">
                  <el-button @click="showPasswordDialog = true" :size="isMobile ? 'default' : 'default'" class="action-btn">
                    <el-icon><Lock /></el-icon>
                    <span v-if="!isMobile">修改密码</span>
                    <span v-else>密码</span>
                  </el-button>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 统计信息和活动记录 -->
      <el-col :xs="24" :sm="24" :md="24" :lg="16" :xl="16" class="stats-col">
        <!-- 统计信息 -->
        <el-card class="stats-card modern-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><DataLine /></el-icon>
                我的统计
              </span>
            </div>
          </template>
          
          <el-row :gutter="isMobile ? 12 : 16" class="stats-grid">
            <el-col :xs="12" :sm="6" :md="6" :lg="6">
              <div class="stat-item modern-stat-item">
                <div class="stat-icon borrow-icon">
                  <el-icon><Box /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-number">{{ stats.borrowCount }}</div>
                  <div class="stat-label">借用次数</div>
                </div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6" :md="6" :lg="6">
              <div class="stat-item modern-stat-item">
                <div class="stat-icon checkin-icon">
                  <el-icon><Clock /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-number">{{ stats.checkinCount }}</div>
                  <div class="stat-label">打卡次数</div>
                </div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6" :md="6" :lg="6">
              <div class="stat-item modern-stat-item">
                <div class="stat-icon duty-icon">
                  <el-icon><OfficeBuilding /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-number">{{ stats.dutyCount }}</div>
                  <div class="stat-label">执勤次数</div>
                </div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6" :md="6" :lg="6">
              <div class="stat-item modern-stat-item">
                <div class="stat-icon leave-icon">
                  <el-icon><DocumentRemove /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-number">{{ stats.leaveCount }}</div>
                  <div class="stat-label">请假次数</div>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
        
        <!-- 最近活动 -->
        <el-card class="activity-card modern-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Clock /></el-icon>
                最近活动
              </span>
            </div>
          </template>
          
          <div class="activity-content">
            <div class="activity-list" v-if="activities.length > 0">
              <div
                v-for="activity in activities"
                :key="activity.id"
                class="activity-item modern-activity-item"
              >
                <div class="activity-icon">
                  <el-icon><component :is="activity.icon" /></el-icon>
                </div>
                <div class="activity-info">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-time">{{ formatTime(activity.time) }}</div>
                </div>
                <div class="activity-status">
                  <el-tag size="small" type="success">完成</el-tag>
                </div>
              </div>
            </div>
            
            <div v-else class="empty-activity">
              <el-empty description="暂无活动记录" :image-size="isMobile ? 60 : 80">
                <template #description>
                  <span class="empty-description">您还没有任何活动记录</span>
                </template>
              </el-empty>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 编辑信息对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑个人信息"
      :width="isMobile ? '95%' : '500px'"
      class="edit-dialog"
      @close="resetProfileEmailCodeState"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        :label-width="isMobile ? '70px' : '80px'"
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱地址" />
        </el-form-item>
        <el-form-item v-if="isProfileEmailChanged" label="验证码" prop="emailCode">
          <div class="email-code-field">
            <el-input
              v-model="editForm.emailCode"
              placeholder="请输入6位验证码"
              maxlength="6"
              clearable
              @input="handleProfileEmailCodeInput"
            />
            <el-button
              class="email-code-btn"
              :loading="profileEmailCodeSending"
              :disabled="!canSendProfileEmailCode"
              @click="sendProfileEmailCode"
            >
              {{ profileEmailCodeCountdown > 0 ? `${profileEmailCodeCountdown}s` : '发送验证码' }}
            </el-button>
          </div>
          <div class="form-tip">验证码将发送到新的邮箱地址，验证通过后才会保存修改。</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="handleUpdateProfile" :loading="updating">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      :width="isMobile ? '95%' : '500px'"
      class="password-dialog"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        :label-width="isMobile ? '80px' : '100px'"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 头像上传对话框 -->
    <el-dialog
      v-model="showAvatarUpload"
      title="更换头像"
      :width="isMobile ? '95%' : '400px'"
      class="avatar-dialog"
    >
      <div class="avatar-upload">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :on-error="handleAvatarError"
          :before-upload="beforeAvatarUpload"
          name="file"
        >
          <img v-if="avatarPreview" :src="avatarPreview" class="avatar-preview" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          <div v-if="uploading" class="avatar-uploading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <p>上传中...</p>
          </div>
        </el-upload>
        <div class="upload-tips">
          <p>支持 JPG、PNG 格式，文件大小不超过 10MB</p>
          <p>建议上传正方形图片，获得最佳显示效果</p>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelAvatarUpload">取消</el-button>
          <el-button type="primary" @click="handleSaveAvatar" :disabled="!newAvatar || uploading" :loading="uploading">
            {{ uploading ? '上传中...' : '保存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Plus, Loading, User, UserFilled, Edit, Lock, DataLine, 
  Clock, Box, OfficeBuilding, DocumentRemove, InfoFilled 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { getUploadedImageUrl } from '@/utils/imageUrl'

const userStore = useUserStore()

// 响应式数据
const windowWidth = ref(window.innerWidth)
const userInfo = computed(() => userStore.userInfo || {})
const isMobile = computed(() => windowWidth.value <= 768)

// 头像URL计算属性
const avatarUrl = computed(() => {
  return getUploadedImageUrl(userInfo.value.avatar)
})
const showEditDialog = ref(false)
const showPasswordDialog = ref(false)
const showAvatarUpload = ref(false)
const updating = ref(false)
const changingPassword = ref(false)
const profileEmailCodeSending = ref(false)
const profileEmailCodeCountdown = ref(0)
let profileEmailCodeTimer = null
const newAvatar = ref('')
const avatarPreview = ref('')
const uploading = ref(false)

// 统计数据
const stats = reactive({
  borrowCount: 0,
  checkinCount: 0,
  dutyCount: 0,
  leaveCount: 0
})

// 活动记录
const activities = ref([])

// 编辑表单
const editForm = reactive({
  realName: '',
  email: '',
  emailCode: ''
})

const normalizeEmail = (email) => String(email || '').trim().toLowerCase()
const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(email || '').trim())

const isProfileEmailChanged = computed(() => {
  return normalizeEmail(editForm.email) !== normalizeEmail(userInfo.value.email)
})

const canSendProfileEmailCode = computed(() => {
  return isProfileEmailChanged.value &&
    isValidEmail(editForm.email) &&
    !profileEmailCodeSending.value &&
    profileEmailCodeCountdown.value === 0
})

const validateProfileEmailCode = (rule, value, callback) => {
  if (!isProfileEmailChanged.value) {
    callback()
    return
  }
  if (!/^\d{6}$/.test(String(value || '').trim())) {
    callback(new Error('请输入6位邮箱验证码'))
    return
  }
  callback()
}

const editRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  emailCode: [
    { validator: validateProfileEmailCode, trigger: 'blur' }
  ]
}

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度在 6 到 50 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 上传配置
const uploadUrl = computed(() => `${request.defaults.baseURL}/users/${userInfo.value.id}/avatar`)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const editFormRef = ref()
const passwordFormRef = ref()

// 初始化编辑表单
const initEditForm = () => {
  editForm.realName = userInfo.value.realName || ''
  editForm.email = userInfo.value.email || ''
  editForm.emailCode = ''
}

const openEditProfileDialog = () => {
  initEditForm()
  resetProfileEmailCodeState()
  showEditDialog.value = true
}

const resetProfileEmailCodeState = () => {
  if (profileEmailCodeTimer) {
    window.clearInterval(profileEmailCodeTimer)
    profileEmailCodeTimer = null
  }
  profileEmailCodeCountdown.value = 0
  profileEmailCodeSending.value = false
}

const startProfileEmailCodeCountdown = () => {
  resetProfileEmailCodeState()
  profileEmailCodeCountdown.value = 60
  profileEmailCodeTimer = window.setInterval(() => {
    profileEmailCodeCountdown.value -= 1
    if (profileEmailCodeCountdown.value <= 0) {
      resetProfileEmailCodeState()
    }
  }, 1000)
}

const handleProfileEmailCodeInput = (value) => {
  const filtered = String(value || '').replace(/\D/g, '').slice(0, 6)
  if (filtered !== editForm.emailCode) {
    editForm.emailCode = filtered
  }
}

const sendProfileEmailCode = async () => {
  if (!isProfileEmailChanged.value) {
    ElMessage.info('邮箱未发生变化，无需验证码')
    return
  }
  if (!isValidEmail(editForm.email)) {
    ElMessage.warning('请先输入正确的新邮箱地址')
    return
  }

  try {
    profileEmailCodeSending.value = true
    await request.post('/auth/email-code', { email: editForm.email.trim() })
    ElMessage.success('验证码已发送，请查收新邮箱')
    startProfileEmailCodeCountdown()
  } catch (error) {
    console.error('发送邮箱验证码失败:', error)
    ElMessage.error(error.message || '验证码发送失败')
  } finally {
    profileEmailCodeSending.value = false
  }
}

watch(
  () => editForm.email,
  () => {
    editForm.emailCode = ''
    if (!isProfileEmailChanged.value) {
      resetProfileEmailCodeState()
    }
    editFormRef.value?.clearValidate?.('emailCode')
  }
)

const getResponsePayload = (response) => response?.data ?? response ?? {}

const toNumber = (value) => {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : 0
}

const pickNumber = (source, keys) => {
  const payload = source || {}
  for (const key of keys) {
    if (payload[key] !== undefined && payload[key] !== null) {
      return toNumber(payload[key])
    }
  }
  return 0
}

// 获取个人统计
const fetchPersonalStats = async () => {
  try {
    console.log('开始获取个人统计数据...')
    
    const [borrowStats, checkinRecords, dutyRecords, leaveStats] = await Promise.allSettled([
      request.get('/borrows/my-statistics').catch(error => {
        console.warn('借用统计获取失败:', error.message)
        return { data: { totalBorrows: 0 } }
      }),

      // 使用分页总数统计全量打卡次数，避免 /checkin/statistics 默认只统计最近30天。
      request.get('/checkin/user-records', {
        params: { page: 0, size: 1 }
      }).catch(error => {
        console.warn('打卡记录统计获取失败:', error.message)
        return { data: { totalElements: 0 } }
      }),

      request.get('/duty/records/my', {
        params: { page: 0, size: 1 }
      }).catch(error => {
        console.warn('执勤记录统计获取失败:', error.message)
        return { data: { totalElements: 0 } }
      }),

      request.get('/leave-requests/statistics').catch(error => {
        console.warn('请假统计获取失败:', error.message)
        return { data: { totalRequests: 0 } }
      })
    ])
    
    if (borrowStats.status === 'fulfilled') {
      const borrowPayload = getResponsePayload(borrowStats.value)
      stats.borrowCount = pickNumber(borrowPayload, ['totalBorrows', 'totalRecords', 'total'])
      console.log('借用次数:', stats.borrowCount)
    } else {
      stats.borrowCount = 0
      console.warn('借用统计获取失败')
    }
    
    if (checkinRecords.status === 'fulfilled') {
      const checkinPayload = getResponsePayload(checkinRecords.value)
      stats.checkinCount = pickNumber(checkinPayload, ['totalElements', 'totalCheckins', 'totalRecords', 'total'])
      console.log('打卡次数:', stats.checkinCount)
    } else {
      stats.checkinCount = 0
      console.warn('打卡统计获取失败')
    }
    
    if (dutyRecords.status === 'fulfilled') {
      const dutyPayload = getResponsePayload(dutyRecords.value)
      stats.dutyCount = pickNumber(dutyPayload, ['totalElements', 'totalRecords', 'totalDuties', 'total'])
      console.log('执勤次数:', stats.dutyCount)
    } else {
      stats.dutyCount = 0
      console.warn('执勤统计获取失败')
    }
    
    if (leaveStats.status === 'fulfilled') {
      const leavePayload = getResponsePayload(leaveStats.value)
      stats.leaveCount = pickNumber(leavePayload, ['totalRequests', 'totalLeaves', 'totalRecords', 'total'])
      console.log('请假次数:', stats.leaveCount)
    } else {
      stats.leaveCount = 0
      console.warn('请假统计获取失败')
    }
    
    console.log('个人统计数据加载完成:', {
      借用: stats.borrowCount,
      打卡: stats.checkinCount,
      执勤: stats.dutyCount,
      请假: stats.leaveCount
    })
    
  } catch (error) {
    console.error('获取个人统计失败:', error)
    // 确保有默认值
    stats.borrowCount = 0
    stats.checkinCount = 0
    stats.dutyCount = 0
    stats.leaveCount = 0
  }
}

// 获取最近活动
const fetchRecentActivities = async () => {
  try {
    // 暂时使用空数据，等待后端实现最近活动接口
    // TODO: 实现后端最近活动接口后取消注释
    // const response = await request.get('/users/recent-activities')
    // if (response.data) {
    //   activities.value = response.data.map(activity => ({
    //     ...activity,
    //     icon: getActivityIcon(activity.type)
    //   }))
    // }
    
    // 设置空的活动列表
    activities.value = []
    console.log('最近活动数据已初始化为空列表')
  } catch (error) {
    console.error('获取活动记录失败:', error)
    activities.value = []
  }
}

// 获取活动图标
const getActivityIcon = (type) => {
  const iconMap = {
    borrow: 'Box',
    return: 'Box',
    checkin: 'Clock',
    duty: 'OfficeBuilding',
    leave: 'DocumentRemove'
  }
  return iconMap[type] || 'InfoFilled'
}

// 更新个人信息
const handleUpdateProfile = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    updating.value = true
    
    const payload = {
      realName: editForm.realName,
      email: editForm.email
    }
    if (isProfileEmailChanged.value) {
      payload.emailCode = editForm.emailCode
    }

    const response = await request.put('/users/profile', payload)
    
    // 更新本地用户信息
    if (response?.data) {
      userStore.updateUserInfo({
        ...response.data,
        avatar: response.data.avatarUrl,
        departmentName: response.data.departmentName
      })
    } else {
      userStore.updateUserInfo({
        realName: payload.realName,
        email: payload.email
      })
    }
    
    ElMessage.success('个人信息更新成功')
    showEditDialog.value = false
    resetProfileEmailCodeState()
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新失败，请重试')
  } finally {
    updating.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true
    
    await request.post(`/users/${userInfo.value.id}/change-password`, {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    ElMessage.success('密码修改成功')
    showPasswordDialog.value = false
    
    // 清空表单
    Object.keys(passwordForm).forEach(key => {
      passwordForm[key] = ''
    })
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('密码修改失败，请检查当前密码是否正确')
  } finally {
    changingPassword.value = false
  }
}

// 头像上传前检查
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 <= 10
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  
  // 创建预览图
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target.result
  }
  reader.readAsDataURL(file)
  
  uploading.value = true
  return true
}

// 头像上传成功
const handleAvatarSuccess = (response) => {
  console.log('头像上传响应:', response)
  uploading.value = false
  
  if (response.success && response.data) {
    // 后端返回的是 ApiResponse<String>，data 直接是头像URL字符串
    newAvatar.value = response.data
    ElMessage.success('头像上传成功，请点击保存按钮确认更改')
  } else {
    ElMessage.error(response.message || '头像上传失败')
    avatarPreview.value = ''
    newAvatar.value = ''
  }
}

// 头像上传失败
const handleAvatarError = (error) => {
  console.error('头像上传失败:', error)
  uploading.value = false
  ElMessage.error('头像上传失败，请重试')
  avatarPreview.value = ''
  newAvatar.value = ''
}

// 取消头像上传
const cancelAvatarUpload = () => {
  showAvatarUpload.value = false
  newAvatar.value = ''
  avatarPreview.value = ''
  uploading.value = false
}

// 保存头像
const handleSaveAvatar = async () => {
  if (!newAvatar.value) {
    ElMessage.error('请先选择头像')
    return
  }
  
  try {
    // 更新用户信息中的头像
    await userStore.refreshUserInfo()
    ElMessage.success('头像更新成功')
    showAvatarUpload.value = false
    newAvatar.value = ''
    avatarPreview.value = ''
  } catch (error) {
    console.error('保存头像失败:', error)
    ElMessage.error('保存头像失败')
  }
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN')
}

// 响应式处理
const handleResize = () => {
  windowWidth.value = window.innerWidth
}

// 组件挂载时初始化
onMounted(async () => {
  // 暂时禁用刷新用户信息，避免400错误
  // TODO: 修复后端/users/profile接口的400错误后重新启用
  // await userStore.refreshUserInfo()
  
  console.log('当前用户信息:', userStore.userInfo)
  console.log('当前token:', userStore.token)
  
  initEditForm()
  fetchPersonalStats()
  fetchRecentActivities()
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  resetProfileEmailCodeState()
})
</script>

<style scoped>
.profile {
  padding: 0;
  background: #ffffff;
  min-height: auto;
  position: relative;
}

/* 页面头部样式 */
.page-header {
  text-align: center;
  margin-bottom: 32px;
  padding: 32px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
  pointer-events: none;
}

.header-content {
  position: relative;
  z-index: 1;
}

.title-section {
  position: relative;
  z-index: 2;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.title-icon {
  font-size: 36px;
  color: rgba(255, 255, 255, 0.9);
}

.page-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 300;
  letter-spacing: 0.3px;
}

.header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.decoration-circle-1 {
  width: 60px;
  height: 60px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.decoration-circle-2 {
  width: 80px;
  height: 80px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.decoration-circle-3 {
  width: 40px;
  height: 40px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

/* 内容区域 */
.profile-content {
  margin-bottom: 24px;
}

/* 卡片样式 */
.modern-card {
  border-radius: 16px;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.05),
    0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
}

.modern-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 8px 25px rgba(0, 0, 0, 0.1),
    0 3px 10px rgba(0, 0, 0, 0.08);
  border-color: #c7d2fe;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.card-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-title .el-icon {
  margin-right: 8px;
  color: #667eea;
}

/* 个人信息样式 */
.profile-info {
  text-align: center;
}

.avatar-section {
  margin-bottom: 24px;
  position: relative;
}

.avatar-container {
  position: relative;
  display: inline-block;
  margin-bottom: 12px;
}

.profile-avatar {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border: 3px solid #ffffff;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  right: 0;
  width: 28px;
  height: 28px;
  background: #667eea;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
  border: 2px solid #ffffff;
}

.avatar-overlay:hover {
  background: #5a6fd8;
  transform: scale(1.1);
}

.upload-btn {
  font-size: 14px;
  color: #667eea;
  font-weight: 500;
}

.upload-btn:hover {
  color: #5a6fd8;
}

/* 信息展示 */
.info-section {
  margin-bottom: 24px;
  text-align: left;
}

/* 桌面端信息列表 */
.info-list .info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

.info-list .info-item:last-child {
  border-bottom: none;
}

.info-list .info-item:hover {
  background: #f8fafc;
  margin: 0 -16px;
  padding: 12px 16px;
  border-radius: 8px;
}

.info-list .info-item label {
  font-weight: 500;
  color: #606266;
  width: 80px;
  flex-shrink: 0;
}

.info-list .info-item span {
  color: #303133;
  flex: 1;
  text-align: right;
  word-break: break-all;
}

/* 移动端信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
}

.info-card:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.info-label {
  font-size: 12px;
  color: #718096;
  margin-bottom: 8px;
  font-weight: 500;
}

.info-card:hover .info-label {
  color: rgba(255, 255, 255, 0.8);
}

.info-value {
  font-size: 14px;
  color: #2d3748;
  font-weight: 600;
  word-break: break-all;
}

.info-card:hover .info-value {
  color: white;
}

/* 操作按钮 */
.action-section {
  margin-top: 24px;
}

.action-btn {
  width: 100%;
  height: 40px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

/* 统计卡片样式 */
.stats-grid {
  margin: 0;
}

.modern-stat-item {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  height: 100%;
}

.modern-stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: #c7d2fe;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
}

.borrow-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.checkin-icon { background: linear-gradient(135deg, #48bb78 0%, #38a169 100%); }
.duty-icon { background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%); }
.leave-icon { background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%); }

.stat-content {
  flex: 1;
  text-align: left;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #718096;
  font-weight: 500;
}

/* 活动记录样式 */
.activity-content {
  min-height: 200px;
}

.activity-list {
  max-height: 300px;
  overflow-y: auto;
}

.modern-activity-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

.modern-activity-item:last-child {
  border-bottom: none;
}

.modern-activity-item:hover {
  background: #f8fafc;
  margin: 0 -20px;
  padding: 16px 20px;
  border-radius: 12px;
}

.modern-activity-item .activity-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
  font-size: 20px;
  flex-shrink: 0;
}

.activity-info {
  flex: 1;
}

.activity-title {
  font-size: 15px;
  color: #303133;
  margin-bottom: 4px;
  font-weight: 500;
}

.activity-time {
  font-size: 13px;
  color: #718096;
}

.activity-status {
  flex-shrink: 0;
}

.empty-activity {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}

.empty-description {
  color: #718096;
  font-size: 14px;
}

/* 头像上传样式 */
.avatar-upload {
  text-align: center;
}

.avatar-uploader {
  margin-bottom: 20px;
}

.avatar-uploader :deep(.el-upload) {
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #667eea;
  background: #f1f5f9;
}

.avatar-uploader-icon {
  font-size: 32px;
  color: #cbd5e0;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 12px;
}

.avatar-uploading {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(102, 126, 234, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  border-radius: 12px;
}

.avatar-uploading p {
  margin-top: 8px;
  font-size: 13px;
  font-weight: 500;
}

.upload-tips {
  color: #718096;
  font-size: 13px;
  line-height: 1.6;
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.upload-tips p {
  margin: 4px 0;
}

.email-code-field {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 118px;
  gap: 10px;
  width: 100%;
}

.email-code-btn {
  width: 118px;
  border-color: rgba(14, 165, 233, 0.32);
  border-radius: 999px;
  background: linear-gradient(135deg, #ecfeff, #f0f9ff);
  color: #0369a1;
  font-weight: 700;
}

.email-code-btn:not(.is-disabled):hover {
  border-color: rgba(14, 165, 233, 0.52);
  background: linear-gradient(135deg, #cffafe, #e0f2fe);
  color: #075985;
}

.form-tip {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.45;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .page-title {
    font-size: 28px;
  }
  
  .title-icon {
    font-size: 32px;
  }
  
  .modern-stat-item {
    padding: 16px;
    gap: 12px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .stat-number {
    font-size: 24px;
  }
}

@media (max-width: 768px) {
  .profile {
    padding: 0 8px;
  }
  
  .page-header {
    margin-bottom: 24px;
    padding: 24px 16px;
    border-radius: 12px;
  }
  
  .page-title {
    font-size: 24px;
    gap: 8px;
  }
  
  .title-icon {
    font-size: 28px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .profile-content .el-col {
    margin-bottom: 16px;
  }
  
  .modern-card {
    margin-bottom: 16px;
    border-radius: 12px;
  }
  
  .modern-stat-item {
    padding: 12px;
    gap: 8px;
    flex-direction: column;
    text-align: center;
  }
  
  .stat-icon {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }
  
  .stat-content {
    text-align: center;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .stat-label {
    font-size: 12px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .info-card {
    padding: 12px;
  }
  
  .info-label {
    font-size: 11px;
    margin-bottom: 6px;
  }
  
  .info-value {
    font-size: 13px;
  }
  
  .action-btn {
    height: 36px;
    font-size: 13px;
  }
  
  .modern-activity-item {
    padding: 12px 0;
  }
  
  .modern-activity-item:hover {
    margin: 0 -16px;
    padding: 12px 16px;
  }
  
  .modern-activity-item .activity-icon {
    width: 40px;
    height: 40px;
    font-size: 16px;
    margin-right: 12px;
  }
  
  .activity-title {
    font-size: 14px;
  }
  
  .activity-time {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .profile {
    padding: 0 4px;
  }
  
  .page-header {
    padding: 20px 12px;
    margin-bottom: 16px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .title-icon {
    font-size: 24px;
  }
  
  .modern-card {
    margin-bottom: 12px;
  }
  
  .modern-stat-item {
    padding: 10px;
  }
  
  .stat-icon {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }
  
  .stat-number {
    font-size: 18px;
  }
  
  .stat-label {
    font-size: 11px;
  }
  
  .info-card {
    padding: 10px;
  }
  
  .info-label {
    font-size: 10px;
  }
  
  .info-value {
    font-size: 12px;
  }
  
  .action-btn {
    height: 32px;
    font-size: 12px;
  }
  
  .avatar-uploader :deep(.el-upload) {
    width: 100px;
    height: 100px;
  }
  
  .avatar-preview {
    width: 100px;
    height: 100px;
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
  }

  .email-code-field {
    grid-template-columns: 1fr;
  }

  .email-code-btn {
    width: 100%;
  }
}

/* 对话框响应式样式 */
:deep(.edit-dialog) {
  @media (max-width: 768px) {
    .el-dialog {
      margin: 20px !important;
      max-height: calc(100vh - 40px) !important;
    }
    
    .el-dialog__body {
      padding: 15px !important;
    }
    
    .el-form-item__label {
      font-size: 13px !important;
    }
  }
}

:deep(.password-dialog) {
  @media (max-width: 768px) {
    .el-dialog {
      margin: 20px !important;
      max-height: calc(100vh - 40px) !important;
    }
    
    .el-dialog__body {
      padding: 15px !important;
    }
    
    .el-form-item__label {
      font-size: 13px !important;
    }
  }
}

:deep(.avatar-dialog) {
  @media (max-width: 768px) {
    .el-dialog {
      margin: 20px !important;
      max-height: calc(100vh - 40px) !important;
    }
    
    .el-dialog__body {
      padding: 15px !important;
    }
  }
}
</style>
