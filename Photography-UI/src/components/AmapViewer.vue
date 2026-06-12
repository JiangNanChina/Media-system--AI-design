<template>
  <div class="amap-viewer">
    <!-- 地图容器 -->
    <div 
      :id="mapId" 
      :style="{ width: width, height: height }"
      class="amap-container"
    ></div>
    
    <!-- 位置信息覆盖层 -->
    <div class="location-overlay" v-if="showOverlay && location">
      <div class="overlay-content">
        <h4>{{ location.name || '定位点' }}</h4>
        <p>{{ location.address }}</p>
        <small>{{ location.longitude }}, {{ location.latitude }}</small>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const props = defineProps({
  // 地图容器ID
  mapId: {
    type: String,
    default: () => 'amap-viewer-' + Date.now()
  },
  // 地图尺寸
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '300px'
  },
  // 位置信息
  location: {
    type: Object,
    required: true
  },
  // 缩放级别
  zoom: {
    type: Number,
    default: 15
  },
  // 是否显示信息覆盖层
  showOverlay: {
    type: Boolean,
    default: true
  },
  // 标记图标
  markerIcon: {
    type: String,
    default: null
  },
  // 是否禁用交互
  disabled: {
    type: Boolean,
    default: false
  }
})

// 响应式数据
const mapConfig = ref(null)

// 地图相关变量
let map = null
let marker = null
let AMap = null

// 组件挂载
onMounted(async () => {
  await loadMapConfig()
  await loadAmapScript()
  await initMap()
})

// 组件卸载
onUnmounted(() => {
  if (map) {
    map.destroy()
  }
})

// 监听位置变化
watch(() => props.location, (newLocation) => {
  if (newLocation && map) {
    updateLocation(newLocation)
  }
}, { deep: true })

// 加载地图配置
const loadMapConfig = async () => {
  try {
    const response = await request.get('/amap/config')
    mapConfig.value = response
    console.log('📍 地图配置加载成功:', response)
  } catch (error) {
    console.error('❌ 地图配置加载失败:', error)
    // 使用默认配置
    mapConfig.value = { jsApiKey: 'your-default-key' }
  }
}

// 动态加载高德地图JS API
const loadAmapScript = () => {
  return new Promise((resolve, reject) => {
    if (window.AMap) {
      AMap = window.AMap
      resolve()
      return
    }

    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${mapConfig.value.jsApiKey}`
    script.async = true
    
    script.onload = () => {
      AMap = window.AMap
      console.log('✅ 高德地图JS API加载成功')
      resolve()
    }
    
    script.onerror = () => {
      console.error('❌ 高德地图JS API加载失败')
      reject(new Error('高德地图加载失败'))
    }
    
    document.head.appendChild(script)
  })
}

// 初始化地图
const initMap = async () => {
  await nextTick()
  
  try {
    const center = [props.location.longitude, props.location.latitude]
    
    map = new AMap.Map(props.mapId, {
      zoom: props.zoom,
      center: center,
      viewMode: '3D',
      features: ['bg', 'point', 'road', 'building'],
      mapStyle: 'amap://styles/normal',
      dragEnable: !props.disabled,
      zoomEnable: !props.disabled,
      doubleClickZoom: !props.disabled,
      keyboardEnable: false,
      scrollWheel: !props.disabled
    })

    // 创建标记
    const markerOptions = {
      position: center,
      title: props.location.name || '位置'
    }
    
    if (props.markerIcon) {
      markerOptions.icon = props.markerIcon
    }
    
    marker = new AMap.Marker(markerOptions)
    map.add(marker)

    console.log('✅ 地图初始化完成')
  } catch (error) {
    console.error('❌ 地图初始化失败:', error)
    ElMessage.error('地图初始化失败')
  }
}

// 更新位置
const updateLocation = (location) => {
  if (!map || !marker) return
  
  const center = [location.longitude, location.latitude]
  
  map.setCenter(center)
  marker.setPosition(center)
  
  if (location.name) {
    marker.setTitle(location.name)
  }
  
  console.log('📍 地图位置已更新:', location)
}

// 暴露方法给父组件
defineExpose({
  setZoom: (zoom) => {
    if (map) {
      map.setZoom(zoom)
    }
  },
  setCenter: (longitude, latitude) => {
    if (map) {
      map.setCenter([longitude, latitude])
    }
  },
  getMap: () => map,
  getMarker: () => marker
})
</script>

<style scoped>
.amap-viewer {
  position: relative;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.amap-container {
  position: relative;
}

.location-overlay {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 6px;
  padding: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-width: 200px;
  z-index: 1000;
}

.overlay-content h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.overlay-content p {
  margin: 0 0 4px 0;
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
}

.overlay-content small {
  font-size: 11px;
  color: #909399;
}
</style>
