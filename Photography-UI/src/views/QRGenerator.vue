<template>
  <div class="qr-generator">
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><Grid /></el-icon>
            二维码生成工具
          </h1>
          <p class="page-subtitle">为打卡配置生成二维码，支持扫码签到</p>
        </div>
      </div>
    </div>

    <div class="generator-content">
      <div class="generator-grid">
        <!-- 生成配置 -->
        <section class="generator-panel config-panel">
          <el-card class="modern-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><Setting /></el-icon>
                <span>生成配置</span>
              </div>
            </template>

            <el-form :model="form" label-width="100px">
              <el-form-item label="选择配置">
                <el-select
                  v-model="selectedConfigId"
                  placeholder="请选择打卡配置"
                  clearable
                  filterable
                  @change="handleConfigChange"
                  style="width: 100%"
                  :loading="loadingConfigs"
                  no-data-text="暂无启用的配置，请先创建并启用配置"
                  popper-class="qr-config-select"
                >
                  <el-option
                    v-for="config in configList"
                    :key="config.id"
                    :label="config.name"
                    :value="config.id"
                  >
                    <div class="config-option">
                      <div class="config-option-main">
                        <el-icon class="config-icon"><Grid /></el-icon>
                        <span class="config-name">{{ config.name }}</span>
                      </div>
                      <div class="config-option-meta">
                        <el-tag 
                          size="small" 
                          type="primary" 
                          effect="plain"
                          style="flex-shrink: 0;"
                        >
                          <el-icon style="margin-right: 4px; vertical-align: -2px;"><Location /></el-icon>
                          {{ config.locationName }}
                        </el-tag>
                        <el-tag 
                          size="small" 
                          type="success" 
                          effect="plain"
                          style="flex-shrink: 0;"
                        >
                          <el-icon style="margin-right: 4px; vertical-align: -2px;"><Clock /></el-icon>
                          {{ config.sessionName }}
                        </el-tag>
                      </div>
                    </div>
                  </el-option>
                </el-select>
                
                <!-- 已选配置信息展示 -->
                <div v-if="selectedConfigId && selectedConfig" class="selected-config-info">
                  <div class="info-item">
                    <el-icon><Grid /></el-icon>
                    <span class="info-label">配置名称:</span>
                    <span class="info-value">{{ selectedConfig.name }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Location /></el-icon>
                    <span class="info-label">打卡地点:</span>
                    <span class="info-value">{{ selectedConfig.locationName }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Clock /></el-icon>
                    <span class="info-label">时间段:</span>
                    <span class="info-value">{{ selectedConfig.sessionName }}</span>
                  </div>
                </div>
                
                <div v-if="!loadingConfigs && configList.length === 0" style="margin-top: 8px;">
                  <el-alert
                    title="提示"
                    type="info"
                    :closable="false"
                    show-icon
                  >
                    暂无启用的打卡配置。请先前往"打卡配置"页面创建并启用配置。
                  </el-alert>
                </div>
              </el-form-item>

              <el-form-item label="有效期">
                <div style="width: 100%;">
                  <el-date-picker
                    v-model="form.expireTime"
                    type="datetime"
                    placeholder="选择过期时间（可选）"
                    format="YYYY-MM-DD HH:mm:ss"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%;"
                    :clearable="true"
                    :disabled-date="(time) => time.getTime() < Date.now()"
                  />
                  <div class="form-item-tip">
                    <el-icon><InfoFilled /></el-icon>
                    <span>设置二维码的有效期限，留空则永久有效</span>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="附加信息">
                <div style="width: 100%;">
                  <el-input
                    v-model="form.extra"
                    type="textarea"
                    :rows="3"
                    placeholder="其他信息（可选）"
                  />
                  <div class="form-item-tip">
                    <el-icon><InfoFilled /></el-icon>
                    <span>可以添加备注、使用说明等额外信息</span>
                  </div>
                </div>
              </el-form-item>

              <el-form-item>
                <div class="form-actions">
                  <el-button 
                    type="primary" 
                    @click="generateQR" 
                    :loading="generating"
                    size="large"
                  >
                    <el-icon><Star /></el-icon>
                    生成二维码
                  </el-button>
                  <el-button 
                    @click="resetForm"
                    size="large"
                  >
                    <el-icon><RefreshLeft /></el-icon>
                    重置
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
          </el-card>
        </section>

        <!-- 二维码预览 -->
        <section class="generator-panel preview-panel">
          <el-card class="modern-card" shadow="never">
            <template #header>
              <div class="card-header preview-header">
                <div class="header-left">
                  <el-icon><View /></el-icon>
                  <span>二维码预览</span>
                </div>
                <div class="header-right" v-if="qrCodeUrl">
                  <span class="countdown-label">自动刷新</span>
                  <el-progress 
                    :percentage="countdownPercent" 
                    :stroke-width="8" 
                    :color="progressColor" 
                    :show-text="false"
                    class="countdown-progress"
                  />
                  <span class="countdown-time">{{ formattedCountdown }}</span>
                </div>
              </div>
            </template>

            <div class="qr-preview">
              <div v-if="!qrCodeUrl" class="empty-state">
                <el-icon size="64" color="#c0c4cc"><Grid /></el-icon>
                <p>请先配置参数并生成二维码</p>
              </div>
              
              <div v-else class="qr-display">
                <!-- 左侧：二维码图片 -->
                <div class="qr-left-section">
                  <div class="qr-image-wrapper">
                    <div class="qr-image-container">
                      <img :src="qrCodeUrl" alt="二维码" class="qr-code-image" />
                    </div>
                    <div class="qr-image-label">
                      <el-icon><Grid /></el-icon>
                      <span>扫码签到</span>
                    </div>
                  </div>
                  
                <!-- 操作按钮 -->
                <div class="qr-actions">
                  <el-button @click="downloadQR" type="success" class="action-btn">
                    <template #icon>
                      <el-icon><Download /></el-icon>
                    </template>
                    下载二维码
                  </el-button>
                  <el-button @click="copyContent" type="primary" plain class="action-btn">
                    <template #icon>
                      <el-icon><CopyDocument /></el-icon>
                    </template>
                    复制内容
                  </el-button>
                  <el-button @click="forceRefresh" type="warning" plain class="action-btn">
                    <template #icon>
                      <el-icon><RefreshLeft /></el-icon>
                    </template>
                    立即刷新
                  </el-button>
                </div>
                </div>
                
                <!-- 右侧：配置信息 -->
                <div class="qr-right-section">
                  <div class="info-card">
                    <div class="info-header">
                      <el-icon class="info-icon"><InfoFilled /></el-icon>
                      <span class="info-title">配置信息</span>
                    </div>
                    <div class="info-content">
                      <div class="info-item">
                        <span class="info-label">配置名称</span>
                        <span class="info-value">{{ form.configName || '-' }}</span>
                      </div>
                      <div class="info-item">
                        <span class="info-label">配置ID</span>
                        <span class="info-value">#{{ form.configId || '-' }}</span>
                      </div>
                      <div class="info-item" v-if="selectedConfig">
                        <span class="info-label">打卡地点</span>
                        <span class="info-value">
                          <el-icon><LocationInformation /></el-icon>
                          {{ selectedConfig.locationName || '-' }}
                        </span>
                      </div>
                      <div class="info-item" v-if="selectedConfig">
                        <span class="info-label">时间段</span>
                        <span class="info-value">
                          <el-icon><Clock /></el-icon>
                          {{ selectedConfig.sessionName || '-' }}
                        </span>
                      </div>
                      <div class="info-item" v-if="form.expireTime">
                        <span class="info-label">有效期至</span>
                        <span class="info-value expiry-time">{{ form.expireTime }}</span>
                      </div>
                    </div>
                  </div>
                  
                  <div class="status-card">
                    <div class="status-item success">
                      <el-icon class="status-icon"><CircleCheck /></el-icon>
                      <div class="status-text">
                        <div class="status-title">已自动保存</div>
                        <div class="status-desc">二维码内容已同步到配置</div>
                      </div>
                    </div>
                  </div>
                  
                  <div class="qr-content-card">
                    <div class="content-header">
                      <span class="content-label">二维码内容</span>
                      <el-button 
                        size="small" 
                        text 
                        @click="copyContent"
                        class="copy-btn"
                      >
                        <el-icon><CopyDocument /></el-icon>
                        复制
                      </el-button>
                    </div>
                    <div class="content-value">
                      <code>{{ qrContent }}</code>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </section>
      </div>

      <!-- 使用说明 -->
      <div class="usage-section">
          <el-card class="modern-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><InfoFilled /></el-icon>
                <span>使用说明</span>
              </div>
            </template>

            <el-steps :active="4" finish-status="success">
              <el-step title="选择配置" description="从下拉列表中选择已创建的打卡配置" />
              <el-step title="自动生成" description="系统自动生成二维码并保存到配置" />
              <el-step title="下载打印" description="下载二维码图片并打印" />
              <el-step title="张贴使用" description="将二维码张贴在签到地点" />
              <el-step title="扫码签到" description="用户扫码即可完成签到" />
            </el-steps>

            <el-alert
              title="功能特性"
              type="success"
              :closable="false"
              show-icon
              style="margin-top: 16px;"
            >
              <ul style="margin: 0; padding-left: 20px;">
                <li><strong>自动保存：</strong>生成的二维码内容会自动保存到打卡配置，无需手动填写</li>
                <li><strong>自动刷新：</strong>二维码每60秒自动刷新一次，防止作弊行为</li>
                <li><strong>持久化：</strong>切换页面后返回，二维码和倒计时会自动恢复</li>
                <li><strong>一键操作：</strong>选择配置后自动生成，简化操作流程</li>
              </ul>
            </el-alert>

            <el-alert
              title="注意事项"
              type="warning"
              :closable="false"
              show-icon
              style="margin-top: 12px;"
            >
              <ul style="margin: 0; padding-left: 20px;">
                <li>请从列表中选择已启用的打卡配置</li>
                <li>二维码内容包含配置信息，请妥善保管</li>
                <li>建议定期更新二维码，提高签到安全性</li>
                <li>可以为不同的签到点生成不同的二维码</li>
                <li>如果列表为空，请先在"打卡配置"页面创建配置</li>
              </ul>
            </el-alert>
          </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Grid, Setting, Star, RefreshLeft, View, Download, 
  CopyDocument, InfoFilled, Location, Clock, LocationInformation,
  CircleCheck
} from '@element-plus/icons-vue'
import QRCode from 'qrcode'
import request from '@/utils/request'

// 响应式数据
const generating = ref(false)
const qrCodeUrl = ref('')
const qrContent = ref('')
const countdownSeconds = ref(0)
let countdownTimer = null
const nextRefreshTime = ref(null)
const STORAGE_KEY = 'qrGeneratorPersisted'
const countdownTotal = ref(60)
const loadingConfigs = ref(false)
const configList = ref([])
const selectedConfigId = ref(null)

const form = reactive({
  configId: '',
  configName: '',
  expireTime: '',
  extra: ''
})

// 当前选中的配置对象（计算属性）
const selectedConfig = computed(() => {
  if (!selectedConfigId.value) return null
  return configList.value.find(c => c.id === selectedConfigId.value)
})

// 加载打卡配置列表
const loadConfigurations = async () => {
  loadingConfigs.value = true
  try {
    const response = await request.get('/checkin/configurations/active')
    console.log('API响应:', response)
    
    // 统一使用 success !== false 判断成功（与其他页面一致）
    if (response && response.success !== false) {
      const data = response.data || response || []
      configList.value = Array.isArray(data) ? data : []
      console.log('加载配置列表成功:', configList.value)
      
      if (configList.value.length === 0) {
        ElMessage.warning('暂无启用的打卡配置，请先在"打卡配置"页面创建并启用配置')
      }
      // 尝试从本地恢复二维码状态（在配置加载完成后再做，确保能匹配已选配置）
      restoreFromStorage()
    } else {
      console.error('加载失败，消息:', response.message)
      ElMessage.error(response.message || '加载配置列表失败')
    }
  } catch (error) {
    console.error('加载配置列表异常:', error)
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error('请先登录后再使用此功能')
    } else {
      ElMessage.error('加载配置列表失败，请检查网络连接')
    }
  } finally {
    loadingConfigs.value = false
  }
}

