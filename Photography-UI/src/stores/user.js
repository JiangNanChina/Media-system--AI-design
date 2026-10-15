import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import request from '@/utils/request'
import {
  getGrantedRoles,
  getHighestRole,
  getHighestRoles,
  getUserRoles,
  hasAnyGrantedRole,
  roleLabel
} from '@/utils/userRoles'

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

const normalizeUserInfo = value => {
  if (!value) return null
  const roles = getUserRoles(value)
  return {
    ...value,
    roles,
    role: getHighestRole(roles)
  }
}

const readUserInfo = () => {
  try {
    const value = localStorage.getItem('userInfo')
    return value ? normalizeUserInfo(JSON.parse(value)) : null
  } catch {
    localStorage.removeItem('userInfo')
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref('')
  const userInfo = ref(readUserInfo())
  const restoring = ref(false)

  const roles = computed(() => getUserRoles(userInfo.value))
  const highestRoles = computed(() => getHighestRoles(roles.value))
  const grantedRoles = computed(() => getGrantedRoles(roles.value))
  const role = computed(() => getHighestRole(roles.value))
  const isLoggedIn = computed(() => Boolean(token.value) && !isTokenExpired(token.value))
  const hasAnyRole = requiredRoles => hasAnyGrantedRole(roles.value, requiredRoles)
  const hasRole = requiredRole => hasAnyRole([requiredRole])
  const isSuperAdmin = computed(() => highestRoles.value.includes('SUPER_ADMIN'))
  const isDirector = computed(() => highestRoles.value.includes('DIRECTOR'))
  const isMinister = computed(() => highestRoles.value.includes('MINISTER'))
  const isAdvisor = computed(() => highestRoles.value.includes('ADVISOR'))
  const canManageBusiness = computed(() => hasRole('MINISTER'))
  const canManageUsers = computed(() => hasRole('DIRECTOR'))
  const canManageSite = computed(() => hasRole('SUPER_ADMIN'))
  const isAdmin = computed(() => canManageBusiness.value)
  const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
  const userRole = computed(() => highestRoles.value.map(roleLabel).join(' / ') || '部员')

  const applyLoginData = (data, explicitLogin = false) => {
    token.value = data.token || ''
    userInfo.value = normalizeUserInfo({
      id: data.userId,
      username: data.username,
      realName: data.realName,
      email: data.email,
      roles: data.roles,
      role: data.role,
      departmentName: data.departmentName,
      avatar: data.avatarUrl,
      accountStatus: data.accountStatus,
      createdAt: data.createdAt
    })
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
      // Login owns the user-facing failure message; keep the request interceptor quiet
      // so a rejected login is not announced a second time.
      const response = await request.post('/auth/login', form, { skipAuthRefresh: true, silent: true })
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
    userInfo.value = normalizeUserInfo({ ...userInfo.value, ...value })
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  const refreshUserInfo = async () => {
    const response = await request.get('/users/profile')
    if (response.data) updateUserInfo({ ...response.data, avatar: response.data.avatarUrl })
  }

  return {
    token, userInfo, restoring, roles, highestRoles, grantedRoles, role, isLoggedIn,
    isAdmin, isSuperAdmin, isDirector, isMinister, isAdvisor,
    canManageBusiness, canManageUsers, canManageSite, userName, userRole, hasRole, hasAnyRole,
    login, logout, restoreSession, clearSession, checkTokenValidity, updateUserInfo, refreshUserInfo
  }
})
