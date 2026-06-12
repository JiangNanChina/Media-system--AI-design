<template>
  <div class="modern-page-header">
    <div class="header-background"></div>
    <div class="header-content">
      <div class="title-section">
        <h1 class="page-title">
          <el-icon v-if="icon" class="title-icon">
            <component :is="icon" />
          </el-icon>
          {{ title }}
        </h1>
        <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
        <div v-if="stats" class="page-stats">
          <span v-for="stat in stats" :key="stat.label" class="stat-item">
            <el-icon v-if="stat.icon"><component :is="stat.icon" /></el-icon>
            <strong>{{ stat.value }}</strong>
            <span>{{ stat.label }}</span>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'

defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: ''
  },
  stats: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
/* 现代化页面头部组件 */
.modern-page-header {
  margin-bottom: var(--spacing-8);
  text-align: center;
  padding: var(--spacing-8) 0;
  background: var(--gradient-primary);
  border-radius: var(--radius-xl);
  position: relative;
  overflow: hidden;
  animation: slideUp var(--duration-slow) var(--easing-spring);
}

.header-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
  pointer-events: none;
}

.header-content {
  position: relative;
  z-index: 1;
}

.title-section {
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: var(--font-size-4xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
  margin: 0 0 var(--spacing-3) 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-3);
  flex-wrap: wrap;
}

.title-icon {
  font-size: var(--font-size-4xl);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0.7));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.page-subtitle {
  font-size: var(--font-size-xl);
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 var(--spacing-4) 0;
  font-weight: var(--font-weight-normal);
  letter-spacing: 0.5px;
  line-height: var(--line-height-relaxed);
}

.page-stats {
  display: flex;
  justify-content: center;
  gap: var(--spacing-6);
  flex-wrap: wrap;
  margin-top: var(--spacing-4);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-1);
  padding: var(--spacing-3) var(--spacing-4);
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  min-width: 80px;
  transition: all var(--duration-normal) var(--easing-ease);
}

.stat-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.stat-item .el-icon {
  font-size: var(--font-size-lg);
  color: rgba(255, 255, 255, 0.8);
}

.stat-item strong {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
  line-height: 1;
}

.stat-item span:last-child {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.8);
  line-height: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modern-page-header {
    padding: var(--spacing-6) var(--spacing-4);
    margin-bottom: var(--spacing-6);
  }
  
  .page-title {
    font-size: var(--font-size-3xl);
    gap: var(--spacing-2);
  }
  
  .title-icon {
    font-size: var(--font-size-3xl);
  }
  
  .page-subtitle {
    font-size: var(--font-size-lg);
    margin-bottom: var(--spacing-3);
  }
  
  .page-stats {
    gap: var(--spacing-3);
  }
  
  .stat-item {
    padding: var(--spacing-2) var(--spacing-3);
    min-width: 60px;
  }
  
  .stat-item strong {
    font-size: var(--font-size-xl);
  }
  
  .stat-item span:last-child {
    font-size: var(--font-size-xs);
  }
}

@media (max-width: 480px) {
  .modern-page-header {
    padding: var(--spacing-5) var(--spacing-3);
  }
  
  .page-title {
    font-size: var(--font-size-2xl);
    flex-direction: column;
    gap: var(--spacing-2);
  }
  
  .title-icon {
    font-size: var(--font-size-2xl);
  }
  
  .page-subtitle {
    font-size: var(--font-size-base);
  }
  
  .page-stats {
    gap: var(--spacing-2);
  }
  
  .stat-item {
    padding: var(--spacing-2);
    min-width: 50px;
  }
}

/* 动画效果 */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
