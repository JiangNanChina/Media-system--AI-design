<template>
  <div class="submission-management">
    <div class="page-header">
      <div><h1>视频投稿</h1><p>审核游客提交的校园视频素材</p></div>
      <el-select v-model="status" clearable placeholder="全部状态" @change="load"><el-option label="待审核" value="PENDING" /><el-option label="已通过" value="APPROVED" /><el-option label="已驳回" value="REJECTED" /></el-select>
    </div>
    <el-table v-loading="loading" :data="rows" row-key="id">
      <el-table-column prop="submissionNumber" label="投稿编号" min-width="170" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column label="投稿人" min-width="150"><template #default="{ row }"><strong>{{ row.submitterName }}</strong><small>{{ row.organization || '未填写单位' }}</small></template></el-table-column>
      <el-table-column prop="qqEmail" label="联系邮箱" min-width="190" />
      <el-table-column label="文件" min-width="150"><template #default="{ row }">{{ row.originalFilename }}<small>{{ fileSize(row.fileSize) }}</small></template></el-table-column>
      <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="tagType(row.status)">{{ statusText(row.status) }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="220" fixed="right"><template #default="{ row }"><el-button text type="primary" @click="download(row)"><el-icon><Download /></el-icon>下载</el-button><el-button v-if="row.status === 'PENDING'" text type="primary" @click="openReview(row)"><el-icon><DocumentChecked /></el-icon>审核</el-button></template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total" layout="total, prev, pager, next" @current-change="load" />

    <el-dialog v-model="dialogVisible" title="审核视频投稿" width="min(520px, 92vw)">
      <el-descriptions v-if="current" :column="1" border><el-descriptions-item label="投稿编号">{{ current.submissionNumber }}</el-descriptions-item><el-descriptions-item label="标题">{{ current.title }}</el-descriptions-item><el-descriptions-item label="说明">{{ current.description || '无' }}</el-descriptions-item></el-descriptions>
      <el-form label-position="top" class="review-form"><el-form-item label="审核反馈"><el-input v-model="feedback" type="textarea" :rows="4" maxlength="1000" show-word-limit /></el-form-item></el-form>
      <template #footer><el-button :loading="reviewing" @click="review('REJECTED')">驳回</el-button><el-button type="primary" :loading="reviewing" @click="review('APPROVED')">通过</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { DocumentChecked, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const rows = ref([]); const loading = ref(false); const reviewing = ref(false)
const status = ref(''); const page = ref(1); const size = ref(10); const total = ref(0)
const dialogVisible = ref(false); const current = ref(null); const feedback = ref('')
const load = async () => {
  loading.value = true
  try { const response = await request.get('/submissions', { params: { status: status.value || undefined, page: page.value - 1, size: size.value, sort: 'createdAt,desc' } }); rows.value = response.data.content; total.value = response.data.totalElements }
  finally { loading.value = false }
}
const openReview = row => { current.value = row; feedback.value = ''; dialogVisible.value = true }
const review = async result => {
  reviewing.value = true
  try { await request.put(`/submissions/${current.value.id}/review`, { status: result, feedback: feedback.value }); ElMessage.success('审核结果已保存'); dialogVisible.value = false; load() }
  finally { reviewing.value = false }
}
const download = async row => {
  const response = await request.get(`/submissions/${row.id}/download`, { responseType: 'blob', timeout: 10 * 60 * 1000 })
  const url = URL.createObjectURL(response.data); const link = document.createElement('a'); link.href = url; link.download = row.originalFilename; link.click(); URL.revokeObjectURL(url)
}
const fileSize = value => value >= 1024 ** 2 ? `${(value / 1024 ** 2).toFixed(1)} MB` : `${Math.round(value / 1024)} KB`
const statusText = value => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' })[value]
const tagType = value => ({ PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' })[value]
onMounted(load)
</script>

<style scoped>
.submission-management { padding: 4px; }
.page-header { display: flex; align-items: center; justify-content: space-between; gap: 20px; }
.page-header h1 { font-size: 25px; letter-spacing: 0; }
.page-header p, small { color: #6b7f89; }
small { display: block; margin-top: 4px; }
.el-table { margin-top: 20px; border: 1px solid #dce8ed; border-radius: 6px; }
.el-pagination { justify-content: flex-end; margin-top: 20px; }
.review-form { margin-top: 20px; }
@media(max-width: 680px) { .page-header { align-items: flex-start; flex-direction: column; } .page-header .el-select { width: 100%; } }
</style>
