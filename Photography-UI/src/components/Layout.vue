<template>
  <div class="main-layout">
    <!-- 移动端遮罩层 -->
    <div 
      v-if="isMobile && !isCollapse" 
      class="mobile-overlay"
      @click="closeMobileSidebar"
    ></div>
    
    <el-container>
      <!-- 侧边栏 -->
      <el-aside 
        :width="sidebarWidth" 
        :class="['sidebar', { 'mobile-sidebar': isMobile, 'sidebar-open': !isCollapse }]"
      >
        <div class="logo">
          <div class="logo-container" :class="{ 'collapsed': isCollapse && !isMobile }">
            <div class="logo-icon">
              <!-- 动态LOGO或默认图标 -->
              <img 
                v-if="siteConfig.siteLogo" 
                :src="siteConfig.siteLogo" 
                :alt="siteConfig.siteTitle || '网站LOGO'" 
                class="site-logo-img"
                :class="{ 'collapsed-logo': isCollapse && !isMobile }"
              />
              <el-icon 
                v-else 
                :size="isCollapse && !isMobile ? 24 : 28"
              ><Camera /></el-icon>
            </div>
            <div v-show="!isCollapse || isMobile" class="logo-content">
              <span class="logo-text">{{ siteConfig.siteTitle || '融媒体管理系统' }}</span>
              <span class="logo-subtitle">{{ siteConfig.siteSubtitle || 'Photography System' }}</span>
            </div>
          </div>
        </div>
        
        <el-scrollbar class="menu-scrollbar">
          <el-menu
            :default-active="$route.path"
            :collapse="isCollapse && !isMobile"
            :unique-opened="true"
            router
            background-color="transparent"
            text-color="#b8c5d1"
            active-text-color="#ffffff"
            @select="handleMenuSelect"
            class="modern-menu"
          >
          <template v-for="route in menuRoutes" :key="route.path">
            <!-- 单级菜单 -->
            <el-menu-item
              v-if="!route.children || route.children.length === 1"
              :index="route.children ? route.children[0].path : route.path"
              class="modern-menu-item"
            >
              <div class="menu-item-content">
                <div class="menu-icon">
                  <el-icon><component :is="route.meta.icon" /></el-icon>
                </div>
                <span class="menu-title">{{ route.meta.title }}</span>
              </div>
            </el-menu-item>
            
            <!-- 多级菜单 -->
            <el-sub-menu
              v-else
              :index="route.path"
              class="modern-sub-menu"
            >
              <template #title>
                <div class="menu-item-content">
                  <div class="menu-icon">
                    <el-icon><component :is="route.meta.icon" /></el-icon>
                  </div>
                  <span class="menu-title">{{ route.meta.title }}</span>
                </div>
              </template>
              <el-menu-item
                v-for="child in route.children"
                :key="child.path"
                :index="child.path"
                class="modern-sub-menu-item"
              >
                <div class="submenu-item-content">
                  <div class="submenu-icon">
                    <el-icon><component :is="child.meta.icon" /></el-icon>
                  </div>
                  <span class="submenu-title">{{ child.meta.title }}</span>
                </div>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
        </el-scrollbar>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="header modern-header">
          <div class="header-left">
            <div class="header-controls">
              <el-button
                text
                @click="toggleSidebar"
                class="sidebar-toggle modern-toggle"
              >
                <el-icon size="20">
                  <component :is="isMobile ? 'Menu' : (isCollapse ? 'Expand' : 'Fold')" />
                </el-icon>
              </el-button>
              
              <!-- 分隔线 -->
              <div v-if="!isMobile" class="header-divider"></div>
            </div>
            
            <!-- 面包屑导航 - 移动端隐藏 -->
            <el-breadcrumb 
              v-if="!isMobile" 
              separator="/" 
              class="breadcrumb modern-breadcrumb"
            >
              <el-breadcrumb-item
                v-for="item in breadcrumbItems"
                :key="item.path"
                :to="item.path"
                class="breadcrumb-item"
              >
                {{ item.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
            
            <!-- 移动端页面标题 -->
            <div v-if="isMobile" class="mobile-page-header">
              <h2 class="mobile-page-title">
                {{ currentPageTitle }}
              </h2>
            </div>
          </div>
          
          <div class="header-right">
            <!-- 快捷操作区 -->
            <div class="quick-actions">
              <!-- 公告提醒 -->
              <div class="action-item">
                <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
                  <el-button 
                    text 
                    @click="showAnnouncements" 
                    class="action-button"
                    :class="{ 'has-notification': unreadCount > 0 }"
                  >
                    <el-icon size="18"><Bell /></el-icon>
                  </el-button>
                </el-badge>
              </div>
              
              <!-- 分隔线 -->
              <div class="header-divider"></div>
            </div>
            
            <!-- 用户菜单 -->
            <el-dropdown @command="handleUserCommand" class="user-dropdown">
              <div class="user-info modern-user-info">
                <el-avatar 
                  :size="isMobile ? 32 : 36" 
                  :src="getAvatarUrl(userStore.userInfo?.avatar)"
                  class="user-avatar"
                >
                  <el-icon><UserFilled /></el-icon>
                </el-avatar>
                <div v-if="!isMobile" class="user-details">
                  <span class="user-name">{{ userStore.userName }}</span>
                  <span class="user-role">{{ userStore.isAdmin ? '管理员' : '成员' }}</span>
                </div>
                <el-icon v-if="!isMobile" class="dropdown-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="modern-dropdown-menu">
                  <el-dropdown-item command="profile" class="dropdown-item">
                    <div class="dropdown-item-content">
                      <el-icon class="dropdown-item-icon"><User /></el-icon>
                      <span>个人中心</span>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout" class="dropdown-item logout-item">
                    <div class="dropdown-item-content">
                      <el-icon class="dropdown-item-icon"><SwitchButton /></el-icon>
                      <span>退出登录</span>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 主内容 -->
        <el-main class="main-content">
          <div class="content-wrapper">
            <router-view v-slot="{ Component }">
              <transition name="fade" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </div>
        </el-main>
      </el-container>
    </el-container>
    
    <!-- 公告弹窗 -->
    <AnnouncementDialog
      v-model="announcementVisible"
      :announcements="announcements"
      @read="handleAnnouncementRead"
    />
    
    <!-- 登录后公告弹窗 -->
    <el-dialog
      v-model="showLoginAnnouncementDialog"
      title="系统公告"
      :width="isMobile ? '90%' : '600px'"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      center
      class="login-announcement-dialog"
    >
      <div v-if="currentLoginAnnouncement" class="announcement-content">
        <h3>{{ currentLoginAnnouncement.title }}</h3>
        <div class="announcement-text" v-html="currentLoginAnnouncement.content.replace(/\n/g, '<br>')"></div>
        <div class="announcement-meta">
          <span>发布时间: {{ formatTime(currentLoginAnnouncement.createdAt) }}</span>
        </div>
      </div>
      
      <template #footer>
        <div class="announcement-footer">
          <div class="countdown-info">
            <span v-if="loginAnnouncementCountdown > 0" class="countdown-text">
              请仔细阅读公告内容，{{ loginAnnouncementCountdown }} 秒后可关闭
            </span>
            <span v-else class="countdown-text">
              您已阅读完毕，可以关闭公告
            </span>
          </div>
          <div class="footer-buttons">
            <el-button 
              v-if="loginAnnouncementIndex < loginAnnouncements.length - 1"
              type="primary" 
              @click="nextLoginAnnouncement"
              :disabled="loginAnnouncementCountdown > 0"
            >
              {{ loginAnnouncementCountdown > 0 ? `${loginAnnouncementCountdown}秒` : '下一条' }}
            </el-button>
            <el-button 
              v-else
              type="primary" 
              @click="closeLoginAnnouncementDialog"
              :disabled="loginAnnouncementCountdown > 0"
            >
              {{ loginAnnouncementCountdown > 0 ? `${loginAnnouncementCountdown}秒` : '我知道了' }}
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Camera, Menu, Expand, Fold, Bell, User, UserFilled, 
  ArrowDown, SwitchButton, Odometer, OfficeBuilding, 
  DocumentCopy, Clock, Document, Monitor, List, 
  Management, Notebook, ChatDotRound, CircleCheck, 
  Grid, Setting, Calendar, TrendCharts, Edit, Tools
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAnnouncementStore } from '@/stores/announcement'
import AnnouncementDialog from '@/components/AnnouncementDialog.vue'
import request from '@/utils/request'
import { getSiteImageUrl } from '@/utils/imageUrl'
import { useAppShortcuts } from '@/composables/useKeyboardShortcuts'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const announcementStore = useAnnouncementStore()

// 响应式数据
const isCollapse = ref(false)
const isMobile = ref(false)
const announcementVisible = ref(false)
const announcements = ref([])
const unreadCount = ref(0)

// 站点配置数据
const siteConfig = reactive({
  siteTitle: '',
  siteSubtitle: '',
  siteLogo: '',
  primaryColor: ''
})

// 登录后公告弹窗相关
const showLoginAnnouncementDialog = ref(false)
const loginAnnouncements = ref([])
const currentLoginAnnouncement = ref(null)
const loginAnnouncementIndex = ref(0)
const loginAnnouncementCountdown = ref(3)
let loginAnnouncementTimer = null

// 头像URL处理函数
const getAvatarUrl = (avatarUrl) => {
  if (!avatarUrl) return ''
  
  // 如果是完整URL，直接返回
  if (avatarUrl.startsWith('http')) {
    return avatarUrl
  }
  
  // 如果是相对路径，直接返回（不添加API前缀，因为静态资源直接访问）
  return avatarUrl
}

// 计算属性
const sidebarWidth = computed(() => {
  if (isMobile.value) {
    return isCollapse.value ? '0px' : '200px'
  }
  return isCollapse.value ? '64px' : '200px'
})

// 当前页面标题
const currentPageTitle = computed(() => {
  const currentRoute = route.matched[route.matched.length - 1]
  return currentRoute?.meta?.title || '首页'
})

// 菜单配置
const menuItems = [
  {
    path: '/dashboard',
    meta: { title: '首页', icon: 'Odometer' }
  },
  {
    path: '/user',
    meta: { title: '用户管理', icon: 'UserFilled', requiresAdmin: true },
    children: [
      { path: '/user/list', meta: { title: '用户列表', icon: 'User' } }
    ]
  },
  {
    path: '/department',
    meta: { title: '部门管理', icon: 'OfficeBuilding', requiresAdmin: true },
    children: [
      { path: '/department/list', meta: { title: '部门列表', icon: 'Management' } }
    ]
  },
  {
    path: '/equipment',
    meta: { title: '设备管理', icon: 'Camera' },
    children: [
      { path: '/equipment/list', meta: { title: '设备列表', icon: 'List' } }
    ]
  },
  {
    path: '/borrow',
    meta: { title: '借还管理', icon: 'DocumentCopy' },
    children: [
      { path: '/borrow/list', meta: { title: '借还记录', icon: 'Notebook' } }
    ]
  },
  {
    path: '/announcement',
    meta: { title: '公告管理', icon: 'Bell', requiresAdmin: true },
    children: [
      { path: '/announcement/list', meta: { title: '公告列表', icon: 'ChatDotRound' } }
    ]
  },
  {
    path: '/checkin',
    meta: { title: '晚自习打卡', icon: 'Clock' },
    children: [
      { path: '/checkin/main', meta: { title: '打卡主页', icon: 'CircleCheck' } },
      { path: '/checkin/records', meta: { title: '打卡记录', icon: 'DocumentCopy' } },
      { path: '/checkin/qr-generator', meta: { title: '二维码生成', icon: 'Grid', requiresAdmin: true } },
      { path: '/checkin/audit', meta: { title: '签到审核', icon: 'DocumentChecked', requiresAdmin: true } },
      { path: '/checkin/configuration', meta: { title: '打卡配置', icon: 'Setting', requiresAdmin: true } }
    ]
  },
  {
    path: '/duty',
    meta: { title: '办公执勤', icon: 'OfficeBuilding' },
    children: [
      { path: '/duty/checkin', meta: { title: '我的执勤', icon: 'UserFilled' } },
      { path: '/duty/list', meta: { title: '执勤管理', icon: 'Calendar', requiresAdmin: true } },
      { path: '/duty/records', meta: { title: '执勤记录', icon: 'DocumentCopy' } },
      { path: '/duty/statistics', meta: { title: '执勤统计', icon: 'TrendCharts', requiresAdmin: true } }
    ]
  },
  {
    path: '/leave',
    meta: { title: '请假管理', icon: 'Document' },
    children: [
      { path: '/leave/list', meta: { title: '请假申请', icon: 'Edit' } }
    ]
  },
  {
    path: '/devices',
    meta: { title: '设备管理', icon: 'Monitor' },
    children: [
      { path: '/devices/my', meta: { title: '我的设备', icon: 'Monitor' } },
      { path: '/devices/admin', meta: { title: '设备管理', icon: 'Setting', requiresAdmin: true } },
      { path: '/devices/site-config', meta: { title: '站点配置', icon: 'Tools', requiresAdmin: true } }
    ]
  }
]

// 过滤菜单路由（根据用户权限）
const menuRoutes = computed(() => {
  return menuItems.map(item => {
    // 深拷贝菜单项
    const filteredItem = { ...item }
    
    // 如果有子菜单，过滤子菜单
    if (item.children) {
      filteredItem.children = item.children.filter(child => {
        // 排除需要管理员权限但用户不是管理员的子菜单
        if (child.meta?.requiresAdmin && !userStore.isAdmin) return false
        return true
      })
    }
    
    return filteredItem
  }).filter(item => {
    // 排除需要管理员权限但用户不是管理员的顶级菜单
    if (item.meta?.requiresAdmin && !userStore.isAdmin) return false
    
    // 如果有子菜单但过滤后没有可见的子菜单，则隐藏整个父菜单
    if (item.children && item.children.length === 0) return false
    
    return true
  })
})

// 面包屑导航
const breadcrumbItems = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  const items = []
  
  matched.forEach(item => {
    if (item.meta.title && item.meta.title !== '首页') {
      items.push({
        path: item.path,
        title: item.meta.title
      })
    }
  })
  
  return items
})

