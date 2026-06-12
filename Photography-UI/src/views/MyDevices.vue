<template>
  <div class="my-devices">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><Monitor /></el-icon>
            我的设备
          </h1>
          <p class="page-subtitle">管理您绑定的登录设备</p>
        </div>
      </div>
    </div>

    <div class="devices-workspace">
      <!-- 安全提示 -->
      <aside class="security-card">
        <div class="security-card-header">
          <div class="security-icon">
            <el-icon><Monitor /></el-icon>
          </div>
          <div>
            <h2>安全提醒</h2>
            <p>账号设备绑定规则</p>
          </div>
        </div>
        <p class="security-lead">系统限制每个账号只能绑定一个设备，绑定后用户无法自行解绑。</p>
        <div class="security-rules">
          <div class="security-rule">
            <span class="rule-index">01</span>
            <span>首次登录时自动绑定当前设备</span>
          </div>
          <div class="security-rule">
            <span class="rule-index">02</span>
            <span>只能在绑定设备上登录和签到</span>
          </div>
          <div class="security-rule">
            <span class="rule-index">03</span>
            <span>设备绑定后无法自行解绑</span>
          </div>
          <div class="security-rule">
            <span class="rule-index">04</span>
            <span>更换设备请联系管理员处理</span>
          </div>
        </div>
        <el-button class="contact-button" :icon="Service" @click="contactAdmin">
          联系管理员
        </el-button>
      </aside>

      <!-- 设备列表 -->
      <section class="devices-panel">
        <div class="devices-panel-header">
          <div>
            <h2>绑定设备</h2>
            <p>{{ devices.length > 0 ? `当前账号已绑定 ${devices.length} 台设备` : '当前账号暂未绑定登录设备' }}</p>
          </div>
          <div class="devices-panel-actions">
            <el-tag :type="devices.length > 0 ? 'success' : 'info'" effect="plain">
              {{ devices.length > 0 ? '已绑定' : '未绑定' }}
            </el-tag>
            <el-button @click="refreshDevices" :icon="Refresh" :loading="loading">
              刷新
            </el-button>
          </div>
        </div>

        <div class="device-cards">
          <div v-if="loading" class="loading-container">
            <el-skeleton :rows="5" animated />
          </div>
          
          <div v-else-if="devices.length === 0" class="empty-state">
            <div class="empty-visual">
              <el-icon><Monitor /></el-icon>
            </div>
            <h3>暂无绑定设备</h3>
            <p>首次在当前浏览器登录或签到后，系统会自动完成设备绑定。</p>
            <div class="empty-actions">
              <el-button type="primary" :icon="Refresh" @click="refreshDevices">刷新状态</el-button>
              <el-button :icon="Service" @click="contactAdmin">联系管理员</el-button>
            </div>
          </div>
          
          <div v-else class="device-grid">
            <div 
              v-for="device in devices" 
              :key="device.id" 
              class="device-card"
              :class="{ 'device-active': device.isActive }"
            >
              <div class="device-card-top">
                <div class="device-icon">
                  <el-icon size="34" :color="getDeviceIconColor(device.deviceType)">
                    <component :is="getDeviceIcon(device.deviceType)" />
                  </el-icon>
                </div>
                <div class="device-status">
                  <el-tag :type="device.isActive ? 'success' : 'danger'" size="small" effect="plain">
                    {{ device.isActive ? '激活' : '停用' }}
                  </el-tag>
                  <el-tag :type="getBindStatusColor(device.bindStatus)" size="small" effect="plain">
                    {{ device.bindStatusDescription }}
                  </el-tag>
                </div>
              </div>

              <div class="device-info">
                <h3 class="device-name">{{ device.deviceName || '未知设备' }}</h3>
                <p class="device-type">{{ device.deviceTypeDescription }}</p>
                
                <div class="device-details">
                  <div class="detail-item">
                    <el-icon><Monitor /></el-icon>
                    <span>{{ device.osInfo }}</span>
                  </div>
                  <div class="detail-item">
                    <el-icon><Monitor /></el-icon>
                    <span>{{ device.browserInfo }}</span>
                  </div>
                  <div class="detail-item" v-if="device.screenResolution">
                    <el-icon><FullScreen /></el-icon>
                    <span>{{ device.screenResolution }}</span>
                  </div>
                  <div class="detail-item">
                    <el-icon><Location /></el-icon>
                    <span>{{ device.ipAddress }}</span>
                  </div>
                </div>

                <div class="device-times">
                  <div class="time-item">
                    <span class="time-label">首次绑定</span>
                    <span class="time-value">{{ formatDateTime(device.firstBoundAt) }}</span>
                  </div>
                  <div class="time-item">
                    <span class="time-label">最后活跃</span>
                    <span class="time-value">{{ formatDateTime(device.lastActiveAt) }}</span>
                    <span class="time-ago">{{ getTimeAgo(device.lastActiveAt) }}</span>
                  </div>
                </div>
              </div>

              <div class="device-actions">
                <el-button type="info" size="small" @click="viewDeviceDetail(device)" :icon="View">
                  查看详情
                </el-button>
                <el-button type="warning" size="small" @click="contactAdmin" :icon="Service">
                  联系管理员
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- 设备详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="设备详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedDevice" class="device-detail-content">
        <div class="detail-header">
          <div class="device-icon-large">
            <el-icon size="64" :color="getDeviceIconColor(selectedDevice.deviceType)">
              <component :is="getDeviceIcon(selectedDevice.deviceType)" />
            </el-icon>
          </div>
          <div class="device-title">
            <h3>{{ selectedDevice.deviceName || '未知设备' }}</h3>
            <p>{{ selectedDevice.deviceTypeDescription }}</p>
          </div>
        </div>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="设备指纹">
            <div class="fingerprint-display">
              <code>{{ selectedDevice.deviceFingerprint }}</code>
              <el-button 
                size="small" 
                @click="copyToClipboard(selectedDevice.deviceFingerprint)"
                :icon="CopyDocument"
              >
                复制
              </el-button>
            </div>
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
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Monitor, Refresh, View, CopyDocument, Location, Service,
  FullScreen
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)
const devices = ref([])
const detailDialogVisible = ref(false)
const selectedDevice = ref(null)

