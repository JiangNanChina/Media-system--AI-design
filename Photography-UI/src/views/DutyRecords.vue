<template>
  <div class="duty-records-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon><DocumentCopy /></el-icon>
            执勤记录
          </h1>
          <p class="page-description">{{ isAdmin ? '查看全员执勤记录' : '查看我的执勤历史记录' }}</p>
        </div>
        <div class="header-right">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateRangeChange"
            style="margin-right: 16px;"
          />
          <el-select
            v-model="statusFilter"
            placeholder="执勤状态"
            clearable
            style="width: 120px; margin-right: 16px;"
            @change="handleStatusChange"
          >
            <el-option label="待执勤" value="待执勤" />
            <el-option label="执勤中" value="执勤中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="缺勤" value="缺勤" />
          </el-select>
          <el-button class="records-search-btn" type="primary" @click="fetchRecords" :icon="Search">
            搜索
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="overview-section">
      <el-row :gutter="24">
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <div class="overview-card total">
            <div class="card-content">
              <div class="card-value">{{ overview.total || 0 }}</div>
              <div class="card-label">总执勤数</div>
              <div class="card-icon">
                <el-icon><Calendar /></el-icon>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <div class="overview-card completed">
            <div class="card-content">
              <div class="card-value">{{ overview.completed || 0 }}</div>
              <div class="card-label">已完成</div>
              <div class="card-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <div class="overview-card ongoing">
            <div class="card-content">
              <div class="card-value">{{ overview.ongoing || 0 }}</div>
              <div class="card-label">执勤中</div>
              <div class="card-icon">
                <el-icon><Clock /></el-icon>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <div class="overview-card missed">
            <div class="card-content">
              <div class="card-value">{{ overview.missed || 0 }}</div>
              <div class="card-label">缺勤数</div>
              <div class="card-icon">
                <el-icon><WarningFilled /></el-icon>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 记录列表 -->
    <div class="records-section">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">执勤记录详情</span>
            <div class="card-actions">
              <el-button class="reset-filter-btn" @click="resetFilters" :icon="Refresh">
                重置筛选
              </el-button>
            </div>
          </div>
        </template>

        <el-table 
          :data="records" 
          v-loading="loading"
          stripe
          style="width: 100%"
          empty-text="暂无执勤记录"
        >
          <!-- 管理员视图下显示执勤人员姓名（使用后端扁平字段 userRealName） -->
          <el-table-column v-if="isAdmin" label="执勤人员" width="120">
            <template #default="{ row }">
              {{ row.userRealName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="dutyDate" label="执勤日期" width="120" sortable>
            <template #default="{ row }">
              {{ formatDate(row.dutyDate) }}
            </template>
          </el-table-column>
          
          <el-table-column label="执勤时段" width="180">
            <template #default="{ row }">
              {{ row.startTime || '-' }} - {{ row.endTime || '-' }}
            </template>
          </el-table-column>
          
          <el-table-column label="签到时间" width="140">
            <template #default="{ row }">
              <span v-if="row.checkinTime">{{ formatTime(row.checkinTime) }}</span>
              <el-tag v-else type="info" size="small">未签到</el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="签退时间" width="140">
            <template #default="{ row }">
              <span v-if="row.checkoutTime">{{ formatTime(row.checkoutTime) }}</span>
              <el-tag v-else-if="row.checkinTime" type="warning" size="small">未签退</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="notes" label="备注" show-overflow-tooltip />
          
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                class="record-detail-btn"
                size="small"
                @click="viewDetail(row)"
                :icon="View"
              >
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="执勤记录详情"
      width="600px"
    >
      <div v-if="selectedRecord" class="record-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="执勤日期">
            {{ formatDate(selectedRecord.dutyDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="执勤状态">
            <el-tag :type="getStatusType(selectedRecord.status)" size="small">
              {{ selectedRecord.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="计划时间">
            {{ selectedRecord.startTime || '-' }} - {{ selectedRecord.endTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="实际签到">
            {{ selectedRecord.checkinTime ? formatTime(selectedRecord.checkinTime) : '未签到' }}
          </el-descriptions-item>
          <el-descriptions-item label="实际签退">
            {{ selectedRecord.checkoutTime ? formatTime(selectedRecord.checkoutTime) : '未签退' }}
          </el-descriptions-item>
          <el-descriptions-item label="执勤时长" v-if="selectedRecord.checkinTime && selectedRecord.checkoutTime">
            {{ calculateDuration(selectedRecord.checkinTime, selectedRecord.checkoutTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ selectedRecord.notes || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DocumentCopy, Search, Refresh, Calendar, CircleCheck, Clock, WarningFilled, View
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(true)
const records = ref([])
const selectedRecord = ref(null)
const showDetailDialog = ref(false)

const dateRange = ref([])
const statusFilter = ref(null)

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 角色信息
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

// 计算概览统计
const overview = computed(() => {
  const total = records.value.length
  const completed = records.value.filter(r => r.status === '已完成').length
  const ongoing = records.value.filter(r => r.status === '执勤中').length
  const missed = records.value.filter(r => r.status === '缺勤').length
  
  return { total, completed, ongoing, missed }
})

// 获取执勤记录
const fetchRecords = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize
    }
    
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const url = isAdmin.value ? '/duty/records' : '/duty/records/my'
    const response = await request.get(url, { params })
    records.value = response.data?.content || []
    pagination.total = response.data?.totalElements || 0
  } catch (error) {
    console.error('获取执勤记录失败:', error)
    ElMessage.error('获取执勤记录失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选
const resetFilters = () => {
  dateRange.value = []
  statusFilter.value = null
  pagination.currentPage = 1
  fetchRecords()
}

// 日期范围变化
const handleDateRangeChange = () => {
  pagination.currentPage = 1
  fetchRecords()
}

// 状态筛选变化
const handleStatusChange = () => {
  pagination.currentPage = 1
  fetchRecords()
}

// 分页变化
const handleSizeChange = (newSize) => {
  pagination.pageSize = newSize
  pagination.currentPage = 1
  fetchRecords()
}

const handleCurrentChange = (newPage) => {
  pagination.currentPage = newPage
  fetchRecords()
}

// 查看详情
const viewDetail = (record) => {
  selectedRecord.value = record
  showDetailDialog.value = true
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    '待执勤': 'info',
    '执勤中': 'warning',
    '已完成': 'success',
    '缺勤': 'danger'
  }
  return typeMap[status] || 'info'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekday = weekdays[date.getDay()]
  const dateString = date.toLocaleDateString('zh-CN')
  return `${dateString} ${weekday}`
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 计算执勤时长
const calculateDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return ''
  
  const start = new Date(startTime)
  const end = new Date(endTime)
  const diffMs = end - start
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  return `${diffHours}小时${diffMinutes}分钟`
}

// 生命周期
onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.duty-records-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

#app .duty-records-container .page-header {
  padding: 30px !important;
  margin-bottom: 24px !important;
  border: 1px solid rgba(14, 165, 233, 0.16) !important;
  border-radius: 24px !important;
  background:
    radial-gradient(circle at 92% 12%, rgba(34, 211, 238, 0.2), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(236, 254, 255, 0.82)) !important;
  color: #123044 !important;
  box-shadow: 0 20px 48px rgba(14, 116, 144, 0.1) !important;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.header-left .page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
  color: #123044 !important;
  text-shadow: none !important;
}

.header-left .page-title .el-icon {
  color: #0891b2 !important;
}

.header-left .page-description {
  font-size: 16px;
  opacity: 1;
  margin: 0;
  color: #496579 !important;
  font-weight: 650;
  text-shadow: none !important;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.records-search-btn.el-button,
.reset-filter-btn.el-button,
.record-detail-btn.el-button {
  border-radius: 999px !important;
  border: 1px solid rgba(14, 165, 233, 0.26) !important;
  background: linear-gradient(135deg, #e2f8ff 0%, #d6f3ff 52%, #e7fff6 100%) !important;
  color: #075985 !important;
  font-weight: 750 !important;
  box-shadow: 0 10px 22px rgba(14, 116, 144, 0.1) !important;
}

.records-search-btn.el-button:hover,
.reset-filter-btn.el-button:hover,
.record-detail-btn.el-button:hover {
  border-color: rgba(14, 165, 233, 0.42) !important;
  color: #064e3b !important;
  box-shadow: 0 14px 28px rgba(14, 116, 144, 0.14) !important;
  transform: translateY(-1px);
}

.overview-section {
  margin-bottom: 24px;
}

#app .duty-records-container .overview-card {
  min-height: 124px;
  padding: 24px !important;
  border: 1px solid rgba(14, 165, 233, 0.14) !important;
  border-radius: 20px !important;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.98), rgba(240, 253, 250, 0.86)) !important;
  box-shadow: 0 18px 38px rgba(14, 116, 144, 0.08) !important;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  color: #123044 !important;
}

#app .duty-records-container .overview-card::before {
  content: '';
  position: absolute;
  inset: 18px auto 18px 0;
  width: 4px;
  border-radius: 0 999px 999px 0;
  background: var(--record-stat-accent, #22d3ee);
}

#app .duty-records-container .overview-card:hover {
  transform: translateY(-2px);
  border-color: rgba(14, 165, 233, 0.26) !important;
  box-shadow: 0 24px 52px rgba(14, 116, 144, 0.13) !important;
}

#app .duty-records-container .overview-card.total {
  --record-stat-accent: #22d3ee;
  --record-stat-color: #0369a1;
  --record-stat-icon-bg: linear-gradient(145deg, rgba(224, 242, 254, 0.98), rgba(207, 250, 254, 0.9));
}

#app .duty-records-container .overview-card.completed {
  --record-stat-accent: #34d399;
  --record-stat-color: #047857;
  --record-stat-icon-bg: linear-gradient(145deg, rgba(220, 252, 231, 0.98), rgba(209, 250, 229, 0.9));
}

#app .duty-records-container .overview-card.ongoing {
  --record-stat-accent: #fbbf24;
  --record-stat-color: #b45309;
  --record-stat-icon-bg: linear-gradient(145deg, rgba(254, 249, 195, 0.98), rgba(255, 237, 213, 0.9));
}

#app .duty-records-container .overview-card.missed {
  --record-stat-accent: #fb7185;
  --record-stat-color: #be123c;
  --record-stat-icon-bg: linear-gradient(145deg, rgba(255, 241, 242, 0.98), rgba(255, 228, 230, 0.9));
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  min-height: 76px;
  padding-right: 72px;
  text-align: left;
  position: relative;
  z-index: 1;
}

.card-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #123044 !important;
  line-height: 1;
  text-shadow: none !important;
}

.card-label {
  font-size: 14px;
  opacity: 1;
  color: #496579 !important;
  font-weight: 700;
  text-shadow: none !important;
}

.card-icon {
  position: absolute;
  top: 50%;
  right: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 18px;
  background: var(--record-stat-icon-bg);
  color: var(--record-stat-color, #0891b2) !important;
  opacity: 1;
  font-size: 26px;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.86),
    0 14px 28px rgba(14, 116, 144, 0.12);
  transform: translateY(-50%);
}

.card-icon .el-icon {
  color: var(--record-stat-color, #0891b2) !important;
}

.records-section {
  margin-bottom: 24px;
}

.records-section :deep(.el-card) {
  border: 1px solid rgba(14, 165, 233, 0.12) !important;
  border-radius: 22px !important;
  background: rgba(255, 255, 255, 0.94) !important;
  box-shadow: 0 20px 44px rgba(14, 116, 144, 0.08) !important;
}

.records-section :deep(.el-card__header) {
  border-bottom: 1px solid rgba(14, 165, 233, 0.1) !important;
  background: rgba(248, 253, 255, 0.72) !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-actions {
  display: flex;
  gap: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.record-detail {
  padding: 16px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .duty-records-container {
    padding: 16px;
  }
  
  .page-header {
    padding: 20px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-left .page-title {
    font-size: 24px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-start;
  }
  
  .overview-card {
    padding: 16px;
  }
  
  .card-value {
    font-size: 24px;
  }
}
</style>
