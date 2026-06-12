import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/theme-chalk/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

import App from './App.vue'
import routes, { setupRouterGuards } from './router/index.js'
import './style.css'

const app = createApp(App)
const pinia = createPinia()
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

// 设置路由守卫
setupRouterGuards(router)

// 应用启动时检查token有效性
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()

// 检查本地存储的token是否有效
if (userStore.token && !userStore.checkTokenValidity()) {
  console.log('应用启动时发现token已过期，清除本地数据')
  userStore.logout()
}

app.mount('#app')