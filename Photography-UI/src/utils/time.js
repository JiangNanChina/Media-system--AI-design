/**
 * 时间工具函数
 */

/**
 * 格式化时间
 * @param {string|Date} time - 时间
 * @param {string} format - 格式 (default: 'YYYY-MM-DD HH:mm:ss')
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(time, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!time) return ''
  
  const date = typeof time === 'string' ? new Date(time) : time
  
  if (isNaN(date.getTime())) return ''
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 获取相对时间描述
 * @param {string|Date} time - 时间
 * @returns {string} 相对时间描述
 */
export function getRelativeTime(time) {
  if (!time) return ''
  
  const date = typeof time === 'string' ? new Date(time) : time
  const now = new Date()
  const diff = now - date
  
  if (diff < 0) return '未来'
  
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(months / 12)
  
  if (years > 0) return `${years}年前`
  if (months > 0) return `${months}个月前`
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  return '刚刚'
}

/**
 * 格式化日期（不包含时间）
 * @param {string|Date} time - 时间
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(time) {
  return formatTime(time, 'YYYY-MM-DD')
}

/**
 * 格式化日期时间（12小时制）
 * @param {string|Date} time - 时间
 * @returns {string} 格式化后的时间字符串
 */
export function formatDateTime12(time) {
  if (!time) return ''
  
  const date = typeof time === 'string' ? new Date(time) : time
  
  if (isNaN(date.getTime())) return ''
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  
  let hours = date.getHours()
  const ampm = hours >= 12 ? 'PM' : 'AM'
  hours = hours % 12
  hours = hours ? hours : 12 // 0时显示为12
  
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes} ${ampm}`
}
