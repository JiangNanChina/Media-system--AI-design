<template>
  <div class="checkin-dialog">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      :label-width="isMobile ? 'auto' : '88px'"
      :label-position="isMobile ? 'top' : 'left'"
      class="checkin-form"
    >
      <!-- 选择打卡配置 -->
      <el-form-item label="打卡配置" prop="configurationId" class="config-item">
        <div class="config-select-wrapper">
          <el-select
            v-model="form.configurationId"
            placeholder="请选择打卡配置"
            style="width: 100%"
            size="default"
            @change="handleConfigurationChange"
          >
            <el-option
              v-for="config in availableSessions"
              :key="config.id"
              :label="`${config.name} - ${config.locationName} (${formatTimeRange(config)})`"
              :value="config.id"
            />
          </el-select>
          <div v-if="form.configurationId" class="location-info">
            <el-icon class="location-icon"><Location /></el-icon>
            <span class="location-text">{{ selectedConfiguration?.locationAddress || selectedConfiguration?.locationName }}</span>
          </div>
        </div>
      </el-form-item>

      <!-- 位置信息已移除（GPS定位功能已取消） -->

      <!-- 打卡方式 -->
      <el-form-item label="方式" prop="checkinMethod" class="method-item">
        <div class="method-wrapper">
          <el-radio-group v-model="form.checkinMethod" class="method-radio-group">
            <el-radio-button value="QR_CODE" class="method-button">
              <span class="method-text">二维码</span>
            </el-radio-button>
            <el-radio-button value="MANUAL_AUDIT" class="method-button">
              <span class="method-text">管理员审核</span>
            </el-radio-button>
          </el-radio-group>
        </div>
      </el-form-item>

      <!-- 二维码扫描 -->
      <el-form-item
        v-if="form.checkinMethod === 'QR_CODE'"
        label="二维码"
        prop="qrCode"
        class="verification-item qr-verification-item"
      >
        <div class="qr-scanner">
          <el-button class="scan-button" @click="showQRScanner = true" type="primary" size="large">
            <el-icon><View /></el-icon>
            {{ form.qrCode ? '重新扫描二维码' : '扫描签到二维码' }}
          </el-button>
          <p class="scan-hint">请扫描签到点当前展示的动态二维码</p>
          <div v-if="form.qrCode" class="qr-result-card">
            <el-icon class="success-icon"><CircleCheck /></el-icon>
            <span class="success-text">二维码已扫描</span>
          </div>
        </div>
        <div class="qr-scanner-tip">
          <el-alert
            type="warning"
            :closable="false"
            show-icon
          >
            <template #title>
              <span>动态二维码安全说明</span>
            </template>
            <ul class="guide-list security-list">
              <li><strong>每45秒轮换</strong>，令牌有效期为90秒，并带有服务端签名</li>
              <li>扫码后系统会自动识别签到地点和时间段</li>
              <li>请扫描现场实时二维码，长期保存的截图无法使用</li>
              <li>扫描后请在令牌有效期内完成提交</li>
            </ul>
          </el-alert>
        </div>
      </el-form-item>

      <!-- 管理员审核方式说明 -->
      <el-form-item
        v-if="form.checkinMethod === 'MANUAL_AUDIT'"
        label="审核说明"
        class="verification-item audit-verification-item"
      >
        <el-alert
          type="info"
          :closable="false"
          show-icon
        >
          <template #title>
            <span>管理员审核签到</span>
          </template>
          <ul class="guide-list audit-guide-list">
            <li>提交签到信息后，记录将处于"待审核"状态</li>
            <li>管理员审核通过后，签到记录生效</li>
            <li>管理员审核拒绝后，记录将标记为"缺勤"</li>
            <li>此方式适用于无法满足GPS或二维码签到条件的特殊情况</li>
            <li>请在备注中说明签到原因，便于管理员审核</li>
          </ul>
        </el-alert>
      </el-form-item>

      <div class="supporting-section-heading" role="heading" aria-level="3">
        <span>补充凭证</span>
        <small>照片与备注均为选填</small>
      </div>

      <!-- 拍照 -->
      <el-form-item label="照片" class="photo-item">
        <div class="photo-upload-wrapper">
          <DragUpload
            ref="uploadRef"
            :action="`/checkin/upload-photo`"
            :headers="uploadHeaders"
            accept="image/*"
            :max-count="1"
            :max-size="5 * 1024 * 1024"
            title="拖拽或点击上传签到照片"
            subtitle="支持 jpg、png 格式，最大 5MB"
            :tips="['建议拍摄学习环境照片，作为签到凭证']"
            @success="handlePhotoUpload"
            class="photo-upload"
          />
        </div>
      </el-form-item>

      <!-- 备注 -->
      <el-form-item label="备注" class="notes-item">
        <div class="notes-wrapper">
          <el-input
            v-model="form.notes"
            type="textarea"
            :rows="6"
            placeholder="请输入备注信息（可选）"
            maxlength="500"
            show-word-limit
            class="notes-input"
          />
        </div>
      </el-form-item>
    </el-form>

    <!-- 位置验证提示已移除（GPS定位功能已取消） -->

    <!-- 操作按钮 -->
    <div class="dialog-footer">
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button
        type="primary"
        @click="handleSubmit"
        :loading="submitting"
        :disabled="!canSubmit"
      >
        确认签到
      </el-button>
    </div>

    <!-- 二维码扫描对话框 -->
    <el-dialog
      v-model="showQRScanner"
      title="扫描二维码"
      width="min(480px, calc(100vw - 32px))"
      class="qr-scanner-dialog"
      append-to-body
      align-center
      destroy-on-close
      :close-on-click-modal="false"
      center
    >
      <QRScanner
        v-if="showQRScanner"
        @success="handleQRScanSuccess"
        @error="handleQRScanError"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  Location, Refresh, View, Search, QuestionFilled, CircleCheck
} from '@element-plus/icons-vue'
import DragUpload from '@/components/DragUpload.vue'
import { generateDeviceFingerprint } from '@/utils/deviceFingerprint'
import { getAccuracyLevel, formatAccuracy, isPossiblyIndoor } from '@/utils/locationHelper'
import QRScanner from '@/components/QRScanner.vue'
import request from '@/utils/request'

