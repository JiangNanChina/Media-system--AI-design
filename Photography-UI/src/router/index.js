import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

// 页面组件懒加载
const Auth = () => import('@/views/Login.vue')  // 现在Login.vue是统一的认证页面
const Layout = () => import('@/components/Layout.vue')
const Dashboard = () => import('@/views/Dashboard.vue')
const UserManagement = () => import('@/views/UserManagement.vue')
const DepartmentManagement = () => import('@/views/DepartmentManagement.vue')
const EquipmentManagement = () => import('@/views/EquipmentManagement.vue')
const CategoryManagement = () => import('@/views/CategoryManagement.vue')
const BorrowManagement = () => import('@/views/BorrowManagement.vue')
const AnnouncementManagement = () => import('@/views/AnnouncementManagement.vue')
const AnnouncementDetail = () => import('@/views/AnnouncementDetail.vue')
const StudyCheckin = () => import('@/views/StudyCheckin.vue')
const CheckinManagement = () => import('@/views/CheckinManagement.vue')
const CheckinRecords = () => import('@/views/CheckinRecords.vue')
const QRGenerator = () => import('@/views/QRGenerator.vue')
const CheckinAudit = () => import('@/views/CheckinAudit.vue')
const DutyManagement = () => import('@/views/DutyManagement.vue')
const DutyCheckin = () => import('@/views/DutyCheckin.vue')
const DutyStatistics = () => import('@/views/DutyStatistics.vue')
const LeaveManagement = () => import('@/views/LeaveManagement.vue')
const DeviceManagement = () => import('@/views/DeviceManagement.vue')
const MyDevices = () => import('@/views/MyDevices.vue')
const SiteConfigManagement = () => import('@/views/SiteConfigManagement.vue')
const Profile = () => import('@/views/Profile.vue')
const NotFound = () => import('@/views/NotFound.vue')
const LoginTest = () => import('@/views/LoginTest.vue')
const Landing = () => import('@/views/Landing.vue')
const VideoSubmission = () => import('@/views/VideoSubmission.vue')
const ForgotPassword = () => import('@/views/ForgotPassword.vue')
const Maintenance = () => import('@/views/Maintenance.vue')
const SubmissionManagement = () => import('@/views/SubmissionManagement.vue')
const LandingManagement = () => import('@/views/LandingManagement.vue')
const AccountReview = () => import('@/views/AccountReview.vue')

