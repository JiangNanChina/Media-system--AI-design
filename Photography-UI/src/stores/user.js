import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import request from '@/utils/request'

const parseJwt = (token) => {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(decodeURIComponent(atob(payload).split('').map(char =>
      `%${(`00${char.charCodeAt(0).toString(16)}`).slice(-2)}`
    ).join('')))
  } catch {
    return null
  }
}

const isTokenExpired = (token) => {
  const payload = token ? parseJwt(token) : null
  return !payload?.exp || Date.now() >= payload.exp * 1000 - 30_000
}

const readUserInfo = () => {
  try {
    const value = localStorage.getItem('userInfo')
    return value ? JSON.parse(value) : null
  } catch {
    localStorage.removeItem('userInfo')
    return null
  }
}

const roleLabels = {
  MEMBER: '部员',
  MINISTER: '部长',
  DIRECTOR: '主任',
  ADVISOR: '指导老师',
  SUPER_ADMIN: '系统超级管理员',
  ADMIN: '管理员'
}

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userInfo = ref(readUserInfo())
  const restoring = ref(false)

  const role = computed(() => userInfo.value?.role || '')
  const isLoggedIn = computed(() => Boolean(token.value) && !isTokenExpired(token.value))
  const isSuperAdmin = computed(() => ['SUPER_ADMIN', 'ADMIN'].includes(role.value))
  const isDirector = computed(() => role.value === 'DIRECTOR')
  const isMinister = computed(() => role.value === 'MINISTER')
  const canManageBusiness = computed(() => isMinister.value || isDirector.value || isSuperAdmin.value)
  const canManageUsers = computed(() => isDirector.value || isSuperAdmin.value)
  const canManageSite = computed(() => isSuperAdmin.value)
  const isAdmin = computed(() => canManageBusiness.value)
  const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
  const userRole = computed(() => roleLabels[role.value] || '成员')

  const applyLoginData = (data, explicitLogin = false) => {
    token.value = data.token || ''
    userInfo.value = {
      id: data.userId,
      username: data.username,
      realName: data.realName,
      email: data.email,
      role: data.role,
      departmentName: data.departmentName,
      avatar: data.avatarUrl,
      accountStatus: data.accountStatus,
      createdAt: data.createdAt
    }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    if (explicitLogin && data.loginSessionId) {
      sessionStorage.setItem('loginSessionId', data.loginSessionId)
      sessionStorage.removeItem(`announcementShown:${data.loginSessionId}`)
    }
  }

  const clearSession = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('userInfo')
    sessionStorage.removeItem('loginSessionId')
  }

  const login = async (form) => {
    try {
      const response = await request.post('/auth/login', form, { skipAuthRefresh: true })
      applyLoginData(response.data, true)
      return { success: true, data: response.data }
    } catch (error) {
      return { success: false, message: error.response?.data?.message || error.message || '登录失败' }
    }
  }

  const restoreSession = async () => {
    if (restoring.value) return false
    restoring.value = true
    try {
      const response = await request.post('/auth/refresh', null, { skipAuthRefresh: true, silent: true })
      applyLoginData(response.data)
      return true
    } catch {
      clearSession()
      return false
    } finally {
      restoring.value = false
    }
  }

  const logout = async () => {
    try {
      await request.post('/auth/logout', null, { skipAuthRefresh: true, silent: true })
    } catch {
      // Local cleanup must still happen when the server is unavailable.
    }
    clearSession()
  }

  const checkTokenValidity = () => {
    if (!token.value || isTokenExpired(token.value)) {
      token.value = ''
      return false
    }
    return true
  }

  const updateUserInfo = (value) => {
    userInfo.value = { ...userInfo.value, ...value }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  const refreshUserInfo = async () => {
    const response = await request.get('/users/profile')
    if (response.data) updateUserInfo({ ...response.data, avatar: response.data.avatarUrl })
  }

  return {
    token, userInfo, restoring, role, isLoggedIn, isAdmin, isSuperAdmin, isDirector, isMinister,
    canManageBusiness, canManageUsers, canManageSite, userName, userRole,
    login, logout, restoreSession, clearSession, checkTokenValidity, updateUserInfo, refreshUserInfo
  }
})
