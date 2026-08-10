<template>
  <div class="account-review">
    <div class="page-header"><div><h1>注册审核</h1><p>为已验证邮箱的新用户分配部门并启用账号</p></div><el-button @click="load"><el-icon><Refresh /></el-icon>刷新</el-button></div>
    <el-table v-loading="loading" :data="rows" empty-text="暂无待审核注册">
      <el-table-column prop="username" label="用户名" min-width="130" />
      <el-table-column prop="realName" label="姓名" min-width="110" />
      <el-table-column prop="email" label="QQ邮箱" min-width="190" />
      <el-table-column label="申请身份" width="100"><template #default><el-tag>部员</el-tag></template></el-table-column>
      <el-table-column label="操作" width="190" fixed="right"><template #default="{ row }"><el-button text type="primary" @click="open(row)">审核</el-button><el-button text type="danger" @click="reject(row)">驳回</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="visible" title="启用账号" width="min(460px, 92vw)">
      <el-form label-position="top"><el-form-item label="用户"><el-input :model-value="current?.realName" disabled /></el-form-item><el-form-item label="所属部门" required><el-select v-model="departmentId" filterable placeholder="请选择部门"><el-option v-for="item in departments" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item></el-form>
      <template #footer><el-button @click="visible = false">取消</el-button><el-button type="primary" :disabled="!departmentId" @click="approve">启用账号</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
const rows = ref([]); const departments = ref([]); const loading = ref(false); const visible = ref(false); const current = ref(null); const departmentId = ref(null)
const load = async () => { loading.value = true; try { const [users, depts] = await Promise.all([request.get('/accounts/admin/pending'), request.get('/departments/list')]); rows.value = users.data; departments.value = depts.data } finally { loading.value = false } }
const open = row => { current.value = row; departmentId.value = null; visible.value = true }
const approve = async () => { await request.put(`/accounts/admin/${current.value.id}/review`, { approved: true, departmentId: departmentId.value }); ElMessage.success('账号已启用'); visible.value = false; load() }
const reject = async row => { await ElMessageBox.confirm(`确认驳回 ${row.realName} 的注册申请？`, '注册审核'); await request.put(`/accounts/admin/${row.id}/review`, { approved: false }); ElMessage.success('申请已驳回'); load() }
onMounted(load)
</script>

<style scoped>
.account-review { padding: 4px; }
.page-header { display: flex; justify-content: space-between; align-items: center; gap: 20px; }
.page-header h1 { font-size: 25px; letter-spacing: 0; }
.page-header p { color: #6b7f89; }
.el-table { margin-top: 20px; border: 1px solid #dce8ed; border-radius: 6px; }
.el-select { width: 100%; }
</style>
