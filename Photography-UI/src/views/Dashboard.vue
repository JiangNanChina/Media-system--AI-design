<template>
  <div class="dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><HomeFilled /></el-icon>
            工作台
          </h1>
          <p class="page-subtitle">欢迎回来，{{ userStore.userName }}！开始您的高效工作</p>
        </div>
        <div class="header-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <section class="stats-section dashboard-section">
      <h2 class="section-title">
        <el-icon><TrendCharts /></el-icon>
        数据概览
      </h2>

      <div class="stats-grid">
        <template v-if="userStore.isAdmin">
          <div v-if="statsLoading" class="modern-stats-card">
            <SkeletonLoader type="stats" />
          </div>
          <div v-else class="modern-stats-card user-stats">
            <div class="stats-header">
              <div class="stats-icon-modern user-icon-modern">
                <el-icon size="26"><UserFilled /></el-icon>
              </div>
              <div class="stats-trend">
                <el-icon class="trend-up"><TrendCharts /></el-icon>
              </div>
            </div>
            <div class="stats-body">
              <div class="stats-number-modern">{{ stats.totalUsers }}</div>
              <div class="stats-label-modern">总用户数</div>
              <div class="stats-description">系统注册用户</div>
            </div>
          </div>

          <div v-if="statsLoading" class="modern-stats-card">
            <SkeletonLoader type="stats" />
          </div>
          <div v-else class="modern-stats-card equipment-stats">
            <div class="stats-header">
              <div class="stats-icon-modern equipment-icon-modern">
                <el-icon size="26"><Camera /></el-icon>
              </div>
              <div class="stats-trend">
                <span class="trend-text">库存充足</span>
              </div>
            </div>
            <div class="stats-body">
              <div class="stats-number-modern">{{ stats.totalEquipment }}</div>
              <div class="stats-label-modern">设备库存</div>
              <div class="stats-description">总库存数量</div>
            </div>
          </div>

          <div v-if="statsLoading" class="modern-stats-card">
            <SkeletonLoader type="stats" />
          </div>
          <div v-else class="modern-stats-card borrow-stats">
            <div class="stats-header">
              <div class="stats-icon-modern borrow-icon-modern">
                <el-icon size="26"><Box /></el-icon>
              </div>
              <div class="stats-trend">
                <el-icon class="trend-up"><TrendCharts /></el-icon>
              </div>
            </div>
            <div class="stats-body">
              <div class="stats-number-modern">{{ stats.totalBorrows }}</div>
              <div class="stats-label-modern">借用记录</div>
              <div class="stats-description">累计借用次数</div>
            </div>
          </div>
        </template>

        <template v-else>
          <div v-if="statsLoading" class="modern-stats-card">
            <SkeletonLoader type="stats" />
          </div>
          <div v-else class="modern-stats-card user-stats">
            <div class="stats-header">
              <div class="stats-icon-modern user-icon-modern">
                <el-icon size="26"><Box /></el-icon>
              </div>
              <div class="stats-trend">
                <span class="trend-text">{{ stats.myTotalBorrows === 0 ? '暂无记录' : '活跃用户' }}</span>
              </div>
            </div>
            <div class="stats-body">
              <div class="stats-number-modern">{{ stats.myTotalBorrows }}</div>
              <div class="stats-label-modern">我的借用</div>
              <div class="stats-description">累计借用次数</div>
            </div>
          </div>

          <div v-if="statsLoading" class="modern-stats-card">
            <SkeletonLoader type="stats" />
          </div>
          <div v-else class="modern-stats-card equipment-stats">
            <div class="stats-header">
              <div class="stats-icon-modern equipment-icon-modern">
                <el-icon size="26"><Camera /></el-icon>
              </div>
              <div class="stats-trend">
                <span class="trend-text">{{ stats.availableEquipment > 0 ? '可借用' : '暂无库存' }}</span>
              </div>
            </div>
            <div class="stats-body">
              <div class="stats-number-modern">{{ stats.availableEquipment }}</div>
              <div class="stats-label-modern">可用设备</div>
              <div class="stats-description">当前可借数量</div>
            </div>
          </div>

          <div v-if="statsLoading" class="modern-stats-card">
            <SkeletonLoader type="stats" />
          </div>
          <div v-else class="modern-stats-card announcement-stats">
            <div class="stats-header">
              <div class="stats-icon-modern announcement-icon-modern">
                <el-icon size="26"><Bell /></el-icon>
              </div>
              <div class="stats-trend">
                <span class="trend-text">{{ stats.publishedAnnouncements > 0 ? '有新消息' : '暂无公告' }}</span>
              </div>
            </div>
            <div class="stats-body">
              <div class="stats-number-modern">{{ stats.publishedAnnouncements }}</div>
              <div class="stats-label-modern">最新公告</div>
              <div class="stats-description">待查看公告数</div>
            </div>
          </div>
        </template>
      </div>
    </section>

    <!-- 内容区域 -->
    <div class="dashboard-bento">
      <section class="dashboard-panel announcements-panel">
        <h2 class="section-title">
          <el-icon><Bell /></el-icon>
          最近公告
        </h2>
        <el-card class="content-card modern-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">公告列表</span>
              <el-button type="primary" link @click="$router.push('/announcement/list')" v-if="userStore.isAdmin">
                查看全部
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>

          <div v-if="announcements.length > 0" class="announcement-list">
            <div
              v-for="announcement in announcements"
              :key="announcement.id"
              class="announcement-item"
              @click="viewAnnouncement(announcement)"
            >
              <div class="announcement-title">{{ announcement.title }}</div>
              <div class="announcement-meta">
                <el-tag :type="getAnnouncementTagType(announcement)" size="small">
                  {{ getPriorityText(announcement) }}
                </el-tag>
                <span class="announcement-time">{{ formatTime(announcement.createdAt) }}</span>
              </div>
            </div>
          </div>

          <el-empty v-else description="暂无公告" :image-size="80" />
        </el-card>
      </section>

      <section class="dashboard-panel todos-panel">
        <h2 class="section-title">
          <el-icon><List /></el-icon>
          待处理事项
        </h2>
        <el-card class="content-card modern-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">我的任务</span>
            </div>
          </template>

          <div class="todo-list">
            <div v-if="userStore.isAdmin" class="todo-section">
              <h4 class="todo-section-title">管理员待办</h4>
              <div class="todo-item" v-if="pendingBorrows > 0" @click="$router.push('/borrow/list')">
                <el-icon class="todo-icon"><Box /></el-icon>
                <span class="todo-text">{{ pendingBorrows }} 个借用申请待审核</span>
                <el-icon class="todo-arrow"><ArrowRight /></el-icon>
              </div>
              <div class="todo-item" v-if="pendingLeaves > 0" @click="$router.push('/leave/list')">
                <el-icon class="todo-icon"><DocumentRemove /></el-icon>
                <span class="todo-text">{{ pendingLeaves }} 个请假申请待审核</span>
                <el-icon class="todo-arrow"><ArrowRight /></el-icon>
              </div>
              <div v-if="pendingBorrows === 0 && pendingLeaves === 0" class="todo-muted">暂无管理员待办</div>
            </div>

            <div class="todo-section">
              <h4 class="todo-section-title">个人待办</h4>
              <div class="todo-item" v-if="overdueReturns > 0" @click="$router.push('/borrow/list')">
                <el-icon class="todo-icon warning"><WarningFilled /></el-icon>
                <span class="todo-text">{{ overdueReturns }} 个设备逾期未还</span>
                <el-icon class="todo-arrow"><ArrowRight /></el-icon>
              </div>
              <div class="todo-item" @click="$router.push('/checkin/main')">
                <el-icon class="todo-icon"><Clock /></el-icon>
                <span class="todo-text">今日晚自习打卡</span>
                <el-icon class="todo-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
        </el-card>
      </section>

      <section class="dashboard-panel duty-panel">
        <h2 class="section-title">
          <el-icon><OfficeBuilding /></el-icon>
          执勤状态
        </h2>
        <div class="duty-card-shell">
          <DutyStatusCard />
        </div>
      </section>

      <section class="dashboard-panel quick-panel">
        <h2 class="section-title">
          <el-icon><Star /></el-icon>
          快速操作
        </h2>
        <el-card class="content-card modern-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">常用功能</span>
            </div>
          </template>

          <div class="quick-actions-grid">
            <div class="quick-action" @click="$router.push('/equipment/list')">
              <el-icon size="24"><Camera /></el-icon>
              <span>浏览设备</span>
            </div>
            <div class="quick-action" @click="$router.push('/borrow/list')">
              <el-icon size="24"><Box /></el-icon>
              <span>借用设备</span>
            </div>
            <div class="quick-action" @click="$router.push('/checkin/main')">
              <el-icon size="24"><Clock /></el-icon>
              <span>晚自习打卡</span>
            </div>
            <div class="quick-action" @click="$router.push('/duty/list')">
              <el-icon size="24"><OfficeBuilding /></el-icon>
              <span>办公执勤</span>
            </div>
            <div class="quick-action" v-if="userStore.isAdmin" @click="$router.push('/user/list')">
              <el-icon size="24"><UserFilled /></el-icon>
              <span>用户管理</span>
            </div>
            <div class="quick-action" @click="$router.push('/profile')">
              <el-icon size="24"><User /></el-icon>
              <span>个人中心</span>
            </div>
          </div>
        </el-card>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import DutyStatusCard from '@/components/DutyStatusCard.vue'
