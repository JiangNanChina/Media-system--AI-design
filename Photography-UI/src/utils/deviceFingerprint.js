/**
 * 设备指纹生成工具
 * 用于生成唯一且稳定的设备标识，防止多设备登录作弊
 */

const DEVICE_ID_STORAGE_KEY = 'photography_device_id'
const DEVICE_ID_COOKIE_NAME = 'photography_device_id'

/**
 * 生成设备指纹
 * @returns {Promise<Object>} 设备信息对象
 */
export async function generateDeviceFingerprint() {
  const stableDeviceId = getOrCreateStableDeviceId()
  const fingerprint = {
    // 基础信息仅用于展示和兼容判断，不再直接作为主指纹
    userAgent: navigator.userAgent,
    language: navigator.language || navigator.userLanguage,
    platform: navigator.platform,
    cookieEnabled: navigator.cookieEnabled,
    
    // 屏幕信息（相对稳定）
    screenResolution: `${screen.width}x${screen.height}`,
    screenColorDepth: screen.colorDepth,
    
    // 时区信息
    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
    
    // 硬件信息（稳定）
    hardwareConcurrency: navigator.hardwareConcurrency || 0,
    deviceMemory: navigator.deviceMemory || 0,
    
    // WebGL指纹（相对稳定）
    webglVendor: getWebGLVendor(),
    webglRenderer: getWebGLRenderer(),
    
    // Canvas指纹（稳定）
    canvasFingerprint: getCanvasFingerprint(),
    
    // 存储信息
    localStorage: isStorageAvailable('localStorage'),
    sessionStorage: isStorageAvailable('sessionStorage'),
    indexedDB: !!window.indexedDB,
    
    // 触摸支持（稳定）
    touchSupport: getTouchSupport()
  }

  const stableFallbackFingerprint = {
    userAgentFamily: getBrowserFamily(),
    osFamily: getOSFamily(),
    deviceType: getDeviceType(),
    platform: navigator.platform,
    timezone: fingerprint.timezone,
    language: fingerprint.language,
    screenResolution: normalizeScreenResolution(fingerprint.screenResolution),
    touchSupport: fingerprint.touchSupport
  }
  
  // 波动项仍保留在 rawFingerprint 中用于排查，主指纹优先使用持久化 ID。
  const fingerprintString = JSON.stringify(stableFallbackFingerprint, Object.keys(stableFallbackFingerprint).sort())
  const fingerprintHash = await generateHash(fingerprintString)
  
  return {
    deviceFingerprint: stableDeviceId || `fallback_${fingerprintHash}`,
    stableDeviceId,
    deviceName: getDeviceName(),
    deviceType: getDeviceType(),
    osInfo: getOSInfo(),
    browserInfo: getBrowserInfo(),
    screenResolution: fingerprint.screenResolution,
    timezone: fingerprint.timezone,
    language: fingerprint.language,
    rawFingerprint: {
      ...fingerprint,
      stableFallbackFingerprint,
      volatileFingerprintHash: await generateHash(JSON.stringify(fingerprint, Object.keys(fingerprint).sort()))
    }
  }
}

/**
 * 获取或创建设备稳定ID。只要用户未清理浏览器数据，同一浏览器会保持不变。
 */
function getOrCreateStableDeviceId() {
  const storedId = readStoredDeviceId()
  if (storedId) {
    return storedId
  }

  const newId = createDeviceId()
  if (writeStoredDeviceId(newId)) {
    return newId
  }

  return null
}

function readStoredDeviceId() {
  try {
    const value = window.localStorage?.getItem(DEVICE_ID_STORAGE_KEY)
    if (value) {
      return value
    }
  } catch (e) {
    // Ignore storage errors
  }

  try {
    const match = document.cookie
      ?.split('; ')
      .find(row => row.startsWith(`${DEVICE_ID_COOKIE_NAME}=`))
    if (match) {
      return decodeURIComponent(match.split('=').slice(1).join('='))
    }
  } catch (e) {
    // Ignore cookie errors
  }

  return null
}

function writeStoredDeviceId(deviceId) {
  let stored = false

  try {
    window.localStorage?.setItem(DEVICE_ID_STORAGE_KEY, deviceId)
    stored = true
  } catch (e) {
    // Ignore storage errors
  }

  try {
    const maxAge = 60 * 60 * 24 * 365 * 3
    document.cookie = `${DEVICE_ID_COOKIE_NAME}=${encodeURIComponent(deviceId)}; Max-Age=${maxAge}; Path=/; SameSite=Lax`
    stored = true
  } catch (e) {
    // Ignore cookie errors
  }

  return stored
}

