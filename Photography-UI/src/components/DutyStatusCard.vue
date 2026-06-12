<template>
  <el-card class="duty-status-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <div class="header-info">
          <el-icon size="20"><OfficeBuilding /></el-icon>
          <span class="header-title">办公执勤</span>
        </div>
        <el-tag v-if="todayRecord?.checkinTime" type="success" size="small">
          {{ todayRecord.status }}
        </el-tag>
        <el-tag v-else-if="currentSchedule" type="info" size="small">
          今日有执勤
        </el-tag>
        <el-tag v-else type="info" size="small">
          今日无执勤
        </el-tag>
      </div>
    </template>

    <div class="duty-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-content">
        <el-skeleton :rows="3" animated />
      </div>

      <!-- 今日有执勤安排 -->
      <div v-else-if="currentSchedule" class="duty-info">
        <!-- 已签到状态 -->
        <div v-if="todayRecord?.checkinTime" class="checked-in">
          <div class="status-info">
            <div class="info-row">
              <span class="label">签到时间：</span>
              <span class="value">{{ formatTime(todayRecord.checkinTime) }}</span>
            </div>
            <div v-if="todayRecord.checkoutTime" class="info-row">
              <span class="label">签退时间：</span>
              <span class="value">{{ formatTime(todayRecord.checkoutTime) }}</span>
            </div>
            <div class="info-row">
              <span class="label">执勤时段：</span>
              <span class="value">{{ formatTimeRange(currentSchedule) }}</span>
            </div>
          </div>
          
          <div class="action-area">
            <el-button 
              v-if="!todayRecord.checkoutTime"
              type="warning" 
              size="small"
              @click="quickCheckout"
              :loading="checkoutLoading"
            >
              <el-icon><Upload /></el-icon>
              快速签退
            </el-button>
            <div v-else class="completed-badge">
              <el-icon color="#67c23a"><CircleCheckFilled /></el-icon>
              <span>执勤完成</span>
            </div>
          </div>
        </div>

        <!-- 未签到状态 -->
        <div v-else class="not-checked-in">
          <div class="schedule-info">
            <div class="schedule-time">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTimeRange(currentSchedule) }}</span>
            </div>
            <div class="schedule-status">
              <span v-if="isCurrentTime" class="current-time">执勤时间内</span>
              <span v-else-if="isBeforeTime" class="before-time">即将开始</span>
              <span v-else class="after-time">已结束</span>
            </div>
          </div>
          
          <div class="action-area">
            <el-button 
              type="primary" 
              size="small"
              @click="quickCheckin"
              :disabled="!canCheckin"
              :loading="checkinLoading"
            >
              <el-icon><LocationFilled /></el-icon>
              快速签到
            </el-button>
          </div>
        </div>
      </div>

      <!-- 今日无执勤安排 -->
      <div v-else class="no-duty">
        <div class="no-duty-icon">
          <el-icon size="32" color="#c0c4cc"><Coffee /></el-icon>
        </div>
        <p class="no-duty-text">今日无执勤安排</p>
        <p class="no-duty-sub">好好休息吧～</p>
      </div>

      <!-- 底部操作 -->
      <div class="card-footer">
        <el-button 
          type="text" 
          size="small" 
          @click="$router.push('/duty/checkin')"
          class="detail-link"
        >
          查看详情
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  OfficeBuilding, Clock, LocationFilled, Upload, CircleCheckFilled,
  ArrowRight, Coffee
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(true)
const checkinLoading = ref(false)
const checkoutLoading = ref(false)
const todayRecord = ref(null)
const currentSchedule = ref(null)

// 计算属性
const canCheckin = computed(() => {
  return currentSchedule.value && !todayRecord.value?.checkinTime && isCurrentTime.value
})

const isCurrentTime = computed(() => {
  if (!currentSchedule.value) return false
  
  const now = new Date()
  const currentTime = now.getHours() * 60 + now.getMinutes()
  
  const [startHour, startMin] = currentSchedule.value.startTime.split(':').map(Number)
  const [endHour, endMin] = currentSchedule.value.endTime.split(':').map(Number)
  
  const startTime = startHour * 60 + startMin
  const endTime = endHour * 60 + endMin
  
  return currentTime >= startTime && currentTime <= endTime
})

const isBeforeTime = computed(() => {
  if (!currentSchedule.value) return false
  
  const now = new Date()
  const currentTime = now.getHours() * 60 + now.getMinutes()
  
  const [startHour, startMin] = currentSchedule.value.startTime.split(':').map(Number)
  const startTime = startHour * 60 + startMin
  
  return currentTime < startTime
})

// 获取数据
const fetchData = async () => {
  try {
    const [recordResponse, scheduleResponse] = await Promise.all([
      request.get('/duty/records/today').catch(() => ({ data: null })),
      request.get('/duty/schedules/current').catch(() => ({ data: null }))
    ])
    
    todayRecord.value = recordResponse.data
    currentSchedule.value = scheduleResponse.data
  } catch (error) {
    console.error('获取执勤状态失败:', error)
  } finally {
    loading.value = false
  }
}

// 快速签到
const quickCheckin = async () => {
  try {
    checkinLoading.value = true
    await request.post('/duty/checkin', { notes: '快速签到' })
    
    ElMessage.success('签到成功！')
    await fetchData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '签到失败')
  } finally {
    checkinLoading.value = false
  }
}

// 快速签退
const quickCheckout = async () => {
  try {
    checkoutLoading.value = true
    await request.post('/duty/checkout', { notes: '快速签退' })
    
    ElMessage.success('签退成功！')
    await fetchData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '签退失败')
  } finally {
    checkoutLoading.value = false
  }
}

// 工具函数
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const formatTimeRange = (schedule) => {
  if (!schedule || !schedule.startTime || !schedule.endTime) {
    return '时间待定'
  }
  return `${schedule.startTime} - ${schedule.endTime}`
}

// 生命周期
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.duty-status-card {
  height: 100%;
  transition: all 0.3s ease;
}

.duty-status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title {
  font-weight: 600;
  color: #303133;
}

.duty-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.loading-content {
  padding: 16px 0;
}

.duty-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.checked-in,
.not-checked-in {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.label {
  color: #909399;
}

.value {
  color: #303133;
  font-weight: 500;
}

.schedule-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.schedule-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.schedule-status {
  font-size: 12px;
}

.current-time {
  color: #67c23a;
  font-weight: 500;
}

.before-time {
  color: #e6a23c;
}

.after-time {
  color: #909399;
}

.action-area {
  display: flex;
  justify-content: center;
}

.completed-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #67c23a;
  font-size: 14px;
  font-weight: 500;
}

.no-duty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
  text-align: center;
}

.no-duty-icon {
  margin-bottom: 12px;
}

.no-duty-text {
  font-size: 14px;
  color: #606266;
  margin: 0 0 4px 0;
}

.no-duty-sub {
  font-size: 12px;
  color: #c0c4cc;
  margin: 0;
}

.card-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: center;
}

.detail-link {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.detail-link:hover {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
  }
  
  .label {
    font-size: 12px;
  }
  
  .value {
    font-size: 14px;
  }
}
</style>
