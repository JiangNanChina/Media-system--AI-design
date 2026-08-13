<template>
  <div class="app-container">
    <InteractiveCursor />
    <RouteTransition />
    <router-view v-slot="{ Component, route }">
      <transition :name="getTransitionName(route)">
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import InteractiveCursor from '@/components/InteractiveCursor.vue'
import RouteTransition from '@/components/RouteTransition.vue'
import { finishInitialRouteTransition } from '@/utils/routeTransition'

onMounted(() => {
  finishInitialRouteTransition()
})

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
  background: #f4f3ef;
  position: relative;
  overflow: hidden;
}

#app .app-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    linear-gradient(rgba(17, 16, 14, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(17, 16, 14, 0.045) 1px, transparent 1px),
    #f4f3ef;
  background-size: 34px 34px;
  pointer-events: none;
  z-index: 0;
}

#app .app-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent 49.9%, rgba(17, 16, 14, 0.05) 50%, transparent 50.1%);
  pointer-events: none;
  z-index: 0;
}

/* 页面过渡动画 - 精细化内容切换 */
/* 登录注册页面专用切换动画 */
.auth-slide-enter-active {
  transition: opacity 260ms ease-out, transform 260ms ease-out, filter 260ms ease-out;
}

.auth-slide-leave-active {
  transition: opacity 150ms ease-out, transform 150ms ease-out, filter 150ms ease-out;
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
.slide-right-enter-active {
  transition: opacity 260ms ease-out, transform 260ms ease-out;
}

.slide-left-leave-active,
.slide-right-leave-active {
  transition: opacity 150ms ease-out, transform 150ms ease-out;
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
.slide-right-enter-from {
  opacity: 0;
  transform: scale(1.02) translateY(-15px);
}

.slide-right-leave-to {
  opacity: 0;
  transform: scale(0.98) translateY(15px);
}

/* 淡入淡出动画 - 精细化 */
.fade-enter-active {
  transition: opacity 220ms ease-out, transform 220ms ease-out, filter 220ms ease-out;
}

.fade-leave-active {
  transition: opacity 150ms ease-out, transform 150ms ease-out, filter 150ms ease-out;
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

@media (prefers-reduced-motion: reduce) {
  .auth-slide-enter-active,
  .auth-slide-leave-active,
  .slide-left-enter-active,
  .slide-left-leave-active,
  .slide-right-enter-active,
  .slide-right-leave-active,
  .fade-enter-active,
  .fade-leave-active {
    transition-duration: 0.01ms;
  }
}
</style>