// 计算属性 - 移除了hasActiveDevices，因为用户无法解绑设备

// 生命周期
onMounted(() => {
  loadMyDevices()
})

// 方法
const loadMyDevices = async () => {
  try {
    loading.value = true
    const response = await request.get('/devices/my')
    
    if (response.success) {
      devices.value = response.data
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

const refreshDevices = () => {
  loadMyDevices()
}

const viewDeviceDetail = (device) => {
  selectedDevice.value = device
  detailDialogVisible.value = true
}

const contactAdmin = () => {
  ElMessageBox.alert(
    `如需更换设备或解决设备相关问题，请联系管理员处理。
    
    联系方式：
    • 系统内消息：通过系统消息功能联系管理员
    • 邮件联系：发送邮件至管理员邮箱
    • 现场联系：直接找管理员处理
    
    请说明您的用户名和遇到的具体问题。`,
    '联系管理员',
    {
      confirmButtonText: '知道了',
      type: 'info',
      dangerouslyUseHTMLString: false
    }
  )
}

const getDeviceIcon = (type) => {
  // 统一使用Monitor图标，因为Element Plus中很多设备图标不存在
  return Monitor
}

const getDeviceIconColor = (type) => {
  const colors = {
    MOBILE: '#f56565',
    TABLET: '#4299e1',
    DESKTOP: '#48bb78',
    UNKNOWN: '#718096'
  }
  return colors[type] || '#718096'
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
</script>

<style scoped>
.my-devices {
  padding: 0;
  background: #ffffff;
  min-height: auto;
}

.page-header {
  text-align: center;
  margin-bottom: 24px;
  padding: 24px 0;
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

.page-title {
  font-size: 28px;
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
  font-size: 32px;
  color: rgba(255, 255, 255, 0.9);
}

.page-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 300;
}

.security-notice {
  margin-bottom: 24px;
}

.loading-container,
.empty-state {
  padding: 40px 20px;
  text-align: center;
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}

.device-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 24px;
  transition: all 0.3s ease;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.device-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  border-color: #c7d2fe;
}

.device-active {
  border-color: #48bb78;
  background: linear-gradient(135deg, rgba(72, 187, 120, 0.05) 0%, rgba(56, 161, 105, 0.02) 100%);
}

.device-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.device-icon {
  text-align: center;
  margin-bottom: 16px;
}

.device-info {
  margin-bottom: 20px;
}

.device-name {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 4px 0;
  text-align: center;
}

.device-type {
  color: #718096;
  margin: 0 0 16px 0;
  text-align: center;
  font-size: 14px;
}

.device-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #4a5568;
}

.detail-item .el-icon {
  color: #718096;
  font-size: 16px;
}

.device-times {
  background: #f7fafc;
  padding: 12px;
  border-radius: 8px;
  font-size: 12px;
}

.time-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.time-item:last-child {
  margin-bottom: 0;
}

.time-label {
  color: #718096;
  font-weight: 500;
}

.time-value {
  color: #2d3748;
}

.time-ago {
  color: #a0aec0;
  font-style: italic;
}

.device-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-buttons {
  text-align: center;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.action-buttons .el-button {
  margin: 0 8px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f7fafc;
  border-radius: 8px;
}

.device-icon-large {
  flex-shrink: 0;
}

.device-title h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #2d3748;
}

.device-title p {
  margin: 0;
  color: #718096;
  font-size: 14px;
}

.fingerprint-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.fingerprint-display code {
  flex: 1;
  background: #f7fafc;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  word-break: break-all;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .device-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .device-card {
    padding: 16px;
  }
  
  .action-buttons .el-button {
    margin: 4px;
    width: calc(50% - 8px);
  }
}

/* Fresh glass workspace layout */
.my-devices {
  max-width: 1440px;
  margin: 0 auto;
  padding: 4px 0 28px;
  background: transparent;
  min-height: auto;
}

.page-header {
  margin-bottom: 18px;
  padding: 0;
  text-align: left;
  background: transparent;
  border-radius: 0;
}

.page-header::before {
  display: none;
}

.header-content {
  padding: 24px 28px;
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 24px;
  background:
    radial-gradient(circle at 86% 4%, rgba(75, 211, 180, 0.16), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(236, 250, 255, 0.76));
  box-shadow: 0 18px 48px rgba(18, 85, 116, 0.1);
}

.page-title {
  justify-content: flex-start;
  margin: 0 0 8px;
  color: #123044;
  font-size: 30px;
  font-weight: 850;
  letter-spacing: 0;
  text-shadow: none;
}

.title-icon {
  color: #12aee7;
  font-size: 32px;
}

.page-subtitle {
  color: #496579;
  font-size: 15px;
  font-weight: 650;
}

.devices-workspace {
  display: grid;
  grid-template-columns: minmax(300px, 0.42fr) minmax(520px, 1fr);
  gap: 20px;
  align-items: start;
}

.security-card,
.devices-panel {
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 48px rgba(18, 85, 116, 0.1);
}

.security-card {
  position: sticky;
  top: 12px;
  padding: 20px;
  background:
    radial-gradient(circle at 92% 0%, rgba(244, 185, 66, 0.12), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 250, 240, 0.76));
}