// 检查是否为移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
  // 移动端默认收起侧边栏
  if (isMobile.value) {
    isCollapse.value = true
  }
}

// 处理窗口大小变化
const handleResize = () => {
  checkMobile()
}

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 关闭移动端侧边栏
const closeMobileSidebar = () => {
  if (isMobile.value) {
    isCollapse.value = true
  }
}

// 处理菜单选择（移动端自动收起）
const handleMenuSelect = () => {
  if (isMobile.value) {
    // 延迟收起，确保路由跳转完成
    setTimeout(() => {
      isCollapse.value = true
    }, 100)
  }
}

// 处理用户菜单命令
const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await userStore.logout()
        ElMessage.success('退出登录成功')
        router.push('/login')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('退出登录失败:', error)
        }
      }
      break
  }
}

// 显示公告
const showAnnouncements = () => {
  announcementVisible.value = true
}

// 处理公告已读
const handleAnnouncementRead = (announcementId) => {
  // 标记公告为已读
  markAnnouncementAsRead(announcementId)
}

// 获取未读公告
const fetchUnreadAnnouncements = async () => {
  try {
    // 暂时禁用公告获取，避免400错误
    // TODO: 修复后端/announcements/public/paged接口的400错误后重新启用
    // const response = await request.get('/announcements/public/paged', {
    //   params: {
    //     page: 0,
    //     size: 10
    //   }
    // })
    
    // if (response.data && response.data.content) {
    //   announcements.value = response.data.content
    //   // 这里可以根据实际需求计算未读数量
    //   unreadCount.value = response.data.content.length
    // }
    
    // 设置默认空数据
    announcements.value = []
    unreadCount.value = 0
    console.log('公告数据已初始化为空列表')
  } catch (error) {
    console.error('获取公告失败:', error)
    announcements.value = []
    unreadCount.value = 0
  }
}