// 处理配置选择变化
const handleConfigChange = (configId) => {
  if (!configId) {
    form.configId = ''
    form.configName = ''
    return
  }
  
  const selectedConfig = configList.value.find(c => c.id === configId)
  if (selectedConfig) {
    form.configId = String(selectedConfig.id)
    form.configName = `${selectedConfig.name} (${selectedConfig.locationName} - ${selectedConfig.sessionName})`
    console.log('选择配置:', selectedConfig)
    // 当选择配置变更时，自动以当前表单内容生成二维码
    // 这样自动刷新时无需重新配置内容
    generateQR()
  }
}

// 组件挂载时加载配置列表
onMounted(() => {
  loadConfigurations()
  // 若已有持久化记录（例如刷新浏览器后），恢复并自动续跑倒计时
  // 具体恢复逻辑在 loadConfigurations 完成后执行
})

// 生成二维码
const generateQR = async () => {
  if (!form.configId.trim()) {
    ElMessage.error('请选择打卡配置')
    return
  }

  generating.value = true

  try {
    // 构建二维码内容
    const qrData = {
      type: 'CHECKIN',
      configId: Number(form.configId.trim()),
      name: selectedConfig.value?.name || '',
      location: selectedConfig.value?.locationName || '',
      session: selectedConfig.value?.sessionName || '',
      expireTime: form.expireTime || '',
      extra: form.extra || '',
      generateTime: new Date().toISOString()
    }

    const content = JSON.stringify(qrData)
    qrContent.value = content

    // 生成二维码
    const options = {
      width: 256,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#FFFFFF'
      }
    }

    const url = await QRCode.toDataURL(content, options)
    qrCodeUrl.value = url

    // 自动写入到后端配置
    await saveQRCodeToBackend(content)

    ElMessage.success('二维码生成成功并已保存到配置')

    // 启动1分钟自动刷新倒计时
    startCountdown(60)
    // 持久化当前二维码
    persistToStorage()

  } catch (error) {
    console.error('生成二维码失败:', error)
    ElMessage.error('生成二维码失败')
  } finally {
    generating.value = false
  }
}