import {
  HomeFilled, UserFilled, Camera, Box, Bell, List, ArrowRight, 
  Clock, Star, WarningFilled, DocumentRemove, TrendCharts, User, OfficeBuilding
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const statsLoading = ref(true)
const stats = reactive({
  // 管理员统计
  totalUsers: 0,
  totalEquipment: 0,
  totalBorrows: 0,
  // 普通用户统计
  myTotalBorrows: 0,
  availableEquipment: 0,
  publishedAnnouncements: 0
})

const announcements = ref([])
const pendingBorrows = ref(0)
const pendingLeaves = ref(0)
const overdueReturns = ref(0)

// 获取统计数据
const fetchStats = async () => {
  try {
    statsLoading.value = true
    const requests = []
    
    if (userStore.isAdmin) {
      // 管理员统计数据
      requests.push(request.get('/users/statistics').catch(() => ({ data: { totalUsers: 0 } })))
      requests.push(request.get('/equipment/statistics').catch(() => ({ data: { totalEquipments: 0 } })))
      requests.push(request.get('/borrows/statistics').catch(() => ({ data: { totalRecords: 0 } })))
      
      const responses = await Promise.allSettled(requests)
      
      // 处理管理员统计数据
      const userStats = responses[0]?.value
      if (userStats?.data) {
        stats.totalUsers = userStats.data.totalUsers || 0
      }
      
      const equipmentStats = responses[1]?.value
      if (equipmentStats?.data) {
        // 显示库存总数，而不是设备种类数
        stats.totalEquipment = equipmentStats.data.totalStock || 0
      }
      
      const borrowStats = responses[2]?.value
      if (borrowStats?.data) {
        stats.totalBorrows = borrowStats.data.totalRecords || 0
      }
    } else {
      // 普通用户统计数据
      requests.push(request.get('/borrows/my-statistics').catch((error) => {
        console.error('借用统计API调用失败:', error)
        return { data: { totalBorrows: 0 } }
      }))
      requests.push(request.get('/equipment/available-count').catch((error) => {
        console.error('可用设备数API调用失败:', error)
        return { data: 0 }
      }))
      requests.push(request.get('/announcements/count').catch((error) => {
        console.error('公告数量API调用失败:', error)
        return { data: 0 }
      }))
      
      const responses = await Promise.allSettled(requests)
      
      // 处理普通用户统计数据
      const myBorrowStats = responses[0]?.value
      if (myBorrowStats?.data) {
        stats.myTotalBorrows = myBorrowStats.data.totalBorrows || 0
      }
      
      const availableEquipmentCount = responses[1]?.value
      console.log('可用设备数API响应:', availableEquipmentCount)
      if (availableEquipmentCount?.data !== undefined) {
        stats.availableEquipment = availableEquipmentCount.data || 0
        console.log('设置可用设备数为:', stats.availableEquipment)
      }
      
      // 如果设备数为0，自动调用调试接口
      if (stats.availableEquipment === 0) {
        console.log('可用设备数为0，获取调试信息...')
        try {
          const debugResponse = await request.get('/equipment/debug-count')
          console.log('设备调试信息:', debugResponse.data)
        } catch (error) {
          console.error('获取设备调试信息失败:', error)
        }
      }
      
      const announcementCount = responses[2]?.value
      if (announcementCount?.data !== undefined) {
        stats.publishedAnnouncements = announcementCount.data || 0
      }
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  } finally {
    statsLoading.value = false
  }
}

// 获取最近公告
const fetchRecentAnnouncements = async () => {
  try {
    const response = await request.get('/announcements/public/paged', {
      params: {
        page: 0,
        size: 5
      }
    })
    
    if (response.data?.content) {
      announcements.value = response.data.content
    } else {
      announcements.value = [] // 如果没有数据，设置空数组
    }
  } catch (error) {
    console.error('获取公告失败:', error)
    announcements.value = [] // 设置空数组避免页面错误
  }
}

// 获取待办事项
const fetchTodoItems = async () => {
  try {
    if (userStore.isAdmin) {
      // 待审核借用申请
      const borrowResponse = await request.get('/borrows', {
        params: {
          status: 'PENDING',
          page: 0,
          size: 1
        }
      }).catch(() => null)
      
      if (borrowResponse?.data?.totalElements) {
        pendingBorrows.value = borrowResponse.data.totalElements
      }
      
      // 待审核请假申请
      const leaveResponse = await request.get('/leave-requests', {
        params: {
          status: 'PENDING',
          page: 0,
          size: 1
        }
      }).catch(() => null)
      
      if (leaveResponse?.data?.totalElements) {
        pendingLeaves.value = leaveResponse.data.totalElements
      }
    }
    
    // 逾期未还设备（仅管理员）
    if (userStore.isAdmin) {
      const overdueResponse = await request.get('/borrows/overdue', {
        params: {
          page: 0,
          size: 1
        }
      }).catch(() => null)
      
      if (overdueResponse?.data?.totalElements) {
        overdueReturns.value = overdueResponse.data.totalElements
      }
    }
  } catch (error) {
    console.error('获取待办事项失败:', error)
  }
}

// 查看公告详情
const viewAnnouncement = (announcement) => {
  // 清空公告浏览历史，因为这是从首页开始的新浏览会话
  console.log('从首页点击公告，清空历史')
  sessionStorage.removeItem('announcementHistory')
  router.push(`/announcement/${announcement.id}`)
}

// 获取公告类型文本（根据 type 枚举）
const getPriorityText = (announcement) => {
  // 优先使用 type 字段（公告类型枚举）
  if (announcement.type) {
    const typeMap = {
      'SYSTEM': '系统通知',
      'IMPORTANT': '重要公告',
      'GENERAL': '一般通知',
      'ACTIVITY': '活动公告'
    }
    return typeMap[announcement.type] || '一般通知'
  }
  
  // 如果没有 type，降级使用 priority（向后兼容）
  const priority = announcement.priority || 0
  if (priority >= 8) return '重要'
  if (priority >= 5) return '一般'
  return '普通'
}

// 获取公告标签类型（颜色）
const getAnnouncementTagType = (announcement) => {
  // 优先使用 type 字段
  if (announcement.type) {
    const typeColorMap = {
      'SYSTEM': 'primary',    // 系统通知 - 蓝色
      'IMPORTANT': 'danger',  // 重要公告 - 红色
      'GENERAL': 'info',      // 一般通知 - 灰色
      'ACTIVITY': 'success'   // 活动公告 - 绿色
    }
    return typeColorMap[announcement.type] || 'info'
  }
  
  // 如果没有 type，降级使用 priority
  const priority = announcement.priority || 0
  if (priority >= 8) return 'danger'
  if (priority >= 5) return 'warning'
  return 'info'
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString('zh-CN')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchStats()
  fetchRecentAnnouncements()
  fetchTodoItems()
})
</script>

<style scoped>
.dashboard {
  padding: var(--spacing-6);
  background: var(--gradient-background);
  min-height: 100vh;
  position: relative;
}

.dashboard::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 119, 198, 0.15) 0%, transparent 50%);
  pointer-events: none;
}

