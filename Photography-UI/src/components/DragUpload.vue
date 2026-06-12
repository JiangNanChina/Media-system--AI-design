<template>
  <div class="drag-upload">
    <el-upload
      ref="uploadRef"
      :action="action"
      :headers="headers"
      :data="data"
      :multiple="multiple"
      :accept="accept"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="handleProgress"
      :show-file-list="false"
      :auto-upload="autoUpload"
      drag
      class="upload-dragger"
    >
      <div class="upload-content">
        <!-- 上传状态：空闲 -->
        <div v-if="uploadStatus === 'idle'" class="upload-idle">
          <div class="upload-icon">
            <el-icon size="48" color="#c0c4cc">
              <UploadFilled />
            </el-icon>
          </div>
          <div class="upload-text">
            <p class="upload-title">{{ title }}</p>
            <p class="upload-subtitle">{{ subtitle }}</p>
          </div>
          <div v-if="tips" class="upload-tips">
            <p v-for="tip in tipsArray" :key="tip" class="tip-item">{{ tip }}</p>
          </div>
        </div>
        
        <!-- 上传状态：拖拽悬停 -->
        <div v-else-if="uploadStatus === 'dragover'" class="upload-dragover">
          <div class="upload-icon">
            <el-icon size="48" color="#409eff">
              <Download />
            </el-icon>
          </div>
          <div class="upload-text">
            <p class="upload-title">松开鼠标开始上传</p>
            <p class="upload-subtitle">{{ multiple ? '支持多文件上传' : '单文件上传' }}</p>
          </div>
        </div>
        
        <!-- 上传状态：上传中 -->
        <div v-else-if="uploadStatus === 'uploading'" class="upload-uploading">
          <div class="upload-progress">
            <LoadingSpinner type="circle" :progress="uploadProgress" size="large" />
          </div>
          <div class="upload-text">
            <p class="upload-title">上传中...</p>
            <p class="upload-subtitle">{{ uploadProgress }}% ({{ currentFileName }})</p>
          </div>
        </div>
        
        <!-- 上传状态：成功 -->
        <div v-else-if="uploadStatus === 'success'" class="upload-success">
          <div class="upload-icon">
            <el-icon size="48" color="#67c23a">
              <CircleCheckFilled />
            </el-icon>
          </div>
          <div class="upload-text">
            <p class="upload-title">上传成功！</p>
            <p class="upload-subtitle">{{ successMessage }}</p>
          </div>
          <div class="upload-actions">
            <el-button type="primary" size="small" @click="resetUpload">
              继续上传
            </el-button>
          </div>
        </div>
        
        <!-- 上传状态：失败 -->
        <div v-else-if="uploadStatus === 'error'" class="upload-error">
          <div class="upload-icon">
            <el-icon size="48" color="#f56c6c">
              <CircleCloseFilled />
            </el-icon>
          </div>
          <div class="upload-text">
            <p class="upload-title">上传失败</p>
            <p class="upload-subtitle">{{ errorMessage }}</p>
          </div>
          <div class="upload-actions">
            <el-button type="danger" size="small" @click="retryUpload">
              重新上传
            </el-button>
            <el-button size="small" @click="resetUpload">
              取消
            </el-button>
          </div>
        </div>
      </div>
    </el-upload>
    
    <!-- 文件列表 -->
    <div v-if="showFileList && fileList.length > 0" class="file-list">
      <div class="file-list-header">
        <span class="file-list-title">已上传文件 ({{ fileList.length }})</span>
        <el-button 
          type="text" 
          size="small" 
          @click="clearFileList"
        >
          清空列表
        </el-button>
      </div>
      
      <div class="file-items">
        <div
          v-for="(file, index) in fileList"
          :key="index"
          class="file-item"
        >
          <div class="file-info">
            <el-icon class="file-icon">
              <Document />
            </el-icon>
            <div class="file-details">
              <span class="file-name">{{ file.name }}</span>
              <span class="file-size">{{ formatFileSize(file.size) }}</span>
            </div>
          </div>
          
          <div class="file-actions">
            <el-button 
              type="text" 
              size="small" 
              @click="previewFile(file)"
              v-if="isImageFile(file)"
            >
              预览
            </el-button>
            <el-button 
              type="text" 
              size="small" 
              class="text-danger"
              @click="removeFile(index)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="图片预览"
      :width="isMobile ? '95%' : '600px'"
      center
    >
      <div class="preview-container">
        <el-image
          :src="previewImageUrl"
          fit="contain"
          style="width: 100%; max-height: 60vh;"
          :preview-src-list="[previewImageUrl]"
          :preview-teleported="true"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  UploadFilled,
  Download,
  CircleCheckFilled,
  CircleCloseFilled,
  Document
} from '@element-plus/icons-vue'
import LoadingSpinner from './LoadingSpinner.vue'

