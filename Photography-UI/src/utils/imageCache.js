/**
 * 图片缓存和预加载工具
 * 提供图片预加载、内存缓存、批量预加载等功能
 */

class ImageCache {
  constructor() {
    // 内存缓存，存储已加载的图片
    this.cache = new Map()
    // 正在加载的图片Promise，避免重复加载
    this.loading = new Map()
    // 预加载队列
    this.preloadQueue = []
    // 最大缓存数量
    this.maxCacheSize = 100
    // 缓存过期时间（毫秒）
    this.cacheExpiry = 30 * 60 * 1000 // 30分钟
  }

  /**
   * 预加载单个图片
   * @param {string} url 图片URL
   * @param {Object} options 选项
   * @returns {Promise<HTMLImageElement>}
   */
  async preload(url, options = {}) {
    if (!url) return null

    // 检查缓存
    const cached = this.cache.get(url)
    if (cached && !this.isExpired(cached.timestamp)) {
      return cached.image
    }

    // 检查是否正在加载
    if (this.loading.has(url)) {
      return this.loading.get(url)
    }

    // 创建加载Promise
    const loadPromise = this.loadImage(url, options)
    this.loading.set(url, loadPromise)

    try {
      const image = await loadPromise
      
      // 存储到缓存
      this.addToCache(url, image)
      
      return image
    } catch (error) {
      console.warn(`图片预加载失败: ${url}`, error)
      return null
    } finally {
      // 清理loading状态
      this.loading.delete(url)
    }
  }

  /**
   * 批量预加载图片
   * @param {Array<string>} urls 图片URL数组
   * @param {Object} options 选项
   * @returns {Promise<Array>}
   */
  async preloadBatch(urls, options = {}) {
    if (!Array.isArray(urls) || urls.length === 0) return []

    const { 
      maxConcurrency = 3, // 最大并发数
      priority = false // 是否优先加载
    } = options

    // 过滤已缓存的URL
    const uncachedUrls = urls.filter(url => {
      const cached = this.cache.get(url)
      return !cached || this.isExpired(cached.timestamp)
    })

    if (uncachedUrls.length === 0) {
      return urls.map(url => this.cache.get(url)?.image).filter(Boolean)
    }

    // 分批加载
    const results = []
    for (let i = 0; i < uncachedUrls.length; i += maxConcurrency) {
      const batch = uncachedUrls.slice(i, i + maxConcurrency)
      const batchPromises = batch.map(url => this.preload(url, options))
      
      try {
        const batchResults = await Promise.allSettled(batchPromises)
        results.push(...batchResults.map(result => 
          result.status === 'fulfilled' ? result.value : null
        ))
      } catch (error) {
        console.warn('批量预加载出错:', error)
      }
    }

    return results.filter(Boolean)
  }

  /**
   * 智能预加载（基于可见性和用户行为）
   * @param {Array<string>} urls 图片URL数组
   * @param {Object} options 选项
   */
  smartPreload(urls, options = {}) {
    const {
      viewport = true, // 是否只预加载视窗附近的图片
      delay = 100 // 延迟时间（毫秒）
    } = options

    // 延迟执行，避免阻塞主线程
    setTimeout(() => {
      if (viewport) {
        // 只预加载可能很快被看到的图片
        const priorityUrls = urls.slice(0, 10) // 前10个图片优先加载
        this.preloadBatch(priorityUrls, { maxConcurrency: 2 })
      } else {
        this.preloadBatch(urls, { maxConcurrency: 1 })
      }
    }, delay)
  }

  /**
   * 加载单个图片
   * @param {string} url 图片URL
   * @param {Object} options 选项
   * @returns {Promise<HTMLImageElement>}
   */
  loadImage(url, options = {}) {
    return new Promise((resolve, reject) => {
      const img = new Image()
      const { timeout = 30000, crossOrigin = null } = options

      // 设置超时
      const timeoutId = setTimeout(() => {
        reject(new Error(`图片加载超时: ${url}`))
      }, timeout)

      img.onload = () => {
        clearTimeout(timeoutId)
        resolve(img)
      }

      img.onerror = (error) => {
        clearTimeout(timeoutId)
        reject(new Error(`图片加载失败: ${url}`))
      }

      if (crossOrigin) {
        img.crossOrigin = crossOrigin
      }

      img.src = url
    })
  }

  /**
   * 添加到缓存
   * @param {string} url 图片URL
   * @param {HTMLImageElement} image 图片对象
   */
  addToCache(url, image) {
    // 检查缓存大小
    if (this.cache.size >= this.maxCacheSize) {
      this.evictOldest()
    }

    this.cache.set(url, {
      image,
      timestamp: Date.now()
    })
  }

  /**
   * 清理最老的缓存
   */
  evictOldest() {
    let oldestKey = null
    let oldestTime = Date.now()

    for (const [key, value] of this.cache.entries()) {
      if (value.timestamp < oldestTime) {
        oldestTime = value.timestamp
        oldestKey = key
      }
    }

    if (oldestKey) {
      this.cache.delete(oldestKey)
    }
  }

  /**
   * 检查缓存是否过期
   * @param {number} timestamp 时间戳
   * @returns {boolean}
   */
  isExpired(timestamp) {
    return Date.now() - timestamp > this.cacheExpiry
  }

  /**
   * 清理过期缓存
   */
  cleanup() {
    const now = Date.now()
    for (const [key, value] of this.cache.entries()) {
      if (this.isExpired(value.timestamp)) {
        this.cache.delete(key)
      }
    }
  }

  /**
   * 获取缓存信息
   * @returns {Object}
   */
  getCacheInfo() {
    return {
      size: this.cache.size,
      maxSize: this.maxCacheSize,
      loadingCount: this.loading.size,
      urls: Array.from(this.cache.keys())
    }
  }

  /**
   * 清空缓存
   */
  clear() {
    this.cache.clear()
    this.loading.clear()
  }
}

// 创建全局单例
const imageCache = new ImageCache()

// 定期清理过期缓存
setInterval(() => {
  imageCache.cleanup()
}, 5 * 60 * 1000) // 每5分钟清理一次

// 导出实例和工具函数
export default imageCache

/**
 * 预加载图片的便捷函数
 * @param {string|Array<string>} urls 图片URL或数组
 * @param {Object} options 选项
 * @returns {Promise}
 */
export const preloadImages = (urls, options = {}) => {
  if (typeof urls === 'string') {
    return imageCache.preload(urls, options)
  } else if (Array.isArray(urls)) {
    return imageCache.preloadBatch(urls, options)
  }
  return Promise.resolve()
}

/**
 * 智能预加载函数
 * @param {Array<string>} urls 图片URL数组
 * @param {Object} options 选项
 */
export const smartPreload = (urls, options = {}) => {
  imageCache.smartPreload(urls, options)
}
