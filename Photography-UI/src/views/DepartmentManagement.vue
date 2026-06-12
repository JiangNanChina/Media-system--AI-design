<template>
  <div class="department-management">
    <div class="page-header">
      <h1 class="page-title">部门管理</h1>
      <p class="page-subtitle">管理系统中的部门信息</p>
    </div>
    
    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <el-row :gutter="20" class="search-row">
        <el-col :xs="24" :sm="8" :md="6" :lg="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索部门名称"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        
        <el-col :xs="24" :sm="16" :md="12" :lg="10">
          <div class="search-actions">
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
            <el-button type="success" @click="showCreateDialog = true">
              <el-icon><Plus /></el-icon>
              新增部门
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 部门列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">
            <el-icon><OfficeBuilding /></el-icon>
            部门列表 (共 {{ pagination.total }} 条)
          </span>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="departmentList"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column prop="name" label="部门名称" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="description" label="部门描述" min-width="300" show-overflow-tooltip />
        
        <el-table-column label="用户数量" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.userCount || 0 }}人</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              type="text" 
              size="small" 
              class="text-danger"
              @click="handleDelete(row)"
              :disabled="row.userCount > 0"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑部门对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingDepartment ? '编辑部门' : '新增部门'"
      width="500px"
      @close="handleCancelEdit"
    >
      <el-form
        ref="departmentFormRef"
        :model="departmentForm"
        :rules="departmentRules"
        label-width="80px"
      >
        <el-form-item label="部门名称" prop="name">
          <el-input
            v-model="departmentForm.name"
            placeholder="请输入部门名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="部门描述" prop="description">
          <el-input
            v-model="departmentForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入部门描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelEdit">取消</el-button>
          <el-button type="primary" @click="handleSaveDepartment" :loading="saving">
            {{ editingDepartment ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showCreateDialog = ref(false)
const editingDepartment = ref(null)
const departmentList = ref([])

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

// 部门表单
const departmentForm = reactive({
  name: '',
  description: ''
})

// 表单验证规则
const departmentRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 100, message: '部门名称长度在 2 到 100 个字符', trigger: 'blur' }
  ]
}

const departmentFormRef = ref()

// 获取部门列表
const fetchDepartments = async () => {
  try {
    loading.value = true
    
    let url = '/departments'
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    
    if (searchForm.keyword) {
      url = '/departments/search'
      params.keyword = searchForm.keyword
    }
    
    const response = await request.get(url, { params })
    
    if (response.data) {
      departmentList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('获取部门列表失败:', error)
    ElMessage.error('获取部门列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.page = 1
  fetchDepartments()
}

// 重置搜索
const handleReset = () => {
  searchForm.keyword = ''
  pagination.page = 1
  fetchDepartments()
}

// 编辑部门
const handleEdit = (department) => {
  editingDepartment.value = department
  departmentForm.name = department.name
  departmentForm.description = department.description || ''
  showCreateDialog.value = true
}

// 删除部门
const handleDelete = async (department) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除部门"${department.name}"吗？此操作将永久删除部门数据，不可恢复！`,
      '警告 - 物理删除',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: true,
        message: `
          <div>
            <p><strong>注意：这是物理删除操作！</strong></p>
            <p>• 部门"${department.name}"将从数据库中永久删除</p>
            <p>• 如果该部门下有用户，删除将失败</p>
            <p>• 此操作无法撤销</p>
          </div>
        `
      }
    )
    
    await request.delete(`/departments/${department.id}`)
    ElMessage.success('部门已成功删除')
    fetchDepartments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除部门失败:', error)
      
      // 显示更详细的错误信息
      const errorMessage = error.response?.data?.message || error.message || '删除部门失败'
      ElMessage.error({
        message: errorMessage,
        duration: 5000,
        showClose: true
      })
    }
  }
}

// 保存部门
const handleSaveDepartment = async () => {
  try {
    // 表单验证
    await departmentFormRef.value.validate()
    
    saving.value = true
    
    if (editingDepartment.value) {
      // 更新部门
      await request.put(`/departments/${editingDepartment.value.id}`, departmentForm)
      ElMessage.success('更新成功')
    } else {
      // 创建部门
      await request.post('/departments', departmentForm)
      ElMessage.success('创建成功')
    }
    
    showCreateDialog.value = false
    fetchDepartments()
  } catch (error) {
    console.error('保存部门失败:', error)
    ElMessage.error('保存部门失败')
  } finally {
    saving.value = false
  }
}

// 取消编辑
const handleCancelEdit = () => {
  showCreateDialog.value = false
  editingDepartment.value = null
  departmentForm.name = ''
  departmentForm.description = ''
  departmentFormRef.value?.resetFields()
}

// 分页处理
const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1
  fetchDepartments()
}

const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchDepartments()
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 页面加载时获取数据
onMounted(() => {
  fetchDepartments()
})
</script>

<style scoped>
.department-management {
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

.search-row {
  align-items: flex-end;
}

.search-actions {
  display: flex;
  gap: 8px;
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

.text-danger {
  color: #f56c6c !important;
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
    flex-wrap: wrap;
    margin-top: 12px;
  }
  
  .search-actions .el-button {
    flex: 1;
    min-width: 0;
  }
}
</style>
