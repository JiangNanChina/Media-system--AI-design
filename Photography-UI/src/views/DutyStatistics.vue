<template>
  <div id="duty-statistics-page" class="duty-statistics-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon><TrendCharts /></el-icon>
            执勤统计
          </h1>
          <p class="page-description">查看执勤数据分析和报表</p>
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
          <el-button
            class="export-report-btn"
            type="primary"
            @click="exportReport"
            :icon="Download"
            :loading="exportLoading"
          >
            导出报表
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="overview-section">
      <h2 class="section-title">
        <el-icon><DataAnalysis /></el-icon>
        数据概览
      </h2>
      <div class="overview-grid">
        <div class="overview-grid-item">
          <div class="overview-card overview-card--schedule">
            <SkeletonLoader v-if="loading" type="stats" />
            <div v-else class="card-content">
              <div class="card-value">{{ overview.totalSchedules || 0 }}</div>
              <div class="card-label">总排班数</div>
              <div class="card-icon">
                <el-icon><Calendar /></el-icon>
              </div>
            </div>
          </div>
        </div>
        
        <div class="overview-grid-item">
          <div class="overview-card overview-card--record">
            <SkeletonLoader v-if="loading" type="stats" />
            <div v-else class="card-content">
              <div class="card-value">{{ overview.totalRecords || 0 }}</div>
              <div class="card-label">总执勤数</div>
              <div class="card-icon">
                <el-icon><UserFilled /></el-icon>
              </div>
            </div>
          </div>
        </div>
        
        <div class="overview-grid-item">
          <div class="overview-card completed overview-card--completed">
            <SkeletonLoader v-if="loading" type="stats" />
            <div v-else class="card-content">
              <div class="card-value">{{ overview.completedDuties || 0 }}</div>
              <div class="card-label">已完成</div>
              <div class="card-icon">
                <el-icon><CircleCheck /></el-icon>
              </div>
            </div>
          </div>
        </div>
        
        <div class="overview-grid-item">
          <div class="overview-card missed overview-card--missed">
            <SkeletonLoader v-if="loading" type="stats" />
            <div v-else class="card-content">
              <div class="card-value">{{ overview.missedDuties || 0 }}</div>
              <div class="card-label">缺勤数</div>
              <div class="card-icon">
                <el-icon><WarningFilled /></el-icon>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="analytics-grid">
        <!-- 执勤完成率趋势 -->
        <el-card class="chart-card trend-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">执勤完成率趋势</span>
              <el-tag type="success" size="small">
                {{ completionRate }}%
              </el-tag>
            </div>
          </template>
          <div class="chart-container trend-container">
            <div v-if="chartLoading" class="chart-loading">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="completionTrendData.length === 0" class="chart-empty">
              <EmptyState type="no-data" description="暂无数据" size="small" />
            </div>
            <div v-else class="completion-trend">
              <!-- 这里可以集成 ECharts 或其他图表库 -->
              <div class="trend-item" v-for="item in completionTrendData" :key="item.date">
                <div class="trend-date">{{ item.date }}</div>
                <div class="trend-bar">
                  <div class="trend-progress" :style="{ width: item.rate + '%' }"></div>
                </div>
                <div class="trend-rate">{{ item.rate }}%</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 人员执勤分布 -->
        <el-card class="chart-card distribution-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">人员执勤分布</span>
            </div>
          </template>
          <div class="chart-container distribution-container">
            <div v-if="chartLoading" class="chart-loading">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="userDistributionData.length === 0" class="chart-empty">
              <EmptyState type="no-data" description="暂无数据" size="small" />
            </div>
            <div v-else class="user-distribution">
              <div class="distribution-item" v-for="item in userDistributionData" :key="item.userId">
                <div class="user-info">
                  <span class="user-name">{{ item.userName }}</span>
                  <span class="user-count">{{ item.count }}次</span>
                </div>
                <div class="distribution-bar">
                  <div class="distribution-progress" :style="{ width: item.percentage + '%' }"></div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 详细数据表格 -->
    <div class="table-section">
      <h2 class="section-title">
        <el-icon><List /></el-icon>
        详细数据
      </h2>
      <el-card class="table-card" shadow="hover">
        <template #header>
          <div class="table-header">
            <span class="table-title">执勤记录详情</span>
            <div class="table-actions">
              <el-input
                v-model="tableSearch.keyword"
                placeholder="搜索用户姓名"
                clearable
                style="width: 200px; margin-right: 16px;"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select
                v-model="tableSearch.status"
                placeholder="执勤状态"
                clearable
                style="width: 120px;"
              >
                <el-option label="已完成" value="已完成" />
                <el-option label="执勤中" value="执勤中" />
                <el-option label="缺勤" value="缺勤" />
              </el-select>
            </div>
          </div>
        </template>
        
        <el-table 
          :data="tableData" 
          v-loading="tableLoading"
          stripe
          style="width: 100%"
        >
          <el-table-column label="执勤人员" width="120">
            <template #default="{ row }">
              {{ getUserName(row) }}
            </template>
          </el-table-column>
          <el-table-column label="部门" width="120">
            <template #default="{ row }">
              {{ getDepartmentName(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="dutyDate" label="执勤日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.dutyDate) }}
            </template>
          </el-table-column>
          <el-table-column label="执勤时段" width="180">
            <template #default="{ row }">
              {{ getDutyTimeRange(row) }}
            </template>
          </el-table-column>
          <el-table-column label="签到时间" width="120">
            <template #default="{ row }">
              <span v-if="row.checkinTime">{{ formatTime(row.checkinTime) }}</span>
              <el-tag v-else type="info" size="small">未签到</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="签退时间" width="120">
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
        </el-table>
        
        <div class="table-pagination">
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  TrendCharts, Download, DataAnalysis, Calendar, UserFilled, CircleCheck,
  WarningFilled, List, Search
} from '@element-plus/icons-vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import EmptyState from '@/components/EmptyState.vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(true)
const chartLoading = ref(true)
const tableLoading = ref(true)
const exportLoading = ref(false)

const dateRange = ref([
  new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
  new Date().toISOString().split('T')[0]
])

const overview = ref({})
const completionTrendData = ref([])
const userDistributionData = ref([])
const tableData = ref([])

const tableSearch = reactive({
  keyword: '',
  status: null
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 计算属性
const completionRate = computed(() => {
  if (!overview.value.totalRecords || !overview.value.completedDuties) return 0
  return Math.round((overview.value.completedDuties / overview.value.totalRecords) * 100)
})

// 获取统计概览
const fetchOverview = async () => {
  try {
    const response = await request.get('/duty/statistics')
    overview.value = response.data || {}
  } catch (error) {
    ElMessage.error('获取统计概览失败')
  } finally {
    loading.value = false
  }
}

// 获取图表数据（改为真实数据）
const fetchChartData = async () => {
  try {
    // 1) 完成率趋势：按周统计最近5周完成率
    const trendResp = await request.get('/duty/statistics')
    // 这里后端已提供 today/thisWeek/total/completed 等聚合；前端按最近五周用 records 重新计算
    // 简化处理：调用明细接口获取近35天数据，再按周聚合
    const end = new Date()
    const start = new Date(end.getTime() - 35 * 24 * 60 * 60 * 1000)
    const params = { startDate: start.toISOString().split('T')[0], endDate: end.toISOString().split('T')[0] }
    const listResp = await request.get('/duty/records', { params: { page: 0, size: 1000, ...params } })
    const items = (listResp.data?.content || [])

    // 按周分组统计完成率
    const weekNames = ['四周前', '三周前', '两周前', '上周', '本周']
    const weeks = [0, 1, 2, 3, 4].map(i => ({ name: weekNames[i], total: 0, completed: 0 }))
    const now = new Date()
    const startOfWeek = (d) => {
      const date = new Date(d)
      const day = (date.getDay() + 6) % 7 // 周一为0
      date.setDate(date.getDate() - day)
      date.setHours(0, 0, 0, 0)
      return date
    }
    const thisWeekStart = startOfWeek(now)
    items.forEach(r => {
      const d = new Date(r.dutyDate || r.duty_date || r.duty_time)
      const diffWeeks = Math.floor((thisWeekStart - startOfWeek(d)) / (7 * 24 * 60 * 60 * 1000))
      const bucket = 4 - diffWeeks
      if (bucket >= 0 && bucket < 5) {
        weeks[bucket].total += 1
        if (r.status === '已完成') weeks[bucket].completed += 1
      }
    })
    completionTrendData.value = weeks.map(w => ({ date: w.name, rate: w.total ? Math.round((w.completed / w.total) * 100) : 0 }))

    // 2) 人员分布：统计各用户执勤记录数量
    const map = new Map()
    items.forEach(r => {
      const userName = getUserName(r)
      const key = r.userId || r.user?.id || r.userRealName || r.realName || r.username || userName
      const obj = map.get(key) || { userId: key, userName, count: 0 }
      obj.count += 1
      map.set(key, obj)
    })
    const arr = Array.from(map.values()).sort((a, b) => b.count - a.count).slice(0, 10)
    const max = arr[0]?.count || 1
    userDistributionData.value = arr.map(i => ({ ...i, percentage: Math.round((i.count / max) * 100) }))
  } catch (error) {
    ElMessage.error('获取图表数据失败')
  } finally {
    chartLoading.value = false
  }
}

// 获取表格数据
const fetchTableData = async () => {
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize
    }
    
    if (tableSearch.keyword) {
      params.keyword = tableSearch.keyword
    }
    if (tableSearch.status) {
      params.status = tableSearch.status
    }
    if (dateRange.value && dateRange.value[0] && dateRange.value[1]) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const response = await request.get('/duty/records', { params })
    tableData.value = response.data?.content || []
    pagination.total = response.data?.totalElements || 0
  } catch (error) {
    ElMessage.error('获取执勤记录失败')
  } finally {
    tableLoading.value = false
  }
}

// 导出报表
const exportReport = async () => {
  try {
    exportLoading.value = true
    
    const params = new URLSearchParams()
    if (dateRange.value && dateRange.value[0] && dateRange.value[1]) {
      params.append('startDate', dateRange.value[0])
      params.append('endDate', dateRange.value[1])
    }
    
    const response = await fetch(`/duty/export?${params}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (response.ok) {
      const blob = await response.blob()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.style.display = 'none'
      a.href = url
      a.download = `执勤报表_${dateRange.value[0]}_${dateRange.value[1]}.xlsx`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
      
      ElMessage.success('报表导出成功')
    } else {
      throw new Error('导出失败')
    }
  } catch (error) {
    ElMessage.error('导出报表失败')
  } finally {
    exportLoading.value = false
  }
}

// 事件处理
const handleDateRangeChange = () => {
  fetchOverview()
  fetchChartData()
  fetchTableData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchTableData()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchTableData()
}

// 工具函数
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const getUserName = (row) => {
  return row.userRealName || row.realName || row.user?.realName || row.user?.username || row.username || '未知人员'
}

const getDepartmentName = (row) => {
  return row.departmentName || row.userDepartmentName || row.user?.department?.name || row.department?.name || '-'
}

const getDutyTimeRange = (row) => {
  const start = row.startTime || row.dutySchedule?.startTime || row.scheduleStartTime
  const end = row.endTime || row.dutySchedule?.endTime || row.scheduleEndTime
  if (!start && !end) return '-'
  return `${start || '-'} - ${end || '-'}`
}

const getStatusType = (status) => {
  const statusTypes = {
    '待执勤': 'info',
    '执勤中': 'warning',
    '已完成': 'success',
    '缺勤': 'danger'
  }
  return statusTypes[status] || 'info'
}

// 生命周期
onMounted(() => {
  fetchOverview()
  fetchChartData()
  fetchTableData()
})
</script>

<style scoped>
.duty-statistics-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-description {
  color: #6b7280;
  margin: 0;
  font-size: 14px;
}

.header-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.overview-section,
.charts-section,
.table-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.overview-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e5e7eb;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.overview-card.completed::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #67c23a, #85ce61);
}

.overview-card.missed::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #f56c6c, #f78989);
}

.card-content {
  display: flex;
  flex-direction: column;
}

.card-value {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.card-label {
  font-size: 14px;
  color: #6b7280;
}

.card-icon {
  position: absolute;
  top: 24px;
  right: 24px;
  font-size: 32px;
  color: #d1d5db;
}

.chart-card,
.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header,
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title,
.table-title {
  font-weight: 600;
  color: #1f2937;
}

.table-actions {
  display: flex;
  align-items: center;
}

.chart-container {
  height: 252px;
  display: flex;
  align-items: stretch;
  justify-content: center;
}

.chart-loading,
.chart-empty {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.completion-trend {
  width: 100%;
  min-height: 100%;
  padding: 6px 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 14px;
}

.trend-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.trend-date {
  width: 60px;
  font-size: 12px;
  color: #6b7280;
  flex-shrink: 0;
}

.trend-bar {
  flex: 1;
  height: 9px;
  background: #f1f5f9;
  border-radius: 999px;
  overflow: hidden;
}

.trend-progress {
  height: 100%;
  background: linear-gradient(90deg, #22c55e, #84cc16);
  transition: width 0.3s ease;
}

.trend-rate {
  width: 40px;
  font-size: 12px;
  color: #1f2937;
  font-weight: 500;
  text-align: right;
  flex-shrink: 0;
}

.user-distribution {
  width: 100%;
  padding: 4px 6px 4px 0;
}

.distribution-item {
  margin-bottom: 13px;
}

.user-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.user-name {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.user-count {
  font-size: 12px;
  color: #6b7280;
}

.distribution-bar {
  height: 7px;
  background: #f1f5f9;
  border-radius: 999px;
  overflow: hidden;
}

.distribution-progress {
  height: 100%;
  background: linear-gradient(90deg, #0ea5e9, #7dd3fc);
  transition: width 0.3s ease;
}

.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 22px;
  width: 100%;
}

.overview-grid-item {
  min-width: 0;
}

.analytics-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 22px;
  align-items: start;
}

#duty-statistics-page .chart-card :deep(.el-card__body) {
  padding: 18px 20px 20px !important;
}

.distribution-container {
  justify-content: flex-start;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 2px;
}

.distribution-container::-webkit-scrollbar {
  width: 6px;
}

.distribution-container::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(14, 165, 233, 0.24);
}

#duty-statistics-page .page-header {
  border: 1px solid rgba(14, 165, 233, 0.16) !important;
  border-radius: 24px !important;
  background:
    radial-gradient(circle at 92% 12%, rgba(34, 211, 238, 0.2), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(236, 254, 255, 0.82)) !important;
  box-shadow: 0 20px 48px rgba(14, 116, 144, 0.1) !important;
}

#duty-statistics-page .page-title {
  color: #123044 !important;
  font-weight: 850 !important;
  text-shadow: none !important;
}

#duty-statistics-page .page-title .el-icon {
  color: #0891b2 !important;
}

#duty-statistics-page .page-description {
  color: #496579 !important;
  font-weight: 650 !important;
  text-shadow: none !important;
}

.export-report-btn.el-button {
  height: 38px;
  padding: 0 18px;
  border: 1px solid rgba(14, 165, 233, 0.26) !important;
  border-radius: 999px !important;
  background: linear-gradient(135deg, #e2f8ff 0%, #d6f3ff 52%, #e7fff6 100%) !important;
  color: #075985 !important;
  font-weight: 780 !important;
  box-shadow: 0 10px 22px rgba(14, 116, 144, 0.1) !important;
}

.export-report-btn.el-button:hover {
  border-color: rgba(14, 165, 233, 0.42) !important;
  color: #064e3b !important;
  box-shadow: 0 14px 28px rgba(14, 116, 144, 0.14) !important;
  transform: translateY(-1px);
}

#duty-statistics-page .section-title {
  margin: 0 0 14px !important;
  color: #123044 !important;
  font-size: 19px;
  font-weight: 850 !important;
}

#duty-statistics-page .section-title .el-icon {
  color: #0891b2 !important;
}

#duty-statistics-page .overview-card {
  min-height: 124px;
  padding: 24px !important;
  border: 1px solid rgba(14, 165, 233, 0.14) !important;
  border-radius: 20px !important;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.98), rgba(240, 253, 250, 0.86)) !important;
  box-shadow: 0 18px 38px rgba(14, 116, 144, 0.08) !important;
  color: #123044 !important;
}

#duty-statistics-page .overview-card::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 4px;
  background: var(--duty-stat-accent, #22d3ee) !important;
}

#duty-statistics-page .overview-card:hover {
  border-color: rgba(14, 165, 233, 0.26) !important;
  box-shadow: 0 24px 52px rgba(14, 116, 144, 0.13) !important;
}

.overview-card--schedule {
  --duty-stat-accent: #22d3ee;
  --duty-stat-color: #0369a1;
  --duty-stat-icon-bg: linear-gradient(145deg, rgba(224, 242, 254, 0.98), rgba(207, 250, 254, 0.9));
}

.overview-card--record {
  --duty-stat-accent: #60a5fa;
  --duty-stat-color: #2563eb;
  --duty-stat-icon-bg: linear-gradient(145deg, rgba(219, 234, 254, 0.98), rgba(224, 242, 254, 0.9));
}

.overview-card--completed {
  --duty-stat-accent: #34d399;
  --duty-stat-color: #047857;
  --duty-stat-icon-bg: linear-gradient(145deg, rgba(220, 252, 231, 0.98), rgba(209, 250, 229, 0.9));
}

.overview-card--missed {
  --duty-stat-accent: #fb7185;
  --duty-stat-color: #be123c;
  --duty-stat-icon-bg: linear-gradient(145deg, rgba(255, 241, 242, 0.98), rgba(255, 228, 230, 0.9));
}

#duty-statistics-page .card-content {
  min-height: 76px;
  padding-right: 72px;
  position: relative;
  z-index: 1;
}

#duty-statistics-page .card-value {
  color: #123044 !important;
  font-size: 34px;
  font-weight: 850;
  line-height: 1;
  text-shadow: none !important;
}

#duty-statistics-page .card-label {
  margin-top: 8px;
  color: #496579 !important;
  font-weight: 720;
  opacity: 1;
  text-shadow: none !important;
}

#duty-statistics-page .card-icon {
  top: 50%;
  right: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 18px;
  background: var(--duty-stat-icon-bg);
  color: var(--duty-stat-color, #0891b2) !important;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.86),
    0 14px 28px rgba(14, 116, 144, 0.12);
  transform: translateY(-50%);
}

#duty-statistics-page .card-icon .el-icon {
  color: var(--duty-stat-color, #0891b2) !important;
}

#duty-statistics-page .chart-card,
#duty-statistics-page .table-card {
  border: 1px solid rgba(14, 165, 233, 0.12) !important;
  border-radius: 22px !important;
  background: rgba(255, 255, 255, 0.94) !important;
  box-shadow: 0 20px 44px rgba(14, 116, 144, 0.08) !important;
}

#duty-statistics-page .chart-card :deep(.el-card__header),
#duty-statistics-page .table-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(14, 165, 233, 0.1) !important;
  background: rgba(248, 253, 255, 0.72) !important;
}

.table-card :deep(.el-card__body) {
  overflow-x: auto;
}

.table-card :deep(.el-table) {
  min-width: 1080px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .duty-statistics-container {
    padding: 16px;
  }

  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .analytics-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  #duty-statistics-page .overview-card {
    min-height: 112px;
    padding: 18px !important;
  }

  #duty-statistics-page .card-content {
    min-height: 66px;
    padding-right: 60px;
  }

  #duty-statistics-page .card-value {
    font-size: 26px;
  }

  #duty-statistics-page .card-icon {
    width: 48px;
    height: 48px;
    border-radius: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-right {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
    width: 100%;
  }
  
  .table-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .table-actions {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
  
  .table-actions .el-input,
  .table-actions .el-select {
    width: 100% !important;
  }
  
  .trend-item {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .trend-date {
    width: auto;
  }
  
  .trend-rate {
    width: auto;
    text-align: left;
  }
}

@media (max-width: 480px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