/* 现代化页面头部 */
.page-header {
  text-align: center;
  margin-bottom: var(--spacing-10);
  position: relative;
  z-index: 1;
}

.header-content {
  position: relative;
  display: inline-block;
}

.title-section {
  position: relative;
  z-index: 2;
}

.page-title {
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
  margin-bottom: var(--spacing-3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-3);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.title-icon {
  font-size: 40px;
  background: linear-gradient(135deg, #ffd89b 0%, #19547b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: var(--font-size-xl);
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: var(--font-weight-light);
  letter-spacing: 0.5px;
}

.header-decoration {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 120px;
  height: 120px;
  top: -60px;
  left: -200px;
  animation-delay: 0s;
}

.circle-2 {
  width: 80px;
  height: 80px;
  top: -40px;
  right: -180px;
  animation-delay: 2s;
}

.circle-3 {
  width: 60px;
  height: 60px;
  bottom: -30px;
  left: -150px;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

.stats-row {
  margin-bottom: 24px;
}

.stats-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  height: 100px;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
}

.user-icon {
  background: linear-gradient(45deg, #159ee5, #82d5ff);
}

.equipment-icon {
  background: linear-gradient(45deg, #35c7a3, #7de0c6);
}

.borrow-icon {
  background: linear-gradient(45deg, #f2b84b, #ffd26f);
}

/* 现代化统计卡片样式 */
.modern-stats-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-xl);
  padding: var(--spacing-8);
  box-shadow: var(--shadow-lg);
  transition: all var(--duration-normal) var(--easing-ease);
  position: relative;
  overflow: hidden;
  margin-bottom: var(--spacing-5);
}

.modern-stats-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--gradient-accent);
}

.modern-stats-card:hover {
  transform: translateY(-4px);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.15),
    0 8px 24px rgba(0, 0, 0, 0.08);
}

