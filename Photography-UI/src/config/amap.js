/**
 * 高德地图配置
 */

// 高德地图API密钥配置
// 注意：在生产环境中，建议将API密钥配置在环境变量中
export const AMAP_CONFIG = {
  // API密钥（需要在高德开放平台申请）
  // 申请地址：https://console.amap.com/dev/key/app
  key: import.meta.env.VITE_AMAP_API_KEY || 'YOUR_AMAP_API_KEY',
  
  // 地图版本
  version: '2.0',
  
  // 加载的插件
  plugins: [
    'AMap.Geolocation',      // 定位插件
    'AMap.PlaceSearch',      // 地点搜索
    'AMap.Geocoder',         // 地理编码
    'AMap.AutoComplete',     // 自动完成
    'AMap.DistrictSearch'    // 行政区搜索
  ],
  
  // 默认地图配置
  mapOptions: {
    zoom: 15,                // 默认缩放级别
    center: [102.712251, 25.040609], // 默认中心点（昆明）
    mapStyle: 'amap://styles/normal', // 地图样式
    resizeEnable: true,      // 允许调整大小
    rotateEnable: false,     // 禁用旋转
    pitchEnable: false,      // 禁用倾斜
    scrollWheel: true,       // 允许滚轮缩放
    doubleClickZoom: true,   // 允许双击放大
    dragEnable: true         // 允许拖拽
  },
  
  // 标记配置
  markerOptions: {
    icon: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png',
    size: [25, 34],
    offset: [-12, -34],
    draggable: true
  },
  
  // 定位配置
  geolocationOptions: {
    enableHighAccuracy: true,   // 启用高精度定位
    timeout: 10000,             // 超时时间
    maximumAge: 0,              // 缓存时间
    buttonPosition: 'RB',       // 定位按钮位置
    buttonOffset: [10, 20],     // 定位按钮偏移
    showMarker: true,           // 显示定位标记
    showCircle: true,           // 显示精度圆
    panToLocation: true,        // 定位成功后将定位到的位置作为地图中心点
    zoomToAccuracy: true        // 定位成功后调整地图视野范围使定位位置及精度范围视野内可见
  }
}

// 验证API密钥是否配置
export const isAmapKeyConfigured = () => {
  return AMAP_CONFIG.key && AMAP_CONFIG.key !== 'YOUR_AMAP_API_KEY'
}

// 获取高德地图API URL
export const getAmapApiUrl = () => {
  const plugins = AMAP_CONFIG.plugins.join(',')
  return `https://webapi.amap.com/maps?v=${AMAP_CONFIG.version}&key=${AMAP_CONFIG.key}&plugin=${plugins}`
}

// 高德地图API状态
export const AmapApiStatus = {
  NOT_LOADED: 'not_loaded',
  LOADING: 'loading', 
  LOADED: 'loaded',
  ERROR: 'error'
}

// 检查高德地图API是否已加载
export const checkAmapApiStatus = () => {
  if (typeof window === 'undefined') {
    return AmapApiStatus.NOT_LOADED
  }
  
  if (window.AMap) {
    return AmapApiStatus.LOADED
  }
  
  return AmapApiStatus.NOT_LOADED
}

// 动态加载高德地图API
export const loadAmapApi = () => {
  return new Promise((resolve, reject) => {
    // 检查是否已加载
    if (checkAmapApiStatus() === AmapApiStatus.LOADED) {
      resolve(window.AMap)
      return
    }
    
    // 检查API密钥
    if (!isAmapKeyConfigured()) {
      reject(new Error('高德地图API密钥未配置'))
      return
    }
    
    // 创建script标签
    const script = document.createElement('script')
    script.type = 'text/javascript'
    script.src = getAmapApiUrl()
    script.onerror = () => reject(new Error('高德地图API加载失败'))
    script.onload = () => {
      if (window.AMap) {
        resolve(window.AMap)
      } else {
        reject(new Error('高德地图API对象未找到'))
      }
    }
    
    document.head.appendChild(script)
  })
}

// 错误处理
export const AmapErrors = {
  API_KEY_NOT_CONFIGURED: 'API密钥未配置',
  API_LOAD_FAILED: 'API加载失败',
  GEOLOCATION_FAILED: '定位失败',
  GEOLOCATION_NOT_SUPPORTED: '浏览器不支持定位',
  NETWORK_ERROR: '网络错误'
}