// 将二维码内容保存到后端配置
const saveQRCodeToBackend = async (qrCodeContent) => {
  try {
    if (!selectedConfig.value) {
      console.warn('未找到选中的配置，跳过保存到后端')
      return
    }

    const configId = selectedConfig.value.id

    // 第一步：从后端获取最新的完整配置数据
    console.log('正在获取配置最新数据...')
    const getResponse = await request.get(`/checkin/configurations/${configId}`)
    
    if (!getResponse || !getResponse.data) {
      console.error('获取配置数据失败')
      return
    }

    const latestConfig = getResponse.data

    // 第二步：转换数据格式（Response → Request）
    // 重要：requiredUsers（对象数组） → requiredUserIds（ID数组）
    const requiredUserIds = latestConfig.requiredUsers 
      ? latestConfig.requiredUsers.map(user => user.id)
      : []

    // 第三步：构建更新数据，只修改 qrCode 字段
    const updateData = {
      name: latestConfig.name,
      description: latestConfig.description || '',
      locationName: latestConfig.locationName,
      locationAddress: latestConfig.locationAddress || '',
      locationDescription: latestConfig.locationDescription || '',
      longitude: latestConfig.longitude,
      latitude: latestConfig.latitude,
      sessionName: latestConfig.sessionName,
      startTime: latestConfig.startTime,
      endTime: latestConfig.endTime,
      sessionDescription: latestConfig.sessionDescription || '',
      isActive: latestConfig.isActive,
      sortOrder: latestConfig.sortOrder || 0,
      earlyCheckinMinutes: latestConfig.earlyCheckinMinutes || 0,
      lateCheckinMinutes: latestConfig.lateCheckinMinutes || 0,
      qrCode: qrCodeContent,  // ✅ 只更新二维码内容
      wifiSsid: latestConfig.wifiSsid || '',
      requiredUserIds: requiredUserIds  // ✅ 转换后的用户ID列表
    }

    console.log('准备更新配置，只修改 qrCode 字段，其他字段保持不变')
    console.log('考勤人员数量:', requiredUserIds.length)

    // 第四步：提交更新
    const response = await request.put(`/checkin/configurations/${configId}`, updateData)
    
    if (response && response.success !== false) {
      console.log('✅ 二维码内容已成功保存到后端配置（其他配置保持不变）')
      // 更新本地配置缓存
      selectedConfig.value.qrCode = qrCodeContent
    } else {
      console.error('保存二维码到后端失败:', response?.message)
    }
  } catch (error) {
    console.error('保存二维码到后端时发生错误:', error)
    // 不阻止二维码生成流程，只记录错误
  }
}

