<template>
  <div class="site-config-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>站点配置管理</h2>
      <p>管理登录界面背景、网站LOGO等全局配置</p>
    </div>

    <!-- 操作按钮区域 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        添加配置
      </el-button>
      <el-button @click="initDefaults">
        <el-icon><Setting /></el-icon>
        初始化默认配置
      </el-button>
      <el-button type="warning" @click="resetDefaults">
        <el-icon><RefreshLeft /></el-icon>
        重置为默认
      </el-button>
      <el-button type="info" @click="fixPaths">
        <el-icon><Tools /></el-icon>
        修复路径
      </el-button>
      <el-button @click="loadConfigs">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
      <el-button type="primary" plain @click="openConfigDialog">
        <el-icon><Setting /></el-icon>
        所有配置
      </el-button>
      <el-button type="success" @click="debugImages">
        <el-icon><Tools /></el-icon>
        调试图片
      </el-button>
    </div>

    <!-- 快捷配置卡片 -->
    <div class="quick-config-section">
      <h3>快捷配置</h3>
      <el-row :gutter="20">
        <!-- LOGO上传 -->
        <el-col :span="12">
          <el-card class="quick-card">
            <template #header>
              <div class="card-header">
                <span>网站LOGO</span>
                <div class="header-actions">
                  <el-button v-if="currentLogo" type="text" @click="previewLogo">预览</el-button>
                  <el-button v-if="currentLogo" type="text" @click="handleRemoveLogo" class="remove-btn">恢复默认</el-button>
                </div>
              </div>
            </template>
            <div class="upload-section">
              <div class="current-image logo-preview" v-if="currentLogo">
                <img :src="currentLogo" alt="当前LOGO" />
              </div>
              <el-upload
                class="logo-uploader"
                :show-file-list="false"
                :http-request="selectLogoForCrop"
                accept="image/*"
              >
                <el-button type="primary" :loading="logoUploading">
                  <el-icon><Upload /></el-icon>
                  上传LOGO
                </el-button>
              </el-upload>
            </div>
          </el-card>
        </el-col>

        <!-- 登录背景上传 -->
        <el-col :span="12">
          <el-card class="quick-card">
            <template #header>
              <div class="card-header">
                <span>登录背景</span>
                <div class="header-actions">
                  <el-button v-if="currentBackground" type="text" @click="previewBackground">预览</el-button>
                  <el-button v-if="currentBackground" type="text" @click="handleRemoveBackground" class="remove-btn">恢复默认</el-button>
                </div>
              </div>
            </template>
            <div class="upload-section">
              <div class="current-image" v-if="currentBackground">
                <img :src="currentBackground" alt="当前背景" />
              </div>
              <el-upload
                class="background-uploader"
                :action="`${apiBaseUrl}/site-config/admin/upload-background`"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleBackgroundSuccess"
                :before-upload="beforeUpload"
                accept="image/*"
              >
                <el-button type="primary">
                  <el-icon><Upload /></el-icon>
                  上传背景
                </el-button>
              </el-upload>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- QQ邮箱与提醒设置 -->
    <div class="mail-config-section">
      <h3>QQ邮箱与提醒设置</h3>
      <el-card class="mail-config-panel">
        <el-form :model="mailForm" class="mail-config-form" label-width="132px">
          <el-row :gutter="20">
            <el-col :xs="24">
              <el-form-item label="启用邮件功能">
                <el-switch v-model="mailForm.enabled" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :xs="24" :md="12">
              <el-form-item label="QQ邮箱账号">
                <el-input v-model="mailForm.qqAccount" placeholder="example@qq.com" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="QQ邮箱授权码">
                <el-input
                  v-model="mailForm.qqAuthCode"
                  type="password"
                  show-password
                  clearable
                  :placeholder="mailAuthCodeConfigured ? '已配置，留空不修改' : '请输入SMTP授权码'"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="发件人名称">
                <el-input v-model="mailForm.senderName" placeholder="融媒体管理系统" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="SMTP服务器">
                <el-input
                  v-model="mailForm.smtpHost"
                  placeholder="smtp.qq.com"
                  clearable
                  @blur="normalizeSmtpHostInput"
                >
                  <template #append>
                    <el-button @click="restoreQqSmtpHost">恢复默认</el-button>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="6">
              <el-form-item label="SMTP端口">
                <el-input-number
                  v-model="mailForm.smtpPort"
                  class="mail-number-input"
                  :min="1"
                  :max="65535"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="2">
              <el-form-item label="SSL" class="mail-compact-form-item" label-width="48px">
                <el-switch v-model="mailForm.smtpSslEnabled" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-divider content-position="left">提醒规则</el-divider>

          <el-row :gutter="20">
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="提前提醒分钟数">
                <el-input-number
                  v-model="mailForm.reminderAdvanceMinutes"
                  class="mail-number-input"
                  :min="0"
                  :max="1440"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="逾期提醒间隔(小时)">
                <el-input-number
                  v-model="mailForm.overdueReminderIntervalHours"
                  class="mail-number-input"
                  :min="1"
                  :max="720"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="日志保留天数">
                <el-input-number
                  v-model="mailForm.logRetentionDays"
                  class="mail-number-input"
                  :min="1"
                  :max="3650"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="执勤提醒">
                <el-switch v-model="mailForm.dutyReminderEnabled" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="晚自习提醒">
                <el-switch v-model="mailForm.checkinReminderEnabled" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="请假审批提醒">
                <el-switch v-model="mailForm.leaveApprovalReminderEnabled" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="设备逾期提醒">
                <el-switch v-model="mailForm.borrowOverdueReminderEnabled" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12" :xl="8">
              <el-form-item label="入部面试QQ群">
                <el-input v-model.trim="mailForm.joinInterviewQqGroup" maxlength="12" placeholder="请输入面试QQ群号" clearable />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="mail-actions">
            <el-button type="primary" :loading="savingMailConfig" @click="saveMailConfigs">
              保存QQ邮箱设置
            </el-button>
            <el-input
              v-model="testMailEmail"
              class="mail-test-input"
              placeholder="测试收件邮箱"
              clearable
            >
              <template #append>
                <el-button :loading="testingMail" @click="testMail">
                  发送测试
                </el-button>
              </template>
            </el-input>
          </div>
        </el-form>
      </el-card>
    </div>

    <!-- 邮件发送日志 -->
    <div class="mail-log-section">
      <div class="section-title-row">
        <div>
          <h3>邮件发送日志</h3>
          <p>查看测试邮件、注册验证码和提醒邮件的发送结果</p>
        </div>
        <el-button :loading="mailLogsLoading" @click="loadMailLogs">
          <el-icon><Refresh /></el-icon>
          刷新日志
        </el-button>
      </div>
      <el-card class="mail-log-panel">
        <div class="mail-log-toolbar">
          <el-select
            v-model="mailLogFilters.notificationType"
            placeholder="邮件类型"
            clearable
            @change="handleMailLogFilterChange"
          >
            <el-option label="测试邮件" value="TEST_MAIL" />
            <el-option label="注册验证码" value="REGISTER_CODE" />
            <el-option label="执勤提醒" value="DUTY_REMINDER" />
            <el-option label="晚自习打卡提醒" value="CHECKIN_REMINDER" />
            <el-option label="请假审批提醒" value="LEAVE_APPROVAL" />
            <el-option label="请假通过通知" value="LEAVE_APPROVED" />
            <el-option label="入部面试通知" value="JOIN_INTERVIEW" />
            <el-option label="设备逾期提醒" value="BORROW_OVERDUE" />
          </el-select>
          <el-select
            v-model="mailLogFilters.success"
            placeholder="发送状态"
            clearable
            @change="handleMailLogFilterChange"
          >
            <el-option label="成功" :value="true" />
            <el-option label="失败" :value="false" />
          </el-select>
        </div>

        <el-table
          :data="mailLogs"
          v-loading="mailLogsLoading"
          stripe
          empty-text="暂无邮件发送日志"
        >
          <el-table-column prop="sentAt" label="发送时间" width="180" align="center">
            <template #default="{ row }">
              {{ formatDateTime(row.sentAt) }}
            </template>
          </el-table-column>
          <el-table-column label="邮件类型" min-width="150">
            <template #default="{ row }">
              <el-tag size="small" effect="plain">
                {{ row.notificationTypeDescription || row.notificationType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="收件人" min-width="220" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="mail-recipient">
                <span>{{ row.recipientName || '-' }}</span>
                <small>{{ row.recipientEmail }}</small>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.success ? 'success' : 'danger'" size="small">
                {{ row.success ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="关联业务" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              {{ formatMailBusiness(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="periodKey" label="周期键" min-width="160" show-overflow-tooltip />
          <el-table-column label="错误信息" min-width="260" show-overflow-tooltip>
            <template #default="{ row }">
              <span :class="{ 'mail-error-text': !row.success && row.errorMessage }">
                {{ row.errorMessage || '-' }}
              </span>
            </template>
          </el-table-column>
        </el-table>

        <div class="mail-log-pagination">
          <el-pagination
            v-model:current-page="mailLogPagination.page"
            v-model:page-size="mailLogPagination.size"
            :page-sizes="[10, 20, 50]"
            :total="mailLogPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleMailLogSizeChange"
            @current-change="handleMailLogPageChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 配置列表弹窗 -->
    <el-dialog
      v-model="configDialogVisible"
      title="所有配置"
      width="92%"
      top="5vh"
      class="config-list-dialog"
    >
      <div class="config-dialog-toolbar">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加配置
        </el-button>
        <el-button :loading="loading" @click="loadConfigs">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      <el-table :data="configs" v-loading="loading" stripe max-height="62vh">
        <el-table-column prop="configKey" label="配置键" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="150" />
        <el-table-column prop="configTypeDescription" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagColor(row.configType)" size="small">
              {{ row.configTypeDescription }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.configType === 'IMAGE'" class="image-value">
              <img v-if="row.configValue" :src="getUploadedImageUrl(row.configValue)" alt="图片" class="table-image" />
              <span v-else>-</span>
            </span>
            <span v-else-if="row.configType === 'COLOR'" class="color-value">
              <span class="color-preview" :style="{ backgroundColor: row.configValue }"></span>
              {{ row.configValue }}
            </span>
            <span v-else>{{ row.configValue || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editConfig(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteConfig(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加/编辑配置对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑配置' : '添加配置'"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="配置键" prop="configKey">
          <el-input
            v-model="form.configKey"
            placeholder="请输入配置键"
            :disabled="isEdit"
          />
        </el-form-item>
        
        <el-form-item label="配置类型" prop="configType">
          <el-select v-model="form.configType" placeholder="请选择配置类型" style="width: 100%">
            <el-option label="文本" value="TEXT" />
            <el-option label="图片" value="IMAGE" />
            <el-option label="颜色" value="COLOR" />
            <el-option label="数字" value="NUMBER" />
            <el-option label="布尔值" value="BOOLEAN" />
            <el-option label="JSON" value="JSON" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="配置值" prop="configValue">
          <el-input
            v-if="form.configType === 'TEXT' || form.configType === 'IMAGE' || form.configType === 'JSON'"
            v-model="form.configValue"
            type="textarea"
            :rows="3"
            placeholder="请输入配置值"
          />
          <el-color-picker
            v-else-if="form.configType === 'COLOR'"
            v-model="form.configValue"
          />
          <el-input-number
            v-else-if="form.configType === 'NUMBER'"
            v-model="form.configValue"
            style="width: 100%"
          />
          <el-switch
            v-else-if="form.configType === 'BOOLEAN'"
            v-model="form.configValue"
            active-text="是"
            inactive-text="否"
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            placeholder="请输入配置描述"
          />
        </el-form-item>
        
        <el-form-item label="排序权重" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="启用状态">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveConfig">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="800px">
      <div class="preview-container">
        <img :src="previewUrl" alt="预览图片" class="preview-image" />
      </div>
    </el-dialog>

    <LogoCropperDialog
      v-model="logoCropVisible"
      :file="logoCropFile"
      @cropped="uploadCroppedLogo"
      @cancel="logoCropFile = null"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Setting, RefreshLeft, Refresh, Upload, Tools } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { getSiteImageUrl, getUploadedImageUrl } from '@/utils/imageUrl'
import LogoCropperDialog from '@/components/LogoCropperDialog.vue'

// 响应式数据
const loading = ref(false)
const configs = ref([])
const dialogVisible = ref(false)
const configDialogVisible = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const isEdit = ref(false)
const formRef = ref()
const savingMailConfig = ref(false)
const testingMail = ref(false)
const testMailEmail = ref('')
const mailAuthCodeConfigured = ref(false)
const logoCropVisible = ref(false)
const logoCropFile = ref(null)
const logoUploading = ref(false)
const qqSmtpHost = 'smtp.qq.com'
const mailLogsLoading = ref(false)
const mailLogs = ref([])

const mailLogFilters = reactive({
  notificationType: '',
  success: ''
})

const mailLogPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const mailForm = reactive({
  enabled: false,
  smtpHost: qqSmtpHost,
  smtpPort: 465,
  smtpSslEnabled: true,
  qqAccount: '',
  qqAuthCode: '',
  senderName: '融媒体管理系统',
  reminderAdvanceMinutes: 30,
  overdueReminderIntervalHours: 24,
  logRetentionDays: 30,
  dutyReminderEnabled: true,
  checkinReminderEnabled: true,
  leaveApprovalReminderEnabled: true,
  borrowOverdueReminderEnabled: true,
  joinInterviewQqGroup: ''
})

const mailConfigDefinitions = [
  { field: 'enabled', key: 'mail.enabled', description: '是否启用QQ邮箱验证码与提醒', configType: 'BOOLEAN', defaultValue: false },
  { field: 'smtpHost', key: 'mail.smtp_host', description: 'QQ邮箱SMTP服务器', configType: 'TEXT', defaultValue: qqSmtpHost },
  { field: 'smtpPort', key: 'mail.smtp_port', description: 'QQ邮箱SMTP端口', configType: 'NUMBER', defaultValue: 465 },
  { field: 'smtpSslEnabled', key: 'mail.smtp_ssl_enabled', description: '是否启用SMTP SSL', configType: 'BOOLEAN', defaultValue: true },
  { field: 'qqAccount', key: 'mail.qq_account', description: 'QQ邮箱账号', configType: 'TEXT', defaultValue: '' },
  { field: 'qqAuthCode', key: 'mail.qq_auth_code', description: 'QQ邮箱SMTP授权码', configType: 'TEXT', defaultValue: '' },
  { field: 'senderName', key: 'mail.sender_name', description: '邮件发件人名称', configType: 'TEXT', defaultValue: '融媒体管理系统' },
  { field: 'reminderAdvanceMinutes', key: 'mail.reminder_advance_minutes', description: '执勤和晚自习提醒提前分钟数', configType: 'NUMBER', defaultValue: 30 },
  { field: 'overdueReminderIntervalHours', key: 'mail.overdue_reminder_interval_hours', description: '设备逾期归还提醒间隔小时数', configType: 'NUMBER', defaultValue: 24 },
  { field: 'logRetentionDays', key: 'mail.log_retention_days', description: '邮件发送日志与验证码记录保留天数', configType: 'NUMBER', defaultValue: 30 },
  { field: 'dutyReminderEnabled', key: 'mail.duty_reminder_enabled', description: '执勤提醒开关', configType: 'BOOLEAN', defaultValue: true },
  { field: 'checkinReminderEnabled', key: 'mail.checkin_reminder_enabled', description: '晚自习打卡提醒开关', configType: 'BOOLEAN', defaultValue: true },
  { field: 'leaveApprovalReminderEnabled', key: 'mail.leave_approval_reminder_enabled', description: '请假审批提醒开关', configType: 'BOOLEAN', defaultValue: true },
  { field: 'borrowOverdueReminderEnabled', key: 'mail.borrow_overdue_reminder_enabled', description: '设备逾期归还提醒开关', configType: 'BOOLEAN', defaultValue: true },
  { field: 'joinInterviewQqGroup', key: 'join.interview_qq_group', description: '入部面试QQ群号', configType: 'TEXT', defaultValue: '' }
]

// API基础URL
const apiBaseUrl = computed(() => {
  return request.defaults.baseURL || '/api'
})

// 上传头部信息
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

// 当前LOGO和背景
const currentLogo = computed(() => {
  const logoConfig = configs.value.find(c => c.configKey === 'site.logo')
  return getSiteImageUrl(logoConfig?.configValue) || null
})

const currentBackground = computed(() => {
  const bgConfig = configs.value.find(c => c.configKey === 'login.background')
  return getSiteImageUrl(bgConfig?.configValue) || null
})

// 表单数据
const form = reactive({
  configKey: '',
  configValue: '',
  description: '',
  configType: 'TEXT',
  enabled: true,
  sortOrder: 0
})

// 表单验证规则
const rules = {
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' }
  ],
  configType: [
    { required: true, message: '请选择配置类型', trigger: 'change' }
  ]
}

// 生命周期
onMounted(() => {
  loadConfigs()
  loadMailLogs()
})

// 加载配置列表
const loadConfigs = async () => {
  loading.value = true
  try {
    const response = await request.get('/site-config/admin/list')
    if (response.success) {
      configs.value = response.data || []
      syncMailForm()
    }
  } catch (error) {
    ElMessage.error('加载配置失败')
    console.error('加载配置失败:', error)
  } finally {
    loading.value = false
  }
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
}

const openConfigDialog = async () => {
  configDialogVisible.value = true
  if (!configs.value.length) {
    await loadConfigs()
  }
}

// 编辑配置
const editConfig = (config) => {
  isEdit.value = true
  Object.assign(form, {
    id: config.id,
    configKey: config.configKey,
    configValue: config.configValue,
    description: config.description,
    configType: config.configType,
    enabled: config.enabled,
    sortOrder: config.sortOrder || 0
  })
  dialogVisible.value = true
}

// 保存配置
const saveConfig = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    const url = isEdit.value ? `/site-config/admin/${form.id}` : '/site-config/admin/save'
    const method = isEdit.value ? 'put' : 'post'
    
    const response = await request[method](url, form)
    if (response.success) {
      ElMessage.success(isEdit.value ? '配置更新成功' : '配置添加成功')
      dialogVisible.value = false
      loadConfigs()
    }
  } catch (error) {
    if (error.name !== 'ElFormValidateError') {
      ElMessage.error('保存配置失败')
      console.error('保存配置失败:', error)
    }
  }
}

// 加载邮件发送日志
const loadMailLogs = async () => {
  mailLogsLoading.value = true
  try {
    const params = {
      page: mailLogPagination.page - 1,
      size: mailLogPagination.size
    }

    if (mailLogFilters.notificationType) {
      params.notificationType = mailLogFilters.notificationType
    }
    if (typeof mailLogFilters.success === 'boolean') {
      params.success = mailLogFilters.success
    }

    const response = await request.get('/site-config/admin/mail/logs', { params })
    if (response.success) {
      const pageData = response.data || {}
      mailLogs.value = pageData.content || []
      mailLogPagination.total = Number(pageData.totalElements || 0)
    }
  } catch (error) {
    ElMessage.error(error.message || '加载邮件日志失败')
    console.error('加载邮件日志失败:', error)
  } finally {
    mailLogsLoading.value = false
  }
}

const handleMailLogFilterChange = () => {
  mailLogPagination.page = 1
  loadMailLogs()
}

const handleMailLogSizeChange = (size) => {
  mailLogPagination.size = size
  mailLogPagination.page = 1
  loadMailLogs()
}

const handleMailLogPageChange = (page) => {
  mailLogPagination.page = page
  loadMailLogs()
}

const syncMailForm = () => {
  mailConfigDefinitions.forEach((definition) => {
    const config = configs.value.find(item => item.configKey === definition.key)
    const rawValue = config?.configValue

    if (definition.field === 'qqAuthCode') {
      mailAuthCodeConfigured.value = rawValue === '******'
      mailForm.qqAuthCode = ''
      return
    }

    if (definition.configType === 'BOOLEAN') {
      mailForm[definition.field] = rawValue === undefined ? definition.defaultValue : rawValue === true || rawValue === 'true'
      return
    }

    if (definition.configType === 'NUMBER') {
      const parsed = Number(rawValue)
      mailForm[definition.field] = Number.isFinite(parsed) ? parsed : definition.defaultValue
      return
    }

    mailForm[definition.field] = rawValue ?? definition.defaultValue
  })
}

const buildMailConfigPayload = () => {
  return mailConfigDefinitions.map((definition, index) => {
    let configValue = mailForm[definition.field]
    if (definition.field === 'smtpHost') {
      configValue = normalizeSmtpHost(configValue)
    }
    if (definition.configType === 'BOOLEAN') {
      configValue = String(Boolean(configValue))
    } else if (definition.configType === 'NUMBER') {
      configValue = String(configValue ?? definition.defaultValue)
    } else {
      configValue = String(configValue ?? '')
    }

    return {
      configKey: definition.key,
      configValue,
      description: definition.description,
      configType: definition.configType,
      enabled: true,
      sortOrder: 1000 + index
    }
  })
}

const normalizeSmtpHost = (value) => String(value || '').trim().toLowerCase()

const normalizeSmtpHostInput = () => {
  mailForm.smtpHost = normalizeSmtpHost(mailForm.smtpHost)
}

const restoreQqSmtpHost = () => {
  mailForm.smtpHost = qqSmtpHost
  mailForm.smtpPort = 465
  mailForm.smtpSslEnabled = true
}

const validateMailSettings = ({ requireAuthCode = false } = {}) => {
  const smtpHost = normalizeSmtpHost(mailForm.smtpHost)
  if (!smtpHost) {
    ElMessage.warning('SMTP服务器不能为空，QQ邮箱请填写 smtp.qq.com')
    return false
  }
  if (smtpHost.includes('@')) {
    ElMessage.warning('SMTP服务器不能填写邮箱账号，请填写 smtp.qq.com')
    return false
  }
  if (smtpHost.includes('/') || smtpHost.includes(':') || smtpHost.startsWith('http')) {
    ElMessage.warning('SMTP服务器只填写域名即可，例如 smtp.qq.com，不要包含 http、端口或路径')
    return false
  }
  if (!isValidEmail(mailForm.qqAccount)) {
    ElMessage.warning('请输入正确的QQ邮箱账号，例如 example@qq.com')
    return false
  }
  if (!Number.isInteger(Number(mailForm.smtpPort)) || Number(mailForm.smtpPort) < 1 || Number(mailForm.smtpPort) > 65535) {
    ElMessage.warning('SMTP端口必须在 1 到 65535 之间，QQ邮箱推荐 465')
    return false
  }
  if (!Number.isInteger(Number(mailForm.logRetentionDays)) || Number(mailForm.logRetentionDays) < 1 || Number(mailForm.logRetentionDays) > 3650) {
    ElMessage.warning('日志保留天数必须在 1 到 3650 天之间')
    return false
  }
  if (requireAuthCode && !mailAuthCodeConfigured.value && !String(mailForm.qqAuthCode || '').trim()) {
    ElMessage.warning('请填写QQ邮箱SMTP授权码，授权码不是QQ登录密码')
    return false
  }
  const joinInterviewQqGroup = String(mailForm.joinInterviewQqGroup || '').trim()
  if (joinInterviewQqGroup && !/^\d{5,12}$/.test(joinInterviewQqGroup)) {
    ElMessage.warning('入部面试QQ群号必须是5到12位数字')
    return false
  }
  return true
}

const saveMailConfigs = async () => {
  if (!validateMailSettings()) {
    return
  }

  savingMailConfig.value = true
  try {
    const response = await request.post('/site-config/admin/batch', buildMailConfigPayload())
    if (response.success) {
      ElMessage.success('QQ邮箱与提醒设置已保存')
      mailForm.qqAuthCode = ''
      await loadConfigs()
    }
  } catch (error) {
    ElMessage.error(error.message || '保存QQ邮箱设置失败')
    console.error('保存QQ邮箱设置失败:', error)
  } finally {
    savingMailConfig.value = false
  }
}

const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(email || '').trim())

const testMail = async () => {
  if (!isValidEmail(testMailEmail.value)) {
    ElMessage.warning('请输入正确的测试收件邮箱')
    return
  }
  if (!validateMailSettings({ requireAuthCode: true })) {
    return
  }

  testingMail.value = true
  try {
    const response = await request.post('/site-config/admin/mail/test', {
      email: testMailEmail.value.trim()
    })
    if (response.success) {
      ElMessage.success('测试邮件已发送')
    }
  } catch (error) {
    ElMessage.error(error.message || '测试邮件发送失败')
    console.error('测试邮件发送失败:', error)
  } finally {
    testingMail.value = false
    await loadMailLogs()
  }
}

// 删除配置
const deleteConfig = async (config) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除配置 "${config.configKey}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await request.delete(`/site-config/admin/${config.id}`)
    if (response.success) {
      ElMessage.success('配置删除成功')
      loadConfigs()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除配置失败')
      console.error('删除配置失败:', error)
    }
  }
}

// 初始化默认配置
const initDefaults = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要初始化默认配置吗？这将添加系统预设的配置项。',
      '初始化确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    const response = await request.post('/site-config/admin/init-defaults')
    if (response.success) {
      ElMessage.success('默认配置初始化成功')
      loadConfigs()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('初始化失败')
      console.error('初始化失败:', error)
    }
  }
}

// 重置为默认配置
const resetDefaults = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置为默认配置吗？这将删除所有现有配置并恢复为系统默认值！',
      '重置确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await request.post('/site-config/admin/reset-defaults')
    if (response.success) {
      ElMessage.success('配置重置成功')
      loadConfigs()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重置失败')
      console.error('重置失败:', error)
    }
  }
}

// 修复路径重复问题
const fixPaths = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要修复路径重复问题吗？这将修复LOGO和背景路径中的重复uploads前缀。',
      '修复路径确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    const response = await request.post('/site-config/admin/fix-paths')
    if (response.success) {
      ElMessage.success('路径修复成功')
      loadConfigs()
    } else {
      ElMessage.error(response.message || '路径修复失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('路径修复失败')
      console.error('路径修复失败:', error)
    }
  }
}

