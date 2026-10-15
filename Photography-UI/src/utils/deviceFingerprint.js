/**
 * 设备指纹生成工具
 * 用于生成唯一且稳定的设备标识，防止多设备登录作弊
 */

const DEVICE_ID_STORAGE_KEY = 'photography_device_id'
const DEVICE_ID_COOKIE_NAME = 'photography_device_id'
const DEVICE_ID_DB_NAME = 'photography_device_identity'
const DEVICE_ID_STORE_NAME = 'identity'
const DEVICE_ID_RECORD_KEY = 'stable_device_id'
let stableDeviceIdPromise = null

/**
 * 生成设备指纹
 * @returns {Promise<Object>} 设备信息对象
 */
export async function generateDeviceFingerprint() {
  const stableDeviceId = await getOrCreateStableDeviceId()
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
  const hardwareFingerprint = await generateHardwareFingerprint(fingerprint)
  
  return {
    deviceFingerprint: stableDeviceId || `fallback_${fingerprintHash}`,
    stableDeviceId,
    hardwareFingerprint,
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
async function getOrCreateStableDeviceId() {
  if (stableDeviceIdPromise) {
    return stableDeviceIdPromise
  }
  stableDeviceIdPromise = (async () => {
  const storedId = await readStoredDeviceId()
  if (storedId) {
    // 自动补齐缺失的存储副本，提高浏览器升级或单个存储失效后的稳定性。
    await writeStoredDeviceId(storedId)
    return storedId
  }

  const newId = createDeviceId()
  if (await writeStoredDeviceId(newId)) {
    return newId
  }

  return null
  })()
  stableDeviceIdPromise.catch(() => {
    stableDeviceIdPromise = null
  })
  return stableDeviceIdPromise
}

async function readStoredDeviceId() {
  try {
    const value = window.localStorage?.getItem(DEVICE_ID_STORAGE_KEY)
    if (isValidStableDeviceId(value)) {
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
      const cookieValue = decodeURIComponent(match.split('=').slice(1).join('='))
      if (isValidStableDeviceId(cookieValue)) {
        return cookieValue
      }
    }
  } catch (e) {
    // Ignore cookie errors
  }

  const indexedDbValue = await readDeviceIdFromIndexedDb()
  return isValidStableDeviceId(indexedDbValue) ? indexedDbValue : null
}

async function writeStoredDeviceId(deviceId) {
  if (!isValidStableDeviceId(deviceId)) {
    return false
  }

  let localStorageStored = false
  let cookieStored = false

  try {
    window.localStorage?.setItem(DEVICE_ID_STORAGE_KEY, deviceId)
    localStorageStored = window.localStorage?.getItem(DEVICE_ID_STORAGE_KEY) === deviceId
  } catch (e) {
    // Ignore storage errors
  }

  try {
    const maxAge = 60 * 60 * 24 * 365 * 3
    document.cookie = `${DEVICE_ID_COOKIE_NAME}=${encodeURIComponent(deviceId)}; Max-Age=${maxAge}; Path=/; SameSite=Lax`
    cookieStored = document.cookie
      .split('; ')
      .some(row => row === `${DEVICE_ID_COOKIE_NAME}=${encodeURIComponent(deviceId)}`)
  } catch (e) {
    // Ignore cookie errors
  }

  const indexedDbStored = await writeDeviceIdToIndexedDb(deviceId)
  return localStorageStored || cookieStored || indexedDbStored
}

function isValidStableDeviceId(deviceId) {
  return typeof deviceId === 'string'
    && /^browser_[a-zA-Z0-9-]{16,100}$/.test(deviceId)
}

function openDeviceIdentityDb() {
  return new Promise((resolve, reject) => {
    if (!window.indexedDB) {
      resolve(null)
      return
    }

    const request = window.indexedDB.open(DEVICE_ID_DB_NAME, 1)
    request.onupgradeneeded = () => {
      const db = request.result
      if (!db.objectStoreNames.contains(DEVICE_ID_STORE_NAME)) {
        db.createObjectStore(DEVICE_ID_STORE_NAME)
      }
    }
    request.onsuccess = () => resolve(request.result)
    request.onerror = () => reject(request.error)
    request.onblocked = () => reject(new Error('IndexedDB is blocked'))
  })
}

async function readDeviceIdFromIndexedDb() {
  let db
  try {
    db = await openDeviceIdentityDb()
    if (!db) return null
    return await new Promise((resolve, reject) => {
      const request = db
        .transaction(DEVICE_ID_STORE_NAME, 'readonly')
        .objectStore(DEVICE_ID_STORE_NAME)
        .get(DEVICE_ID_RECORD_KEY)
      request.onsuccess = () => resolve(request.result || null)
      request.onerror = () => reject(request.error)
    })
  } catch (e) {
    return null
  } finally {
    db?.close()
  }
}

async function writeDeviceIdToIndexedDb(deviceId) {
  let db
  try {
    db = await openDeviceIdentityDb()
    if (!db) return false
    await new Promise((resolve, reject) => {
      const transaction = db.transaction(DEVICE_ID_STORE_NAME, 'readwrite')
      transaction.objectStore(DEVICE_ID_STORE_NAME).put(deviceId, DEVICE_ID_RECORD_KEY)
      transaction.oncomplete = () => resolve()
      transaction.onerror = () => reject(transaction.error)
      transaction.onabort = () => reject(transaction.error)
    })
    return true
  } catch (e) {
    return false
  } finally {
    db?.close()
  }
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
 * 生成用于同设备恢复的硬件特征。排除浏览器名称/版本、UA 全文和屏幕方向等易变项。
 * 该摘要只由后端在同一账号、同一设备槽位内做唯一匹配。
 */
async function generateHardwareFingerprint(fingerprint) {
  const clientHints = await getStableClientHints()
  const hardwareProfile = {
    version: 1,
    deviceType: getDeviceType(),
    osFamily: getOSFamily(),
    platform: normalizePlatform(clientHints.platform || navigator.platform),
    model: clientHints.model,
    architecture: clientHints.architecture,
    bitness: clientHints.bitness,
    screenResolution: normalizeScreenResolution(fingerprint.screenResolution),
    screenColorDepth: fingerprint.screenColorDepth || 0,
    hardwareConcurrency: fingerprint.hardwareConcurrency || 0,
    deviceMemory: fingerprint.deviceMemory || 0,
    maxTouchPoints: fingerprint.touchSupport?.maxTouchPoints || 0,
    touchEvent: Boolean(fingerprint.touchSupport?.touchEvent),
    webglVendor: normalizeFingerprintValue(fingerprint.webglVendor),
    webglRenderer: normalizeFingerprintValue(fingerprint.webglRenderer)
  }
  const serialized = JSON.stringify(hardwareProfile, Object.keys(hardwareProfile).sort())
  return 'hardware_v1_' + await generateHash64(serialized)
}

async function getStableClientHints() {
  const userAgentData = navigator.userAgentData
  if (!userAgentData?.getHighEntropyValues) {
    return {
      platform: userAgentData?.platform || '',
      model: '',
      architecture: '',
      bitness: ''
    }
  }

  try {
    const values = await userAgentData.getHighEntropyValues([
      'platform',
      'model',
      'architecture',
      'bitness'
    ])
    return {
      platform: normalizeFingerprintValue(values.platform),
      model: normalizeFingerprintValue(values.model),
      architecture: normalizeFingerprintValue(values.architecture),
      bitness: normalizeFingerprintValue(values.bitness)
    }
  } catch (e) {
    return {
      platform: normalizeFingerprintValue(userAgentData.platform),
      model: '',
      architecture: '',
      bitness: ''
    }
  }
}

async function generateHash64(value) {
  try {
    const webCrypto = window.crypto || window.msCrypto
    if (webCrypto?.subtle) {
      const encoder = new TextEncoder()
      const data = encoder.encode(value)
      const hashBuffer = await webCrypto.subtle.digest('SHA-256', data)
      return Array.from(new Uint8Array(hashBuffer))
        .map(byte => byte.toString(16).padStart(2, '0'))
        .join('')
    }
  } catch (e) {
    // Continue with the deterministic fixed-length fallback below.
  }

  // 八个不同种子的 32 位 FNV-1a 片段组成 64 位十六进制摘要。
  return Array.from({ length: 8 }, (_, seed) => {
    let hash = (0x811c9dc5 ^ seed) >>> 0
    for (let index = 0; index < value.length; index += 1) {
      hash ^= value.charCodeAt(index)
      hash = Math.imul(hash, 0x01000193) >>> 0
    }
    return hash.toString(16).padStart(8, '0')
  }).join('')
}

function normalizePlatform(platform) {
  return String(platform || 'unknown').trim().toLowerCase()
}

function normalizeFingerprintValue(value) {
  return String(value || '').trim().replace(/\s+/g, ' ').toLowerCase()
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
