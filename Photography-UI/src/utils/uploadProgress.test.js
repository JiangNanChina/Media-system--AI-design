import { describe, expect, it } from 'vitest'
import { getUploadPercentage } from './uploadProgress'

describe('getUploadPercentage', () => {
  it('calculates a rounded percentage from uploaded bytes', () => {
    expect(getUploadPercentage({ loaded: 513, total: 1024 })).toBe(50)
  })

  it('uses the normalized progress value when the total size is unavailable', () => {
    expect(getUploadPercentage({ progress: 0.375 })).toBe(38)
  })

  it('keeps browser progress values within the visible range', () => {
    expect(getUploadPercentage({ loaded: 120, total: 100 })).toBe(100)
    expect(getUploadPercentage({ progress: -0.1 })).toBe(0)
  })

  it('ignores events without usable progress information', () => {
    expect(getUploadPercentage()).toBeNull()
    expect(getUploadPercentage({ loaded: 10, total: 0 })).toBeNull()
  })
})