const props = defineProps({
  availableSessions: {
    type: Array,
    default: () => []
  },
  nearbyLocations: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['success', 'cancel', 'configurationsLoaded'])

const userStore = useUserStore()

// 响应式屏幕尺寸检测
const windowWidth = ref(window.innerWidth)

// 计算属性：判断是否为移动设备
const isMobile = computed(() => windowWidth.value <= 768)

// 监听窗口大小变化
const handleResize = () => {
  windowWidth.value = window.innerWidth
}

// 响应式数据
const formRef = ref()
const uploadRef = ref()
const submitting = ref(false)
const locationLoading = ref(false)
const wifiLoading = ref(false)
const showQRScanner = ref(false)
const wifiValidationResult = ref(null)
// 最近一次定位精度（米）
const lastAccuracy = ref(null)

const form = reactive({
  configurationId: null, // 使用配置ID替代sessionId和locationId
  latitude: null,
  longitude: null,
  coordinates: '', // 添加coordinates字段用于验证
  address: '',
  checkinMethod: 'QR_CODE', // 默认选择二维码签到
  qrCode: '',
  wifiSsid: '',
  wifiMac: '',
  notes: '',
  deviceInfo: '',
  ipAddress: '',
  userAgent: ''
})

const locationValidation = reactive({
  show: false,
  valid: false,
  message: ''
})

// 表单验证规则
const rules = computed(() => {
  const baseRules = {
    configurationId: [
      { required: true, message: '请选择打卡配置', trigger: 'change' }
    ]
  }
  
  // GPS定位功能已取消，不再要求坐标信息
  
  // 根据签到方式添加额外验证
  if (form.checkinMethod === 'QR_CODE') {
    baseRules.qrCode = [
      { required: true, message: '请扫描二维码', trigger: 'change' }
    ]
  } else if (form.checkinMethod === 'MANUAL_AUDIT') {
    // 管理员审核方式：建议填写备注说明签到原因
    // 备注不是必填，但建议填写
  }
  
  return baseRules
})

// 计算属性
const selectedConfiguration = computed(() => {
  return props.availableSessions.find(config => config.id === form.configurationId)
})

const coordinatesDisplay = computed(() => {
  if (form.latitude && form.longitude) {
    return `${form.latitude.toFixed(6)}, ${form.longitude.toFixed(6)}`
  }
  return ''
})

const canSubmit = computed(() => {
  if (form.checkinMethod === 'QR_CODE') {
    return !!form.configurationId && !!form.qrCode && !submitting.value
  }
  if (form.checkinMethod === 'MANUAL_AUDIT') {
    // 管理员审核方式：只需要配置ID（GPS定位功能已取消）
    return !!form.configurationId && !submitting.value
  }
  // 其他方式
  return !!form.configurationId && !submitting.value
})

const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))

// 监听位置变化，验证位置
watch([() => form.configurationId, () => form.latitude, () => form.longitude], () => {
  if (form.checkinMethod !== 'QR_CODE') {
    validateLocation()
  }
})

// 监听坐标变化，同步coordinates字段
watch([() => form.latitude, () => form.longitude], () => {
  if (form.latitude && form.longitude) {
    form.coordinates = `${form.latitude.toFixed(6)}, ${form.longitude.toFixed(6)}`
  } else {
    form.coordinates = ''
  }
})

