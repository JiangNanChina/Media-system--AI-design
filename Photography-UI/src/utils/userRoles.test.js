import { describe, expect, it } from 'vitest'
import {
  getGrantedRoles,
  getHighestRole,
  getHighestRoles,
  getUserRoles,
  hasAnyGrantedRole,
  normalizeRoles,
  sameRoles
} from './userRoles'

describe('user role utilities', () => {
  it('normalizes legacy ADMIN values, removes duplicates, and sorts roles by rank', () => {
    expect(normalizeRoles(['MEMBER', 'ADMIN', 'DIRECTOR', 'SUPER_ADMIN'])).toEqual([
      'SUPER_ADMIN',
      'DIRECTOR',
      'MEMBER'
    ])
  })

  it('falls back to the legacy role field when roles are absent', () => {
    expect(getUserRoles({ role: 'ADMIN' })).toEqual(['SUPER_ADMIN'])
    expect(getUserRoles({ roles: ['ADVISOR', 'MEMBER'], role: 'DIRECTOR' })).toEqual(['ADVISOR', 'MEMBER'])
  })

  it('keeps tied highest roles and uses MINISTER as the legacy single-role value', () => {
    const roles = ['MEMBER', 'ADVISOR', 'MINISTER']

    expect(getHighestRoles(roles)).toEqual(['MINISTER', 'ADVISOR'])
    expect(getHighestRole(roles)).toBe('MINISTER')
  })

  it('unions tied-role grants without promoting ADVISOR to MINISTER', () => {
    expect(getGrantedRoles(['MINISTER', 'ADVISOR', 'MEMBER'])).toEqual(['MINISTER', 'ADVISOR'])
    expect(hasAnyGrantedRole(['ADVISOR', 'MEMBER'], ['MINISTER'])).toBe(false)
    expect(hasAnyGrantedRole(['MINISTER', 'ADVISOR'], ['MINISTER'])).toBe(true)
  })

  it('applies only the explicit management inheritance chain', () => {
    expect(getGrantedRoles(['DIRECTOR', 'ADVISOR'])).toEqual(['DIRECTOR', 'MINISTER'])
    expect(hasAnyGrantedRole(['DIRECTOR', 'ADVISOR'], ['MINISTER'])).toBe(true)
    expect(hasAnyGrantedRole(['DIRECTOR', 'ADVISOR'], ['ADVISOR'])).toBe(false)
    expect(hasAnyGrantedRole(['SUPER_ADMIN'], ['DIRECTOR'])).toBe(true)
  })

  it('compares role sets independent of input order and legacy aliases', () => {
    expect(sameRoles(['MEMBER', 'ADMIN'], ['SUPER_ADMIN', 'MEMBER'])).toBe(true)
    expect(sameRoles(['MINISTER'], ['MINISTER', 'ADVISOR'])).toBe(false)
  })
})
