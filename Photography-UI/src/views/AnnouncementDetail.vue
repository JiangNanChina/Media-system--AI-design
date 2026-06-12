<template>
  <div class="announcement-detail">
    <!-- 加载状态 -->
    <el-skeleton v-if="loading" :rows="8" animated />
    
    <!-- 公告详情内容 -->
    <div v-else-if="announcement" class="detail-content">
      <!-- 头部信息 -->
      <div class="detail-header">
        <el-page-header @back="goBack" content="公告详情">
          <template #extra>
            <el-button v-if="userStore.isAdmin" type="primary" @click="editAnnouncement">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
          </template>
        </el-page-header>
      </div>
      
      <!-- 公告内容 -->
      <el-card class="announcement-card" shadow="never">
        <!-- 标题和状态 -->
        <div class="announcement-header">
          <h1 class="announcement-title">{{ announcement.title }}</h1>
          <div class="announcement-badges">
            <el-tag :type="getTypeColor(announcement.type)" size="large">
              {{ getTypeText(announcement.type) }}
            </el-tag>
            <el-tag 
              v-if="announcement.priority >= 8" 
              type="danger" 
              size="large"
              class="ml-2"
            >
              重要
            </el-tag>
            <el-tag 
              :type="announcement.published ? 'success' : 'info'" 
              size="large"
              class="ml-2"
            >
              {{ announcement.published ? '已发布' : '未发布' }}
            </el-tag>
          </div>
        </div>
        
        <!-- 元信息 -->
        <div class="announcement-meta">
          <div class="meta-row">
            <el-icon><User /></el-icon>
            <span>发布者：{{ announcement.createdByName || announcement.createdByUsername }}</span>
          </div>
          <div class="meta-row">
            <el-icon><Calendar /></el-icon>
            <span>创建时间：{{ formatTime(announcement.createdAt) }}</span>
          </div>
          <div v-if="announcement.publishedAt" class="meta-row">
            <el-icon><Clock /></el-icon>
            <span>发布时间：{{ formatTime(announcement.publishedAt) }}</span>
          </div>
          <div class="meta-row">
            <el-icon><View /></el-icon>
            <span>查看次数：{{ announcement.viewCount || 0 }}</span>
          </div>
          <div class="meta-row">
            <el-icon><Star /></el-icon>
            <span>优先级：{{ announcement.priority }}</span>
          </div>
        </div>
        
        <!-- 分割线 -->
        <el-divider />
        
        <!-- 公告内容 -->
        <div class="announcement-content">
          <div class="content-html" v-html="formatContent(announcement.content)"></div>
        </div>
        
        <!-- 底部操作 -->
        <div v-if="userStore.isAdmin" class="announcement-actions">
          <el-divider />
          <div class="action-buttons">
            <el-button 
              v-if="!announcement.published" 
              type="success" 
              @click="publishAnnouncement"
              :loading="publishing"
            >
              <el-icon><Upload /></el-icon>
              发布公告
            </el-button>
            <el-button 
              v-else 
              type="warning" 
              @click="unpublishAnnouncement"
              :loading="unpublishing"
            >
              <el-icon><Download /></el-icon>
              取消发布
            </el-button>
            <el-button type="primary" @click="editAnnouncement">
              <el-icon><Edit /></el-icon>
              编辑公告
            </el-button>
            <el-button type="danger" @click="deleteAnnouncement">
              <el-icon><Delete /></el-icon>
              删除公告
            </el-button>
          </div>
        </div>
      </el-card>
      
      <!-- 相关公告推荐 -->
      <el-card v-if="relatedAnnouncements.length > 0" class="related-announcements" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Document /></el-icon>
            <span>相关公告</span>
          </div>
        </template>
        <div class="related-list">
          <div 
            v-for="related in relatedAnnouncements" 
            :key="related.id"
            class="related-item"
            @click="viewRelated(related.id)"
          >
            <div class="related-title">
              <el-icon class="related-icon"><Document /></el-icon>
              {{ related.title }}
            </div>
            <div class="related-meta">
              <el-tag :type="getTypeColor(related.type)" size="small">
                {{ getTypeText(related.type) }}
              </el-tag>
              <span class="related-time">{{ formatTime(related.createdAt) }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 错误状态 -->
    <el-result
      v-else
      icon="warning"
      title="公告不存在"
      sub-title="您访问的公告可能已被删除或不存在"
    >
      <template #extra>
        <el-button type="primary" @click="goBack">返回</el-button>
        <el-button @click="goToAnnouncementList">查看公告列表</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit, User, Calendar, Clock, View, Star, Upload, Download, Delete, Document
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { formatTime } from '@/utils/time'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(true)
const publishing = ref(false)
const unpublishing = ref(false)
const announcement = ref(null)
const relatedAnnouncements = ref([])

// 获取公告详情
const fetchAnnouncementDetail = async () => {
  try {
    loading.value = true
    const response = await request.get(`/announcements/${route.params.id}`)
    if (response.data) {
      announcement.value = response.data
      // 获取相关公告
      await fetchRelatedAnnouncements()
    }
  } catch (error) {
    console.error('获取公告详情失败:', error)
    if (error.response?.status === 404) {
      ElMessage.error('公告不存在或已被删除')
    } else if (error.response?.status === 400) {
      ElMessage.error('请求参数错误，公告ID无效')
    } else {
      ElMessage.error('获取公告详情失败: ' + (error.response?.data?.message || error.message))
    }
  } finally {
    loading.value = false
  }
}

// 获取相关公告
const fetchRelatedAnnouncements = async () => {
  try {
    const response = await request.get('/announcements/public/paged', {
      params: {
        page: 0,
        size: 5,
        type: announcement.value.type
      }
    })
    if (response.data?.content) {
      // 排除当前公告
      relatedAnnouncements.value = response.data.content
        .filter(item => item.id !== announcement.value.id)
        .slice(0, 3)
    }
  } catch (error) {
    console.error('获取相关公告失败:', error)
  }
}

// 格式化内容
const formatContent = (content) => {
  if (!content) return ''
  // 简单的换行处理
  return content.replace(/\n/g, '<br/>')
}

// 获取类型颜色
const getTypeColor = (type) => {
  const colorMap = {
    'SYSTEM': 'danger',
    'IMPORTANT': 'warning', 
    'GENERAL': 'primary',
    'ACTIVITY': 'success'
  }
  return colorMap[type] || 'primary'
}

// 获取类型文本
const getTypeText = (type) => {
  const textMap = {
    'SYSTEM': '系统通知',
    'IMPORTANT': '重要公告',
    'GENERAL': '一般通知', 
    'ACTIVITY': '活动公告'
  }
  return textMap[type] || '一般通知'
}

// 返回上一页
const goBack = () => {
  // 检查公告浏览历史
  const announcementHistory = JSON.parse(sessionStorage.getItem('announcementHistory') || '[]')
  console.log('当前公告历史:', announcementHistory)
  console.log('当前公告ID:', route.params.id)
  
  if (announcementHistory.length > 0) {
    // 获取上一个公告ID
    const previousAnnouncementId = announcementHistory.pop()
    console.log('返回到公告:', previousAnnouncementId)
    
    // 更新历史记录
    sessionStorage.setItem('announcementHistory', JSON.stringify(announcementHistory))
    
    // 跳转到上一个公告
    router.push(`/announcement/${previousAnnouncementId}`)
    return
  }
  
  console.log('没有公告历史，返回到首页')
  // 如果没有公告历史记录，返回到首页
  router.push('/dashboard')
}

// 跳转到公告列表
const goToAnnouncementList = () => {
  if (userStore.isAdmin) {
    router.push('/announcement/list')
  } else {
    router.push('/dashboard')
  }
}

// 编辑公告
const editAnnouncement = () => {
  if (userStore.isAdmin) {
    router.push(`/announcement/list?edit=${announcement.value.id}`)
  } else {
    ElMessage.warning('您没有编辑权限')
  }
}

// 发布公告
const publishAnnouncement = async () => {
  try {
    publishing.value = true
    await request.put(`/announcements/${announcement.value.id}/publish`)
    ElMessage.success('公告发布成功')
    announcement.value.published = true
    announcement.value.publishedAt = new Date().toISOString()
  } catch (error) {
    console.error('发布公告失败:', error)
    ElMessage.error('发布公告失败')
  } finally {
    publishing.value = false
  }
}

// 取消发布公告
const unpublishAnnouncement = async () => {
  try {
    unpublishing.value = true
    await request.put(`/announcements/${announcement.value.id}/unpublish`)
    ElMessage.success('取消发布成功')
    announcement.value.published = false
    announcement.value.publishedAt = null
  } catch (error) {
    console.error('取消发布失败:', error)
    ElMessage.error('取消发布失败')
  } finally {
    unpublishing.value = false
  }
}

// 删除公告
const deleteAnnouncement = async () => {
  try {
    await ElMessageBox.confirm(
      '此操作将永久删除该公告，是否继续？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    await request.delete(`/announcements/${announcement.value.id}`)
    ElMessage.success('删除成功')
    router.push('/announcement-management')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除公告失败:', error)
      ElMessage.error('删除公告失败')
    }
  }
}

// 查看相关公告
const viewRelated = (id) => {
  if (id === parseInt(route.params.id)) {
    ElMessage.info('您已在查看此公告')
    return
  }
  
  // 记录当前公告到浏览历史中
  const currentId = route.params.id
  let announcementHistory = JSON.parse(sessionStorage.getItem('announcementHistory') || '[]')
  
  console.log('点击相关公告，当前ID:', currentId)
  console.log('点击前的历史:', announcementHistory)
  
  // 避免重复记录
  if (!announcementHistory.includes(currentId)) {
    announcementHistory.push(currentId)
  }
  
  // 限制历史记录长度
  if (announcementHistory.length > 10) {
    announcementHistory = announcementHistory.slice(-10)
  }
  
  console.log('保存的历史:', announcementHistory)
  sessionStorage.setItem('announcementHistory', JSON.stringify(announcementHistory))
  router.push(`/announcement/${id}`)
}

// 监听路由参数变化，当用户点击相关公告时重新加载数据
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    // 重置状态
    loading.value = true
    announcement.value = null
    relatedAnnouncements.value = []
    // 重新获取数据
    fetchAnnouncementDetail()
  }
}, { immediate: false })

