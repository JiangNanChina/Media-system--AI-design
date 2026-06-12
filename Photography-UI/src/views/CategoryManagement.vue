<template>
  <div class="category-management">
    <div class="page-header">
      <h1 class="page-title">分类管理</h1>
      <p class="page-subtitle">管理设备分类，设置分类信息和排序</p>
    </div>
    
    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <el-row :gutter="20" class="search-row">
        <el-col :xs="24" :sm="8" :md="6" :lg="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索分类名称"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        
        <el-col :xs="24" :sm="16" :md="18" :lg="18">
          <div class="search-actions">
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
            <el-button 
              v-if="userStore.isAdmin"
              type="success" 
              @click="showCreateDialog = true"
            >
              <el-icon><Plus /></el-icon>
              新增分类
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 分类列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <div class="table-title">
            <el-icon><Collection /></el-icon>
            分类列表（共 {{ pagination.total }} 条）
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="categoryList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="name" label="分类名称" min-width="150" show-overflow-tooltip />
        
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="sortOrder" label="排序" width="100" />
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'">
              {{ row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" :width="userStore.isAdmin ? 200 : 80" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="text" size="small" @click="handleView(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <template v-if="userStore.isAdmin">
                <el-button type="text" size="small" @click="handleEdit(row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button 
                  type="text" 
                  size="small" 
                  @click="handleToggleStatus(row)"
                >
                  <el-icon><Switch /></el-icon>
                  {{ row.isActive ? '禁用' : '启用' }}
                </el-button>
                <el-button 
                  type="text" 
                  size="small" 
                  class="text-danger"
                  @click="handleDelete(row)"
                >
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :small="false"
          :disabled="loading"
          :background="true"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑分类对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="!userStore.isAdmin && editingCategory ? '查看分类详情' : (editingCategory ? '编辑分类' : '新增分类')"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="categoryRules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input 
            v-model="categoryForm.name" 
            placeholder="请输入分类名称" 
            :readonly="isReadOnlyMode"
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述"
            :readonly="isReadOnlyMode"
          />
        </el-form-item>
        
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number
            v-model="categoryForm.sortOrder"
            :min="0"
            :max="9999"
            style="width: 100%"
            :disabled="isReadOnlyMode"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="isActive">
          <el-switch
            v-model="categoryForm.isActive"
            active-text="启用"
            inactive-text="禁用"
            :disabled="isReadOnlyMode"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelEdit">
            {{ !userStore.isAdmin && editingCategory ? '关闭' : '取消' }}
          </el-button>
          <el-button 
            v-if="userStore.isAdmin"
            type="primary" 
            @click="handleSaveCategory" 
            :loading="saving"
          >
            {{ editingCategory ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

// 用户状态
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const categoryList = ref([])
const showCreateDialog = ref(false)
const editingCategory = ref(null)

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 是否为只读模式（普通用户查看分类详情时）
const isReadOnlyMode = computed(() => {
  return !userStore.isAdmin && !!editingCategory.value
})

// 分类表单
const categoryForm = reactive({
  name: '',
  description: '',
  sortOrder: 0,
  isActive: true
})

// 表单验证规则
const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '分类名称长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

// 表单引用
const categoryFormRef = ref(null)

// 获取分类列表
const fetchCategories = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      sortBy: 'sortOrder',
      sortDir: 'asc'
    }
    
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    
    const response = await request.get('/equipment-categories', { params })
    
    if (response.data) {
      categoryList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchCategories()
}

// 重置搜索
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.page = 1
  fetchCategories()
}

// 查看分类详情
const handleView = (category) => {
  editingCategory.value = category
  Object.keys(categoryForm).forEach(key => {
    categoryForm[key] = category[key] || (key === 'sortOrder' ? 0 : (key === 'isActive' ? true : ''))
  })
  showCreateDialog.value = true
}

// 编辑分类
const handleEdit = (category) => {
  editingCategory.value = category
  Object.keys(categoryForm).forEach(key => {
    categoryForm[key] = category[key] || (key === 'sortOrder' ? 0 : (key === 'isActive' ? true : ''))
  })
  showCreateDialog.value = true
}

// 取消编辑
const handleCancelEdit = () => {
  showCreateDialog.value = false
  editingCategory.value = null
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.keys(categoryForm).forEach(key => {
    categoryForm[key] = key === 'sortOrder' ? 0 : (key === 'isActive' ? true : '')
  })
  if (categoryFormRef.value) {
    categoryFormRef.value.clearValidate()
  }
}

// 保存分类
const handleSaveCategory = async () => {
  try {
    await categoryFormRef.value.validate()
    saving.value = true
    
    if (editingCategory.value) {
      // 更新分类
      await request.put(`/equipment-categories/${editingCategory.value.id}`, categoryForm)
      ElMessage.success('分类更新成功')
    } else {
      // 创建分类
      await request.post('/equipment-categories', categoryForm)
      ElMessage.success('分类创建成功')
    }
    
    handleCancelEdit()
    fetchCategories()
  } catch (error) {
    console.error('保存分类失败:', error)
    ElMessage.error('保存分类失败')
  } finally {
    saving.value = false
  }
}

// 删除分类
const handleDelete = async (category) => {
  try {
    await ElMessageBox.confirm(`确定要删除分类 "${category.name}" 吗？此操作不可恢复！`, '确认删除', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    
    await request.delete(`/equipment-categories/${category.id}`)
    ElMessage.success('分类删除成功')
    fetchCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 切换分类状态
const handleToggleStatus = async (category) => {
  try {
    await request.put(`/equipment-categories/${category.id}/toggle-status`)
    ElMessage.success(`分类已${category.isActive ? '禁用' : '启用'}`)
    fetchCategories()
  } catch (error) {
    console.error('切换分类状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 分页处理
const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1
  fetchCategories()
}

const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchCategories()
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #303133;
}

.page-subtitle {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.search-row {
  align-items: center;
}

.search-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.table-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title {
  display: flex;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.table-title .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.action-buttons .el-button {
  margin: 0;
  padding: 4px 8px;
}

.text-danger {
  color: #f56c6c;
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
@media (max-width: 768px) {
  .search-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .search-actions .el-button {
    width: 100%;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