function createDeviceId() {
  const webCrypto = window.crypto || window.msCrypto

  if (webCrypto?.randomUUID) {
    return `browser_${webCrypto.randomUUID()}`
  }

  if (webCrypto?.getRandomValues) {
    const bytes = new Uint8Array(16)
    webCrypto.getRandomValues(bytes)
    return `browser_${Array.from(bytes).map(b => b.toString(16).padStart(2, '0')).join('')}`
  }

  return `browser_${Math.random().toString(36).slice(2)}${Math.random().toString(36).slice(2)}`
}

/**
 * 获取WebGL供应商
 */
function getWebGLVendor() {
  try {
    const canvas = document.createElement('canvas')
    const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl')
    if (gl) {
      const debugInfo = gl.getExtension('WEBGL_debug_renderer_info')
      if (debugInfo) {
        return gl.getParameter(debugInfo.UNMASKED_VENDOR_WEBGL)
      }
    }
  } catch (e) {
    // Ignore errors
  }
  return null
}

/**
 * 获取WebGL渲染器
 */
function getWebGLRenderer() {
  try {
    const canvas = document.createElement('canvas')
    const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl')
    if (gl) {
      const debugInfo = gl.getExtension('WEBGL_debug_renderer_info')
      if (debugInfo) {
        return gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL)
      }
    }
  } catch (e) {
    // Ignore errors
  }
  return null
}

/**
 * 获取Canvas指纹
 */
function getCanvasFingerprint() {
  try {
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    
    // 绘制特定图案
    ctx.textBaseline = 'top'
    ctx.font = '14px Arial'
    ctx.fillStyle = '#f60'
    ctx.fillRect(125, 1, 62, 20)
    ctx.fillStyle = '#069'
    ctx.fillText('Device Fingerprint 🔐', 2, 15)
    ctx.fillStyle = 'rgba(102, 204, 0, 0.7)'
    ctx.fillText('Device Fingerprint 🔐', 4, 17)
    
    return canvas.toDataURL()
  } catch (e) {
    return null
  }
}

/**
 * 检查存储是否可用
 */
function isStorageAvailable(type) {
  try {
    const storage = window[type]
    const x = '__storage_test__'
    storage.setItem(x, x)
    storage.removeItem(x)
    return true
  } catch (e) {
    return false
  }
}

/**
 * 获取触摸支持信息
 */
function getTouchSupport() {
  return {
    maxTouchPoints: navigator.maxTouchPoints || 0,
    touchEvent: 'ontouchstart' in window
  }
}

/**
 * 生成哈希
 */
async function generateHash(str) {
  try {
    const webCrypto = window.crypto || window.msCrypto
    if (!webCrypto?.subtle) {
      throw new Error('SubtleCrypto unavailable')
    }
    const encoder = new TextEncoder()
    const data = encoder.encode(str)
    const hashBuffer = await webCrypto.subtle.digest('SHA-256', data)
    const hashArray = Array.from(new Uint8Array(hashBuffer))
    const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('')
    return hashHex
  } catch (e) {
    // Fallback: 简单哈希
    let hash = 0
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i)
      hash = ((hash << 5) - hash) + char
      hash = hash & hash // Convert to 32-bit integer
    }
    return Math.abs(hash).toString(16)
  }
}

/**
 * 获取设备名称
 */
function getDeviceName() {
  const userAgent = navigator.userAgent
  const isIPadOS = navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1
  
  // 尝试从User-Agent中提取设备信息
  if (/iPhone/i.test(userAgent)) {
    const match = userAgent.match(/iPhone OS ([\d_]+)/)
    return match ? `iPhone (iOS ${match[1].replace(/_/g, '.')})` : 'iPhone'
  }
  
  if (/iPad/i.test(userAgent) || isIPadOS) {
    return 'iPad'
  }
  
  if (/Android/i.test(userAgent)) {
    const match = userAgent.match(/Android ([\d.]+)/)
    const modelMatch = userAgent.match(/Android [\d.]+;\s*([^;)]+?)(?:\s+Build|\)|;)/i)
    const model = modelMatch ? modelMatch[1].trim() : ''
    return match ? `Android ${match[1]}${model ? ` ${model}` : ''}` : 'Android Device'
  }
  
  if (/Windows NT/i.test(userAgent)) {
    const match = userAgent.match(/Windows NT ([\d.]+)/)
    return match ? `Windows ${match[1]}` : 'Windows PC'
  }
  
  if (/Mac OS X/i.test(userAgent)) {
    const match = userAgent.match(/Mac OS X ([\d_]+)/)
    return match ? `macOS ${match[1].replace(/_/g, '.')}` : 'macOS'
  }
  
  if (/Linux/i.test(userAgent)) {
    return 'Linux PC'
  }
  
  return 'Unknown Device'
}