// 监听WiFi输入变化
watch(() => form.wifiSsid, (newValue) => {
  if (newValue && form.checkinMethod === 'WIFI') {
    // 延迟验证，避免用户输入时频繁验证
    setTimeout(() => {
      validateWifi()
    }, 500)
  } else {
    wifiValidationResult.value = null
  }
})

// 监听签到方式变化，重置相关状态
watch(() => form.checkinMethod, (newMethod) => {
  if (newMethod === 'WIFI') {
    wifiValidationResult.value = null
    // 如果配置中有WiFi信息，自动填充
    if (selectedConfiguration.value?.wifiSsid) {
      form.wifiSsid = selectedConfiguration.value.wifiSsid
    }
  } else if (newMethod === 'QR_CODE') {
    form.qrCode = ''
    // 二维码模式不需要也不显示位置校验
    locationValidation.show = false
    locationValidation.valid = true
    locationValidation.message = ''
  }
})

// 获取当前位置
const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    ElMessage.error('您的浏览器不支持地理位置功能')
    return
  }

  locationLoading.value = true
  
  // 优化的定位配置
  const locationOptions = {
    enableHighAccuracy: true,    // 启用高精度定位
    timeout: 15000,              // 增加超时时间
    maximumAge: 30000            // 减少缓存时间到30秒
  }
  
  navigator.geolocation.getCurrentPosition(
    (position) => {
      const accuracy = position.coords.accuracy
      console.log('🎯 打卡对话框定位精度:', accuracy, '米')
      lastAccuracy.value = Math.round(accuracy)
      
      form.latitude = position.coords.latitude
      form.longitude = position.coords.longitude
      
      // 获取地址信息
      reverseGeocode(form.latitude, form.longitude)
      
      // 根据精度提供智能提示
      const accuracyInfo = getAccuracyLevel(accuracy)
      
      if (accuracy <= 20) {
        console.log(`🎯 ${accuracyInfo.label}定位成功 (${formatAccuracy(accuracy)})`)
      } else if (accuracy > 50) {
        ElMessage.warning({
          message: `定位精度较低 (${formatAccuracy(accuracy)})，可能影响打卡准确性`,
          duration: 4000
        })
        
        if (isPossiblyIndoor(accuracy)) {
          ElMessage.info({
            message: '建议移至室外空旷地带获取更高精度',
            duration: 3000
          })
        }
      }
      
      locationLoading.value = false
    },
    (error) => {
      console.error('获取位置失败:', error)
      let errorMessage = '获取位置失败'
      
      switch(error.code) {
        case error.PERMISSION_DENIED:
          errorMessage = '定位权限被拒绝，请在浏览器设置中允许位置访问'
          break
        case error.POSITION_UNAVAILABLE:
          errorMessage = '位置服务不可用，请检查GPS设置'
          break
        case error.TIMEOUT:
          errorMessage = '定位超时，请重试'
          break
        default:
          errorMessage = '获取位置失败：' + error.message
      }
      
      ElMessage.error(errorMessage)
      locationLoading.value = false
    },
    locationOptions
  )
}

// 逆地理编码获取地址
const reverseGeocode = async (latitude, longitude) => {
  try {
    // 这里可以调用地图API获取详细地址
    // 暂时使用简单的坐标显示
    form.address = `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`
  } catch (error) {
    console.error('获取地址失败:', error)
  }
}

// 验证打卡位置（已移除GPS定位验证，仅用于信息记录）
const validateLocation = async () => {
  // 所有打卡方式不再进行位置验证
  locationValidation.show = false
  locationValidation.valid = true
  return
}

// 计算距离函数已移除（GPS定位功能已取消）

// 格式化时间范围
const formatTimeRange = (session) => {
  if (!session || !session.startTime || !session.endTime) {
    return '时间待定'
  }
  
  // 处理LocalTime格式（如：09:00:00）转换为简短格式（如：09:00）
  const formatTime = (timeStr) => {
    if (typeof timeStr === 'string') {
      return timeStr.substring(0, 5)
    }
    return timeStr
  }
  
  return `${formatTime(session.startTime)} - ${formatTime(session.endTime)}`
}

// 处理配置变化
const handleConfigurationChange = () => {
  const config = selectedConfiguration.value
  if (config) {
    // 自动设置地址信息
    if (config.locationAddress) {
      form.address = config.locationAddress
    }
    
    // 如果配置有WiFi信息，自动设置
    if (config.wifiSsid) {
      form.wifiSsid = config.wifiSsid
    }
  }
}

