<template>
  <div class="daily-checkin-records">
    <el-card class="search-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ userStore.isAdmin ? '每日打卡汇总' : '我的打卡记录' }}</span>
        </div>
      </template>
      
      <!-- 搜索条件 -->
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateRangeChange"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 加载状态 -->
    <SkeletonLoader 
      v-if="loading"
      :rows="5"
      :show-avatar="true"
      :show-title="true"
    />

    <!-- 空状态 -->
    <EmptyState 
      v-else-if="summaries.length === 0"
      type="no-data"
      title="暂无打卡记录"
      description="还没有任何打卡记录"
      size="small"
    />

    <!-- 每日汇总卡片列表 -->
    <el-card v-else class="summaries-card" shadow="hover">
      <div class="summaries-list">
        <div 
          v-for="summary in summaries" 
          :key="summary.date"
          class="summary-card"
        >
          <!-- 卡片头部 -->
          <div class="card-header-section">
            <div class="date-info">
              <div class="date-main">
                <el-icon><Calendar /></el-icon>
                <span class="date-text">{{ formatDate(summary.date) }}</span>
                <span class="weekday">{{ getWeekday(summary.date) }}</span>
              </div>
              <div class="config-info">
                <div class="info-item" v-if="summary.configurationName">
                  <el-icon><Setting /></el-icon>
                  <span>{{ summary.configurationName }}</span>
                </div>
                <div class="info-item" v-if="summary.mainLocationName">
                  <el-icon><LocationInformation /></el-icon>
                  <span>{{ summary.mainLocationName }}</span>
                </div>
                <div class="info-item" v-if="summary.mainSessionName">
                  <el-icon><Timer /></el-icon>
                  <span>{{ summary.mainSessionName }}</span>
                </div>
              </div>
            </div>
            
            <div class="stats-section">
              <div class="stat-item">
                <span class="stat-label">应签到</span>
                <span class="stat-value total">{{ summary.totalRequiredCount }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">已签到</span>
                <span class="stat-value checked">{{ summary.checkedInCount }}</span>
              </div>
              <div class="stat-item" v-if="userStore.isAdmin && summary.pendingAuditCount > 0">
                <span class="stat-label">待审核</span>
                <span class="stat-value pending">{{ summary.pendingAuditCount }}</span>
              </div>
              <div class="stat-item" v-if="userStore.isAdmin && summary.lateCount > 0">
                <span class="stat-label">迟到</span>
                <span class="stat-value late">{{ summary.lateCount }}</span>
              </div>
              <div class="stat-item" v-if="summary.leaveCount > 0">
                <span class="stat-label">请假</span>
                <span class="stat-value leave">{{ summary.leaveCount }}</span>
              </div>
              <div class="stat-item" v-if="userStore.isAdmin && summary.absentCount > 0">
                <span class="stat-label">缺勤</span>
                <span class="stat-value absent">{{ summary.absentCount }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">签到率</span>
                <span class="stat-value rate" :class="getRateClass(summary.checkinRate)">
                  {{ formatRate(summary.checkinRate) }}
                </span>
              </div>
            </div>
            
            <div class="action-section">
              <el-button 
                type="primary" 
                :icon="View"
                @click="showUserDetails(summary)"
                size="default"
              >
                查看详情
              </el-button>
              <el-button 
                v-if="userStore.isAdmin" 
                type="danger" 
                :icon="Delete"
                @click="handleDeleteDailyRecords(summary)"
                size="default"
                plain
              >
                删除当天记录
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[5, 10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="dialogTitle"
      width="80%"
      :before-close="handleCloseDialog"
      class="user-detail-dialog"
    >
      <template #header="{ close, titleId, titleClass }">
        <div class="dialog-header">
          <span :id="titleId" :class="titleClass">{{ dialogTitle }}</span>
          <div class="dialog-actions">
            <el-button
              v-if="userStore.isAdmin"
              type="success"
              :icon="Download"
              @click="exportDailyData"
              size="default"
            >
              导出Excel
            </el-button>
          </div>
        </div>
      </template>
      <div v-if="detailLoading" class="dialog-loading">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else class="user-details-content">
        <!-- 汇总信息头部 -->
        <div class="detail-header">
          <div class="header-info">
            <h3>{{ selectedSummary?.configurationName || '打卡详情' }}</h3>
            <div class="header-stats">
              <el-tag type="info">应签到 {{ selectedSummary?.totalRequiredCount || 0 }} 人</el-tag>
              <el-tag type="success">已签到 {{ selectedSummary?.checkedInCount || 0 }} 人</el-tag>
              <el-tag v-if="selectedSummary?.pendingAuditCount > 0" type="warning">待审核 {{ selectedSummary.pendingAuditCount }} 人</el-tag>
              <el-tag v-if="selectedSummary?.lateCount > 0" type="warning">迟到 {{ selectedSummary.lateCount }} 人</el-tag>
              <el-tag v-if="selectedSummary?.absentCount > 0" type="danger">缺勤 {{ selectedSummary.absentCount }} 人</el-tag>
              <el-tag v-if="selectedSummary?.leaveCount > 0" type="info">请假 {{ selectedSummary.leaveCount }} 人</el-tag>
            </div>
          </div>
        </div>
        
        <!-- 用户状态列表 -->
        <div class="user-status-list">
          <div 
            v-for="userStatus in selectedSummary?.userStatuses || []" 
            :key="userStatus.userId"
            class="user-status-item"
            :class="getStatusClass(userStatus.status)"
          >
            <!-- 用户基本信息 -->
            <div class="user-header">
              <el-avatar 
                :size="40" 
                :style="{ backgroundColor: getAvatarColor(userStatus.status) }"
              >
                {{ getAvatarText(userStatus.userName) }}
              </el-avatar>
              <div class="user-info">
                <div class="user-name">
                  {{ userStatus.userName }}
                  <span v-if="userStatus.departmentName" class="user-department">
                    {{ userStatus.departmentName }}
                  </span>
                </div>
                <div class="user-status-tags">
                  <el-tag 
                    :type="getStatusTagType(userStatus.status, userStatus.auditStatus)"
                    size="small"
                  >
                    {{ getStatusText(userStatus.status, userStatus.auditStatus) }}
                  </el-tag>
                  <el-tag 
                    v-if="userStatus.isLate && userStatus.auditStatus !== 'PENDING'"
                    type="warning"
                    size="small"
                  >
                    迟到{{ userStatus.lateMinutes }}分
                  </el-tag>
                </div>
              </div>
            </div>
            
            <!-- 详细信息（紧凑展示） -->
            <div class="user-details-compact">
              <!-- 打卡信息 -->
              <div v-if="userStatus.checkinTime" class="info-group">
                <span class="info-item">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(userStatus.checkinTime) }}
                </span>
                <span v-if="userStatus.checkoutTime" class="info-item">
                  <el-icon><Upload /></el-icon>
                  {{ formatTime(userStatus.checkoutTime) }}
                </span>
                <span v-if="userStatus.durationMinutes" class="info-item duration">
                  <el-icon><Timer /></el-icon>
                  {{ formatDuration(userStatus.durationMinutes) }}
                </span>
              </div>
              
              <!-- 审核信息 -->
              <div v-if="userStatus.auditStatus && userStatus.auditStatus !== 'NOT_REQUIRED'" class="info-group audit-info">
                <span class="info-item">
                  <el-icon><User /></el-icon>
                  审核：{{ userStatus.auditedByName || '-' }}
                </span>
                <span v-if="userStatus.auditTime" class="info-item">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(userStatus.auditTime) }}
                </span>
                <span v-if="userStatus.auditNotes" class="info-item audit-notes" :title="userStatus.auditNotes">
                  <el-icon><ChatLineRound /></el-icon>
                  {{ userStatus.auditNotes }}
                </span>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="action-buttons" v-if="userStatus.status === 'CHECKED_IN'">
              <el-button
                v-if="canDeleteRecord(userStatus.userId)"
                type="danger"
                size="small"
                :icon="Delete"
                circle
                @click="handleDeleteUserRecord(userStatus)"
                title="删除记录"
              />
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  Search, Refresh, Calendar, Clock, Upload, LocationInformation,
  Setting, Timer, Delete, User, View, Download, ChatLineRound
} from '@element-plus/icons-vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import EmptyState from '@/components/EmptyState.vue'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(true)
const summaries = ref([])
const dateRange = ref([])
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const selectedSummary = ref(null)

