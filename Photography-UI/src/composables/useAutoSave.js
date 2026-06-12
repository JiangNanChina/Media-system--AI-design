import { ref, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 表单自动保存组合函数
 * @param {Object} options 配置选项
 * @returns {Object} 自动保存相关的响应式数据和方法
 */
export const useAutoSave = (options = {}) => {
  const {
    key = 'auto-save-data',
    delay = 2000,
    onSave,
    onRestore,
    enabled = true,
    showMessages = true
  } = options

  // 响应式状态
  const isSaving = ref(false)
  const lastSaved = ref(null)
  const hasChanges = ref(false)
  const autoSaveEnabled = ref(enabled)

  let saveTimer = null
  let watchStoppers = []

  /**
   * 保存数据到本地存储
   */
  const saveToStorage = (data) => {
    try {
      const saveData = {
        data,
        timestamp: Date.now(),
        version: '1.0'
      }
      localStorage.setItem(key, JSON.stringify(saveData))
      lastSaved.value = new Date()
      hasChanges.value = false
      
      if (showMessages) {
        ElMessage({
          message: '数据已自动保存',
          type: 'success',
          duration: 1500,
          customClass: 'auto-save-message'
        })
      }
    } catch (error) {
      console.warn('自动保存失败:', error)
      if (showMessages) {
        ElMessage({
          message: '自动保存失败',
          type: 'warning',
          duration: 2000
        })
      }
    }
  }

  /**
   * 从本地存储恢复数据
   */
  const restoreFromStorage = () => {
    try {
      const saved = localStorage.getItem(key)
      if (saved) {
        const saveData = JSON.parse(saved)
        return saveData.data
      }
    } catch (error) {
      console.warn('恢复数据失败:', error)
    }
    return null
  }

  /**
   * 获取保存的时间戳
   */
  const getSaveTimestamp = () => {
    try {
      const saved = localStorage.getItem(key)
      if (saved) {
        const saveData = JSON.parse(saved)
        return new Date(saveData.timestamp)
      }
    } catch (error) {
      console.warn('获取保存时间失败:', error)
    }
    return null
  }

  /**
   * 清除保存的数据
   */
  const clearSaved = () => {
    localStorage.removeItem(key)
    lastSaved.value = null
    hasChanges.value = false
  }

  /**
   * 手动触发保存
   */
  const save = async (data) => {
    if (!autoSaveEnabled.value) return

    isSaving.value = true
    
    try {
      if (onSave) {
        await onSave(data)
      } else {
        saveToStorage(data)
      }
    } catch (error) {
      console.error('保存失败:', error)
      if (showMessages) {
        ElMessage({
          message: '保存失败: ' + error.message,
          type: 'error',
          duration: 3000
        })
      }
    } finally {
      isSaving.value = false
    }
  }

  /**
   * 恢复数据
   */
  const restore = () => {
    const savedData = restoreFromStorage()
    if (savedData) {
      if (onRestore) {
        onRestore(savedData)
      }
      lastSaved.value = getSaveTimestamp()
      return savedData
    }
    return null
  }

  /**
   * 检查是否有保存的数据
   */
  const hasSavedData = () => {
    return !!localStorage.getItem(key)
  }

  /**
   * 询问是否恢复数据
   */
  const promptRestore = () => {
    return new Promise((resolve) => {
      if (hasSavedData()) {
        const timestamp = getSaveTimestamp()
        const timeStr = timestamp ? timestamp.toLocaleString() : '未知时间'
        
        ElMessageBox.confirm(
          `检测到有未保存的数据 (${timeStr})，是否恢复？`,
          '恢复数据',
          {
            confirmButtonText: '恢复',
            cancelButtonText: '忽略',
            type: 'info'
          }
        ).then(() => {
          const restored = restore()
          resolve(restored)
        }).catch(() => {
          clearSaved()
          resolve(null)
        })
      } else {
        resolve(null)
      }
    })
  }

  /**
   * 监听数据变化并自动保存
   */
  const watchData = (dataRef, options = {}) => {
    const { immediate = false, deep = true } = options
    
    const stopWatcher = watch(
      dataRef,
      (newValue) => {
        if (!autoSaveEnabled.value) return
        
        hasChanges.value = true
        
        // 清除之前的定时器
        if (saveTimer) {
          clearTimeout(saveTimer)
        }
        
        // 设置新的定时器
        saveTimer = setTimeout(() => {
          save(newValue)
        }, delay)
      },
      { immediate, deep }
    )
    
    watchStoppers.push(stopWatcher)
    return stopWatcher
  }

  /**
   * 启用/禁用自动保存
   */
  const toggle = (enabled) => {
    autoSaveEnabled.value = enabled
    
    if (!enabled && saveTimer) {
      clearTimeout(saveTimer)
      saveTimer = null
    }
  }

  /**
   * 获取格式化的最后保存时间
   */
  const getLastSavedText = () => {
    if (!lastSaved.value) return ''
    
    const now = new Date()
    const diff = now - lastSaved.value
    
    if (diff < 60000) { // 小于1分钟
      return '刚刚保存'
    } else if (diff < 3600000) { // 小于1小时
      return `${Math.floor(diff / 60000)}分钟前保存`
    } else if (diff < 86400000) { // 小于1天
      return `${Math.floor(diff / 3600000)}小时前保存`
    } else {
      return lastSaved.value.toLocaleDateString()
    }
  }

  /**
   * 清理定时器和监听器
   */
  const cleanup = () => {
    if (saveTimer) {
      clearTimeout(saveTimer)
      saveTimer = null
    }
    
    watchStoppers.forEach(stop => stop())
    watchStoppers = []
  }

  // 组件卸载时清理
  onUnmounted(() => {
    cleanup()
  })

  return {
    // 状态
    isSaving,
    lastSaved,
    hasChanges,
    autoSaveEnabled,
    
    // 方法
    save,
    restore,
    clearSaved,
    watchData,
    toggle,
    hasSavedData,
    promptRestore,
    getLastSavedText,
    cleanup
  }
}

/**
 * 表单自动保存的便捷使用方式
 */
export const useFormAutoSave = (formData, options = {}) => {
  const {
    key = `form-auto-save-${window.location.pathname}`,
    ...restOptions
  } = options

  const autoSave = useAutoSave({
    key,
    ...restOptions
  })

  // 自动监听表单数据变化
  autoSave.watchData(formData)

  return autoSave
}

/**
 * 页面离开前提醒保存
 */
export const useBeforeUnloadWarning = (hasUnsavedChanges) => {
  const handleBeforeUnload = (event) => {
    if (hasUnsavedChanges.value) {
      event.preventDefault()
      event.returnValue = '您有未保存的更改，确定要离开吗？'
      return '您有未保存的更改，确定要离开吗？'
    }
  }

  onMounted(() => {
    window.addEventListener('beforeunload', handleBeforeUnload)
  })

  onUnmounted(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
  })
}

export default useAutoSave