// 检测WiFi
const detectWifi = async () => {
  wifiLoading.value = true
  wifiValidationResult.value = null
  
  try {
    // 多种方式尝试获取网络信息
    let detectionResults = []
    let hasPresetWifi = false
    
    // 1. 检查是否有预设的WiFi名称
    if (selectedConfiguration.value?.wifiSsid) {
      form.wifiSsid = selectedConfiguration.value.wifiSsid
      hasPresetWifi = true
      detectionResults.push(`已使用预设WiFi: ${selectedConfiguration.value.wifiSsid}`)
    }
    
    // 2. 尝试使用Network Information API
    if ('connection' in navigator && navigator.connection) {
      const connection = navigator.connection
      if (connection.effectiveType) {
        detectionResults.push(`网络类型: ${connection.effectiveType}`)
      }
      if (connection.downlink) {
        detectionResults.push(`网络速度: ${connection.downlink} Mbps`)
      }
    }
    
    // 3. 检查在线状态
    if ('onLine' in navigator) {
      detectionResults.push(`连接状态: ${navigator.onLine ? '已连接' : '离线'}`)
    }
    
    // 4. 尝试获取位置精度（高精度通常表示WiFi连接）
    if ('geolocation' in navigator && navigator.onLine) {
      try {
        const position = await new Promise((resolve, reject) => {
          navigator.geolocation.getCurrentPosition(resolve, reject, {
            timeout: 2000,
            enableHighAccuracy: false
          })
        })
        const accuracy = Math.round(position.coords.accuracy)
        detectionResults.push(`位置精度: ${accuracy}m ${accuracy < 100 ? '(可能为WiFi连接)' : '(可能为移动网络)'}`)
      } catch (geoError) {
        detectionResults.push('无法获取位置信息')
      }
    }
    
    // 显示检测结果
    if (hasPresetWifi) {
      ElMessage({
        dangerouslyUseHTMLString: true,
        message: `
          <div style="text-align: left;">
            <p><strong>✅ 已自动填入预设WiFi名称</strong></p>
            <p style="color: #67C23A;">WiFi: ${selectedConfiguration.value.wifiSsid}</p>
            <p>如果当前连接的不是此WiFi，请手动修改</p>
          </div>
        `,
        type: 'success',
        duration: 4000,
        showClose: true
      })
      validateWifi()
    } else {
      ElMessage({
        dangerouslyUseHTMLString: true,
        message: `
          <div style="text-align: left;">
            <p><strong>🔍 网络检测结果：</strong></p>
            ${detectionResults.length > 0 ? detectionResults.map(result => `<p>• ${result}</p>`).join('') : '<p>• 无法获取网络信息</p>'}
            <hr style="margin: 8px 0; border: none; border-top: 1px solid #eee;">
            <p><strong>⚠️ 浏览器安全限制</strong></p>
            <p>无法直接获取WiFi名称，请手动输入</p>
            <p><strong>提示：</strong>点击"如何查看"获取详细帮助</p>
          </div>
        `,
        type: 'warning',
        duration: 6000,
        showClose: true
      })
    }
    
    // 自动聚焦到输入框（如果没有预设WiFi）
    if (!hasPresetWifi) {
      setTimeout(() => {
        const wifiInput = document.querySelector('input[placeholder*="WiFi名称"]')
        if (wifiInput) {
          wifiInput.focus()
        }
      }, 500)
    }
    
  } catch (error) {
    console.error('WiFi检测失败:', error)
    ElMessage({
      dangerouslyUseHTMLString: true,
      message: `
        <div style="text-align: left;">
          <p><strong>❌ 检测失败</strong></p>
          <p>请手动输入WiFi名称</p>
          <p>如需帮助，请点击"如何查看"按钮</p>
        </div>
      `,
      type: 'error',
      duration: 4000,
      showClose: true
    })
  } finally {
    wifiLoading.value = false
  }
}

// 验证WiFi
const validateWifi = async () => {
  if (!form.wifiSsid || !selectedConfiguration.value) {
    wifiValidationResult.value = null
    return
  }
  
  try {
    // 这里可以调用后端API验证WiFi名称是否匹配
    const expectedWifi = selectedConfiguration.value.wifiSsid
    if (expectedWifi && form.wifiSsid === expectedWifi) {
      wifiValidationResult.value = {
        success: true,
        message: 'WiFi验证通过'
      }
    } else if (expectedWifi) {
      wifiValidationResult.value = {
        success: false,
        message: `WiFi不匹配，期望: ${expectedWifi}`
      }
    } else {
      wifiValidationResult.value = {
        success: true,
        message: 'WiFi信息已记录'
      }
    }
  } catch (error) {
    console.error('WiFi验证失败:', error)
    wifiValidationResult.value = {
      success: false,
      message: 'WiFi验证失败'
    }
  }
}

