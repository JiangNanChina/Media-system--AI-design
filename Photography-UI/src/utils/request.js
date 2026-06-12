import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api', // 通过nginx代理访问后端API
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    try {
      const userStore = useUserStore()
      
    // 只对非登录和公开API相关的请求检查token有效性
    const isAuthRequest = config.url?.includes('/auth/') || config.url?.includes('/login') || config.url?.includes('/register')
    const isPublicRequest = config.url?.includes('/public/') || config.url?.endsWith('/public') // 修复：支持 /public 结尾和 /public/ 路径
    const isDepartmentListRequest = config.url?.includes('/departments/list') // 部门列表公开API
    const isEquipmentCategoriesRequest = config.url?.includes('/equipment-categories/active') // 设备分类公开API
    const shouldSkipTokenCheck = isAuthRequest || isPublicRequest || isDepartmentListRequest || isEquipmentCategoriesRequest
      
      if (!shouldSkipTokenCheck) {
        // 检查token有效性
        if (!userStore.checkTokenValidity()) {
          // Token无效或过期，跳转到登录页
          try {
            const currentPath = router.currentRoute?.value?.path
            if (currentPath && currentPath !== '/login') {
              router.push('/login')
            }
          } catch (routerError) {
            console.warn('路由跳转失败:', routerError)
          }
          return Promise.reject(new Error('Token已过期，请重新登录'))
        }
        
        // 如果有token，添加到请求头
        if (userStore.token) {
          config.headers.Authorization = `Bearer ${userStore.token}`
        }
      }
      
      return config
    } catch (error) {
      console.error('请求拦截器错误:', error)
      return config
    }
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const { data } = response
    
    // 如果是文件下载响应，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 检查响应数据结构
    if (data && typeof data === 'object') {
      // 如果是成功响应
      if (data.success !== false) {
        return data
      } else {
        // 业务错误
        ElMessage.error(data.message || '操作失败')
        return Promise.reject(new Error(data.message || '操作失败'))
      }
    }
    
    return data
  },
  error => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败 (${status})`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else {
      ElMessage.error('网络连接失败，请检查网络设置')
    }
    
    return Promise.reject(error)
  }
)

export default request
