import { describe, expect, it } from 'vitest'
import { getRequestErrorMessage } from './requestError'

describe('getRequestErrorMessage', () => {
  it('explains an upstream 413 response instead of showing the Axios error', () => {
    expect(getRequestErrorMessage({
      status: 413,
      fallbackMessage: 'Request failed with status code 413'
    })).toBe('上传失败：服务器当前允许的文件大小低于页面标注，请联系管理员检查上传限制')
  })

  it('keeps a structured backend message for an oversized upload', () => {
    expect(getRequestErrorMessage({
      status: 413,
      serverMessage: '上传文件超过服务器允许的大小'
    })).toBe('上传文件超过服务器允许的大小')
  })

  it('keeps the existing authentication and timeout messages', () => {
    expect(getRequestErrorMessage({ status: 401 })).toBe('登录已过期，请重新登录')
    expect(getRequestErrorMessage({ errorCode: 'ECONNABORTED' })).toBe('请求超时，请稍后重试')
  })
})
