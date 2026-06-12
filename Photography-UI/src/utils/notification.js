import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'

/**
 * 优雅的消息通知工具类
 */
class NotificationManager {
  constructor() {
    this.defaultDuration = 3000
    this.queue = new Map() // 防重复消息队列
  }

  /**
   * 防重复消息
   */
  _shouldShowMessage(message, type) {
    const key = `${type}_${message}`
    const now = Date.now()
    
    if (this.queue.has(key)) {
      const lastTime = this.queue.get(key)
      if (now - lastTime < 1000) { // 1秒内不重复显示相同消息
        return false
      }
    }
    
    this.queue.set(key, now)
    
    // 清理过期的消息记录
    setTimeout(() => {
      this.queue.delete(key)
    }, 5000)
    
    return true
  }

  /**
   * 成功消息
   */
  success(message, options = {}) {
    if (!this._shouldShowMessage(message, 'success')) return
    
    return ElMessage({
      type: 'success',
      message,
      duration: options.duration || this.defaultDuration,
      showClose: true,
      customClass: 'custom-message success-message',
      ...options
    })
  }

  /**
   * 错误消息
   */
  error(message, options = {}) {
    if (!this._shouldShowMessage(message, 'error')) return
    
    return ElMessage({
      type: 'error',
      message,
      duration: options.duration || 5000, // 错误消息显示时间更长
      showClose: true,
      customClass: 'custom-message error-message',
      ...options
    })
  }

  /**
   * 警告消息
   */
  warning(message, options = {}) {
    if (!this._shouldShowMessage(message, 'warning')) return
    
    return ElMessage({
      type: 'warning',
      message,
      duration: options.duration || 4000,
      showClose: true,
      customClass: 'custom-message warning-message',
      ...options
    })
  }

  /**
   * 信息消息
   */
  info(message, options = {}) {
    if (!this._shouldShowMessage(message, 'info')) return
    
    return ElMessage({
      type: 'info',
      message,
      duration: options.duration || this.defaultDuration,
      showClose: true,
      customClass: 'custom-message info-message',
      ...options
    })
  }

  /**
   * 加载消息
   */
  loading(message = '加载中...', options = {}) {
    return ElMessage({
      type: 'info',
      message,
      duration: 0, // 不自动关闭
      showClose: false,
      customClass: 'custom-message loading-message',
      icon: 'Loading',
      ...options
    })
  }

  /**
   * 操作成功通知
   */
  operationSuccess(operation = '操作', options = {}) {
    return this.success(`${operation}成功！`, {
      customClass: 'custom-message operation-success',
      ...options
    })
  }

  /**
   * 操作失败通知
   */
  operationError(operation = '操作', error = '', options = {}) {
    const message = error ? `${operation}失败：${error}` : `${operation}失败，请重试`
    return this.error(message, {
      customClass: 'custom-message operation-error',
      ...options
    })
  }

  /**
   * 网络错误通知
   */
  networkError(options = {}) {
    return this.error('网络连接失败，请检查网络后重试', {
      customClass: 'custom-message network-error',
      duration: 5000,
      ...options
    })
  }

  /**
   * 权限错误通知
   */
  permissionError(options = {}) {
    return this.error('权限不足，请联系管理员', {
      customClass: 'custom-message permission-error',
      duration: 5000,
      ...options
    })
  }

  /**
   * 桌面通知
   */
  notify(title, message, options = {}) {
    return ElNotification({
      title,
      message,
      duration: options.duration || 4500,
      position: 'top-right',
      customClass: 'custom-notification',
      ...options
    })
  }

  /**
   * 成功桌面通知
   */
  notifySuccess(title, message, options = {}) {
    return this.notify(title, message, {
      type: 'success',
      customClass: 'custom-notification success-notification',
      ...options
    })
  }

  /**
   * 错误桌面通知
   */
  notifyError(title, message, options = {}) {
    return this.notify(title, message, {
      type: 'error',
      duration: 6000,
      customClass: 'custom-notification error-notification',
      ...options
    })
  }

  /**
   * 确认对话框
   */
  async confirm(message, title = '确认', options = {}) {
    try {
      await ElMessageBox.confirm(message, title, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        customClass: 'custom-message-box',
        buttonSize: 'default',
        ...options
      })
      return true
    } catch {
      return false
    }
  }

  /**
   * 删除确认对话框
   */
  async confirmDelete(itemName = '此项', options = {}) {
    return this.confirm(
      `确定要删除${itemName}吗？此操作不可撤销。`,
      '删除确认',
      {
        type: 'error',
        confirmButtonText: '删除',
        confirmButtonClass: 'el-button--danger',
        customClass: 'custom-message-box delete-confirm',
        ...options
      }
    )
  }

  /**
   * 批量删除确认对话框
   */
  async confirmBatchDelete(count, itemType = '项', options = {}) {
    return this.confirm(
      `确定要删除选中的 ${count} ${itemType}吗？此操作不可撤销。`,
      '批量删除确认',
      {
        type: 'error',
        confirmButtonText: '批量删除',
        confirmButtonClass: 'el-button--danger',
        customClass: 'custom-message-box batch-delete-confirm',
        ...options
      }
    )
  }

  /**
   * 输入对话框
   */
  async prompt(message, title = '请输入', options = {}) {
    try {
      const { value } = await ElMessageBox.prompt(message, title, {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'custom-message-box',
        inputType: 'text',
        ...options
      })
      return value
    } catch {
      return null
    }
  }

  /**
   * 进度提示
   */
  progress(title, initialProgress = 0) {
    let currentProgress = initialProgress
    
    const notification = ElNotification({
      title,
      message: `进度: ${currentProgress}%`,
      duration: 0,
      type: 'info',
      customClass: 'custom-notification progress-notification',
      dangerouslyUseHTMLString: true
    })

    return {
      update(progress, message) {
        currentProgress = progress
        const progressBar = `
          <div style="margin-bottom: 8px;">${message || title}</div>
          <div style="background: #f0f0f0; border-radius: 4px; overflow: hidden;">
            <div style="background: #409eff; height: 6px; width: ${progress}%; transition: width 0.3s ease;"></div>
          </div>
          <div style="text-align: right; font-size: 12px; color: #909399; margin-top: 4px;">${progress}%</div>
        `
        notification.message = progressBar
      },
      close() {
        notification.close()
      }
    }
  }

  /**
   * 关闭所有消息
   */
  closeAll() {
    ElMessage.closeAll()
  }
}

// 创建单例实例
const notification = new NotificationManager()

export default notification

// 导出常用方法的快捷方式
export const {
  success,
  error,
  warning,
  info,
  loading,
  operationSuccess,
  operationError,
  networkError,
  permissionError,
  notify,
  notifySuccess,
  notifyError,
  confirm,
  confirmDelete,
  confirmBatchDelete,
  prompt,
  progress,
  closeAll
} = notification