// WiFi建议列表
const getWifiSuggestions = (queryString, callback) => {
  const suggestions = [
    { value: '公司WiFi', label: '公司WiFi - 常见企业网络' },
    { value: 'CompanyWiFi', label: 'CompanyWiFi - 企业网络' },
    { value: 'Office-5G', label: 'Office-5G - 办公室5G网络' },
    { value: 'Office-2.4G', label: 'Office-2.4G - 办公室2.4G网络' },
    { value: 'Guest-WiFi', label: 'Guest-WiFi - 访客网络' },
    { value: 'Meeting-Room', label: 'Meeting-Room - 会议室网络' },
    { value: 'Library-WiFi', label: 'Library-WiFi - 图书馆网络' },
    { value: 'Student-WiFi', label: 'Student-WiFi - 学生网络' },
    { value: 'Lab-Network', label: 'Lab-Network - 实验室网络' },
    { value: 'Free-WiFi', label: 'Free-WiFi - 免费网络' }
  ]
  
  // 如果有配置中的预设WiFi，优先显示
  if (selectedConfiguration.value?.wifiSsid) {
    suggestions.unshift({
      value: selectedConfiguration.value.wifiSsid,
      label: `${selectedConfiguration.value.wifiSsid} - 预设WiFi`
    })
  }
  
  const results = queryString 
    ? suggestions.filter(item => 
        item.value.toLowerCase().includes(queryString.toLowerCase()) ||
        item.label.toLowerCase().includes(queryString.toLowerCase())
      )
    : suggestions.slice(0, 6) // 默认显示前6个
    
  callback(results)
}

// 处理WiFi选择
const handleWifiSelect = (item) => {
  form.wifiSsid = item.value
  validateWifi()
}

// 显示WiFi帮助信息
const showWifiHelp = () => {
  ElMessageBox.alert(`
    <div style="text-align: left; line-height: 1.6;">
      <h3 style="margin-top: 0; color: #409EFF;">📶 如何查看WiFi名称</h3>
      
      <h4 style="color: #333; margin: 15px 0 5px 0;">🖥️ Windows系统：</h4>
      <p>1. 右键点击任务栏WiFi图标</p>
      <p>2. 选择"打开网络和Internet设置"</p>
      <p>3. 点击"WiFi" → 查看"已连接"的网络名称</p>
      <p><strong>或者：</strong>点击WiFi图标，查看带有"已连接"标记的网络</p>
      
      <h4 style="color: #333; margin: 15px 0 5px 0;">🍎 Mac系统：</h4>
      <p>1. 点击菜单栏的WiFi图标</p>
      <p>2. 查看带有"✓"标记的网络名称</p>
      <p><strong>或者：</strong>按住Option键点击WiFi图标，查看更详细信息</p>
      
      <h4 style="color: #333; margin: 15px 0 5px 0;">📱 手机系统：</h4>
      <p><strong>Android：</strong>设置 → WLAN → 查看已连接的网络</p>
      <p><strong>iPhone：</strong>设置 → WiFi → 查看带有"✓"的网络</p>
      
      <h4 style="color: #333; margin: 15px 0 5px 0;">🔧 命令行方式：</h4>
      <p><strong>Windows：</strong>命令提示符中输入 <code>netsh wlan show profile</code></p>
      <p><strong>Mac/Linux：</strong>终端中输入 <code>iwgetid -r</code> 或 <code>nmcli -t -f active,ssid dev wifi | egrep '^yes' | cut -d: -f2</code></p>
      
      <div style="background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 4px; padding: 10px; margin: 15px 0;">
        <p style="margin: 0; color: #0369a1;"><strong>💡 小贴士：</strong></p>
        <p style="margin: 5px 0 0 0; color: #0369a1;">WiFi名称区分大小写，请确保输入准确！</p>
      </div>
    </div>
  `, '帮助信息', {
    dangerouslyUseHTMLString: true,
    showCancelButton: false,
    confirmButtonText: '我知道了',
    customClass: 'wifi-help-dialog'
  })
}