const routes = [
  {
    path: '/',
    name: 'Landing',
    component: Landing,
    meta: { public: true, title: '校融媒体中心' }
  },
  {
    path: '/submission',
    name: 'VideoSubmission',
    component: VideoSubmission,
    meta: { public: true, title: '视频投稿' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPassword,
    meta: { public: true, title: '找回密码' }
  },
  {
    path: '/maintenance',
    name: 'Maintenance',
    component: Maintenance,
    meta: { public: true, title: '系统维护' }
  },
  {
    path: '/login',
    name: 'Login',
    component: Auth,
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Auth,
    meta: { requiresAuth: false, title: '注册', mode: 'register' }
  },
  {
    path: '/login-test',
    name: 'LoginTest',
    component: LoginTest,
    meta: { requiresAuth: false, title: '登录测试' }
  },
  {
    path: '/dashboard',
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '首页', icon: 'Odometer' }
      }
    ]
  },
  {
    path: '/profile',
    component: Layout,
    meta: { requiresAuth: true },
    children: [{ path: '', name: 'Profile', component: Profile, meta: { title: '个人中心', hidden: true } }]
  },
  {
    path: '/user',
    component: Layout,
    redirect: '/user/list',
    meta: { requiresAuth: true, roles: ['MINISTER', 'DIRECTOR', 'SUPER_ADMIN', 'ADMIN'], title: '用户管理', icon: 'UserFilled' },
    children: [
      {
        path: 'list',
        name: 'UserManagement',
        component: UserManagement,
        meta: { title: '用户列表', icon: 'User' }
      },
      {
        path: 'review',
        name: 'AccountReview',
        component: AccountReview,
        meta: { title: '注册审核', icon: 'CircleCheck', roles: ['SUPER_ADMIN', 'ADMIN'] }
      }
    ]
  },
  {
    path: '/department',
    component: Layout,
    redirect: '/department/list',
    meta: { requiresAuth: true, roles: ['DIRECTOR', 'SUPER_ADMIN', 'ADMIN'], title: '部门管理', icon: 'OfficeBuilding' },
    children: [
      {
        path: 'list',
        name: 'DepartmentManagement',
        component: DepartmentManagement,
        meta: { title: '部门列表', icon: 'Management' }
      }
    ]
  },
  {
    path: '/equipment',
    component: Layout,
    redirect: '/equipment/list',
    meta: { requiresAuth: true, title: '设备管理', icon: 'Camera' },
    children: [
      {
        path: 'list',
        name: 'EquipmentManagement',
        component: EquipmentManagement,
        meta: { title: '设备列表', icon: 'List' }
      },
      {
        path: 'categories',
        name: 'CategoryManagement',
        component: CategoryManagement,
        meta: { title: '分类管理', icon: 'Collection', requiresAdmin: true }
      }
    ]
  },
  {
    path: '/borrow',
    component: Layout,
    redirect: '/borrow/list',
    meta: { requiresAuth: true, title: '借还管理', icon: 'Box' },
    children: [
      {
        path: 'list',
        name: 'BorrowManagement',
        component: BorrowManagement,
        meta: { title: '借还记录', icon: 'DocumentCopy' }
      }
    ]
  },
  {
    path: '/announcement',
    component: Layout,
    redirect: '/announcement/list',
    meta: { requiresAuth: true, requiresAdmin: true, title: '公告管理', icon: 'Bell' },
    children: [
      {
        path: 'list',
        name: 'AnnouncementManagement',
        component: AnnouncementManagement,
        meta: { title: '公告列表', icon: 'Document' }
      },
      {
        path: ':id',
        name: 'AnnouncementDetail',
        component: AnnouncementDetail,
        meta: { title: '公告详情', icon: 'Document', hidden: true }
      }
    ]
  },
  {
    path: '/checkin',
    component: Layout,
    redirect: '/checkin/main',
    meta: { requiresAuth: true, title: '晚自习打卡', icon: 'Clock' },
    children: [
      {
        path: 'main',
        name: 'CheckinManagement',
        component: CheckinManagement,
        meta: { title: '打卡主页', icon: 'CircleCheck' }
      },
      {
        path: 'records',
        name: 'CheckinRecords',
        component: CheckinRecords,
        meta: { title: '打卡记录', icon: 'DocumentCopy' }
      },
      {
        path: 'qr-generator',
        name: 'QRGenerator',
        component: QRGenerator,
        meta: { title: '二维码生成', icon: 'Grid', requiresAdmin: true }
      },
      {
        path: 'audit',
        name: 'CheckinAudit',
        component: CheckinAudit,
        meta: { title: '签到审核', icon: 'DocumentChecked', requiresAdmin: true }
      },
      {
        path: 'records/:id',
        name: 'CheckinRecordDetail',
        component: CheckinRecords,
        meta: { title: '记录详情', icon: 'Document', hidden: true }
      },
      {
        path: 'configuration',
        name: 'CheckinConfiguration',
        component: () => import('@/views/CheckinConfiguration.vue'),
        meta: { title: '打卡配置', icon: 'Setting', requiresAdmin: true }
      },
      {
        path: 'attendance',
        name: 'AttendanceStatistics',
        component: () => import('@/views/AttendanceStatistics.vue'),
        meta: { title: '考勤统计', icon: 'DataAnalysis', requiresAdmin: true }
      },
      {
        path: 'test',
        name: 'CheckinTest',
        component: () => import('@/views/CheckinTest.vue'),
        meta: { title: 'API测试', icon: 'Tools', hidden: true }
      }
    ]
  },
  {
    path: '/duty',
    component: Layout,
    redirect: '/duty/checkin',
    meta: { requiresAuth: true, title: '办公执勤', icon: 'OfficeBuilding' },
    children: [
      {
        path: 'checkin',
        name: 'DutyCheckin',
        component: DutyCheckin,
        meta: { title: '我的执勤', icon: 'UserFilled' }
      },
      {
        path: 'list',
        name: 'DutyManagement',
        component: DutyManagement,
        meta: { title: '执勤管理', icon: 'Calendar', requiresAdmin: true }
      },
      {
        path: 'records',
        name: 'DutyRecords',
        component: () => import('@/views/DutyRecords.vue'),
        meta: { title: '执勤记录', icon: 'DocumentCopy' }
      },
      {
        path: 'statistics',
        name: 'DutyStatistics',
        component: DutyStatistics,
        meta: { title: '执勤统计', icon: 'TrendCharts', requiresAdmin: true }
      }
    ]
  },
  {
    path: '/leave',
    component: Layout,
    redirect: '/leave/list',
    meta: { requiresAuth: true, title: '请假管理', icon: 'DocumentRemove' },
    children: [
      {
        path: 'list',
        name: 'LeaveManagement',
        component: LeaveManagement,
        meta: { title: '请假申请', icon: 'Edit' }
      }
    ]
  },
  {
    path: '/devices',
    component: Layout,
    redirect: '/devices/my',
    meta: { requiresAuth: true, title: '设备管理', icon: 'Monitor' },
    children: [
      {
        path: 'my',
        name: 'MyDevices',
        component: MyDevices,
        meta: { title: '我的设备', icon: 'Monitor' }
      },
      {
        path: 'admin',
        name: 'DeviceManagement',
        component: DeviceManagement,
        meta: { title: '设备管理', icon: 'Setting', roles: ['SUPER_ADMIN', 'ADMIN'] }
      },
      {
        path: 'site-config',
        name: 'SiteConfigManagement',
        component: SiteConfigManagement,
        meta: { title: '站点配置', icon: 'Tools', roles: ['SUPER_ADMIN', 'ADMIN'] }
      },
      {
        path: 'landing-config',
        name: 'LandingManagement',
        component: LandingManagement,
        meta: { title: '落地页设置', icon: 'Picture', roles: ['SUPER_ADMIN', 'ADMIN'] }
      }
    ]
  },
  {
    path: '/submission-management',
    component: Layout,
    meta: {
      requiresAuth: true,
      roles: ['MINISTER', 'DIRECTOR', 'SUPER_ADMIN', 'ADMIN'],
      title: '投稿管理',
      icon: 'VideoCamera'
    },
    children: [{
      path: '',
      name: 'SubmissionManagement',
      component: SubmissionManagement,
      meta: { title: '视频投稿', icon: 'VideoCamera' }
    }]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: NotFound,
    meta: { hidden: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true }
  }
]

