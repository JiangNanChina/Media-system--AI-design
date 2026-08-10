<template>
  <div class="user-management">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <p class="page-subtitle">管理系统用户信息、权限和状态</p>
    </div>
    
    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <el-row :gutter="20" class="search-row">
        <el-col :xs="24" :sm="6" :md="4" :lg="4">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名/姓名"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        
        <el-col :xs="24" :sm="6" :md="4" :lg="4">
          <el-select v-model="searchForm.role" placeholder="选择角色" clearable>
            <el-option v-for="option in roleOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-col>
        
        <el-col :xs="24" :sm="6" :md="4" :lg="4">
          <el-select v-model="searchForm.departmentId" placeholder="选择部门" clearable>
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-col>
        
        <el-col :xs="24" :sm="6" :md="4" :lg="4">
          <el-select v-model="searchForm.enabled" placeholder="用户状态" clearable>
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="8" :lg="8">
          <div class="search-actions">
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
            <el-button v-if="!userStore.isMinister" type="success" @click="handleCreate">
              <el-icon><Plus /></el-icon>
              新增用户
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 用户列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">
            <el-icon><UserFilled /></el-icon>
            用户列表 (共 {{ pagination.total }} 条)
          </span>
          <div v-if="!userStore.isMinister" class="table-actions">
            <el-button 
              type="warning" 
              size="small"
              :disabled="selectedUsers.length === 0"
              @click="handleBatchDisable"
            >
              <el-icon><Lock /></el-icon>
              批量禁用
            </el-button>
            <el-button 
              type="success" 
              size="small"
              :disabled="selectedUsers.length === 0"
              @click="handleBatchEnable"
            >
              <el-icon><Unlock /></el-icon>
              批量启用
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="userList"
        @selection-change="handleSelectionChange"
        stripe
        style="width: 100%"
        :class="{ 'mobile-table': isMobile }"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="getAvatarUrl(row.avatarUrl)">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
          </template>
        </el-table-column>
        
        <el-table-column prop="username" label="用户名" min-width="120" />
        
        <el-table-column prop="realName" label="真实姓名" min-width="120" />
        
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip v-if="!isMobile" />
        
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" size="small">
              {{ roleLabel(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="部门" min-width="120" v-if="!isMobile">
          <template #default="{ row }">
            {{ row.departmentName || '未分配' }}
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="创建时间" width="180" v-if="!isMobile">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column
          label="操作"
          :width="isMobile ? '120' : '268'"
          fixed="right"
          align="center"
          header-align="center"
          class-name="operation-column"
        >
          <template #default="{ row }">
            <div v-if="isMobile" class="mobile-actions">
              <el-dropdown trigger="click" @command="(command) => handleMobileAction(command, row)">
                <el-button type="primary" size="small">
                  操作 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="edit">
                      <el-icon><Edit /></el-icon> 编辑
                    </el-dropdown-item>
                    <el-dropdown-item v-if="!userStore.isMinister" command="resetPassword">
                      <el-icon><Key /></el-icon> 重置密码
                    </el-dropdown-item>
                    <el-dropdown-item :command="row.enabled ? 'disable' : 'enable'">
                      <el-icon><component :is="row.enabled ? 'Lock' : 'Unlock'" /></el-icon>
                      {{ row.enabled ? '禁用' : '启用' }}
                    </el-dropdown-item>
                    <el-dropdown-item v-if="!userStore.isMinister" command="delete" :disabled="row.id === userStore.userInfo?.id">
                      <el-icon><Delete /></el-icon> 删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div v-else class="desktop-actions">
              <el-button class="action-btn action-edit" size="small" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button v-if="!userStore.isMinister"
                class="action-btn action-reset"
                size="small" 
                @click="handleResetPassword(row)"
              >
                <el-icon><Key /></el-icon>
                重置密码
              </el-button>
              <el-button 
                class="action-btn action-status"
                size="small" 
                @click="handleToggleStatus(row)"
                :class="row.enabled ? 'is-warning' : 'is-success'"
              >
                <el-icon><component :is="row.enabled ? 'Lock' : 'Unlock'" /></el-icon>
                {{ row.enabled ? '禁用' : '启用' }}
              </el-button>
              <el-button v-if="!userStore.isMinister"
                size="small" 
                class="action-btn action-delete"
                @click="handleDelete(row)"
                :disabled="row.id === userStore.userInfo?.id"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
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
          :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          :small="isMobile"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingUser ? '编辑用户' : '新增用户'"
      :width="isMobile ? '90%' : '600px'"
      :close-on-click-modal="false"
      class="user-dialog"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        :label-width="isMobile ? '80px' : '100px'"
        class="user-form"
      >
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="userForm.username" 
                placeholder="请输入用户名"
                :disabled="!!editingUser"
                clearable
                @input="handleUserFormUsernameInput"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input 
                v-model="userForm.realName" 
                placeholder="请输入真实姓名" 
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="邮箱" prop="email">
              <el-input 
                v-model="userForm.email" 
                placeholder="请输入邮箱地址" 
                type="email"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="角色" prop="role">
              <el-select 
                v-model="userForm.role" 
                placeholder="选择角色" 
                style="width: 100%"
                :disabled="!userStore.isSuperAdmin"
              >
                <el-option v-for="option in roleOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="部门" prop="departmentId">
              <el-select 
                v-model="userForm.departmentId" 
                placeholder="选择部门" 
                style="width: 100%"
                clearable
              >
                <el-option
                  v-for="dept in departments"
                  :key="dept.id"
                  :label="dept.name"
                  :value="dept.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="状态" prop="enabled">
              <el-switch
                v-model="userForm.enabled"
                active-text="启用"
                inactive-text="禁用"
                :inline-prompt="!isMobile"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="isMobile ? 10 : 20" v-if="!editingUser">
          <el-col :span="24">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="userForm.password"
                type="password"
                placeholder="请输入初始密码"
                show-password
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelEdit">取消</el-button>
          <el-button type="primary" @click="handleSaveUser" :loading="saving">
            {{ editingUser ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Plus, 
  UserFilled, 
  Lock, 
  Unlock, 
  Edit, 
  Delete, 
  ArrowDown,
  Key
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const userStore = useUserStore()
const roleOptions = [
  { label: '部员', value: 'MEMBER' },
  { label: '部长', value: 'MINISTER' },
  { label: '主任', value: 'DIRECTOR' },
  { label: '指导老师', value: 'ADVISOR' },
  { label: '系统超级管理员', value: 'SUPER_ADMIN' }
]
const roleLabel = role => roleOptions.find(option => option.value === role)?.label || (role === 'ADMIN' ? '系统超级管理员' : role)
const roleTagType = role => ({ SUPER_ADMIN: 'danger', ADMIN: 'danger', DIRECTOR: 'warning', MINISTER: 'success', ADVISOR: 'info' }[role] || 'primary')

// 移动端检测
const screenWidth = ref(window.innerWidth)
const isMobile = computed(() => screenWidth.value < 768)

const updateScreenWidth = () => {
  screenWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', updateScreenWidth)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateScreenWidth)
})

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const showCreateDialog = ref(false)
const editingUser = ref(null)
const selectedUsers = ref([])
const userList = ref([])
const departments = ref([])
const usernameFormatErrorUserForm = ref('')

// 头像URL处理函数
const getAvatarUrl = (avatarUrl) => {
  if (!avatarUrl) return ''
  
  // 如果是完整URL，直接返回
  if (avatarUrl.startsWith('http')) {
    return avatarUrl
  }
  
  // 如果是相对路径，直接返回（不添加/api前缀，因为静态资源直接访问）
  return avatarUrl
}

// 搜索表单
const searchForm = reactive({
  keyword: '',
  role: '',
  departmentId: '',
  enabled: ''
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 用户表单
const userForm = reactive({
  username: '',
  realName: '',
  email: '',
  role: 'MEMBER',
  departmentId: '',
  enabled: true,
  password: ''
})

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线，且不能包含中文', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 8, max: 72, message: '密码长度在 8 到 72 个字符', trigger: 'blur' }
  ]
}

const userFormRef = ref()

// 获取用户列表
const fetchUsers = async () => {
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
    
    console.log('发送用户查询请求，参数:', params)
    if (userStore.isMinister) {
      const response = await request.get('/department-members')
      let rows = response.data || []
      if (searchForm.keyword) {
        const keyword = searchForm.keyword.toLowerCase()
        rows = rows.filter(user => `${user.username || ''}${user.realName || ''}`.toLowerCase().includes(keyword))
      }
      if (searchForm.role) rows = rows.filter(user => user.role === searchForm.role)
      if (searchForm.enabled !== '') rows = rows.filter(user => user.enabled === searchForm.enabled)
      pagination.total = rows.length
      const offset = (pagination.page - 1) * pagination.size
      userList.value = rows.slice(offset, offset + pagination.size)
      return
    }

    const response = await request.get('/users', { params })
    
    if (response.data) {
      userList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
      
      console.log('真实API数据加载成功:')
      console.log('- 总用户数:', pagination.total)
      console.log('- 当前页用户数:', userList.value.length)
      console.log('- 用户列表:', userList.value)
    }
    
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
    // 确保在错误情况下也设置一些基本数据
    userList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
    console.log('用户列表加载完成，loading状态:', loading.value)
    console.log('最终用户列表长度:', userList.value.length)
  }
}

// 获取部门列表
const fetchDepartments = async () => {
  try {
    const response = await request.get('/departments/list')
    if (response.success !== false && response.data && Array.isArray(response.data)) {
      departments.value = response.data
    } else {
      console.warn('部门数据格式不正确或接口返回错误')
      departments.value = []
    }
  } catch (error) {
    console.error('获取部门列表失败:', error)
    departments.value = []
  }
}

// 搜索用户
const handleSearch = () => {
  pagination.page = 1
  fetchUsers()
}

// 重置搜索
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.page = 1
  fetchUsers()
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 编辑用户
// 移动端操作处理
const handleMobileAction = (command, user) => {
  switch (command) {
    case 'edit':
      handleEdit(user)
      break
    case 'resetPassword':
      handleResetPassword(user)
      break
    case 'enable':
    case 'disable':
      handleToggleStatus(user)
      break
    case 'delete':
      handleDelete(user)
      break
  }
}

// 新增用户
const handleCreate = () => {
  editingUser.value = null
  resetForm()
  showCreateDialog.value = true
  
  // 在弹窗打开后自动聚焦（桌面端）
  nextTick(() => {
    if (userFormRef.value && !isMobile.value) {
      const firstInput = userFormRef.value.$el.querySelector('input')
      if (firstInput) {
        firstInput.focus()
      }
    }
  })
}

const handleEdit = (user) => {
  editingUser.value = user
  Object.keys(userForm).forEach(key => {
    if (key === 'password') {
      userForm[key] = '' // 编辑时不显示密码
    } else {
      userForm[key] = user[key] || ''
    }
  })
  showCreateDialog.value = true
  
  // 在弹窗打开后自动聚焦（桌面端）
  nextTick(() => {
    if (userFormRef.value && !isMobile.value) {
      const firstInput = userFormRef.value.$el.querySelector('input:not([disabled])')
      if (firstInput) {
        firstInput.focus()
      }
    }
  })
}

// 取消编辑
const handleCancelEdit = () => {
  showCreateDialog.value = false
  editingUser.value = null
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.keys(userForm).forEach(key => {
    if (key === 'role') {
      userForm[key] = 'MEMBER'
    } else if (key === 'enabled') {
      userForm[key] = true
    } else {
      userForm[key] = ''
    }
  })
  if (userFormRef.value) {
    userFormRef.value.clearValidate()
  }
}

// 保存用户
const handleSaveUser = async () => {
  if (!userFormRef.value) return
  
  try {
    if (usernameFormatErrorUserForm.value) {
      ElMessage.error(usernameFormatErrorUserForm.value)
      return
    }
    await userFormRef.value.validate()
    saving.value = true
    
    const userData = { ...userForm }
    
    if (editingUser.value) {
      delete userData.password
      const selectedRole = userData.role
      delete userData.role
      if (userStore.isMinister) {
        await request.put(`/department-members/${editingUser.value.id}`, {
          realName: userData.realName,
          email: userData.email,
          enabled: userData.enabled
        })
      } else {
        await request.put(`/users/${editingUser.value.id}`, userData)
        if (userStore.isSuperAdmin && selectedRole !== editingUser.value.role) {
          await request.put(`/accounts/admin/${editingUser.value.id}/role`, { role: selectedRole })
        }
      }
      ElMessage.success('用户更新成功')
    } else {
      // 新增用户
      await request.post('/users', userData)
      ElMessage.success('用户创建成功')
    }
    
    showCreateDialog.value = false
    editingUser.value = null
    resetForm()
    fetchUsers()
  } catch (error) {
    console.error('保存用户失败:', error)
    ElMessage.error('保存用户失败')
  } finally {
    saving.value = false
  }
}

// 新增/编辑用户对话框内：用户名输入实时校验（禁止中文）
const handleUserFormUsernameInput = (value) => {
  const hasChinese = /[\u4e00-\u9fa5]/.test(value)
  const filtered = value.replace(/[^a-zA-Z0-9_]/g, '')
  if (hasChinese) {
    usernameFormatErrorUserForm.value = '用户名不能包含中文'
    ElMessage.error('用户名不能包含中文')
  } else if (filtered !== value) {
    usernameFormatErrorUserForm.value = '用户名只能包含字母、数字和下划线'
    ElMessage.warning('用户名只能包含字母、数字和下划线')
  } else {
    usernameFormatErrorUserForm.value = ''
  }
  if (filtered !== userForm.username) {
    userForm.username = filtered
  }
}

// 重置密码
const handleResetPassword = async (user) => {
  try {
    const { value: newPassword } = await ElMessageBox.prompt(
      `为用户“${user.realName || user.username}”设置一次性新密码`,
      '重置密码',
      {
        type: 'warning',
        confirmButtonText: '确认重置',
        cancelButtonText: '取消',
        inputType: 'password',
        inputPattern: /^.{8,72}$/,
        inputErrorMessage: '密码长度必须为8-72位'
      }
    )

    await request.post(`/users/${user.id}/reset-password`, { newPassword })
    ElMessage.success('密码已重置，用户需要重新登录')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

// 切换用户状态
const handleToggleStatus = async (user) => {
  try {
    const action = user.enabled ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}用户 "${user.realName || user.username}" 吗？`, '确认操作', {
      type: 'warning'
    })
    
    if (userStore.isMinister) {
      await request.put(`/department-members/${user.id}`, { enabled: !user.enabled })
    } else {
      await request.put(`/users/${user.id}`, { enabled: !user.enabled })
    }
    
    ElMessage.success(`用户${action}成功`)
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换用户状态失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 删除用户
const handleDelete = async (user) => {
  try {
    // 第一步：选择删除类型
    const { value: deleteType } = await ElMessageBox.prompt(
      `请选择删除类型：\n\n1. 软删除：隐藏用户但保留数据\n2. 物理删除：彻底删除用户及所有相关数据\n\n用户："${user.realName || user.username}"`,
      '选择删除方式',
      {
        customClass: 'delete-method-message-box',
        confirmButtonText: '下一步',
        cancelButtonText: '取消',
        inputPlaceholder: '输入 1 软删除，或 2 物理删除',
        inputValidator: (value) => {
          if (!value || (value !== '1' && value !== '2')) {
            return '请输入 1 或 2'
          }
          return true
        }
      }
    )
    
    const isPhysicalDelete = deleteType === '2'
    const deleteTypeText = isPhysicalDelete ? '物理删除' : '软删除'
    const warningMessage = isPhysicalDelete 
      ? `确定要彻底删除用户 "${user.realName || user.username}" 及其所有相关数据吗？\n\n风险提示：此操作将删除该用户的：\n• 借还记录\n• 请假记录\n• 执勤记录\n• 打卡记录\n• 设备绑定\n• 头像文件\n\n此操作不可恢复！`
      : `确定要删除用户 "${user.realName || user.username}" 吗？\n\n用户将被隐藏，但数据会保留。`
    
    // 第二步：确认删除
    await ElMessageBox.confirm(warningMessage, `确认${deleteTypeText}`, {
      customClass: 'delete-confirm-message-box',
      type: isPhysicalDelete ? 'error' : 'warning',
      confirmButtonText: `确定${deleteTypeText}`,
      confirmButtonClass: isPhysicalDelete ? 'el-button--danger' : 'el-button--primary',
      cancelButtonText: '取消',
      dangerouslyUseHTMLString: false
    })
    
    // 执行删除
    const endpoint = isPhysicalDelete ? `/users/${user.id}/physical` : `/users/${user.id}`
    await request.delete(endpoint)
    
    ElMessage.success(`用户${deleteTypeText}成功`)
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量禁用
const handleBatchDisable = async () => {
  try {
    await ElMessageBox.confirm(`确定要禁用选中的 ${selectedUsers.value.length} 个用户吗？`, '批量禁用', {
      type: 'warning'
    })
    
    const userIds = selectedUsers.value.map(user => user.id)
    await request.put('/users/batch-disable', { userIds })
    
    ElMessage.success('批量禁用成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量禁用失败:', error)
      ElMessage.error('批量禁用失败')
    }
  }
}

// 批量启用
const handleBatchEnable = async () => {
  try {
    await ElMessageBox.confirm(`确定要启用选中的 ${selectedUsers.value.length} 个用户吗？`, '批量启用', {
      type: 'warning'
    })
    
    const userIds = selectedUsers.value.map(user => user.id)
    await request.put('/users/batch-enable', { userIds })
    
    ElMessage.success('批量启用成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量启用失败:', error)
      ElMessage.error('批量启用失败')
    }
  }
}

// 分页处理
const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1
  fetchUsers()
}

const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchUsers()
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN')
}

// 组件挂载时获取数据
onMounted(() => {
  fetchUsers()
  fetchDepartments()
})
</script>

<style scoped>
/* 现代化用户管理页面 */
.user-management {
  max-width: 1400px;
  margin: 0 auto;
  animation: fadeIn var(--duration-normal) var(--easing-ease);
}

/* 现代化页面头部 */
.page-header {
  margin-bottom: var(--spacing-8);
  text-align: center;
  padding: var(--spacing-8) 0;
  background: var(--gradient-primary);
  border-radius: var(--radius-xl);
  position: relative;
  overflow: hidden;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.05) 0%, transparent 50%);
  pointer-events: none;
}

.page-title {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-white);
  margin: 0 0 var(--spacing-2) 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
}

.page-subtitle {
  font-size: var(--font-size-lg);
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  position: relative;
  z-index: 1;
}

/* 现代化搜索和表格卡片 */
.search-card,
.table-card {
  background: var(--color-white);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  margin-bottom: var(--spacing-5);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: all var(--duration-normal) var(--easing-ease);
}

.search-card:hover,
.table-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-xl);
}

.search-row {
  align-items: flex-end;
}

/* 现代化搜索操作按钮 */
.search-actions {
  display: flex;
  gap: var(--spacing-2);
  flex-wrap: wrap;
}

.search-actions .el-button {
  border-radius: var(--radius-md);
  font-weight: var(--font-weight-medium);
  transition: all var(--duration-normal) var(--easing-ease);
  box-shadow: var(--shadow-sm);
}

.search-actions .el-button:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-base);
}

/* 现代化表格头部 */
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-1) 0;
  border-bottom: 1px solid var(--color-divider);
  margin-bottom: var(--spacing-4);
}

.table-title {
  display: flex;
  align-items: center;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  gap: var(--spacing-2);
}

.table-title .el-icon {
  color: var(--color-primary-500);
  font-size: var(--font-size-xl);
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-actions .el-button {
  height: 32px;
  padding: 8px 16px;
  font-size: 13px;
  border-radius: 6px;
  font-weight: 500;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.text-primary {
  color: #409eff !important;
}

.text-warning {
  color: #e6a23c !important;
}

.text-success {
  color: #67c23a !important;
}

.text-danger {
  color: #f56c6c !important;
}

/* 原有的 dialog-footer 样式已移至上方弹窗样式部分 */

/* 移动端表格样式 */
.mobile-table {
  font-size: 14px;
}

.mobile-actions {
  display: flex;
  justify-content: center;
}

.desktop-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  width: 100%;
  min-width: 220px;
  padding: 2px 0;
}

.desktop-actions .el-button + .el-button {
  margin-left: 0;
}

.desktop-actions .action-btn {
  width: 100%;
  min-height: 32px;
  margin: 0 !important;
  padding: 7px 10px !important;
  justify-content: center;
  gap: 5px;
  color: var(--button-primary-text) !important;
  font-size: 12px;
  font-weight: 750 !important;
  line-height: 1;
  white-space: nowrap;
  border: 1px solid var(--button-primary-border) !important;
  border-radius: var(--radius-md) !important;
  background: var(--button-primary-bg) !important;
  box-shadow: 0 8px 18px rgba(21, 122, 177, 0.08) !important;
  transform: none !important;
}

.desktop-actions .action-btn .el-icon {
  margin-right: 0;
  font-size: 13px;
}

.desktop-actions .action-btn:hover,
.desktop-actions .action-btn:focus-visible {
  color: var(--color-primary-700) !important;
  border-color: rgba(37, 184, 242, 0.42) !important;
  background: linear-gradient(135deg, #f2fbff 0%, #dff3ff 58%, #ecfff9 100%) !important;
  box-shadow: 0 10px 22px rgba(21, 122, 177, 0.12) !important;
  transform: translateY(-1px) !important;
}

.desktop-actions .action-reset {
  color: #0f6f95 !important;
  background: linear-gradient(135deg, #f8fdff 0%, #e7f7ff 100%) !important;
  border-color: rgba(74, 151, 193, 0.24) !important;
}

.desktop-actions .action-status.is-warning {
  color: var(--button-warning-text) !important;
  background: var(--button-warning-bg) !important;
  border-color: var(--button-warning-border) !important;
}

.desktop-actions .action-status.is-success {
  color: var(--button-success-text) !important;
  background: var(--button-success-bg) !important;
  border-color: var(--button-success-border) !important;
}

.desktop-actions .action-delete {
  color: var(--button-danger-text) !important;
  background: var(--button-danger-bg) !important;
  border-color: var(--button-danger-border) !important;
}

.desktop-actions .action-delete.is-disabled,
.desktop-actions .action-delete:disabled {
  color: var(--color-text-disabled) !important;
  background: rgba(247, 252, 255, 0.72) !important;
  border-color: rgba(178, 198, 211, 0.26) !important;
  box-shadow: none !important;
}

.desktop-actions .action-delete:not(.is-disabled):hover,
.desktop-actions .action-delete:not(:disabled):hover,
.desktop-actions .action-delete:not(.is-disabled):focus-visible,
.desktop-actions .action-delete:not(:disabled):focus-visible {
  color: #a92f42 !important;
  border-color: rgba(236, 106, 120, 0.42) !important;
  background: linear-gradient(135deg, #fff8fa 0%, #ffdce5 100%) !important;
  box-shadow: 0 10px 22px rgba(236, 106, 120, 0.12) !important;
}

:deep(.operation-column .cell) {
  display: flex;
  justify-content: center;
  padding-left: 14px !important;
  padding-right: 14px !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-management {
    padding: 0 8px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .search-card,
  .table-card {
    margin-bottom: 16px;
    border-radius: 8px;
  }
  
  .search-actions {
    flex-direction: column;
    width: 100%;
    gap: 8px;
  }
  
  .search-actions .el-button {
    width: 100%;
    margin: 0;
  }
  
  .table-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    padding: 8px 0;
  }
  
  .table-title {
    text-align: center;
    justify-content: center;
    font-size: 15px;
  }
  
  .table-actions {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .table-actions .el-button {
    height: 36px;
    min-width: 100px;
    flex: 1;
    max-width: 140px;
  }
  
  .search-row .el-col {
    margin-bottom: 12px;
  }
  
  /* 表格列调整 */
  .el-table .el-table__cell {
    padding: 8px 4px;
  }
  
  .el-table .el-table-column--selection .el-checkbox {
    zoom: 0.8;
  }
  
  /* 分页调整 */
  .pagination-container {
    margin-top: 16px;
  }
  
  .el-pagination.is-background .el-pager li {
    min-width: 28px;
    height: 28px;
    line-height: 28px;
  }
}

@media (max-width: 480px) {
  .user-management {
    padding: 0 4px;
  }
  
  .page-header {
    margin-bottom: 16px;
    text-align: center;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .page-subtitle {
    font-size: 12px;
  }
  
  .table-header {
    padding: 6px 0;
  }
  
  .table-title {
    font-size: 14px;
    margin-bottom: 8px;
  }
  
  .table-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .table-actions .el-button {
    width: 100%;
    height: 40px;
    font-size: 14px;
    margin: 0;
  }
  
  /* 进一步缩小表格 */
  .el-table {
    font-size: 12px;
  }
  
  .el-table .el-table__cell {
    padding: 6px 2px;
  }
  
  .el-avatar {
    width: 32px !important;
    height: 32px !important;
  }
  
  .el-tag {
    font-size: 10px;
    padding: 0 4px;
    height: 20px;
    line-height: 18px;
  }
  
  /* 下拉菜单按钮 */
  .mobile-actions .el-button {
    font-size: 12px;
    padding: 4px 8px;
  }
  
  /* 分页进一步调整 */
  .el-pagination.is-background .el-pager li {
    min-width: 24px;
    height: 24px;
    line-height: 24px;
    font-size: 12px;
  }
  
  .el-pagination .btn-next,
  .el-pagination .btn-prev {
    width: 24px;
    height: 24px;
    line-height: 24px;
  }
}

/* 用户弹窗样式 */
.user-dialog .el-dialog__body {
  padding: 20px;
}

.user-form .el-form-item {
  margin-bottom: 20px;
}

.user-form .el-form-item__label {
  font-weight: 500;
  color: #606266;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 10px;
}

/* 移动端弹窗优化 */
@media (max-width: 768px) {
  .user-dialog .el-dialog {
    margin: 5vh auto;
  }
  
  .user-dialog .el-dialog__header {
    padding: 16px 20px 10px;
  }
  
  .user-dialog .el-dialog__body {
    padding: 10px 20px 20px;
  }
  
  .user-dialog .el-dialog__footer {
    padding: 10px 20px 20px;
  }
  
  .user-form .el-form-item {
    margin-bottom: 16px;
  }
  
  .user-form .el-form-item__label {
    font-size: 14px;
    line-height: 1.4;
  }
  
  .user-form .el-input__inner,
  .user-form .el-select .el-input__inner {
    font-size: 14px;
  }
  
  .dialog-footer {
    flex-direction: column-reverse;
    gap: 8px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
    margin: 0;
  }
}

@media (max-width: 480px) {
  .user-dialog .el-dialog {
    margin: 0;
    width: 100% !important;
    height: 100vh;
    max-height: none;
    border-radius: 0;
    display: flex;
    flex-direction: column;
  }
  
  .user-dialog .el-dialog__header {
    padding: 12px 16px 8px;
    border-bottom: 1px solid #ebeef5;
  }
  
  .user-dialog .el-dialog__title {
    font-size: 16px;
  }
  
  .user-dialog .el-dialog__body {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
  }
  
  .user-dialog .el-dialog__footer {
    padding: 12px 16px 16px;
    border-top: 1px solid #ebeef5;
  }
  
  .user-form .el-form-item {
    margin-bottom: 12px;
  }
  
  .user-form .el-form-item__label {
    font-size: 13px;
    text-align: left !important;
    padding-right: 8px;
  }
  
  .user-form .el-input,
  .user-form .el-select {
    font-size: 14px;
  }
  
  .user-form .el-switch__label {
    font-size: 12px;
  }
}
</style>
