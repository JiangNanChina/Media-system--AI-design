import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const request = { post: vi.fn(), get: vi.fn() }
vi.mock('@/utils/request', () => ({ default: request }))

const { useUserStore } = await import('./user.js')

const token = payload => {
  const encoded = value => btoa(JSON.stringify(value)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')
  return `${encoded({ alg: 'none' })}.${encoded(payload)}.signature`
}

const activeLogin = (role = 'MEMBER', roles) => ({
  token: token({ exp: Math.floor(Date.now() / 1000) + 3600 }),
  userId: 7,
  username: 'member7',
  realName: '测试成员',
  email: '10001@qq.com',
  role,
  ...(roles ? { roles } : {}),
  accountStatus: 'ACTIVE',
  loginSessionId: 'login-session-7'
})

describe('user session store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('keeps the access token in memory and records an explicit login session', async () => {
    request.post.mockResolvedValue({ data: activeLogin('MINISTER') })
    const store = useUserStore()

    const result = await store.login({ username: 'member7', password: 'password-123' })

    expect(result.success).toBe(true)
    expect(request.post).toHaveBeenCalledWith(
      '/auth/login',
      { username: 'member7', password: 'password-123' },
      { skipAuthRefresh: true, silent: true }
    )
    expect(store.isLoggedIn).toBe(true)
    expect(store.isMinister).toBe(true)
    expect(store.userRole).toBe('部长')
    expect(localStorage.getItem('token')).toBeNull()
    expect(sessionStorage.getItem('loginSessionId')).toBe('login-session-7')
  })

  it('preserves the backend login rejection message', async () => {
    request.post.mockRejectedValue({
      response: {
        status: 400,
        data: { message: '用户名或密码错误' }
      }
    })
    const store = useUserStore()
    const credentials = { username: 'member7', password: 'wrong-password' }

    await expect(store.login(credentials)).resolves.toEqual({
      success: false,
      message: '用户名或密码错误'
    })
    expect(request.post).toHaveBeenCalledWith(
      '/auth/login',
      credentials,
      { skipAuthRefresh: true, silent: true }
    )
  })

  it('restores a rotating refresh-cookie session without creating a new login popup session', async () => {
    request.post.mockResolvedValue({ data: activeLogin('ADVISOR') })
    const store = useUserStore()

    await expect(store.restoreSession()).resolves.toBe(true)

    expect(store.role).toBe('ADVISOR')
    expect(sessionStorage.getItem('loginSessionId')).toBeNull()
  })

  it('uses all tied highest roles while keeping MINISTER as the legacy role', async () => {
    request.post.mockResolvedValue({
      data: activeLogin('MINISTER', ['MEMBER', 'ADVISOR', 'MINISTER'])
    })
    const store = useUserStore()

    await store.login({ username: 'member7', password: 'password-123' })

    expect(store.roles).toEqual(['MINISTER', 'ADVISOR', 'MEMBER'])
    expect(store.highestRoles).toEqual(['MINISTER', 'ADVISOR'])
    expect(store.role).toBe('MINISTER')
    expect(store.isMinister).toBe(true)
    expect(store.isAdvisor).toBe(true)
    expect(store.canManageBusiness).toBe(true)
    expect(store.userRole).toBe('部长 / 指导老师')
  })

  it('ignores lower explicit roles when evaluating permissions', async () => {
    request.post.mockResolvedValue({
      data: activeLogin('DIRECTOR', ['ADVISOR', 'DIRECTOR'])
    })
    const store = useUserStore()

    await store.login({ username: 'director7', password: 'password-123' })

    expect(store.roles).toEqual(['DIRECTOR', 'ADVISOR'])
    expect(store.highestRoles).toEqual(['DIRECTOR'])
    expect(store.hasAnyRole(['MINISTER'])).toBe(true)
    expect(store.hasAnyRole(['ADVISOR'])).toBe(false)
    expect(store.isAdvisor).toBe(false)
  })

  it('normalizes a legacy ADMIN identity to SUPER_ADMIN', async () => {
    request.post.mockResolvedValue({ data: activeLogin('ADMIN') })
    const store = useUserStore()

    await store.login({ username: 'legacy-admin', password: 'password-123' })

    expect(store.roles).toEqual(['SUPER_ADMIN'])
    expect(store.role).toBe('SUPER_ADMIN')
    expect(store.isSuperAdmin).toBe(true)
    expect(store.hasAnyRole(['DIRECTOR'])).toBe(true)
  })

  it('clears stale local identity when refresh fails', async () => {
    localStorage.setItem('userInfo', JSON.stringify({ username: 'stale' }))
    request.post.mockRejectedValue(new Error('expired'))
    const store = useUserStore()

    await expect(store.restoreSession()).resolves.toBe(false)

    expect(store.userInfo).toBeNull()
    expect(localStorage.getItem('userInfo')).toBeNull()
  })

  it('revokes the server session and always clears local state on logout', async () => {
    request.post.mockResolvedValueOnce({ data: activeLogin() }).mockRejectedValueOnce(new Error('offline'))
    const store = useUserStore()
    await store.login({ username: 'member7', password: 'password-123' })

    await store.logout()

    expect(store.token).toBe('')
    expect(store.userInfo).toBeNull()
    expect(sessionStorage.getItem('loginSessionId')).toBeNull()
  })
})