const searchForm = reactive({
  startDate: '',
  endDate: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 计算属性
const dialogTitle = computed(() => {
  if (!selectedSummary.value) return '用户详情'
  return `${formatDate(selectedSummary.value.date)} - ${selectedSummary.value.configurationName || '打卡详情'}`
})

// 获取每日汇总列表
const fetchSummaries = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...searchForm
    }
    
    // 根据用户角色选择不同的API端点
    const endpoint = userStore.isAdmin ? '/daily-checkin/summaries' : '/daily-checkin/user-summaries'
    const response = await request.get(endpoint, { params })
    
    summaries.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
    
  } catch (error) {
    console.error('获取每日汇总失败:', error)
    ElMessage.error('获取每日汇总失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 显示用户详情
const showUserDetails = async (summary) => {
  try {
    selectedSummary.value = summary
    detailDialogVisible.value = true
    
    // 如果没有用户状态数据，则加载详细信息
    if (!summary.userStatuses || summary.userStatuses.length === 0) {
      detailLoading.value = true
      const response = await request.get(`/daily-checkin/detail/${summary.date}`)
      selectedSummary.value = response.data
      detailLoading.value = false
    }
  } catch (error) {
    console.error('加载用户详情失败:', error)
    ElMessage.error('加载详情失败: ' + (error.response?.data?.message || error.message))
    detailLoading.value = false
  }
}

// 关闭弹窗
const handleCloseDialog = () => {
  detailDialogVisible.value = false
  selectedSummary.value = null
}

// 删除当天所有打卡记录
const handleDeleteDailyRecords = async (summary) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${summary.date} 当天的所有打卡记录吗？此操作不可撤销！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )

    console.log('开始删除当天记录:', summary.date)
    
    const response = await request.delete(`/daily-checkin/delete-daily-records`, {
      params: { date: summary.date }
    })

    console.log('删除API响应:', response)

    // 修复：正确检查响应格式，request.js已经处理了ApiResponse结构
    if (response && response.success !== false) {
      console.log('响应数据:', response.data)
      ElMessage.success(response.message || response.data || '删除成功')
      // 重新获取数据
      await fetchSummaries()
    } else {
      ElMessage.error(response?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除当天记录失败:', error)
      console.error('错误详情:', {
        message: error.message,
        response: error.response,
        status: error.response?.status,
        data: error.response?.data
      })
      
      if (error.response?.data?.message) {
        ElMessage.error(`删除失败: ${error.response.data.message}`)
      } else {
        ElMessage.error('删除失败，请重试')
      }
    }
  }
}

