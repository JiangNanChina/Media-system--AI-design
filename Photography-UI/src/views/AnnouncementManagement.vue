<template>
  <div class="announcement-management">
    <div class="page-header">
      <h1 class="page-title">公告管理</h1>
      <p class="page-subtitle">管理系统公告的发布、编辑和状态</p>
    </div>
    
    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <!-- 搜索条件行 -->
      <el-row :gutter="16" class="search-row">
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索公告标题"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <el-select v-model="searchForm.type" placeholder="公告类型" clearable style="width: 100%">
            <el-option label="系统通知" value="SYSTEM" />
            <el-option label="重要公告" value="IMPORTANT" />
            <el-option label="一般通知" value="GENERAL" />
            <el-option label="活动公告" value="ACTIVITY" />
          </el-select>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <el-select v-model="searchForm.published" placeholder="发布状态" clearable style="width: 100%">
            <el-option label="已发布" :value="true" />
            <el-option label="草稿" :value="false" />
          </el-select>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6" :lg="6">
          <div class="basic-actions">
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              <span v-if="!isMobile">搜索</span>
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              <span v-if="!isMobile">重置</span>
            </el-button>
          </div>
        </el-col>
      </el-row>
      
      <!-- 操作按钮行 -->
      <el-row :gutter="16" class="action-buttons-row">
        <el-col :span="24">
          <div class="action-buttons">
            <el-button type="success" @click="showCreateDialog = true">
              <el-icon><Plus /></el-icon>
              <span>新增公告</span>
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 公告列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">
            <el-icon><Bell /></el-icon>
            公告列表 (共 {{ pagination.total }} 条)
          </span>
        </div>
      </template>
      
      <!-- 桌面端表格 -->
      <el-table
        v-if="!isMobile"
        v-loading="loading"
        :data="announcementList"
        stripe
        style="width: 100%"
        class="desktop-table"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <div class="priority-display">
              <el-rate
                v-model="row.priority"
                :max="3"
                disabled
                show-score
                size="small"
              />
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <div style="display: flex; flex-direction: column; gap: 4px;">
              <el-tag :type="row.published ? 'success' : 'info'" size="small">
                {{ row.published ? '已发布' : '草稿' }}
              </el-tag>
              <el-tag v-if="row.archived" type="warning" size="small">
                已归档
              </el-tag>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="查看次数" width="100">
          <template #default="{ row }">
            <div class="view-count">
              <el-icon><View /></el-icon>
              {{ row.viewCount || 0 }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="发布时间" width="180">
          <template #default="{ row }">
            {{ row.publishedAt ? formatTime(row.publishedAt) : '未发布' }}
          </template>
        </el-table-column>
        
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="232" fixed="right" align="center" class-name="announcement-operations-column">
          <template #default="{ row }">
            <div class="announcement-row-actions">
              <el-tooltip content="查看" placement="top">
                <el-button
                  class="icon-action view-action"
                  circle
                  aria-label="查看公告"
                  @click="handleViewDetail(row)"
                >
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>

              <el-tooltip content="编辑" placement="top">
                <el-button
                  class="icon-action edit-action"
                  circle
                  aria-label="编辑公告"
                  @click="handleEdit(row)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
              </el-tooltip>

              <el-tooltip v-if="!row.published" content="发布" placement="top">
                <el-button
                  class="icon-action publish-action"
                  circle
                  aria-label="发布公告"
                  @click="handlePublish(row)"
                >
                  <el-icon><Upload /></el-icon>
                </el-button>
              </el-tooltip>

              <el-tooltip v-if="row.published" content="撤回" placement="top">
                <el-button
                  class="icon-action withdraw-action"
                  circle
                  aria-label="撤回公告"
                  @click="handleUnpublish(row)"
                >
                  <el-icon><Download /></el-icon>
                </el-button>
              </el-tooltip>

              <el-tooltip v-if="!row.archived" content="归档" placement="top">
                <el-button
                  class="icon-action archive-action"
                  circle
                  aria-label="归档公告"
                  @click="handleArchive(row)"
                >
                  <el-icon><Box /></el-icon>
                </el-button>
              </el-tooltip>

              <el-tooltip v-if="row.archived" content="取消归档" placement="top">
                <el-button
                  class="icon-action restore-action"
                  circle
                  aria-label="取消归档公告"
                  @click="handleUnarchive(row)"
                >
                  <el-icon><RefreshRight /></el-icon>
                </el-button>
              </el-tooltip>

              <el-tooltip content="删除" placement="top">
                <el-button
                  class="icon-action delete-action"
                  circle
                  aria-label="删除公告"
                  @click="handleDelete(row)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 移动端卡片布局 -->
      <div v-if="isMobile" v-loading="loading" class="mobile-cards">
        <div v-for="announcement in announcementList" :key="announcement.id" class="mobile-card">
          <div class="card-header">
            <div class="card-title">
              <span class="title-text">{{ announcement.title }}</span>
              <div class="card-badges">
                <el-tag :type="getTypeColor(announcement.type)" size="small">
                  {{ getTypeText(announcement.type) }}
                </el-tag>
                <el-tag :type="announcement.published ? 'success' : 'info'" size="small">
                  {{ announcement.published ? '已发布' : '草稿' }}
                </el-tag>
                <el-tag v-if="announcement.archived" type="warning" size="small">
                  已归档
                </el-tag>
              </div>
            </div>
          </div>
          
          <div class="card-content">
            <div class="card-info">
              <div class="info-item">
                <span class="info-label">ID:</span>
                <span class="info-value">{{ announcement.id }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">优先级:</span>
                <el-rate
                  :model-value="announcement.priority"
                  :max="3"
                  disabled
                  size="small"
                />
              </div>
              <div class="info-item">
                <span class="info-label">查看次数:</span>
                <span class="info-value">
                  <el-icon><View /></el-icon>
                  {{ announcement.viewCount || 0 }}
                </span>
              </div>
            </div>
            
            <div class="card-times">
              <div class="time-item">
                <span class="time-label">发布时间:</span>
                <span class="time-value">{{ announcement.publishedAt ? formatTime(announcement.publishedAt) : '未发布' }}</span>
              </div>
              <div class="time-item">
                <span class="time-label">创建时间:</span>
                <span class="time-value">{{ formatTime(announcement.createdAt) }}</span>
              </div>
            </div>
          </div>
          
          <div class="card-actions" @click.stop>
            <div class="announcement-row-actions announcement-card-actions">
              <el-button
                class="icon-action view-action"
                circle
                aria-label="查看公告"
                @click="handleViewDetail(announcement)"
              >
                <el-icon><View /></el-icon>
              </el-button>
              <el-button
                class="icon-action edit-action"
                circle
                aria-label="编辑公告"
                @click="handleEdit(announcement)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button
                v-if="!announcement.published"
                class="icon-action publish-action"
                circle
                aria-label="发布公告"
                @click="handlePublish(announcement)"
              >
                <el-icon><Upload /></el-icon>
              </el-button>
              <el-button
                v-if="announcement.published"
                class="icon-action withdraw-action"
                circle
                aria-label="撤回公告"
                @click="handleUnpublish(announcement)"
              >
                <el-icon><Download /></el-icon>
              </el-button>
              <el-button
                v-if="!announcement.archived"
                class="icon-action archive-action"
                circle
                aria-label="归档公告"
                @click="handleArchive(announcement)"
              >
                <el-icon><Box /></el-icon>
              </el-button>
              <el-button
                v-if="announcement.archived"
                class="icon-action restore-action"
                circle
                aria-label="取消归档公告"
                @click="handleUnarchive(announcement)"
              >
                <el-icon><RefreshRight /></el-icon>
              </el-button>
              <el-button
                class="icon-action delete-action"
                circle
                aria-label="删除公告"
                @click="handleDelete(announcement)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          :small="isMobile"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑公告对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingAnnouncement ? '编辑公告' : '新增公告'"
      :width="isMobile ? '95%' : '800px'"
      :close-on-click-modal="false"
      class="announcement-dialog"
    >
      <el-form
        ref="announcementFormRef"
        :model="announcementForm"
        :rules="announcementRules"
        :label-width="isMobile ? '80px' : '100px'"
        class="announcement-form"
      >
        <el-form-item label="公告标题" prop="title">
          <el-input 
            v-model="announcementForm.title" 
            placeholder="请输入公告标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="公告类型" prop="type">
              <el-select v-model="announcementForm.type" placeholder="选择类型" style="width: 100%">
                <el-option label="系统通知" value="SYSTEM" />
                <el-option label="重要公告" value="IMPORTANT" />
                <el-option label="一般通知" value="GENERAL" />
                <el-option label="活动公告" value="ACTIVITY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="优先级" prop="priority">
              <el-rate
                v-model="announcementForm.priority"
                :max="3"
                show-text
                :texts="['低', '中', '高']"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="announcementForm.content"
            type="textarea"
            :rows="isMobile ? 6 : 8"
            :autosize="{ minRows: 4, maxRows: 10 }"
            placeholder="请输入公告内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="发布设置">
          <el-checkbox v-model="announcementForm.published">立即发布</el-checkbox>
          <el-checkbox v-model="announcementForm.pinned">置顶显示</el-checkbox>
        </el-form-item>
        
        <el-form-item v-if="announcementForm.published" label="发布时间">
          <el-date-picker
            v-model="announcementForm.publishedAt"
            type="datetime"
            placeholder="选择发布时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelEdit">取消</el-button>
          <el-button @click="handleSaveDraft" :loading="saving">
            保存草稿
          </el-button>
          <el-button type="primary" @click="handleSaveAnnouncement" :loading="saving">
            {{ editingAnnouncement ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box, RefreshRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

// 响应式屏幕尺寸检测
const windowWidth = ref(window.innerWidth)

// 计算属性：判断是否为移动设备
const isMobile = computed(() => windowWidth.value <= 768)

// 监听窗口大小变化
const handleResize = () => {
  windowWidth.value = window.innerWidth
}

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showCreateDialog = ref(false)
const editingAnnouncement = ref(null)
const announcementList = ref([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  type: '',
  published: ''
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 公告表单
const announcementForm = reactive({
  title: '',
  content: '',
  type: 'GENERAL',
  priority: 1,
  published: false,
  pinned: false,
  publishedAt: ''
})

// 表单验证规则
const announcementRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '内容长度在 10 到 2000 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择公告类型', trigger: 'change' }
  ]
}

const announcementFormRef = ref()

// 获取公告列表
const fetchAnnouncements = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...searchForm
    }
    
    // 清空空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    
    const response = await request.get('/announcements', { params })
    
    if (response.data) {
      announcementList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchAnnouncements()
}

// 重置搜索
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.page = 1
  fetchAnnouncements()
}

// 查看公告详情
const handleViewDetail = (announcement) => {
  // 清空公告浏览历史，因为这是从管理页面开始的新浏览会话
  sessionStorage.removeItem('announcementHistory')
  router.push(`/announcement/${announcement.id}`)
}

// 编辑公告
const handleEdit = (announcement) => {
  editingAnnouncement.value = announcement
  Object.keys(announcementForm).forEach(key => {
    if (key === 'priority') {
      announcementForm[key] = announcement[key] || 1
    } else if (key === 'published' || key === 'pinned') {
      announcementForm[key] = !!announcement[key]
    } else {
      announcementForm[key] = announcement[key] || ''
    }
  })
  showCreateDialog.value = true
}

// 取消编辑
const handleCancelEdit = () => {
  showCreateDialog.value = false
  editingAnnouncement.value = null
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.keys(announcementForm).forEach(key => {
    if (key === 'type') {
      announcementForm[key] = 'GENERAL'
    } else if (key === 'priority') {
      announcementForm[key] = 1
    } else if (key === 'published' || key === 'pinned') {
      announcementForm[key] = false
    } else {
      announcementForm[key] = ''
    }
  })
  if (announcementFormRef.value) {
    announcementFormRef.value.clearValidate()
  }
}

// 保存草稿
const handleSaveDraft = async () => {
  if (!announcementFormRef.value) return
  
  try {
    await announcementFormRef.value.validate()
    saving.value = true
    
    const draftData = {
      ...announcementForm,
      published: false,
      publishedAt: null
    }
    
    
    if (editingAnnouncement.value) {
      await request.put(`/announcements/${editingAnnouncement.value.id}`, draftData)
      ElMessage.success('草稿保存成功')
    } else {
      await request.post('/announcements', draftData)
      ElMessage.success('草稿创建成功')
    }
    
    showCreateDialog.value = false
    editingAnnouncement.value = null
    resetForm()
    fetchAnnouncements()
  } catch (error) {
    console.error('保存草稿失败:', error)
    
    // 显示详细的错误信息
    let errorMessage = '保存草稿失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.data?.content) {
      // 处理验证错误
      const validationErrors = error.response.data.content
      if (Array.isArray(validationErrors) && validationErrors.length > 0) {
        errorMessage = validationErrors[0]
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    saving.value = false
  }
}

// 保存公告
const handleSaveAnnouncement = async () => {
  if (!announcementFormRef.value) return
  
  try {
    await announcementFormRef.value.validate()
    saving.value = true
    
    const announcementData = { ...announcementForm }
    
    // 如果选择立即发布但没有设置发布时间，使用当前时间
    if (announcementData.published && !announcementData.publishedAt) {
      announcementData.publishedAt = new Date().toISOString().slice(0, 19).replace('T', ' ')
    }
    
    
    if (editingAnnouncement.value) {
      await request.put(`/announcements/${editingAnnouncement.value.id}`, announcementData)
      ElMessage.success('公告更新成功')
    } else {
      await request.post('/announcements', announcementData)
      ElMessage.success('公告创建成功')
    }
    
    showCreateDialog.value = false
    editingAnnouncement.value = null
    resetForm()
    fetchAnnouncements()
  } catch (error) {
    console.error('保存公告失败:', error)
    
    // 显示详细的错误信息
    let errorMessage = '保存公告失败'
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.data?.content) {
      // 处理验证错误
      const validationErrors = error.response.data.content
      if (Array.isArray(validationErrors) && validationErrors.length > 0) {
        errorMessage = validationErrors[0]
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    ElMessage.error(errorMessage)
  } finally {
    saving.value = false
  }
}

// 发布公告
const handlePublish = async (announcement) => {
  try {
    await ElMessageBox.confirm('确定要发布这个公告吗？', '确认发布', {
      type: 'info'
    })
    
    await request.put(`/announcements/${announcement.id}/publish`)
    ElMessage.success('公告发布成功')
    fetchAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布公告失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

// 撤回公告
const handleUnpublish = async (announcement) => {
  try {
    await ElMessageBox.confirm('确定要撤回这个公告吗？撤回后用户将无法看到此公告。', '确认撤回', {
      type: 'warning'
    })
    
    await request.put(`/announcements/${announcement.id}/unpublish`)
    ElMessage.success('公告已撤回')
    fetchAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回公告失败:', error)
      ElMessage.error('撤回失败')
    }
  }
}

// 删除公告
const handleDelete = async (announcement) => {
  try {
    await ElMessageBox.confirm(`确定要删除公告 "${announcement.title}" 吗？此操作不可恢复！`, '确认删除', {
      type: 'error',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    
    await request.delete(`/announcements/${announcement.id}`)
    ElMessage.success('公告删除成功')
    fetchAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除公告失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 归档公告
const handleArchive = async (announcement) => {
  try {
    await ElMessageBox.confirm(`确定要归档公告 "${announcement.title}" 吗？归档后不会在登录弹窗显示。`, '确认归档', {
      type: 'warning',
      confirmButtonText: '确定归档',
      cancelButtonText: '取消'
    })
    
    await request.put(`/announcements/${announcement.id}/archive`)
    ElMessage.success('公告归档成功')
    fetchAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('归档公告失败:', error)
      ElMessage.error('归档失败')
    }
  }
}

// 取消归档公告
const handleUnarchive = async (announcement) => {
  try {
    await ElMessageBox.confirm(`确定要取消归档公告 "${announcement.title}" 吗？取消归档后可在登录弹窗显示。`, '确认取消归档', {
      type: 'info',
      confirmButtonText: '确定取消归档',
      cancelButtonText: '取消'
    })
    
    await request.put(`/announcements/${announcement.id}/unarchive`)
    ElMessage.success('取消归档成功')
    fetchAnnouncements()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消归档公告失败:', error)
      ElMessage.error('取消归档失败')
    }
  }
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
  return textMap[type] || '未知'
}

// 分页处理
const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1
  fetchAnnouncements()
}

const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchAnnouncements()
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchAnnouncements()
  window.addEventListener('resize', handleResize)
})

// 组件卸载时清理事件监听器
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.announcement-management {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 16px;
  color: #909399;
  margin: 0;
}

.search-card,
.table-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

/* 搜索行样式 */
.search-row {
  align-items: flex-end;
  margin-bottom: 16px;
}

.basic-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  align-items: center;
}

.basic-actions .el-button {
  min-width: 80px;
  height: 32px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* 操作按钮行样式 */
.action-buttons-row {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
}

.action-buttons .el-button {
  min-width: 120px;
  height: 36px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.table-title .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.priority-display {
  display: flex;
  align-items: center;
}

.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
}

.desktop-table :deep(.announcement-operations-column .cell) {
  display: flex;
  justify-content: center;
  overflow: visible;
  padding-left: 12px;
  padding-right: 12px;
}

.announcement-row-actions,
.announcement-card-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-wrap: nowrap;
  gap: 6px;
  max-width: 100%;
  padding: 5px 6px;
  border: 1px solid rgba(98, 177, 210, 0.2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.88), 0 8px 18px rgba(18, 174, 231, 0.08);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.announcement-row-actions {
  width: 202px;
}

.announcement-card-actions {
  width: 100%;
  justify-content: flex-start;
}

.announcement-row-actions .icon-action,
.announcement-card-actions .icon-action {
  width: 30px !important;
  height: 30px !important;
  min-width: 30px !important;
  min-height: 30px !important;
  margin: 0 !important;
  padding: 0 !important;
  border-radius: 999px !important;
  border: 1px solid rgba(98, 177, 210, 0.2) !important;
  background: rgba(255, 255, 255, 0.84) !important;
  color: #0876a5 !important;
  box-shadow: none !important;
  transform: none !important;
}

.announcement-row-actions .icon-action :deep(.el-icon),
.announcement-card-actions .icon-action :deep(.el-icon) {
  margin: 0 !important;
  font-size: 15px;
}

.announcement-row-actions .icon-action:hover,
.announcement-card-actions .icon-action:hover {
  transform: translateY(-1px) !important;
}

.announcement-row-actions .view-action:hover,
.announcement-card-actions .view-action:hover {
  border-color: rgba(24, 185, 236, 0.42) !important;
  background: #e8f9ff !important;
  color: #067aa8 !important;
}

.announcement-row-actions .edit-action,
.announcement-card-actions .edit-action {
  color: #0f8f72 !important;
}

.announcement-row-actions .edit-action:hover,
.announcement-card-actions .edit-action:hover {
  border-color: rgba(33, 185, 139, 0.42) !important;
  background: #e7fbf4 !important;
  color: #087f63 !important;
}

.announcement-row-actions .publish-action,
.announcement-card-actions .publish-action {
  color: #087f63 !important;
}

.announcement-row-actions .publish-action:hover,
.announcement-card-actions .publish-action:hover {
  border-color: rgba(33, 185, 139, 0.42) !important;
  background: #e7fbf4 !important;
  color: #067a5f !important;
}

.announcement-row-actions .withdraw-action,
.announcement-card-actions .withdraw-action {
  color: #9a640d !important;
}

.announcement-row-actions .withdraw-action:hover,
.announcement-card-actions .withdraw-action:hover {
  border-color: rgba(244, 185, 66, 0.46) !important;
  background: #fff6dc !important;
  color: #815008 !important;
}

.announcement-row-actions .archive-action,
.announcement-card-actions .archive-action {
  color: #0c7ea3 !important;
}

.announcement-row-actions .archive-action:hover,
.announcement-card-actions .archive-action:hover {
  border-color: rgba(18, 174, 231, 0.34) !important;
  background: #e8f9ff !important;
  color: #066e90 !important;
}

.announcement-row-actions .restore-action,
.announcement-card-actions .restore-action {
  color: #0f8f72 !important;
}

.announcement-row-actions .restore-action:hover,
.announcement-card-actions .restore-action:hover {
  border-color: rgba(33, 185, 139, 0.42) !important;
  background: #e7fbf4 !important;
  color: #087f63 !important;
}

.announcement-row-actions .delete-action,
.announcement-card-actions .delete-action {
  color: #b4233e !important;
}

.announcement-row-actions .delete-action:hover,
.announcement-card-actions .delete-action:hover {
  border-color: rgba(240, 82, 104, 0.46) !important;
  background: #fff0f3 !important;
  color: #941b32 !important;
}

/* 移动端卡片样式 */
.mobile-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.mobile-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.card-header {
  padding: 16px;
  border-bottom: 1px solid #f5f7fa;
  background: #fafbfc;
}

.card-title {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
}

.card-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.card-content {
  padding: 16px;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.info-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-times {
  border-top: 1px solid #f5f7fa;
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.time-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-label {
  font-size: 13px;
  color: #909399;
}

.time-value {
  font-size: 13px;
  color: #606266;
}

.card-actions {
  padding: 12px 16px;
  border-top: 1px solid #f5f7fa;
  background: #fafbfc;
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
}

.announcement-card-actions .icon-action {
  flex: 0 0 auto;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 1024px) and (min-width: 769px) {
  .basic-actions .el-button {
    min-width: 70px;
    font-size: 12px;
  }
  
  .action-buttons .el-button {
    min-width: 100px;
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .search-row .el-col {
    margin-bottom: 12px;
  }
  
  .basic-actions {
    justify-content: center;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .basic-actions .el-button {
    flex: 1;
    min-width: auto;
    height: 36px;
    font-size: 14px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 10px;
    justify-content: stretch;
  }
  
  .action-buttons .el-button {
    width: 100%;
    min-width: auto;
    height: 40px;
    font-size: 14px;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .desktop-table {
    display: none;
  }
  
  .mobile-cards {
    display: block;
  }
  
  .pagination-container {
    margin-top: 12px;
  }
  
  .pagination-container .el-pagination {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .announcement-management {
    padding: 0 8px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .basic-actions .el-button {
    height: 40px;
    font-size: 13px;
    padding: 10px 16px;
  }
  
  .action-buttons .el-button {
    height: 42px;
    font-size: 13px;
    padding: 10px 16px;
  }
  
  .mobile-card {
    margin: 0 -4px;
    border-radius: 6px;
  }
  
  .card-header {
    padding: 12px;
  }
  
  .card-content {
    padding: 12px;
  }
  
  .card-actions {
    padding: 10px 12px;
  }

  .announcement-row-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .announcement-row-actions .icon-action,
  .announcement-card-actions .icon-action {
    width: 34px !important;
    height: 34px !important;
    min-width: 34px !important;
    min-height: 34px !important;
  }
  
  .title-text {
    font-size: 15px;
  }
  
  .info-label, .info-value {
    font-size: 13px;
  }
  
  .time-label, .time-value {
    font-size: 12px;
  }
  
  /* 手机端对话框全屏 */
  .announcement-dialog .el-dialog {
    margin: 0;
    width: 100% !important;
    height: 100vh;
    max-height: none;
    border-radius: 0;
    display: flex;
    flex-direction: column;
  }
  
  .announcement-dialog .el-dialog__header {
    padding: 12px 16px 8px;
    border-bottom: 1px solid #ebeef5;
    flex-shrink: 0;
  }
  
  .announcement-dialog .el-dialog__title {
    font-size: 16px;
  }
  
  .announcement-dialog .el-dialog__body {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
  }
  
  .announcement-dialog .el-dialog__footer {
    padding: 12px 16px 16px;
    border-top: 1px solid #ebeef5;
    flex-shrink: 0;
  }
  
  .announcement-form .el-form-item {
    margin-bottom: 16px;
  }
  
  .announcement-form .el-form-item__label {
    font-size: 14px;
    text-align: left !important;
    padding-right: 8px;
  }
  
  .announcement-form .el-input__inner,
  .announcement-form .el-textarea__inner {
    font-size: 14px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
    margin: 4px 0;
  }
}
</style>