const props = defineProps({
  action: {
    type: String,
    required: true
  },
  headers: {
    type: Object,
    default: () => ({})
  },
  data: {
    type: Object,
    default: () => ({})
  },
  multiple: {
    type: Boolean,
    default: false
  },
  accept: {
    type: String,
    default: 'image/*'
  },
  maxSize: {
    type: Number,
    default: 10 * 1024 * 1024 // 10MB
  },
  maxCount: {
    type: Number,
    default: 5
  },
  title: {
    type: String,
    default: '点击或拖拽文件到此区域上传'
  },
  subtitle: {
    type: String,
    default: '支持单个或批量上传'
  },
  tips: {
    type: [String, Array],
    default: () => ['支持 jpg、png、gif 格式', '单个文件不超过 10MB']
  },
  autoUpload: {
    type: Boolean,
    default: true
  },
  showFileList: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['success', 'error', 'progress', 'change'])

// 响应式数据
const uploadRef = ref()
const uploadStatus = ref('idle') // idle, dragover, uploading, success, error
const uploadProgress = ref(0)
const currentFileName = ref('')
const successMessage = ref('')
const errorMessage = ref('')
const fileList = ref([])
const previewVisible = ref(false)
const previewImageUrl = ref('')
const isMobile = ref(false)
const pendingFiles = ref([])

// 计算属性
const tipsArray = computed(() => {
  if (Array.isArray(props.tips)) {
    return props.tips
  }
  return [props.tips]
})

// 检查是否为移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 上传前验证
const beforeUpload = (file) => {
  // 检查文件大小
  if (file.size > props.maxSize) {
    ElMessage.error(`文件大小不能超过 ${formatFileSize(props.maxSize)}`)
    return false
  }
  
  // 检查文件数量
  if (fileList.value.length >= props.maxCount) {
    ElMessage.error(`最多只能上传 ${props.maxCount} 个文件`)
    return false
  }
  
  // 检查文件类型
  if (props.accept && !isFileTypeAllowed(file)) {
    ElMessage.error('文件类型不支持')
    return false
  }
  
  currentFileName.value = file.name
  uploadStatus.value = 'uploading'
  uploadProgress.value = 0
  
  return true
}

// 检查文件类型是否允许
const isFileTypeAllowed = (file) => {
  if (!props.accept) return true
  
  const acceptTypes = props.accept.split(',').map(type => type.trim())
  const fileType = file.type
  const fileName = file.name
  
  return acceptTypes.some(acceptType => {
    if (acceptType.startsWith('.')) {
      return fileName.toLowerCase().endsWith(acceptType.toLowerCase())
    } else if (acceptType.includes('*')) {
      const baseType = acceptType.split('/')[0]
      return fileType.startsWith(baseType + '/')
    } else {
      return fileType === acceptType
    }
  })
}

// 上传成功
const handleSuccess = (response, file) => {
  uploadStatus.value = 'success'
  uploadProgress.value = 100
  successMessage.value = `${file.name} 上传成功`
  
  // 添加到文件列表
  fileList.value.push({
    name: file.name,
    size: file.size,
    url: response.url || response.data?.url,
    response
  })
  
  emit('success', response, file, fileList.value)
  emit('change', fileList.value)
  
  // 3秒后自动重置状态
  setTimeout(() => {
    if (uploadStatus.value === 'success') {
      resetUpload()
    }
  }, 3000)
}

// 上传失败
const handleError = (error, file) => {
  uploadStatus.value = 'error'
  errorMessage.value = error.message || '上传失败，请重试'
  
  // 保存失败的文件用于重试
  pendingFiles.value = [file]
  
  emit('error', error, file)
}

// 上传进度
const handleProgress = (event, file) => {
  uploadProgress.value = Math.round(event.percent)
  emit('progress', event, file)
}

// 重置上传状态
const resetUpload = () => {
  uploadStatus.value = 'idle'
  uploadProgress.value = 0
  currentFileName.value = ''
  successMessage.value = ''
  errorMessage.value = ''
  pendingFiles.value = []
  fileList.value = [] // 🔧 关键修复：重置时清空文件列表，解决"最多只能上传1个文件"问题
  emit('change', fileList.value) // 🔧 通知父组件文件列表已清空
}

// 重新上传
const retryUpload = () => {
  if (pendingFiles.value.length > 0) {
    uploadStatus.value = 'uploading'
    uploadProgress.value = 0
    // 这里可以实现重新上传逻辑
    uploadRef.value?.submit()
  }
}

// 清空文件列表
const clearFileList = () => {
  fileList.value = []
  emit('change', fileList.value)
}

// 移除文件
const removeFile = (index) => {
  fileList.value.splice(index, 1)
  emit('change', fileList.value)
}

// 预览文件
const previewFile = (file) => {
  if (file.url) {
    previewImageUrl.value = file.url
    previewVisible.value = true
  }
}

// 检查是否为图片文件
const isImageFile = (file) => {
  const imageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  return imageTypes.some(type => file.name.toLowerCase().includes(type.split('/')[1]))
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 拖拽事件处理
const handleDragOver = () => {
  if (uploadStatus.value === 'idle') {
    uploadStatus.value = 'dragover'
  }
}

const handleDragLeave = () => {
  if (uploadStatus.value === 'dragover') {
    uploadStatus.value = 'idle'
  }
}

const handleDrop = () => {
  uploadStatus.value = 'idle'
}

// 窗口大小变化处理
const handleResize = () => {
  checkMobile()
}

// 组件挂载
onMounted(() => {
  checkMobile()
  window.addEventListener('resize', handleResize)
  
  // 监听拖拽事件
  const uploadElement = uploadRef.value?.$el
  if (uploadElement) {
    uploadElement.addEventListener('dragover', handleDragOver)
    uploadElement.addEventListener('dragleave', handleDragLeave)
    uploadElement.addEventListener('drop', handleDrop)
  }
})

// 组件卸载
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  
  const uploadElement = uploadRef.value?.$el
  if (uploadElement) {
    uploadElement.removeEventListener('dragover', handleDragOver)
    uploadElement.removeEventListener('dragleave', handleDragLeave)
    uploadElement.removeEventListener('drop', handleDrop)
  }
})

