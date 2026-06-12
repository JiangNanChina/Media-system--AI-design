<template>
  <div class="amap-picker">
    <!-- 地址搜索框 -->
    <div class="search-box" v-if="showSearch">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索地点或输入地址"
        clearable
        @keyup.enter="searchLocation"
        class="search-input"
      >
        <template #append>
          <el-button @click="searchLocation" :loading="searching">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 地图容器 -->
    <div 
      :id="mapId" 
      :style="{ width: width, height: height }"
      class="amap-container"
    ></div>

    <!-- 位置信息面板 -->
    <div class="location-info" v-if="showLocationInfo && selectedLocation">
      <div class="info-header">
        <el-icon><LocationInformation /></el-icon>
        <span>选中位置</span>
      </div>
      <div class="info-content">
        <p><strong>地址：</strong>{{ selectedLocation.address }}</p>
        <p><strong>坐标：</strong>{{ selectedLocation.longitude }}, {{ selectedLocation.latitude }}</p>
        <div v-if="selectedLocation.pois && selectedLocation.pois.length > 0" class="nearby-pois">
          <strong>附近地点：</strong>
          <div class="poi-list">
            <span 
              v-for="poi in selectedLocation.pois.slice(0, 3)" 
              :key="poi.id"
              class="poi-tag"
            >
              {{ poi.name }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="actions" v-if="showActions">
      <el-button @click="getCurrentLocation" :loading="locating">
        <el-icon><Aim /></el-icon>
        当前位置
      </el-button>
      <el-button type="primary" @click="confirmLocation" :disabled="!selectedLocation">
        确认位置
      </el-button>
      <el-button @click="cancelSelection" v-if="showCancel">
        取消
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, LocationInformation, Aim } from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  // 地图容器ID
  mapId: {
    type: String,
    default: () => 'amap-' + Date.now()
  },
  // 地图尺寸
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '400px'
  },
  // 初始中心点
  center: {
    type: Array,
    default: () => [104.402790, 31.093902] // 默认成都位置
  },
  // 初始缩放级别
  zoom: {
    type: Number,
    default: 15
  },
  // 是否显示搜索框
  showSearch: {
    type: Boolean,
    default: true
  },
  // 是否显示位置信息
  showLocationInfo: {
    type: Boolean,
    default: true
  },
  // 是否显示操作按钮
  showActions: {
    type: Boolean,
    default: true
  },
  // 是否显示取消按钮
  showCancel: {
    type: Boolean,
    default: false
  },
  // 是否允许点击选择位置
  clickable: {
    type: Boolean,
    default: true
  },
  // 预设位置
  initialLocation: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['location-selected', 'location-confirmed', 'cancel'])

// 响应式数据
const searchKeyword = ref('')
const searching = ref(false)
const locating = ref(false)
const selectedLocation = ref(null)
const mapConfig = ref(null)

// 地图相关变量
let map = null
let marker = null
let AMap = null

// 组件挂载
onMounted(async () => {
  await loadMapConfig()
  await loadAmapScript()
  initMap()
})

// 组件卸载
onUnmounted(() => {
  if (map) {
    map.destroy()
  }
})

// 监听初始位置变化
watch(() => props.initialLocation, (newLocation) => {
  if (newLocation && map) {
    setMarkerPosition(newLocation.longitude, newLocation.latitude)
    map.setCenter([newLocation.longitude, newLocation.latitude])
    selectedLocation.value = newLocation
  }
}, { immediate: true })

