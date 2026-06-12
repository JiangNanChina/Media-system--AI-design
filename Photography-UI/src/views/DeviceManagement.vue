<template>
  <div class="device-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><Monitor /></el-icon>
            设备管理
          </h1>
          <p class="page-subtitle">管理用户设备绑定，防止作弊行为</p>
        </div>
      </div>
      
      <!-- 装饰元素 -->
      <div class="header-decoration">
        <div class="decoration-circle decoration-circle-1"></div>
        <div class="decoration-circle decoration-circle-2"></div>
        <div class="decoration-circle decoration-circle-3"></div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :md="6" v-for="(stat, index) in statistics" :key="index">
          <div class="modern-stats-card">
            <div class="stats-icon" :class="stat.iconClass">
              <el-icon><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stats-content">
              <div class="stats-value">{{ stat.value }}</div>
              <div class="stats-label">{{ stat.label }}</div>
            </div>
            <div class="stats-trend" v-if="stat.trend">
              <el-icon><TrendCharts /></el-icon>
              <span>{{ stat.trend }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 管理员说明 -->
    <div class="admin-notice" v-if="isAdmin">
      <el-alert
        title="管理员权限说明"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #default>
          <p><strong>采用严格的设备指纹验证机制，有效防止多设备作弊行为。</strong></p>
          <ul style="margin: 8px 0 0 20px; padding: 0;">
            <li><strong>首次绑定</strong>：用户首次登录时自动绑定设备</li>
            <li><strong>指纹验证</strong>：后续登录必须使用相同设备，设备指纹不匹配将被拒绝</li>
            <li><strong>更换设备</strong>：用户无法自行更换设备，必须联系管理员</li>
            <li><strong>管理员权限</strong>：可删除设备记录，允许用户重新绑定新设备</li>
            <li><strong>防作弊保护</strong>：杜绝一号多设备的作弊行为</li>
          </ul>
        </template>
      </el-alert>
    </div>

    <!-- 搜索和操作区域 -->
    <el-card class="search-card">
      <el-row :gutter="isMobile ? 12 : 16" class="search-row">
        <!-- 搜索框 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="10" :xl="8">
          <div class="search-item">
            <label class="search-label" v-if="!isMobile">搜索设备</label>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名、设备名称..."
              :prefix-icon="Search"
              @input="handleSearch"
              clearable
              class="search-input"
            />
          </div>
        </el-col>
        
        <!-- 操作按钮 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="14" :xl="16">
          <div class="search-item search-actions-wrapper">
            <label class="search-label invisible" v-if="!isMobile">操作</label>
            <div class="search-actions">
              <el-row :gutter="8" class="action-buttons-row">
                <el-col :span="isMobile ? 8 : 6">
                  <el-button 
                    type="primary" 
                    @click="refreshData" 
                    :icon="Refresh" 
                    :loading="loading"
                    :size="isMobile ? 'default' : 'default'"
                    class="action-btn"
                  >
                    <span v-if="!isMobile">刷新数据</span>
                  </el-button>
                </el-col>
                <el-col :span="isMobile ? 8 : 9" v-if="isAdmin">
                  <el-button 
                    type="warning" 
                    @click="cleanupDevices" 
                    :icon="Delete"
                    :size="isMobile ? 'default' : 'default'"
                    class="action-btn"
                  >
                    <span v-if="!isMobile">清理未活跃设备</span>
                    <span v-else>清理设备</span>
                  </el-button>
                </el-col>
                <el-col :span="isMobile ? 8 : 9" v-if="isAdmin">
                  <el-button 
                    type="danger" 
                    @click="showBatchDeleteDialog" 
                    :icon="Delete"
                    :size="isMobile ? 'default' : 'default'"
                    class="action-btn"
                  >
                    <span v-if="!isMobile">批量删除设备</span>
                    <span v-else>批量删除</span>
                  </el-button>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 设备列表 -->
    <div class="device-list">
      <!-- 桌面端表格 -->
      <el-table
        v-if="!isMobile"
        v-loading="loading"
        :data="devices"
        border
        stripe
        @sort-change="handleSortChange"
      >
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="device-details">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="设备指纹">
                  <el-tag size="small" type="info">{{ row.deviceFingerprint }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="IP地址">{{ row.ipAddress }}</el-descriptions-item>
                <el-descriptions-item label="屏幕分辨率">{{ row.screenResolution }}</el-descriptions-item>
                <el-descriptions-item label="时区">{{ row.timezone }}</el-descriptions-item>
                <el-descriptions-item label="语言">{{ row.language }}</el-descriptions-item>
                <el-descriptions-item label="首次绑定">{{ formatDateTime(row.firstBoundAt) }}</el-descriptions-item>
                <el-descriptions-item label="最后活跃">{{ formatDateTime(row.lastActiveAt) }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ formatDateTime(row.createdAt) }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="id" label="设备ID" width="80" align="center" />
        
        <el-table-column prop="user.username" label="用户" min-width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="32" :src="row.user?.avatarUrl">
                {{ row.user?.realName?.charAt(0) }}
              </el-avatar>
              <div class="user-details">
                <div class="username">{{ row.user?.username }}</div>
                <div class="realname">{{ row.user?.realName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="deviceName" label="设备名称" min-width="150" show-overflow-tooltip />
        
        <el-table-column prop="deviceType" label="设备类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getDeviceTypeColor(row.deviceType)" size="small">
              {{ row.deviceTypeDescription }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="osInfo" label="操作系统" min-width="120" show-overflow-tooltip />
        
        <el-table-column prop="browserInfo" label="浏览器" min-width="120" show-overflow-tooltip />
        
        <el-table-column prop="isActive" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">
              {{ row.isActive ? '激活' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="bindStatus" label="绑定状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getBindStatusColor(row.bindStatus)" size="small">
              {{ row.bindStatusDescription }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="lastActiveAt" label="最后活跃" width="160" sortable="custom">
          <template #default="{ row }">
            <div class="time-info">
              <div>{{ formatDateTime(row.lastActiveAt) }}</div>
              <div class="time-ago">{{ getTimeAgo(row.lastActiveAt) }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-dropdown @command="handleAction">
              <el-button type="primary" size="small">
                操作<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{action: 'view', row}" :icon="View">
                    查看详情
                  </el-dropdown-item>
                  <el-dropdown-item 
                    :command="{action: 'unbind', row}" 
                    :icon="Delete"
                    v-if="isAdmin"
                  >
                    强制解绑
                  </el-dropdown-item>
                  <el-dropdown-item 
                    :command="{action: 'physicalDelete', row}" 
                    :icon="Delete"
                    v-if="isAdmin"
                    style="color: #f56c6c;"
                  >
                    彻底删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 移动端卡片布局 -->
      <div v-else v-loading="loading" class="mobile-cards">
        <div v-if="devices.length === 0" class="empty-state">
          <el-empty description="暂无设备数据" />
        </div>
        <div v-else>
          <div v-for="device in devices" :key="device.id" class="device-card">
            <div class="device-card-header">
              <div class="device-basic-info">
                <div class="device-id-badge">
                  <el-tag size="small" type="info">#{{ device.id }}</el-tag>
                </div>
                <div class="device-name">{{ device.deviceName || '未知设备' }}</div>
                <div class="device-type">
                  <el-tag :type="getDeviceTypeColor(device.deviceType)" size="small">
                    {{ device.deviceTypeDescription }}
                  </el-tag>
                </div>
              </div>
              <div class="device-status">
                <el-tag :type="device.isActive ? 'success' : 'danger'" size="small">
                  {{ device.isActive ? '激活' : '停用' }}
                </el-tag>
              </div>
            </div>
            
            <div class="device-card-body">
              <div class="user-section">
                <div class="user-info">
                  <el-avatar :size="32" :src="device.user?.avatarUrl">
                    {{ device.user?.realName?.charAt(0) }}
                  </el-avatar>
                  <div class="user-details">
                    <div class="username">{{ device.user?.username }}</div>
                    <div class="realname">{{ device.user?.realName }}</div>
                  </div>
                </div>
              </div>
              
              <div class="device-info-grid">
                <div class="info-item">
                  <span class="info-label">操作系统</span>
                  <span class="info-value">{{ device.osInfo || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">浏览器</span>
                  <span class="info-value">{{ device.browserInfo || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">绑定状态</span>
                  <el-tag :type="getBindStatusColor(device.bindStatus)" size="small">
                    {{ device.bindStatusDescription }}
                  </el-tag>
                </div>
                <div class="info-item">
                  <span class="info-label">最后活跃</span>
                  <span class="info-value">{{ getTimeAgo(device.lastActiveAt) }}</span>
                </div>
              </div>
            </div>
            
            <div class="device-card-footer">
              <div class="last-active-time">
                <el-icon><Monitor /></el-icon>
                <span>{{ formatDateTime(device.lastActiveAt) }}</span>
              </div>
              <div class="card-actions">
                <el-dropdown @command="handleAction" trigger="click">
                  <el-button type="primary" size="small" class="mobile-action-btn">
                    操作<el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="{action: 'view', row: device}" :icon="View">
                        查看详情
                      </el-dropdown-item>
                      <el-dropdown-item 
                        :command="{action: 'unbind', row: device}" 
                        :icon="Delete"
                        v-if="isAdmin"
                      >
                        强制解绑
                      </el-dropdown-item>
                      <el-dropdown-item 
                        :command="{action: 'physicalDelete', row: device}" 
                        :icon="Delete"
                        v-if="isAdmin"
                        style="color: #f56c6c;"
                      >
                        彻底删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
        @current-change="loadDevices"
        @size-change="loadDevices"
        :small="isMobile"
      />
    </div>

    <!-- 设备详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="设备详情"
      :width="isMobile ? '95%' : '800px'"
      :close-on-click-modal="false"
      class="device-detail-dialog"
    >
      <div v-if="selectedDevice" class="device-detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户信息">
            <div class="user-info-detailed">
              <el-avatar :size="40" :src="selectedDevice.user?.avatarUrl">
                {{ selectedDevice.user?.realName?.charAt(0) }}
              </el-avatar>
              <div>
                <div><strong>{{ selectedDevice.user?.realName }}</strong> ({{ selectedDevice.user?.username }})</div>
                <div class="text-secondary">{{ selectedDevice.user?.email }}</div>
              </div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="设备指纹">
            <el-input :value="selectedDevice.deviceFingerprint" readonly>
              <template #append>
                <el-button @click="copyToClipboard(selectedDevice.deviceFingerprint)">
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </template>
            </el-input>
          </el-descriptions-item>
          <el-descriptions-item label="设备名称">{{ selectedDevice.deviceName }}</el-descriptions-item>
          <el-descriptions-item label="设备类型">
            <el-tag :type="getDeviceTypeColor(selectedDevice.deviceType)">
              {{ selectedDevice.deviceTypeDescription }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作系统">{{ selectedDevice.osInfo }}</el-descriptions-item>
          <el-descriptions-item label="浏览器">{{ selectedDevice.browserInfo }}</el-descriptions-item>
          <el-descriptions-item label="屏幕分辨率">{{ selectedDevice.screenResolution }}</el-descriptions-item>
          <el-descriptions-item label="时区">{{ selectedDevice.timezone }}</el-descriptions-item>
          <el-descriptions-item label="语言">{{ selectedDevice.language }}</el-descriptions-item>
          <el-descriptions-item label="IP地址">{{ selectedDevice.ipAddress }}</el-descriptions-item>
          <el-descriptions-item label="激活状态">
            <el-tag :type="selectedDevice.isActive ? 'success' : 'danger'">
              {{ selectedDevice.isActive ? '激活' : '停用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="绑定状态">
            <el-tag :type="getBindStatusColor(selectedDevice.bindStatus)">
              {{ selectedDevice.bindStatusDescription }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="首次绑定时间">{{ formatDateTime(selectedDevice.firstBoundAt) }}</el-descriptions-item>
          <el-descriptions-item label="最后活跃时间">{{ formatDateTime(selectedDevice.lastActiveAt) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(selectedDevice.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(selectedDevice.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 批量删除设备对话框 -->
    <el-dialog
      v-model="batchDeleteDialogVisible"
      title="批量删除用户设备"
      :width="isMobile ? '95%' : '500px'"
      :close-on-click-modal="false"
      class="batch-delete-dialog"
    >
      <el-form>
        <el-form-item label="选择用户：">
          <el-select
            v-model="selectedUserId"
            placeholder="请选择要删除设备的用户"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="`${user.realName} (${user.username})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-alert
          title="警告"
          type="error"
          :closable="false"
          show-icon
        >
          <p>此操作将彻底删除选定用户的所有设备记录和审计日志！</p>
          <p>包括PC端和移动端设备的所有历史数据。</p>
          <p style="color: #f56c6c; font-weight: bold;">此操作不可撤销，请谨慎操作！</p>
        </el-alert>
      </el-form>
      
      <template #footer>
        <el-button @click="batchDeleteDialogVisible = false">取消</el-button>
        <el-button 
          type="danger" 
          @click="confirmBatchDelete"
          :disabled="!selectedUserId"
        >
          确认删除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Monitor, Refresh, Delete, Search, View, ArrowDown, CopyDocument,
  TrendCharts, User
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const devices = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')
const windowWidth = ref(window.innerWidth)

const detailDialogVisible = ref(false)
const selectedDevice = ref(null)

const batchDeleteDialogVisible = ref(false)
const selectedUserId = ref('')
const userList = ref([])

const statisticsData = ref({
  totalDevices: 0,
  activeDevices: 0,
  deviceTypeDistribution: {},
  bindStatusDistribution: {},
  osDistribution: {}
})

// 计算属性
const isAdmin = computed(() => userStore.isAdmin)
const isMobile = computed(() => windowWidth.value <= 768)

const statistics = computed(() => [
  {
    label: '总设备数',
    value: statisticsData.value.totalDevices,
    icon: Monitor,
    iconClass: 'stats-icon-primary',
    trend: '+12%'
  },
  {
    label: '激活设备',
    value: statisticsData.value.activeDevices,
    icon: Monitor, // 使用Monitor代替Desktop
    iconClass: 'stats-icon-success'
  },
  {
    label: '移动设备',
    value: statisticsData.value.deviceTypeDistribution.MOBILE || 0,
    icon: Monitor, // 使用Monitor代替Mobile
    iconClass: 'stats-icon-warning'
  },
  {
    label: '桌面设备',
    value: statisticsData.value.deviceTypeDistribution.DESKTOP || 0,
    icon: Monitor, // 使用Monitor代替Desktop
    iconClass: 'stats-icon-info'
  }
])

// 生命周期
onMounted(() => {
  loadDevices()
  loadStatistics()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 响应式处理
const handleResize = () => {
  windowWidth.value = window.innerWidth
}

// 方法
const loadDevices = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }
    
    const response = await request.get('/devices/admin/list', { params })
    
    if (response.success) {
      devices.value = response.data.content
      total.value = response.data.totalElements
    } else {
      ElMessage.error(response.message || '加载设备列表失败')
    }
  } catch (error) {
    console.error('加载设备列表失败:', error)
    ElMessage.error('加载设备列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await request.get('/devices/admin/statistics')
    
    if (response.success) {
      statisticsData.value = response.data
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const refreshData = () => {
  loadDevices()
  loadStatistics()
}

const handleSearch = () => {
  currentPage.value = 1
  loadDevices()
}

const handleSortChange = ({ prop, order }) => {
  // 实现排序逻辑
  console.log('排序:', prop, order)
}

const handleAction = async (command) => {
  const { action, row } = command
  
  switch (action) {
    case 'view':
      selectedDevice.value = row
      detailDialogVisible.value = true
      break
      
    case 'unbind':
      try {
        await ElMessageBox.confirm(
          `确认强制解绑用户"${row.user?.username}"的设备"${row.deviceName}"吗？`,
          '确认解绑',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const response = await request.delete(`/devices/admin/${row.id}`)
        
        if (response.success) {
          ElMessage.success('设备强制解绑成功')
          refreshData()
        } else {
          ElMessage.error(response.message || '设备强制解绑失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('设备强制解绑失败:', error)
          ElMessage.error('设备强制解绑失败')
        }
      }
      break
      
    case 'physicalDelete':
      try {
        await ElMessageBox.confirm(
          `⚠️ 警告：此操作将彻底删除设备"${row.deviceName}"的所有记录和历史数据，包括审计日志！\n\n此操作不可撤销，将释放数据库存储空间。\n\n确认要彻底删除吗？`,
          '确认彻底删除',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'error',
            dangerouslyUseHTMLString: false
          }
        )
        
        const response = await request.delete(`/devices/admin/physical/${row.id}`)
        
        if (response.success) {
          ElMessage.success('设备记录已彻底删除，存储空间已释放')
          refreshData()
        } else {
          ElMessage.error(response.message || '彻底删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('彻底删除设备失败:', error)
          ElMessage.error('彻底删除失败')
        }
      }
      break
  }
}

const cleanupDevices = async () => {
  try {
    await ElMessageBox.confirm(
      '确认清理长时间未活跃的设备吗？此操作不可撤销。',
      '确认清理',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await request.post('/devices/admin/cleanup')
    
    if (response.success) {
      ElMessage.success('设备清理完成')
      refreshData()
    } else {
      ElMessage.error(response.message || '设备清理失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设备清理失败:', error)
      ElMessage.error('设备清理失败')
    }
  }
}

const getDeviceTypeColor = (type) => {
  const colors = {
    MOBILE: 'warning',
    TABLET: 'info',
    DESKTOP: 'primary',
    UNKNOWN: 'info'
  }
  return colors[type] || 'info'
}

const getBindStatusColor = (status) => {
  const colors = {
    ACTIVE: 'success',
    SUSPENDED: 'warning',
    REVOKED: 'danger'
  }
  return colors[status] || 'info'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const getTimeAgo = (dateTime) => {
  if (!dateTime) return ''
  
  const now = new Date()
  const time = new Date(dateTime)
  const diff = now - time
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else {
    return `${days}天前`
  }
}

const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const loadUserList = async () => {
  try {
    const response = await request.get('/users/admin/list', { params: { size: 1000 } })
    if (response.success) {
      userList.value = response.data || []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

const showBatchDeleteDialog = async () => {
  await loadUserList()
  selectedUserId.value = ''
  batchDeleteDialogVisible.value = true
}

const confirmBatchDelete = async () => {
  if (!selectedUserId.value) {
    ElMessage.warning('请选择要删除设备的用户')
    return
  }
  
  const selectedUser = userList.value.find(user => user.id === selectedUserId.value)
  const userName = selectedUser ? `${selectedUser.realName} (${selectedUser.username})` : '未知用户'
  
  try {
    await ElMessageBox.confirm(
      `⚠️ 最后确认：您即将删除用户"${userName}"的所有设备记录！\n\n这将包括：\n• 所有PC端和移动端设备记录\n• 所有相关的审计日志\n• 所有历史数据\n\n此操作将永久删除数据，无法恢复！\n\n确认要继续吗？`,
      '最终确认删除',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'error',
        dangerouslyUseHTMLString: false
      }
    )
    
    const response = await request.delete(`/devices/admin/user/${selectedUserId.value}/all-devices`)
    
    if (response.success) {
      ElMessage.success(`用户"${userName}"的所有设备记录已彻底删除`)
      batchDeleteDialogVisible.value = false
      refreshData()
    } else {
      ElMessage.error(response.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除设备失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}
</script>

<style scoped>
.device-management {
  padding: 0;
  background: #ffffff;
  min-height: auto;
  position: relative;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
  padding: 32px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
  pointer-events: none;
}

.header-content {
  position: relative;
  z-index: 1;
}

.title-section {
  position: relative;
  z-index: 2;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.title-icon {
  font-size: 36px;
  color: rgba(255, 255, 255, 0.9);
}

.page-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 300;
  letter-spacing: 0.3px;
}

.header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.decoration-circle-1 {
  width: 60px;
  height: 60px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.decoration-circle-2 {
  width: 80px;
  height: 80px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.decoration-circle-3 {
  width: 40px;
  height: 40px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.stats-section {
  margin-bottom: 24px;
}

.admin-notice {
  margin-bottom: 24px;
}

.modern-stats-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.05),
    0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.modern-stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 8px 25px rgba(0, 0, 0, 0.1),
    0 3px 10px rgba(0, 0, 0, 0.08);
  border-color: #c7d2fe;
}

.stats-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stats-icon-primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.stats-icon-success { background: linear-gradient(135deg, #48bb78 0%, #38a169 100%); }
.stats-icon-warning { background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%); }
.stats-icon-info { background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%); }

.stats-content {
  flex: 1;
}

.stats-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
}

.stats-label {
  font-size: 14px;
  color: #718096;
  margin-top: 4px;
}

.stats-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #48bb78;
  font-size: 12px;
  font-weight: 500;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.device-list {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.device-details {
  padding: 16px;
  background: #f8fafc;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username {
  font-weight: 600;
  color: #2d3748;
}

.realname {
  font-size: 12px;
  color: #718096;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.time-ago {
  font-size: 12px;
  color: #718096;
}

.user-info-detailed {
  display: flex;
  align-items: center;
  gap: 12px;
}

.text-secondary {
  color: #718096;
  font-size: 14px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

/* 搜索卡片样式 */
.search-card {
  margin-bottom: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-row {
  align-items: flex-end;
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.search-label.invisible {
  visibility: hidden;
}

.search-input {
  width: 100%;
}

.search-actions-wrapper {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.search-actions {
  flex: 1;
  display: flex;
  align-items: flex-end;
}

.action-buttons-row {
  width: 100%;
}

.action-btn {
  width: 100%;
  justify-content: center;
}

/* 移动端卡片样式 */
.mobile-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.device-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.device-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  border-color: #c7d2fe;
}

.device-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.device-basic-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.device-id-badge {
  align-self: flex-start;
}

.device-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a202c;
  line-height: 1.4;
  word-break: break-all;
}

.device-type {
  align-self: flex-start;
}

.device-status {
  flex-shrink: 0;
  margin-left: 12px;
}

.device-card-body {
  margin-bottom: 16px;
}

.user-section {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f8fafc;
}

.device-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #718096;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #2d3748;
  word-break: break-all;
  line-height: 1.4;
}

.device-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.last-active-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #718096;
}

.card-actions {
  flex-shrink: 0;
}

.mobile-action-btn {
  font-size: 13px;
  padding: 6px 12px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .page-title {
    font-size: 28px;
  }
  
  .modern-stats-card {
    padding: 20px;
  }
  
  .stats-value {
    font-size: 24px;
  }
}

@media (max-width: 768px) {
  .device-management {
    padding: 0 8px;
  }
  
  .page-header {
    margin-bottom: 24px;
    padding: 24px 16px;
    border-radius: 12px;
  }
  
  .page-title {
    font-size: 24px;
    gap: 8px;
  }
  
  .title-icon {
    font-size: 28px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .stats-section .el-col {
    margin-bottom: 16px;
  }
  
  .modern-stats-card {
    padding: 16px;
    gap: 12px;
  }
  
  .stats-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .stats-value {
    font-size: 20px;
  }
  
  .stats-label {
    font-size: 13px;
  }
  
  .search-card {
    margin-bottom: 16px;
    border-radius: 8px;
  }
  
  .search-row {
    align-items: stretch;
  }
  
  .search-item {
    gap: 6px;
  }
  
  .search-actions {
    margin-top: 8px;
  }
  
  .action-btn {
    height: 36px;
    font-size: 13px;
  }
  
  .device-card {
    padding: 12px;
    border-radius: 8px;
  }
  
  .device-card-header {
    margin-bottom: 12px;
    padding-bottom: 8px;
  }
  
  .device-name {
    font-size: 15px;
  }
  
  .device-info-grid {
    gap: 8px 12px;
  }
  
  .info-value {
    font-size: 13px;
  }
  
  .pagination {
    margin-top: 16px;
  }
}

@media (max-width: 480px) {
  .device-management {
    padding: 0 4px;
  }
  
  .page-header {
    padding: 20px 12px;
    margin-bottom: 16px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .title-icon {
    font-size: 24px;
  }
  
  .modern-stats-card {
    padding: 12px;
    gap: 8px;
  }
  
  .stats-icon {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }
  
  .stats-value {
    font-size: 18px;
  }
  
  .stats-label {
    font-size: 12px;
  }
  
  .device-card {
    padding: 10px;
  }
  
  .device-name {
    font-size: 14px;
  }
  
  .device-info-grid {
    grid-template-columns: 1fr;
    gap: 6px;
  }
  
  .info-value {
    font-size: 12px;
  }
  
  .device-card-footer {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
  
  .last-active-time {
    justify-content: center;
    font-size: 11px;
  }
  
  .card-actions {
    align-self: center;
  }
  
  .mobile-action-btn {
    font-size: 12px;
    padding: 4px 8px;
  }
}

/* 对话框响应式样式 */
:deep(.device-detail-dialog) {
  @media (max-width: 768px) {
    .el-dialog {
      margin: 20px !important;
      max-height: calc(100vh - 40px) !important;
    }
    
    .el-dialog__body {
      padding: 15px !important;
      max-height: calc(100vh - 140px) !important;
      overflow-y: auto !important;
    }
    
    .el-descriptions {
      font-size: 13px !important;
    }
    
    .el-descriptions__label {
      font-size: 12px !important;
      width: 80px !important;
    }
    
    .el-descriptions__content {
      font-size: 13px !important;
    }
  }
}

:deep(.batch-delete-dialog) {
  @media (max-width: 768px) {
    .el-dialog {
      margin: 20px !important;
      max-height: calc(100vh - 40px) !important;
    }
    
    .el-dialog__body {
      padding: 15px !important;
    }
    
    .el-form-item__label {
      font-size: 13px !important;
    }
    
    .el-alert {
      font-size: 12px !important;
    }
    
    .el-alert__title {
      font-size: 13px !important;
    }
  }
}
</style>