// 重置表单
const resetForm = () => {
  selectedConfigId.value = null
  form.configId = ''
  form.configName = ''
  form.expireTime = ''
  form.extra = ''
  qrCodeUrl.value = ''
  qrContent.value = ''
  stopCountdown()
  nextRefreshTime.value = null
  // 清除持久化
  try { localStorage.removeItem(STORAGE_KEY) } catch (e) {}
}

// 下载二维码
const downloadQR = () => {
  if (!qrCodeUrl.value) return

  const link = document.createElement('a')
  link.download = `qr-config-${form.configId}-${Date.now()}.png`
  link.href = qrCodeUrl.value
  link.click()

  ElMessage.success('二维码已下载')
}

// 倒计时与自动刷新
const startCountdown = (seconds, total = seconds) => {
  stopCountdown()
  countdownSeconds.value = seconds
  nextRefreshTime.value = Date.now() + seconds * 1000
  countdownTotal.value = total
  countdownTimer = setInterval(() => {
    if (countdownSeconds.value > 0) {
      countdownSeconds.value--
    }
    if (countdownSeconds.value === 0) {
      // 到时间自动刷新二维码
      forceRefresh()
    }
  }, 1000)
  // 保存最新倒计时基准
  persistToStorage()
}

const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const formattedCountdown = computed(() => {
  const m = Math.floor(countdownSeconds.value / 60)
  const s = countdownSeconds.value % 60
  const mm = String(m).padStart(2, '0')
  const ss = String(s).padStart(2, '0')
  return `${mm}:${ss}`
})

