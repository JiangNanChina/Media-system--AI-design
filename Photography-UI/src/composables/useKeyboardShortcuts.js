import { onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 键盘快捷键组合键
 */
export const useKeyboardShortcuts = () => {
  const shortcuts = new Map()
  const listeners = new Set()

  /**
   * 注册快捷键
   * @param {string} keys - 快捷键组合，如 'ctrl+s', 'alt+n', 'ctrl+shift+d'
   * @param {Function} callback - 回调函数
   * @param {Object} options - 选项
   */
  const register = (keys, callback, options = {}) => {
    const {
      description = '',
      preventDefault = true,
      stopPropagation = true,
      target = document,
      disabled = false
    } = options

    const normalizedKeys = normalizeKeys(keys)
    const shortcut = {
      keys: normalizedKeys,
      callback,
      description,
      preventDefault,
      stopPropagation,
      target,
      disabled
    }

    shortcuts.set(keys, shortcut)

    // 如果已经挂载，立即添加监听器
    if (listeners.size > 0) {
      addListener(shortcut)
    }
  }

  /**
   * 注销快捷键
   */
  const unregister = (keys) => {
    const shortcut = shortcuts.get(keys)
    if (shortcut) {
      removeListener(shortcut)
      shortcuts.delete(keys)
    }
  }

  /**
   * 启用/禁用快捷键
   */
  const toggle = (keys, enabled) => {
    const shortcut = shortcuts.get(keys)
    if (shortcut) {
      shortcut.disabled = !enabled
    }
  }

  /**
   * 标准化快捷键字符串
   */
  const normalizeKeys = (keys) => {
    return keys.toLowerCase()
      .replace(/\s+/g, '')
      .split('+')
      .map(key => {
        const keyMap = {
          'command': 'meta',
          'cmd': 'meta',
          'control': 'ctrl',
          'option': 'alt',
          'return': 'enter',
          'escape': 'esc',
          'delete': 'del'
        }
        return keyMap[key] || key
      })
      .sort()
  }

  /**
   * 检查事件是否匹配快捷键
   */
  const matchesShortcut = (event, shortcut) => {
    const eventKeys = []
    
    if (event.ctrlKey || event.metaKey) eventKeys.push('ctrl')
    if (event.altKey) eventKeys.push('alt')
    if (event.shiftKey) eventKeys.push('shift')
    
    const key = event.key.toLowerCase()
    if (!['control', 'alt', 'shift', 'meta'].includes(key)) {
      eventKeys.push(key)
    }

    eventKeys.sort()
    
    return JSON.stringify(eventKeys) === JSON.stringify(shortcut.keys)
  }

  /**
   * 添加键盘事件监听器
   */
  const addListener = (shortcut) => {
    const listener = (event) => {
      if (shortcut.disabled) return
      
      // 忽略在输入框中的按键
      const activeElement = document.activeElement
      if (activeElement && (
        activeElement.tagName === 'INPUT' ||
        activeElement.tagName === 'TEXTAREA' ||
        activeElement.contentEditable === 'true'
      )) {
        // 允许某些全局快捷键在输入框中工作
        const globalKeys = ['ctrl+s', 'ctrl+z', 'ctrl+y', 'esc']
        const currentKeys = shortcut.keys.join('+')
        if (!globalKeys.includes(currentKeys)) {
          return
        }
      }

      if (matchesShortcut(event, shortcut)) {
        if (shortcut.preventDefault) {
          event.preventDefault()
        }
        if (shortcut.stopPropagation) {
          event.stopPropagation()
        }
        
        shortcut.callback(event)
      }
    }

    shortcut.listener = listener
    shortcut.target.addEventListener('keydown', listener)
    listeners.add(listener)
  }

  /**
   * 移除键盘事件监听器
   */
  const removeListener = (shortcut) => {
    if (shortcut.listener) {
      shortcut.target.removeEventListener('keydown', shortcut.listener)
      listeners.delete(shortcut.listener)
    }
  }

  /**
   * 初始化所有监听器
   */
  const initListeners = () => {
    shortcuts.forEach(shortcut => {
      addListener(shortcut)
    })
  }

  /**
   * 清理所有监听器
   */
  const cleanupListeners = () => {
    shortcuts.forEach(shortcut => {
      removeListener(shortcut)
    })
    listeners.clear()
  }

  /**
   * 获取所有已注册的快捷键
   */
  const getShortcuts = () => {
    return Array.from(shortcuts.entries()).map(([keys, shortcut]) => ({
      keys,
      description: shortcut.description,
      disabled: shortcut.disabled
    }))
  }

  /**
   * 显示快捷键帮助
   */
  const showHelp = () => {
    const shortcutList = getShortcuts()
      .filter(s => s.description && !s.disabled)
      .map(s => `${s.keys}: ${s.description}`)
      .join('\n')
    
    if (shortcutList) {
      ElMessage({
        message: `快捷键帮助:\n${shortcutList}`,
        type: 'info',
        duration: 5000,
        showClose: true
      })
    }
  }

  onMounted(() => {
    nextTick(() => {
      initListeners()
    })
  })

  onUnmounted(() => {
    cleanupListeners()
  })

  return {
    register,
    unregister,
    toggle,
    getShortcuts,
    showHelp
  }
}

/**
 * 常用快捷键预设
 */
export const commonShortcuts = {
  save: 'ctrl+s',
  search: 'ctrl+f',
  new: 'ctrl+n',
  refresh: 'f5',
  help: 'f1',
  close: 'esc',
  copy: 'ctrl+c',
  paste: 'ctrl+v',
  undo: 'ctrl+z',
  redo: 'ctrl+y',
  selectAll: 'ctrl+a'
}

/**
 * 应用级别快捷键
 */
export const useAppShortcuts = (router) => {
  const { register } = useKeyboardShortcuts()

  // 全局搜索
  register(commonShortcuts.search, (event) => {
    const searchInput = document.querySelector('.search-input input')
    if (searchInput) {
      searchInput.focus()
    }
  }, {
    description: '全局搜索'
  })

  // 刷新页面
  register(commonShortcuts.refresh, () => {
    window.location.reload()
  }, {
    description: '刷新页面'
  })

  // 显示帮助
  register(commonShortcuts.help, () => {
    // 这里可以打开帮助对话框
    ElMessage.info('按 ? 查看当前页面的快捷键')
  }, {
    description: '显示帮助'
  })

  // 导航快捷键
  register('alt+1', () => {
    router.push('/dashboard')
  }, {
    description: '跳转到首页'
  })

  register('alt+2', () => {
    router.push('/equipment/list')
  }, {
    description: '跳转到设备管理'
  })

  register('alt+3', () => {
    router.push('/borrow/list')
  }, {
    description: '跳转到借还管理'
  })

  register('alt+4', () => {
    router.push('/user/list')
  }, {
    description: '跳转到用户管理'
  })
}

/**
 * 表单快捷键
 */
export const useFormShortcuts = (options = {}) => {
  const { register } = useKeyboardShortcuts()
  const {
    onSave,
    onCancel,
    onNew,
    onDelete
  } = options

  if (onSave) {
    register(commonShortcuts.save, onSave, {
      description: '保存表单'
    })
  }

  if (onCancel) {
    register(commonShortcuts.close, onCancel, {
      description: '取消/关闭'
    })
  }

  if (onNew) {
    register(commonShortcuts.new, onNew, {
      description: '新建'
    })
  }

  if (onDelete) {
    register('delete', onDelete, {
      description: '删除'
    })
  }
}

/**
 * 表格快捷键
 */
export const useTableShortcuts = (options = {}) => {
  const { register } = useKeyboardShortcuts()
  const {
    onRefresh,
    onNew,
    onDelete,
    onEdit,
    onSearch
  } = options

  if (onRefresh) {
    register('ctrl+r', onRefresh, {
      description: '刷新表格'
    })
  }

  if (onNew) {
    register('ctrl+shift+n', onNew, {
      description: '新增记录'
    })
  }

  if (onDelete) {
    register('ctrl+shift+d', onDelete, {
      description: '删除选中记录'
    })
  }

  if (onEdit) {
    register('ctrl+e', onEdit, {
      description: '编辑选中记录'
    })
  }

  if (onSearch) {
    register('ctrl+shift+f', onSearch, {
      description: '高级搜索'
    })
  }
}

export default useKeyboardShortcuts
