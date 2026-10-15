import { describe, expect, it } from 'vitest'
import { usesArchiveRouteTransition } from './routeTransition.js'

describe('route transition theme', () => {
  it.each([
    '/',
    '/join-us',
    '/submission',
    '/login',
    '/register',
    '/forgot-password',
    '/maintenance'
  ])('keeps the archive transition on %s', (path) => {
    expect(usesArchiveRouteTransition(path)).toBe(true)
  })

  it.each([
    '/dashboard',
    '/user/list',
    '/equipment/list',
    '/submission-management',
    '/login-test'
  ])('uses the admin transition on %s', (path) => {
    expect(usesArchiveRouteTransition(path)).toBe(false)
  })

  it('normalizes query strings, hashes, and trailing slashes', () => {
    expect(usesArchiveRouteTransition('/login/?redirect=%2Fdashboard#form')).toBe(true)
  })
})