const countdownPercent = computed(() => {
  const total = countdownTotal.value || 60
  const left = Math.max(0, Math.min(total, countdownSeconds.value))
  return Math.round((left / total) * 100)
})

const progressColor = computed(() => {
  // 颜色从绿色(#67C23A)渐变到红色(#F56C6C)；按百分比插值
  const p = 1 - (countdownPercent.value / 100)
  const lerp = (a, b) => Math.round(a + (b - a) * p)
  const g = { r: 103, g: 194, b: 58 }
  const r = { r: 245, g: 108, b: 108 }
  const rr = lerp(g.r, r.r)
  const rg = lerp(g.g, r.g)
  const rb = lerp(g.b, r.b)
  return `rgb(${rr}, ${rg}, ${rb})`
})

const forceRefresh = async () => {
  if (!form.configId) return
  await generateQR()
}

onBeforeUnmount(() => {
  stopCountdown()
})

// 持久化与恢复
const persistToStorage = () => {
  try {
    if (!form.configId || !qrCodeUrl.value) return
    const data = {
      form: { ...form },
      qrCodeUrl: qrCodeUrl.value,
      qrContent: qrContent.value,
      nextRefreshAt: nextRefreshTime.value
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  } catch (e) {}
}

const restoreFromStorage = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return
    const data = JSON.parse(raw)
    if (!data?.form?.configId || !data.qrCodeUrl) return
    // 还原表单与二维码
    form.configId = String(data.form.configId)
    form.configName = data.form.configName || ''
    form.expireTime = data.form.expireTime || ''
    form.extra = data.form.extra || ''
    // 保存上次生成时间（用于推断总时长）
    const lastGenerateTime = data.form?.generateTime
    selectedConfigId.value = Number(form.configId)
    qrCodeUrl.value = data.qrCodeUrl
    qrContent.value = data.qrContent || ''
    nextRefreshTime.value = data.nextRefreshAt || null
    // 计算剩余倒计时
    if (nextRefreshTime.value) {
      const remain = Math.max(0, Math.floor((nextRefreshTime.value - Date.now()) / 1000))
      if (remain > 0) {
        // 使用上次的总秒数（nextRefreshTime - generateTime 推断为 countdownTotal），若无法推断则沿用当前 total
        const raw = lastGenerateTime ? (data.nextRefreshAt - new Date(lastGenerateTime).getTime()) : 0
        const inferredTotal = raw > 0 ? Math.round(raw / 1000) : (countdownTotal.value || 60)
        startCountdown(remain, inferredTotal)
      } else {
        // 已过期则立即刷新
        forceRefresh()
      }
    }
  } catch (e) {}
}