// 🔧 调试图片文件状态
const debugImages = async () => {
  try {
    const response = await request.get('/site-config/admin/debug-images')
    if (response.success) {
      console.log('🔍 站点图片调试信息:', response.data)
      
      // 构建调试信息提示
      const debugInfo = response.data
      let message = '图片调试信息:\n\n'
      
      // LOGO信息
      if (debugInfo.logo) {
        message += `📷 LOGO:\n`
        message += `  路径: ${debugInfo.logo.path || '未设置'}\n`
        message += `  文件存在: ${debugInfo.logo.exists ? '✅ 是' : '❌ 否'}\n`
        message += `  文件大小: ${debugInfo.logo.size} 字节\n\n`
      }
      
      // 背景信息
      if (debugInfo.background) {
        message += `🖼️ 登录背景:\n`
        message += `  路径: ${debugInfo.background.path || '未设置'}\n`
        message += `  文件存在: ${debugInfo.background.exists ? '✅ 是' : '❌ 否'}\n`
        message += `  文件大小: ${debugInfo.background.size} 字节\n\n`
      }
      
      // 系统信息
      if (debugInfo.system) {
        message += `🖥️ 系统信息:\n`
        message += `  操作系统: ${debugInfo.system.os}\n`
        message += `  工作目录: ${debugInfo.system['user.dir']}\n`
      }
      
      ElMessageBox.alert(message, '调试信息', {
        confirmButtonText: '确定',
        type: 'info'
      })
      
      ElMessage.success('调试信息获取成功，请查看控制台和弹窗')
    } else {
      ElMessage.error(response.message || '获取调试信息失败')
    }
  } catch (error) {
    ElMessage.error('获取调试信息失败')
    console.error('调试失败:', error)
  }
}

