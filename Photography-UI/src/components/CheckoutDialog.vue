<template>
  <div class="checkout-dialog">
    <!-- 签到信息回顾 -->
    <div class="checkin-summary">
      <h4>签到信息</h4>
      <div class="summary-item">
        <span class="label">签到时间：</span>
        <span class="value">{{ formatDateTime(record.checkinTime) }}</span>
      </div>
      <div class="summary-item">
        <span class="label">签到地点：</span>
        <span class="value">{{ record.locationName }}</span>
      </div>
      <div class="summary-item">
        <span class="label">学习时长：</span>
        <span class="value duration">{{ calculateDuration() }}</span>
      </div>
    </div>

    <el-divider />

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
    >
      <!-- 当前位置 -->
      <el-form-item label="当前位置" prop="coordinates">
        <el-input
          v-model="coordinatesDisplay"
          readonly
          placeholder="正在获取位置信息..."
        >
          <template #prefix>
            <el-icon><Location /></el-icon>
          </template>
          <template #suffix>
            <el-button
              type="text"
              size="small"
              @click="getCurrentLocation"
              :loading="locationLoading"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </template>
        </el-input>
      </el-form-item>

      <!-- 签退照片 -->
      <el-form-item label="签退照片">
        <DragUpload
          ref="uploadRef"
          :action="`/checkin/upload-photo`"
          :headers="uploadHeaders"
          accept="image/*"
          :max-count="1"
          :max-size="5 * 1024 * 1024"
          title="拖拽或点击上传签退照片"
          subtitle="记录你的学习成果"
          :tips="['建议拍摄学习环境或成果照片', '照片将作为签退凭证']"
          @success="handlePhotoUpload"
        />
      </el-form-item>

      <!-- 学习总结 -->
      <el-form-item label="学习总结">
        <el-input
          v-model="form.notes"
          type="textarea"
          :rows="4"
          placeholder="请简单总结今天的学习情况（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <!-- 位置验证提示 -->
    <el-alert
      v-if="locationValidation.show"
      :type="locationValidation.valid ? 'success' : 'warning'"
      :title="locationValidation.message"
      :closable="false"
      style="margin-bottom: 20px;"
    />

    <!-- 操作按钮 -->
    <div class="dialog-footer">
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button
        type="primary"
        @click="handleSubmit"
        :loading="submitting"
        :disabled="!canSubmit"
      >
        确认签退
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { Location, Refresh } from '@element-plus/icons-vue'
import DragUpload from '@/components/DragUpload.vue'
import { getAccuracyLevel, formatAccuracy, isPossiblyIndoor } from '@/utils/locationHelper'
import request from '@/utils/request'

const props = defineProps({
  record: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['success', 'cancel'])

const userStore = useUserStore()

// 响应式数据
const formRef = ref()
const uploadRef = ref()
const submitting = ref(false)
const locationLoading = ref(false)

const form = reactive({
  recordId: props.record.id,
  latitude: null,
  longitude: null,
  address: '',
  notes: '',
  checkoutPhoto: '',
  ipAddress: '',
  userAgent: ''
})

const locationValidation = reactive({
  show: false,
  valid: false,
  message: ''
})

// 表单验证规则
const rules = {
  coordinates: [
    { required: true, message: '请获取位置信息', trigger: 'change' }
  ]
}

// 计算属性
const coordinatesDisplay = computed(() => {
  if (form.latitude && form.longitude) {
    return `${form.latitude.toFixed(6)}, ${form.longitude.toFixed(6)}`
  }
  return ''
})

const canSubmit = computed(() => {
  return form.latitude && 
         form.longitude &&
         !submitting.value
})

const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))

// 监听位置变化，验证位置
watch([() => form.latitude, () => form.longitude], () => {
  validateLocation()
})

// 计算学习时长
const calculateDuration = () => {
  if (!props.record.checkinTime) return '0分钟'
  
  const checkinTime = new Date(props.record.checkinTime)
  const now = new Date()
  const diffMinutes = Math.floor((now - checkinTime) / (1000 * 60))
  
  const hours = Math.floor(diffMinutes / 60)
  const minutes = diffMinutes % 60
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

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
      console.log('🎯 签退对话框定位精度:', accuracy, '米')
      
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
          message: `定位精度较低 (${formatAccuracy(accuracy)})，可能影响签退准确性`,
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
    form.address = `${latitude.toFixed(6)}, ${longitude.toFixed(6)}`
  } catch (error) {
    console.error('获取地址失败:', error)
  }
}

// 验证签退位置
const validateLocation = async () => {
  if (!form.latitude || !form.longitude) {
    locationValidation.show = false
    return
  }

  // 检查是否在同一地点签退（可选验证）
  const distance = calculateDistance(
    props.record.checkinLatitude,
    props.record.checkinLongitude,
    form.latitude,
    form.longitude
  )

  locationValidation.show = true
  
  if (distance <= 500) { // 500米内认为是合理范围
    locationValidation.valid = true
    locationValidation.message = `位置验证成功，距离签到点 ${Math.round(distance)}m`
  } else {
    locationValidation.valid = true // 签退位置可以不同
    locationValidation.message = `位置已获取，距离签到点 ${Math.round(distance)}m`
  }
}

// 计算两点间距离
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371e3 // 地球半径（米）
  const φ1 = lat1 * Math.PI/180
  const φ2 = lat2 * Math.PI/180
  const Δφ = (lat2 - lat1) * Math.PI/180
  const Δλ = (lon2 - lon1) * Math.PI/180

  const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
          Math.cos(φ1) * Math.cos(φ2) *
          Math.sin(Δλ/2) * Math.sin(Δλ/2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))

  return R * c
}

// 处理照片上传
const handlePhotoUpload = (response) => {
  form.checkoutPhoto = response.url || response.data?.url
  ElMessage.success('照片上传成功')
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString('zh-CN')
}

// 收集设备信息
const collectDeviceInfo = () => {
  form.userAgent = navigator.userAgent
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    // 收集设备信息
    collectDeviceInfo()
    
    const response = await request.post('/checkin/checkout', form)
    
    ElMessage.success(response.data.message || '签退成功！')
    emit('success', response.data)
    
  } catch (error) {
    console.error('签退失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('签退失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

// 组件挂载时获取位置
onMounted(() => {
  getCurrentLocation()
})
</script>

<style scoped>
.checkout-dialog {
  padding: 20px 0;
}

.checkin-summary {
  margin-bottom: 20px;
}

.checkin-summary h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-weight: 600;
}

.summary-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #909399;
  margin-right: 8px;
  min-width: 80px;
}

.value {
  color: #303133;
  font-weight: 500;
}

.value.duration {
  color: #67c23a;
  font-size: 16px;
  font-weight: 600;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .checkout-dialog {
    padding: 16px 0;
  }
  
  .dialog-footer {
    flex-direction: column-reverse;
    gap: 8px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
  }
}
</style>
