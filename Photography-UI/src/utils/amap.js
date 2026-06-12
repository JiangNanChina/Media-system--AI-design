/**
 * 高德地图工具类
 */

import { AMAP_CONFIG, loadAmapApi, AmapErrors } from '@/config/amap'
import { ElMessage } from 'element-plus'

/**
 * 高德地图管理器
 */
export class AmapManager {
  constructor() {
    this.map = null
    this.marker = null
    this.geolocation = null
    this.geocoder = null
    this.placeSearch = null
    this.isReady = false
  }

  /**
   * 初始化地图
   */
  async init(container, options = {}) {
    try {
      // 加载高德地图API
      const AMap = await loadAmapApi()
      
      // 合并配置
      const mapOptions = {
        ...AMAP_CONFIG.mapOptions,
        ...options,
        container: container
      }
      
      // 创建地图实例
      this.map = new AMap.Map(container, mapOptions)
      
      // 初始化插件
      await this.initPlugins(AMap)
      
      this.isReady = true
      return this.map
    } catch (error) {
      console.error('地图初始化失败:', error)
      throw error
    }
  }

  /**
   * 初始化插件
   */
  async initPlugins(AMap) {
    // 初始化定位插件
    this.geolocation = new AMap.Geolocation(AMAP_CONFIG.geolocationOptions)
    this.map.addControl(this.geolocation)
    
    // 初始化地理编码插件
    this.geocoder = new AMap.Geocoder({
      city: '全国'
    })
    
    // 初始化地点搜索插件
    this.placeSearch = new AMap.PlaceSearch({
      city: '全国',
      citylimit: false,
      pageSize: 10,
      pageIndex: 1
    })
  }

  /**
   * 添加点击事件监听
   */
  onMapClick(callback) {
    if (!this.map) return
    
    this.map.on('click', (e) => {
      const { lng, lat } = e.lnglat
      callback(lng, lat, e)
    })
  }

  /**
   * 设置标记
   */
  setMarker(lng, lat, options = {}) {
    if (!this.map) return null
    
    // 移除现有标记
    if (this.marker) {
      this.map.remove(this.marker)
    }
    
    // 创建新标记
    const markerOptions = {
      ...AMAP_CONFIG.markerOptions,
      ...options,
      position: [lng, lat]
    }
    
    this.marker = new window.AMap.Marker(markerOptions)
    this.map.add(this.marker)
    
    // 添加拖拽事件
    if (markerOptions.draggable) {
      this.marker.on('dragend', (e) => {
        const { lng, lat } = e.target.getPosition()
        if (options.onDragEnd) {
          options.onDragEnd(lng, lat)
        }
      })
    }
    
    return this.marker
  }

  /**
   * 移动地图中心点
   */
  setCenter(lng, lat, zoom) {
    if (!this.map) return
    
    this.map.setCenter([lng, lat])
    if (zoom !== undefined) {
      this.map.setZoom(zoom)
    }
  }

  /**
   * 获取当前位置
   */
  getCurrentPosition() {
    return new Promise((resolve, reject) => {
      if (!this.geolocation) {
        reject(new Error(AmapErrors.GEOLOCATION_NOT_SUPPORTED))
        return
      }
      
      this.geolocation.getCurrentPosition((status, result) => {
        if (status === 'complete') {
          const { lng, lat } = result.position
          resolve({
            longitude: lng,
            latitude: lat,
            accuracy: result.accuracy,
            address: result.formattedAddress || '',
            addressComponent: result.addressComponent || {}
          })
        } else {
          reject(new Error(result.message || AmapErrors.GEOLOCATION_FAILED))
        }
      })
    })
  }

  /**
   * 地理编码（地址转坐标）
   */
  geocode(address) {
    return new Promise((resolve, reject) => {
      if (!this.geocoder) {
        reject(new Error('地理编码插件未初始化'))
        return
      }
      
      this.geocoder.getLocation(address, (status, result) => {
        if (status === 'complete' && result.geocodes.length > 0) {
          const { lng, lat } = result.geocodes[0].location
          resolve({
            longitude: lng,
            latitude: lat,
            formattedAddress: result.geocodes[0].formattedAddress,
            addressComponent: result.geocodes[0].addressComponent
          })
        } else {
          reject(new Error('地址解析失败'))
        }
      })
    })
  }

  /**
   * 逆地理编码（坐标转地址）
   */
  reverseGeocode(lng, lat) {
    return new Promise((resolve, reject) => {
      if (!this.geocoder) {
        reject(new Error('地理编码插件未初始化'))
        return
      }
      
      this.geocoder.getAddress([lng, lat], (status, result) => {
        if (status === 'complete' && result.regeocode) {
          resolve({
            formattedAddress: result.regeocode.formattedAddress,
            addressComponent: result.regeocode.addressComponent,
            pois: result.regeocode.pois || []
          })
        } else {
          reject(new Error('坐标解析失败'))
        }
      })
    })
  }

  /**
   * 搜索地点
   */
  searchPlace(keyword) {
    return new Promise((resolve, reject) => {
      if (!this.placeSearch) {
        reject(new Error('地点搜索插件未初始化'))
        return
      }
      
      this.placeSearch.search(keyword, (status, result) => {
        if (status === 'complete' && result.poiList) {
          const places = result.poiList.pois.map(poi => ({
            id: poi.id,
            name: poi.name,
            address: poi.address,
            longitude: poi.location.lng,
            latitude: poi.location.lat,
            type: poi.type,
            tel: poi.tel || '',
            distance: poi.distance || 0
          }))
          resolve(places)
        } else {
          reject(new Error('地点搜索失败'))
        }
      })
    })
  }

  /**
   * 销毁地图
   */
  destroy() {
    if (this.map) {
      this.map.destroy()
      this.map = null
    }
    this.marker = null
    this.geolocation = null
    this.geocoder = null
    this.placeSearch = null
    this.isReady = false
  }
}

/**
 * 创建高德地图实例
 */
export const createAmapInstance = () => {
  return new AmapManager()
}

/**
 * 格式化坐标
 */
export const formatCoordinate = (value, precision = 6) => {
  return parseFloat(value).toFixed(precision)
}

/**
 * 验证坐标有效性
 */
export const validateCoordinates = (lng, lat) => {
  const longitude = parseFloat(lng)
  const latitude = parseFloat(lat)
  
  if (isNaN(longitude) || isNaN(latitude)) {
    return { valid: false, message: '坐标格式错误' }
  }
  
  if (longitude < -180 || longitude > 180) {
    return { valid: false, message: '经度应在 -180 到 180 之间' }
  }
  
  if (latitude < -90 || latitude > 90) {
    return { valid: false, message: '纬度应在 -90 到 90 之间' }
  }
  
  return { valid: true }
}

/**
 * 计算两点之间距离（米）
 */
export const calculateDistance = (lng1, lat1, lng2, lat2) => {
  if (!window.AMap) return 0
  
  const point1 = new window.AMap.LngLat(lng1, lat1)
  const point2 = new window.AMap.LngLat(lng2, lat2)
  return point1.distance(point2)
}

/**
 * 错误处理
 */
export const handleAmapError = (error) => {
  console.error('高德地图错误:', error)
  
  let message = '地图操作失败'
  
  if (error.message.includes('API')) {
    message = '地图API加载失败，请检查网络连接'
  } else if (error.message.includes('密钥')) {
    message = '地图服务配置错误，请联系管理员'
  } else if (error.message.includes('定位')) {
    message = '定位失败，请检查定位权限'
  }
  
  ElMessage.error(message)
}
