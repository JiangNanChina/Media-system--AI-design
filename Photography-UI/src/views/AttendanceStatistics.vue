<template>
  <div class="attendance-statistics">
    <!-- 查询条件 -->
    <el-card class="search-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>考勤统计</span>
        </div>
      </template>
      
      <el-form :model="searchForm" :inline="true" class="search-form">
        <el-form-item label="打卡配置">
          <el-select 
            v-model="searchForm.configId" 
            placeholder="请选择打卡配置" 
            clearable
            style="width: 200px"
            @change="handleConfigChange"
          >
            <el-option
              v-for="config in configList"
              :key="config.id"
              :label="config.name"
              :value="config.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="统计日期">
          <el-date-picker
            v-model="searchForm.date"
            type="date"
            placeholder="请选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadStatistics" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计概览 -->
    <el-card v-if="statistics" class="overview-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ statistics.configName }} - {{ statistics.statisticsDate }}</span>
        </div>
      </template>
      
      <el-row :gutter="20" class="statistics-overview">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ statistics.requiredCount }}</div>
            <div class="stat-label">应到人数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item success">
            <div class="stat-value">{{ statistics.actualCount }}</div>
            <div class="stat-label">实到人数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item warning">
            <div class="stat-value">{{ statistics.leaveCount }}</div>
            <div class="stat-label">请假人数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item danger">
            <div class="stat-value">{{ statistics.absentCount }}</div>
            <div class="stat-label">缺勤人数</div>
          </div>
        </el-col>
      </el-row>
      
      <el-row style="margin-top: 20px">
        <el-col :span="24">
          <div class="attendance-rate">
            <span>出勤率：</span>
            <el-progress 
              :percentage="statistics.attendanceRate" 
              :color="getProgressColor(statistics.attendanceRate)"
              :format="formatProgress"
            />
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 详细列表 -->
    <el-card v-if="statistics && statistics.userDetails" class="details-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>考勤详情</span>
        </div>
      </template>
      
      <el-table 
        :data="statistics.userDetails" 
        style="width: 100%"
        :default-sort="{ prop: 'status', order: 'ascending' }"
      >
        <el-table-column prop="userName" label="姓名" width="120" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column label="考勤状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="打卡时间" width="160">
          <template #default="{ row }">
            <span v-if="row.checkinTime">
              {{ formatTime(row.checkinTime) }}
            </span>
            <span v-else style="color: #999">未打卡</span>
          </template>
        </el-table-column>
        <el-table-column label="迟到信息" width="100">
          <template #default="{ row }">
            <span v-if="row.isLate" class="late-info">
              迟到{{ row.lateMinutes }}分钟
            </span>
            <span v-else-if="row.status === 'PRESENT'">正常</span>
          </template>
        </el-table-column>
        <el-table-column label="请假信息" min-width="200">
          <template #default="{ row }">
            <div v-if="row.status === 'LEAVE'">
              <div><strong>{{ row.leaveType }}</strong></div>
              <div class="leave-reason">{{ row.leaveReason }}</div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 空状态 -->
    <EmptyState 
      v-if="!loading && !statistics"
      type="no-data"
      title="暂无考勤数据"
      description="请选择打卡配置和日期查询考勤统计"
      size="large"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/request'
import EmptyState from '@/components/EmptyState.vue'

// 响应式数据
const loading = ref(false)
const configList = ref([])
const statistics = ref(null)

const searchForm = reactive({
  configId: null,
  date: new Date().toISOString().split('T')[0] // 默认今天
})

// 生命周期
onMounted(() => {
  loadConfigList()
})

// 方法
const loadConfigList = async () => {
  try {
    const response = await request.get('/checkin/configurations')
    if (response && response.success !== false) {
      const data = response.data || response || []
      configList.value = Array.isArray(data) ? data : []
    } else {
      ElMessage.error(response?.message || '获取配置列表失败')
    }
  } catch (error) {
    console.error('获取配置列表失败:', error)
    ElMessage.error('获取配置列表失败')
  }
}

const loadStatistics = async () => {
  if (!searchForm.configId || !searchForm.date) {
    ElMessage.warning('请选择打卡配置和日期')
    return
  }

  try {
    loading.value = true
    const response = await request.get('/attendance/statistics', {
      params: {
        configId: searchForm.configId,
        date: searchForm.date
      }
    })
    
    if (response && response.success !== false) {
      statistics.value = response.data || response
      console.log('考勤统计数据:', statistics.value)
    } else {
      ElMessage.error(response?.message || '获取考勤统计失败')
      statistics.value = null
    }
  } catch (error) {
    console.error('获取考勤统计失败:', error)
    ElMessage.error('获取考勤统计失败')
    statistics.value = null
  } finally {
    loading.value = false
  }
}

const handleConfigChange = () => {
  if (searchForm.configId && searchForm.date) {
    loadStatistics()
  }
}

const handleDateChange = () => {
  if (searchForm.configId && searchForm.date) {
    loadStatistics()
  }
}

const handleReset = () => {
  searchForm.configId = null
  searchForm.date = new Date().toISOString().split('T')[0]
  statistics.value = null
}

// 工具方法
const getStatusType = (status) => {
  const typeMap = {
    PRESENT: 'success',
    LATE: 'warning', 
    LEAVE: 'info',
    ABSENT: 'danger'
  }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    PRESENT: '正常出勤',
    LATE: '迟到',
    LEAVE: '请假',
    ABSENT: '缺勤'
  }
  return textMap[status] || status
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    return date.toLocaleTimeString('zh-CN', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  } catch (error) {
    return timeStr
  }
}

const getProgressColor = (percentage) => {
  if (percentage >= 90) return '#67c23a'
  if (percentage >= 80) return '#e6a23c'
  if (percentage >= 60) return '#f56c6c'
  return '#f56c6c'
}

const formatProgress = (percentage) => {
  return `${percentage}%`
}
</script>

<style lang="scss" scoped>
.attendance-statistics {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.overview-card {
  margin-bottom: 20px;
}

.statistics-overview {
  .stat-item {
    text-align: center;
    padding: 20px;
    border-radius: 8px;
    background: #f8f9fa;
    border-left: 4px solid #409eff;
    
    &.success {
      border-left-color: #67c23a;
    }
    
    &.warning {
      border-left-color: #e6a23c;
    }
    
    &.danger {
      border-left-color: #f56c6c;
    }
    
    .stat-value {
      font-size: 28px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 8px;
    }
    
    .stat-label {
      font-size: 14px;
      color: #606266;
    }
  }
}

.attendance-rate {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  
  :deep(.el-progress) {
    flex: 1;
  }
}

.details-card {
  margin-bottom: 20px;
}

.late-info {
  color: #e6a23c;
  font-size: 12px;
}

.leave-reason {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
  max-width: 200px;
  word-break: break-all;
}

.search-form {
  .el-form-item {
    margin-bottom: 0;
  }
}
</style>
