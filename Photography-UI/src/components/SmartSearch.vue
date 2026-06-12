<template>
  <div class="smart-search">
    <el-input
      ref="searchInput"
      v-model="searchValue"
      :placeholder="placeholder"
      :size="size"
      :clearable="clearable"
      :loading="searching"
      class="search-input"
      @input="handleInput"
      @clear="handleClear"
      @keyup.enter="handleSearch"
      @focus="handleFocus"
      @blur="handleBlur"
    >
      <template #prefix>
        <el-icon class="search-icon">
          <Search />
        </el-icon>
      </template>
      
      <template #suffix>
        <div class="search-actions">
          <el-button
            v-if="searchValue && !searching"
            type="text"
            size="small"
            class="search-btn"
            @click="handleSearch"
          >
            搜索
          </el-button>
          <LoadingSpinner
            v-if="searching"
            type="pulse"
            size="small"
          />
        </div>
      </template>
    </el-input>
    
    <!-- 搜索建议下拉框 -->
    <div 
      v-if="showSuggestions && suggestions.length > 0"
      class="search-suggestions"
      :class="{ 'suggestions-visible': suggestionsVisible }"
    >
      <div class="suggestions-header">
        <span class="suggestions-title">搜索建议</span>
        <el-button 
          type="text" 
          size="small" 
          @click="clearHistory"
        >
          清除历史
        </el-button>
      </div>
      
      <div class="suggestions-list">
        <div
          v-for="(suggestion, index) in suggestions"
          :key="index"
          class="suggestion-item"
          :class="{ 'suggestion-active': index === activeSuggestionIndex }"
          @click="selectSuggestion(suggestion)"
          @mouseenter="activeSuggestionIndex = index"
        >
          <el-icon class="suggestion-icon">
            <component :is="suggestion.type === 'history' ? 'Clock' : 'Search'" />
          </el-icon>
          <span class="suggestion-text">{{ suggestion.text }}</span>
          <el-tag 
            v-if="suggestion.count" 
            size="small" 
            type="info"
            class="suggestion-count"
          >
            {{ suggestion.count }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { Search, Clock } from '@element-plus/icons-vue'
import LoadingSpinner from './LoadingSpinner.vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入搜索关键词'
  },
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['large', 'default', 'small'].includes(value)
  },
  clearable: {
    type: Boolean,
    default: true
  },
  suggestions: {
    type: Array,
    default: () => []
  },
  showSuggestions: {
    type: Boolean,
    default: true
  },
  searchDelay: {
    type: Number,
    default: 300
  },
  maxHistory: {
    type: Number,
    default: 10
  },
  storageKey: {
    type: String,
    default: 'search-history'
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'clear', 'suggestion-select'])

// 响应式数据
const searchInput = ref()
const searchValue = ref(props.modelValue)
const searching = ref(false)
const suggestionsVisible = ref(false)
const activeSuggestionIndex = ref(-1)
const searchHistory = ref([])

let searchTimer = null

// 计算属性
const suggestions = computed(() => {
  if (!searchValue.value.trim()) {
    return searchHistory.value.map(item => ({
      text: item,
      type: 'history'
    }))
  }
  
  // 合并历史搜索和建议搜索
  const historySuggestions = searchHistory.value
    .filter(item => item.toLowerCase().includes(searchValue.value.toLowerCase()))
    .map(item => ({
      text: item,
      type: 'history'
    }))
  
  const propSuggestions = props.suggestions.map(item => ({
    text: typeof item === 'string' ? item : item.text,
    count: typeof item === 'object' ? item.count : null,
    type: 'suggestion'
  }))
  
  return [...historySuggestions, ...propSuggestions].slice(0, 8)
})

// 监听modelValue变化
watch(() => props.modelValue, (newValue) => {
  searchValue.value = newValue
})

// 监听搜索值变化
watch(searchValue, (newValue) => {
  emit('update:modelValue', newValue)
})