// 加载地图配置
const loadMapConfig = async () => {
  try {
    const response = await request.get('/amap/config')
    mapConfig.value = response
    console.log('📍 地图配置加载成功:', response)
  } catch (error) {
    console.error('❌ 地图配置加载失败:', error)
    ElMessage.error('地图配置加载失败')
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
    const apiKey = mapConfig.value?.jsApiKey || mapConfig.value?.data?.jsApiKey
    
    // 检查API Key是否有效
    if (!apiKey || apiKey === 'undefined') {
      console.warn('⚠️ 无效的高德地图API Key')
      ElMessage.warning('地图API Key无效，请联系管理员配置正确的高德地图API Key')
      reject(new Error('高德地图API Key未配置'))
      return
    }
    
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${apiKey}&plugin=AMap.PlaceSearch,AMap.Geocoder`
    script.async = true
    
    console.log('🔑 高德地图API Key已配置')
    console.log('📦 地图配置:', mapConfig.value)
    
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
    if (!window.AMap) {
      console.error('❌ AMap对象未加载')
      throw new Error('高德地图API未正确加载')
    }
    
    AMap = window.AMap
    console.log('🗺️ 开始初始化地图，容器ID:', props.mapId)
    
    map = new AMap.Map(props.mapId, {
      zoom: props.zoom,
      center: props.center,
      viewMode: '3D',
      features: ['bg', 'point', 'road', 'building'],
      mapStyle: 'amap://styles/normal'
    })

    // 创建标记
    marker = new AMap.Marker({
      position: props.center,
      draggable: true,
      cursor: 'move'
    })

    // 添加标记到地图
    map.add(marker)

    // 监听地图点击事件
    if (props.clickable) {
      map.on('click', onMapClick)
    }

    // 监听标记拖拽事件
    marker.on('dragend', onMarkerDragEnd)

    console.log('✅ 地图初始化完成')

    // 如果有初始位置，设置标记
    if (props.initialLocation) {
      setMarkerPosition(props.initialLocation.longitude, props.initialLocation.latitude)
      selectedLocation.value = props.initialLocation
    }
  } catch (error) {
    console.error('❌ 地图初始化失败:', error)
    ElMessage.error('地图初始化失败')
  }
}

// 地图点击事件
const onMapClick = async (e) => {
  const longitude = e.lnglat.getLng()
  const latitude = e.lnglat.getLat()
  
  setMarkerPosition(longitude, latitude)
  await getLocationInfo(longitude, latitude)
}

// 标记拖拽结束事件
const onMarkerDragEnd = async (e) => {
  const longitude = e.lnglat.getLng()
  const latitude = e.lnglat.getLat()
  
  await getLocationInfo(longitude, latitude)
}

// 设置标记位置
const setMarkerPosition = (longitude, latitude) => {
  if (marker) {
    marker.setPosition([longitude, latitude])
  }
  if (map) {
    map.setCenter([longitude, latitude])
  }
}

// 获取位置信息
const getLocationInfo = async (longitude, latitude) => {
  try {
    const response = await request.get('/amap/checkin/location-info', {
      params: { longitude, latitude }
    })
    
    selectedLocation.value = {
      longitude: longitude,
      latitude: latitude,
      address: response.formattedAddress,
      pois: response.nearbyPois || [],
      staticMapUrl: response.staticMapUrl
    }
    
    emit('location-selected', selectedLocation.value)
    console.log('📍 位置信息获取成功:', selectedLocation.value)
  } catch (error) {
    console.error('❌ 位置信息获取失败:', error)
    ElMessage.error('位置信息获取失败')
  }
}

// 搜索位置
const searchLocation = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键字')
    return
  }

  searching.value = true
  try {
    const response = await request.get('/amap/place/search', {
      params: {
        keywords: searchKeyword.value,
        city: '成都' // 可以根据需要配置
      }
    })

    if (response.pois && response.pois.length > 0) {
      const poi = response.pois[0]
      const [longitude, latitude] = poi.location.split(',').map(Number)
      
      setMarkerPosition(longitude, latitude)
      await getLocationInfo(longitude, latitude)
      
      ElMessage.success('搜索成功')
    } else {
      ElMessage.warning('未找到相关地点')
    }
  } catch (error) {
    console.error('❌ 地点搜索失败:', error)
    ElMessage.error('地点搜索失败')
  } finally {
    searching.value = false
  }
}

// 高精度位置获取策略
const getBestPosition = async () => {
  return new Promise((resolve, reject) => {
    let bestPosition = null
    let bestAccuracy = Infinity
    let attempts = 0
    const maxAttempts = 5
    const targetAccuracy = 20
    const maxWaitTime = 20000
    
    const startTime = Date.now()
    
    const options = {
      enableHighAccuracy: true,
      timeout: 30000,
      maximumAge: 0
    }
    
    const watchId = navigator.geolocation.watchPosition(
      (position) => {
        attempts++
        const accuracy = position.coords.accuracy
        const elapsed = Date.now() - startTime
        
        console.log(`🎯 地图定位第${attempts}次: 精度±${Math.round(accuracy)}米`)
        
        if (!bestPosition || accuracy < bestAccuracy) {
          bestPosition = position
          bestAccuracy = accuracy
        }
        
        if (accuracy <= targetAccuracy || attempts >= maxAttempts || elapsed >= maxWaitTime) {
          navigator.geolocation.clearWatch(watchId)
          resolve(bestPosition)
        }
      },
      (error) => {
        navigator.geolocation.clearWatch(watchId)
        if (bestPosition && bestAccuracy < 100) {
          resolve(bestPosition)
        } else {
          reject(error)
        }
      },
      options
    )
    
    setTimeout(() => {
      navigator.geolocation.clearWatch(watchId)
      if (bestPosition) {
        resolve(bestPosition)
      } else {
        reject(new Error('定位超时'))
      }
    }, maxWaitTime)
  })
}

// 获取当前位置
const getCurrentLocation = async () => {
  if (!navigator.geolocation) {
    ElMessage.error('浏览器不支持定位功能')
    return
  }

  locating.value = true
  
  try {
    const position = await getBestPosition()
    const longitude = position.coords.longitude
    const latitude = position.coords.latitude
    const accuracy = position.coords.accuracy
    
    console.log('🎯 地图最佳定位精度:', accuracy, '米')
    
    setMarkerPosition(longitude, latitude)
    await getLocationInfo(longitude, latitude)
    
    // 根据精度显示不同的提示信息
    if (accuracy <= 20) {
      ElMessage.success(`高精度定位成功 (±${Math.round(accuracy)}米)`)
    } else if (accuracy <= 50) {
      ElMessage.success(`定位成功，精度 ±${Math.round(accuracy)}米`)
    } else {
      ElMessage.warning(`定位精度较低 (±${Math.round(accuracy)}米)，建议移至空旷地带`)
    }
  } catch (error) {
    console.error('❌ 定位失败:', error)
    let errorMessage = '定位失败'
    
    switch(error.code) {
      case error.PERMISSION_DENIED:
        errorMessage = '定位被拒绝，请允许网站访问位置信息'
        break
      case error.POSITION_UNAVAILABLE:
        errorMessage = '位置信息不可用，请检查GPS或网络'
        break
      case error.TIMEOUT:
        errorMessage = '定位超时，请重试'
        break
      default:
        errorMessage = '定位失败: ' + error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    locating.value = false
  }
}

// 确认位置
const confirmLocation = () => {
  if (!selectedLocation.value) {
    ElMessage.warning('请先选择位置')
    return
  }
  
  emit('location-confirmed', selectedLocation.value)
  ElMessage.success('位置已确认')
}

// 取消选择
const cancelSelection = () => {
  emit('cancel')
}

// 暴露方法给父组件
defineExpose({
  setCenter: (longitude, latitude) => {
    if (map) {
      map.setCenter([longitude, latitude])
      setMarkerPosition(longitude, latitude)
    }
  },
  getSelectedLocation: () => selectedLocation.value,
  refreshLocation: () => {
    if (selectedLocation.value) {
      getLocationInfo(selectedLocation.value.longitude, selectedLocation.value.latitude)
    }
  }
})
</script>

<style scoped>
.amap-picker {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.search-box {
  padding: 10px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.search-input {
  width: 100%;
}

.amap-container {
  position: relative;
}

.location-info {
  padding: 15px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}

.info-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-weight: 600;
  color: #303133;
}

.info-header .el-icon {
  margin-right: 5px;
  color: #409eff;
}

.info-content p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.nearby-pois {
  margin-top: 10px;
}

.poi-list {
  margin-top: 5px;
}

.poi-tag {
  display: inline-block;
  padding: 2px 8px;
  margin: 2px 4px 2px 0;
  background: #f0f2f5;
  border-radius: 12px;
  font-size: 12px;
  color: #606266;
}

.actions {
  padding: 15px;
  background: #f5f7fa;
  border-top: 1px solid #ebeef5;
  text-align: right;
}

.actions .el-button {
  margin-left: 10px;
}
</style>
