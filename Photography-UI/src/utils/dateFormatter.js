/**
 * 日期格式化工具函数
 */

/**
 * 格式化日期时间
 * @param {string|Date} dateTime - 日期时间字符串或Date对象
 * @param {string} format - 格式模板，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(dateTime, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!dateTime) return '-'
  
  const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
  
  if (isNaN(date.getTime())) return '-'
  
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
 * 格式化日期
 * @param {string|Date} dateTime - 日期时间字符串或Date对象
 * @returns {string} 格式化后的日期字符串 YYYY-MM-DD
 */
export function formatDate(dateTime) {
  return formatDateTime(dateTime, 'YYYY-MM-DD')
}

/**
 * 格式化时间
 * @param {string|Date} dateTime - 日期时间字符串或Date对象
 * @returns {string} 格式化后的时间字符串 HH:mm:ss
 */
export function formatTime(dateTime) {
  return formatDateTime(dateTime, 'HH:mm:ss')
}

/**
 * 计算时间差（分钟）
 * @param {string|Date} startTime - 开始时间
 * @param {string|Date} endTime - 结束时间
 * @returns {number} 时间差（分钟）
 */
export function getMinutesDiff(startTime, endTime) {
  const start = typeof startTime === 'string' ? new Date(startTime) : startTime
  const end = typeof endTime === 'string' ? new Date(endTime) : endTime
  
  return Math.floor((end - start) / (1000 * 60))
}

/**
 * 获取相对时间描述
 * @param {string|Date} dateTime - 日期时间
 * @returns {string} 相对时间描述，如"刚刚"、"5分钟前"等
 */
export function getRelativeTime(dateTime) {
  if (!dateTime) return '-'
  
  const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
  const now = new Date()
  const diff = now - date
  
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return formatDate(date)
}