// 标记公告为已读
const markAnnouncementAsRead = async (announcementId) => {
  try {
    await request.put(`/announcements/${announcementId}/view`)
    // 减少未读数量
    if (unreadCount.value > 0) {
      unreadCount.value--
    }
  } catch (error) {
    console.error('标记公告已读失败:', error)
  }
}

// 组件挂载时获取公告
// 格式化时间
const formatTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 通过 Pinia 在运行时记录本会话的已读公告（刷新页面后重置，但路由切换不丢失）
const getCurrentPageReadAnnouncements = () => announcementStore.readIdsInRuntime
const markCurrentPageAnnouncementAsRead = (announcementId) => announcementStore.markRead(announcementId)
const clearCurrentPageReadAnnouncementsCache = () => {
  announcementStore.clearRuntime()
  console.log('当前页面/会话已读公告缓存已清理')
  ElMessage.success('当前会话已读公告缓存已清理')
}

// 注意：调试方法将在所有函数定义后暴露到全局

// 获取登录后公告（排除归档的公告）
const fetchLoginAnnouncements = async () => {
  try {
    console.log('开始获取登录公告...')
    
    const response = await request.get('/announcements/public/login-popup', {
      params: {
        page: 0,
        size: 10
      }
    })
    
    console.log('登录公告API响应:', response.data)
    
    if (response.data?.content && response.data.content.length > 0) {
      console.log(`后端返回 ${response.data.content.length} 条公告`)
      
      // 获取当前页面已读的公告列表
      const currentPageReadList = getCurrentPageReadAnnouncements()
      console.log('当前页面已读公告列表:', currentPageReadList)
      
      // 过滤出当前页面未读的公告
      const unreadAnnouncements = response.data.content.filter(
        announcement => !currentPageReadList.includes(announcement.id)
      )
      
      console.log(`过滤后未读公告数量: ${unreadAnnouncements.length}`)
      
      if (unreadAnnouncements.length > 0) {
        console.log('显示公告弹窗')
        loginAnnouncements.value = unreadAnnouncements
        showLoginAnnouncementPopup()
      } else {
        console.log('当前页面中所有公告均已阅读，不显示弹窗')
      }
    } else {
      console.log('后端未返回公告数据或数据为空')
    }
  } catch (error) {
    console.error('获取登录公告失败:', error)
  }
}

// 显示登录公告弹窗
const showLoginAnnouncementPopup = () => {
  if (loginAnnouncements.value.length > 0) {
    loginAnnouncementIndex.value = 0
    currentLoginAnnouncement.value = loginAnnouncements.value[0]
    showLoginAnnouncementDialog.value = true
    startLoginAnnouncementCountdown()
  }
}

// 开始倒计时
const startLoginAnnouncementCountdown = () => {
  loginAnnouncementCountdown.value = 3
  if (loginAnnouncementTimer) {
    clearInterval(loginAnnouncementTimer)
  }
  
  loginAnnouncementTimer = setInterval(() => {
    loginAnnouncementCountdown.value--
    if (loginAnnouncementCountdown.value <= 0) {
      clearInterval(loginAnnouncementTimer)
      loginAnnouncementTimer = null
    }
  }, 1000)
}

