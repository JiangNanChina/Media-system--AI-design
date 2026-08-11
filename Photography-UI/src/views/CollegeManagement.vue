<template>
  <div class="college-management">
    <div class="page-header">
      <h1 class="page-title">学院管理</h1>
      <p class="page-subtitle">维护外部借用和入部申请可选择的学院信息</p>
    </div>

    <el-card class="search-card">
      <el-row :gutter="20" class="search-row">
        <el-col :xs="24" :sm="8" :md="6" :lg="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索学院名称"
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
              新增学院
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">
            <el-icon><School /></el-icon>
            学院列表 (共 {{ pagination.total }} 条)
          </span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="collegeList"
        stripe
        style="width: 100%"
        empty-text="暂无学院数据"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="学院名称" min-width="220" show-overflow-tooltip />
        <el-table-column prop="description" label="学院描述" min-width="320" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="text" size="small" class="text-danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <el-dialog
      v-model="showCreateDialog"
      :title="editingCollege ? '编辑学院' : '新增学院'"
      width="500px"
      @close="handleCancelEdit"
    >
      <el-form ref="collegeFormRef" :model="collegeForm" :rules="collegeRules" label-width="80px">
        <el-form-item label="学院名称" prop="name">
          <el-input
            v-model="collegeForm.name"
            placeholder="请输入学院名称"
            maxlength="160"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="学院描述" prop="description">
          <el-input
            v-model="collegeForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入学院描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelEdit">取消</el-button>
          <el-button type="primary" @click="handleSaveCollege" :loading="saving">
            {{ editingCollege ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const saving = ref(false)
const showCreateDialog = ref(false)
const editingCollege = ref(null)
const collegeList = ref([])
const collegeFormRef = ref()

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const collegeForm = reactive({
  name: '',
  description: ''
})

const collegeRules = {
  name: [
    { required: true, message: '请输入学院名称', trigger: 'blur' },
    { min: 2, max: 160, message: '学院名称长度在 2 到 160 个字符', trigger: 'blur' }
  ]
}

const fetchColleges = async () => {
  try {
    loading.value = true
    let url = '/colleges'
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }

    if (searchForm.keyword.trim()) {
      url = '/colleges/search'
      params.keyword = searchForm.keyword.trim()
    }

    const response = await request.get(url, { params })
    collegeList.value = response.data?.content || []
    pagination.total = response.data?.totalElements || 0
  } catch (error) {
    console.error('获取学院列表失败:', error)
    ElMessage.error('获取学院列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchColleges()
}

const handleReset = () => {
  searchForm.keyword = ''
  pagination.page = 1
  fetchColleges()
}

const handleEdit = (college) => {
  editingCollege.value = college
  collegeForm.name = college.name
  collegeForm.description = college.description || ''
  showCreateDialog.value = true
}

const handleDelete = async (college) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学院"${college.name}"吗？删除后表单下拉中将不再显示该学院。`,
      '删除学院',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await request.delete(`/colleges/${college.id}`)
    ElMessage.success('学院已删除')
    fetchColleges()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除学院失败:', error)
      ElMessage.error(error.message || '删除学院失败')
    }
  }
}

const handleSaveCollege = async () => {
  try {
    await collegeFormRef.value.validate()
    saving.value = true

    if (editingCollege.value) {
      await request.put(`/colleges/${editingCollege.value.id}`, collegeForm)
      ElMessage.success('更新成功')
    } else {
      await request.post('/colleges', collegeForm)
      ElMessage.success('创建成功')
    }

    showCreateDialog.value = false
    fetchColleges()
  } catch (error) {
    console.error('保存学院失败:', error)
    ElMessage.error(error.message || '保存学院失败')
  } finally {
    saving.value = false
  }
}

const handleCancelEdit = () => {
  showCreateDialog.value = false
  editingCollege.value = null
  collegeForm.name = ''
  collegeForm.description = ''
  collegeFormRef.value?.resetFields()
}

const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1
  fetchColleges()
}

const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchColleges()
}

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

onMounted(() => {
  fetchColleges()
})
</script>

<style scoped>
.college-management {
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
