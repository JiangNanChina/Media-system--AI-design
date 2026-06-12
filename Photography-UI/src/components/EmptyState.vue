<template>
  <div class="empty-state" :class="size">
    <div class="empty-illustration">
      <!-- 无数据插图 -->
      <div v-if="type === 'no-data'" class="illustration no-data-illustration">
        <svg viewBox="0 0 200 200" class="empty-svg">
          <defs>
            <linearGradient id="gradient1" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#e3f2fd;stop-opacity:1" />
              <stop offset="100%" style="stop-color:#bbdefb;stop-opacity:1" />
            </linearGradient>
          </defs>
          <!-- 文件夹 -->
          <rect x="60" y="80" width="80" height="60" rx="4" fill="url(#gradient1)" stroke="#90caf9" stroke-width="2"/>
          <rect x="60" y="75" width="20" height="8" rx="2" fill="#90caf9"/>
          <!-- 搜索图标 -->
          <circle cx="120" cy="50" r="15" fill="none" stroke="#90caf9" stroke-width="3"/>
          <line x1="131" y1="61" x2="145" y2="75" stroke="#90caf9" stroke-width="3" stroke-linecap="round"/>
        </svg>
      </div>
      
      <!-- 搜索无结果插图 -->
      <div v-else-if="type === 'no-search'" class="illustration no-search-illustration">
        <svg viewBox="0 0 200 200" class="empty-svg">
          <defs>
            <linearGradient id="gradient2" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#fff3e0;stop-opacity:1" />
              <stop offset="100%" style="stop-color:#ffcc02;stop-opacity:0.3" />
            </linearGradient>
          </defs>
          <!-- 放大镜 -->
          <circle cx="80" cy="80" r="30" fill="url(#gradient2)" stroke="#ff9800" stroke-width="3"/>
          <circle cx="80" cy="80" r="20" fill="none" stroke="#ff9800" stroke-width="2"/>
          <line x1="102" y1="102" x2="130" y2="130" stroke="#ff9800" stroke-width="4" stroke-linecap="round"/>
          <!-- 问号 -->
          <text x="80" y="88" text-anchor="middle" font-size="20" font-weight="bold" fill="#ff9800">?</text>
        </svg>
      </div>
      
      <!-- 网络错误插图 -->
      <div v-else-if="type === 'network-error'" class="illustration network-error-illustration">
        <svg viewBox="0 0 200 200" class="empty-svg">
          <defs>
            <linearGradient id="gradient3" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#ffebee;stop-opacity:1" />
              <stop offset="100%" style="stop-color:#ffcdd2;stop-opacity:1" />
            </linearGradient>
          </defs>
          <!-- WiFi图标 -->
          <path d="M100 140 L85 155 Q100 170 115 155 Z" fill="#f44336"/>
          <path d="M100 125 L70 155 Q100 185 130 155 Z" fill="none" stroke="#f44336" stroke-width="3"/>
          <path d="M100 110 L55 155 Q100 200 145 155 Z" fill="none" stroke="#f44336" stroke-width="3"/>
          <!-- X标记 -->
          <line x1="140" y1="60" x2="160" y2="80" stroke="#f44336" stroke-width="4" stroke-linecap="round"/>
          <line x1="160" y1="60" x2="140" y2="80" stroke="#f44336" stroke-width="4" stroke-linecap="round"/>
        </svg>
      </div>
      
      <!-- 权限不足插图 -->
      <div v-else-if="type === 'no-permission'" class="illustration no-permission-illustration">
        <svg viewBox="0 0 200 200" class="empty-svg">
          <defs>
            <linearGradient id="gradient4" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#fff8e1;stop-opacity:1" />
              <stop offset="100%" style="stop-color:#ffecb3;stop-opacity:1" />
            </linearGradient>
          </defs>
          <!-- 锁图标 -->
          <rect x="75" y="90" width="50" height="40" rx="4" fill="url(#gradient4)" stroke="#ff9800" stroke-width="2"/>
          <path d="M85 90 L85 75 Q85 60 100 60 Q115 60 115 75 L115 90" fill="none" stroke="#ff9800" stroke-width="3"/>
          <circle cx="100" cy="110" r="3" fill="#ff9800"/>
        </svg>
      </div>
      
      <!-- 自定义图标 -->
      <div v-else-if="type === 'custom'" class="illustration custom-illustration">
        <slot name="illustration">
          <el-icon :size="iconSize" :color="iconColor">
            <component :is="icon" />
          </el-icon>
        </slot>
      </div>
    </div>
    
    <!-- 标题和描述 -->
    <div class="empty-content">
      <h3 v-if="title" class="empty-title">{{ title }}</h3>
      <p v-if="description" class="empty-description">{{ description }}</p>
    </div>
    
    <!-- 操作按钮 -->
    <div v-if="$slots.action || action" class="empty-actions">
      <slot name="action">
        <el-button v-if="action" type="primary" @click="$emit('action')">
          {{ action }}
        </el-button>
      </slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'no-data',
    validator: (value) => ['no-data', 'no-search', 'network-error', 'no-permission', 'custom'].includes(value)
  },
  title: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  },
  action: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  icon: {
    type: [String, Object],
    default: 'Box'
  },
  iconSize: {
    type: Number,
    default: 64
  },
  iconColor: {
    type: String,
    default: '#c0c4cc'
  }
})

