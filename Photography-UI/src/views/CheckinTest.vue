<template>
  <div class="checkin-test">
    <h1>打卡API测试页面</h1>
    
    <el-card class="test-card">
      <template #header>
        <h3>API测试结果</h3>
      </template>
      
      <div class="test-section">
        <h4>1. 今日状态 API</h4>
        <el-button @click="testTodayStatus" type="primary">测试 /api/checkin/today-status</el-button>
        <pre v-if="todayStatusResult">{{ JSON.stringify(todayStatusResult, null, 2) }}</pre>
      </div>
      
      <div class="test-section">
        <h4>2. 用户统计 API</h4>
        <el-button @click="testUserStats" type="primary">测试 /api/checkin/user-statistics</el-button>
        <pre v-if="userStatsResult">{{ JSON.stringify(userStatsResult, null, 2) }}</pre>
      </div>
      
      <div class="test-section">
        <h4>3. 最近记录 API</h4>
        <el-button @click="testRecentRecords" type="primary">测试 /api/checkin/recent-records</el-button>
        <pre v-if="recentRecordsResult">{{ JSON.stringify(recentRecordsResult, null, 2) }}</pre>
      </div>
      
      <div class="test-section">
        <h4>4. 可用时段 API</h4>
        <el-button @click="testAvailableSessions" type="primary">测试 /api/checkin/available-sessions</el-button>
        <pre v-if="availableSessionsResult">{{ JSON.stringify(availableSessionsResult, null, 2) }}</pre>
      </div>
      
      <div class="test-section">
        <h4>5. 可用地点 API</h4>
        <el-button @click="testAvailableLocations" type="primary">测试 /api/checkin/available-locations</el-button>
        <pre v-if="availableLocationsResult">{{ JSON.stringify(availableLocationsResult, null, 2) }}</pre>
      </div>
      
      <div class="test-section">
        <h4>6. 地点管理 API</h4>
        <el-button @click="testLocationManagement" type="primary">测试 /api/checkin/locations</el-button>
        <pre v-if="locationManagementResult">{{ JSON.stringify(locationManagementResult, null, 2) }}</pre>
      </div>
    </el-card>

    <!-- 强制删除测试 -->
    <el-card class="test-card">
      <template #header>
        <h4>强制删除功能测试</h4>
      </template>
      <div class="test-section">
        <h5>步骤1: 标记地点为删除</h5>
        <div style="margin-bottom: 10px;">
          <el-input-number v-model="markDeletedId" :min="1" placeholder="地点ID" style="width: 120px; margin-right: 10px;" />
          <el-button @click="markLocationAsDeleted" :loading="loadingMarkDeleted">标记删除</el-button>
        </div>
        <pre v-if="markDeletedResult">{{ markDeletedResult }}</pre>
      </div>
      
      <div class="test-section">
        <h5>步骤2: 强制删除地点</h5>
        <div style="margin-bottom: 10px;">
          <el-input-number v-model="forceDeleteId" :min="1" placeholder="地点ID" style="width: 120px; margin-right: 10px;" />
          <el-button @click="forceDeleteLocation" :loading="loadingForceDelete" type="danger">强制删除</el-button>
        </div>
        <pre v-if="forceDeleteResult">{{ forceDeleteResult }}</pre>
      </div>
      
      <div class="test-section">
        <h5>步骤3: 验证删除结果</h5>
        <el-button @click="checkDeletionResult" :loading="loadingCheck">检查地点是否存在</el-button>
        <pre v-if="checkResult">{{ checkResult }}</pre>
      </div>
    </el-card>

    <!-- 配置管理测试 -->
    <el-card class="test-card">
      <template #header>
        <h4>配置管理功能测试</h4>
      </template>
      <div class="test-section">
        <h5>获取配置列表</h5>
        <el-button @click="testGetConfigurations" :loading="loadingConfigs">获取配置</el-button>
        <pre v-if="configResult">{{ configResult }}</pre>
      </div>
      
      <div class="test-section">
        <h5>创建配置</h5>
        <div style="margin-bottom: 10px;">
          <el-input v-model="newConfigName" placeholder="配置名称" style="width: 200px; margin-right: 10px;" />
          <el-button @click="testCreateConfiguration" :loading="loadingCreateConfig" type="primary">创建配置</el-button>
        </div>
        <pre v-if="createConfigResult">{{ createConfigResult }}</pre>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// 测试结果
const todayStatusResult = ref(null)
const userStatsResult = ref(null)
const recentRecordsResult = ref(null)
const availableSessionsResult = ref(null)
const availableLocationsResult = ref(null)
const locationManagementResult = ref(null)

// 强制删除测试相关变量
const markDeletedId = ref(1)
const loadingMarkDeleted = ref(false)
const markDeletedResult = ref(null)

const forceDeleteId = ref(1)
const loadingForceDelete = ref(false)
const forceDeleteResult = ref(null)

const loadingCheck = ref(false)
const checkResult = ref(null)

// 配置管理测试相关变量
const loadingConfigs = ref(false)
const configResult = ref(null)
const loadingCreateConfig = ref(false)
const createConfigResult = ref(null)
const newConfigName = ref('测试配置')