export default routes

// 路由守卫
export const setupRouterGuards = (router) => {
  router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()
    
    // 设置页面标题
    if (to.meta.title) {
      // 尝试从缓存或默认获取站点标题
      const siteTitle = localStorage.getItem('siteTitle') || '融媒体管理系统'
      document.title = `${to.meta.title} - ${siteTitle}`
    }
    
    if (!to.meta.public && to.path !== '/maintenance') {
      try {
        const maintenance = await request.get('/maintenance/public/status', { silent: true })
        if (maintenance.data?.enabled && !maintenance.data?.unlocked) {
          next({ path: '/maintenance', query: { redirect: to.fullPath } })
          return
        }
      } catch {
        // The target request will surface connectivity errors.
      }
    }

    // 检查是否需要登录
    if (to.meta.requiresAuth) {
      // 检查token有效性
      if (!userStore.checkTokenValidity()) {
        console.log('Token无效或过期，跳转到登录页')
        next('/login')
        return
      }
      
      // 双重检查：isLoggedIn计算属性也会检查token过期
      if (!userStore.isLoggedIn) {
        next('/login')
        return
      }
    }
    
    const requiredRoles = to.meta.roles || to.matched.flatMap(record => record.meta.roles || [])
    if (requiredRoles.length && !requiredRoles.includes(userStore.role)) {
      next('/404')
      return
    }

    if (to.meta.requiresAdmin && !userStore.canManageBusiness) {
      next('/404')
      return
    }
    
    // 已登录用户访问登录页或注册页，重定向到首页
    if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn) {
      next('/dashboard')
      return
    }
    
    next()
  })
}