onMounted(() => {
  fetchAnnouncementDetail()
})
</script>

<style scoped>
.announcement-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.detail-header {
  margin-bottom: 20px;
}

.announcement-card {
  margin-bottom: 20px;
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.announcement-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  flex: 1;
  margin-right: 20px;
}

.announcement-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.announcement-meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.meta-row .el-icon {
  color: #909399;
}

.announcement-content {
  padding: 20px 0;
  line-height: 1.8;
  font-size: 16px;
  color: #303133;
}

.content-html {
  word-break: break-word;
}

.content-html :deep(br) {
  line-height: 2.5;
}

.announcement-actions {
  margin-top: 20px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.related-announcements {
  margin-top: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.related-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.related-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.related-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.related-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  font-size: 14px;
  display: flex;
  align-items: flex-start;
  gap: 6px;
}

.related-icon {
  margin-top: 2px;
  color: #409eff;
  flex-shrink: 0;
}

.related-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
}

.related-time {
  color: #909399;
}

.ml-2 {
  margin-left: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .announcement-detail {
    padding: 12px;
  }
  
  .announcement-header {
    flex-direction: column;
    gap: 12px;
  }
  
  .announcement-title {
    font-size: 24px;
    margin-right: 0;
  }
  
  .announcement-meta {
    grid-template-columns: 1fr;
  }
  
  .action-buttons {
    justify-content: center;
  }
}
</style>
