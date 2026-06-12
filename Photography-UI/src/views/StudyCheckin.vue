<template>
  <div class="study-checkin">
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <span>晚自习打卡</span>
          <el-button type="primary" @click="refreshData" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <div class="checkin-info">
        <div class="date-info">
          <h3>{{ formatDate(new Date()) }}</h3>
          <p>{{ formatWeekday(new Date()) }}</p>
        </div>
        
        <div class="status-info">
          <div v-if="loading" class="loading-status">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          
          <!-- 已签到状态 -->
          <div v-else-if="todayRecord?.status === 'NORMAL' || todayRecord?.status === 'LATE'" class="checked-in-status">
            <el-icon class="status-icon success"><CircleCheckFilled /></el-icon>
            <div class="status-text">
              <h4>{{ todayRecord?.status === 'LATE' ? '已签到（迟到）' : '已签到' }}</h4>
              <p>签到时间：{{ formatDateTime(todayRecord?.checkinTime) }}</p>
              <p v-if="todayRecord?.locationName">地点：{{ todayRecord.locationName }}</p>
              <p v-if="todayRecord?.sessionName">时段：{{ todayRecord.sessionName }}</p>
              <p v-if="todayRecord?.isLate && todayRecord?.lateMinutes">迟到 {{ todayRecord.lateMinutes }} 分钟</p>
            </div>
          </div>
          
          <!-- 请假状态 -->
          <div v-else-if="todayRecord?.status === 'LEAVE'" class="leave-approved-status">
            <el-icon class="leave-icon"><DocumentChecked /></el-icon>
            <div class="leave-message">
              <h4 class="leave-text">请假申请已通过</h4>
              <p class="leave-text">无需签到</p>
            </div>
            
            <div class="leave-details">
              <div class="info-row">
                <span class="info-label">打卡时段：{{ getCheckinSession(todayRecord) }}</span>
                <span class="info-extra">审核人：{{ getApproverName(todayRecord) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">请假原因：{{ getLeaveReason(todayRecord) }}</span>
                <span class="info-extra">审核时间：{{ getApproveTime(todayRecord) }}</span>
              </div>
            </div>
          </div>
          
          <!-- 未签到状态 -->
          <div v-else class="not-checked-in-status">
            <el-icon class="status-icon warning"><WarningFilled /></el-icon>
            <div class="status-text">
              <h4>还未签到</h4>
              <p>请及时完成晚自习签到</p>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 签到按钮区域 -->
    <el-card v-if="!todayRecord || todayRecord.status === 'ABSENT'" class="action-card">
      <el-button 
        type="primary" 
        size="large" 
        @click="showCheckinDialog = true"
        :disabled="loading"
      >
        <el-icon><Location /></el-icon>
        立即签到
      </el-button>
    </el-card>
    
    <!-- 最近记录 -->
    <el-card class="records-card">
      <template #header>
        <span>最近打卡记录</span>
      </template>
      
      <div v-if="recentRecords.length === 0" class="no-records">
        <el-empty description="暂无打卡记录" />
      </div>
      
      <div v-else class="records-list">
        <div 
          v-for="record in recentRecords" 
          :key="record.id" 
          class="record-item"
        >
          <div class="record-info">
            <div class="record-header">
              <span class="record-date">{{ formatDate(record.checkinTime) }}</span>
              <el-tag 
                :type="getStatusTagType(record.status)"
                size="small"
              >
                {{ getStatusText(record.status) }}
              </el-tag>
            </div>
            <div class="record-details">
              <p>时间：{{ formatDateTime(record.checkinTime) }}</p>
              <p v-if="record.locationName">地点：{{ record.locationName }}</p>
              <p v-if="record.sessionName">时段：{{ record.sessionName }}</p>
              <p v-if="record.notes" class="record-notes">备注：{{ record.notes }}</p>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 签到对话框 -->
    <el-dialog
      v-model="showCheckinDialog"
      title="晚自习签到"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="checkin-dialog">
        <p>即将推出完整的签到功能...</p>
        <p>包括GPS定位、二维码扫描等多种签到方式</p>
      </div>
      <template #footer>
        <el-button @click="showCheckinDialog = false">取消</el-button>
        <el-button type="primary" @click="showCheckinDialog = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { 
  Refresh, 
  Loading, 
  CircleCheckFilled, 
  WarningFilled, 
  Location, 
  DocumentChecked 
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// 响应式数据
const loading = ref(false)
const todayRecord = ref(null)
const recentRecords = ref([])
const showCheckinDialog = ref(false)

// 页面加载时获取数据
onMounted(() => {
  fetchData()
})

// 获取数据
const fetchData = async () => {
  await Promise.all([
    fetchTodayRecord(),
    fetchRecentRecords()
  ])
}

// 刷新数据
const refreshData = async () => {
  loading.value = true
  try {
    await fetchData()
  } finally {
    loading.value = false
  }
}

// 获取今日打卡记录
const fetchTodayRecord = async () => {
  try {
    const response = await request.get('/checkin/today')
    if (response.data && response.data.hasCheckedIn) {
      // 如果有签到记录，需要获取完整的记录信息
      const recordResponse = await request.get('/checkin/records/today')
      todayRecord.value = recordResponse.data
      console.log('今日打卡记录:', todayRecord.value)
      if (todayRecord.value) {
        console.log('打卡记录状态:', todayRecord.value.status)
        console.log('状态类型:', typeof todayRecord.value.status)
        console.log('是否为LEAVE:', todayRecord.value.status === 'LEAVE')
        console.log('备注内容:', todayRecord.value.notes)
        console.log('签到时间:', todayRecord.value.checkinTime)
        console.log('备注原文:', todayRecord.value.notes)
        console.log('解析的审核人:', getApproverName(todayRecord.value))
        console.log('解析的请假原因:', getLeaveReason(todayRecord.value))
      }
    } else {
      todayRecord.value = null
    }
  } catch (error) {
    console.error('获取今日打卡记录失败:', error)
    todayRecord.value = null
  }
}

// 获取最近打卡记录
const fetchRecentRecords = async () => {
  try {
    const response = await request.get('/checkin/records/recent?size=5')
    recentRecords.value = response.data || []
  } catch (error) {
    console.error('获取最近打卡记录失败:', error)
    recentRecords.value = []
  }
}

// 解析审核人姓名
const getApproverName = (record) => {
  if (!record?.notes) return '未知'
  
  const match = record.notes.match(/审核人：(.+?)(?:\s|$)/)
  return match ? match[1] : '未知'
}

// 解析请假原因
const getLeaveReason = (record) => {
  if (!record?.notes) return '未知'
  
  const parts = record.notes.split(' | ')
  if (parts.length > 0) {
    const reasonPart = parts[0]
    const match = reasonPart.match(/请假申请已批准 - (.+)/)
    return match ? match[1] : reasonPart
  }
  return '未知'
}

// 解析审核时间
const getApproveTime = (record) => {
  if (!record?.updatedAt) return '未知'
  return formatDateTime(record.updatedAt)
}

// 获取打卡时段
const getCheckinSession = (record) => {
  if (!record?.configuration) return '未知时段'
  
  const config = record.configuration
  if (config.startTime && config.endTime) {
    return `${config.startTime} - ${config.endTime}`
  }
  return config.sessionName || '未知时段'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 格式化星期
const formatWeekday = (date) => {
  if (!date) return ''
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return weekdays[new Date(date).getDay()]
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const d = new Date(datetime)
  return d.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 'NORMAL': return 'success'
    case 'LATE': return 'warning'
    case 'LEAVE': return 'info'
    case 'ABSENT': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'NORMAL': return '正常'
    case 'LATE': return '迟到'
    case 'LEAVE': return '请假'
    case 'ABSENT': return '缺席'
    default: return '未知'
  }
}
</script>

<style scoped>
.study-checkin {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.header-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.checkin-info {
  display: flex;
  align-items: center;
  gap: 30px;
}

.date-info h3 {
  margin: 0 0 5px 0;
  color: #303133;
  font-size: 24px;
}

.date-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.status-info {
  flex: 1;
}

.loading-status,
.checked-in-status,
.leave-approved-status,
.not-checked-in-status {
  display: flex;
  align-items: center;
  gap: 15px;
}

.status-icon {
  font-size: 32px;
}

.status-icon.success {
  color: #67c23a;
}

.status-icon.warning {
  color: #e6a23c;
}

.status-text h4 {
  margin: 0 0 5px 0;
  color: #303133;
  font-size: 18px;
}

.status-text p {
  margin: 2px 0;
  color: #606266;
  font-size: 14px;
}

.leave-approved-status {
  padding: 20px;
  background: linear-gradient(135deg, #e8f5e8 0%, #f0f9ff 100%);
  border-radius: 8px;
  border-left: 4px solid #67c23a;
}

.leave-icon {
  font-size: 48px;
  color: #67c23a;
  margin-right: 20px;
}

.leave-message {
  flex: 1;
  margin-right: 20px;
}

.leave-text {
  color: #67c23a;
  font-weight: bold;
  margin: 0;
}

.leave-text:first-child {
  font-size: 20px;
  margin-bottom: 5px;
}

.leave-details {
  flex: 2;
  min-width: 0;
}

.leave-details .info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  gap: 20px;
}

.leave-details .info-label {
  color: #303133;
  font-size: 14px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.leave-details .info-extra {
  color: #909399;
  font-size: 13px;
  flex-shrink: 0;
  text-align: right;
}

.action-card {
  margin-bottom: 20px;
  text-align: center;
}

.action-card .el-button {
  padding: 15px 30px;
  font-size: 16px;
}

.records-card {
  margin-bottom: 20px;
}

.no-records {
  text-align: center;
  padding: 40px 0;
}

.records-list {
  max-height: 400px;
  overflow-y: auto;
}

.record-item {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
}

.record-item:last-child {
  border-bottom: none;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.record-date {
  font-weight: bold;
  color: #303133;
}

.record-details p {
  margin: 3px 0;
  color: #606266;
  font-size: 14px;
}

.record-notes {
  color: #909399;
  font-style: italic;
}

.checkin-dialog {
  text-align: center;
  padding: 20px 0;
}

.checkin-dialog p {
  margin: 10px 0;
  color: #606266;
}
</style>
