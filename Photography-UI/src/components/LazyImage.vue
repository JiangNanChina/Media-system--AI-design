<template>
  <div class="lazy-image-container" :style="{ width: width, height: height }">
    <transition name="fade" mode="out-in">
      <!-- 加载状态 -->
      <div v-if="loading" key="loading" class="loading-placeholder">
        <el-skeleton-item variant="image" :style="{ width: '100%', height: '100%' }" />
      </div>
      
      <!-- 加载失败状态 -->
      <div v-else-if="error" key="error" class="error-placeholder">
        <el-icon size="24" color="#ccc">
          <Picture />
        </el-icon>
        <span class="error-text">加载失败</span>
      </div>
      
      <!-- 实际图片 -->
      <el-image
        v-else
        key="image"
        :src="currentSrc"
        :preview-src-list="previewSrcList"
        :fit="fit"
        :loading="lazy ? 'lazy' : 'eager'"
        :placeholder="placeholder"
        :style="{ width: '100%', height: '100%' }"
        @load="handleLoad"
        @error="handleError"
        v-bind="$attrs"
      />
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { Picture } from '@element-plus/icons-vue'

const props = defineProps({
  // 图片源，支持字符串或对象形式
  src: {
    type: [String, Object],
    required: true
  },
  // 缩略图URL（用于快速显示）
  thumbnailSrc: {
    type: String,
    default: ''
  },
  // 预览图片列表
  previewSrcList: {
    type: Array,
    default: () => []
  },
  // 图片适应方式
  fit: {
    type: String,
    default: 'cover'
  },
  // 宽度
  width: {
    type: String,
    default: '100%'
  },
  // 高度  
  height: {
    type: String,
    default: '100%'
  },
  // 是否启用懒加载
  lazy: {
    type: Boolean,
    default: true
  },
  // 占位图
  placeholder: {
    type: String,
    default: ''
  },
  // 可见区域偏移（用于提前加载）
  rootMargin: {
    type: String,
    default: '50px'
  }
})

const loading = ref(true)
const error = ref(false)
const isVisible = ref(!props.lazy) // 如果不启用懒加载，直接设为可见
const imageRef = ref(null)
const observer = ref(null)

// 当前显示的图片源
const currentSrc = computed(() => {
  if (typeof props.src === 'string') {
    return props.src
  }
  
  // 如果src是对象，支持不同分辨率
  const { thumbnail, small, medium, large, original } = props.src
  
  // 根据可见状态决定加载哪个版本
  if (!isVisible.value && thumbnail) {
    return thumbnail
  }
  
  // 可见时加载更高质量的版本
  return large || medium || small || original || thumbnail || ''
})

// 处理图片加载成功
const handleLoad = () => {
  loading.value = false
  error.value = false
}

// 处理图片加载失败
const handleError = () => {
  loading.value = false
  error.value = true
}

// 创建 Intersection Observer
const createObserver = () => {
  if (!window.IntersectionObserver || !props.lazy) {
    isVisible.value = true
    return
  }

  observer.value = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          isVisible.value = true
          // 开始加载后就不再需要观察
          observer.value?.disconnect()
        }
      })
    },
    {
      rootMargin: props.rootMargin,
      threshold: 0.1
    }
  )
}

// 开始观察
const startObserving = () => {
  if (observer.value && imageRef.value) {
    observer.value.observe(imageRef.value.$el || imageRef.value)
  }
}

// 监听src变化，重新加载图片
watch(() => props.src, () => {
  if (isVisible.value) {
    loading.value = true
    error.value = false
  }
}, { deep: true })

onMounted(() => {
  createObserver()
  startObserving()
})

onUnmounted(() => {
  observer.value?.disconnect()
})
</script>

<style scoped>
.lazy-image-container {
  position: relative;
  overflow: hidden;
  background-color: #f5f7fa;
}

.loading-placeholder,
.error-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
}

.error-placeholder {
  color: #909399;
}

.error-text {
  margin-top: 8px;
  font-size: 12px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 骨架屏样式优化 */
:deep(.el-skeleton__image) {
  border-radius: 4px;
}
</style>