// 文件上传前检查
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

const selectLogoForCrop = (options) => {
  const file = options.file
  if (!beforeUpload(file)) {
    options.onError?.(new Error('LOGO文件不符合要求'))
    return
  }

  logoCropFile.value = file
  logoCropVisible.value = true
  options.onSuccess?.({})
}

const uploadCroppedLogo = async (file) => {
  logoUploading.value = true
  try {
    const data = new FormData()
    data.append('file', file)
    const response = await request.post('/site-config/admin/upload-logo', data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (response.success) {
      ElMessage.success('圆形LOGO上传成功')
      logoCropFile.value = null
      await loadConfigs()
    } else {
      ElMessage.error(response.message || 'LOGO上传失败')
    }
  } catch (error) {
    ElMessage.error(error.message || 'LOGO上传失败')
    console.error('LOGO上传失败:', error)
  } finally {
    logoUploading.value = false
  }
}

// 背景上传成功
const handleBackgroundSuccess = (response) => {
  if (response.success) {
    ElMessage.success('背景图上传成功')
    loadConfigs()
  } else {
    ElMessage.error(response.message || '背景图上传失败')
  }
}

// 恢复默认LOGO
const handleRemoveLogo = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要恢复默认LOGO吗？当前上传的LOGO将被移除。',
      '恢复默认LOGO',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 找到LOGO配置并删除配置值
    const logoConfig = configs.value.find(c => c.configKey === 'site.logo')
    if (logoConfig) {
      logoConfig.configValue = ''
      const response = await request.post('/site-config/admin/save', logoConfig)
      if (response.success) {
        ElMessage.success('已恢复默认LOGO')
        loadConfigs()
      } else {
        ElMessage.error(response.message || '操作失败')
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error('恢复默认LOGO失败:', error)
    }
  }
}

