<template>
  <div class="app-container">
    <InteractiveCursor />
    <router-view v-slot="{ Component, route }">
      <transition 
        :name="getTransitionName(route)" 
        appear
      >
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import InteractiveCursor from '@/components/InteractiveCursor.vue'

// 存储上一个路由路径
let previousPath = ''

// 获取过渡动画名称
const getTransitionName = (currentRoute) => {
  const currentPath = currentRoute.path
  
  // 定义页面层级
  const pageHierarchy = {
    '/login': 1,
    '/register': 2,
    '/': 3,
    '/dashboard': 3
  }
  
  const currentLevel = pageHierarchy[currentPath] || 3
  const previousLevel = pageHierarchy[previousPath] || 3
  
  let transitionName = 'fade'
  
  // 登录和注册页面之间的特殊切换动画
  if ((currentPath === '/login' && previousPath === '/register') || 
      (currentPath === '/register' && previousPath === '/login')) {
    transitionName = 'auth-slide'
  }
  // 从登录/注册页面到其他页面
  else if ((previousPath === '/login' || previousPath === '/register') && 
           (currentPath !== '/login' && currentPath !== '/register')) {
    transitionName = 'slide-left'
  }
  // 从其他页面到登录/注册页面
  else if ((previousPath !== '/login' && previousPath !== '/register') && 
           (currentPath === '/login' || currentPath === '/register')) {
    transitionName = 'slide-right'
  }
  // 根据层级决定动画方向
  else if (currentLevel > previousLevel) {
    transitionName = 'slide-left'
  } else if (currentLevel < previousLevel) {
    transitionName = 'slide-right'
  }
  
  // 更新上一个路径
  previousPath = currentPath
  
  return transitionName
}

</script>

<style>
/* 全局样式已在 style.css 中定义 */

/* App容器样式 - 防止白屏 */
.app-container {
  min-height: 100vh;
  background: var(--gradient-background);
  position: relative;
  overflow: hidden;
}

.app-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 18% 14%, rgba(24, 185, 236, 0.14) 0%, transparent 34%),
    radial-gradient(circle at 82% 18%, rgba(75, 211, 180, 0.12) 0%, transparent 30%),
    radial-gradient(circle at 46% 82%, rgba(255, 213, 106, 0.12) 0%, transparent 28%);
  pointer-events: none;
  z-index: 0;
}

.app-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    linear-gradient(rgba(18, 174, 231, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(18, 174, 231, 0.045) 1px, transparent 1px);
  background-size: 36px 36px;
  pointer-events: none;
  z-index: 0;
}

/* 页面过渡动画 - 精细化内容切换 */
/* 登录注册页面专用切换动画 */
.auth-slide-enter-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.auth-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.auth-slide-enter-from {
  opacity: 0;
  transform: scale(0.98) translateY(10px);
  filter: blur(1px);
}

.auth-slide-leave-to {
  opacity: 0;
  transform: scale(1.02) translateY(-10px);
  filter: blur(1px);
}

/* 左滑动画 - 内容区域动画 */
.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-left-enter-from {
  opacity: 0;
  transform: scale(0.98) translateY(15px);
}

.slide-left-leave-to {
  opacity: 0;
  transform: scale(1.02) translateY(-15px);
}

/* 右滑动画 - 内容区域动画 */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.slide-right-enter-from {
  opacity: 0;
  transform: scale(1.02) translateY(-15px);
}

.slide-right-leave-to {
  opacity: 0;
  transform: scale(0.98) translateY(15px);
}

/* 淡入淡出动画 - 精细化 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.99);
  filter: blur(1px);
}

/* 页面容器样式 */
.router-view-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* 简化的层叠效果 */
.auth-slide-enter-active,
.slide-left-enter-active,
.slide-right-enter-active,
.fade-enter-active {
  position: relative;
  z-index: 2;
  min-height: 100vh;
}

.auth-slide-leave-active,
.slide-left-leave-active,
.slide-right-leave-active,
.fade-leave-active {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  z-index: 1;
  pointer-events: none;
}

/* 简化的内容动画 */
.auth-slide-enter-active .login-form,
.auth-slide-enter-active .register-card {
  animation: contentFadeIn 0.4s ease-out 0.1s both;
}

.auth-slide-leave-active .login-form,
.auth-slide-leave-active .register-card {
  animation: contentFadeOut 0.3s ease-in forwards;
}

@keyframes contentFadeIn {
  0% {
    opacity: 0;
    transform: translateY(10px) scale(0.98);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes contentFadeOut {
  0% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(-10px) scale(0.98);
  }
}
</style>
