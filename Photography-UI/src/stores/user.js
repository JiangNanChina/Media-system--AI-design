import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

// JWT token解码函数
const parseJwt = (token) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error('Token解析失败:', error)
    return null
  }
}

// 检查token是否过期
const isTokenExpired = (token) => {
  if (!token) return true
  
  try {
    const payload = parseJwt(token)
    if (!payload || !payload.exp) return true
    
    // exp是秒级时间戳，需要转换为毫秒
    const expirationTime = payload.exp * 1000
    const currentTime = Date.now()
    
    // 提前5分钟判定为过期，避免请求时才发现过期
    const bufferTime = 5 * 60 * 1000 // 5分钟
    
    return currentTime >= (expirationTime - bufferTime)
  } catch (error) {
    console.error('Token过期检查失败:', error)
    return true
  }
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  
  // 安全解析localStorage中的用户信息
  const getUserInfoFromStorage = () => {
    try {
      const stored = localStorage.getItem('userInfo')
      return stored && stored !== 'undefined' ? JSON.parse(stored) : null
    } catch (error) {
      console.warn('Failed to parse userInfo from localStorage:', error)
      localStorage.removeItem('userInfo') // 清除无效数据
      return null
    }
  }
  
  const userInfo = ref(getUserInfoFromStorage())
  
  // 计算属性
  const isLoggedIn = computed(() => {
    // 检查token是否存在且未过期
    return !!token.value && !isTokenExpired(token.value)
  })
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
  const userRole = computed(() => {
    const role = userInfo.value?.role
    return role === 'ADMIN' ? '管理员' : '成员'
  })

  // 检查token有效性
  const checkTokenValidity = () => {
    // 如果没有token，返回false（表示无效）
    if (!token.value) {
      return false
    }
    
    // 如果token过期，自动登出并返回false
    if (isTokenExpired(token.value)) {
      console.log('Token已过期，自动登出')
      logout()
      return false
    }
    
    // token存在且未过期
    return true
  }
  
  // 登录
  const login = async (loginForm) => {
    try {
      const response = await request.post('/auth/login', loginForm)
      
      if (response.success !== false && response.data) {
        const loginData = response.data
        
        // 保存token和用户信息
        token.value = loginData.token
        userInfo.value = {
          id: loginData.userId,
          username: loginData.username,
          realName: loginData.realName,
          email: loginData.email,
          role: loginData.role,
          departmentName: loginData.departmentName,
          avatar: loginData.avatarUrl,
          createdAt: loginData.createdAt
        }
        
        // 持久化存储
        localStorage.setItem('token', loginData.token)
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        
        return { success: true, data: response.data }
      } else {
        return { success: false, message: response.message || '登录失败' }
      }
    } catch (error) {
      console.error('登录错误:', error)
      return { success: false, message: error.message || '登录失败' }
    }
  }
  
  // 登出
  const logout = async () => {
    try {
      // JWT是无状态的，只需清除本地数据即可
      // 后端不需要登出接口，token过期后自然失效
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      
      console.log('用户已成功登出')
    } catch (error) {
      console.error('登出失败:', error)
    }
  }
  
  // 更新用户信息
  const updateUserInfo = (newUserInfo) => {
    userInfo.value = { ...userInfo.value, ...newUserInfo }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }
  
  // 刷新用户信息
  const refreshUserInfo = async () => {
    try {
      const response = await request.get('/users/profile')
      if (response.success !== false && response.data) {
        const profileData = response.data
        // 映射字段，确保avatar字段正确
        const mappedData = {
          ...profileData,
          avatar: profileData.avatarUrl, // 将avatarUrl映射为avatar
          departmentName: profileData.departmentName
        }
        updateUserInfo(mappedData)
      }
    } catch (error) {
      console.error('刷新用户信息失败:', error)
    }
  }
  
  return {
    // 状态
    token,
    userInfo,
    // 计算属性
    isLoggedIn,
    isAdmin,
    userName,
    userRole,
    // 方法
    login,
    logout,
    updateUserInfo,
    refreshUserInfo,
    checkTokenValidity
  }
})