// 下一条公告
const nextLoginAnnouncement = () => {
  // 标记当前公告为当前页面已读
  if (currentLoginAnnouncement.value) {
    markCurrentPageAnnouncementAsRead(currentLoginAnnouncement.value.id)
    markAnnouncementAsRead(currentLoginAnnouncement.value.id)
  }
  
  if (loginAnnouncementIndex.value < loginAnnouncements.value.length - 1) {
    loginAnnouncementIndex.value++
    currentLoginAnnouncement.value = loginAnnouncements.value[loginAnnouncementIndex.value]
    startLoginAnnouncementCountdown()
  } else {
    // 如果是最后一条公告，直接关闭弹窗
    closeLoginAnnouncementDialog()
  }
}

// 关闭登录公告弹窗
const closeLoginAnnouncementDialog = () => {
  showLoginAnnouncementDialog.value = false
  if (loginAnnouncementTimer) {
    clearInterval(loginAnnouncementTimer)
    loginAnnouncementTimer = null
  }
  
  // 标记所有登录公告为当前页面已读（刷新页面后会重新弹出）
  loginAnnouncements.value.forEach(announcement => {
    markCurrentPageAnnouncementAsRead(announcement.id)
    // 同时调用原来的后端接口标记已读
    markAnnouncementAsRead(announcement.id)
  })
  
  console.log('公告已标记为当前页面已读，刷新页面后会重新弹出')
}

// 加载站点配置
const loadSiteConfig = async () => {
  try {
    const response = await request.get('/site-config/public')
    
    if (response.success && response.data) {
      const configData = response.data
      
      siteConfig.siteTitle = configData['site.title'] || ''
      siteConfig.siteSubtitle = configData['site.subtitle'] || ''
      siteConfig.siteLogo = getSiteImageUrl(configData['site.logo'])
      siteConfig.primaryColor = configData['theme.primary_color'] || ''
      
      // 缓存站点标题到localStorage，供路由使用
      if (siteConfig.siteTitle) {
        localStorage.setItem('siteTitle', siteConfig.siteTitle)
      }
      
      // 如果有主题色，动态设置CSS变量
      if (siteConfig.primaryColor) {
        document.documentElement.style.setProperty('--el-color-primary', siteConfig.primaryColor)
      }
    }
  } catch (error) {
    console.warn('Layout组件加载站点配置失败:', error)
  }
}

onMounted(() => {
  loadSiteConfig()
  fetchUnreadAnnouncements()
  // 登录后自动获取公告
  fetchLoginAnnouncements()
  
  // 初始检查移动端
  checkMobile()
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
  
  // 初始化全局快捷键
  useAppShortcuts(router)
})

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('resize', handleResize)
  
  // 清理定时器
  if (loginAnnouncementTimer) {
    clearInterval(loginAnnouncementTimer)
  }
})

// 在开发环境下暴露调试方法到全局（放在所有函数定义之后）
if (process.env.NODE_ENV === 'development') {
  window.clearCurrentPageReadAnnouncementsCache = clearCurrentPageReadAnnouncementsCache
  window.debugFetchLoginAnnouncements = fetchLoginAnnouncements
  window.debugGetCurrentPageReadAnnouncements = getCurrentPageReadAnnouncements
  window.debugShowMockAnnouncement = () => {
    // 创建模拟公告数据用于测试
    loginAnnouncements.value = [{
      id: 'test-' + Date.now(),
      title: '测试公告',
      content: '这是一条测试公告，用于验证弹窗功能',
      createdAt: new Date().toISOString()
    }]
    showLoginAnnouncementPopup()
  }
}

// 监听路由变化，移动端自动收起侧边栏
watch(() => route.path, () => {
  if (isMobile.value) {
    isCollapse.value = true
  }
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  background: var(--color-background);
}

/* 现代化侧边栏样式 */
.sidebar {
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  transition: width var(--duration-normal) var(--easing-ease);
  overflow: hidden;
  box-shadow: var(--shadow-base);
  position: relative;
  z-index: 10;
}

.sidebar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 10% 20%, rgba(255, 255, 255, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 90% 80%, rgba(255, 255, 255, 0.03) 0%, transparent 50%);
  pointer-events: none;
}

/* 现代化Logo区域 */
.logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 var(--spacing-5);
  background: rgba(0, 0, 0, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 1;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  transition: all var(--duration-normal) var(--easing-ease);
}

.logo-container.collapsed {
  justify-content: center;
  gap: 0;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: var(--gradient-primary);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-white);
  box-shadow: var(--shadow-hover);
  flex-shrink: 0;
  transition: all var(--duration-normal) var(--easing-ease);
}

.logo-container.collapsed .logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
}

/* LOGO图片样式 */
.site-logo-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.site-logo-img.collapsed-logo {
  border-radius: 10px;
}

.logo-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.logo-text {
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  white-space: nowrap;
  line-height: 1.2;
}

.logo-subtitle {
  color: rgba(255, 255, 255, 0.7);
  font-size: 11px;
  font-weight: 400;
  white-space: nowrap;
  line-height: 1;
}

/* 菜单滚动条 */
.menu-scrollbar {
  height: calc(100vh - 70px);
}

.menu-scrollbar :deep(.el-scrollbar__view) {
  padding: 12px 0;
}

/* 现代化菜单样式 */
.modern-menu {
  border-right: none;
  background: transparent;
}

.modern-menu :deep(.el-menu-item),
.modern-menu :deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  margin: var(--spacing-1) var(--spacing-3);
  border-radius: var(--radius-md);
  transition: all var(--duration-normal) var(--easing-ease);
  position: relative;
  overflow: hidden;
  display: flex !important;
  align-items: center !important;
  padding: 0 var(--spacing-4) !important;
  color: rgba(255, 255, 255, 0.95) !important;
}

.modern-menu :deep(.el-menu-item:hover),
.modern-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.15) !important;
  color: var(--color-white) !important;
  transform: translateX(4px);
}

.modern-menu :deep(.el-menu-item.is-active) {
  background: var(--gradient-primary) !important;
  color: var(--color-white) !important;
  box-shadow: var(--shadow-hover);
  transform: translateX(4px);
}

.modern-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  background: rgba(255, 255, 255, 0.15) !important;
  color: #ffffff !important;
}

.menu-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0;
  width: 100%;
}