.security-card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

.security-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  color: #9a640d;
  background: var(--button-warning-bg);
  border: 1px solid var(--button-warning-border);
  border-radius: 16px;
  box-shadow: 0 10px 22px rgba(244, 185, 66, 0.12);
}

.security-card h2,
.devices-panel-header h2 {
  margin: 0;
  color: #123044;
  font-size: 18px;
  font-weight: 850;
}

.security-card-header p,
.devices-panel-header p {
  margin: 4px 0 0;
  color: #5b7588;
  font-size: 13px;
  font-weight: 650;
}

.security-lead {
  margin: 0 0 14px;
  padding: 12px 14px;
  color: #7a520d;
  background: rgba(255, 250, 240, 0.82);
  border: 1px solid rgba(244, 185, 66, 0.22);
  border-radius: 16px;
  font-size: 13px;
  font-weight: 750;
  line-height: 1.6;
}

.security-rules {
  display: grid;
  gap: 10px;
  margin-bottom: 16px;
}

.security-rule {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  min-height: 42px;
  padding: 9px 10px;
  color: #496579;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(98, 177, 210, 0.12);
  border-radius: 14px;
  font-size: 13px;
  font-weight: 700;
}

.rule-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 24px;
  color: #0876a5;
  background: rgba(229, 249, 255, 0.86);
  border-radius: 999px;
  font-family: var(--font-family-mono);
  font-size: 12px;
  font-weight: 850;
}

.contact-button {
  width: 100%;
  height: 42px;
  color: var(--button-warning-text);
  font-weight: 850;
  background: var(--button-warning-bg);
  border-color: var(--button-warning-border);
  border-radius: 999px;
}

.devices-panel {
  overflow: hidden;
}

.devices-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 22px;
  border-bottom: 1px solid rgba(98, 177, 210, 0.18);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(240, 251, 255, 0.72));
}

.devices-panel-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.devices-panel-actions .el-button,
.empty-actions .el-button,
.device-actions .el-button {
  height: 38px;
  border-radius: 999px;
  font-weight: 850;
}

.devices-panel-actions .el-button,
.empty-actions .el-button--primary {
  color: var(--button-primary-text);
  background: var(--button-primary-bg);
  border-color: var(--button-primary-border);
  box-shadow: 0 10px 22px rgba(24, 185, 236, 0.1);
}

.device-cards {
  padding: 20px;
}