// 处理输入
const handleInput = (value) => {
  searchValue.value = value
  
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  if (value.trim()) {
    searching.value = true
    searchTimer = setTimeout(() => {
      handleSearch()
    }, props.searchDelay)
  } else {
    searching.value = false
    suggestionsVisible.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  const keyword = searchValue.value.trim()
  if (keyword) {
    // 添加到搜索历史
    addToHistory(keyword)
    // 触发搜索事件
    emit('search', keyword)
  }
  
  searching.value = false
  suggestionsVisible.value = false
  searchInput.value?.blur()
}

// 处理清除
const handleClear = () => {
  searchValue.value = ''
  searching.value = false
  suggestionsVisible.value = false
  emit('clear')
}

// 处理聚焦
const handleFocus = () => {
  if (props.showSuggestions && suggestions.value.length > 0) {
    suggestionsVisible.value = true
    activeSuggestionIndex.value = -1
  }
}

// 处理失焦
const handleBlur = () => {
  // 延迟隐藏建议，允许点击建议项
  setTimeout(() => {
    suggestionsVisible.value = false
    activeSuggestionIndex.value = -1
  }, 200)
}

// 选择建议
const selectSuggestion = (suggestion) => {
  searchValue.value = suggestion.text
  emit('suggestion-select', suggestion)
  nextTick(() => {
    handleSearch()
  })
}

// 添加到搜索历史
const addToHistory = (keyword) => {
  // 移除重复项
  const index = searchHistory.value.indexOf(keyword)
  if (index > -1) {
    searchHistory.value.splice(index, 1)
  }
  
  // 添加到开头
  searchHistory.value.unshift(keyword)
  
  // 限制历史数量
  if (searchHistory.value.length > props.maxHistory) {
    searchHistory.value = searchHistory.value.slice(0, props.maxHistory)
  }
  
  // 保存到本地存储
  saveHistory()
}

// 清除搜索历史
const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem(props.storageKey)
  suggestionsVisible.value = false
}

// 保存搜索历史
const saveHistory = () => {
  try {
    localStorage.setItem(props.storageKey, JSON.stringify(searchHistory.value))
  } catch (error) {
    console.warn('保存搜索历史失败:', error)
  }
}

// 加载搜索历史
const loadHistory = () => {
  try {
    const saved = localStorage.getItem(props.storageKey)
    if (saved) {
      searchHistory.value = JSON.parse(saved)
    }
  } catch (error) {
    console.warn('加载搜索历史失败:', error)
  }
}

// 键盘导航
const handleKeydown = (event) => {
  if (!suggestionsVisible.value || suggestions.value.length === 0) return
  
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      activeSuggestionIndex.value = Math.min(
        activeSuggestionIndex.value + 1,
        suggestions.value.length - 1
      )
      break
    case 'ArrowUp':
      event.preventDefault()
      activeSuggestionIndex.value = Math.max(activeSuggestionIndex.value - 1, -1)
      break
    case 'Enter':
      event.preventDefault()
      if (activeSuggestionIndex.value >= 0) {
        selectSuggestion(suggestions.value[activeSuggestionIndex.value])
      } else {
        handleSearch()
      }
      break
    case 'Escape':
      suggestionsVisible.value = false
      activeSuggestionIndex.value = -1
      searchInput.value?.blur()
      break
  }
}

// 组件挂载
onMounted(() => {
  loadHistory()
  document.addEventListener('keydown', handleKeydown)
})

// 组件卸载
onUnmounted(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  document.removeEventListener('keydown', handleKeydown)
})

// 暴露方法
defineExpose({
  focus: () => searchInput.value?.focus(),
  blur: () => searchInput.value?.blur(),
  clear: handleClear
})
</script>

<style scoped>
.smart-search {
  position: relative;
  width: 100%;
}

.search-input {
  width: 100%;
}

.search-input :deep(.el-input__inner) {
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.search-input :deep(.el-input__inner:focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.search-icon {
  color: #909399;
  transition: color 0.3s ease;
}

.search-input:hover .search-icon {
  color: #409eff;
}

.search-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.search-btn {
  color: #409eff;
  padding: 0 8px;
  font-size: 12px;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

/* 搜索建议样式 */
.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 1000;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  margin-top: 4px;
  max-height: 300px;
  overflow-y: auto;
  opacity: 0;
  transform: translateY(-10px);
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  pointer-events: none;
}

.suggestions-visible {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.suggestions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
  border-radius: 8px 8px 0 0;
}

.suggestions-title {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.suggestions-list {
  padding: 8px 0;
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 4px;
  margin: 0 8px;
}

.suggestion-item:hover,
.suggestion-active {
  background: rgba(64, 158, 255, 0.06);
}

.suggestion-icon {
  margin-right: 12px;
  color: #c0c4cc;
  font-size: 14px;
  flex-shrink: 0;
}

.suggestion-text {
  flex: 1;
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suggestion-count {
  margin-left: 8px;
  font-size: 11px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-suggestions {
    max-height: 250px;
  }
  
  .suggestions-header {
    padding: 10px 12px;
  }
  
  .suggestion-item {
    padding: 10px 12px;
    margin: 0 4px;
  }
  
  .suggestion-text {
    font-size: 13px;
  }
  
  .search-btn {
    display: none;
  }
}
</style>