// 复制内容
const copyContent = async () => {
  if (!qrContent.value) return

  try {
    await navigator.clipboard.writeText(qrContent.value)
    ElMessage.success('内容已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}
</script>

<style scoped>
.qr-generator {
  padding: 0 8px 32px;
  background: transparent;
  min-height: auto;
  position: relative;
}

.page-header {
  max-width: 1440px;
  margin: 0 auto 22px;
  padding: 28px 34px;
  text-align: left;
  background:
    radial-gradient(circle at 86% 8%, rgba(75, 211, 180, 0.16), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(236, 250, 255, 0.76));
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 24px;
  box-shadow: 0 18px 52px rgba(18, 85, 116, 0.1);
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
  font-weight: 850;
  color: #123044;
  margin: 0 0 8px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  text-shadow: none;
  letter-spacing: 0;
}

.title-icon {
  font-size: 36px;
  color: #12aee7;
}

.page-subtitle {
  font-size: 16px;
  color: #496579;
  margin: 0;
  font-weight: 650;
  letter-spacing: 0;
}

.generator-content {
  position: relative;
  z-index: 1;
  max-width: 1440px;
  margin: 0 auto;
}

.generator-grid {
  display: grid;
  grid-template-columns: minmax(390px, 0.76fr) minmax(620px, 1.24fr);
  gap: 20px;
  align-items: start;
}

.generator-panel {
  min-width: 0;
}

.modern-card {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 22px;
  box-shadow: 0 22px 62px rgba(18, 85, 116, 0.11);
  overflow: hidden;
  transition: border-color 0.22s ease, box-shadow 0.22s ease;
}

.modern-card:hover {
  box-shadow: 0 26px 70px rgba(18, 85, 116, 0.13);
  border-color: rgba(24, 185, 236, 0.28);
}

.modern-card :deep(.el-card__header) {
  padding: 17px 22px;
  border-bottom: 1px solid rgba(98, 177, 210, 0.18);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(240, 251, 255, 0.72));
}

.modern-card :deep(.el-card__body) {
  padding: 22px;
}

.config-panel :deep(.el-form-item) {
  margin-bottom: 20px;
}

.config-panel :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.config-panel :deep(.el-form-item__label) {
  color: #496579;
  font-weight: 850;
}

.config-panel :deep(.el-input__wrapper),
.config-panel :deep(.el-select__wrapper),
.config-panel :deep(.el-textarea__inner) {
  min-height: 44px;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 0 0 1px rgba(98, 177, 210, 0.18) inset;
}

.config-panel :deep(.el-textarea__inner) {
  min-height: 88px !important;
  padding: 12px 14px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #123044;
  font-size: 15px;
  font-weight: 850;
}

.card-header .el-icon {
  color: #0876a5;
}

.preview-header {
  justify-content: space-between;
}

.preview-header .header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-header .header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.countdown-label {
  font-size: 12px;
  color: #496579;
  font-weight: 800;
}

.countdown-progress {
  width: 150px;
}

.countdown-time {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 12px;
  color: #123044;
  font-weight: 800;
}

.qr-preview {
  text-align: left;
}

.empty-state {
  display: grid;
  place-items: center;
  min-height: 420px;
  padding: 40px 20px;
  color: #7691a4;
  text-align: center;
}

.empty-state p {
  margin-top: 16px;
  font-size: 14px;
}

/* 二维码展示区域 - 左右布局 */
.qr-display {
  display: grid;
  grid-template-columns: minmax(280px, 360px) minmax(300px, 1fr);
  gap: 22px;
  align-items: start;
  min-height: auto;
}

/* 左侧区域 */
.qr-left-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.qr-image-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.qr-image-container {
  position: relative;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #18b9ec 0%, #4bd3b4 100%);
  border-radius: 22px;
  padding: 20px;
  box-shadow: 0 18px 42px rgba(18, 174, 231, 0.22), inset 0 1px 0 rgba(255, 255, 255, 0.24);
  transition: box-shadow 0.22s ease, transform 0.22s ease;
}

.qr-image-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 22px 52px rgba(18, 174, 231, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.24);
}

.qr-code-image {
  width: 100%;
  max-width: 300px;
  height: auto;
  border-radius: 14px;
  background: white;
  display: block;
}

.qr-image-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(98, 177, 210, 0.16);
  border-radius: 16px;
  color: #123044;
  font-weight: 850;
  font-size: 14px;
}

.qr-image-label .el-icon {
  font-size: 18px;
  color: #12aee7;
}

/* 操作按钮组 */
.qr-actions {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.action-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 850;
  border-radius: 15px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 0 20px;
  letter-spacing: 0;
  box-shadow: none;
}

.action-btn:hover {
  transform: translateY(-1px);
}

.action-btn:active {
  transform: translateY(0);
}

.action-btn :deep(.el-icon) {
  font-size: 18px;
  margin-right: 0;
}

/* 右侧区域 */
.qr-right-section {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 14px;
  min-width: 0;
}

/* 信息卡片 */
.info-card {
  background:
    radial-gradient(circle at 96% 0%, rgba(75, 211, 180, 0.12), transparent 36%),
    rgba(255, 255, 255, 0.78);
  border-radius: 20px;
  padding: 18px;
  border: 1px solid rgba(98, 177, 210, 0.18);
  box-shadow: 0 12px 30px rgba(18, 174, 231, 0.06);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.info-card:hover {
  border-color: rgba(24, 185, 236, 0.3);
  box-shadow: 0 16px 34px rgba(18, 174, 231, 0.1);
}

.info-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(98, 177, 210, 0.2);
}

.info-icon {
  font-size: 20px;
  color: #12aee7;
}

.info-title {
  font-size: 16px;
  font-weight: 850;
  color: #123044;
}

.info-content {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.info-card .info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 48px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.82);
  border-radius: 14px;
  border: 1px solid rgba(98, 177, 210, 0.14);
}

.info-card .info-label {
  font-size: 13px;
  color: #5b7588;
  font-weight: 800;
  flex: 0 0 auto;
}

.info-card .info-value {
  font-size: 14px;
  color: #123044;
  font-weight: 850;
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: flex-end;
  min-width: 0;
  text-align: right;
  word-break: break-word;
}