.menu-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
  color: inherit;
}

.menu-title {
  font-size: 14px;
  font-weight: 500;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: inherit;
}

/* 子菜单样式 */
.modern-menu :deep(.el-menu--inline) {
  background: rgba(0, 0, 0, 0.1);
  margin: 4px 12px;
  border-radius: 8px;
  padding: 4px 0;
}

.modern-menu :deep(.el-menu--inline .el-menu-item) {
  height: 40px;
  line-height: 40px;
  margin: 1px 8px;
  border-radius: 8px;
  padding: 0 16px !important;
  display: flex !important;
  align-items: center !important;
  color: rgba(255, 255, 255, 0.9) !important;
}

.modern-menu :deep(.el-menu--inline .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.12) !important;
  color: #ffffff !important;
}

.modern-menu :deep(.el-menu--inline .el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.2) !important;
  color: #ffffff !important;
}

.submenu-item-content {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0;
  width: 100%;
  margin-left: 24px; /* 子菜单项缩进 */
}

.submenu-icon {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 14px;
  color: inherit;
}

.submenu-title {
  font-size: 13px;
  font-weight: 400;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: inherit;
}

/* 现代化顶部导航栏 */
.modern-header {
  background: var(--color-white);
  border-bottom: 1px solid var(--color-divider);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-6);
  box-shadow: var(--shadow-sm);
  position: relative;
  z-index: 100;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.modern-toggle {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-secondary-500);
  transition: all var(--duration-normal) var(--easing-ease);
  border: 1px solid transparent;
}

.modern-toggle:hover {
  background: var(--color-secondary-100) !important;
  color: var(--color-primary-600) !important;
  border-color: var(--color-divider);
  transform: scale(1.05);
}

.header-divider {
  width: 1px;
  height: 24px;
  background: #e2e8f0;
  margin: 0 4px;
}

.modern-breadcrumb {
  margin-left: 8px;
}

.modern-breadcrumb :deep(.el-breadcrumb__item) {
  font-size: 14px;
  font-weight: 500;
}

.modern-breadcrumb :deep(.el-breadcrumb__inner) {
  color: #64748b;
  transition: color 0.3s ease;
}

.modern-breadcrumb :deep(.el-breadcrumb__inner:hover) {
  color: var(--color-primary-600);
}

.modern-breadcrumb :deep(.el-breadcrumb__inner.is-link) {
  color: var(--color-primary-600);
}

.mobile-page-header {
  margin-left: 12px;
  flex: 1;
  min-width: 0;
}

.mobile-page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.quick-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-item {
  position: relative;
}

.action-button {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.action-button:hover {
  background: #f2fbff !important;
  color: var(--color-primary-600) !important;
  transform: scale(1.05);
}

.action-button.has-notification {
  color: #f59e0b;
}

.action-button.has-notification:hover {
  color: #d97706 !important;
  background: #fef3c7 !important;
}

.notification-badge :deep(.el-badge__content) {
  background: #ef4444;
  border: 2px solid #ffffff;
  font-size: 10px;
  font-weight: 600;
  min-width: 18px;
  height: 18px;
  line-height: 14px;
}

.user-dropdown {
  position: relative;
}

.modern-user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  gap: 12px;
}

.modern-user-info:hover {
  background: #f8fafc;
  border-color: #e2e8f0;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  border: 2px solid #ffffff;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: #64748b;
  line-height: 1;
  white-space: nowrap;
}

.dropdown-icon {
  font-size: 14px;
  color: #94a3b8;
  transition: transform 0.3s ease;
}

.user-dropdown:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 下拉菜单样式 */
.modern-dropdown-menu :deep(.el-dropdown-menu) {
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 
    0 10px 25px rgba(0, 0, 0, 0.1),
    0 4px 6px rgba(0, 0, 0, 0.05);
  padding: 8px;
  min-width: 160px;
}

.modern-dropdown-menu :deep(.el-dropdown-menu__item) {
  border-radius: 8px;
  padding: 8px 12px;
  margin: 2px 0;
  transition: all 0.3s ease;
}

.modern-dropdown-menu :deep(.el-dropdown-menu__item:hover) {
  background: #f2fbff;
  color: var(--color-primary-600);
}

.modern-dropdown-menu :deep(.el-dropdown-menu__item.logout-item:hover) {
  background: #fef2f2;
  color: #ef4444;
}

.dropdown-item-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dropdown-item-icon {
  font-size: 16px;
}

/* 现代化主内容区域 */
.main-content {
  background: var(--color-background);
  padding: 0;
  flex: 1;
  overflow-y: auto;
  height: 100vh;
}

.content-wrapper {
  padding: var(--spacing-6);
  width: 100%;
  max-width: 100%;
  animation: fadeIn var(--duration-normal) var(--easing-ease);
}

/* 页面过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 现代化移动端遮罩层 */
.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  z-index: 1999;
  transition: all var(--duration-normal) var(--easing-ease);
}

/* 移动端侧边栏 */
.mobile-sidebar {
  position: fixed !important;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 2000;
  transform: translateX(-100%);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  width: 240px !important;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
}

.mobile-sidebar.sidebar-open {
  transform: translateX(0);
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .sidebar {
    width: 200px;
  }
  
  .logo-icon {
    width: 36px;
    height: 36px;
  }
  
  .logo-text {
    font-size: 15px;
  }
  
  .logo-subtitle {
    font-size: 10px;
  }
}

