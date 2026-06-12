<template>
  <div class="checkin-dialog">
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      :label-width="isMobile ? '70px' : '80px'"
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
      <el-form-item v-if="form.checkinMethod === 'QR_CODE'" label="二维码">
        <div class="qr-scanner">
          <el-button @click="showQRScanner = true" type="primary" size="large">
            <el-icon><View /></el-icon>
            扫描签到二维码
          </el-button>
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
              <span>⚠️ 动态二维码签到 - 防作弊机制</span>
            </template>
            <ul style="margin: 0; padding-left: 20px; font-size: 13px; line-height: 1.8;">
              <li><strong>二维码每60秒自动刷新</strong>，旧二维码立即失效</li>
              <li><strong>只有最新的二维码有效</strong>，历史二维码无法使用</li>
              <li>扫码后系统会自动识别签到地点和时间段</li>
              <li><strong>严禁使用截图、拍照的二维码</strong>，必须扫描实时二维码</li>
              <li>截图作弊将在下次刷新后被系统自动拦截</li>
            </ul>
          </el-alert>
        </div>
      </el-form-item>

      <!-- 管理员审核方式说明 -->
      <el-form-item v-if="form.checkinMethod === 'MANUAL_AUDIT'" label="">
        <el-alert
          type="info"
          :closable="false"
          show-icon
        >
          <template #title>
            <span>📝 管理员审核签到方式</span>
          </template>
          <ul style="margin: 0; padding-left: 20px; font-size: 13px; line-height: 1.8;">
            <li>提交签到信息后，记录将处于"待审核"状态</li>
            <li>管理员审核通过后，签到记录生效</li>
            <li>管理员审核拒绝后，记录将标记为"缺勤"</li>
            <li>此方式适用于无法满足GPS或二维码签到条件的特殊情况</li>
            <li>请在备注中说明签到原因，便于管理员审核</li>
          </ul>
        </el-alert>
      </el-form-item>

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
            :title="isMobile ? '拖拽或点击上传照片' : '拖拽或点击上传签到照片'"
            :subtitle="isMobile ? '支持 jpg、png，最大 5MB' : '支持 jpg、png 格式，最大 5MB'"
            :tips="isMobile ? ['建议拍摄学习环境照片'] : ['建议拍摄学习环境照片', '照片将作为签到凭证']"
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
            :rows="isMobile ? 2 : 3"
            :autosize="{ minRows: 2, maxRows: 4 }"
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
      width="400px"
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
      
      // 验证配置ID是否存在
      if (!qrData.configId) {
        ElMessage.warning('二维码配置信息不完整')
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
      // 如果不是JSON格式，使用旧的简单验证
      console.log('二维码不是JSON格式，使用兼容模式')
      ElMessage.success('二维码扫描成功')
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
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
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
.checkin-dialog {
  padding: 20px 0;
}

.checkin-form {
  max-width: 100%;
}

/* 打卡配置样式 */
.config-item .config-select-wrapper {
  width: 100%;
}

.location-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ecf0f3 100%);
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  font-size: 13px;
  color: #606266;
  transition: all 0.3s ease;
}

.location-info:hover {
  background: linear-gradient(135deg, #ecf0f3 0%, #e4e7ed 100%);
  border-color: #d3d4d6;
}

.location-icon {
  color: #409eff;
  font-size: 16px;
}

.location-text {
  font-weight: 500;
  color: #303133;
}

/* 位置信息样式 */
.coordinates-wrapper {
  width: 100%;
}

.coordinates-input {
  width: 100%;
}

.refresh-btn {
  color: #409eff;
  font-weight: 500;
}

.refresh-btn:hover {
  color: #66b1ff;
}

/* 打卡方式样式 */
.method-wrapper {
  width: 100%;
}

.method-radio-group {
  width: 100%;
  display: flex;
  gap: 0;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.method-button {
  flex: 1;
  margin-right: 0 !important;
  position: relative;
}

.method-button:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 1px;
  height: 60%;
  background: rgba(255, 255, 255, 0.3);
  z-index: 1;
}

.method-button :deep(.el-radio-button__inner) {
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 0;
  background: #f5f7fa;
  color: #606266;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: none;
}

.method-button :deep(.el-radio-button__inner):hover {
  background: #e6f7ff;
  color: #409eff;
}

.method-button :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
  border-color: #409eff;
  transform: translateY(-1px);
}

