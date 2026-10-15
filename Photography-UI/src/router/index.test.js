import { beforeEach, describe, expect, it, vi } from 'vitest'

const userStore = {
  role: '',
  roles: [],
  highestRoles: [],
  grantedRoles: [],
  isLoggedIn: false,
  canManageBusiness: false,
  hasAnyRole: vi.fn(requiredRoles => requiredRoles.some(role => {
    const normalized = role === 'ADMIN' ? 'SUPER_ADMIN' : role
    return userStore.grantedRoles.includes(normalized)
  })),
  checkTokenValidity: vi.fn(() => false)
}

const requestGet = vi.fn()

vi.mock('@/stores/user', () => ({ useUserStore: () => userStore }))
vi.mock('@/utils/request', () => ({ default: { get: requestGet } }))

const { resetMaintenanceStatusCache, setupRouterGuards } = await import('./index.js')

const installGuard = () => {
  let guard
  setupRouterGuards({ beforeEach: callback => { guard = callback } })
  return guard
}

const navigate = async (guard, to) => {
  const next = vi.fn()
  await guard({ matched: [], meta: {}, ...to }, {}, next)
  return next
}

describe('route access guard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    resetMaintenanceStatusCache()
    Object.assign(userStore, {
      role: '',
      roles: [],
      highestRoles: [],
      grantedRoles: [],
      isLoggedIn: false,
      canManageBusiness: false
    })
    userStore.checkTokenValidity.mockReturnValue(false)
    requestGet.mockResolvedValue({ data: { enabled: false, unlocked: false } })
  })

  it('redirects protected pages to the maintenance gate', async () => {
    requestGet.mockResolvedValue({ data: { enabled: true, unlocked: false } })
    const next = await navigate(installGuard(), { path: '/dashboard', fullPath: '/dashboard', meta: { requiresAuth: true } })
    expect(next).toHaveBeenCalledWith({ path: '/maintenance', query: { redirect: '/dashboard' } })
  })

  it('allows the public landing page without a maintenance status request', async () => {
    const next = await navigate(installGuard(), { path: '/', fullPath: '/', meta: { public: true, title: '校融媒体中心' } })
    expect(requestGet).not.toHaveBeenCalled()
    expect(next).toHaveBeenCalledWith()
  })

  it('redirects an expired protected session to login', async () => {
    const next = await navigate(installGuard(), { path: '/dashboard', fullPath: '/dashboard', meta: { requiresAuth: true } })
    expect(next).toHaveBeenCalledWith('/login')
  })

  it('rejects a role outside the route role matrix', async () => {
    Object.assign(userStore, {
      role: 'MEMBER',
      roles: ['MEMBER'],
      highestRoles: ['MEMBER'],
      grantedRoles: ['MEMBER'],
      isLoggedIn: true
    })
    userStore.checkTokenValidity.mockReturnValue(true)
    const next = await navigate(installGuard(), {
      path: '/devices/site-config', fullPath: '/devices/site-config',
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN'] }
    })
    expect(next).toHaveBeenCalledWith('/404')
  })

  it('allows a matching role and redirects authenticated login visits', async () => {
    Object.assign(userStore, {
      role: 'SUPER_ADMIN',
      roles: ['SUPER_ADMIN'],
      highestRoles: ['SUPER_ADMIN'],
      grantedRoles: ['SUPER_ADMIN', 'DIRECTOR', 'MINISTER'],
      isLoggedIn: true,
      canManageBusiness: true
    })
    userStore.checkTokenValidity.mockReturnValue(true)
    const guard = installGuard()
    const allowed = await navigate(guard, {
      path: '/devices/site-config', fullPath: '/devices/site-config',
      meta: { requiresAuth: true, roles: ['SUPER_ADMIN'] }
    })
    expect(allowed).toHaveBeenCalledWith()

    const redirected = await navigate(guard, { path: '/login', fullPath: '/login', meta: {} })
    expect(redirected).toHaveBeenCalledWith('/dashboard')
  })

  it('does not grant access from a lower non-effective role', async () => {
    Object.assign(userStore, {
      role: 'DIRECTOR',
      roles: ['DIRECTOR', 'ADVISOR'],
      highestRoles: ['DIRECTOR'],
      grantedRoles: ['DIRECTOR', 'MINISTER'],
      isLoggedIn: true,
      canManageBusiness: true
    })
    userStore.checkTokenValidity.mockReturnValue(true)

    const next = await navigate(installGuard(), {
      path: '/advisor-only',
      fullPath: '/advisor-only',
      meta: { requiresAuth: true, roles: ['ADVISOR'] }
    })

    expect(next).toHaveBeenCalledWith('/404')
  })
})