@media (max-width: 768px) {
  .main-layout {
    overflow-x: hidden;
  }
  
  .sidebar:not(.mobile-sidebar) {
    display: none;
  }
  
  .modern-header {
    padding: 0 16px;
    height: 60px;
  }
  
  .header-controls {
    gap: 8px;
  }
  
  .modern-toggle {
    width: 36px;
    height: 36px;
  }
  
  .header-divider {
    display: none;
  }
  
  .content-wrapper {
    padding: 16px;
  }
  
  .quick-actions {
    gap: 8px;
  }
  
  .action-button {
    width: 36px;
    height: 36px;
  }
  
  .modern-user-info {
    padding: 4px 8px;
    gap: 8px;
  }
  
  .user-avatar {
    border-width: 1px;
  }
  
  /* 移动端侧边栏样式调整 */
  .mobile-sidebar {
    width: 260px !important;
  }
  
  .mobile-sidebar .logo {
    height: 60px;
    padding: 0 16px;
  }
  
  .mobile-sidebar .logo-icon {
    width: 32px;
    height: 32px;
  }
  
  .mobile-sidebar .logo-text {
    font-size: 14px;
  }
  
  .mobile-sidebar .logo-subtitle {
    font-size: 10px;
  }
  
  .mobile-sidebar .menu-scrollbar {
    height: calc(100vh - 60px);
  }
  
  .mobile-sidebar .modern-menu :deep(.el-menu-item),
  .mobile-sidebar .modern-menu :deep(.el-sub-menu__title) {
    height: 44px;
    line-height: 44px;
    margin: 2px 8px;
  }
  
  .mobile-sidebar .menu-title {
    font-size: 13px;
  }
  
  .mobile-sidebar .submenu-title {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .modern-header {
    padding: 0 12px;
    height: 56px;
  }
  
  .content-wrapper {
    padding: 12px;
  }
  
  .mobile-page-title {
    font-size: 16px;
  }
  
  .modern-toggle {
    width: 32px;
    height: 32px;
  }
  
  .action-button {
    width: 32px;
    height: 32px;
  }
  
  .user-avatar {
    width: 28px !important;
    height: 28px !important;
  }
  
  .modern-user-info {
    padding: 2px 6px;
  }
  
  /* 超小屏幕侧边栏 */
  .mobile-sidebar {
    width: 240px !important;
  }
  
  .mobile-sidebar .logo {
    height: 56px;
    padding: 0 12px;
  }
  
  .mobile-sidebar .logo-icon {
    width: 28px;
    height: 28px;
  }
  
  .mobile-sidebar .logo-text {
    font-size: 13px;
  }
  
  .mobile-sidebar .modern-menu :deep(.el-menu-item),
  .mobile-sidebar .modern-menu :deep(.el-sub-menu__title) {
    height: 40px;
    line-height: 40px;
    margin: 1px 6px;
  }
  
  .mobile-sidebar .menu-title {
    font-size: 12px;
  }
  
  .mobile-sidebar .submenu-title {
    font-size: 11px;
  }
}

/* 收起状态样式 */
.sidebar {
  &.el-aside--collapsed {
    .logo-content {
      display: none;
    }
  }
}

/* 菜单收起状态 */
.modern-menu.el-menu--collapse {
  .menu-title,
  .submenu-title {
    display: none;
  }
  
  .menu-item-content {
    justify-content: center;
    padding: 0;
    gap: 0;
    width: 100%;
    margin-left: 0; /* 收起时移除缩进 */
  }
  
  .submenu-item-content {
    justify-content: center;
    padding: 0;
    gap: 0;
    width: 100%;
    margin-left: 0; /* 收起时移除缩进 */
  }
  
  .menu-icon,
  .submenu-icon {
    margin: 0;
    width: 20px;
    height: 20px;
    font-size: 18px;
  }
}

.modern-menu.el-menu--collapse :deep(.el-menu-item),
.modern-menu.el-menu--collapse :deep(.el-sub-menu__title) {
  padding: 0 !important;
  text-align: center;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  margin: 2px 8px;
  width: calc(100% - 16px);
}

.modern-menu.el-menu--collapse :deep(.el-menu--inline) {
  background: rgba(0, 0, 0, 0.15);
  margin: 4px 8px;
  padding: 2px 0;
}

.modern-menu.el-menu--collapse :deep(.el-menu--inline .el-menu-item) {
  padding: 0 !important;
  margin: 1px 4px;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  height: 36px;
  line-height: 36px;
}

/* 收起状态的tooltip */
.modern-menu.el-menu--collapse :deep(.el-tooltip) {
  width: 100%;
  display: flex;
  justify-content: center;
}

.modern-menu.el-menu--collapse :deep(.el-tooltip__trigger) {
  width: 100%;
  display: flex;
  justify-content: center;
}

/* 收起状态的子菜单箭头隐藏 */
.modern-menu.el-menu--collapse :deep(.el-sub-menu__icon-arrow) {
  display: none;
}

/* 收起状态图标精确对齐 */
.modern-menu.el-menu--collapse .menu-icon {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  width: 20px !important;
  height: 20px !important;
  margin: 0 auto !important;
}

.modern-menu.el-menu--collapse .submenu-icon {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  width: 16px !important;
  height: 16px !important;
  margin: 0 auto !important;
}

/* 确保收起状态下所有菜单项图标垂直居中 */
.modern-menu.el-menu--collapse :deep(.el-menu-item .menu-item-content),
.modern-menu.el-menu--collapse :deep(.el-sub-menu__title .menu-item-content) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  height: 100% !important;
  width: 100% !important;
}

.modern-menu.el-menu--collapse :deep(.el-menu--inline .el-menu-item .submenu-item-content) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  height: 100% !important;
  width: 100% !important;
}

/* 收起状态子菜单弹窗样式 */
.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup) {
  background: #ffffff !important;
  border: 2px solid #d1d5db !important;
  border-radius: 12px !important;
  box-shadow: 
    0 10px 25px rgba(0, 0, 0, 0.2),
    0 4px 10px rgba(0, 0, 0, 0.1) !important;
  padding: 8px 0 !important;
  margin-top: -4px !important;
  margin-left: 8px !important;
  min-width: 180px !important;
}

.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup .el-menu-item) {
  height: 40px !important;
  line-height: 40px !important;
  margin: 2px 8px !important;
  border-radius: 8px !important;
  padding: 0 16px !important;
  color: #1f2937 !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup .el-menu-item:hover) {
  background: #f1f5f9 !important;
  color: #0f172a !important;
  transform: translateX(4px) !important;
}

.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup .el-menu-item.is-active) {
  background: linear-gradient(135deg, #f7fdff 0%, #e0f5ff 58%, #ecfff9 100%) !important;
  color: var(--color-primary-600) !important;
  box-shadow: 0 4px 12px rgba(21, 122, 177, 0.12) !important;
  transform: translateX(4px) !important;
}

.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup .el-menu-item .submenu-item-content) {
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
  padding: 0 !important;
  margin-left: 0 !important;
  width: 100% !important;
}

.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup .el-menu-item .submenu-icon) {
  width: 16px !important;
  height: 16px !important;
  font-size: 14px !important;
  margin: 0 !important;
  flex-shrink: 0 !important;
  color: #4b5563 !important;
}

.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup .el-menu-item:hover .submenu-icon) {
  color: #1f2937 !important;
}

.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup .el-menu-item.is-active .submenu-icon) {
  color: #ffffff !important;
}

