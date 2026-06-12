<template>
  <div class="checkin-configuration">
    <!-- 页面标题 -->
    <div class="page-header">
      <el-page-header @back="$router.back()">
        <template #content>
          <span class="page-title">打卡配置管理</span>
        </template>
      </el-page-header>
    </div>

    <!-- 操作栏 -->
    <el-card class="toolbar-card">
      <div class="toolbar-content">
        <div class="toolbar-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索配置名称..."
            :prefix-icon="Search"
            @input="handleSearch"
            clearable
            size="default"
            class="search-input"
          />
        </div>

        <div class="toolbar-actions">
          <el-button class="soft-action-btn" @click="handleRefresh" :icon="Refresh">
            <span v-if="!isMobile">刷新</span>
          </el-button>
          <el-button type="primary" class="soft-primary-btn" @click="handleAdd" :icon="Plus">
            <span>新增配置</span>
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 配置列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">
            <el-icon><Edit /></el-icon>
             配置列表 (共 {{ total }} 条)
          </span>
        </div>
      </template>
      
      <!-- 桌面端表格 -->
      <el-table
        v-if="!isMobile"
        v-loading="loading"
        :data="configurations"
        stripe
        @sort-change="handleSortChange"
        class="desktop-table"
      >
      <el-table-column prop="id" label="配置ID" width="80" align="center" sortable="custom" />
      
      <el-table-column prop="name" label="配置名称" min-width="150" sortable="custom">
        <template #default="{ row }">
          <div class="config-name-cell">
            <el-icon :class="['config-icon', { active: row.isActive }]">
              <Grid />
            </el-icon>
            <span class="config-name-text">{{ row.name }}</span>
            <el-badge 
              v-if="row.isActive" 
              value="已启用" 
              type="success"
              class="config-badge"
            />
            <el-badge 
              v-else 
              value="已停用" 
              type="info"
              class="config-badge"
            />
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="locationName" label="地点名称" min-width="120" />
      
      <el-table-column prop="locationAddress" label="地点地址" min-width="150" show-overflow-tooltip />
      
      <el-table-column prop="sessionName" label="时段名称" min-width="100" />
      
      <el-table-column label="时间范围" min-width="140">
        <template #default="{ row }">
          {{ row.startTime }} - {{ row.endTime }}
        </template>
      </el-table-column>

      <el-table-column label="打卡日期" min-width="170">
        <template #default="{ row }">
          <div class="weekday-tags">
            <span
              v-for="day in getWeekdayItems(row.requiredWeekdays)"
              :key="day.value"
              class="weekday-tag"
              :class="{ weekend: day.value >= 5 }"
            >
              {{ day.short }}
            </span>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="earlyCheckinMinutes" label="提前打卡(分钟)" width="120" align="center" />
      
      <el-table-column prop="lateCheckinMinutes" label="延迟打卡(分钟)" width="120" align="center" />
      
       <el-table-column prop="isActive" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.isActive"
            @change="handleToggleStatus(row)"
            :loading="row.toggleLoading"
          />
        </template>
      </el-table-column>
      
      <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      
      <el-table-column prop="createdAt" label="创建时间" width="160" sortable="custom">
        <template #default="{ row }">
          {{ formatDateTime(row.createdAt) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="178" fixed="right" align="center">
        <template #default="{ row }">
          <div class="operation-actions">
            <el-button type="primary" size="small" class="table-action-btn edit-action" @click="handleEdit(row)" :icon="Edit">
              编辑
            </el-button>
            <el-button type="danger" size="small" class="table-action-btn delete-action" @click="handleDelete(row)" :icon="Delete">
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 移动端卡片布局 -->
    <div v-if="isMobile" v-loading="loading" class="mobile-cards">
      <div v-for="config in configurations" :key="config.id" class="mobile-card">
        <div class="card-header">
          <div class="card-title">
            <el-icon :class="['mobile-config-icon', { active: config.isActive }]">
              <Grid />
            </el-icon>
            <span class="title-text">{{ config.name }}</span>
            <el-badge 
              v-if="config.isActive" 
              value="已启用" 
              type="success"
              class="mobile-config-badge"
            />
            <el-badge 
              v-else 
              value="已停用" 
              type="info"
              class="mobile-config-badge"
            />
          </div>
        </div>
        
        <div class="card-content">
          <div class="card-info">
            <div class="info-item">
              <span class="info-label">配置ID:</span>
              <span class="info-value">{{ config.id }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">地点:</span>
              <span class="info-value">{{ config.locationName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">时段:</span>
              <span class="info-value">{{ config.sessionName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">时间:</span>
              <span class="info-value">{{ config.startTime }} - {{ config.endTime }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">打卡日期:</span>
              <span class="info-value">{{ formatRequiredWeekdays(config.requiredWeekdays) }}</span>
            </div>
          </div>
          
          <div class="card-settings">
            <div class="setting-item">
              <span class="setting-label">提前打卡:</span>
              <span class="setting-value">{{ config.earlyCheckinMinutes }}分钟</span>
            </div>
            <div class="setting-item">
              <span class="setting-label">延迟打卡:</span>
              <span class="setting-value">{{ config.lateCheckinMinutes }}分钟</span>
            </div>
            <div class="setting-item">
              <span class="setting-label">排序:</span>
              <span class="setting-value">{{ config.sortOrder }}</span>
            </div>
          </div>
          
          <div class="card-status">
            <div class="status-item">
              <span class="status-label">状态控制:</span>
              <el-switch
                v-model="config.isActive"
                @change="handleToggleStatus(config)"
                :loading="config.toggleLoading"
                size="small"
              />
            </div>
          </div>
        </div>
        
        <div class="card-actions">
          <el-button type="primary" size="small" @click="handleEdit(config)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(config)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>
    </div>
    </el-card>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
        :small="isMobile"
        @current-change="loadConfigurations"
        @size-change="loadConfigurations"
      />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑配置' : '新增配置'"
      :width="isMobile ? '95%' : '920px'"
      :close-on-click-modal="false"
      class="config-dialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-width="isMobile ? '80px' : '120px'"
        @submit.prevent
        class="config-form"
      >
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="配置名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入配置名称" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="排序序号" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="配置描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入配置描述"
          />
        </el-form-item>

        <!-- 地点信息 -->
        <el-divider content-position="left">地点信息</el-divider>
        
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="地点名称" prop="locationName">
              <el-input v-model="form.locationName" placeholder="请输入地点名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="地点地址" prop="locationAddress">
              <el-input v-model="form.locationAddress" placeholder="请输入地点地址" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="地点描述" prop="locationDescription">
          <el-input
            v-model="form.locationDescription"
            type="textarea"
            :rows="isMobile ? 2 : 3"
            :autosize="{ minRows: 2, maxRows: 4 }"
            placeholder="请输入地点描述"
            clearable
            show-word-limit
            maxlength="200"
          />
        </el-form-item>
        
        <!-- 经纬度和位置操作已移除（GPS定位功能已取消） -->

        <!-- 时间信息 -->
        <el-divider content-position="left">时间信息</el-divider>
        
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="时段名称" prop="sessionName">
              <el-input v-model="form.sessionName" placeholder="请输入时段名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="时间范围" prop="timeRange">
              <el-time-picker
                v-model="timeRange"
                is-range
                format="HH:mm"
                value-format="HH:mm"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                style="width: 100%"
                @change="handleTimeRangeChange"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="时段描述" prop="sessionDescription">
          <el-input
            v-model="form.sessionDescription"
            type="textarea"
            :rows="isMobile ? 2 : 3"
            :autosize="{ minRows: 2, maxRows: 4 }"
            placeholder="请输入时段描述"
            clearable
            show-word-limit
            maxlength="200"
          />
        </el-form-item>

        <el-form-item label="打卡日期" prop="requiredWeekdays" class="weekday-form-item">
          <div class="weekday-select-panel">
            <div class="weekday-select-header">
              <div>
                <span class="weekday-select-title">晚自习打卡日期</span>
                <span class="weekday-select-tip">默认周一至周四，周五至周日不打卡</span>
              </div>
              <el-button link type="primary" class="weekday-reset-btn" @click="setDefaultWeekdays">
                恢复默认
              </el-button>
            </div>
            <el-checkbox-group v-model="form.requiredWeekdays" class="weekday-selector">
              <el-checkbox-button
                v-for="day in weekdayOptions"
                :key="day.value"
                :label="day.value"
                :class="{ weekend: day.value >= 5 }"
              >
                <span class="weekday-option-content">
                  <span class="weekday-short">{{ day.short }}</span>
                  <span class="weekday-label">{{ day.label }}</span>
                </span>
              </el-checkbox-button>
            </el-checkbox-group>
          </div>
        </el-form-item>

        <!-- 配置选项 -->
        <el-divider content-position="left">配置选项</el-divider>
        
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :xs="24" :sm="8">
            <el-form-item label="是否启用" prop="isActive">
              <el-switch v-model="form.isActive" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="提前打卡(分钟)" prop="earlyCheckinMinutes">
              <el-input-number 
                v-model="form.earlyCheckinMinutes" 
                :min="0" 
                :max="60" 
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8">
            <el-form-item label="延迟打卡(分钟)" prop="lateCheckinMinutes">
              <el-input-number 
                v-model="form.lateCheckinMinutes" 
                :min="0" 
                :max="120" 
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 签到方式配置 -->
        <el-divider content-position="left">签到方式配置</el-divider>
        
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="二维码内容" prop="qrCode">
              <el-input
                v-model="form.qrCode"
                type="textarea"
                :rows="isMobile ? 2 : 3"
                :autosize="{ minRows: 2, maxRows: 5 }"
                placeholder="通过「二维码生成工具」自动生成，或手动输入JSON格式内容"
                clearable
                show-word-limit
                maxlength="500"
                readonly
              />
              <div class="form-help">
                <el-text size="small" type="success">
                  <el-icon><CircleCheck /></el-icon>
                  推荐使用「晚自习签到」→「二维码生成」功能自动生成和更新
                </el-text>
              </div>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="WiFi名称" prop="wifiSsid">
              <el-input
                v-model="form.wifiSsid"
                placeholder="请输入WiFi名称，留空则不支持WiFi签到"
                clearable
              />
              <div class="form-help">
                <el-text size="small" type="info">
                  输入指定WiFi名称，用户需连接此WiFi才能签到
                </el-text>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <!-- 考勤人员 -->
        <el-divider content-position="left">考勤人员</el-divider>
        
        <el-form-item label="需要打卡的人员" prop="requiredUserIds">
          <div class="user-selector">
            <el-button 
              type="primary" 
              :icon="User" 
              @click="openUserSelector"
              class="select-users-btn"
              :size="isMobile ? 'default' : 'small'"
            >
              选择人员 ({{ selectedUsersCount }})
            </el-button>
            <div v-if="selectedUsers.length > 0" class="selected-users-preview">
              <el-tag
                v-for="user in selectedUsers.slice(0, isMobile ? 2 : 3)"
                :key="user.id"
                size="small"
                class="user-tag"
              >
                {{ user.realName }}
              </el-tag>
              <el-tag v-if="selectedUsers.length > (isMobile ? 2 : 3)" size="small" type="info">
                +{{ selectedUsers.length - (isMobile ? 2 : 3) }}...
              </el-tag>
            </div>
          </div>
          <div class="form-tip">
            选择需要参与此打卡配置的人员，只有被选择的人员才需要打卡
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            {{ isEditing ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 地图选择器已移除（GPS定位功能已取消） -->

    <!-- 用户选择弹窗 -->
    <el-dialog
      v-model="userSelectorVisible"
      title="选择需要打卡的人员"
      :width="isMobile ? '95%' : '860px'"
      :close-on-click-modal="false"
      class="user-pick-dialog"
    >
      <div class="user-selector-dialog">
        <!-- 搜索框 -->
        <div class="search-header">
          <el-input
            v-model="userSearchKeyword"
            placeholder="搜索用户姓名、用户名或部门"
            :prefix-icon="Search"
            clearable
            class="search-input"
          />
          <div class="selection-info">
            已选择 <strong>{{ tempSelectedUserIds.length }}</strong> 人
          </div>
        </div>

        <!-- 用户列表 -->
        <div class="user-list-container">
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          <div v-else-if="filteredUserList.length === 0" class="empty-container">
            <el-empty description="暂无用户数据" />
          </div>
          <div v-else class="user-grid">
            <div
              v-for="user in filteredUserList"
              :key="user.id"
              class="user-card"
              :class="{ selected: tempSelectedUserIds.includes(user.id) }"
              @click="toggleUserSelection(user.id)"
            >
              <div class="user-avatar">
                <el-avatar :size="40" :src="user.avatarUrl">
                  {{ user.realName?.charAt(0) || 'U' }}
                </el-avatar>
              </div>
              <div class="user-info">
                <div class="user-name">{{ user.realName }}</div>
                <div class="user-details">{{ user.username }}</div>
                <div v-if="user.departmentName" class="user-department">
                  {{ user.departmentName }}
                </div>
              </div>
              <div class="selection-indicator">
                <el-icon v-if="tempSelectedUserIds.includes(user.id)" class="selected-icon">
                  <Check />
                </el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- 已选择用户预览 -->
        <div v-if="tempSelectedUsers.length > 0" class="selected-preview">
          <div class="preview-title">已选择的人员：</div>
          <div class="selected-tags">
            <el-tag
              v-for="user in tempSelectedUsers"
              :key="user.id"
              closable
              @close="removeUserSelection(user.id)"
              class="selected-tag"
            >
              {{ user.realName }}
              <span v-if="user.departmentName" class="tag-department">
                ({{ user.departmentName }})
              </span>
            </el-tag>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelUserSelection">取消</el-button>
          <el-button @click="clearAllSelection">清空选择</el-button>
          <el-button type="primary" @click="confirmUserSelection">
            确认选择 ({{ tempSelectedUserIds.length }})
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Minus, Refresh, Search, Edit, Delete, User, Check, Grid, CircleCheck } from '@element-plus/icons-vue'
// 地理位置相关导入已移除（GPS定位功能已取消）

// 响应式屏幕尺寸检测
const windowWidth = ref(window.innerWidth)

// 计算属性：判断是否为移动设备
const isMobile = computed(() => windowWidth.value <= 768)

const defaultRequiredWeekdays = [1, 2, 3, 4]
const weekdayOptions = [
  { value: 1, label: '星期一', short: '周一' },
  { value: 2, label: '星期二', short: '周二' },
  { value: 3, label: '星期三', short: '周三' },
  { value: 4, label: '星期四', short: '周四' },
  { value: 5, label: '星期五', short: '周五' },
  { value: 6, label: '星期六', short: '周六' },
  { value: 7, label: '星期日', short: '周日' }
]

const normalizeRequiredWeekdays = (weekdays) => {
  if (!Array.isArray(weekdays) || weekdays.length === 0) {
    return [...defaultRequiredWeekdays]
  }
  const normalized = [...new Set(weekdays.map(Number))]
    .filter(day => Number.isInteger(day) && day >= 1 && day <= 7)
    .sort((a, b) => a - b)
  return normalized.length > 0 ? normalized : [...defaultRequiredWeekdays]
}

const getWeekdayItems = (weekdays) => {
  const normalized = normalizeRequiredWeekdays(weekdays)
  return weekdayOptions.filter(day => normalized.includes(day.value))
}

const formatRequiredWeekdays = (weekdays) => {
  return getWeekdayItems(weekdays).map(day => day.short).join('、')
}

// 监听窗口大小变化
const handleResize = () => {
  windowWidth.value = window.innerWidth
}
import request from '@/utils/request'
// AmapPicker组件导入已移除（GPS定位功能已取消）

// 响应式数据
const loading = ref(false)
const configurations = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEditing = ref(false)
const submitLoading = ref(false)
const formRef = ref()

// 地理位置相关数据已移除（GPS定位功能已取消）

// 表单数据
const form = reactive({
  id: null,
  name: '',
  description: '',
  locationName: '',
  locationAddress: '',
  locationDescription: '',
  // longitude 和 latitude 字段已移除（GPS定位功能已取消）
  sessionName: '',
  startTime: '',
  endTime: '',
  timeRange: [], // 添加timeRange字段用于验证
  sessionDescription: '',
  requiredWeekdays: [...defaultRequiredWeekdays],
  isActive: true,
  sortOrder: 0,
  earlyCheckinMinutes: 0,
  lateCheckinMinutes: 0,
  qrCode: '',
  wifiSsid: '',
  requiredUserIds: [] // 需要打卡的用户ID列表
})

// 时间范围
const timeRange = ref([])

// 用户列表
const userList = ref([])

// 用户选择相关
const userSelectorVisible = ref(false)
const userSearchKeyword = ref('')
const tempSelectedUserIds = ref([])
const selectedUsers = ref([])

// 高德地图相关变量已移除（GPS定位功能已取消）

// 计算属性
const selectedUsersCount = computed(() => selectedUsers.value.length)

const setDefaultWeekdays = () => {
  form.requiredWeekdays = [...defaultRequiredWeekdays]
}

const tempSelectedUsers = computed(() => {
  return userList.value.filter(user => tempSelectedUserIds.value.includes(user.id))
})

const filteredUserList = computed(() => {
  if (!userSearchKeyword.value) return userList.value
  const keyword = userSearchKeyword.value.toLowerCase()
  return userList.value.filter(user => 
    user.realName?.toLowerCase().includes(keyword) ||
    user.username?.toLowerCase().includes(keyword) ||
    user.departmentName?.toLowerCase().includes(keyword)
  )
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入配置名称', trigger: 'blur' },
    { min: 2, max: 100, message: '配置名称长度在2-100字符之间', trigger: 'blur' }
  ],
  locationName: [
    { required: true, message: '请输入地点名称', trigger: 'blur' },
    { min: 2, max: 100, message: '地点名称长度在2-100字符之间', trigger: 'blur' }
  ],
  sessionName: [
    { required: true, message: '请输入时段名称', trigger: 'blur' },
    { min: 2, max: 100, message: '时段名称长度在2-100字符之间', trigger: 'blur' }
  ],
  timeRange: [
    { required: true, message: '请选择时间范围', trigger: 'change' }
  ],
  requiredWeekdays: [
    { type: 'array', required: true, min: 1, message: '请选择需要打卡的日期', trigger: 'change' }
  ]
}

// 生命周期
onMounted(() => {
  loadConfigurations()
  loadUserList()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 地图实例清理由组件内部处理
  window.removeEventListener('resize', handleResize)
})

// 方法
const loadConfigurations = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    let apiUrl = '/checkin/configurations'
    
    // 如果有搜索关键词，使用搜索端点
    if (searchKeyword.value && searchKeyword.value.trim()) {
      apiUrl = '/checkin/configurations/search'
      params.keyword = searchKeyword.value.trim()
    }
    
    console.log('正在请求:', apiUrl, '参数:', params)
    const response = await request.get(apiUrl, { params })
    console.log('API响应:', response)
    
    if (response && response.success !== false) {
      const data = response.data || response
      if (data && data.content) {
        configurations.value = data.content.map(item => ({
          ...item,
          requiredWeekdays: normalizeRequiredWeekdays(item.requiredWeekdays),
          toggleLoading: false
        }))
        total.value = data.totalElements
        console.log('配置数据加载成功，共', total.value, '条记录')
      } else {
        console.error('响应数据格式错误:', response)
        ElMessage.error('响应数据格式错误')
      }
    } else {
      console.error('API响应失败:', response)
      ElMessage.error(response?.message || '获取配置列表失败')
    }
  } catch (error) {
    console.error('获取配置列表失败:', error)
    ElMessage.error('获取配置列表失败')
  } finally {
    loading.value = false
  }
}

// 加载用户列表
const loadUserList = async () => {
  try {
    console.log('开始加载用户列表...')
    const response = await request.get('/users/simple')
    console.log('用户列表响应:', response)
    
    if (response && response.success !== false) {
      const data = response.data || response || []
      userList.value = Array.isArray(data) ? data : []
      console.log('用户列表加载成功，共', userList.value.length, '个用户')
    } else {
      console.error('用户列表API响应失败:', response)
      ElMessage.error(response?.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

// 用户选择相关方法
const openUserSelector = () => {
  // 初始化临时选择状态
  tempSelectedUserIds.value = [...form.requiredUserIds]
  userSearchKeyword.value = ''
  userSelectorVisible.value = true
}

const toggleUserSelection = (userId) => {
  const index = tempSelectedUserIds.value.indexOf(userId)
  if (index > -1) {
    tempSelectedUserIds.value.splice(index, 1)
  } else {
    tempSelectedUserIds.value.push(userId)
  }
}

const removeUserSelection = (userId) => {
  const index = tempSelectedUserIds.value.indexOf(userId)
  if (index > -1) {
    tempSelectedUserIds.value.splice(index, 1)
  }
}

const clearAllSelection = () => {
  tempSelectedUserIds.value = []
}

const cancelUserSelection = () => {
  userSelectorVisible.value = false
  tempSelectedUserIds.value = []
}

const confirmUserSelection = () => {
  // 更新表单数据
  form.requiredUserIds = [...tempSelectedUserIds.value]
  
  // 更新已选择用户列表（用于显示）
  selectedUsers.value = userList.value.filter(user => 
    form.requiredUserIds.includes(user.id)
  )
  
  userSelectorVisible.value = false
  ElMessage.success(`已选择 ${form.requiredUserIds.length} 位人员`)
}

const handleSearch = () => {
  currentPage.value = 1
  loadConfigurations()
}

const handleRefresh = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  loadConfigurations()
}

const handleAdd = () => {
  resetForm()
  isEditing.value = false
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, row)
  form.requiredWeekdays = normalizeRequiredWeekdays(row.requiredWeekdays)
  timeRange.value = [row.startTime, row.endTime]
  form.timeRange = [row.startTime, row.endTime] // 同步设置form.timeRange字段
  
  // 设置需要打卡的用户ID列表
  if (row.requiredUsers && Array.isArray(row.requiredUsers)) {
    form.requiredUserIds = row.requiredUsers.map(user => user.id)
    selectedUsers.value = [...row.requiredUsers] // 设置已选择用户列表
  }
  
  isEditing.value = true
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认永久删除配置"${row.name}"吗？
      
⚠️ 注意：
• 此操作将从数据库中完全删除该配置
• 如果存在关联的打卡记录，删除将失败
• 删除后无法恢复，请谨慎操作
• 建议先禁用配置而非删除`,
      '确认永久删除',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true
      }
    )
    
    const response = await request.delete(`/checkin/configurations/${row.id}`)
    
    if (response.success !== false) {
      ElMessage.success('配置删除成功')
      loadConfigurations()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除配置失败:', error)
      
      // 处理响应错误
      if (error.response && error.response.data) {
        ElMessage.error(error.response.data.message || '删除失败')
      } else {
        ElMessage.error('删除失败，请检查网络连接')
      }
    }
  }
}

const handleToggleStatus = async (row) => {
  try {
    row.toggleLoading = true
    const response = await request.put(`/checkin/configurations/${row.id}/toggle`)
    
    // 修复：正确检查响应格式，后端返回的是 ApiResponse 结构
    if (response && response.success !== false) {
      ElMessage.success(`配置状态已${row.isActive ? '启用' : '禁用'}`)
      loadConfigurations()
    } else {
      // 恢复原状态
      row.isActive = !row.isActive
      ElMessage.error(response?.message || '状态切换失败')
    }
  } catch (error) {
    // 恢复原状态
    row.isActive = !row.isActive
    console.error('切换状态失败:', error)
    
    // 处理错误响应
    if (error.response && error.response.data) {
      ElMessage.error(error.response.data.message || '状态切换失败')
    } else {
      ElMessage.error('状态切换失败，请检查网络连接')
    }
  } finally {
    row.toggleLoading = false
  }
}

const handleTimeRangeChange = (value) => {
  if (value && value.length === 2) {
    form.startTime = value[0]
    form.endTime = value[1]
    form.timeRange = value // 同步更新timeRange字段用于验证
  } else {
    form.startTime = ''
    form.endTime = ''
    form.timeRange = [] // 清空timeRange字段
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    
    const data = { ...form }
    delete data.id
    delete data.timeRange // 移除timeRange字段，后端不需要此字段
    data.requiredWeekdays = normalizeRequiredWeekdays(data.requiredWeekdays)
    
    let response
    if (isEditing.value) {
      response = await request.put(`/checkin/configurations/${form.id}`, data)
    } else {
      response = await request.post('/checkin/configurations', data)
    }
    
    if (response && response.success !== false) {
      ElMessage.success(`配置${isEditing.value ? '更新' : '创建'}成功`)
      dialogVisible.value = false
      loadConfigurations()
    } else {
      ElMessage.error(response?.message || `配置${isEditing.value ? '更新' : '创建'}失败`)
    }
  } catch (error) {
    console.error(`配置${isEditing.value ? '更新' : '创建'}失败:`, error)
    
    // 处理响应错误
    if (error.response && error.response.data) {
      ElMessage.error(error.response.data.message || `配置${isEditing.value ? '更新' : '创建'}失败`)
    } else {
      ElMessage.error(`配置${isEditing.value ? '更新' : '创建'}失败，请检查网络连接`)
    }
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    locationName: '',
    locationAddress: '',
    locationDescription: '',
    // longitude 和 latitude 字段已移除（GPS定位功能已取消）
    sessionName: '',
    startTime: '',
    endTime: '',
    timeRange: [], // 重置timeRange字段
    sessionDescription: '',
    requiredWeekdays: [...defaultRequiredWeekdays],
    isActive: true,
    sortOrder: 0,
    earlyCheckinMinutes: 0,
    lateCheckinMinutes: 0,
    qrCode: '',
    wifiSsid: '',
    requiredUserIds: [] // 重置用户ID列表
  })
  timeRange.value = []
  selectedUsers.value = [] // 重置已选择用户列表
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleSortChange = ({ prop, order }) => {
  // 实现排序逻辑
  console.log('排序变化:', prop, order)
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// ========== 地理位置相关方法已全部移除（GPS定位功能已取消）==========
// 所有地理位置和地图相关方法已删除
// ==================================================================
</script>

<style scoped>
.checkin-configuration {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 500;
}

/* 工具栏样式 */
.toolbar-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.toolbar-row {
  align-items: flex-end;
  margin-bottom: 16px;
}

.basic-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  align-items: center;
}

.basic-actions .el-button {
  min-width: 80px;
  height: 32px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.action-buttons-row {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
}

.action-buttons .el-button {
  min-width: 120px;
  height: 36px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

/* 表格卡片样式 */
.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 配置名称单元格样式 */
.config-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
  transition: all 0.3s ease;
}

.config-name-cell:hover .config-icon {
  transform: scale(1.1);
}

.config-icon {
  font-size: 18px;
  color: #909399;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.config-icon.active {
  color: #67c23a;
}

.config-name-text {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.3s ease;
}

.config-name-cell:hover .config-name-text {
  color: #409eff;
}

.config-badge {
  flex-shrink: 0;
}

.table-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.table-title .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 移动端卡片样式 */
.mobile-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.mobile-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.card-header {
  padding: 16px;
  border-bottom: 1px solid #f5f7fa;
  background: #fafbfc;
}

.mobile-config-icon {
  font-size: 20px;
  color: #909399;
  flex-shrink: 0;
  margin-right: 8px;
  transition: color 0.3s ease;
}

.mobile-config-icon.active {
  color: #67c23a;
}

.mobile-config-badge {
  margin-left: auto;
  flex-shrink: 0;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  flex: 1;
}

.card-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.card-content {
  padding: 16px;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  color: #303133;
  text-align: right;
  flex: 1;
  margin-left: 8px;
}

.card-settings {
  border-top: 1px solid #f5f7fa;
  padding-top: 12px;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.setting-label {
  font-size: 13px;
  color: #909399;
}

.setting-value {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.card-status {
  border-top: 1px solid #f5f7fa;
  padding-top: 12px;
  margin-bottom: 16px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.card-actions {
  padding: 12px 16px;
  border-top: 1px solid #f5f7fa;
  background: #fafbfc;
  display: flex;
  gap: 8px;
  justify-content: space-between;
  align-items: center;
}

.card-actions .el-button {
  flex: 1;
  height: 36px;
}

.form-help {
  margin-top: 4px;
}

.form-help .el-text {
  line-height: 1.2;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-table) {
  border-radius: 4px;
}

:deep(.el-table__header) {
  background-color: #fafafa;
}

:deep(.el-divider__text) {
  font-weight: 500;
  color: #409eff;
}

/* 表单优化样式 */
.config-form .el-form-item {
  margin-bottom: 20px;
}

.config-form .el-form-item__label {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.config-form .el-input,
.config-form .el-textarea,
.config-form .el-input-number {
  font-size: 14px;
}

.config-form .el-input__inner,
.config-form .el-textarea__inner {
  border-radius: 6px;
  transition: all 0.3s ease;
}

.config-form .el-input__inner:focus,
.config-form .el-textarea__inner:focus {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 地理位置相关样式已移除（GPS定位功能已取消） */

.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
  margin-top: 4px;
}

/* 用户选择器样式 */
.user-selector {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.select-users-btn {
  align-self: flex-start;
}

.selected-users-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.user-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

/* 用户选择弹窗样式 */
.user-selector-dialog {
  height: 500px;
  display: flex;
  flex-direction: column;
}

.search-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.search-input {
  flex: 1;
  max-width: 300px;
}

.selection-info {
  color: #606266;
  font-size: 14px;
  white-space: nowrap;
}

.user-list-container {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 16px;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.user-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
  padding: 4px;
}

.user-card {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.user-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.user-card.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.user-avatar {
  margin-right: 12px;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-details {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.user-department {
  font-size: 12px;
  color: #606266;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selection-indicator {
  margin-left: 8px;
  flex-shrink: 0;
}

.selected-icon {
  color: #409eff;
  font-size: 18px;
}

.selected-preview {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.preview-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-tag {
  margin-right: 0;
  margin-bottom: 0;
}

.tag-department {
  color: #909399;
  font-size: 11px;
  margin-left: 4px;
}

/* 清新玻璃化布局覆盖 */
.checkin-configuration {
  max-width: 1520px;
  padding: 24px;
  color: #0f3f5c;
}

.page-header {
  padding: 18px 20px;
  border: 1px solid rgba(14, 165, 233, 0.16);
  border-radius: 20px;
  background:
    linear-gradient(135deg, rgba(236, 254, 255, 0.92), rgba(255, 255, 255, 0.86)),
    radial-gradient(circle at top right, rgba(125, 211, 252, 0.24), transparent 36%);
  box-shadow: 0 18px 44px rgba(14, 116, 144, 0.08);
  backdrop-filter: blur(18px);
}

.page-title {
  color: #0c4a6e;
  font-size: 22px;
  font-weight: 700;
}

.toolbar-card,
.table-card {
  border: 1px solid rgba(14, 165, 233, 0.14);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 18px 42px rgba(14, 116, 144, 0.08);
  backdrop-filter: blur(18px);
}

.toolbar-card {
  margin-bottom: 18px;
}

.toolbar-card :deep(.el-card__body) {
  padding: 16px;
}

.toolbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.toolbar-search {
  flex: 1;
  min-width: 240px;
  max-width: 460px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.search-input {
  max-width: none;
}

:deep(.el-button) {
  border-radius: 999px;
  font-weight: 650;
  cursor: pointer;
}

.soft-action-btn {
  border-color: rgba(14, 165, 233, 0.22);
  background: linear-gradient(135deg, rgba(240, 249, 255, 0.96), rgba(255, 255, 255, 0.96));
  color: #0369a1;
}

.soft-primary-btn,
.select-users-btn {
  border: none;
  background: linear-gradient(135deg, #67e8f9, #7dd3fc);
  color: #075985;
  box-shadow: 0 10px 24px rgba(14, 165, 233, 0.22);
}

.soft-action-btn:hover,
.soft-primary-btn:hover,
.select-users-btn:hover {
  border-color: rgba(14, 165, 233, 0.38);
  color: #075985;
  filter: brightness(1.02);
  transform: translateY(-1px);
}

.table-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(14, 165, 233, 0.12);
  background: linear-gradient(135deg, rgba(236, 254, 255, 0.78), rgba(255, 255, 255, 0.72));
}

.table-title {
  color: #0c4a6e;
  font-size: 17px;
}

.table-title .el-icon {
  color: #0891b2;
}

.desktop-table {
  border-radius: 16px;
  overflow: hidden;
}

.desktop-table :deep(.el-table__inner-wrapper::before) {
  display: none;
}

.desktop-table :deep(th.el-table__cell) {
  background: #f0fdfa;
  color: #0f4f62;
  font-weight: 700;
}

.desktop-table :deep(.el-table__row) {
  --el-table-row-hover-bg-color: #ecfeff;
}

.config-icon.active,
.mobile-config-icon.active {
  color: #0891b2;
}

.config-name-text {
  color: #153f56;
  font-weight: 650;
}

.config-name-cell:hover .config-name-text {
  color: #0284c7;
}

.weekday-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.weekday-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 26px;
  padding: 0 9px;
  border: 1px solid rgba(14, 165, 233, 0.18);
  border-radius: 999px;
  background: linear-gradient(135deg, #e0f2fe, #ecfeff);
  color: #0369a1;
  font-size: 12px;
  font-weight: 700;
}

.weekday-tag.weekend {
  border-color: rgba(148, 163, 184, 0.22);
  background: #f8fafc;
  color: #64748b;
}

.operation-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  width: 100%;
}

.table-action-btn {
  width: 100%;
  margin: 0;
  border: none;
  color: #0f4f62;
}

.table-action-btn + .table-action-btn {
  margin-left: 0;
}

.edit-action {
  background: linear-gradient(135deg, #bae6fd, #cffafe);
  color: #0369a1;
}

.delete-action {
  background: linear-gradient(135deg, #fee2e2, #fff1f2);
  color: #be123c;
}

.mobile-card {
  border: 1px solid rgba(14, 165, 233, 0.14);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 14px 32px rgba(14, 116, 144, 0.08);
}

.card-header,
.card-actions {
  background: linear-gradient(135deg, rgba(240, 253, 250, 0.88), rgba(240, 249, 255, 0.88));
  border-color: rgba(14, 165, 233, 0.1);
}

.card-actions .el-button {
  border: none;
}

:global(.config-dialog .el-dialog),
:global(.el-dialog) {
  border-radius: 22px;
  background:
    linear-gradient(135deg, rgba(248, 253, 255, 0.98), rgba(255, 255, 255, 0.96)),
    radial-gradient(circle at top right, rgba(125, 211, 252, 0.18), transparent 36%);
  box-shadow: 0 28px 72px rgba(8, 47, 73, 0.18);
  overflow: hidden;
}

:global(.config-dialog .el-dialog__header),
:global(.el-dialog .el-dialog__header) {
  margin: 0;
  padding: 22px 26px 16px;
  border-bottom: 1px solid rgba(14, 165, 233, 0.12);
}

:global(.config-dialog .el-dialog__title),
:global(.el-dialog .el-dialog__title) {
  color: #0c4a6e;
  font-weight: 750;
}

:global(.config-dialog .el-dialog__body) {
  max-height: min(70vh, 720px);
  padding: 22px 26px 10px;
  overflow-y: auto;
}

:global(.config-dialog .el-dialog__footer),
:global(.el-dialog .el-dialog__footer) {
  padding: 16px 26px 22px;
  border-top: 1px solid rgba(14, 165, 233, 0.1);
  background: rgba(240, 249, 255, 0.5);
}

.config-form :deep(.el-form-item__label) {
  color: #0f4f62;
  font-weight: 700;
}

.config-form :deep(.el-input__wrapper),
.config-form :deep(.el-textarea__inner) {
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 0 0 1px rgba(14, 165, 233, 0.14) inset;
}

.config-form :deep(.el-input__wrapper:hover),
.config-form :deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px rgba(14, 165, 233, 0.28) inset;
}

.config-form :deep(.el-input__wrapper.is-focus),
.config-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #38bdf8 inset, 0 0 0 4px rgba(56, 189, 248, 0.14);
}

:deep(.el-divider) {
  margin: 24px 0 20px;
  border-color: rgba(14, 165, 233, 0.12);
}

:deep(.el-divider__text) {
  padding: 5px 12px;
  border: 1px solid rgba(14, 165, 233, 0.14);
  border-radius: 999px;
  background: #ecfeff;
  color: #0369a1;
  font-size: 13px;
  font-weight: 750;
}

.weekday-select-panel {
  width: 100%;
  padding: 14px;
  border: 1px solid rgba(14, 165, 233, 0.14);
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(236, 254, 255, 0.88), rgba(255, 255, 255, 0.9));
}

.weekday-select-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.weekday-select-title,
.weekday-select-tip {
  display: block;
}

.weekday-select-title {
  color: #0c4a6e;
  font-size: 14px;
  font-weight: 750;
}

.weekday-select-tip {
  margin-top: 3px;
  color: #4b7186;
  font-size: 12px;
  line-height: 1.45;
}

.weekday-reset-btn {
  flex-shrink: 0;
  color: #0284c7;
}

.weekday-selector {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 10px;
  width: 100%;
}

.weekday-selector :deep(.el-checkbox-button) {
  width: 100%;
}

.weekday-selector :deep(.el-checkbox-button__inner) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 72px;
  padding: 10px 6px;
  border: 1px solid rgba(14, 165, 233, 0.16) !important;
  border-radius: 16px !important;
  background: rgba(255, 255, 255, 0.86);
  color: #0f4f62;
  box-shadow: none;
  transition: all 0.2s ease;
}

.weekday-selector :deep(.el-checkbox-button__inner:hover) {
  border-color: rgba(14, 165, 233, 0.38) !important;
  color: #0369a1;
  transform: translateY(-1px);
}

.weekday-selector :deep(.el-checkbox-button.is-checked .el-checkbox-button__inner) {
  border-color: rgba(14, 165, 233, 0.52) !important;
  background: linear-gradient(135deg, #a7f3d0, #bae6fd);
  color: #075985;
  box-shadow: 0 10px 22px rgba(14, 165, 233, 0.18);
}

.weekday-selector :deep(.el-checkbox-button.weekend .el-checkbox-button__inner) {
  background: rgba(248, 250, 252, 0.86);
  color: #64748b;
}

.weekday-option-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  line-height: 1.1;
}

.weekday-short {
  font-size: 15px;
  font-weight: 800;
}

.weekday-label {
  font-size: 11px;
  font-weight: 600;
  opacity: 0.72;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.dialog-footer .el-button {
  min-width: 92px;
  margin-left: 0;
}

.dialog-footer .el-button--primary {
  border: none;
  background: linear-gradient(135deg, #67e8f9, #7dd3fc);
  color: #075985;
  box-shadow: 0 10px 24px rgba(14, 165, 233, 0.18);
}

.user-selector-dialog {
  height: min(62vh, 560px);
}

.user-card {
  border-color: rgba(14, 165, 233, 0.14);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
}

.user-card:hover,
.user-card.selected {
  border-color: rgba(14, 165, 233, 0.42);
  background: #ecfeff;
  box-shadow: 0 12px 24px rgba(14, 116, 144, 0.08);
}

@media (max-width: 768px) {
  .checkin-configuration {
    padding: 14px;
  }

  .toolbar-content {
    align-items: stretch;
    flex-direction: column;
  }

  .toolbar-search {
    max-width: none;
  }

  .toolbar-actions {
    justify-content: stretch;
  }

  .toolbar-actions .el-button {
    flex: 1;
  }

  .weekday-selector {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .weekday-selector :deep(.el-checkbox-button__inner) {
    min-height: 62px;
  }

  .weekday-select-header {
    flex-direction: column;
  }

  :global(.config-dialog .el-dialog__body) {
    max-height: 68vh;
    padding: 18px 14px 8px;
  }

  .dialog-footer {
    flex-wrap: wrap;
  }

  .dialog-footer .el-button {
    flex: 1;
    min-width: 110px;
  }
}
</style>
