import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 30_000,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json;charset=UTF-8' }
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use((response) => {
  if (response.config.responseType === 'blob') return response
  const data = response.data
  if (data && typeof data === 'object' && data.success === false) {
    if (!response.config.silent) ElMessage.error(data.message || '操作失败')
    return Promise.reject(new Error(data.message || '操作失败'))
  }
  return data
}, async (error) => {
  const config = error.config || {}
  const status = error.response?.status
  const message = error.response?.data?.message

  if (status === 401 && !config._retry && !config.skipAuthRefresh && !String(config.url).includes('/auth/')) {
    config._retry = true
    const userStore = useUserStore()
    if (await userStore.restoreSession()) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${userStore.token}`
      return request(config)
    }
  }

  if (status === 503 && message === 'MAINTENANCE_MODE') {
    const current = `${window.location.pathname}${window.location.search}`
    if (window.location.pathname !== '/' && window.location.pathname !== '/maintenance') {
      window.location.assign(`/maintenance?redirect=${encodeURIComponent(current)}`)
    }
  } else if (!config.silent) {
    if (status === 401) ElMessage.error('登录已过期，请重新登录')
    else if (status === 403) ElMessage.error('权限不足')
    else if (status === 404) ElMessage.error('请求的资源不存在')
    else if (error.code === 'ECONNABORTED') ElMessage.error('请求超时，请稍后重试')
    else ElMessage.error(message || error.message || '网络连接失败')
  }
  return Promise.reject(error)
})

export default request