.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup .el-menu-item .submenu-title) {
  font-size: 14px !important;
  font-weight: 600 !important;
  flex: 1 !important;
  display: block !important;
  color: inherit !important;
  letter-spacing: 0.025em !important;
}

/* 滚动条样式 */
.menu-scrollbar :deep(.el-scrollbar__bar) {
  opacity: 0.3;
}

.menu-scrollbar :deep(.el-scrollbar__thumb) {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}

.menu-scrollbar :deep(.el-scrollbar__bar.is-horizontal) {
  display: none;
}

/* 登录公告弹窗样式 */
.login-announcement-dialog .el-dialog__body {
  padding: 20px;
}

.announcement-content h3 {
  color: var(--color-primary-600);
  margin-bottom: 15px;
  font-size: 18px;
}

.announcement-text {
  line-height: 1.6;
  color: #606266;
  margin-bottom: 15px;
  max-height: 300px;
  overflow-y: auto;
}

.announcement-meta {
  color: #909399;
  font-size: 12px;
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
}

.announcement-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.countdown-info {
  flex: 1;
}

.countdown-text {
  color: #909399;
  font-size: 14px;
}

.footer-buttons {
  margin-left: 15px;
}

/* 移动端公告弹窗样式 */
@media (max-width: 768px) {
  .login-announcement-dialog .el-dialog__body {
    padding: 15px;
  }
  
  .announcement-content h3 {
    font-size: 16px;
    margin-bottom: 12px;
  }
  
  .announcement-text {
    font-size: 14px;
    max-height: 250px;
  }
  
  .announcement-footer {
    flex-direction: column;
    gap: 10px;
  }
  
  .footer-buttons {
    margin-left: 0;
    width: 100%;
  }
  
  .footer-buttons .el-button {
    width: 100%;
  }
  
  .countdown-info {
    text-align: center;
  }
}

/* Glass bento layout overrides */
.main-layout {
  background: transparent !important;
}

.main-layout > .el-container {
  min-height: 100vh;
}

.sidebar {
  margin: 14px 0 14px 14px;
  height: calc(100vh - 28px) !important;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(241, 250, 255, 0.82)),
    radial-gradient(circle at 14% 10%, rgba(37, 184, 242, 0.12), transparent 34%),
    radial-gradient(circle at 78% 92%, rgba(98, 214, 189, 0.09), transparent 32%) !important;
  border: 1px solid rgba(255, 255, 255, 0.72) !important;
  border-radius: 22px !important;
  box-shadow: 0 24px 70px rgba(21, 122, 177, 0.12) !important;
  backdrop-filter: blur(24px) saturate(1.08);
  -webkit-backdrop-filter: blur(24px) saturate(1.08);
  overflow: hidden !important;
}

.sidebar::before {
  display: none !important;
}

.logo {
  height: 76px !important;
  padding: 0 16px !important;
  background: transparent !important;
  border-bottom: 1px solid rgba(135, 185, 214, 0.18) !important;
}

.logo-icon {
  background: linear-gradient(135deg, #f7fdff, #e0f5ff 58%, #ecfff9) !important;
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 14px !important;
  color: var(--color-primary-600) !important;
  box-shadow: 0 12px 28px rgba(21, 122, 177, 0.1) !important;
}

.logo-text {
  color: #123044 !important;
  font-weight: 700 !important;
  letter-spacing: 0 !important;
}

.logo-subtitle {
  color: rgba(73, 101, 121, 0.72) !important;
}

.menu-scrollbar {
  height: calc(100vh - 104px) !important;
}

.modern-menu :deep(.el-menu-item),
.modern-menu :deep(.el-sub-menu__title) {
  height: 42px !important;
  line-height: 42px !important;
  margin: 4px 10px !important;
  border-radius: 13px !important;
  color: rgba(79, 109, 130, 0.92) !important;
  transform: none !important;
}

.modern-menu :deep(.el-menu-item:hover),
.modern-menu :deep(.el-sub-menu__title:hover) {
  background: rgba(242, 251, 255, 0.9) !important;
  color: var(--color-primary-600) !important;
  transform: translateX(2px) !important;
}

.modern-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(242, 251, 255, 0.96), rgba(232, 255, 248, 0.78)) !important;
  border: 1px solid rgba(37, 184, 242, 0.28) !important;
  color: var(--color-primary-600) !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7), 0 12px 28px rgba(21, 122, 177, 0.1) !important;
  transform: translateX(2px) !important;
}

.modern-menu :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  background: rgba(242, 251, 255, 0.86) !important;
  color: var(--color-primary-600) !important;
}

.menu-icon,
.submenu-icon {
  color: inherit !important;
}

.menu-title,
.submenu-title {
  letter-spacing: 0 !important;
}

.modern-menu :deep(.el-menu--inline) {
  margin: 4px 10px !important;
  background: rgba(255, 255, 255, 0.62) !important;
  border-radius: 14px !important;
}

.modern-menu :deep(.el-menu--inline .el-menu-item) {
  margin: 2px 6px !important;
  height: 36px !important;
  line-height: 36px !important;
  color: rgba(79, 109, 130, 0.84) !important;
}

.modern-header {
  height: 64px !important;
  margin: 14px 14px 0 14px;
  padding: 0 16px !important;
  background: rgba(255, 255, 255, 0.84) !important;
  border: 1px solid rgba(255, 255, 255, 0.66) !important;
  border-radius: 20px !important;
  box-shadow: 0 16px 38px rgba(21, 122, 177, 0.08) !important;
  backdrop-filter: blur(22px) saturate(1.1);
  -webkit-backdrop-filter: blur(22px) saturate(1.1);
}

.modern-toggle,
.action-button,
.modern-user-info {
  background: rgba(255, 255, 255, 0.62) !important;
  border: 1px solid rgba(135, 185, 214, 0.2) !important;
  border-radius: 14px !important;
  color: #496579 !important;
  box-shadow: none !important;
  transform: none !important;
}

.modern-toggle:hover,
.action-button:hover,
.modern-user-info:hover {
  background: rgba(242, 251, 255, 0.96) !important;
  border-color: rgba(37, 184, 242, 0.3) !important;
  color: var(--color-primary-600) !important;
  transform: translateY(-1px) !important;
}

.header-divider {
  background: rgba(135, 185, 214, 0.2) !important;
}