.info-card .info-value .el-icon {
  font-size: 16px;
  color: #12aee7;
}

.expiry-time {
  color: #9a640d;
}

/* 状态卡片 */
.status-card {
  background: linear-gradient(135deg, rgba(240, 253, 248, 0.94), rgba(231, 251, 244, 0.82));
  border-radius: 20px;
  padding: 18px;
  border: 1px solid rgba(33, 185, 139, 0.2);
  box-shadow: 0 12px 30px rgba(33, 185, 139, 0.08);
}

.status-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-icon {
  font-size: 34px;
  color: #21b98b;
  flex-shrink: 0;
}

.status-text {
  flex: 1;
}

.status-title {
  font-size: 15px;
  font-weight: 850;
  color: #087f63;
  margin-bottom: 4px;
}

.status-desc {
  font-size: 13px;
  color: #0f8f72;
  font-weight: 650;
}

/* 二维码内容卡片 */
.qr-content-card {
  background: rgba(255, 255, 255, 0.78);
  border-radius: 20px;
  padding: 16px;
  border: 1px solid rgba(98, 177, 210, 0.18);
  box-shadow: 0 12px 30px rgba(18, 174, 231, 0.06);
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.content-label {
  font-size: 13px;
  color: #5b7588;
  font-weight: 850;
}

.copy-btn {
  color: #0876a5 !important;
  font-weight: 850;
}

.copy-btn:hover {
  color: #12aee7 !important;
}

.content-value {
  background: rgba(240, 251, 255, 0.7);
  border-radius: 14px;
  padding: 13px 14px;
  border: 1px solid rgba(98, 177, 210, 0.16);
  max-height: 112px;
  overflow-y: auto;
}

.content-value code {
  font-family: 'Fira Code', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.55;
  color: #123044;
  word-break: break-all;
  display: block;
}

/* 滚动条样式 */
.content-value::-webkit-scrollbar {
  width: 6px;
}

.content-value::-webkit-scrollbar-track {
  background: rgba(229, 249, 255, 0.8);
  border-radius: 3px;
}

.content-value::-webkit-scrollbar-thumb {
  background: rgba(18, 174, 231, 0.3);
  border-radius: 3px;
}

.content-value::-webkit-scrollbar-thumb:hover {
  background: rgba(18, 174, 231, 0.45);
}

/* 配置选项样式 */
.config-option {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px 4px;
  min-height: 60px; /* 确保有足够的高度 */
}

.config-option-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.config-icon {
  font-size: 18px;
  color: #12aee7;
  flex-shrink: 0;
}

.config-name {
  font-size: 15px;
  font-weight: 800;
  color: #123044;
  flex: 1;
  word-break: break-word; /* 允许长文本换行 */
}

.config-option-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-left: 26px;
  gap: 8px;
  line-height: 1.8; /* 增加行高 */
  min-height: 28px; /* 确保标签有足够空间 */
}