// 测试今日状态API
const testTodayStatus = async () => {
  try {
    const response = await request.get('/checkin/today-status')
    todayStatusResult.value = response
    ElMessage.success('今日状态API测试成功')
  } catch (error) {
    todayStatusResult.value = { error: error.message, response: error.response?.data }
    ElMessage.error('今日状态API测试失败')
  }
}

// 测试用户统计API
const testUserStats = async () => {
  try {
    const response = await request.get('/checkin/user-statistics')
    userStatsResult.value = response
    ElMessage.success('用户统计API测试成功')
  } catch (error) {
    userStatsResult.value = { error: error.message, response: error.response?.data }
    ElMessage.error('用户统计API测试失败')
  }
}

// 测试最近记录API
const testRecentRecords = async () => {
  try {
    const response = await request.get('/checkin/recent-records', {
      params: { size: 5 }
    })
    recentRecordsResult.value = response
    ElMessage.success('最近记录API测试成功')
  } catch (error) {
    recentRecordsResult.value = { error: error.message, response: error.response?.data }
    ElMessage.error('最近记录API测试失败')
  }
}

// 测试可用时段API
const testAvailableSessions = async () => {
  try {
    const response = await request.get('/checkin/available-configurations')
    availableSessionsResult.value = response
    ElMessage.success('可用时段API测试成功')
  } catch (error) {
    availableSessionsResult.value = { error: error.message, response: error.response?.data }
    ElMessage.error('可用时段API测试失败')
  }
}

// 测试可用地点API
const testAvailableLocations = async () => {
  try {
    const response = await request.get('/checkin/available-locations')
    availableLocationsResult.value = response
    ElMessage.success('可用地点API测试成功')
  } catch (error) {
    availableLocationsResult.value = { error: error.message, response: error.response?.data }
    ElMessage.error('可用地点API测试失败')
  }
}

// 测试地点管理API
const testLocationManagement = async () => {
  try {
    const response = await request.get('/checkin/locations', {
      params: { page: 0, size: 10 }
    })
    locationManagementResult.value = response
    ElMessage.success('地点管理API测试成功')
  } catch (error) {
    locationManagementResult.value = { error: error.message, response: error.response?.data }
    ElMessage.error('地点管理API测试失败')
  }
}

// 标记地点为删除
const markLocationAsDeleted = async () => {
  loadingMarkDeleted.value = true
  try {
    const response = await request.post(`/api/checkin/locations/debug/mark-deleted/${markDeletedId.value}`)
    markDeletedResult.value = response
    ElMessage.success('地点已标记为删除')
  } catch (error) {
    markDeletedResult.value = { error: error.message, response: error.response?.data }
    ElMessage.error('标记删除失败')
  } finally {
    loadingMarkDeleted.value = false
  }
}

// 强制删除地点
const forceDeleteLocation = async () => {
  loadingForceDelete.value = true
  try {
    const response = await request.delete(`/api/checkin/locations/force/${forceDeleteId.value}`)
    forceDeleteResult.value = response
    ElMessage.success('地点已强制删除')
  } catch (error) {
    forceDeleteResult.value = { error: error.message, response: error.response?.data }
    ElMessage.error('强制删除失败')
  } finally {
    loadingForceDelete.value = false
  }
}

// 检查删除结果
const checkDeletionResult = async () => {
  loadingCheck.value = true
  try {
    const response = await request.get('/checkin/locations/debug/all')
    checkResult.value = response
    ElMessage.success('检查完成')
  } catch (error) {
    checkResult.value = { error: error.message, response: error.response?.data }
    ElMessage.error('检查失败')
  } finally {
    loadingCheck.value = false
  }
}

// 测试获取配置列表
const testGetConfigurations = async () => {
  loadingConfigs.value = true
  try {
    const response = await request.get('/checkin/configurations')
    configResult.value = response
    ElMessage.success('获取配置列表成功')
  } catch (error) {
    configResult.value = { error: error.message, response: error.response?.data }
    ElMessage.error('获取配置列表失败')
  } finally {
    loadingConfigs.value = false
  }
}

// 测试创建配置
const testCreateConfiguration = async () => {
  loadingCreateConfig.value = true
  try {
    const response = await request.post('/api/checkin/configurations', {
      name: newConfigName.value,
      description: '这是一个测试配置',
      locationId: 1,
      sessionId: 1,
      isActive: true
    })
    createConfigResult.value = response
    ElMessage.success('创建配置成功')
  } catch (error) {
    createConfigResult.value = { error: error.message, response: error.response?.data }
    ElMessage.error('创建配置失败')
  } finally {
    loadingCreateConfig.value = false
  }
}
</script>

<style scoped>
.checkin-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-card {
  margin-bottom: 20px;
}

.test-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.test-section h4 {
  margin-bottom: 10px;
  color: #333;
}

pre {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 5px;
  overflow-x: auto;
  font-size: 12px;
  margin-top: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.el-button {
  margin-bottom: 10px;
}
</style>