// 恢复默认背景
const handleRemoveBackground = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要恢复默认登录背景吗？当前上传的背景图将被移除。',
      '恢复默认背景',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 找到背景配置并删除配置值
    const backgroundConfig = configs.value.find(c => c.configKey === 'login.background')
    if (backgroundConfig) {
      backgroundConfig.configValue = ''
      const response = await request.post('/site-config/admin/save', backgroundConfig)
      if (response.success) {
        ElMessage.success('已恢复默认背景')
        loadConfigs()
      } else {
        ElMessage.error(response.message || '操作失败')
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error('恢复默认背景失败:', error)
    }
  }
}

// 预览LOGO
const previewLogo = () => {
  if (currentLogo.value) {
    previewUrl.value = currentLogo.value
    previewVisible.value = true
  } else {
    ElMessage.info('当前没有设置LOGO')
  }
}

// 预览背景
const previewBackground = () => {
  if (currentBackground.value) {
    previewUrl.value = currentBackground.value
    previewVisible.value = true
  } else {
    ElMessage.info('当前没有设置登录背景')
  }
}

// 获取类型标签颜色
const getTypeTagColor = (type) => {
  const colorMap = {
    TEXT: 'info',
    IMAGE: 'success',
    COLOR: 'warning',
    NUMBER: 'primary',
    BOOLEAN: 'danger',
    JSON: 'success'
  }
  return colorMap[type] || 'info'
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const formatMailBusiness = (row) => {
  const type = row.businessTypeDescription || row.businessType || '-'
  return row.businessId ? `${type} #${row.businessId}` : type
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    configKey: '',
    configValue: '',
    description: '',
    configType: 'TEXT',
    enabled: true,
    sortOrder: 0
  })
}
</script>

