<template>
  <div class="qr-scanner">
    <div v-if="!hasCamera" class="no-camera">
      <el-icon size="48" color="#f56c6c"><Camera /></el-icon>
      <p>无法访问摄像头，请检查权限设置</p>
    </div>
    
    <div v-else class="scanner-container">
      <div v-if="!scanning" class="scanner-ready-overlay">
        <el-icon size="64" color="#409eff"><Camera /></el-icon>
        <p>准备开始扫描二维码</p>
        <p class="scanner-hint">请确保允许浏览器访问摄像头</p>
      </div>
      
      <video
        ref="videoRef"
        autoplay
        playsinline
        muted
        class="scanner-video"
        :style="{ visibility: scanning ? 'visible' : 'hidden' }"
      ></video>
      
      <div v-if="scanning" class="scanner-overlay">
        <div class="scanner-frame"></div>
        <p class="scanner-tip">请将二维码对准扫描框</p>
      </div>
      
      <canvas ref="canvasRef" style="display: none;"></canvas>
    </div>
    
    <div class="scanner-actions">
      <el-button @click="startScan" :disabled="scanning || !canRequestCamera" type="primary">
        {{ scanning ? '扫描中...' : '开始扫描' }}
      </el-button>
      <el-button @click="stopScan" :disabled="!scanning">
        停止扫描
      </el-button>
      <el-upload
        action="#"
        :auto-upload="false"
        :show-file-list="false"
        accept="image/*"
        capture="environment"
        @change="onImagePicked"
      >
        <el-button>从相册选择二维码</el-button>
      </el-upload>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import jsQR from 'jsqr'

const emit = defineEmits(['success', 'error'])

const videoRef = ref()
const canvasRef = ref()
const hasCamera = ref(false)
const scanning = ref(false)
let stream = null
let scanInterval = null
const scanError = ref('')
const isSecure = window.isSecureContext || location.protocol === 'https:' || ['localhost','127.0.0.1'].includes(location.hostname)
const canRequestCamera = computed(() => hasCamera.value && isSecure)

const startScan = async () => {
  try {
    scanError.value = ''
    scanning.value = true // 先设置状态，确保视频元素可见
    
    // 等待DOM更新
    await nextTick()
    
    // 再等待一个短暂的时间确保元素完全渲染
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // 检查视频元素是否存在
    if (!videoRef.value) {
      throw new Error('视频元素未准备好')
    }
    
    // 优先后置摄像头，失败则回退默认摄像头
    try {
      stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: { ideal: 'environment' } }, audio: false })
    } catch (e1) {
      stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false })
    }
    
    videoRef.value.srcObject = stream
    hasCamera.value = true
    
    // 等待视频开始播放后再开始扫描
    videoRef.value.addEventListener('loadedmetadata', () => {
      scanInterval = setInterval(scanQRCode, 100)
    })
    
  } catch (error) {
    console.error('无法访问摄像头:', error)
    hasCamera.value = false
    scanning.value = false // 发生错误时重置状态
    scanError.value = getReadableError(error)
    ElMessage.error(scanError.value)
    emit('error', error)
  }
}

const stopScan = () => {
  if (stream) {
    stream.getTracks().forEach(track => track.stop())
    stream = null
  }
  
  if (scanInterval) {
    clearInterval(scanInterval)
    scanInterval = null
  }
  
  scanning.value = false
}

// 备用：从相册选择二维码图片并识别
const onImagePicked = async (fileEvent) => {
  try {
    const file = fileEvent?.raw || fileEvent?.target?.files?.[0]
    if (!file) return
    const img = await readImage(file)
    const canvas = canvasRef.value
    const ctx = canvas.getContext('2d')
    canvas.width = img.width
    canvas.height = img.height
    ctx.drawImage(img, 0, 0)
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    const code = jsQR(imageData.data, imageData.width, imageData.height)
    if (code && code.data) {
      emit('success', code.data)
    } else {
      ElMessage.warning('未识别到二维码，请更换更清晰的图片')
    }
  } catch (err) {
    ElMessage.error('读取图片失败')
  }
}

const readImage = (file) => new Promise((resolve, reject) => {
  const reader = new FileReader()
  reader.onload = () => {
    const img = new Image()
    img.onload = () => resolve(img)
    img.onerror = reject
    img.src = reader.result
  }
  reader.onerror = reject
  reader.readAsDataURL(file)
})

const getReadableError = (error) => {
  const name = error?.name || ''
  if (!isSecure) return '当前页面非HTTPS或非localhost，浏览器可能禁止调用摄像头'
  switch (name) {
    case 'NotAllowedError':
    case 'SecurityError':
      return '摄像头权限被拒绝，请在浏览器或系统设置中开启相机权限'
    case 'NotFoundError':
    case 'OverconstrainedError':
      return '未检测到可用摄像头或不支持后置摄像头，已提供相册识别作为备用方案'
    case 'NotReadableError':
      return '摄像头被占用或不可用，请关闭其他使用相机的应用后重试'
    default:
      return '无法访问摄像头，请检查权限与兼容性'
  }
}

const scanQRCode = () => {
  if (!videoRef.value || !canvasRef.value) return
  
  const video = videoRef.value
  const canvas = canvasRef.value
  const context = canvas.getContext('2d')
  
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  
  context.drawImage(video, 0, 0, canvas.width, canvas.height)
  
  const imageData = context.getImageData(0, 0, canvas.width, canvas.height)
  const code = jsQR(imageData.data, imageData.width, imageData.height)
  
  if (code) {
    stopScan()
    emit('success', code.data)
  }
}

onMounted(() => {
  // 检查是否支持摄像头
  if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
    hasCamera.value = true
    if (!isSecure) {
      ElMessage.warning('建议使用HTTPS或本地localhost访问，否则部分浏览器将禁用摄像头')
    }
  } else {
    hasCamera.value = false
    ElMessage.error('您的浏览器不支持摄像头功能')
  }
})

onUnmounted(() => {
  stopScan()
})
</script>

<style scoped>
.qr-scanner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  text-align: center;
}

.scanner-container {
  position: relative;
  display: block;
  border-radius: 12px;
  overflow: hidden;
  width: 320px;
  max-width: 90vw;
  aspect-ratio: 1 / 1;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.scanner-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #000;
}

.scanner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.scanner-frame {
  width: 70%;
  aspect-ratio: 1 / 1;
  border: 2px solid #409eff;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
}

.scanner-tip {
  color: white;
  margin-top: 16px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.no-camera,
.scanner-ready {
  padding: 40px;
  color: #909399;
  text-align: center;
}

.scanner-ready {
  color: #409eff;
}

.scanner-ready-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.92);
  z-index: 10;
}

.scanner-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.scanner-actions {
  width: 100%;
  max-width: 320px;
  margin-top: 4px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 12px;
}

.scanner-actions :deep(.el-button) {
  width: 100%;
}

.scanner-actions :deep(.el-upload) {
  width: 100%;
}

.scanner-actions :deep(.el-upload .el-button) {
  width: 100%;
}

@media (max-width: 420px) {
  .scanner-container {
    border-radius: 10px;
  }
  .scanner-frame {
    border-radius: 10px;
  }
}
</style>
