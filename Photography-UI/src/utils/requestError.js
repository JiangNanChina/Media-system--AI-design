const PAYLOAD_TOO_LARGE_MESSAGE = '上传失败：服务器当前允许的文件大小低于页面标注，请联系管理员检查上传限制'

export const getRequestErrorMessage = ({
  status,
  serverMessage,
  errorCode,
  fallbackMessage
} = {}) => {
  if (status === 401) return '登录已过期，请重新登录'
  if (status === 403) return '权限不足'
  if (status === 404) return '请求的资源不存在'
  if (status === 413) return serverMessage || PAYLOAD_TOO_LARGE_MESSAGE
  if (errorCode === 'ECONNABORTED') return '请求超时，请稍后重试'
  return serverMessage || fallbackMessage || '网络连接失败'
}