<style scoped>
.site-config-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.quick-config-section {
  margin-bottom: 30px;
}

.quick-config-section h3 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.mail-config-section {
  margin-bottom: 30px;
}

.mail-config-section h3 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.mail-config-panel {
  border-radius: 6px;
}

.mail-config-form {
  width: 100%;
}

.mail-config-form :deep(.el-row) {
  row-gap: 4px;
}

.mail-config-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.mail-number-input {
  width: 100%;
  min-width: 190px;
}

.mail-number-input :deep(.el-input__wrapper) {
  min-width: 0;
}

.mail-compact-form-item {
  min-width: 120px;
}

.mail-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.mail-test-input {
  width: 420px;
  max-width: 100%;
}

@media (max-width: 767px) {
  .mail-config-form {
    --mail-label-width: 118px;
  }

  .mail-config-form :deep(.el-form-item__label) {
    width: var(--mail-label-width) !important;
  }

  .mail-config-form :deep(.el-form-item__content) {
    margin-left: var(--mail-label-width) !important;
  }

  .mail-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .mail-actions .el-button,
  .mail-test-input,
  .mail-number-input {
    width: 100%;
    min-width: 0;
  }
}

.mail-log-section {
  margin-bottom: 30px;
}

.section-title-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.section-title-row h3 {
  margin: 0 0 6px 0;
  color: #303133;
  font-size: 16px;
}

