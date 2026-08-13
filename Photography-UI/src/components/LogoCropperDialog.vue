<template>
  <el-dialog
    :model-value="modelValue"
    title="裁剪圆形 LOGO"
    width="min(720px, 94vw)"
    class="logo-cropper-dialog"
    destroy-on-close
    :close-on-click-modal="false"
    @closed="handleClosed"
    @update:model-value="handleModelValueUpdate"
  >
    <div class="logo-cropper">
      <div
        ref="stageRef"
        :class="['crop-stage', { 'is-dragging': dragging }]"
        @mousedown="startDrag"
        @mousemove="moveDrag"
        @mouseup="endDrag"
        @mouseleave="endDrag"
        @touchstart="startDrag"
        @touchmove.prevent="moveDrag"
        @touchend="endDrag"
      >
        <div class="stage-mask">
          <img
            v-if="sourceUrl"
            :src="sourceUrl"
            alt="LOGO 裁剪预览"
            draggable="false"
            :style="previewImageStyle"
          />
          <div v-else class="crop-empty">
            <el-icon><Picture /></el-icon>
            <span>请选择图片</span>
          </div>
        </div>
        <div class="crop-ring" aria-hidden="true"></div>
        <div class="crop-hint">拖拽调整主体位置</div>
      </div>

      <div class="crop-controls">
        <label class="control-row">
          <span>输出尺寸</span>
          <el-input-number
            v-model="outputSize"
            :min="minSize"
            :max="maxSize"
            :step="64"
            controls-position="right"
          />
        </label>
        <label class="control-row">
          <span>缩放</span>
          <el-slider v-model="zoom" :min="1" :max="3" :step="0.01" />
        </label>
      </div>
    </div>

    <template #footer>
      <div class="dialog-actions">
        <el-button @click="resetView">
          <el-icon><RefreshLeft /></el-icon>
          重置
        </el-button>
        <span class="size-hint">{{ outputSize }} x {{ outputSize }} PNG</span>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" :loading="processing" @click="confirmCrop">
          <el-icon><Check /></el-icon>
          确认裁剪
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { Check, Picture, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  file: {
    type: File,
    default: null
  },
  defaultSize: {
    type: Number,
    default: 512
  },
  minSize: {
    type: Number,
    default: 64
  },
  maxSize: {
    type: Number,
    default: 1024
  }
})

const emit = defineEmits(['update:modelValue', 'cropped', 'cancel'])

const sourceUrl = ref('')
const sourceImage = ref(null)
const stageRef = ref(null)
const outputSize = ref(props.defaultSize)
const zoom = ref(1)
const offsetX = ref(0)
const offsetY = ref(0)
const dragging = ref(false)
const processing = ref(false)
const dragStart = ref({ x: 0, y: 0, offsetX: 0, offsetY: 0 })

const clamp = (value, min, max) => Math.min(max, Math.max(min, value))

const boundedOutputSize = computed(() => {
  return clamp(Number(outputSize.value) || props.defaultSize, props.minSize, props.maxSize)
})

const previewImageStyle = computed(() => ({
  transform: `translate(${offsetX.value}px, ${offsetY.value}px) scale(${zoom.value})`
}))

const revokeSourceUrl = () => {
  if (sourceUrl.value) {
    URL.revokeObjectURL(sourceUrl.value)
    sourceUrl.value = ''
  }
}

const resetView = () => {
  zoom.value = 1
  offsetX.value = 0
  offsetY.value = 0
  outputSize.value = props.defaultSize
}

const loadImage = file => new Promise((resolve, reject) => {
  const url = URL.createObjectURL(file)
  const image = new Image()
  image.onload = () => resolve({ image, url })
  image.onerror = () => {
    URL.revokeObjectURL(url)
    reject(new Error('图片读取失败，请重新选择'))
  }
  image.src = url
})

watch(() => props.file, async file => {
  revokeSourceUrl()
  sourceImage.value = null
  resetView()

  if (!file) return

  try {
    const loaded = await loadImage(file)
    sourceImage.value = loaded.image
    sourceUrl.value = loaded.url
    await nextTick()
  } catch (error) {
    ElMessage.error(error.message || '图片读取失败')
  }
}, { immediate: true })

const pointFromEvent = event => event.touches?.[0] || event

const cropBounds = () => {
  const stageSize = stageRef.value?.getBoundingClientRect().width || 0
  const image = sourceImage.value
  if (!image || !stageSize) return { x: 0, y: 0 }

  const baseScale = Math.max(stageSize / image.naturalWidth, stageSize / image.naturalHeight)
  return {
    x: Math.max(0, (image.naturalWidth * baseScale * zoom.value - stageSize) / 2),
    y: Math.max(0, (image.naturalHeight * baseScale * zoom.value - stageSize) / 2)
  }
}

const clampOffsets = () => {
  const bounds = cropBounds()
  offsetX.value = clamp(offsetX.value, -bounds.x, bounds.x)
  offsetY.value = clamp(offsetY.value, -bounds.y, bounds.y)
}

watch(zoom, clampOffsets)

const startDrag = event => {
  if (!sourceImage.value) return
  const point = pointFromEvent(event)
  dragging.value = true
  dragStart.value = {
    x: point.clientX,
    y: point.clientY,
    offsetX: offsetX.value,
    offsetY: offsetY.value
  }
  event.preventDefault?.()
}

