import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const request = { post: vi.fn(), get: vi.fn() }
vi.mock('@/utils/request', () => ({ default: request }))

const { useUserStore } = await import('./user.js')

const token = payload => {
  const encoded = value => btoa(JSON.stringify(value)).replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_')
  return `${encoded({ alg: 'none' })}.${encoded(payload)}.signature`
}

const activeLogin = (role = 'MEMBER') => ({
  token: token({ exp: Math.floor(Date.now() / 1000) + 3600 }),
  userId: 7,
  username: 'member7',
  realName: '测试成员',
  email: '10001@qq.com',
  role,
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
    expect(store.isLoggedIn).toBe(true)
    expect(store.isMinister).toBe(true)
    expect(store.userRole).toBe('部长')
    expect(localStorage.getItem('token')).toBeNull()
    expect(sessionStorage.getItem('loginSessionId')).toBe('login-session-7')
  })

  it('restores a rotating refresh-cookie session without creating a new login popup session', async () => {
    request.post.mockResolvedValue({ data: activeLogin('ADVISOR') })
    const store = useUserStore()

    await expect(store.restoreSession()).resolves.toBe(true)

    expect(store.role).toBe('ADVISOR')
    expect(sessionStorage.getItem('loginSessionId')).toBeNull()
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