.section-title-row p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.mail-log-panel {
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
  backdrop-filter: blur(14px);
}

.mail-log-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.mail-log-toolbar .el-select {
  width: 180px;
}

.mail-recipient {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.35;
}

.mail-recipient small {
  color: #909399;
}

.mail-error-text {
  color: #d93026;
}

.mail-log-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.quick-card {
  height: auto;
  min-height: 220px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.remove-btn {
  color: #f56c6c !important;
}

.remove-btn:hover {
  color: #f78989 !important;
}

.upload-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  min-height: 126px;
  height: auto;
}

.current-image {
  width: 80px;
  height: 60px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.current-image.logo-preview {
  width: 82px;
  height: 82px;
  border-radius: 999px;
  background: #fff;
}

.current-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}

.current-image.logo-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 999px;
}

.config-table-section h3 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.config-dialog-toolbar {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-bottom: 16px;
}

:deep(.config-list-dialog .el-dialog) {
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.18);
  backdrop-filter: blur(18px);
}

:deep(.config-list-dialog .el-dialog__body) {
  padding-top: 10px;
}

.table-image {
  width: 40px;
  height: 30px;
  object-fit: cover;
  border-radius: 4px;
}

.color-value {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-preview {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1px solid #dcdfe6;
}

.image-value {
  display: flex;
  align-items: center;
}

.preview-container {
  text-align: center;
}

.preview-image {
  max-width: 100%;
  max-height: 600px;
  object-fit: contain;
}
</style>