.config-option-meta .el-tag {
  flex-shrink: 0; /* 防止标签被压缩 */
  margin: 2px 0; /* 上下间距 */
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .generator-grid {
    grid-template-columns: 1fr;
  }

  .qr-display {
    grid-template-columns: minmax(280px, 360px) minmax(280px, 1fr);
    gap: 18px;
  }

  .qr-left-section {
    width: 100%;
    max-width: none;
  }

  .qr-right-section {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .qr-generator {
    padding: 0 4px 24px;
  }

  .page-header {
    padding: 22px;
    border-radius: 20px;
  }

  .page-title {
    font-size: 26px;
  }

  .preview-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .preview-header .header-right {
    width: 100%;
  }

  .countdown-progress {
    flex: 1;
    min-width: 120px;
  }

  .qr-display {
    grid-template-columns: 1fr;
  }

  .qr-left-section {
    max-width: 100%;
  }

  .qr-image-container {
    padding: 20px;
  }

  .action-btn {
    height: 48px;
    font-size: 15px;
    padding: 0 20px;
  }

  .info-card,
  .status-card,
  .qr-content-card {
    padding: 16px;
  }

  .info-content {
    grid-template-columns: 1fr;
  }

  .info-card .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .info-card .info-label,
  .info-card .info-value {
    width: 100%;
    text-align: left;
    justify-content: flex-start;
  }
}

/* 表单按钮容器 */
.form-actions {
  display: flex;
  gap: 14px;
  width: 100%;
}

.form-actions .el-button {
  flex: 1;
  height: 46px;
  font-size: 15px;
  font-weight: 850;
  border-radius: 15px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.form-actions .el-button:hover {
  transform: translateY(-1px);
}

.form-actions :deep(.el-button--primary),
.action-btn.el-button--success,
.action-btn.el-button--primary {
  color: var(--button-primary-text);
  background: var(--button-primary-bg);
  border-color: var(--button-primary-border);
  box-shadow: 0 12px 26px rgba(24, 185, 236, 0.12);
}

.action-btn.el-button--success {
  color: var(--button-success-text);
  background: var(--button-success-bg);
  border-color: var(--button-success-border);
}

.action-btn.el-button--warning {
  color: var(--button-warning-text);
  background: var(--button-warning-bg);
  border-color: var(--button-warning-border);
}

/* 表单提示文字样式 */
.form-item-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 8px 12px;
  font-size: 12px;
  color: #5b7588;
  background: rgba(240, 251, 255, 0.68);
  border-radius: 13px;
  border: 1px solid rgba(98, 177, 210, 0.14);
  line-height: 1.5;
  font-weight: 650;
}

.form-item-tip .el-icon {
  font-size: 14px;
  color: #12aee7;
  flex-shrink: 0;
  vertical-align: middle;
}

.form-item-tip span {
  flex: 1;
}

/* 已选配置信息样式 */
.selected-config-info {
  margin-top: 12px;
  padding: 14px;
  background:
    radial-gradient(circle at 96% 0%, rgba(75, 211, 180, 0.12), transparent 36%),
    rgba(240, 251, 255, 0.76);
  border-radius: 18px;
  border: 1px solid rgba(98, 177, 210, 0.18);
  box-shadow: 0 12px 28px rgba(18, 174, 231, 0.07);
}

.selected-config-info .info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  padding: 8px 10px;
  color: #496579;
  font-size: 13px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.62);
}

.selected-config-info .info-item:not(:last-child) {
  margin-bottom: 8px;
}

.selected-config-info .info-item .el-icon {
  font-size: 16px;
  color: #7a92ff;
  flex-shrink: 0;
}

.selected-config-info .info-label {
  flex: 0 0 72px;
  font-weight: 800;
  color: #5b7588;
  min-width: 0;
}

.selected-config-info .info-value {
  min-width: 0;
  color: #123044;
  font-weight: 850;
  text-align: right;
  word-break: break-word;
}

.usage-section {
  margin-top: 20px;
}

.usage-section :deep(.el-steps) {
  padding: 4px 4px 18px;
}

.usage-section :deep(.el-step__title) {
  color: #123044;
  font-weight: 850;
}

.usage-section :deep(.el-step__description) {
  color: #5b7588;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .qr-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .action-btn {
    width: 100%;
    height: 48px;
  }
  
  .config-option-meta {
    margin-left: 0;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .form-actions .el-button {
    width: 100%;
  }
  
  .selected-config-info {
    padding: 12px;
  }
  
  .selected-config-info .info-label {
    min-width: 70px;
    font-size: 13px;
  }
}
</style>

<style>
/* 全局样式：优化下拉框面板 */
.qr-config-select {
  padding: 8px !important;
  border: 1px solid rgba(255, 255, 255, 0.7) !important;
  background: rgba(255, 255, 255, 0.96) !important;
  box-shadow: 0 24px 70px rgba(18, 85, 116, 0.18) !important;
  border-radius: 20px !important;
  margin-top: 4px !important;
  min-width: 400px !important; /* 确保下拉框有足够宽度 */
}

.qr-config-select .el-select-dropdown__item {
  padding: 8px 12px !important;
  min-height: 70px !important; /* 确保下拉项有足够的高度 */
  height: auto !important; /* 允许自动高度 */
  line-height: normal !important;
  transition: all 0.2s ease !important;
  border-radius: 14px !important;
  white-space: normal !important; /* 允许换行 */
}

.qr-config-select .el-select-dropdown__item:hover {
  background-color: rgba(229, 249, 255, 0.78) !important;
}

.qr-config-select .el-select-dropdown__item.selected {
  background-color: rgba(231, 251, 244, 0.82) !important;
  color: #087f63 !important;
  font-weight: 850 !important;
}

/* 确保标签完整显示 */
.qr-config-select .el-tag {
  white-space: nowrap !important;
  max-width: none !important;
}

/* 响应式：移动端下拉框宽度 */
@media (max-width: 768px) {
  .qr-config-select {
    min-width: calc(100vw - 40px) !important;
    max-width: calc(100vw - 40px) !important;
  }
  
  .qr-config-select .el-select-dropdown__item {
    padding: 10px 12px !important;
  }
}
</style>