// 判断是否可以删除记录
const canDeleteRecord = (userId) => {
  return userStore.isAdmin || userId === userStore.userInfo?.id
}

// 删除用户记录
const handleDeleteUserRecord = async (userStatus) => {
  try {
    await ElMessageBox.confirm(
      `确认删除 ${userStatus.userName} 在 ${formatDate(selectedSummary.value.date)} 的打卡记录吗？`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    // 这里需要根据用户状态找到对应的记录ID
    // 由于我们只有用户状态信息，需要从records中找到对应的记录
    const record = selectedSummary.value.records?.find(r => r.userId === userStatus.userId)
    if (!record) {
      ElMessage.error('找不到对应的打卡记录')
      return
    }
    
    const response = await request.delete(`/checkin/records/${record.id}`)
    
    if (response && response.success !== false) {
      ElMessage.success('记录删除成功')
      // 重新加载详情和列表
      await showUserDetails(selectedSummary.value)
      fetchSummaries()
    } else {
      ElMessage.error(response?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除记录失败:', error)
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

// 处理日期范围变化
const handleDateRangeChange = (dates) => {
  if (dates && dates.length === 2) {
    searchForm.startDate = dates[0]
    searchForm.endDate = dates[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchSummaries()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    startDate: '',
    endDate: ''
  })
  dateRange.value = []
  pagination.page = 1
  fetchSummaries()
}


// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchSummaries()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  fetchSummaries()
}

// 格式化函数
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const formatTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${month}/${day} ${hour}:${minute}`
}

const formatDuration = (minutes) => {
  if (!minutes) return ''
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return hours > 0 ? `${hours}小时${mins}分钟` : `${mins}分钟`
}

const formatRate = (rate) => {
  if (rate === null || rate === undefined) return '0%'
  return `${(rate * 100).toFixed(1)}%`
}

const getWeekday = (date) => {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return weekdays[new Date(date).getDay()]
}

const getRateClass = (rate) => {
  if (rate >= 0.9) return 'excellent'
  if (rate >= 0.8) return 'good'
  if (rate >= 0.6) return 'normal'
  return 'poor'
}

const getStatusClass = (status) => {
  return `status-${status?.toLowerCase().replace('_', '-')}`
}

const getStatusTagType = (status, auditStatus) => {
  // 待审核状态使用警告色
  if (auditStatus === 'PENDING') {
    return 'warning'
  }
  
  // 审核拒绝后，根据实际签到状态显示颜色（通常是danger，因为status是ABSENT）
  // 审核通过或无需审核，也根据实际签到状态显示颜色
  const typeMap = {
    'NORMAL': 'success',
    'LATE': 'warning',
    'CHECKED_IN': 'success',
    'ON_LEAVE': 'info',
    'ABSENT': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status, auditStatus) => {
  // 待审核状态优先显示
  if (auditStatus === 'PENDING') {
    return '待审核'
  }
  
  // 审核拒绝后，显示实际的签到状态（通常是"缺勤"）
  // 审核通过或无需审核，也显示实际的签到状态
  const textMap = {
    'NORMAL': '已签到',
    'LATE': '迟到',
    'CHECKED_IN': '已签到',
    'ON_LEAVE': '请假',
    'ABSENT': '缺勤'
  }
  return textMap[status] || '未知'
}

const getAvatarText = (userName) => {
  if (!userName || userName === '未知用户') return '?'
  return userName.charAt(0).toUpperCase()
}

const getAvatarColor = (status) => {
  const colorMap = {
    'CHECKED_IN': '#67c23a',
    'ON_LEAVE': '#409eff',
    'ABSENT': '#f56c6c'
  }
  return colorMap[status] || '#909399'
}

// 导出当日打卡数据
const exportDailyData = async () => {
  // 权限检查
  if (!userStore.isAdmin) {
    ElMessage.error('只有管理员才能导出数据')
    return
  }

  if (!selectedSummary.value) {
    ElMessage.warning('请先选择要导出的日期')
    return
  }

  try {
    const response = await request.get('/daily-checkin/export', {
      params: { date: selectedSummary.value.date },
      responseType: 'blob'
    })

    // 创建下载链接
    const blob = new Blob([response.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `每日打卡汇总_${selectedSummary.value.date}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请重试')
  }
}

// 生命周期
onMounted(() => {
  fetchSummaries()
})
</script>

<style scoped>
.daily-checkin-records {
  padding: 20px 12px 32px;
  max-width: 1320px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 18px;
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 18px 46px rgba(18, 174, 231, 0.08);
  overflow: hidden;
}

.search-card :deep(.el-card__header) {
  padding: 18px 22px;
  border-bottom: 1px solid rgba(98, 177, 210, 0.18);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(236, 250, 255, 0.72));
}

.search-card :deep(.el-card__body) {
  padding: 18px 22px 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #123044;
  font-size: 16px;
  font-weight: 800;
}

.search-form {
  margin-bottom: 0;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.search-form :deep(.el-form-item__label) {
  color: #496579;
  font-weight: 800;
}

.search-form :deep(.el-input__wrapper) {
  min-height: 42px;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.88);
}

.search-form :deep(.el-button) {
  height: 42px;
  padding: 0 18px;
  border-radius: 14px;
  font-weight: 800;
}

.search-form :deep(.el-button--primary) {
  color: var(--button-primary-text);
  background: var(--button-primary-bg);
  border-color: var(--button-primary-border);
  box-shadow: 0 10px 22px rgba(24, 185, 236, 0.1);
}

.summaries-card {
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 24px 70px rgba(18, 85, 116, 0.12);
  overflow: hidden;
}

.summaries-card :deep(.el-card__body) {
  padding: 16px;
}

.summaries-list {
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.summary-card {
  position: relative;
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 20px;
  background:
    radial-gradient(circle at 98% 0%, rgba(75, 211, 180, 0.12), transparent 30%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(241, 252, 255, 0.84));
  box-shadow: 0 12px 30px rgba(18, 174, 231, 0.06);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
  overflow: hidden;
}

.summary-card::before {
  content: '';
  position: absolute;
  inset: 0 auto 0 0;
  width: 5px;
  background: linear-gradient(180deg, #18b9ec, #4bd3b4);
  opacity: 0.82;
}

.summary-card:hover {
  border-color: rgba(24, 185, 236, 0.34);
  box-shadow: 0 18px 42px rgba(18, 174, 231, 0.12);
  transform: translateY(-1px);
}

.card-header-section {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) minmax(430px, 1.35fr) auto;
  align-items: center;
  gap: 18px;
  padding: 20px 22px 20px 26px;
  background: transparent;
}

.date-info {
  min-width: 0;
}

.date-main {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.date-main .el-icon {
  color: #0876a5;
}

.date-text {
  color: #123044;
  font-size: 22px;
  font-weight: 850;
  letter-spacing: 0;
}

.weekday {
  padding: 4px 12px;
  color: #496579;
  background: rgba(241, 245, 249, 0.9);
  border: 1px solid rgba(98, 177, 210, 0.12);
  border-radius: 999px;
  font-size: 13px;
  font-weight: 800;
}

.config-info {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.config-info .info-item {
  display: flex;
  align-items: center;
  gap: 7px;
  color: #5b7588;
  font-size: 14px;
  font-weight: 650;
}

.config-info .info-item .el-icon {
  color: #8aa5b7;
  font-size: 16px;
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(6, minmax(58px, 1fr));
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 54px;
  gap: 3px;
  padding: 7px 8px;
  border: 1px solid rgba(98, 177, 210, 0.16);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.66);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.82);
}

.stat-label {
  color: #5b7588;
  font-size: 12px;
  font-weight: 750;
  white-space: nowrap;
}

.stat-value {
  color: #123044;
  font-family: var(--font-family-mono);
  font-size: 18px;
  font-weight: 800;
  line-height: 1.1;
}

.stat-value.total {
  color: #123044;
}

.stat-value.checked {
  color: #0f8f72;
}

.stat-value.pending {
  color: #9a640d;
}

.stat-value.late {
  color: #9a640d;
}

.stat-value.leave {
  color: #0876a5;
}

.stat-value.absent {
  color: #b4233e;
}

.stat-value.rate.excellent {
  color: #0f8f72;
}

.stat-value.rate.good {
  color: #0876a5;
}

.stat-value.rate.normal {
  color: #9a640d;
}

.stat-value.rate.poor {
  color: #b4233e;
}

.action-section {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  min-width: 250px;
}

.action-section :deep(.el-button) {
  height: 40px;
  margin: 0;
  padding: 0 16px;
  border-radius: 999px;
  font-weight: 850;
}

.action-section :deep(.el-button--primary) {
  color: var(--button-primary-text);
  background: var(--button-primary-bg);
  border-color: var(--button-primary-border);
  box-shadow: 0 12px 28px rgba(24, 185, 236, 0.12);
}

.action-section :deep(.el-button--danger.is-plain) {
  color: var(--button-danger-text);
  background: var(--button-danger-bg);
  border-color: var(--button-danger-border);
  box-shadow: 0 12px 28px rgba(240, 82, 104, 0.1);
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(98, 177, 210, 0.18);
}

/* 弹窗样式 */
:deep(.user-detail-dialog.el-dialog),
.user-detail-dialog :deep(.el-dialog) {
  width: min(92vw, 1360px) !important;
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 26px;
  background:
    radial-gradient(circle at 88% 8%, rgba(75, 211, 180, 0.14), transparent 34%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(242, 252, 255, 0.9));
  box-shadow: 0 36px 90px rgba(18, 85, 116, 0.22);
  overflow: hidden;
}

:deep(.user-detail-dialog .el-dialog__header) {
  padding: 22px 26px 18px;
  margin: 0;
  border-bottom: 1px solid rgba(98, 177, 210, 0.18);
}

:deep(.user-detail-dialog .el-dialog__body) {
  padding: 0 22px 18px;
  background: linear-gradient(180deg, rgba(240, 251, 255, 0.38), rgba(255, 255, 255, 0.18));
}

:deep(.user-detail-dialog .el-dialog__footer) {
  padding: 14px 26px 20px;
  border-top: 1px solid rgba(98, 177, 210, 0.18);
  background: rgba(255, 255, 255, 0.66);
}

:deep(.user-detail-dialog .el-dialog__headerbtn) {
  top: 18px;
  right: 18px;
  width: 36px;
  height: 36px;
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.74);
}

:deep(.user-detail-dialog .el-dialog__footer .el-button) {
  height: 40px;
  padding: 0 20px;
  border-radius: 999px;
  color: #496579;
  font-weight: 850;
  background: rgba(255, 255, 255, 0.78);
  border-color: rgba(98, 177, 210, 0.24);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 18px;
  padding-right: 44px;
  color: #123044;
  font-size: 20px;
  font-weight: 850;
}

.dialog-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dialog-actions :deep(.el-button) {
  height: 42px;
  border-radius: 999px;
  color: var(--button-success-text);
  font-weight: 850;
  background: var(--button-success-bg);
  border-color: var(--button-success-border);
  box-shadow: 0 12px 28px rgba(33, 185, 139, 0.12);
}

.dialog-loading {
  padding: 40px 0;
}

.user-details-content {
  max-height: 70vh;
  overflow-y: auto;
  padding: 18px 0 0;
}

.detail-header {
  margin-bottom: 18px;
  padding: 20px 22px 22px;
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 24px;
  background:
    radial-gradient(circle at 88% 8%, rgba(75, 211, 180, 0.16), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(236, 250, 255, 0.72));
  box-shadow: 0 14px 36px rgba(18, 174, 231, 0.08);
}

.header-info h3 {
  margin: 0 0 14px;
  color: #123044;
  font-size: 22px;
  font-weight: 850;
}

.header-stats {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.header-stats :deep(.el-tag) {
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  font-weight: 850;
}

.user-status-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
  max-height: 52vh;
  overflow-y: auto;
  padding-right: 6px;
}

.user-status-item {
  position: relative;
  display: grid;
  grid-template-columns: minmax(220px, 0.9fr) minmax(320px, 1.8fr) auto;
  align-items: center;
  gap: 18px;
  min-height: 76px;
  padding: 14px 18px;
  border: 1px solid rgba(98, 177, 210, 0.16);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 10px 24px rgba(18, 174, 231, 0.06);
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
  overflow: hidden;
}

.user-status-item::before {
  content: '';
  position: absolute;
  inset: 0 auto 0 0;
  width: 4px;
  background: rgba(118, 145, 164, 0.5);
}

.user-status-item:hover {
  border-color: rgba(24, 185, 236, 0.32);
  box-shadow: 0 16px 34px rgba(18, 174, 231, 0.1);
  transform: translateY(-1px);
}

.status-normal,
.status-checked-in {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(240, 253, 248, 0.72));
}

