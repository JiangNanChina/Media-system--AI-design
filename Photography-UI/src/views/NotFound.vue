<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="not-found-image">
        <el-icon size="120" color="#909399"><WarningFilled /></el-icon>
      </div>
      <h1 class="not-found-title">404</h1>
      <p class="not-found-subtitle">抱歉，您访问的页面不存在</p>
      <p class="not-found-description">
        您可能输入了错误的网址，或者该页面已被移动或删除
      </p>
      <div class="not-found-actions">
        <el-button type="primary" @click="$router.push('/')">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button @click="$router.back()">
          <el-icon><Back /></el-icon>
          返回上页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
// 页面组件无需额外逻辑
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

.not-found-content {
  text-align: center;
  max-width: 500px;
}

.not-found-image {
  margin-bottom: 30px;
}

.not-found-title {
  font-size: 72px;
  font-weight: bold;
  color: #909399;
  margin: 0 0 16px 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.not-found-subtitle {
  font-size: 24px;
  color: #606266;
  margin: 0 0 16px 0;
  font-weight: 500;
}

.not-found-description {
  font-size: 16px;
  color: #909399;
  margin: 0 0 40px 0;
  line-height: 1.5;
}

.not-found-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.not-found-actions .el-button {
  padding: 12px 24px;
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .not-found-title {
    font-size: 56px;
  }
  
  .not-found-subtitle {
    font-size: 20px;
  }
  
  .not-found-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .not-found-actions .el-button {
    width: 200px;
  }
}
</style>