/**
 * 获取设备类型
 */
function getDeviceType() {
  const userAgent = navigator.userAgent
  const isIPadOS = navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1
  
  if (/iPad|Tablet/i.test(userAgent) || isIPadOS || (/Android/i.test(userAgent) && !/Mobile/i.test(userAgent))) {
    return 'TABLET'
  }
  
  if (/Mobile|iPhone|iPod|BlackBerry|Opera Mini|IEMobile|WPDesktop|Android/i.test(userAgent)) {
    return 'MOBILE'
  }
  
  return 'DESKTOP'
}

/**
 * 获取操作系统信息
 */
function getOSInfo() {
  const userAgent = navigator.userAgent
  const platform = navigator.platform
  const isIPadOS = platform === 'MacIntel' && navigator.maxTouchPoints > 1
  
  if (/Windows NT/i.test(userAgent)) {
    const match = userAgent.match(/Windows NT ([\d.]+)/)
    return match ? `Windows ${match[1]}` : 'Windows'
  }

  if (isIPadOS) {
    return 'iPadOS'
  }
  
  if (/Mac OS X/i.test(userAgent)) {
    const match = userAgent.match(/Mac OS X ([\d_]+)/)
    return match ? `macOS ${match[1].replace(/_/g, '.')}` : 'macOS'
  }
  
  if (/Android/i.test(userAgent)) {
    const match = userAgent.match(/Android ([\d.]+)/)
    return match ? `Android ${match[1]}` : 'Android'
  }
  
  if (/iPhone OS/i.test(userAgent)) {
    const match = userAgent.match(/iPhone OS ([\d_]+)/)
    return match ? `iOS ${match[1].replace(/_/g, '.')}` : 'iOS'
  }
  
  if (/Linux/i.test(userAgent)) {
    return 'Linux'
  }
  
  return platform || 'Unknown OS'
}

/**
 * 获取浏览器信息
 */
function getBrowserInfo() {
  const userAgent = navigator.userAgent

  const browserRules = [
    { name: 'Edge', pattern: /\bEdgA?\/([\d.]+)/i },
    { name: 'Edge iOS', pattern: /\bEdgiOS\/([\d.]+)/i },
    { name: 'Opera', pattern: /\bOPR\/([\d.]+)/i },
    { name: 'Samsung Internet', pattern: /SamsungBrowser\/([\d.]+)/i },
    { name: 'UC Browser', pattern: /UCBrowser\/([\d.]+)/i },
    { name: 'QQ Browser', pattern: /(?:MQQBrowser|QQBrowser)\/([\d.]+)/i },
    { name: 'WeChat', pattern: /MicroMessenger\/([\d.]+)/i },
    { name: 'Chrome iOS', pattern: /CriOS\/([\d.]+)/i },
    { name: 'Firefox iOS', pattern: /FxiOS\/([\d.]+)/i },
    { name: 'Firefox', pattern: /Firefox\/([\d.]+)/i },
    { name: 'Chrome', pattern: /Chrome\/([\d.]+)/i },
    { name: 'Safari', pattern: /Version\/([\d.]+).*Safari/i }
  ]

  for (const rule of browserRules) {
    const match = userAgent.match(rule.pattern)
    if (match) {
      return `${rule.name} ${match[1]}`
    }
  }
  
  return 'Unknown Browser'
}

function getBrowserFamily() {
  return getBrowserInfo().replace(/\s+[\d.]+$/, '')
}

function getOSFamily() {
  return getOSInfo().replace(/\s+[\d._]+$/, '')
}

function normalizeScreenResolution(resolution) {
  if (!resolution || !resolution.includes('x')) {
    return resolution
  }

  const parts = resolution.split('x').map(part => Number(part))
  if (parts.some(Number.isNaN)) {
    return resolution
  }

  return parts.sort((a, b) => a - b).join('x')
}

/**
 * 为调试目的显示设备指纹详情
 */
export async function debugDeviceFingerprint() {
  const deviceInfo = await generateDeviceFingerprint()
  console.log('设备指纹调试信息:', {
    fingerprint: deviceInfo.deviceFingerprint,
    deviceName: deviceInfo.deviceName,
    deviceType: deviceInfo.deviceType,
    osInfo: deviceInfo.osInfo,
    browserInfo: deviceInfo.browserInfo,
    rawFingerprint: deviceInfo.rawFingerprint
  })
  return deviceInfo
}