// 处理二维码扫描成功
const handleQRScanSuccess = async (result) => {
  try {
    form.qrCode = result
    console.log('扫描到的二维码内容:', result)
    
    // 尝试解析二维码JSON内容
    try {
      const qrData = JSON.parse(result)
      console.log('二维码JSON数据:', qrData)
      
      // 验证二维码类型
      if (qrData.type !== 'CHECKIN') {
        ElMessage.warning('这不是一个有效的签到二维码')
        showQRScanner.value = false
        return
      }

      // 动态二维码必须由服务端签发，不能接受旧版可伪造的纯配置 JSON。
      if (qrData.version !== 2 || !qrData.signature || !qrData.issuedAt || !qrData.expiresAt) {
        ElMessage.warning('二维码版本已过期，请扫描现场最新二维码')
        showQRScanner.value = false
        return
      }
      
      // 验证配置ID是否存在
      if (!qrData.configId) {
        ElMessage.warning('二维码配置信息不完整')
        showQRScanner.value = false
        return
      }

      if (Number(qrData.expiresAt) <= Date.now()) {
        ElMessage.warning('二维码已过期，请扫描现场最新二维码')
        showQRScanner.value = false
        return
      }
      
      // 检查二维码生成时间（辅助信息，后端会做动态验证）
      if (qrData.generateTime) {
        const generateTime = new Date(qrData.generateTime)
        const now = new Date()
        const minutesDiff = Math.floor((now - generateTime) / (1000 * 60))
        const secondsDiff = Math.floor((now - generateTime) / 1000)
        
        // 前端只做友好提示，实际有效性由后端验证（与配置中存储的最新二维码对比）
        if (minutesDiff > 2) {
          console.warn(`二维码生成于 ${minutesDiff} 分钟前，可能已被新版本替代`)
          ElMessage.warning({
            message: `此二维码生成于 ${minutesDiff} 分钟前，可能已失效。如果签到失败，请扫描最新的二维码`,
            duration: 5000
          })
        } else if (secondsDiff > 0) {
          console.log(`二维码生成于 ${secondsDiff} 秒前`)
        }
      }
      
      // 自动选择对应的配置
      const configId = Number(qrData.configId)
      
      // 如果当前选择的配置与二维码不匹配，自动切换
      if (form.configurationId !== configId) {
        console.log(`自动切换配置: ${form.configurationId} -> ${configId}`)
        form.configurationId = configId
        
        // 触发配置变更，重新加载配置详情
        await handleConfigurationChange(configId)
        
        ElMessage.success({
          message: `已自动选择配置：${qrData.name || qrData.location || 'ID:' + configId}`,
          duration: 3000
        })
      } else {
        ElMessage.success('二维码扫描成功')
      }
      
      // 显示二维码包含的信息
      if (qrData.location && qrData.session) {
        console.log(`打卡地点: ${qrData.location}, 时间段: ${qrData.session}`)
      }
      
    } catch (parseError) {
      // 动态二维码必须是服务端签发的 JSON 令牌，不再接受旧的纯文本兼容格式。
      form.qrCode = ''
      ElMessage.warning('二维码格式无效，请扫描签到点当前展示的动态二维码')
      showQRScanner.value = false
      return
    }
    
    showQRScanner.value = false
    
  } catch (error) {
    console.error('处理二维码时发生错误:', error)
    ElMessage.error('二维码处理失败，请重试')
  }
}

// 处理二维码扫描错误
const handleQRScanError = (error) => {
  console.error('二维码扫描失败:', error)
  ElMessage.error('二维码扫描失败，请重试')
}

// 处理照片上传
const handlePhotoUpload = (response) => {
  form.checkinPhoto = response.url || response.data?.url
  ElMessage.success('照片上传成功')
}