.user-stats::before {
  background: linear-gradient(90deg, #159ee5, #82d5ff);
}

.equipment-stats::before {
  background: linear-gradient(90deg, #35c7a3, #7de0c6);
}

.borrow-stats::before {
  background: linear-gradient(90deg, #f2b84b, #ffd26f);
}

.announcement-stats::before {
  background: linear-gradient(90deg, #f56c6c, #f78989);
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-5);
}

.stats-icon-modern {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-base);
}

.user-icon-modern {
  background: linear-gradient(135deg, var(--color-primary-500), #66b1ff);
  color: var(--color-white);
}

.equipment-icon-modern {
  background: linear-gradient(135deg, var(--color-success), #85ce61);
  color: var(--color-white);
}

.borrow-icon-modern {
  background: linear-gradient(135deg, var(--color-warning), #ebb563);
  color: var(--color-white);
}

.announcement-icon-modern {
  background: linear-gradient(135deg, var(--color-error), #f78989);
  color: var(--color-white);
}

.stats-trend {
  display: flex;
  align-items: center;
}

.trend-up {
  color: #35c7a3;
  font-size: 20px;
}

.trend-text {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 12px;
  background: rgba(103, 194, 58, 0.1);
  color: #35c7a3;
}

.equipment-stats .trend-text {
  background: rgba(103, 194, 58, 0.1);
  color: #35c7a3;
}

.user-stats .trend-text {
  background: rgba(64, 158, 255, 0.1);
  color: #159ee5;
}

.borrow-stats .trend-text {
  background: rgba(230, 162, 60, 0.1);
  color: #f2b84b;
}

.announcement-stats .trend-text {
  background: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.stats-body {
  text-align: left;
}

.stats-number-modern {
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  line-height: var(--line-height-tight);
  margin-bottom: var(--spacing-2);
}

.stats-label-modern {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-secondary);
  line-height: var(--line-height-tight);
  margin-bottom: var(--spacing-1);
}

.stats-description {
  font-size: var(--font-size-sm);
  color: var(--color-text-placeholder);
  line-height: var(--line-height-tight);
}

/* 现代化内容区域 */
.stats-section,
.content-sections,
.duty-section,
.quick-actions-section {
  margin-bottom: var(--spacing-8);
  position: relative;
  z-index: 1;
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-white);
  margin-bottom: var(--spacing-5);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.modern-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  transition: all var(--duration-normal) var(--easing-ease);
}

.modern-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 16px 48px rgba(0, 0, 0, 0.15),
    0 4px 24px rgba(0, 0, 0, 0.08);
}

.stats-content {
  flex: 1;
}

.stats-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1;
}

.stats-label {
  color: #909399;
  font-size: 14px;
}

.content-row {
  margin-bottom: 24px;
}

.content-card {
  height: 100%;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.content-card :deep(.el-card__body) {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  color: #159ee5;
}

.announcement-list {
  max-height: 300px;
  overflow-y: auto;
}

.announcement-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.announcement-item:hover {
  background-color: #f8f9fa;
  border-radius: 6px;
  padding: 12px;
  margin: 0 -12px;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 8px;
  font-weight: 500;
}

.announcement-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-time {
  font-size: 12px;
  color: #909399;
}

.todo-list {
  max-height: 300px;
  overflow-y: auto;
}

.todo-section {
  margin-bottom: 16px;
}

.todo-section:last-child {
  margin-bottom: 0;
}

.todo-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin: 0 0 8px 0;
  padding-bottom: 4px;
  border-bottom: 1px solid #ebeef5;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 6px;
}

.todo-item:hover {
  background-color: #f8f9fa;
  padding: 8px 12px;
  margin: 0 -12px;
}

.todo-icon {
  margin-right: 12px;
  color: #159ee5;
}

.todo-icon.warning {
  color: #f2b84b;
}

.todo-text {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.todo-arrow {
  color: #c0c4cc;
  font-size: 12px;
}

.quick-actions-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 现代化快速操作 */
.quick-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-5);
  border-radius: var(--radius-base);
  cursor: pointer;
  transition: all var(--duration-normal) var(--easing-ease);
  background: var(--color-secondary-100);
  border: 1px solid var(--color-divider);
}

.quick-action:hover {
  background: var(--color-primary-500);
  color: var(--color-white);
  transform: translateY(-2px) scale(1.02);
  box-shadow: var(--shadow-hover);
  border-color: var(--color-primary-500);
}

.quick-action .el-icon {
  margin-bottom: var(--spacing-2);
  transition: transform var(--duration-normal) var(--easing-ease);
}

.quick-action:hover .el-icon {
  transform: scale(1.1);
}

.quick-action span {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  transition: all var(--duration-normal) var(--easing-ease);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard {
    padding: 0 8px;
  }
  
  .stats-card {
    padding: 16px;
    height: 80px;
    margin-bottom: 12px;
  }
  
  .stats-icon {
    width: 48px;
    height: 48px;
    margin-right: 12px;
  }
  
  .stats-icon .el-icon {
    font-size: 24px;
  }
  
  .stats-number {
    font-size: 24px;
  }
  
  .stats-label {
    font-size: 13px;
  }
  
  .content-card {
    margin-bottom: 16px;
  }
  
  .content-card :deep(.el-card__body) {
    padding: 16px;
  }
  
  .announcement-item {
    padding: 10px 0;
  }
  
  .announcement-title {
    font-size: 13px;
    line-height: 1.4;
  }
  
  .todo-item {
    padding: 10px 0;
  }
  
  .todo-text {
    font-size: 13px;
  }
  
  .quick-action {
    padding: 16px 12px;
  }
  
  .quick-action .el-icon {
    font-size: 20px;
  }
  
  .quick-action span {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .dashboard {
    padding: 0 4px;
  }
  
  .dashboard-header {
    text-align: center;
    margin-bottom: 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .stats-row {
    margin-bottom: 16px;
  }
  
  .stats-card {
    padding: 12px;
    height: 70px;
  }
  
  .stats-icon {
    width: 40px;
    height: 40px;
    margin-right: 10px;
  }
  
  .stats-icon .el-icon {
    font-size: 20px;
  }
  
  .stats-number {
    font-size: 20px;
  }
  
  .stats-label {
    font-size: 12px;
  }
  
  .content-row {
    margin-bottom: 16px;
  }
  
  .content-row .el-col {
    margin-bottom: 12px;
  }
  
  .card-title {
    font-size: 15px;
  }
  
  .announcement-list {
    max-height: 200px;
  }
  
  .todo-list {
    max-height: 200px;
  }
  
  .todo-section-title {
    font-size: 13px;
  }
  
  .quick-action {
    padding: 12px 8px;
  }
  
  .quick-action .el-icon {
    font-size: 18px;
    margin-bottom: 6px;
  }
  
  .quick-action span {
    font-size: 12px;
  }
}

/* 超小屏幕优化 */
@media (max-width: 360px) {
  .stats-card {
    flex-direction: column;
    text-align: center;
    height: auto;
    padding: 12px 8px;
  }
  
  .stats-icon {
    margin-right: 0;
    margin-bottom: 8px;
  }
  
  .quick-action {
    padding: 10px 6px;
  }
}

/* Fresh bento dashboard layout */
.dashboard {
  max-width: 1440px;
  margin: 0 auto;
  padding: 4px 0 28px;
  background: transparent;
  min-height: auto;
}

.dashboard::before,
.header-decoration {
  display: none;
}

.page-header {
  margin-bottom: 18px;
  text-align: left;
}

.header-content {
  display: block;
  padding: 22px 26px;
  border: 1px solid rgba(135, 185, 214, 0.18);
  border-radius: 24px;
  background:
    radial-gradient(circle at 86% 4%, rgba(98, 214, 189, 0.1), transparent 34%),
    radial-gradient(circle at 8% 0%, rgba(37, 184, 242, 0.07), transparent 30%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.97), rgba(248, 253, 255, 0.88));
  box-shadow: 0 18px 48px rgba(21, 122, 177, 0.08);
}

.page-title {
  justify-content: flex-start;
  margin: 0 0 8px;
  color: var(--color-text-primary);
  font-size: 30px;
  font-weight: 850;
  text-shadow: none;
  letter-spacing: 0;
}

.title-icon {
  font-size: 32px;
  color: var(--color-primary-500);
  background: none;
  -webkit-text-fill-color: currentColor;
}

.page-subtitle {
  color: var(--color-text-secondary);
  font-size: 15px;
  font-weight: 650;
  letter-spacing: 0;
}

.dashboard-section,
.dashboard-panel {
  position: relative;
  z-index: 1;
}

.stats-section {
  margin-bottom: 20px;
}

.section-title {
  margin: 0 0 12px;
  color: var(--color-text-primary);
  font-size: 20px;
  font-weight: 850;
  text-shadow: none;
  letter-spacing: 0;
}

.section-title .el-icon {
  color: var(--color-primary-600);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.modern-stats-card {
  --stat-accent: #25b8f2;
  --stat-accent-soft: rgba(37, 184, 242, 0.1);
  min-height: 126px;
  margin: 0;
  padding: 18px;
  border: 1px solid rgba(135, 185, 214, 0.18);
  border-radius: 22px;
  background:
    radial-gradient(circle at 92% 0%, var(--stat-accent-soft), transparent 34%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.98), rgba(248, 253, 255, 0.9));
  box-shadow: 0 16px 38px rgba(21, 122, 177, 0.08);
}

.modern-stats-card::before {
  width: 5px;
  height: 100%;
  right: auto;
  bottom: 0;
  background: linear-gradient(180deg, var(--stat-accent), rgba(255, 255, 255, 0.24));
}

.user-stats {
  --stat-accent: #25b8f2;
  --stat-accent-soft: rgba(37, 184, 242, 0.1);
}

.equipment-stats {
  --stat-accent: #35c7a3;
  --stat-accent-soft: rgba(53, 199, 163, 0.1);
}

.borrow-stats {
  --stat-accent: #f2b84b;
  --stat-accent-soft: rgba(242, 184, 75, 0.11);
}

.announcement-stats {
  --stat-accent: #ec6a78;
  --stat-accent-soft: rgba(236, 106, 120, 0.1);
}

.modern-stats-card:hover,
.modern-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 20px 46px rgba(21, 122, 177, 0.11);
}

.stats-header {
  margin-bottom: 12px;
}

.stats-icon-modern {
  width: 46px;
  height: 46px;
  border-radius: 16px;
  box-shadow: 0 10px 22px rgba(21, 122, 177, 0.1);
}

.user-icon-modern,
.equipment-icon-modern,
.borrow-icon-modern,
.announcement-icon-modern {
  color: #0876a5;
  background: var(--button-primary-bg);
  border: 1px solid var(--button-primary-border);
}

.equipment-icon-modern {
  color: #087f63;
  background: var(--button-success-bg);
  border-color: var(--button-success-border);
}

.borrow-icon-modern {
  color: #9a640d;
  background: var(--button-warning-bg);
  border-color: var(--button-warning-border);
}

.announcement-icon-modern {
  color: #b4233e;
  background: var(--button-danger-bg);
  border-color: var(--button-danger-border);
}

.stats-number-modern {
  color: var(--color-text-primary);
  font-family: var(--font-family-mono);
  font-size: 30px;
  font-weight: 850;
}

.stats-label-modern {
  color: var(--color-text-primary);
  font-size: 16px;
  font-weight: 850;
}

.stats-description {
  color: var(--color-text-secondary);
  font-weight: 650;
}

.trend-text {
  color: #14795f;
  background: rgba(245, 255, 251, 0.92);
  border: 1px solid rgba(53, 199, 163, 0.2);
  border-radius: 999px;
  font-weight: 800;
}

.trend-up {
  color: var(--color-success);
}

.dashboard-bento {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  grid-template-areas:
    "announcements todos"
    "duty quick";
  gap: 20px;
  align-items: start;
}

.announcements-panel {
  grid-area: announcements;
}

.todos-panel {
  grid-area: todos;
}

.duty-panel {
  grid-area: duty;
}

.quick-panel {
  grid-area: quick;
}

.modern-card,
.content-card {
  height: 100%;
  border: 1px solid rgba(135, 185, 214, 0.18);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 48px rgba(21, 122, 177, 0.08);
}

.modern-card :deep(.el-card__header),
.content-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(135, 185, 214, 0.16);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(248, 253, 255, 0.76));
}

