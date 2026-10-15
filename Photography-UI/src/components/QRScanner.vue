<template>
  <div class="qr-scanner">
    <el-alert
      v-if="!isSecure || scanError"
      class="scanner-alert"
      :title="scanError || '当前页面不是安全上下文，浏览器会禁止摄像头。请使用 HTTPS 访问，或从相册选择二维码。'"
      type="warning"
      :closable="false"
      show-icon
    />

    <div
      class="scanner-container"
      :class="{ 'is-scanning': scanning, 'is-unavailable': !hasCamera }"
    >
      <div v-if="!hasCamera" class="scanner-empty-state">
        <span class="scanner-icon-shell is-error" aria-hidden="true">
          <el-icon><Camera /></el-icon>
        </span>
        <strong>摄像头暂不可用</strong>
        <p>{{ cameraUnavailableHint }}</p>
      </div>

      <div v-if="hasCamera && !scanning" class="scanner-ready-overlay">
        <span class="scanner-icon-shell" aria-hidden="true">
          <el-icon><Camera /></el-icon>
        </span>
        <strong>扫描现场二维码</strong>
        <p>点击下方按钮，按提示开启摄像头权限</p>
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
        <p class="scanner-tip">将二维码完整放入扫描框</p>
      </div>
      
      <canvas ref="canvasRef" style="display: none;"></canvas>
    </div>
    
    <div class="scanner-actions" :class="{ 'is-gallery-only': !canRequestCamera }">
      <el-button
        v-if="canRequestCamera"
        class="camera-action"
        :type="scanning ? 'danger' : 'primary'"
        @click="scanning ? stopScan() : startScan()"
      >
        <el-icon><VideoPause v-if="scanning" /><Camera v-else /></el-icon>
        {{ scanning ? '停止扫描' : (scanError ? '重试摄像头' : '开始扫描') }}
      </el-button>
      <el-upload
        class="gallery-upload"
        action="#"
        :auto-upload="false"
        :show-file-list="false"
        accept="image/*"
        capture="environment"
        @change="onImagePicked"
      >
        <el-button class="gallery-action">
          <el-icon><Picture /></el-icon>
          从相册识别
        </el-button>
      </el-upload>
    </div>
    <p class="scanner-privacy-note">二维码仅在当前设备识别，不会保存所选图片</p>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera, Picture, VideoPause } from '@element-plus/icons-vue'
import jsQR from 'jsqr'

const emit = defineEmits(['success', 'error'])

const videoRef = ref()
const canvasRef = ref()
const hasCamera = ref(false)
const cameraSupported = ref(false)
const scanning = ref(false)
let stream = null
let scanInterval = null
const scanError = ref('')
const isSecure = window.isSecureContext || location.protocol === 'https:' || ['localhost','127.0.0.1'].includes(location.hostname)
const canRequestCamera = computed(() => cameraSupported.value && isSecure)
const cameraUnavailableHint = computed(() => {
  if (!cameraSupported.value) return '当前浏览器不支持摄像头，可从相册选择二维码图片'
  if (!isSecure) return '当前页面无法调用摄像头，可从相册选择二维码图片'
  return '请检查浏览器或系统相机权限，也可以直接从相册识别'
})

const beginFrameScan = () => {
  if (!scanInterval && videoRef.value?.readyState >= 2) {
    scanInterval = setInterval(scanQRCode, 100)
  }
}

const startScan = async () => {
  try {
    scanError.value = ''
    if (!isSecure) {
      throw new DOMException('Camera access requires HTTPS', 'SecurityError')
    }
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
    
    // 部分浏览器在监听器注册前就已经触发 loadedmetadata，需同时覆盖两种时序。
    videoRef.value.addEventListener('loadedmetadata', beginFrameScan, { once: true })
    await videoRef.value.play()
    beginFrameScan()
    
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

  if (videoRef.value) {
    videoRef.value.srcObject = null
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
    if (scanning.value) stopScan()
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

  if (video.readyState < 2 || video.videoWidth === 0 || video.videoHeight === 0) return
  
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
    cameraSupported.value = true
    hasCamera.value = true
    if (!isSecure) {
      scanError.value = '当前页面不是 HTTPS，浏览器禁止摄像头。请使用 HTTPS 访问，或从相册选择二维码'
      ElMessage.warning('建议使用HTTPS或本地localhost访问，否则部分浏览器将禁用摄像头')
    }
  } else {
    cameraSupported.value = false
    hasCamera.value = false
    scanError.value = '当前浏览器不支持摄像头，请从相册选择二维码'
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
  gap: var(--spacing-4);
  width: 100%;
  text-align: center;
}

.scanner-alert {
  text-align: left;
}

.scanner-alert :deep(.el-alert) {
  align-items: flex-start;
}

.scanner-alert :deep(.el-alert__title) {
  line-height: 1.5;
}

.scanner-container {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: var(--color-primary-50);
  border: 1px solid var(--color-primary-100);
  border-radius: var(--radius-lg);
}

.scanner-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background: #071722;
}

.scanner-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(3, 17, 27, 0.18);
}

.scanner-frame {
  width: min(58%, 220px);
  aspect-ratio: 1 / 1;
  border: 3px solid rgba(255, 255, 255, 0.94);
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 999px rgba(4, 21, 32, 0.36);
}

.scanner-tip {
  z-index: 1;
  margin: var(--spacing-3) 0 0;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  text-shadow: 0 1px 3px rgba(3, 17, 27, 0.62);
}

.scanner-ready-overlay,
.scanner-empty-state {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-6);
  background: var(--color-background-soft);
  z-index: 2;
}

.scanner-icon-shell {
  display: grid;
  place-items: center;
  width: 56px;
  height: 56px;
  margin-bottom: var(--spacing-3);
  color: var(--color-primary-600);
  background: var(--color-primary-100);
  border-radius: var(--radius-md);
}

.scanner-icon-shell.is-error {
  color: #b73548;
  background: #fff0f3;
}

.scanner-icon-shell .el-icon {
  font-size: 28px;
}

.scanner-ready-overlay strong,
.scanner-empty-state strong {
  color: var(--color-text-primary);
  font-size: 16px;
  line-height: 1.4;
}

.scanner-ready-overlay p,
.scanner-empty-state p {
  max-width: 30ch;
  margin: var(--spacing-2) 0 0;
  color: var(--color-text-secondary);
  font-size: 13px;
  line-height: 1.55;
}

.scanner-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--spacing-3);
  width: 100%;
}

.scanner-actions.is-gallery-only {
  grid-template-columns: 1fr;
}

.scanner-actions :deep(.el-button) {
  width: 100%;
  height: 44px;
  margin: 0;
  border-radius: var(--radius-md);
  font-weight: 650;
}

.gallery-upload,
.gallery-upload :deep(.el-upload) {
  width: 100%;
}

.scanner-privacy-note {
  margin: calc(var(--spacing-2) * -1) 0 0;
  color: var(--color-text-placeholder);
  font-size: 12px;
  line-height: 1.45;
}

@media (max-width: 420px) {
  .scanner-container {
    aspect-ratio: 1 / 1;
  }

  .scanner-actions {
    grid-template-columns: 1fr;
    gap: var(--spacing-2);
  }

  .scanner-ready-overlay,
  .scanner-empty-state {
    padding: var(--spacing-4);
  }
}
</style>
