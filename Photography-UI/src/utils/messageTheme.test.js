import { afterEach, describe, expect, it, vi } from 'vitest'
import { applyMessageTheme, resolveMessageTheme, setupMessageTheme } from './messageTheme'

describe('message theme routing', () => {
  afterEach(() => {
    delete document.body.dataset.messageTheme
    window.history.replaceState({}, '', '/')
  })

  it.each([
    ['/', 'landing'],
    ['/join-us', 'join'],
    ['/join-us/', 'join'],
    ['/submission', 'submission'],
    ['/login', 'auth'],
    ['/register', 'auth'],
    ['/forgot-password', 'recovery'],
    ['/dashboard', 'admin'],
    ['/devices/admin', 'admin']
  ])('maps %s to the %s message theme', (path, theme) => {
    expect(resolveMessageTheme(path)).toBe(theme)
  })

  it('updates the body theme initially and after navigation', () => {
    window.history.replaceState({}, '', '/join-us')
    let afterEachHandler
    const router = { afterEach: vi.fn(handler => { afterEachHandler = handler }) }

    setupMessageTheme(router)
    expect(document.body.dataset.messageTheme).toBe('join')

    afterEachHandler({ path: '/dashboard' })
    expect(document.body.dataset.messageTheme).toBe('admin')
  })

  it('returns the applied theme for non-browser callers', () => {
    expect(applyMessageTheme('/forgot-password')).toBe('recovery')
    expect(document.body.dataset.messageTheme).toBe('recovery')
  })
})