.content-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.card-title {
  color: var(--color-text-primary);
  font-size: 15px;
  font-weight: 850;
}

.card-header :deep(.el-button) {
  height: 32px;
  padding: 0 10px;
  border-radius: 999px;
  color: var(--color-primary-600);
  font-weight: 800;
  background: rgba(242, 251, 255, 0.82);
  border: 1px solid rgba(37, 184, 242, 0.16);
}

.announcement-list,
.todo-list {
  max-height: none;
  overflow: visible;
}

.announcement-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  min-height: 66px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(135, 185, 214, 0.12);
}

.announcement-item:hover {
  margin: 0;
  padding: 12px;
  background: rgba(245, 252, 255, 0.86);
  border-radius: 14px;
}

.announcement-title {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 750;
}

.announcement-meta {
  justify-content: flex-end;
  gap: 10px;
}

.announcement-time {
  color: var(--color-text-placeholder);
  font-weight: 650;
}

.todo-section {
  margin-bottom: 12px;
}

.todo-section-title {
  margin: 0 0 8px;
  padding: 0;
  color: var(--color-text-secondary);
  border: 0;
  font-size: 13px;
  font-weight: 850;
}

.todo-item,
.todo-muted {
  min-height: 42px;
  padding: 9px 10px;
  border: 1px solid rgba(135, 185, 214, 0.14);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.74);
}