// 收集设备信息
const collectDeviceInfo = async () => {
  try {
    const deviceInfo = await generateDeviceFingerprint()
    form.deviceInfo = deviceInfo
    form.userAgent = navigator.userAgent
  } catch (error) {
    console.error('生成设备指纹失败:', error)
    // 降级处理：使用简单的设备信息
    form.deviceInfo = {
      deviceFingerprint: `fallback_${navigator.platform || 'unknown'}_${screen.width}x${screen.height}_${navigator.language || 'unknown'}`,
      deviceName: 'Unknown Device',
      deviceType: 'UNKNOWN',
      osInfo: navigator.platform,
      browserInfo: navigator.userAgent,
      screenResolution: `${screen.width}x${screen.height}`,
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
      language: navigator.language
    }
    form.userAgent = navigator.userAgent
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    // 收集设备信息
    await collectDeviceInfo()
    
    const response = await request.post('/checkin/signin', form)
    
    // 根据审核状态显示不同的消息
    if (response.data?.auditStatus === 'PENDING') {
      ElMessage.success({
        message: '签到提交成功！等待管理员审核',
        duration: 5000
      })
    } else {
      ElMessage.success(response.data.message || '签到成功！')
    }
    
    emit('success', response.data)
    
  } catch (error) {
    console.error('签到失败:', error)
    if (error.response?.data?.message || error.message) {
      ElMessage.error(error.response?.data?.message || error.message)
    } else {
      ElMessage.error('签到失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

// 组件挂载时初始化（GPS定位功能已移除）
onMounted(() => {
  // GPS自动定位已移除，不再自动获取位置
  // 如果没有附近地点但有可用配置，加载所有配置
  if ((!props.nearbyLocations || props.nearbyLocations.length === 0) && 
      (!props.availableSessions || props.availableSessions.length === 0)) {
    loadAllConfigurations()
  }
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理事件监听器
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 加载所有可用配置
const loadAllConfigurations = async () => {
  try {
    const response = await request.get('/checkin/available-configurations')
    // 通过emit告知父组件更新配置列表
    if (response.data && response.data.length > 0) {
      emit('configurationsLoaded', response.data)
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  }
}
</script>

<style scoped>
:global(.qr-scanner-dialog) {
  align-self: center !important;
  flex: 0 0 auto !important;
  height: auto !important;
  min-height: 0 !important;
  max-height: calc(100vh - 32px) !important;
  margin: 0 !important;
  border-radius: var(--radius-xl) !important;
}

:global(.qr-scanner-dialog .el-dialog__header) {
  padding: var(--spacing-5) var(--spacing-6) var(--spacing-3) !important;
}

:global(.qr-scanner-dialog .el-dialog__headerbtn) {
  width: 44px;
  height: 44px;
}

:global(.qr-scanner-dialog .el-dialog__body) {
  max-height: none;
  padding: var(--spacing-5) var(--spacing-6) var(--spacing-6) !important;
  overflow-y: auto;
}

.checkin-dialog {
  --checkin-control-height: 44px;
  padding: 0;
}

.checkin-form {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(280px, 0.9fr);
  gap: var(--spacing-4) var(--spacing-5);
  width: 100%;
}

.checkin-form :deep(.el-form-item) {
  min-width: 0;
  margin-bottom: 0;
}

.checkin-form :deep(.el-form-item__label) {
  height: var(--checkin-control-height);
  padding-right: var(--spacing-3);
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 650;
  line-height: var(--checkin-control-height);
}

.checkin-form :deep(.el-form-item__content) {
  min-width: 0;
  line-height: normal;
}

.checkin-form :deep(.el-select__wrapper) {
  min-height: var(--checkin-control-height);
  border-radius: var(--radius-md);
}

.config-item .config-select-wrapper {
  width: 100%;
}

.location-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  margin-top: var(--spacing-2);
  padding: var(--spacing-2) var(--spacing-3);
  overflow: hidden;
  background: var(--color-primary-50);
  border: 1px solid var(--color-primary-100);
  border-radius: var(--radius-base);
  font-size: 13px;
}

.location-icon {
  flex: 0 0 auto;
  color: var(--color-primary-600);
  font-size: 16px;
}

.location-text {
  overflow: hidden;
  color: var(--color-text-primary);
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.method-wrapper {
  width: 100%;
}

.method-radio-group {
  display: flex;
  width: 100%;
  overflow: hidden;
  background: var(--color-background-soft);
  border: 1px solid var(--color-primary-100);
  border-radius: var(--radius-md);
}

.method-button {
  position: relative;
  flex: 1;
  margin-right: 0 !important;
}

.method-button :deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: calc(var(--checkin-control-height) - 2px);
  padding: 0 var(--spacing-3);
  border: none;
  border-radius: 0;
  background: transparent;
  color: var(--color-text-secondary);
  font-weight: 500;
  box-shadow: none;
  transition: background-color 160ms ease, color 160ms ease;
}

.method-button :deep(.el-radio-button__inner):hover {
  background: var(--color-primary-50);
  color: var(--color-primary-700);
}

.method-button :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background: var(--color-primary-500);
  color: #fff;
  box-shadow: none;
}

.method-button :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner):hover {
  background: var(--color-primary-600);
}

.method-button :deep(.el-radio-button__original-radio:focus-visible + .el-radio-button__inner) {
  outline: 3px solid rgba(21, 158, 229, 0.28);
  outline-offset: -3px;
}

.method-text {
  font-size: 14px;
  white-space: nowrap;
}

.verification-item,
.supporting-section-heading {
  grid-column: 1 / -1;
}

.verification-item :deep(.el-form-item__content) {
  display: grid;
  grid-template-columns: 1fr;
}

.qr-scanner {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  width: 100%;
}

.scan-button {
  min-width: 180px;
  height: var(--checkin-control-height);
  border-radius: var(--radius-md);
  font-weight: 650;
}

.scan-hint {
  flex: 1;
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.5;
}

.qr-result-card {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
  gap: var(--spacing-2);
  min-height: var(--checkin-control-height);
  padding: 0 var(--spacing-3);
  background: #edfbf7;
  border: 1px solid #b7eedf;
  border-radius: var(--radius-md);
}

.success-icon {
  color: #168d72;
  font-size: 18px;
}

.success-text {
  color: #116d5a;
  font-size: 14px;
  font-weight: 650;
}

.qr-scanner-tip {
  margin-top: var(--spacing-3);
}

.verification-item :deep(.el-alert) {
  align-items: flex-start;
  padding: var(--spacing-3) var(--spacing-4);
  border-radius: var(--radius-md);
}

.verification-item :deep(.el-alert__icon) {
  margin-top: 1px;
}

.verification-item :deep(.el-alert__title) {
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 650;
  line-height: 1.4;
}

.guide-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--spacing-1) var(--spacing-6);
  margin: var(--spacing-2) 0 0;
  padding-left: 18px;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.55;
}

.guide-list li::marker {
  color: currentColor;
}

.supporting-section-heading {
  display: flex;
  align-items: baseline;
  gap: var(--spacing-2);
  margin-top: var(--spacing-1);
  padding-top: var(--spacing-4);
  border-top: 1px solid rgba(79, 109, 130, 0.14);
}

.supporting-section-heading span {
  color: var(--color-text-primary);
  font-size: 15px;
  font-weight: 700;
}

.supporting-section-heading small {
  color: var(--color-text-placeholder);
  font-size: 12px;
}

.photo-upload-wrapper {
  width: 100%;
}

.photo-upload {
  width: 100%;
}

.photo-upload :deep(.el-upload-dragger) {
  height: 150px;
  border-radius: var(--radius-md) !important;
}

.photo-upload :deep(.upload-content) {
  padding: var(--spacing-3);
}

.photo-upload :deep(.upload-icon) {
  margin-bottom: var(--spacing-2);
}

.photo-upload :deep(.upload-icon .el-icon) {
  font-size: 34px !important;
}

.photo-upload :deep(.upload-text) {
  margin-bottom: var(--spacing-1);
}

.photo-upload :deep(.upload-title) {
  margin-bottom: var(--spacing-1);
  font-size: 14px;
}

.photo-upload :deep(.upload-subtitle) {
  font-size: 12px;
}

.photo-upload :deep(.upload-tips) {
  margin-top: var(--spacing-1);
}

.notes-wrapper {
  width: 100%;
}

.notes-input {
  width: 100%;
}

.notes-input :deep(.el-textarea__inner) {
  min-height: 150px !important;
  border-radius: var(--radius-md);
  line-height: 1.6;
  resize: vertical;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-3);
  margin-top: var(--spacing-6);
  padding-top: var(--spacing-4);
  border-top: 1px solid rgba(79, 109, 130, 0.14);
}

.dialog-footer .el-button {
  min-width: 112px;
  height: var(--checkin-control-height);
  margin-left: 0;
  border-radius: var(--radius-md);
  font-size: 14px;
  font-weight: 650;
}

@media (max-width: 768px) {
  .checkin-form {
    grid-template-columns: minmax(0, 1fr);
    gap: var(--spacing-4);
  }

  .verification-item,
  .supporting-section-heading {
    grid-column: auto;
  }

  .checkin-form :deep(.el-form-item__label) {
    height: auto;
    margin-bottom: var(--spacing-2);
    padding: 0;
    text-align: left !important;
    line-height: 1.4;
  }

  .location-info {
    margin-top: var(--spacing-2);
  }

  .qr-scanner {
    flex-wrap: wrap;
  }

  .scan-hint {
    flex-basis: 180px;
  }

  .guide-list {
    grid-template-columns: 1fr;
    gap: var(--spacing-1);
  }

  .supporting-section-heading {
    margin-top: 0;
  }

  .dialog-footer {
    margin-top: var(--spacing-5);
  }

  .dialog-footer .el-button {
    flex: 1;
    min-width: 0;
    height: 44px;
  }
}

@media (max-width: 480px) {
  :global(.qr-scanner-dialog) {
    width: calc(100vw - 24px) !important;
    height: auto !important;
    min-height: 0 !important;
    border-radius: var(--radius-lg) !important;
  }

  :global(.qr-scanner-dialog .el-dialog__header) {
    padding: var(--spacing-4) var(--spacing-4) var(--spacing-3) !important;
  }

  :global(.qr-scanner-dialog .el-dialog__body) {
    padding: var(--spacing-4) !important;
  }

  .checkin-form {
    gap: var(--spacing-3);
  }

  .qr-scanner {
    align-items: stretch;
    flex-direction: column;
    gap: var(--spacing-2);
  }

  .scan-button {
    width: 100%;
  }

  .scan-hint {
    flex-basis: auto;
    text-align: center;
  }

  .qr-result-card {
    justify-content: center;
  }

  .verification-item :deep(.el-alert) {
    padding: var(--spacing-3);
  }

  .photo-upload :deep(.el-upload-dragger),
  .notes-input :deep(.el-textarea__inner) {
    min-height: 140px !important;
    height: 140px;
  }

  .supporting-section-heading {
    align-items: flex-start;
    flex-direction: column;
    gap: 2px;
  }
}
</style>
