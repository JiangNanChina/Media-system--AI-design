<template>
  <div class="checkin-audit-container">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon><DocumentChecked /></el-icon>
            签到审核
          </h1>
          <p class="page-description">审核用户提交的签到记录，通过或拒绝签到申请</p>
        </div>
        <div class="header-right">
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" :max="99" class="badge-item">
            <el-button type="primary" @click="fetchRecords">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </el-badge>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ pendingCount }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>
    </div>

    <!-- 审核记录列表 -->
    <el-card class="records-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><List /></el-icon>
            审核记录
          </span>
        </div>
      </template>

      <!-- 选项卡 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="audit-tabs">
        <el-tab-pane label="待审核" name="PENDING">
          <template #label>
            <span class="tab-label">
              <el-icon><Clock /></el-icon>
              待审核
              <el-badge v-if="pendingCount > 0" :value="pendingCount" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已通过" name="APPROVED">
          <template #label>
            <span class="tab-label">
              <el-icon><CircleCheck /></el-icon>
              已通过
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已拒绝" name="REJECTED">
          <template #label>
            <span class="tab-label">
              <el-icon><CircleClose /></el-icon>
              已拒绝
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <el-table
        v-loading="loading"
        :data="records"
        stripe
        style="width: 100%"
        :empty-text="loading ? '加载中...' : '暂无待审核记录'"
      >
        <el-table-column prop="id" label="ID" width="80" />
        
        <el-table-column label="用户" width="180">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="32">
                {{ row.userName?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="user-details">
                <div class="user-name">{{ row.userName || '未知用户' }}</div>
                <div class="user-department">{{ row.departmentName || '未分配部门' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="打卡配置" min-width="200">
          <template #default="{ row }">
            <div class="config-info">
              <div class="config-name">{{ row.configurationName || '未知配置' }}</div>
              <div class="config-detail">
                <el-tag size="small" type="info">{{ row.locationName || '未知地点' }}</el-tag>
                <el-tag size="small" style="margin-left: 5px;">{{ row.sessionName || '未知时段' }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="签到时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.checkinTime) }}
          </template>
        </el-table-column>

        <el-table-column label="位置" min-width="150">
          <template #default="{ row }">
            <div class="location-info">
              <el-icon><Location /></el-icon>
              <span>{{ row.checkinAddress || '未提供' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="签到方式" width="120">
          <template #default="{ row }">
            <el-tag 
              :type="getCheckinMethodType(row.checkinMethod)" 
              size="small"
            >
              {{ getCheckinMethodText(row.checkinMethod) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="备注" min-width="150">
          <template #default="{ row }">
            <span class="notes">{{ row.notes || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="照片" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.checkinPhoto"
              :src="row.checkinPhoto"
              :preview-src-list="[row.checkinPhoto]"
              fit="cover"
              style="width: 50px; height: 50px; border-radius: 4px; cursor: pointer;"
            />
            <span v-else class="no-photo">-</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <!-- 待审核选项卡：显示审核按钮 -->
            <div v-if="activeTab === 'PENDING'" class="action-buttons">
              <el-button
                type="success"
                size="small"
                @click="approveRecord(row)"
                :loading="row.processing"
              >
                <el-icon><CircleCheck /></el-icon>
                通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="showRejectDialog(row)"
                :loading="row.processing"
              >
                <el-icon><CircleClose /></el-icon>
                拒绝
              </el-button>
              <el-button
                type="info"
                size="small"
                plain
                @click="deleteRecord(row)"
                :loading="row.processing"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
            
            <!-- 已审核选项卡：显示审核信息和删除按钮 -->
            <div v-else class="audit-result-with-delete">
              <div class="audit-result">
                <div v-if="row.auditedByName" class="audit-info-compact">
                  <div class="audit-person">
                    <el-icon><User /></el-icon>
                    <span>{{ row.auditedByName }}</span>
                  </div>
                  <div class="audit-time">
                    {{ formatDateTime(row.auditTime) }}
                  </div>
                </div>
                <el-tooltip v-if="row.auditNotes" :content="row.auditNotes" placement="top">
                  <el-tag size="small" type="info">
                    <el-icon><Document /></el-icon>
                    查看备注
                  </el-tag>
                </el-tooltip>
              </div>
              <el-button
                type="danger"
                size="small"
                plain
                @click="deleteRecord(row)"
                :loading="row.processing"
                class="delete-btn-compact"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 拒绝签到对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝签到"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="用户">
          <span>{{ selectedRecord?.user?.realName }}</span>
        </el-form-item>
        <el-form-item label="签到时间">
          <span>{{ formatDateTime(selectedRecord?.checkinTime) }}</span>
        </el-form-item>
        <el-form-item label="拒绝原因" required>
          <el-input
            v-model="rejectForm.auditNotes"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因，将记录在审核备注中"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button
          type="danger"
          @click="confirmReject"
          :loading="rejecting"
          :disabled="!rejectForm.auditNotes"
        >
          确认拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DocumentChecked, Refresh, Clock, List, Location, CircleCheck, CircleClose, User, Document, Delete
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { formatDateTime } from '@/utils/dateFormatter'

// 数据
const loading = ref(false)
const records = ref([])
const pendingCount = ref(0)
const total = ref(0)
const activeTab = ref('PENDING') // 当前选中的选项卡

const pagination = reactive({
  currentPage: 1,
  pageSize: 20
})

const rejectDialogVisible = ref(false)
const selectedRecord = ref(null)
const rejecting = ref(false)
const rejectForm = reactive({
  auditNotes: ''
})

// 方法
const fetchRecords = async () => {
  loading.value = true
  try {
    // 根据选项卡状态选择不同的API
    let endpoint = '/checkin/audit/pending'
    if (activeTab.value === 'APPROVED') {
      endpoint = '/checkin/audit/approved'
    } else if (activeTab.value === 'REJECTED') {
      endpoint = '/checkin/audit/rejected'
    }
    
    const response = await request.get(endpoint, {
      params: {
        page: pagination.currentPage - 1,
        size: pagination.pageSize
      }
    })
    
    records.value = response.data?.content || []
    total.value = response.data?.totalElements || 0
    
    // 初始化processing状态
    records.value.forEach(record => {
      record.processing = false
    })
    
    console.log(`${activeTab.value}记录:`, records.value)
  } catch (error) {
    console.error('获取审核记录失败:', error)
    ElMessage.error('获取审核记录失败')
  } finally {
    loading.value = false
  }
}

// 获取待审核数量
const fetchPendingCount = async () => {
  try {
    const response = await request.get('/checkin/audit/pending/count')
    pendingCount.value = response.data.pendingCount || 0
  } catch (error) {
    console.error('获取待审核数量失败:', error)
  }
}

// 选项卡切换
const handleTabChange = (tabName) => {
  pagination.currentPage = 1
  fetchRecords()
}

const approveRecord = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确认通过 ${record.userName} 的签到申请吗？`,
      '确认审核',
      {
        confirmButtonText: '确认通过',
        cancelButtonText: '取消',
        type: 'success'
      }
    )
    
    record.processing = true
    
    await request.post(`/checkin/audit/${record.id}/approve`, {
      auditNotes: '审核通过'
    })
    
    ElMessage.success('审核通过！')
    fetchRecords()
    fetchPendingCount() // 更新待审核数量
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过失败:', error)
      ElMessage.error(error.response?.data?.message || '审核失败')
    }
  } finally {
    record.processing = false
  }
}

const showRejectDialog = (record) => {
  selectedRecord.value = record
  rejectForm.auditNotes = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.auditNotes) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  rejecting.value = true
  
  try {
    await request.post(`/checkin/audit/${selectedRecord.value.id}/reject`, {
      auditNotes: rejectForm.auditNotes
    })
    
    ElMessage.success('已拒绝签到，标记为缺勤')
    rejectDialogVisible.value = false
    fetchRecords()
    fetchPendingCount() // 更新待审核数量
    
  } catch (error) {
    console.error('拒绝签到失败:', error)
    ElMessage.error(error.response?.data?.message || '拒绝失败')
  } finally {
    rejecting.value = false
  }
}

// 删除签到记录（物理删除）
const deleteRecord = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确认要永久删除用户 "${record.userName}" 的签到记录吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    record.processing = true
    
    await request.delete(`/checkin/records/${record.id}/physical`)
    
    ElMessage.success('签到记录已永久删除')
    fetchRecords()
    if (activeTab.value === 'PENDING') {
      fetchPendingCount() // 如果是待审核记录，更新待审核数量
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除签到记录失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  } finally {
    record.processing = false
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchRecords()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchRecords()
}

// 签到方式相关方法
const getCheckinMethodText = (method) => {
  switch (method) {
    case 'GPS': return 'GPS定位'
    case 'QR_CODE': return '二维码'
    case 'MANUAL_AUDIT': return '管理员审核'
    default: return '未知'
  }
}

const getCheckinMethodType = (method) => {
  switch (method) {
    case 'GPS': return 'primary'
    case 'QR_CODE': return 'success'
    case 'MANUAL_AUDIT': return 'warning'
    default: return 'info'
  }
}

onMounted(() => {
  fetchRecords()
  fetchPendingCount()
})
</script>

<style scoped>
.checkin-audit-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-description {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.pending {
  background: #fff3e0;
  color: #f59e0b;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

/* 记录卡片 */
.records-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 表格内容样式 */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.user-department {
  font-size: 12px;
  color: #909399;
}

.config-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.config-name {
  font-weight: 500;
  color: #303133;
}

.config-detail {
  display: flex;
  gap: 4px;
}

.location-info {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
}

.notes {
  color: #606266;
  font-size: 14px;
}

.no-photo {
  color: #c0c4cc;
}

/* 分页 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 选项卡样式 */
.audit-tabs {
  margin-bottom: 20px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.tab-label .el-icon {
  font-size: 16px;
}

.tab-badge :deep(.el-badge__content) {
  margin-left: 4px;
}

/* 操作列样式 */
.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.audit-result-with-delete {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.audit-result {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.delete-btn-compact {
  align-self: flex-start;
}

.audit-info-compact {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}

.audit-person {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-weight: 500;
}

.audit-person .el-icon {
  font-size: 14px;
  color: #909399;
}

.audit-time {
  color: #909399;
  font-size: 11px;
  padding-left: 18px;
}

/* 响应式 */
@media (max-width: 768px) {
  .checkin-audit-container {
    padding: 12px;
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>

