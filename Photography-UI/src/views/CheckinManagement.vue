<template>
  <div class="checkin-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><LocationFilled /></el-icon>
            晚自习打卡
          </h1>
          <p class="page-subtitle">记录你的学习时光，见证每一次成长</p>
        </div>
        <div class="header-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>
      </div>
    </div>

    <!-- 今日打卡状态卡片 -->
    <div class="status-section">
      <el-card class="status-card modern-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon size="20"><Clock /></el-icon>
          <span>今日打卡状态</span>
          <el-tag v-if="todayStatus.hasCheckedIn" type="success" size="small">已签到</el-tag>
          <el-tag v-else type="info" size="small">未签到</el-tag>
        </div>
      </template>
      
      <div class="status-content">
        <div v-if="loading" class="status-loading">
          <SkeletonLoader type="stats" />
        </div>
        
        <div v-else-if="todayStatus.hasCheckedIn" class="checked-in-status">
          <div class="checkin-info">
            <div class="info-item">
              <span class="label">签到时间：</span>
              <span class="value">{{ formatDateTime(todayStatus.checkinTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">签到地点：</span>
              <span class="value">{{ todayStatus.locationName }}</span>
            </div>
            <div class="info-item">
              <span class="label">时段：</span>
              <span class="value">{{ todayStatus.sessionName }}</span>
            </div>
            <div v-if="todayStatus.isLate" class="info-item">
              <span class="label">迟到时间：</span>
              <span class="value late">{{ todayStatus.lateMinutes }}分钟</span>
            </div>
          </div>
          
          <div class="action-buttons">
            <el-button 
              v-if="todayStatus.requireCheckout && !todayStatus.hasCheckedOut"
              type="warning" 
              @click="showCheckoutDialog = true"
              :disabled="!canCheckout"
            >
              <el-icon><Upload /></el-icon>
              签退
            </el-button>
            <el-button v-else-if="todayStatus.hasCheckedOut" type="success" disabled>
              <el-icon><CircleCheckFilled /></el-icon>
              已签退
            </el-button>
          </div>
        </div>
        
        <div v-else class="not-checked-in">
          <div class="checkin-prompt">
            <el-icon size="48" color="#409eff"><LocationInformation /></el-icon>
            <p class="prompt-text">还未签到，点击下方按钮开始签到</p>
            <div v-if="availableSessions.length > 0" class="available-sessions">
              <p class="sessions-title">当前可用时段：</p>
              <div class="session-tags">
                <el-tag 
                  v-for="session in availableSessions" 
                  :key="session.id"
                  type="primary"
                  class="session-tag"
                >
                  {{ session.name }} ({{ formatTime(session.checkinStartTime) }} - {{ formatTime(session.checkinEndTime) }})
                </el-tag>
              </div>
            </div>
          </div>
          
          <div class="checkin-button-group">
            <el-button 
              type="primary" 
              size="large" 
              @click="startCheckin"
              :disabled="availableSessions.length === 0"
              class="checkin-btn"
            >
              <el-icon><LocationFilled /></el-icon>
              立即签到
            </el-button>
          </div>
        </div>
      </div>
      </el-card>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <h2 class="section-title">
        <el-icon><TrendCharts /></el-icon>
        学习统计
      </h2>
      <el-row :gutter="24" class="stats-row">
        <el-col :xs="24" :sm="12" :md="12" :lg="12">
          <div v-if="statsLoading" class="modern-stats-card">
            <SkeletonLoader type="stats" />
          </div>
          <div v-else class="modern-stats-card checkin-stats">
            <div class="stats-header">
              <div class="stats-icon-modern checkin-icon-modern">
                <el-icon size="28"><Calendar /></el-icon>
              </div>
              <div class="stats-trend">
                <el-icon class="trend-up"><TrendCharts /></el-icon>
              </div>
            </div>
            <div class="stats-body">
              <div class="stats-number-modern">{{ userStats.totalCheckins }}</div>
              <div class="stats-label-modern">总签到次数</div>
              <div class="stats-description">累计学习记录</div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="12" :lg="12">
          <div v-if="statsLoading" class="modern-stats-card">
            <SkeletonLoader type="stats" />
          </div>
          <div v-else class="modern-stats-card late-stats">
            <div class="stats-header">
              <div class="stats-icon-modern late-icon-modern">
                <el-icon size="28"><WarningFilled /></el-icon>
              </div>
              <div class="stats-trend">
                <span class="trend-text">{{ userStats.lateCount === 0 ? '完美记录' : '需要改善' }}</span>
              </div>
            </div>
            <div class="stats-body">
              <div class="stats-number-modern">{{ userStats.lateCount }}</div>
              <div class="stats-label-modern">迟到次数</div>
              <div class="stats-description">准时是美德</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 最近打卡记录 -->
    <div class="records-section">
      <h2 class="section-title">
        <el-icon><List /></el-icon>
        最近记录
      </h2>
      <el-card class="records-card modern-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>打卡历史</span>
            <el-button type="primary" link @click="$router.push('/checkin/records')">
              查看全部
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
      
      <div v-if="recordsLoading" class="records-loading">
        <SkeletonLoader v-for="i in 3" :key="i" type="list" />
      </div>
      
      <EmptyState 
        v-else-if="recentRecords.length === 0"
        type="no-data"
        title="暂无打卡记录"
        description="开始你的第一次打卡吧！"
        size="small"
      />
      
      <div v-else class="records-list">
        <div 
          v-for="record in recentRecords" 
          :key="record.id"
          class="record-item"
          @click="viewRecordDetail(record)"
        >
          <div class="record-info">
            <div class="record-header">
              <span class="record-date">{{ formatDate(record.checkinTime) }}</span>
              <el-tag :type="getStatusType(record.status)" size="small">
                {{ getStatusText(record.status) }}
              </el-tag>
            </div>
            <div class="record-details">
              <span class="detail-item">
                <el-icon><LocationInformation /></el-icon>
                {{ record.locationName }}
              </span>
              <span class="detail-item">
                <el-icon><Clock /></el-icon>
                {{ record.sessionName }}
              </span>
              <span v-if="record.durationMinutes" class="detail-item">
                <el-icon><Timer /></el-icon>
                {{ formatDuration(record.durationMinutes) }}
              </span>
            </div>
          </div>
          <el-icon class="record-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
      </el-card>
    </div>

    <!-- 签到对话框 -->
    <el-dialog
      v-model="showCheckinDialog"
      title="签到"
      :width="isMobile ? '95%' : '500px'"
      center
    >
      <CheckinDialog
        v-if="showCheckinDialog"
        :available-sessions="availableSessions"
        :nearby-locations="nearbyLocations"
        @success="handleCheckinSuccess"
        @cancel="showCheckinDialog = false"
        @configurations-loaded="handleConfigurationsLoaded"
      />
    </el-dialog>

    <!-- 签退对话框 -->
    <el-dialog
      v-model="showCheckoutDialog"
      title="签退"
      :width="isMobile ? '95%' : '400px'"
      center
    >
      <CheckoutDialog
        v-if="showCheckoutDialog"
        :record="todayStatus"
        @success="handleCheckoutSuccess"
        @cancel="showCheckoutDialog = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Clock, Upload, CircleCheckFilled, LocationInformation, LocationFilled,
  Calendar, WarningFilled, Timer, List, ArrowRight, TrendCharts
} from '@element-plus/icons-vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import EmptyState from '@/components/EmptyState.vue'
import CheckinDialog from '@/components/CheckinDialog.vue'
import CheckoutDialog from '@/components/CheckoutDialog.vue'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(true)
const statsLoading = ref(true)
const recordsLoading = ref(true)
const showCheckinDialog = ref(false)
const showCheckoutDialog = ref(false)
const isMobile = ref(false)

const todayStatus = reactive({
  hasCheckedIn: false,
  hasCheckedOut: false,
  checkinTime: null,
  locationName: '',
  sessionName: '',
  isLate: false,
  lateMinutes: 0,
  requireCheckout: false
})

const userStats = reactive({
  totalCheckins: 0,
  lateCount: 0
})

const availableSessions = ref([])
const nearbyLocations = ref([])
const recentRecords = ref([])

// 计算属性
const canCheckout = computed(() => {
  // 这里可以添加签退的时间限制逻辑
  return true
})

// 检查是否为移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 窗口大小变化处理
const handleResize = () => {
  checkMobile()
}

// 获取今日打卡状态
const fetchTodayStatus = async () => {
  try {
    const response = await request.get('/checkin/today-status')
    Object.assign(todayStatus, response.data)
  } catch (error) {
    console.error('获取今日打卡状态失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取用户统计数据
const fetchUserStats = async () => {
  try {
    const response = await request.get('/checkin/user-statistics')
    Object.assign(userStats, response.data)
  } catch (error) {
    console.error('获取用户统计数据失败:', error)
  } finally {
    statsLoading.value = false
  }
}

// 获取最近打卡记录
const fetchRecentRecords = async () => {
  try {
    const response = await request.get('/checkin/recent-records', {
      params: { size: 5 }
    })
    recentRecords.value = response.data || []
  } catch (error) {
    console.error('获取最近打卡记录失败:', error)
  } finally {
    recordsLoading.value = false
  }
}

// 获取可用的签到时段
const fetchAvailableSessions = async () => {
  try {
    const response = await request.get('/checkin/available-configurations')
    availableSessions.value = response.data || []
  } catch (error) {
    console.error('获取可用时段失败:', error)
  }
}

// 开始签到（晚自习打卡不需要获取位置）
const startCheckin = async () => {
  try {
    // 直接打开签到对话框，不再获取位置信息
    // 晚自习打卡通过二维码或管理员审核方式进行，无需GPS定位
    nearbyLocations.value = []
    showCheckinDialog.value = true
  } catch (error) {
    console.error('打开签到对话框失败:', error)
    ElMessage.error('打开签到对话框失败')
  }
}

// 处理签到成功
const handleCheckinSuccess = (response) => {
  showCheckinDialog.value = false
  ElMessage.success(response.message || '签到成功！')
  
  // 刷新页面数据
  fetchTodayStatus()
  fetchUserStats()
  fetchRecentRecords()
}

// 处理签退成功
const handleCheckoutSuccess = (response) => {
  showCheckoutDialog.value = false
  ElMessage.success(response.message || '签退成功！')
  
  // 刷新页面数据
  fetchTodayStatus()
  fetchUserStats()
  fetchRecentRecords()
}

// 处理配置加载
const handleConfigurationsLoaded = (configurations) => {
  availableSessions.value = configurations
  ElMessage.success('已加载全部可用配置，您可以选择二维码或WiFi方式签到')
}

// 查看记录详情
const viewRecordDetail = (record) => {
  router.push(`/checkin/records/${record.id}`)
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString('zh-CN')
}

// 格式化日期
const formatDate = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleDateString('zh-CN')
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return time.substring(0, 5) // HH:mm
}

// 格式化持续时间
const formatDuration = (minutes) => {
  if (!minutes || minutes === 0) return '0分钟'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return hours > 0 ? `${hours}小时${mins}分钟` : `${mins}分钟`
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'NORMAL': 'success',
    'LATE': 'warning',
    'EARLY_LEAVE': 'warning',
    'ABSENT': 'danger',
    'MAKEUP': 'info',
    'LEAVE': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'NORMAL': '正常',
    'LATE': '迟到',
    'EARLY_LEAVE': '早退',
    'ABSENT': '缺席',
    'MAKEUP': '补签',
    'LEAVE': '请假'
  }
  return textMap[status] || '未知'
}

// 组件挂载
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', handleResize)
  
  // 获取数据
  fetchTodayStatus()
  fetchUserStats()
  fetchRecentRecords()
  fetchAvailableSessions()
})

// 组件卸载
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.checkin-management {
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  position: relative;
}

.checkin-management::before {
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

.page-header {
  text-align: center;
  margin-bottom: 40px;
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
  font-size: 36px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
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
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 300;
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

/* 现代化卡片样式 */
.status-section,
.stats-section,
.records-section {
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 22px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 20px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.modern-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 16px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: all 0.3s ease;
}

.modern-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 16px 48px rgba(0, 0, 0, 0.15),
    0 4px 24px rgba(0, 0, 0, 0.08);
}

.status-card {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.status-content {
  padding: 20px 0;
}

.checked-in-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.checkin-info {
  flex: 1;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #909399;
  margin-right: 8px;
  min-width: 80px;
}

.value {
  color: #303133;
  font-weight: 500;
}

.value.late {
  color: #f56c6c;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.not-checked-in {
  text-align: center;
}

.checkin-prompt {
  margin-bottom: 24px;
}

.prompt-text {
  font-size: 16px;
  color: #606266;
  margin: 16px 0;
}

.available-sessions {
  margin-top: 20px;
}

.sessions-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.session-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.session-tag {
  margin: 0;
}

.checkin-button-group {
  display: flex;
  justify-content: center;
}

.checkin-btn {
  padding: 12px 32px;
  font-size: 16px;
  border-radius: 8px;
}

/* 现代化统计卡片样式 */
.stats-row {
  margin-bottom: 0;
}

.modern-stats-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.modern-stats-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.modern-stats-card:hover {
  transform: translateY(-4px);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.15),
    0 8px 24px rgba(0, 0, 0, 0.08);
}

.checkin-stats::before {
  background: linear-gradient(90deg, #67c23a, #85ce61);
}

.late-stats::before {
  background: linear-gradient(90deg, #e6a23c, #ebb563);
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-icon-modern {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.checkin-icon-modern {
  background: linear-gradient(135deg, #67c23a, #85ce61);
  color: white;
}

.late-icon-modern {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
  color: white;
}

.stats-trend {
  display: flex;
  align-items: center;
}

.trend-up {
  color: #67c23a;
  font-size: 20px;
}

.trend-text {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 8px;
  border-radius: 12px;
  background: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.late-stats .trend-text {
  background: rgba(230, 162, 60, 0.1);
  color: #e6a23c;
}

.stats-body {
  text-align: left;
}

.stats-number-modern {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stats-label-modern {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  line-height: 1;
  margin-bottom: 4px;
}

.stats-description {
  font-size: 13px;
  color: #909399;
  line-height: 1;
}

.records-list {
  padding: 0;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
  margin: 0 -20px;
  padding-left: 20px;
  padding-right: 20px;
}

.record-item:last-child {
  border-bottom: none;
}

.record-item:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  transform: translateX(4px);
}

.record-info {
  flex: 1;
}

.record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.record-date {
  font-weight: 600;
  color: #303133;
}

.record-details {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.record-arrow {
  color: #c0c4cc;
  margin-left: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .checkin-management {
    padding: 16px;
  }
  
  .page-title {
    font-size: 28px;
  }
  
  .page-subtitle {
    font-size: 16px;
  }
  
  .header-decoration {
    display: none;
  }
  
  .checked-in-status {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .action-buttons {
    flex-direction: row;
    justify-content: center;
  }
  
  .modern-stats-card {
    padding: 20px;
    margin-bottom: 16px;
  }
  
  .stats-number-modern {
    font-size: 28px;
  }
  
  .section-title {
    font-size: 20px;
  }
  
  .record-details {
    gap: 12px;
  }
  
  .detail-item {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .session-tags {
    flex-direction: column;
    align-items: center;
  }
  
  .record-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .record-details {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