const moveDrag = event => {
  if (!dragging.value) return
  const point = pointFromEvent(event)
  offsetX.value = dragStart.value.offsetX + point.clientX - dragStart.value.x
  offsetY.value = dragStart.value.offsetY + point.clientY - dragStart.value.y
  clampOffsets()
}

const endDrag = () => {
  dragging.value = false
}

const buildCroppedFile = async () => {
  if (!sourceImage.value || !stageRef.value) {
    throw new Error('请选择需要裁剪的 LOGO 图片')
  }

  const size = boundedOutputSize.value
  const stageSize = stageRef.value.getBoundingClientRect().width
  const image = sourceImage.value
  const canvas = document.createElement('canvas')
  const context = canvas.getContext('2d')

  clampOffsets()
  canvas.width = size
  canvas.height = size
  context.clearRect(0, 0, size, size)
  context.save()
  context.beginPath()
  context.arc(size / 2, size / 2, size / 2, 0, Math.PI * 2)
  context.clip()

  const baseScale = Math.max(stageSize / image.naturalWidth, stageSize / image.naturalHeight)
  const exportScale = size / stageSize
  const drawWidth = image.naturalWidth * baseScale * zoom.value * exportScale
  const drawHeight = image.naturalHeight * baseScale * zoom.value * exportScale
  const drawX = (stageSize / 2 + offsetX.value) * exportScale - drawWidth / 2
  const drawY = (stageSize / 2 + offsetY.value) * exportScale - drawHeight / 2

  context.drawImage(image, drawX, drawY, drawWidth, drawHeight)
  context.restore()

  const blob = await new Promise((resolve, reject) => {
    canvas.toBlob(value => value ? resolve(value) : reject(new Error('LOGO 裁剪失败')), 'image/png')
  })
  const basename = (props.file?.name || 'site-logo').replace(/\.[^.]+$/, '')
  return new File([blob], `${basename}-circle-logo.png`, { type: 'image/png' })
}

const confirmCrop = async () => {
  processing.value = true
  try {
    const file = await buildCroppedFile()
    emit('cropped', file)
    emit('update:modelValue', false)
  } catch (error) {
    ElMessage.error(error.message || 'LOGO 裁剪失败')
  } finally {
    processing.value = false
  }
}

const closeDialog = () => {
  emit('cancel')
  emit('update:modelValue', false)
}

const handleModelValueUpdate = value => {
  if (!value) emit('cancel')
  emit('update:modelValue', value)
}

const handleClosed = () => {
  dragging.value = false
  sourceImage.value = null
  revokeSourceUrl()
}

onBeforeUnmount(revokeSourceUrl)
</script>

<style scoped>
.logo-cropper {
  display: grid;
  gap: 18px;
}

.crop-stage {
  position: relative;
  width: min(100%, 420px);
  aspect-ratio: 1;
  margin: 0 auto;
  overflow: hidden;
  cursor: grab;
  touch-action: none;
  user-select: none;
  border-radius: 999px;
  background:
    linear-gradient(45deg, rgba(21, 158, 229, 0.08) 25%, transparent 25%),
    linear-gradient(-45deg, rgba(21, 158, 229, 0.08) 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, rgba(21, 158, 229, 0.08) 75%),
    linear-gradient(-45deg, transparent 75%, rgba(21, 158, 229, 0.08) 75%),
    #f8fcff;
  background-position: 0 0, 0 12px, 12px -12px, -12px 0;
  background-size: 24px 24px;
}

.crop-stage.is-dragging {
  cursor: grabbing;
}

.stage-mask {
  position: absolute;
  inset: 0;
  overflow: hidden;
  border-radius: inherit;
}

.stage-mask img {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  pointer-events: none;
  transform-origin: center;
  will-change: transform;
}

.crop-ring {
  position: absolute;
  inset: 0;
  border: 2px solid rgba(14, 116, 144, 0.7);
  border-radius: 999px;
  box-shadow:
    inset 0 0 0 1px rgba(255, 255, 255, 0.72),
    0 0 0 999px rgba(7, 89, 133, 0.12);
  pointer-events: none;
}

.crop-hint {
  position: absolute;
  left: 50%;
  bottom: 16px;
  max-width: calc(100% - 32px);
  padding: 7px 11px;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  line-height: 1;
  white-space: nowrap;
  background: rgba(8, 47, 73, 0.68);
  border-radius: 999px;
  transform: translateX(-50%);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  pointer-events: none;
}

.crop-empty {
  height: 100%;
  display: grid;
  place-items: center;
  gap: 8px;
  color: #6a808b;
  font-size: 14px;
}

.crop-empty .el-icon {
  font-size: 34px;
}

.crop-controls {
  display: grid;
  gap: 14px;
}

.control-row {
  margin: 0;
  display: grid;
  grid-template-columns: 82px minmax(0, 1fr);
  align-items: center;
  gap: 14px;
  color: #315266;
  font-weight: 700;
}

.control-row :deep(.el-input-number) {
  width: 180px;
  max-width: 100%;
}

.dialog-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
}

.size-hint {
  margin-right: auto;
  color: #6a808b;
  font-size: 13px;
  font-weight: 700;
}

@media (max-width: 560px) {
  .crop-stage {
    width: min(100%, 320px);
  }

  .control-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .dialog-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .dialog-actions .el-button {
    width: 100%;
    margin-left: 0;
  }

  .size-hint {
    margin: 0;
    text-align: center;
  }
}
</style>