.todo-item:hover {
  margin: 0;
  padding: 9px 10px;
  background: rgba(242, 251, 255, 0.9);
}

.todo-muted {
  color: var(--color-text-placeholder);
  font-size: 13px;
  font-weight: 650;
}

.todo-icon {
  color: var(--color-primary-600);
}

.todo-icon.warning {
  color: var(--button-warning-text);
}

.todo-text {
  color: var(--color-text-primary);
  font-weight: 750;
}

.todo-arrow {
  color: var(--color-text-placeholder);
}

.duty-card-shell :deep(.duty-status-card) {
  height: 100%;
  border: 1px solid rgba(135, 185, 214, 0.18);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 48px rgba(21, 122, 177, 0.08);
  overflow: hidden;
}

.duty-card-shell :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom-color: rgba(135, 185, 214, 0.16);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(248, 253, 255, 0.76));
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.quick-action {
  min-height: 86px;
  padding: 14px 12px;
  border: 1px solid rgba(135, 185, 214, 0.16);
  border-radius: 18px;
  color: var(--color-text-primary);
  background:
    radial-gradient(circle at 92% 0%, rgba(98, 214, 189, 0.08), transparent 34%),
    rgba(255, 255, 255, 0.78);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.82);
}

.quick-action:hover {
  color: var(--color-primary-600);
  background: rgba(242, 251, 255, 0.92);
  border-color: rgba(37, 184, 242, 0.28);
  transform: translateY(-1px);
  box-shadow: 0 12px 28px rgba(21, 122, 177, 0.08);
}

.quick-action .el-icon {
  color: var(--color-primary-500);
}

@media (max-width: 1200px) {
  .dashboard-bento {
    grid-template-columns: 1fr;
    grid-template-areas:
      "announcements"
      "todos"
      "duty"
      "quick";
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 0 0 22px;
  }

  .header-content {
    padding: 18px;
    border-radius: 20px;
  }

  .page-title {
    font-size: 24px;
  }

  .page-subtitle {
    font-size: 14px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .modern-stats-card {
    min-height: 112px;
    padding: 16px;
  }

  .dashboard-bento {
    gap: 16px;
  }

  .announcement-item {
    grid-template-columns: 1fr;
    align-items: flex-start;
  }

  .announcement-meta {
    justify-content: space-between;
    width: 100%;
  }

  .quick-actions-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 420px) {
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
