<template>
  <div class="loading-container" :class="{ 'loading-overlay': overlay }">
    <div class="loading-spinner" :class="size">
      <!-- 默认旋转加载器 -->
      <div v-if="type === 'spinner'" class="spinner">
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
        <div class="spinner-ring"></div>
      </div>
      
      <!-- 脉冲加载器 -->
      <div v-else-if="type === 'pulse'" class="pulse">
        <div class="pulse-dot"></div>
        <div class="pulse-dot"></div>
        <div class="pulse-dot"></div>
      </div>
      
      <!-- 波浪加载器 -->
      <div v-else-if="type === 'wave'" class="wave">
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
        <div class="wave-bar"></div>
      </div>
      
      <!-- 进度环 -->
      <div v-else-if="type === 'circle'" class="circle-progress">
        <svg class="circle-svg" viewBox="0 0 50 50">
          <circle
            class="circle-bg"
            cx="25"
            cy="25"
            r="20"
            fill="none"
            stroke="#f0f0f0"
            stroke-width="2"
          ></circle>
          <circle
            class="circle-progress-bar"
            :class="{ 'loading-animation': normalizedProgress === 0 }"
            cx="25"
            cy="25"
            r="20"
            fill="none"
            stroke="#409eff"
            stroke-width="2"
            stroke-linecap="round"
            :style="{ 
              strokeDasharray: normalizedProgress === 0 
                ? '31.416 125.664' 
                : `${circumference * (normalizedProgress / 100)} ${circumference}` 
            }"
          ></circle>
        </svg>
        <div v-if="showProgress" class="circle-text">{{ normalizedProgress }}%</div>
      </div>
    </div>
    
    <!-- 加载文本 -->
    <div v-if="text" class="loading-text" :class="size">{{ text }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'spinner',
    validator: (value) => ['spinner', 'pulse', 'wave', 'circle'].includes(value)
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  text: {
    type: String,
    default: ''
  },
  overlay: {
    type: Boolean,
    default: false
  },
  progress: {
    type: Number,
    default: 0,
    validator: (value) => value >= 0 && value <= 100
  },
  showProgress: {
    type: Boolean,
    default: true
  }
})

// 🔧 修复进度条显示同步问题
// 圆的周长 = 2π × 半径，这里半径是20
const circumference = computed(() => 2 * Math.PI * 20)

// 标准化进度值，确保在0-100范围内，并保持整数显示
const normalizedProgress = computed(() => {
  const progress = Math.max(0, Math.min(100, props.progress))
  return Math.round(progress)
})
</script>

<style scoped>
/* 现代化加载容器 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-4);
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  z-index: 9999;
  animation: fadeIn var(--duration-normal) var(--easing-ease);
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn var(--duration-normal) var(--easing-ease);
}

.loading-spinner.small {
  width: 24px;
  height: 24px;
}

.loading-spinner.medium {
  width: 40px;
  height: 40px;
}

.loading-spinner.large {
  width: 60px;
  height: 60px;
}

/* 现代化旋转加载器 */
.spinner {
  position: relative;
  width: 100%;
  height: 100%;
}

.spinner-ring {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 2px solid transparent;
  border-top: 2px solid var(--color-primary-500);
  border-radius: var(--radius-full);
  animation: spin 1.2s var(--easing-ease) infinite;
}

.spinner-ring:nth-child(1) {
  animation-delay: -0.45s;
}

.spinner-ring:nth-child(2) {
  animation-delay: -0.3s;
}

.spinner-ring:nth-child(3) {
  animation-delay: -0.15s;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 现代化脉冲加载器 */
.pulse {
  display: flex;
  gap: var(--spacing-1);
  align-items: center;
  justify-content: center;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  background: var(--color-primary-500);
  animation: pulse-scale 1.4s var(--easing-ease) infinite both;
}

.pulse-dot:nth-child(1) {
  animation-delay: -0.32s;
}

.pulse-dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes pulse-scale {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 现代化波浪加载器 */
.wave {
  display: flex;
  gap: var(--spacing-1);
  align-items: end;
  justify-content: center;
  height: 100%;
}

.wave-bar {
  width: 4px;
  background: var(--color-primary-500);
  border-radius: var(--radius-sm);
  animation: wave-stretch 1.2s var(--easing-ease) infinite;
}

.wave-bar:nth-child(1) {
  animation-delay: -1.2s;
}

.wave-bar:nth-child(2) {
  animation-delay: -1.1s;
}

.wave-bar:nth-child(3) {
  animation-delay: -1.0s;
}

.wave-bar:nth-child(4) {
  animation-delay: -0.9s;
}

.wave-bar:nth-child(5) {
  animation-delay: -0.8s;
}

@keyframes wave-stretch {
  0%, 40%, 100% {
    height: 20%;
  }
  20% {
    height: 100%;
  }
}

/* 现代化进度环 */
.circle-progress {
  position: relative;
  width: 100%;
  height: 100%;
}

.circle-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.circle-progress-bar {
  transition: stroke-dasharray var(--duration-normal) var(--easing-ease);
}

/* 🔧 只在没有具体进度时显示加载旋转动画 */
.circle-progress-bar.loading-animation {
  animation: circle-rotate 2s linear infinite;
}

@keyframes circle-rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.circle-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-500);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* 现代化加载文本 */
.loading-text {
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  text-align: center;
  animation: text-fade 2s var(--easing-ease) infinite;
}

.loading-text.small {
  font-size: var(--font-size-xs);
}

.loading-text.large {
  font-size: var(--font-size-lg);
}

@keyframes text-fade {
  0%, 100% {
    opacity: 0.6;
  }
  50% {
    opacity: 1;
  }
}

/* 响应式优化 */
@media (max-width: 768px) {
  .loading-container {
    gap: var(--spacing-3);
  }
  
  .loading-spinner.medium {
    width: 32px;
    height: 32px;
  }
  
  .loading-spinner.large {
    width: 48px;
    height: 48px;
  }
  
  .loading-text {
    font-size: var(--font-size-sm);
  }
}
</style>
