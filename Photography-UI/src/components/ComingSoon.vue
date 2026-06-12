<template>
  <div class="coming-soon">
    <div class="coming-soon-content">
      <div class="coming-soon-icon">
        <el-icon size="80" color="#409eff"><Tools /></el-icon>
      </div>
      <h2 class="coming-soon-title">{{ title }}</h2>
      <p class="coming-soon-subtitle">功能开发中，敬请期待...</p>
      <div class="coming-soon-features">
        <div v-for="feature in features" :key="feature" class="feature-item">
          <el-icon color="#67c23a"><Check /></el-icon>
          <span>{{ feature }}</span>
        </div>
      </div>
      <el-button type="primary" @click="$router.push('/dashboard')">
        <el-icon><HomeFilled /></el-icon>
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { Tools, Check, HomeFilled } from '@element-plus/icons-vue'

defineProps({
  title: {
    type: String,
    default: '功能开发中'
  },
  features: {
    type: Array,
    default: () => [
      '现代化的用户界面',
      '完整的功能实现',
      '优秀的用户体验',
      '响应式设计'
    ]
  }
})
</script>

<style scoped>
.coming-soon {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 40px 20px;
}

.coming-soon-content {
  text-align: center;
  max-width: 500px;
}

.coming-soon-icon {
  margin-bottom: 24px;
}

.coming-soon-title {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.coming-soon-subtitle {
  font-size: 16px;
  color: #909399;
  margin: 0 0 32px 0;
}

.coming-soon-features {
  margin-bottom: 32px;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.feature-item .el-icon {
  margin-right: 8px;
}

.feature-item:last-child {
  margin-bottom: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .coming-soon-title {
    font-size: 24px;
  }
  
  .feature-item {
    justify-content: flex-start;
  }
}
</style>