.method-button :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner):hover {
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

.method-button:first-child :deep(.el-radio-button__inner) {
  border-top-left-radius: 6px;
  border-bottom-left-radius: 6px;
}

.method-button:last-child :deep(.el-radio-button__inner) {
  border-top-right-radius: 6px;
  border-bottom-right-radius: 6px;
}

.method-text {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

/* 二维码扫描样式 */
.qr-scanner {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.qr-scanner :deep(.el-button) {
  height: 48px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 8px;
}

.qr-result-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 100%);
  border: 1px solid #a7f3d0;
  border-radius: 8px;
}

.success-icon {
  font-size: 20px;
  color: #10b981;
}

.success-text {
  color: #065f46;
  font-size: 14px;
  font-weight: 600;
}

.qr-scanner-tip {
  margin-top: 12px;
}

.qr-scanner-tip :deep(.el-alert) {
  border-radius: 8px;
}

.qr-scanner-tip :deep(.el-alert__title) {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

/* WiFi信息样式 */
.wifi-info {
  width: 100%;
}

/* 照片上传样式 */
.photo-upload-wrapper {
  width: 100%;
}

.photo-upload {
  width: 100%;
  margin-top: 8px;
}

/* 备注样式 */
.notes-wrapper {
  width: 100%;
}

.notes-input {
  width: 100%;
}

.wifi-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.wifi-validation {
  margin-top: 12px;
}

/* WiFi帮助对话框样式 */
:deep(.wifi-help-dialog) {
  width: 600px;
  max-width: 90vw;
}

:deep(.wifi-help-dialog .el-message-box__content) {
  max-height: 70vh;
  overflow-y: auto;
}

:deep(.wifi-help-dialog code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 0.9em;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

/* 表单项间距优化 */
.checkin-form .el-form-item {
  margin-bottom: 24px;
}

.checkin-form .el-form-item:last-of-type {
  margin-bottom: 16px;
}

/* 表单标签样式 */
.checkin-form .el-form-item__label {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

/* 输入框统一样式 */
.checkin-form .el-input,
.checkin-form .el-select,
.checkin-form .el-textarea {
  font-size: 14px;
}

.checkin-form .el-input__inner,
.checkin-form .el-textarea__inner {
  border-radius: 6px;
  transition: all 0.3s ease;
}

.checkin-form .el-input__inner:focus,
.checkin-form .el-textarea__inner:focus {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 按钮组样式优化 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.dialog-footer .el-button {
  min-width: 100px;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
}

/* 响应式设计 */
@media (max-width: 1024px) and (min-width: 769px) {
  .method-radio-group {
    gap: 0;
  }
  
  .method-button :deep(.el-radio-button__inner) {
    height: 38px;
    font-size: 13px;
  }
  
  .method-text {
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .checkin-dialog {
    padding: 16px 0;
  }
  
  .checkin-form .el-form-item {
    margin-bottom: 20px;
  }
  
  .checkin-form .el-form-item__label {
    font-size: 13px;
    text-align: left !important;
    padding-right: 8px;
  }
  
  .location-info {
    padding: 10px 12px;
    margin-top: 10px;
    font-size: 12px;
  }
  
  .location-icon {
    font-size: 14px;
  }
  
  .method-radio-group {
    flex-direction: column;
    gap: 0;
    border-radius: 8px;
  }
  
  .method-button {
    flex: none;
    width: 100%;
    margin-bottom: 0;
  }
  
  .method-button:not(:last-child)::after {
    display: none;
  }
  
  .method-button:not(:last-child) {
    border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  }
  
  .method-button :deep(.el-radio-button__inner) {
    height: 44px;
    border-radius: 0;
    width: 100%;
    font-size: 14px;
  }
  
  .method-button:first-child :deep(.el-radio-button__inner) {
    border-top-left-radius: 8px;
    border-top-right-radius: 8px;
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
  }
  
  .method-button:last-child :deep(.el-radio-button__inner) {
    border-top-left-radius: 0;
    border-top-right-radius: 0;
    border-bottom-left-radius: 8px;
    border-bottom-right-radius: 8px;
  }
  
  .method-button:only-child :deep(.el-radio-button__inner) {
    border-radius: 8px;
  }
  
  .method-text {
    font-size: 14px;
  }
  
  .qr-scanner {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .wifi-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .wifi-actions .el-button {
    width: 100%;
  }
  
  .dialog-footer {
    flex-direction: column-reverse;
    gap: 12px;
    margin-top: 24px;
    padding-top: 20px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
    height: 44px;
    font-size: 15px;
  }
}

@media (max-width: 480px) {
  .checkin-dialog {
    padding: 12px 0;
  }
  
  .checkin-form .el-form-item {
    margin-bottom: 18px;
  }
  
  .checkin-form .el-form-item__label {
    font-size: 12px;
    margin-bottom: 6px;
  }
  
  .location-info {
    padding: 8px 10px;
    margin-top: 8px;
    font-size: 11px;
    border-radius: 6px;
  }
  
  .coordinates-input,
  .notes-input {
    font-size: 13px;
  }
  
  .method-button :deep(.el-radio-button__inner) {
    height: 46px;
    font-size: 15px;
    font-weight: 600;
  }
  
  .method-text {
    font-size: 15px;
  }
  
  .dialog-footer {
    margin-top: 20px;
    padding-top: 16px;
  }
  
  .dialog-footer .el-button {
    height: 46px;
    font-size: 14px;
    font-weight: 600;
  }
}
</style>
