<template>
  <el-dialog
    v-model="dialogVisible"
    title="系统公告"
    width="600px"
    :before-close="handleClose"
    class="announcement-dialog"
  >
    <div v-if="announcements.length > 0" class="announcements-container">
      <div
        v-for="announcement in announcements"
        :key="announcement.id"
        class="announcement-item"
      >
        <div class="announcement-header">
          <h3 class="announcement-title">{{ announcement.title }}</h3>
          <div class="announcement-meta">
            <el-tag
              :type="announcement.priority >= 8 ? 'danger' : announcement.priority >= 5 ? 'warning' : 'info'"
              size="small"
            >
              {{ getPriorityText(announcement.priority) }}
            </el-tag>
            <span class="announcement-time">
              {{ formatTime(announcement.createdAt) }}
            </span>
          </div>
        </div>
        
        <div class="announcement-content" v-html="formatContent(announcement.content)"></div>
        
        <div class="announcement-footer">
          <el-button
            type="primary"
            size="small"
            @click="markAsRead(announcement.id)"
          >
            我已阅读
          </el-button>
        </div>
      </div>
    </div>
    
    <div v-else class="no-announcements">
      <el-empty description="暂无新公告" />
    </div>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="markAllAsRead" v-if="announcements.length > 0">
          全部已读
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  announcements: {
    type: Array,
    default: () => []
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'read'])

// 响应式数据
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const readTimer = ref(null)
const readCount = ref(0)

// 关闭弹窗
const handleClose = () => {
  if (readTimer.value) {
    clearTimeout(readTimer.value)
  }
  emit('update:modelValue', false)
}

// 标记单个公告为已读
const markAsRead = (announcementId) => {
  emit('read', announcementId)
  ElMessage.success('已标记为已读')
}

// 标记所有公告为已读
const markAllAsRead = () => {
  props.announcements.forEach(announcement => {
    emit('read', announcement.id)
  })
  ElMessage.success('所有公告已标记为已读')
  handleClose()
}

// 获取优先级文本
const getPriorityText = (priority) => {
  if (priority >= 8) return '重要'
  if (priority >= 5) return '一般'
  return '普通'
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN')
}

// 格式化内容（处理换行符）
const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

// 监听弹窗显示，自动开始阅读计时
watch(dialogVisible, (visible) => {
  if (visible && props.announcements.length > 0) {
    // 3秒后才能关闭（如果有重要公告）
    const hasImportant = props.announcements.some(a => a.priority >= 8)
    if (hasImportant) {
      readCount.value = 3
      const countdown = () => {
        if (readCount.value > 0) {
          readCount.value--
          readTimer.value = setTimeout(countdown, 1000)
        }
      }
      countdown()
    }
  }
})
</script>

<style scoped>
.announcement-dialog :deep(.el-dialog__body) {
  padding: 10px 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.announcements-container {
  max-height: 50vh;
  overflow-y: auto;
}

.announcement-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  background: white;
  transition: box-shadow 0.3s ease;
}

.announcement-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.announcement-item:last-child {
  margin-bottom: 0;
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.announcement-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
  flex: 1;
  margin-right: 12px;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.announcement-time {
  font-size: 12px;
  color: #909399;
}

.announcement-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 12px;
  word-break: break-word;
}

.announcement-content :deep(br) {
  margin: 4px 0;
}

.announcement-footer {
  text-align: right;
}

.no-announcements {
  text-align: center;
  padding: 40px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .announcement-dialog :deep(.el-dialog) {
    width: 95% !important;
    margin: 0 auto;
  }
  
  .announcement-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .announcement-meta {
    align-self: flex-end;
  }
}
</style>
