<template>
  <div 
    class="modern-card"
    :class="{
      'hover-effect': hoverEffect,
      'glassmorphism': glassmorphism,
      'bordered': bordered,
      [`size-${size}`]: size
    }"
  >
    <div v-if="$slots.header || title" class="card-header">
      <slot name="header">
        <div class="header-content">
          <div class="header-title">
            <el-icon v-if="icon" class="header-icon">
              <component :is="icon" />
            </el-icon>
            <span class="title-text">{{ title }}</span>
          </div>
          <div v-if="$slots.actions" class="header-actions">
            <slot name="actions"></slot>
          </div>
        </div>
      </slot>
    </div>
    
    <div class="card-body" :class="{ 'has-header': $slots.header || title }">
      <slot></slot>
    </div>
    
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer"></slot>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineSlots } from 'vue'

defineProps({
  title: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: ''
  },
  hoverEffect: {
    type: Boolean,
    default: true
  },
  glassmorphism: {
    type: Boolean,
    default: false
  },
  bordered: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['small', 'default', 'large'].includes(value)
  }
})

defineSlots()
</script>

<style scoped>
/* 现代化卡片组件 */
.modern-card {
  background: var(--color-white);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  transition: all var(--duration-normal) var(--easing-ease);
  position: relative;
}

.modern-card.hover-effect:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-xl);
}

.modern-card.glassmorphism {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.modern-card.bordered {
  border: 2px solid var(--color-divider);
}

.modern-card.bordered:hover {
  border-color: var(--color-primary-300);
}

/* 尺寸变化 */
.modern-card.size-small {
  border-radius: var(--radius-md);
}

.modern-card.size-large {
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
}

/* 卡片头部 */
.card-header {
  padding: var(--spacing-5) var(--spacing-6);
  border-bottom: 1px solid var(--color-divider);
  background: var(--color-secondary-50);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-4);
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  flex: 1;
  min-width: 0;
}

.header-icon {
  font-size: var(--font-size-xl);
  color: var(--color-primary-500);
  flex-shrink: 0;
}

.title-text {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  line-height: var(--line-height-tight);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  flex-shrink: 0;
}

/* 卡片主体 */
.card-body {
  padding: var(--spacing-6);
}

.card-body.has-header {
  padding-top: var(--spacing-5);
}

.size-small .card-body {
  padding: var(--spacing-4);
}

.size-large .card-body {
  padding: var(--spacing-8);
}

/* 卡片底部 */
.card-footer {
  padding: var(--spacing-4) var(--spacing-6);
  border-top: 1px solid var(--color-divider);
  background: var(--color-secondary-50);
}

.size-small .card-footer {
  padding: var(--spacing-3) var(--spacing-4);
}

.size-large .card-footer {
  padding: var(--spacing-5) var(--spacing-8);
}

/* 特殊效果 */
.modern-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--gradient-primary);
  opacity: 0;
  transition: opacity var(--duration-normal) var(--easing-ease);
}

.modern-card:hover::before {
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-header {
    padding: var(--spacing-4) var(--spacing-5);
  }
  
  .header-content {
    flex-direction: column;
    align-items: stretch;
    gap: var(--spacing-3);
  }
  
  .header-title {
    justify-content: center;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .card-body {
    padding: var(--spacing-5);
  }
  
  .card-body.has-header {
    padding-top: var(--spacing-4);
  }
  
  .card-footer {
    padding: var(--spacing-3) var(--spacing-5);
  }
}

@media (max-width: 480px) {
  .modern-card {
    border-radius: var(--radius-lg);
  }
  
  .card-header {
    padding: var(--spacing-3) var(--spacing-4);
  }
  
  .title-text {
    font-size: var(--font-size-base);
  }
  
  .header-icon {
    font-size: var(--font-size-lg);
  }
  
  .card-body {
    padding: var(--spacing-4);
  }
  
  .card-footer {
    padding: var(--spacing-2) var(--spacing-4);
  }
}

/* 加载状态 */
.modern-card.loading {
  pointer-events: none;
  opacity: 0.7;
}

.modern-card.loading::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