defineEmits(['action'])

// 默认内容
const defaultContent = computed(() => {
  const contents = {
    'no-data': {
      title: '暂无数据',
      description: '当前没有相关数据，请稍后再试或刷新页面'
    },
    'no-search': {
      title: '无搜索结果',
      description: '没有找到匹配的结果，请尝试其他搜索条件'
    },
    'network-error': {
      title: '网络连接失败',
      description: '请检查您的网络连接，然后重试'
    },
    'no-permission': {
      title: '权限不足',
      description: '您没有权限访问此内容，请联系管理员'
    },
    'custom': {
      title: '空状态',
      description: '这里暂时没有内容'
    }
  }
  
  return contents[props.type] || contents['no-data']
})

// 计算标题和描述
const computedTitle = computed(() => props.title || defaultContent.value.title)
const computedDescription = computed(() => props.description || defaultContent.value.description)
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  color: #606266;
}

.empty-state.small {
  padding: 20px 16px;
}

.empty-state.large {
  padding: 60px 20px;
}

.empty-illustration {
  margin-bottom: 20px;
  opacity: 0.8;
}

.empty-state.small .empty-illustration {
  margin-bottom: 16px;
}

.empty-state.large .empty-illustration {
  margin-bottom: 24px;
}

.illustration {
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-svg {
  width: 120px;
  height: 120px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
}

.empty-state.small .empty-svg {
  width: 80px;
  height: 80px;
}

.empty-state.large .empty-svg {
  width: 160px;
  height: 160px;
}

.custom-illustration {
  opacity: 0.6;
}

.empty-content {
  margin-bottom: 20px;
}

.empty-state.small .empty-content {
  margin-bottom: 16px;
}

.empty-state.large .empty-content {
  margin-bottom: 24px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.empty-state.small .empty-title {
  font-size: 16px;
  margin-bottom: 6px;
}

.empty-state.large .empty-title {
  font-size: 20px;
  margin-bottom: 12px;
}

.empty-description {
  font-size: 14px;
  color: #909399;
  margin: 0;
  line-height: 1.6;
  max-width: 400px;
}

.empty-state.small .empty-description {
  font-size: 13px;
  max-width: 300px;
}

.empty-state.large .empty-description {
  font-size: 16px;
  max-width: 500px;
}

.empty-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

/* 动画效果 */
.empty-illustration {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.empty-svg {
  animation: fade-in 0.6s ease-out;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 0.8;
    transform: scale(1);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .empty-state {
    padding: 30px 16px;
  }
  
  .empty-state.large {
    padding: 40px 16px;
  }
  
  .empty-svg {
    width: 100px;
    height: 100px;
  }
  
  .empty-state.large .empty-svg {
    width: 120px;
    height: 120px;
  }
  
  .empty-title {
    font-size: 16px;
  }
  
  .empty-state.large .empty-title {
    font-size: 18px;
  }
  
  .empty-description {
    font-size: 13px;
  }
  
  .empty-state.large .empty-description {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .empty-state {
    padding: 20px 12px;
  }
  
  .empty-svg {
    width: 80px;
    height: 80px;
  }
  
  .empty-description {
    max-width: 280px;
  }
}
</style>
