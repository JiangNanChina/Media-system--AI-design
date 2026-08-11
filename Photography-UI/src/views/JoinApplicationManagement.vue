<template>
  <div class="join-application-management">
    <div class="page-header">
      <div>
        <h1>入部申请</h1>
        <p>审核游客提交的入部申请并发送面试通知</p>
      </div>
      <div class="header-actions">
        <el-select v-model="status" clearable placeholder="全部状态" @change="load">
          <el-option label="待审核" value="PENDING" />
          <el-option label="进入面试" value="INTERVIEW" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
        <el-button :loading="loading" @click="load">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <div class="config-panel">
      <div>
        <strong>面试QQ群号</strong>
        <span>同意申请后，系统会把该群号写入邮件通知。</span>
      </div>
      <div class="group-editor">
        <el-input v-model.trim="interviewGroup" maxlength="12" placeholder="请输入5到12位QQ群号" clearable />
        <el-button type="primary" :loading="savingGroup" @click="saveInterviewGroup">保存</el-button>
      </div>
    </div>

    <el-table v-loading="loading" :data="rows" row-key="id" empty-text="暂无入部申请">
      <el-table-column prop="applicationNumber" label="申请编号" min-width="170" />
      <el-table-column label="申请人" min-width="170">
        <template #default="{ row }">
          <strong>{{ row.realName }}</strong>
          <small>{{ row.qqEmail }}</small>
          <small>{{ row.phone }}</small>
        </template>
      </el-table-column>
      <el-table-column label="基本信息" min-width="170">
        <template #default="{ row }">
          {{ row.genderDescription || '-' }}
          <small>{{ row.enrollmentYear }} 级</small>
        </template>
      </el-table-column>
      <el-table-column label="学院专业" min-width="200">
        <template #default="{ row }">
          {{ row.college }}
          <small>{{ row.major }}</small>
        </template>
      </el-table-column>
      <el-table-column label="作品" width="120">
        <template #default="{ row }">
          <el-button v-if="row.hasWork" text type="primary" @click="downloadWork(row)">
            <el-icon><Download /></el-icon>
            下载
          </el-button>
          <span v-else class="muted">未上传</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="tagType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="通知" width="130">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'INTERVIEW'" :type="row.notificationSent ? 'success' : 'warning'">
            {{ row.notificationSent ? '已发送' : '发送失败' }}
          </el-tag>
          <span v-else class="muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDetail(row)">
            <el-icon><View /></el-icon>
            详情
          </el-button>
          <el-button v-if="row.status === 'PENDING'" text type="primary" @click="openReview(row)">
            <el-icon><DocumentChecked /></el-icon>
            审核
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
    />

    <el-dialog v-model="detailVisible" title="申请详情" width="min(720px, 94vw)">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="申请编号">{{ current.applicationNumber }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ current.realName }}</el-descriptions-item>
        <el-descriptions-item label="QQ邮箱">{{ current.qqEmail }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ current.phone }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ current.genderDescription }}</el-descriptions-item>
        <el-descriptions-item label="学院专业">{{ current.college }} / {{ current.major }}</el-descriptions-item>
        <el-descriptions-item label="入学年份">{{ current.enrollmentYear }}</el-descriptions-item>
        <el-descriptions-item label="自我介绍">
          <p class="intro-text">{{ current.selfIntroduction }}</p>
        </el-descriptions-item>
        <el-descriptions-item label="作品">
          <el-button v-if="current.hasWork" type="primary" plain @click="downloadWork(current)">
            <el-icon><Download /></el-icon>
            下载 {{ current.workOriginalFilename }}
          </el-button>
          <span v-else>未上传</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="current.status !== 'PENDING'" label="审核结果">
          {{ statusText(current.status) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="current.reviewFeedback" label="审核备注">
          {{ current.reviewFeedback }}
        </el-descriptions-item>
        <el-descriptions-item v-if="current.notificationError" label="通知失败原因">
          <span class="error-text">{{ current.notificationError }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="审核入部申请" width="min(560px, 94vw)">
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="申请人">{{ current.realName }}</el-descriptions-item>
        <el-descriptions-item label="学院专业">{{ current.college }} / {{ current.major }}</el-descriptions-item>
        <el-descriptions-item label="当前面试群">{{ interviewGroup || '未配置' }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-position="top" class="review-form">
        <el-form-item label="审核备注">
          <el-input v-model="feedback" type="textarea" :rows="4" maxlength="1000" show-word-limit placeholder="可填写给申请人的备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :loading="reviewing" @click="review(false)">驳回</el-button>
        <el-button type="primary" :loading="reviewing" :disabled="!interviewGroup" @click="review(true)">同意进入面试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { DocumentChecked, Download, Refresh, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const rows = ref([])
const loading = ref(false)
const reviewing = ref(false)
const savingGroup = ref(false)
const status = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const reviewVisible = ref(false)
const current = ref(null)
const feedback = ref('')
const interviewGroup = ref('')

const load = async () => {
  loading.value = true
  try {
    const response = await request.get('/join-applications', {
      params: {
        status: status.value || undefined,
        page: page.value - 1,
        size: size.value,
        sort: 'createdAt,desc'
      }
    })
    rows.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

const loadInterviewGroup = async () => {
  const response = await request.get('/join-applications/admin/interview-group')
  interviewGroup.value = response.data?.qqGroupNumber || ''
}

const saveInterviewGroup = async () => {
  if (!/^\d{5,12}$/.test(interviewGroup.value || '')) {
    ElMessage.warning('面试QQ群号必须是5到12位数字')
    return
  }
  savingGroup.value = true
  try {
    await request.put('/join-applications/admin/interview-group', { qqGroupNumber: interviewGroup.value })
    ElMessage.success('面试QQ群号已保存')
  } finally {
    savingGroup.value = false
  }
}

const openDetail = row => {
  current.value = row
  detailVisible.value = true
}

const openReview = row => {
  current.value = row
  feedback.value = ''
  reviewVisible.value = true
}

const review = async approved => {
  if (approved && !interviewGroup.value) {
    ElMessage.warning('请先配置面试QQ群号')
    return
  }
  reviewing.value = true
  try {
    const response = await request.put(`/join-applications/${current.value.id}/review`, {
      approved,
      feedback: feedback.value
    })
    const data = response.data || {}
    if (approved && data.notificationSent) {
      ElMessage.success('已同意进入面试并发送邮件')
    } else if (approved) {
      ElMessage.warning(data.notificationError || '已进入面试，但邮件通知发送失败')
    } else {
      ElMessage.success('申请已驳回')
    }
    reviewVisible.value = false
    await load()
  } finally {
    reviewing.value = false
  }
}

const downloadWork = async row => {
  const response = await request.get(`/join-applications/${row.id}/work`, {
    responseType: 'blob',
    timeout: 10 * 60 * 1000
  })
  const url = URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = url
  link.download = row.workOriginalFilename || `${row.applicationNumber}-work`
  link.click()
  URL.revokeObjectURL(url)
}

const statusText = value => ({ PENDING: '待审核', INTERVIEW: '进入面试', REJECTED: '已驳回' })[value] || value
const tagType = value => ({ PENDING: 'warning', INTERVIEW: 'success', REJECTED: 'danger' })[value] || 'info'

onMounted(async () => {
  await Promise.all([load(), loadInterviewGroup()])
})
</script>

<style scoped>
.join-application-management {
  padding: 4px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 25px;
  letter-spacing: 0;
}

.page-header p,
small,
.muted {
  color: #6b7f89;
}

small {
  display: block;
  margin-top: 4px;
}

.header-actions,
.group-editor {
  display: flex;
  align-items: center;
  gap: 10px;
}

.config-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin: 20px 0;
  padding: 16px 18px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 8px;
}

.config-panel strong {
  display: block;
  color: #123044;
  font-size: 15px;
}

.config-panel span {
  display: block;
  margin-top: 4px;
  color: #6b7f89;
  font-size: 13px;
}

.group-editor .el-input {
  width: 220px;
}

.el-table {
  border: 1px solid #dce8ed;
  border-radius: 6px;
}

.el-pagination {
  justify-content: flex-end;
  margin-top: 20px;
}

.intro-text {
  max-height: 220px;
  overflow-y: auto;
  margin: 0;
  color: #31556e;
  line-height: 1.8;
  white-space: pre-wrap;
}

.error-text {
  color: #d93026;
}

.review-form {
  margin-top: 20px;
}

@media (max-width: 760px) {
  .page-header,
  .config-panel,
  .header-actions,
  .group-editor {
    align-items: stretch;
    flex-direction: column;
  }

  .header-actions .el-select,
  .group-editor .el-input,
  .header-actions .el-button,
  .group-editor .el-button {
    width: 100%;
  }
}
</style>
