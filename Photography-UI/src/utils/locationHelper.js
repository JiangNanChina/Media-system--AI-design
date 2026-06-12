/**
 * 位置获取辅助工具
 */

/**
 * 获取位置精度等级描述
 * @param {number} accuracy 精度值（米）
 * @returns {object} 包含等级、颜色、建议的对象
 */
export const getAccuracyLevel = (accuracy) => {
  if (accuracy <= 10) {
    return {
      level: 'excellent',
      label: '极高精度',
      color: '#67c23a',
      suggestion: '定位精度极佳，可以放心使用'
    }
  } else if (accuracy <= 20) {
    return {
      level: 'high',
      label: '高精度',
      color: '#409eff',
      suggestion: '定位精度很好，推荐使用'
    }
  } else if (accuracy <= 50) {
    return {
      level: 'medium',
      label: '中等精度',
      color: '#e6a23c',
      suggestion: '定位精度一般，可以使用'
    }
  } else if (accuracy <= 100) {
    return {
      level: 'low',
      label: '低精度',
      color: '#f56c6c',
      suggestion: '定位精度较低，建议重新定位'
    }
  } else {
    return {
      level: 'poor',
      label: '极低精度',
      color: '#909399',
      suggestion: '定位精度很差，强烈建议移至空旷地带重新定位'
    }
  }
}

/**
 * 获取定位环境建议
 * @param {number} accuracy 当前精度
 * @returns {string[]} 建议列表
 */
export const getLocationSuggestions = (accuracy) => {
  const suggestions = []
  
  if (accuracy > 50) {
    suggestions.push('移动到空旷的室外环境')
    suggestions.push('远离高楼大厦和金属建筑物')
    suggestions.push('确保设备GPS功能已开启')
    suggestions.push('等待GPS信号稳定（通常需要1-2分钟）')
  }
  
  if (accuracy > 100) {
    suggestions.push('检查网络连接是否稳定')
    suggestions.push('尝试重启设备的位置服务')
    suggestions.push('避免在地下室或室内密闭空间使用')
  }
  
  return suggestions
}

/**
 * 检查是否为室内环境（基于精度推测）
 * @param {number} accuracy 精度值
 * @returns {boolean} 是否可能在室内
 */
export const isPossiblyIndoor = (accuracy) => {
  return accuracy > 65 // 大于65米精度通常表示可能在室内或信号受阻
}

/**
 * 格式化精度显示
 * @param {number} accuracy 精度值
 * @returns {string} 格式化后的精度字符串
 */
export const formatAccuracy = (accuracy) => {
  if (accuracy < 1) {
    return `±${Math.round(accuracy * 100)}厘米`
  } else if (accuracy < 1000) {
    return `±${Math.round(accuracy)}米`
  } else {
    return `±${(accuracy / 1000).toFixed(1)}公里`
  }
}

/**
 * 获取定位质量评分
 * @param {number} accuracy 精度值
 * @returns {number} 评分 (0-100)
 */
export const getLocationScore = (accuracy) => {
  if (accuracy <= 5) return 100
  if (accuracy <= 10) return 90
  if (accuracy <= 20) return 80
  if (accuracy <= 50) return 60
  if (accuracy <= 100) return 40
  return 20
}