.loading-container {
  padding: 24px;
}

.empty-state {
  display: grid;
  place-items: center;
  min-height: 360px;
  padding: 40px 24px;
  text-align: center;
  border: 1px dashed rgba(98, 177, 210, 0.24);
  border-radius: 22px;
  background:
    radial-gradient(circle at 50% 0%, rgba(24, 185, 236, 0.1), transparent 34%),
    rgba(255, 255, 255, 0.62);
}

.empty-visual {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 86px;
  height: 86px;
  margin-bottom: 16px;
  color: #12aee7;
  background: rgba(229, 249, 255, 0.86);
  border: 1px solid rgba(24, 185, 236, 0.2);
  border-radius: 28px;
  box-shadow: 0 16px 34px rgba(18, 174, 231, 0.1);
}

.empty-visual .el-icon {
  font-size: 42px;
}

.empty-state h3 {
  margin: 0 0 8px;
  color: #123044;
  font-size: 20px;
  font-weight: 850;
}

.empty-state p {
  max-width: 420px;
  margin: 0 0 18px;
  color: #5b7588;
  font-size: 14px;
  font-weight: 650;
  line-height: 1.7;
}

.empty-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.device-grid {
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 16px;
}

.device-card {
  padding: 18px;
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 22px;
  background:
    radial-gradient(circle at 92% 0%, rgba(75, 211, 180, 0.12), transparent 34%),
    rgba(255, 255, 255, 0.84);
  box-shadow: 0 14px 34px rgba(18, 85, 116, 0.09);
}

.device-card:hover {
  transform: translateY(-1px);
  border-color: rgba(24, 185, 236, 0.3);
  box-shadow: 0 18px 42px rgba(18, 85, 116, 0.12);
}

.device-active {
  border-color: rgba(33, 185, 139, 0.28);
  background:
    radial-gradient(circle at 92% 0%, rgba(75, 211, 180, 0.16), transparent 34%),
    rgba(255, 255, 255, 0.88);
}

.device-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.device-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  margin: 0;
  background: rgba(240, 251, 255, 0.82);
  border: 1px solid rgba(98, 177, 210, 0.16);
  border-radius: 18px;
}

.device-status {
  justify-content: flex-end;
  gap: 8px;
  margin: 0;
  flex-wrap: wrap;
}

.device-name,
.device-type {
  text-align: left;
}

.device-name {
  color: #123044;
  font-size: 18px;
  font-weight: 850;
}

.device-type {
  color: #5b7588;
  font-weight: 650;
}

.detail-item {
  min-height: 34px;
  padding: 7px 10px;
  color: #496579;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(98, 177, 210, 0.12);
  border-radius: 12px;
  font-weight: 650;
}

.detail-item .el-icon {
  color: #12aee7;
}

.device-times {
  display: grid;
  gap: 8px;
  padding: 12px;
  background: rgba(240, 251, 255, 0.68);
  border: 1px solid rgba(98, 177, 210, 0.14);
  border-radius: 16px;
}

.time-item {
  align-items: flex-start;
  gap: 8px;
}

.time-label {
  color: #5b7588;
  font-weight: 800;
}

.time-value {
  color: #123044;
  font-weight: 750;
  text-align: right;
}

.time-ago {
  color: #0876a5;
  font-style: normal;
  font-weight: 750;
}

.device-actions {
  justify-content: flex-start;
  flex-wrap: wrap;
}

.device-actions .el-button--info {
  color: var(--button-primary-text);
  background: var(--button-primary-bg);
  border-color: var(--button-primary-border);
}

.device-actions .el-button--warning,
.empty-actions .el-button:not(.el-button--primary) {
  color: var(--button-warning-text);
  background: var(--button-warning-bg);
  border-color: var(--button-warning-border);
}

@media (max-width: 1024px) {
  .devices-workspace {
    grid-template-columns: 1fr;
  }

  .security-card {
    position: static;
  }
}

@media (max-width: 768px) {
  .my-devices {
    padding: 0 0 22px;
  }

  .header-content {
    padding: 20px;
    border-radius: 20px;
  }

  .page-title {
    font-size: 24px;
  }

  .devices-panel-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .devices-panel-actions,
  .devices-panel-actions .el-button {
    width: 100%;
  }

  .device-cards {
    padding: 14px;
  }

  .device-grid {
    grid-template-columns: 1fr;
  }

  .empty-actions,
  .empty-actions .el-button {
    width: 100%;
  }

  .device-card-top {
    align-items: flex-start;
  }

  .device-actions,
  .device-actions .el-button {
    width: 100%;
  }
}
</style>