.status-late {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 250, 240, 0.72));
}

.status-on-leave {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(239, 252, 255, 0.72));
}

.status-absent {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 245, 247, 0.72));
}

.status-normal::before,
.status-checked-in::before {
  background: #21b98b;
}

.status-late::before {
  background: #f4b942;
}

.status-on-leave::before {
  background: #12aee7;
}

.status-absent::before {
  background: #f05268;
}

/* 用户头部信息 */
.user-header {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.user-header :deep(.el-avatar) {
  flex: 0 0 auto;
  width: 46px !important;
  height: 46px !important;
  border: 3px solid rgba(255, 255, 255, 0.86);
  box-shadow: 0 8px 18px rgba(18, 85, 116, 0.12);
  font-size: 17px;
  font-weight: 850;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.user-name {
  color: #123044;
  font-size: 15px;
  font-weight: 850;
  display: flex;
  align-items: center;
  gap: 8px;
  line-height: 1.4;
}

.user-department {
  font-size: 12px;
  color: #5b7588;
  padding: 3px 8px;
  background: rgba(241, 245, 249, 0.86);
  border: 1px solid rgba(98, 177, 210, 0.12);
  border-radius: 999px;
  font-weight: 750;
  white-space: nowrap;
}

.user-status-tags {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.user-status-tags :deep(.el-tag) {
  height: 23px;
  border-radius: 999px;
  font-weight: 800;
}

/* 紧凑的详细信息 */
.user-details-compact {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
  font-size: 13px;
}

.info-group {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.audit-info {
  padding: 9px 12px;
  background: rgba(255, 250, 240, 0.88);
  border: 1px solid rgba(244, 185, 66, 0.3);
  border-radius: 14px;
}

.info-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #496579;
  font-weight: 650;
  white-space: nowrap;
}

.info-item .el-icon {
  font-size: 15px;
  color: #8aa5b7;
}

.info-item.duration {
  color: #0f8f72;
  font-weight: 850;
}

.info-item.audit-notes {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: help;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-end;
}

.action-buttons :deep(.el-button) {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  color: var(--button-danger-text);
  background: var(--button-danger-bg);
  border-color: var(--button-danger-border);
  box-shadow: 0 10px 22px rgba(240, 82, 104, 0.1);
}

/* 响应式设计 */
@media (max-width: 1180px) {
  .card-header-section {
    grid-template-columns: 1fr;
    align-items: stretch;
  }

  .stats-section {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .action-section {
    justify-content: flex-start;
    min-width: 0;
  }

  .user-status-item {
    grid-template-columns: minmax(190px, 0.9fr) minmax(260px, 1.6fr) auto;
  }
}

@media (max-width: 768px) {
  .daily-checkin-records {
    padding: 10px;
  }
  
  .card-header-section {
    grid-template-columns: 1fr;
    gap: 16px;
    align-items: stretch;
    padding: 18px;
  }
  
  .stats-section {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .action-section {
    flex-direction: column;
    align-items: stretch;
  }

  .action-section :deep(.el-button) {
    width: 100%;
  }
  
  .user-status-item {
    grid-template-columns: 1fr;
    align-items: flex-start;
    gap: 10px;
    padding: 14px 14px 14px 16px;
  }
  
  .user-header {
    min-width: auto;
    width: 100%;
  }
  
  .user-details-compact {
    width: 100%;
  }
  
  .info-group {
    gap: 12px;
  }
  
  .info-item {
    font-size: 12px;
  }
  
  .action-buttons {
    align-self: flex-end;
  }
  
  :deep(.user-detail-dialog.el-dialog),
  .user-detail-dialog :deep(.el-dialog) {
    width: 95vw !important;
    border-radius: 22px;
  }

  .dialog-header {
    flex-direction: column;
    align-items: flex-start;
    padding-right: 34px;
  }

  .dialog-actions {
    width: 100%;
  }

  .dialog-actions :deep(.el-button) {
    width: 100%;
  }

  .detail-header {
    padding: 16px;
  }
}
</style>