.modern-breadcrumb :deep(.el-breadcrumb__inner),
.user-role,
.dropdown-icon {
  color: #7691a4 !important;
}

.modern-breadcrumb :deep(.el-breadcrumb__inner.is-link),
.modern-breadcrumb :deep(.el-breadcrumb__inner:hover),
.user-name {
  color: #123044 !important;
}

.user-avatar {
  border-color: rgba(255, 255, 255, 0.92) !important;
  box-shadow: 0 6px 18px rgba(21, 122, 177, 0.1) !important;
}

.main-content {
  height: 100vh !important;
  background: transparent !important;
}

.content-wrapper {
  min-height: calc(100vh - 78px) !important;
  padding: 22px 22px 28px !important;
}

.mobile-overlay {
  background: rgba(18, 85, 116, 0.32) !important;
}

.login-announcement-dialog :deep(.el-dialog) {
  border-radius: 20px;
}

.announcement-content h3 {
  color: var(--color-text-primary) !important;
}

.announcement-text {
  color: var(--color-text-secondary) !important;
}

.announcement-meta,
.countdown-text {
  color: var(--color-text-placeholder) !important;
}

@media (max-width: 768px) {
  .sidebar {
    margin: 0 !important;
    height: 100vh !important;
    border-radius: 0 22px 22px 0 !important;
  }

  .modern-header {
    height: 56px !important;
    margin: 10px 10px 0 !important;
    padding: 0 12px !important;
    border-radius: 16px !important;
  }

  .content-wrapper {
    padding: 16px 12px 22px !important;
  }

  .mobile-page-title {
    color: #0f172a !important;
    font-size: 16px !important;
  }
}

/* Fixed navigation + independent content scroll */
.main-layout {
  height: 100vh !important;
  min-height: 100vh !important;
  overflow: hidden !important;
}

.main-layout > .el-container {
  height: 100vh !important;
  min-height: 0 !important;
  overflow: hidden !important;
}

.main-layout > .el-container > .el-container {
  min-width: 0 !important;
  min-height: 0 !important;
  height: 100vh !important;
  overflow: hidden !important;
}

.sidebar {
  flex: 0 0 auto !important;
  position: sticky !important;
  top: 14px;
}

.modern-header {
  flex: 0 0 64px !important;
  position: relative;
  z-index: 30;
}

.main-content {
  flex: 1 1 auto !important;
  min-height: 0 !important;
  height: auto !important;
  padding: 0 !important;
  overflow-x: hidden !important;
  overflow-y: auto !important;
  overscroll-behavior: contain;
  scrollbar-gutter: stable;
}

.content-wrapper {
  min-height: 100% !important;
  padding: 22px 22px 32px !important;
}

@media (max-width: 768px) {
  .main-layout,
  .main-layout > .el-container,
  .main-layout > .el-container > .el-container {
    height: 100dvh !important;
  }

  .modern-header {
    flex-basis: 56px !important;
    position: relative;
    z-index: 30;
  }

  .main-content {
    height: auto !important;
  }

  .content-wrapper {
    padding: 16px 12px 24px !important;
  }
}

/* Keep second-level menu readable on the light glass sidebar. */
.modern-menu :deep(.el-menu--inline .el-menu-item),
.modern-menu :deep(.el-menu--inline .el-menu-item .submenu-title),
.modern-menu :deep(.el-menu--inline .el-menu-item .submenu-icon) {
  color: rgba(73, 101, 121, 0.9) !important;
}

.modern-menu :deep(.el-menu--inline .el-menu-item:hover),
.modern-menu :deep(.el-menu--inline .el-menu-item:hover .submenu-title),
.modern-menu :deep(.el-menu--inline .el-menu-item:hover .submenu-icon) {
  color: #0876a5 !important;
}

.modern-menu :deep(.el-menu--inline .el-menu-item.is-active),
.modern-menu :deep(.el-menu--inline .el-menu-item.is-active .submenu-title),
.modern-menu :deep(.el-menu--inline .el-menu-item.is-active .submenu-icon) {
  color: #0876a5 !important;
}

.modern-menu :deep(.el-menu--inline .el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(242, 251, 255, 0.98), rgba(232, 255, 248, 0.86)) !important;
  border: 1px solid rgba(37, 184, 242, 0.28) !important;
}

.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup .el-menu-item.is-active),
.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup .el-menu-item.is-active .submenu-title),
.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup .el-menu-item.is-active .submenu-icon) {
  color: #0876a5 !important;
}

.modern-menu.el-menu--collapse :deep(.el-sub-menu .el-menu--popup .el-menu-item.is-active) {
  background: linear-gradient(135deg, #effcff 0%, #d8f5ff 100%) !important;
  border: 1px solid rgba(24, 185, 236, 0.34) !important;
  box-shadow: 0 10px 24px rgba(24, 185, 236, 0.12) !important;
}

/* Mobile layout repair: keep the drawer out of document flow. */
@media (max-width: 768px) {
  .main-layout {
    width: 100vw !important;
    max-width: 100vw !important;
    overflow: hidden !important;
  }

  .main-layout > .el-container {
    display: block !important;
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    overflow: hidden !important;
  }

  .main-layout > .el-container > .el-container {
    display: flex !important;
    flex-direction: column !important;
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    margin: 0 !important;
    transform: none !important;
  }

  .sidebar.mobile-sidebar {
    position: fixed !important;
    inset: 0 auto 0 0 !important;
    width: min(82vw, 260px) !important;
    max-width: min(82vw, 260px) !important;
    min-width: 0 !important;
    flex: 0 0 auto !important;
    margin: 0 !important;
    z-index: 2000 !important;
  }

  .sidebar.mobile-sidebar:not(.sidebar-open) {
    transform: translateX(-105%) !important;
  }

  .sidebar.mobile-sidebar.sidebar-open {
    transform: translateX(0) !important;
  }

  .modern-header {
    width: calc(100% - 20px) !important;
    max-width: calc(100% - 20px) !important;
    box-sizing: border-box !important;
    flex: 0 0 56px !important;
  }

  .main-content {
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    flex: 1 1 auto !important;
    box-sizing: border-box !important;
  }

  .content-wrapper {
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    box-sizing: border-box !important;
  }
}

@media (max-width: 480px) {
  .modern-header {
    width: calc(100% - 16px) !important;
    max-width: calc(100% - 16px) !important;
    margin: 8px 8px 0 !important;
  }

  .content-wrapper {
    padding: 12px 10px 22px !important;
  }
}
</style>