// 暴露方法
defineExpose({
  submit: () => uploadRef.value?.submit(),
  clearFiles: () => uploadRef.value?.clearFiles(),
  reset: resetUpload, // 完整重置：状态 + 文件列表
  resetStatus: () => { // 仅重置状态，保留文件列表
    uploadStatus.value = 'idle'
    uploadProgress.value = 0
    currentFileName.value = ''
    successMessage.value = ''
    errorMessage.value = ''
    pendingFiles.value = []
  },
  clearFileList, // 仅清空文件列表
  getFileList: () => fileList.value
})
</script>

<style scoped>
.drag-upload {
  width: 100%;
}

.upload-dragger {
  width: 100%;
}

.upload-dragger :deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  background: #fafafa;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  overflow: hidden;
}

.upload-dragger :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.05);
}

.upload-dragger :deep(.el-upload-dragger.is-dragover) {
  border-color: #409eff;
  background: rgba(64, 158, 255, 0.1);
  transform: scale(1.02);
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 20px;
  text-align: center;
}

.upload-idle,
.upload-dragover,
.upload-uploading,
.upload-success,
.upload-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  animation: upload-state-enter 0.3s ease-out;
}

@keyframes upload-state-enter {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.upload-icon {
  margin-bottom: 16px;
  animation: icon-float 3s ease-in-out infinite;
}

@keyframes icon-float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-5px);
  }
}

.upload-dragover .upload-icon {
  animation: icon-bounce 0.6s ease-in-out;
}

@keyframes icon-bounce {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.upload-progress {
  margin-bottom: 16px;
}

.upload-text {
  margin-bottom: 16px;
}

.upload-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.upload-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
  line-height: 1.4;
}

.upload-tips {
  margin-top: 8px;
}

.tip-item {
  font-size: 12px;
  color: #c0c4cc;
  margin: 2px 0;
  line-height: 1.3;
}

.upload-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

/* 文件列表样式 */
.file-list {
  margin-top: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: white;
}

.file-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
  border-radius: 8px 8px 0 0;
}

.file-list-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.file-items {
  padding: 8px 0;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  border-radius: 4px;
  margin: 0 8px;
  transition: background-color 0.3s ease;
}

.file-item:hover {
  background: rgba(64, 158, 255, 0.05);
}

.file-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.file-icon {
  margin-right: 12px;
  color: #909399;
  font-size: 16px;
  flex-shrink: 0;
}

.file-details {
  display: flex;
  flex-direction: column;
  min-width: 0;
  flex: 1;
}

.file-name {
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.file-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.text-danger {
  color: #f56c6c;
}

/* 预览对话框 */
.preview-container {
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .upload-dragger :deep(.el-upload-dragger) {
    height: 160px;
  }
  
  .upload-content {
    padding: 16px;
  }
  
  .upload-title {
    font-size: 15px;
  }
  
  .upload-subtitle {
    font-size: 13px;
  }
  
  .tip-item {
    font-size: 11px;
  }
  
  .upload-actions {
    gap: 8px;
  }
  
  .upload-actions .el-button {
    padding: 6px 12px;
    font-size: 13px;
  }
  
  .file-list-header {
    padding: 10px 12px;
  }
  
  .file-item {
    padding: 10px 12px;
    margin: 0 4px;
  }
  
  .file-name {
    font-size: 13px;
  }
  
  .file-size {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .upload-dragger :deep(.el-upload-dragger) {
    height: 140px;
  }
  
  .upload-content {
    padding: 12px;
  }
  
  .upload-icon {
    margin-bottom: 12px;
  }
  
  .upload-icon .el-icon {
    font-size: 36px;
  }
  
  .upload-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .upload-actions .el-button {
    width: 100%;
  }
  
  .file-actions {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
