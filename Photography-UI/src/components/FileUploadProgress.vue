<template>
  <div
    class="file-upload-progress"
    :class="`is-${status}`"
    role="status"
    aria-live="polite"
    :aria-label="`${statusTitle}，${percentage}%`"
  >
    <div class="file-upload-progress__header">
      <span>{{ statusTitle }}</span>
      <strong>{{ percentage }}%</strong>
    </div>
    <el-progress
      :percentage="percentage"
      :show-text="false"
      :stroke-width="8"
      :color="progressColor"
      :status="status === 'error' ? 'exception' : undefined"
    />
    <small>{{ statusDetail }}</small>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  percentage: {
    type: Number,
    default: 0
  },
  status: {
    type: String,
    default: 'uploading',
    validator: value => ['uploading', 'processing', 'error'].includes(value)
  },
  subject: {
    type: String,
    default: '文件'
  },
  progressColor: {
    type: String,
    default: '#d94c3b'
  }
})

const statusTitle = computed(() => {
  if (props.status === 'error') return `${props.subject}上传中断`
  if (props.status === 'processing') return `${props.subject}上传完成`
  return `${props.subject}上传中`
})

const statusDetail = computed(() => {
  if (props.status === 'error') return '网络连接中断，文件仍已保留，可重新提交。'
  if (props.status === 'processing') return '文件已传输完成，正在等待服务器确认。'
  return '请保持页面开启，上传期间请勿更换文件。'
})
</script>

<style scoped>
.file-upload-progress {
  grid-column: 1 / -1;
  display: grid;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.14);
}

.file-upload-progress__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: rgba(255, 255, 255, 0.88);
  font-size: 11px;
  font-weight: 800;
}

.file-upload-progress__header strong {
  color: #fff;
  font-variant-numeric: tabular-nums;
}

.file-upload-progress small {
  color: rgba(255, 255, 255, 0.58);
  font-size: 10px;
  font-weight: 600;
  line-height: 1.5;
}

.file-upload-progress.is-error .file-upload-progress__header strong,
.file-upload-progress.is-error small {
  color: #ffb4aa;
}

.file-upload-progress :deep(.el-progress-bar__outer) {
  background: rgba(255, 255, 255, 0.16);
  border-radius: 2px;
}

.file-upload-progress :deep(.el-progress-bar__inner) {
  border-radius: 2px;
  transition: width 180ms ease-out;
}

@media (prefers-reduced-motion: reduce) {
  .file-upload-progress :deep(.el-progress-bar__inner) {
    transition-duration: 0.01ms;
  }
}
</style>
