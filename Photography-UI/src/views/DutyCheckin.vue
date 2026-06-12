<template>
  <div class="duty-checkin-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><OfficeBuilding /></el-icon>
            办公室执勤
          </h1>
          <p class="page-subtitle">执勤签到/签退，记录你的责任时光</p>
        </div>
        <div class="header-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>
      </div>
    </div>

    <!-- 今日执勤状态卡片 -->
    <div class="status-section">
      <el-card class="status-card modern-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon size="20"><Clock /></el-icon>
            <span>今日执勤状态</span>
            <el-tag v-if="todayRecord?.checkinTime" type="success" size="small">已签到</el-tag>
            <el-tag v-else type="info" size="small">未签到</el-tag>
          </div>
        </template>
        
        <div class="status-content">
          <div v-if="loading" class="status-loading">
            <SkeletonLoader type="stats" />
          </div>

          <template v-else>
            <div v-if="todaySwapInfo" class="swap-banner">
              <div class="swap-text">
                <strong>今日调换：</strong>{{ todaySwapBannerText }}
              </div>
              <div class="swap-meta">
                <span>调换日期：{{ formatDate(todaySwapInfo.swapDate) }}</span>
                <span v-if="todaySwapScheduleText">执勤时段：{{ todaySwapScheduleText }}</span>
              </div>
            </div>

            <div v-if="todayRecord?.status === '已请假'" class="leave-approved-status">
              <div class="leave-info">
                <div class="leave-icon">
                  <el-icon size="48" color="#67c23a"><DocumentChecked /></el-icon>
                </div>
                <div class="leave-message">
                  <h3>请假申请已通过</h3>
                  <p class="leave-text">无需签到</p>
                </div>
              </div>
              <div class="leave-details">
                <div class="info-row">
                  <span class="info-label">
                    执勤时段：
                    {{ todayRecord.dutySchedule?.startTime || todayRecord.startTime }} -
                    {{ todayRecord.dutySchedule?.endTime || todayRecord.endTime }}
                  </span>
                  <span class="info-extra">审核人：{{ getApproverName(todayRecord) }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">请假原因：{{ getLeaveReason(todayRecord) }}</span>
                  <span class="info-extra">审核时间：{{ getApproveTime(todayRecord) }}</span>
                </div>
              </div>
            </div>
            
            <div v-else-if="todayRecord?.checkinTime" class="checked-in-status">
              <div class="checkin-info">
                <div class="info-item">
                  <span class="label">签到时间：</span>
                  <span class="value">{{ formatDateTime(todayRecord.checkinTime) }}</span>
                </div>
                <div v-if="todayRecord.checkoutTime" class="info-item">
                  <span class="label">签退时间：</span>
                  <span class="value">{{ formatDateTime(todayRecord.checkoutTime) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">执勤时段：</span>
                  <span class="value">
                    {{ todayRecord.dutySchedule?.startTime || todayRecord.startTime }} -
                    {{ todayRecord.dutySchedule?.endTime || todayRecord.endTime }}
                  </span>
                </div>
                <div class="info-item">
                  <span class="label">执勤状态：</span>
                  <el-tag :type="getStatusType(todayRecord.status)" size="small">
                    {{ todayRecord.status }}
                  </el-tag>
                </div>
              </div>
              
              <div class="action-buttons">
                <el-button 
                  v-if="!todayRecord.checkoutTime"
                  type="warning" 
                  @click="showCheckoutDialog = true"
                  :disabled="!canCheckout"
                >
                  <el-icon><Upload /></el-icon>
                  签退
                </el-button>
                <el-button v-else type="success" disabled>
                  <el-icon><CircleCheckFilled /></el-icon>
                  已签退
                </el-button>
              </div>
            </div>
            
            <div v-else class="not-checked-in">
              <div class="checkin-prompt">
                <el-icon size="48" color="#409eff"><LocationInformation /></el-icon>
                <p class="prompt-text">还未签到，点击下方按钮开始执勤</p>
                <div v-if="currentSchedule" class="schedule-info">
                  <p class="schedule-title">今日执勤安排：</p>
                  <div class="schedule-details">
                    <el-tag type="primary" class="schedule-tag">
                      {{ formatTimeRange(currentSchedule) }}
                    </el-tag>
                  </div>
                </div>
              </div>
              
              <div class="checkin-button-group">
                <el-button 
                  type="primary" 
                  size="large" 
                  @click="showCheckinDialog = true"
                  :disabled="!canCheckin"
                  class="checkin-btn"
                >
                  <el-icon><LocationFilled /></el-icon>
                  立即签到
                </el-button>
              </div>
            </div>
          </template>
        </div>
      </el-card>
    </div>

    <!-- 我的执勤安排 -->
    <div class="schedule-section">
      <h2 class="section-title">
        <el-icon><Calendar /></el-icon>
        我的执勤安排
      </h2>
      <el-card class="schedule-card modern-card" shadow="never">
        <div v-if="scheduleLoading" class="schedule-loading">
          <SkeletonLoader v-for="i in 3" :key="i" type="list" />
        </div>
        
        <EmptyState 
          v-else-if="mySchedules.length === 0"
          type="no-data"
          title="暂无执勤安排"
          description="联系管理员为您安排执勤时间"
          size="small"
        />
        
        <div v-else class="schedule-list">
          <div 
            v-for="schedule in mySchedules" 
            :key="schedule.id"
            class="schedule-item"
          >
            <div class="schedule-info">
              <div class="schedule-day">
                <el-tag size="large" :type="isToday(schedule.dayOfWeek) ? 'primary' : 'info'">
                  {{ getDayName(schedule.dayOfWeek) }}
                </el-tag>
              </div>
              <div class="schedule-details">
                <div class="time-range">
                  <el-icon><Clock /></el-icon>
                  {{ schedule.startTime }} - {{ schedule.endTime }}
                </div>
                <div v-if="schedule.notes" class="schedule-notes">
                  <el-icon><Document /></el-icon>
                  {{ schedule.notes }}
                </div>
              </div>
            </div>
            <div class="schedule-actions">
              <el-tag :type="schedule.active ? 'success' : 'danger'" size="small">
                {{ schedule.active ? '启用' : '禁用' }}
              </el-tag>
              <el-button
                v-if="schedule.active"
                type="primary"
                size="small"
                round
                class="swap-button"
                @click="openSwapDialog(schedule)"
              >
                <el-icon class="swap-icon"><RefreshRight /></el-icon>
                申请调换
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 排班调换申请 -->
    <div class="swap-section">
      <h2 class="section-title">
        <el-icon><List /></el-icon>
        排班调换申请
      </h2>
      <el-card class="records-card modern-card" shadow="never">
        <div v-if="swapLoading" class="records-loading">
          <SkeletonLoader v-for="i in 2" :key="i" type="list" />
        </div>

        <EmptyState
          v-else-if="swapRequests.length === 0"
          type="no-data"
          title="暂无排班调换申请"
          description="可以在上方执勤安排中选择排班发起调换申请"
          size="small"
        />

        <div v-else class="records-list">
          <div
            v-for="item in swapRequests"
            :key="item.id"
            class="record-item swap-item"
          >
            <div class="record-info">
              <div class="record-header">
                <span class="record-date">
                  {{ getSwapTitle(item) }}
                </span>
                  <el-tag
                    v-if="isCrossWeekSwap(item)"
                    type="danger"
                    effect="plain"
                    size="small"
                  >
                    跨星期
                  </el-tag>
                <el-tag :type="getSwapStatusType(item.status)" size="small">
                  {{ getSwapStatusLabel(item.status) }}
                </el-tag>
              </div>
              <div class="record-details swap-details">
                <span class="detail-item">
                  <strong>我的排班：</strong>
                  {{ formatSwapMySchedule(item) }}
                </span>
                <span class="detail-item">
                  <strong>对方排班：</strong>
                  {{ formatSwapTargetSchedule(item) }}
                </span>
                <span v-if="item.reason" class="detail-item">
                  <strong>申请原因：</strong>
                  {{ item.reason }}
                </span>
                <span v-if="item.responseReason" class="detail-item">
                  <strong>处理备注：</strong>
                  {{ item.responseReason }}
                </span>
              </div>
            </div>
            <div class="swap-actions" v-if="item.status === 'PENDING' && isSwapTarget(item)">
              <el-button
                type="success"
                size="small"
                @click="handleSwapDecision(item, true)"
                :loading="swapSubmitting && currentHandlingSwapId === item.id && lastDecisionApprove === true"
              >
                同意
              </el-button>
              <el-button
                type="danger"
                size="small"
                plain
                @click="handleSwapDecision(item, false)"
                :loading="swapSubmitting && currentHandlingSwapId === item.id && lastDecisionApprove === false"
              >
                拒绝
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 最近执勤记录 -->
    <div class="records-section">
      <h2 class="section-title">
        <el-icon><List /></el-icon>
        最近记录
      </h2>
      <el-card class="records-card modern-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>执勤历史</span>
            <el-button type="primary" link @click="$router.push('/duty/records')">
              查看全部
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>
      
        <div v-if="recordsLoading" class="records-loading">
          <SkeletonLoader v-for="i in 3" :key="i" type="list" />
        </div>
        
        <EmptyState 
          v-else-if="recentRecords.length === 0"
          type="no-data"
          title="暂无执勤记录"
          description="开始你的第一次执勤吧！"
          size="small"
        />
        
        <div v-else class="records-list">
          <div 
            v-for="record in recentRecords" 
            :key="record.id"
            class="record-item"
          >
            <div class="record-info">
              <div class="record-header">
                <span class="record-date">{{ formatDate(record.dutyDate) }}</span>
                <el-tag :type="getStatusType(record.status)" size="small">
                  {{ record.status }}
                </el-tag>
              </div>
              <div class="record-details">
                <span class="detail-item">
                  <el-icon><Clock /></el-icon>
                  {{ record.dutySchedule?.startTime || record.startTime }} -
                  {{ record.dutySchedule?.endTime || record.endTime }}
                </span>
                <span v-if="record.checkinTime" class="detail-item">
                  <el-icon><UserFilled /></el-icon>
                  签到: {{ formatTime(record.checkinTime) }}
                </span>
                <span v-if="record.checkoutTime" class="detail-item">
                  <el-icon><Back /></el-icon>
                  签退: {{ formatTime(record.checkoutTime) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 签到对话框 -->
    <el-dialog
      v-model="showCheckinDialog"
      title="执勤签到"
      width="400px"
      center
    >
      <div class="checkin-form">
        <div class="form-item">
          <label>执勤时段：</label>
          <span class="schedule-time">
            {{ currentSchedule ? formatTimeRange(currentSchedule) : '无安排' }}
          </span>
        </div>
        <div class="form-item">
          <label>备注：</label>
          <el-input
            v-model="checkinForm.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入签到备注（可选）"
            maxlength="200"
            show-word-limit
          />
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showCheckinDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCheckin" :loading="checkinLoading">
          确认签到
        </el-button>
      </template>
    </el-dialog>

    <!-- 签退对话框 -->
    <el-dialog
      v-model="showCheckoutDialog"
      title="执勤签退"
      width="400px"
      center
    >
      <div class="checkout-form">
        <div class="form-item">
          <label>签到时间：</label>
          <span>{{ formatDateTime(todayRecord?.checkinTime) }}</span>
        </div>
        <div class="form-item">
          <label>执勤时长：</label>
          <span>{{ getDutyDuration() }}</span>
        </div>
        <div class="form-item">
          <label>备注：</label>
          <el-input
            v-model="checkoutForm.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入签退备注（可选）"
            maxlength="200"
            show-word-limit
          />
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showCheckoutDialog = false">取消</el-button>
        <el-button type="warning" @click="handleCheckout" :loading="checkoutLoading">
          确认签退
        </el-button>
      </template>
    </el-dialog>

    <!-- 排班调换申请对话框 -->
    <el-dialog
      v-model="showSwapDialog"
      title="申请排班调换"
      width="480px"
      center
    >
      <div class="checkin-form">
        <div class="form-item">
          <label>我的排班：</label>
          <span class="schedule-time">
            {{ selectedSwapSchedule ? formatTimeRange(selectedSwapSchedule) : '未选择' }}
          </span>
        </div>
        <div class="form-item">
          <label>调换日期：</label>
          <el-date-picker
            v-model="swapForm.swapDate"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled-date="disableSwapDate"
            placeholder="请选择调换日期"
            style="width: 100%"
          />
        </div>
        <div class="form-item">
          <label>调换对象：</label>
          <el-select
            v-model="swapForm.targetScheduleId"
            placeholder="请选择要调换的排班"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="schedule in availableSwapTargets"
              :key="schedule.id"
              :label="formatActiveScheduleOption(schedule)"
              :value="schedule.id"
            />
          </el-select>
        </div>
        <div class="form-item">
          <label>申请原因：</label>
          <el-input
            v-model="swapForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入申请原因（建议填写，方便对方判断）"
            maxlength="200"
            show-word-limit
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="showSwapDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSwapRequest" :loading="swapSubmitting">
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  Clock, Upload, CircleCheckFilled, LocationInformation, LocationFilled,
  Calendar, List, ArrowRight, OfficeBuilding, Document, UserFilled, Back, DocumentChecked,
  RefreshRight
} from '@element-plus/icons-vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import EmptyState from '@/components/EmptyState.vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const weekNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
const getDayName = (dayOfWeek) => weekNames[dayOfWeek] || ''

// 响应式数据
const loading = ref(true)
const scheduleLoading = ref(true)
const recordsLoading = ref(true)
const checkinLoading = ref(false)
const checkoutLoading = ref(false)
const showCheckinDialog = ref(false)
const showCheckoutDialog = ref(false)

const todayRecord = ref(null)
const currentSchedule = ref(null)
const mySchedules = ref([])
const recentRecords = ref([])

const checkinForm = reactive({
  notes: ''
})

const checkoutForm = reactive({
  notes: ''
})

// 排班调换相关
const showSwapDialog = ref(false)
const swapSubmitting = ref(false)
const swapLoading = ref(false)
const swapRequests = ref([])
const activeSchedules = ref([])
const selectedSwapSchedule = ref(null)
const currentHandlingSwapId = ref(null)
const lastDecisionApprove = ref(null)

const swapForm = reactive({
  requesterScheduleId: null,
  targetScheduleId: null,
  reason: '',
  swapDate: null
})

// 计算属性
const canCheckin = computed(() => {
  return currentSchedule.value && !todayRecord.value?.checkinTime
})

const canCheckout = computed(() => {
  return todayRecord.value?.checkinTime && !todayRecord.value?.checkoutTime
})

// 当前选择的调换日期对应的星期几（1-7，周一到周日）
const selectedSwapDayOfWeek = computed(() => {
  if (!swapForm.swapDate) return null
  const date = new Date(swapForm.swapDate)
  if (Number.isNaN(date.getTime())) return null
  const jsDay = date.getDay() // 0-6，0=周日
  return jsDay === 0 ? 7 : jsDay
})

// 可选的调换目标排班：当前选择日期的所有执勤人员（排除自己、排除同一条排班）
const availableSwapTargets = computed(() => {
  if (!swapForm.requesterScheduleId || !selectedSwapDayOfWeek.value) return []
  const currentUserId = userStore.userInfo?.id
  const targetDayOfWeek = selectedSwapDayOfWeek.value
  return activeSchedules.value.filter(item => {
    return (
      item.id !== swapForm.requesterScheduleId &&
      item.user &&
      item.user.id !== currentUserId &&
      item.active &&
      item.dayOfWeek === targetDayOfWeek
    )
  })
})

const disableSwapDate = (date) => {
  const current = new Date(date)
  current.setHours(0, 0, 0, 0)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return current < today
}

const todaySwapInfo = computed(() => {
  const todayStr = new Date().toISOString().split('T')[0]
  return swapRequests.value.find(item => item.status === 'APPROVED' && item.swapDate === todayStr)
})

const todaySwapPeerName = computed(() => {
  const swap = todaySwapInfo.value
  const currentUserId = userStore.userInfo?.id
  if (!swap || !currentUserId) return ''
  if (swap.requester?.id === currentUserId) {
    return swap.targetUser?.realName || '对方'
  }
  if (swap.targetUser?.id === currentUserId) {
    return swap.requester?.realName || '对方'
  }
  return ''
})

const todaySwapScheduleText = computed(() => {
  const swap = todaySwapInfo.value
  const currentUserId = userStore.userInfo?.id
  if (!swap || !currentUserId) return ''
  let schedule = null
  if (swap.requester?.id === currentUserId) {
    schedule = swap.targetSchedule
  } else if (swap.targetUser?.id === currentUserId) {
    schedule = swap.requesterSchedule
  }
  if (!schedule) return ''
  return `${getDayName(schedule.dayOfWeek)} ${schedule.startTime} - ${schedule.endTime}`
})

const todaySwapBannerText = computed(() => {
  if (!todaySwapInfo.value || !todaySwapPeerName.value) return ''
  const currentUserId = userStore.userInfo?.id
  const isRequester = todaySwapInfo.value.requester?.id === currentUserId
  return isRequester
    ? `今日排班来自与 ${todaySwapPeerName.value} 的调换`
    : `${todaySwapPeerName.value} 与你完成了今日排班调换`
})

const isCrossWeekSwap = (item) => {
  const requesterDay = item.requesterSchedule?.dayOfWeek
  const targetDay = item.targetSchedule?.dayOfWeek
  return requesterDay !== undefined && targetDay !== undefined && requesterDay !== targetDay
}

// 获取今日执勤记录
const fetchTodayRecord = async () => {
  try {
    const response = await request.get('/duty/records/today')
    todayRecord.value = response.data
      console.log('今日执勤记录:', todayRecord.value)
      if (todayRecord.value) {
        console.log('执勤记录状态:', todayRecord.value.status)
        console.log('签到时间:', todayRecord.value.checkinTime)
        console.log('备注原文:', todayRecord.value.notes)
        console.log('解析的审核人:', getApproverName(todayRecord.value))
        console.log('解析的请假原因:', getLeaveReason(todayRecord.value))
      }
  } catch (error) {
    console.error('获取今日执勤记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取当前执勤安排
const fetchCurrentSchedule = async () => {
  try {
    const response = await request.get('/duty/schedules/current')
    currentSchedule.value = response.data
  } catch (error) {
    console.error('获取当前执勤安排失败:', error)
  }
}

// 获取我的执勤安排
const fetchMySchedules = async () => {
  try {
    const response = await request.get('/duty/schedules/my')
    mySchedules.value = response.data || []
  } catch (error) {
    ElMessage.error('获取执勤安排失败')
  } finally {
    scheduleLoading.value = false
  }
}

// 获取所有启用排班（用于调换候选）
const fetchActiveSchedules = async () => {
  try {
    const response = await request.get('/duty/schedules/active')
    activeSchedules.value = response.data || []
  } catch (error) {
    console.error('获取启用排班失败:', error)
  }
}

// 获取与我相关的排班调换申请
const fetchMySwapRequests = async () => {
  swapLoading.value = true
  try {
    const response = await request.get('/duty/swap-requests/my')
    swapRequests.value = response.data || []
  } catch (error) {
    console.error('获取排班调换申请失败:', error)
    ElMessage.error('获取排班调换申请失败')
  } finally {
    swapLoading.value = false
  }
}

// 获取最近执勤记录
const fetchRecentRecords = async () => {
  try {
    const response = await request.get('/duty/records/my', {
      params: { size: 5 }
    })
    recentRecords.value = response.data?.content || response.data || []
  } catch (error) {
    ElMessage.error('获取执勤记录失败')
  } finally {
    recordsLoading.value = false
  }
}

// 执勤签到
const handleCheckin = async () => {
  try {
    checkinLoading.value = true
    const response = await request.post('/duty/checkin', checkinForm)
    
    ElMessage.success('签到成功！')
    showCheckinDialog.value = false
    checkinForm.notes = ''
    
    // 刷新数据
    await fetchTodayRecord()
    await fetchRecentRecords()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '签到失败')
  } finally {
    checkinLoading.value = false
  }
}

// 执勤签退
const handleCheckout = async () => {
  try {
    checkoutLoading.value = true
    const response = await request.post('/duty/checkout', checkoutForm)
    
    ElMessage.success('签退成功！')
    showCheckoutDialog.value = false
    checkoutForm.notes = ''
    
    // 刷新数据
    await fetchTodayRecord()
    await fetchRecentRecords()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '签退失败')
  } finally {
    checkoutLoading.value = false
  }
}

// 打开排班调换对话框
const openSwapDialog = (schedule) => {
  selectedSwapSchedule.value = schedule
  swapForm.requesterScheduleId = schedule.id
  swapForm.targetScheduleId = null
  swapForm.reason = ''
  swapForm.swapDate = null
  showSwapDialog.value = true
}

// 提交排班调换申请
const handleCreateSwapRequest = async () => {
  if (!swapForm.requesterScheduleId || !swapForm.targetScheduleId) {
    ElMessage.warning('请选择要调换的排班')
    return
  }
  if (!swapForm.swapDate) {
    ElMessage.warning('请选择调换日期')
    return
  }
  try {
    swapSubmitting.value = true
    await request.post('/duty/swap-requests', {
      requesterScheduleId: swapForm.requesterScheduleId,
      targetScheduleId: swapForm.targetScheduleId,
      reason: swapForm.reason,
      // 传到后端的 LocalDate，使用 ISO 字符串
      swapDate: swapForm.swapDate
    })
    ElMessage.success('排班调换申请已提交')
    showSwapDialog.value = false
    await fetchMySwapRequests()
  } catch (error) {
    console.error('提交排班调换申请失败:', error)
    ElMessage.error(error.response?.data?.message || '提交排班调换申请失败')
  } finally {
    swapSubmitting.value = false
  }
}

// 判断当前用户是否为调换发起人
const isSwapRequester = (item) => {
  const currentUserId = userStore.userInfo?.id
  return item.requester && item.requester.id === currentUserId
}

// 判断当前用户是否为被调换人
const isSwapTarget = (item) => {
  const currentUserId = userStore.userInfo?.id
  return item.targetUser && item.targetUser.id === currentUserId
}

// 标题：包含申请人姓名
const getSwapTitle = (item) => {
  const requesterName = item.requester?.realName || '未知申请人'
  const targetName = item.targetUser?.realName || '未知用户'
  if (isSwapRequester(item)) {
    return `我向 ${targetName} 申请调换`
  }
  return `${requesterName} 向我申请调换`
}

// 调换状态显示类型
const getSwapStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

// 调换状态中文标签
const getSwapStatusLabel = (status) => {
  const map = {
    PENDING: '待处理',
    APPROVED: '已同意',
    REJECTED: '已拒绝',
    CANCELLED: '已撤销'
  }
  return map[status] || status
}

// 格式化“我的排班”在调换记录中的显示
const formatSwapMySchedule = (item) => {
  const currentUserId = userStore.userInfo?.id
  const isRequester = item.requester && item.requester.id === currentUserId
  const schedule = isRequester ? item.requesterSchedule : item.targetSchedule
  if (!schedule) return '未知排班'
  return `${getDayName(schedule.dayOfWeek)} ${schedule.startTime} - ${schedule.endTime}`
}

// 格式化“对方排班”在调换记录中的显示
const formatSwapTargetSchedule = (item) => {
  const currentUserId = userStore.userInfo?.id
  const isRequester = item.requester && item.requester.id === currentUserId
  const schedule = isRequester ? item.targetSchedule : item.requesterSchedule
  if (!schedule) return '未知排班'
  const userName = schedule.user?.realName || '未知用户'
  return `${userName} · ${getDayName(schedule.dayOfWeek)} ${schedule.startTime} - ${schedule.endTime}`
}

// 格式化启用排班选项
const formatActiveScheduleOption = (schedule) => {
  const userName = schedule.user?.realName || '未知用户'
  const departmentName = schedule.user?.department?.name || '无部门'
  return `${userName}（${departmentName}） - ${getDayName(schedule.dayOfWeek)} ${schedule.startTime} - ${schedule.endTime}`
}

// 处理调换申请（同意 / 拒绝）
const handleSwapDecision = async (item, approve) => {
  try {
    currentHandlingSwapId.value = item.id
    lastDecisionApprove.value = approve
    swapSubmitting.value = true
    await request.post(`/duty/swap-requests/${item.id}/decision`, {
      approve,
      reason: ''
    })
    ElMessage.success(approve ? '已同意该调换申请' : '已拒绝该调换申请')
    await fetchMySchedules()
    await fetchActiveSchedules()
    await fetchMySwapRequests()
  } catch (error) {
    console.error('处理排班调换申请失败:', error)
    ElMessage.error(error.response?.data?.message || '处理排班调换申请失败')
  } finally {
    swapSubmitting.value = false
    currentHandlingSwapId.value = null
    lastDecisionApprove.value = null
  }
}

// 工具函数
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString('zh-CN')
}

const formatDate = (date) => {
  if (!date) return ''
  const dateObj = new Date(date)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekday = weekdays[dateObj.getDay()]
  const dateString = dateObj.toLocaleDateString('zh-CN')
  return `${dateString} ${weekday}`
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const formatTimeRange = (schedule) => {
  if (!schedule || !schedule.startTime || !schedule.endTime) {
    return '时间待定'
  }
  return `${schedule.startTime} - ${schedule.endTime}`
}

const isToday = (dayOfWeek) => {
  const today = new Date().getDay()
  const adjustedToday = today === 0 ? 7 : today // 调整周日为7
  return dayOfWeek === adjustedToday
}

const getStatusType = (status) => {
  const statusTypes = {
    '待执勤': 'info',
    '执勤中': 'warning',
    '已完成': 'success',
    '缺勤': 'danger'
  }
  return statusTypes[status] || 'info'
}

const getDutyDuration = () => {
  if (!todayRecord.value?.checkinTime) return '0分钟'
  
  const checkinTime = new Date(todayRecord.value.checkinTime)
  const now = new Date()
  const diffMs = now - checkinTime
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  return diffHours > 0 ? `${diffHours}小时${diffMinutes}分钟` : `${diffMinutes}分钟`
}

// 获取审核人姓名
const getApproverName = (record) => {
  if (!record || !record.notes) return '系统'
  
  // 从备注中提取审核人信息，格式：请假申请已批准 - 原因 | 审核人：姓名
  const notes = record.notes
  if (notes.includes('| 审核人：')) {
    const approverPart = notes.split('| 审核人：')[1]
    return approverPart || '系统'
  }
  return '系统'
}

// 获取请假原因
const getLeaveReason = (record) => {
  if (!record || !record.notes) return '暂无备注'
  
  // 从备注中提取请假原因，格式：请假申请已批准 - 原因 | 审核人：姓名
  const notes = record.notes
  if (notes.includes('请假申请已批准 - ')) {
    let reasonPart = notes.split('请假申请已批准 - ')[1]
    if (reasonPart && reasonPart.includes(' | 审核人：')) {
      reasonPart = reasonPart.split(' | 审核人：')[0]
    }
    return reasonPart || '暂无备注'
  }
  return notes
}

// 获取审核时间
const getApproveTime = (record) => {
  if (!record || !record.updatedAt) return '未知'
  
  // 格式化审核时间
  const date = new Date(record.updatedAt)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  fetchTodayRecord()
  fetchCurrentSchedule()
  fetchMySchedules()
  fetchRecentRecords()
  fetchActiveSchedules()
  fetchMySwapRequests()
})
</script>

<style scoped>
.duty-checkin-container {
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  position: relative;
}

.duty-checkin-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 119, 198, 0.15) 0%, transparent 50%);
  pointer-events: none;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  position: relative;
  z-index: 1;
}

.header-content {
  position: relative;
  display: inline-block;
}

.title-section {
  position: relative;
  z-index: 2;
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.title-icon {
  font-size: 40px;
  background: linear-gradient(135deg, #ffd89b 0%, #19547b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
  font-weight: 300;
  letter-spacing: 0.5px;
}

.header-decoration {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 120px;
  height: 120px;
  top: -60px;
  left: -200px;
  animation-delay: 0s;
}

.circle-2 {
  width: 80px;
  height: 80px;
  top: -40px;
  right: -180px;
  animation-delay: 2s;
}

.circle-3 {
  width: 60px;
  height: 60px;
  bottom: -30px;
  left: -150px;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

/* 现代化卡片样式 */
.status-section,
.schedule-section,
.records-section {
  margin-bottom: 32px;
  position: relative;
  z-index: 1;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 22px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 20px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.modern-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    0 2px 16px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: all 0.3s ease;
}

.modern-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 16px 48px rgba(0, 0, 0, 0.15),
    0 4px 24px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.status-content {
  padding: 20px 0;
}

/* 调换提示 */
.swap-banner {
  background: #fdf6ec;
  border: 1px solid #f5c97a;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  color: #8c5a13;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.swap-text {
  font-size: 14px;
  font-weight: 500;
}

.swap-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 6px;
  font-size: 13px;
}

/* 请假状态样式 */
.leave-approved-status {
  text-align: center;
  padding: 20px;
}

.leave-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.leave-icon {
  margin-bottom: 16px;
}

.leave-message h3 {
  font-size: 20px;
  color: #67c23a;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.leave-text {
  font-size: 16px;
  color: #909399;
  margin: 0;
}

.leave-details {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  text-align: left;
}

.leave-details .info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.leave-details .info-row:last-child {
  margin-bottom: 0;
}

.leave-details .info-label {
  color: #303133;
  font-weight: 600;
  flex: 1;
}

.leave-details .info-extra {
  color: #606266;
  font-size: 14px;
  font-weight: 400;
  text-align: right;
}

.checked-in-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.checkin-info {
  flex: 1;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  color: #909399;
  margin-right: 8px;
  min-width: 80px;
}

.value {
  color: #303133;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.not-checked-in {
  text-align: center;
}

.checkin-prompt {
  margin-bottom: 24px;
}

.prompt-text {
  font-size: 16px;
  color: #606266;
  margin: 16px 0;
}

.schedule-info {
  margin-top: 20px;
}

.schedule-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.schedule-details {
  display: flex;
  justify-content: center;
}

.schedule-tag {
  margin: 0;
}

.checkin-button-group {
  display: flex;
  justify-content: center;
}

.checkin-btn {
  padding: 12px 32px;
  font-size: 16px;
  border-radius: 8px;
}

/* 执勤安排列表 */
.schedule-list {
  padding: 0;
}

.schedule-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.schedule-item:last-child {
  border-bottom: none;
}

.schedule-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
}

.schedule-day .el-tag {
  font-weight: 600;
}

.schedule-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-range,
.schedule-notes {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
}

.schedule-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.swap-button {
  background: linear-gradient(135deg, #5c8dff 0%, #7b5cfc 100%) !important;
  border: none !important;
  color: #fff;
  padding: 6px 14px;
  box-shadow: 0 8px 16px rgba(92, 141, 255, 0.25);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.swap-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 20px rgba(92, 141, 255, 0.35);
}

.swap-button:focus-visible {
  outline: 2px solid rgba(92, 141, 255, 0.5);
  outline-offset: 2px;
}

.swap-icon {
  margin-right: 4px;
}

/* 记录列表 */
.records-list {
  padding: 0;
}

.record-item {
  padding: 20px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  border-radius: 12px;
  margin: 0 -20px;
  padding-left: 20px;
  padding-right: 20px;
}

.record-item:last-child {
  border-bottom: none;
}

.record-item:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  transform: translateX(4px);
}

.record-info {
  flex: 1;
}

.record-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.record-date {
  font-weight: 600;
  color: #303133;
}

.record-details {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

/* 对话框样式 */
.checkin-form,
.checkout-form {
  padding: 16px 0;
}

.form-item {
  margin-bottom: 16px;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #303133;
}

.schedule-time {
  color: #409eff;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .duty-checkin-container {
    padding: 16px;
  }
  
  .page-title {
    font-size: 28px;
  }
  
  .page-subtitle {
    font-size: 16px;
  }
  
  .header-decoration {
    display: none;
  }
  
  .checked-in-status {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .action-buttons {
    flex-direction: row;
    justify-content: center;
  }
  
  .schedule-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .schedule-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .record-details {
    gap: 12px;
  }
  
  .detail-item {
    font-size: 12px;
  }
}

/* 清新玻璃化统一样式 */
.duty-checkin-container {
  width: min(100%, 1480px);
  margin: 0 auto;
  padding: 24px 28px 48px;
  background:
    radial-gradient(circle at 92% 6%, rgba(75, 211, 180, 0.12), transparent 32%),
    radial-gradient(circle at 8% 4%, rgba(24, 185, 236, 0.1), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.62), rgba(240, 251, 255, 0.48));
  border-radius: 0;
  color: #123f55;
  min-height: auto;
}

.duty-checkin-container::before {
  display: none;
}

.page-header {
  margin-bottom: 22px;
  text-align: left;
}

.header-content {
  display: block;
  padding: 26px 28px;
  border: 1px solid rgba(14, 165, 233, 0.14);
  border-radius: 22px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(236, 254, 255, 0.72)),
    radial-gradient(circle at 88% 0%, rgba(75, 211, 180, 0.16), transparent 34%);
  box-shadow: 0 18px 44px rgba(14, 116, 144, 0.08);
  backdrop-filter: blur(18px);
}

.header-decoration {
  display: none;
}

.page-title {
  justify-content: flex-start;
  margin: 0 0 8px;
  color: #0c4a6e;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 0;
  text-shadow: none;
}

.title-icon {
  color: #0891b2;
  background: none;
  -webkit-text-fill-color: currentColor;
}

.page-subtitle {
  color: #4b7186;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0;
}

.status-section,
.schedule-section,
.swap-section,
.records-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 14px;
  color: #0c4a6e;
  font-size: 20px;
  font-weight: 800;
  text-shadow: none;
}

.section-title .el-icon {
  color: #0891b2;
}

.modern-card {
  border: 1px solid rgba(14, 165, 233, 0.14);
  border-radius: 22px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.93), rgba(248, 253, 255, 0.84)),
    radial-gradient(circle at 96% 0%, rgba(75, 211, 180, 0.1), transparent 34%);
  box-shadow: 0 18px 42px rgba(14, 116, 144, 0.08);
  backdrop-filter: blur(18px);
}

.modern-card:hover {
  transform: none;
  box-shadow: 0 20px 48px rgba(14, 116, 144, 0.11);
}

.modern-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(14, 165, 233, 0.1);
  background: linear-gradient(135deg, rgba(236, 254, 255, 0.68), rgba(255, 255, 255, 0.52));
}

.modern-card :deep(.el-card__body) {
  padding: 20px;
}

.card-header {
  color: #0f4f62;
  font-weight: 800;
}

.card-header .el-icon {
  color: #0891b2;
}

.status-content {
  padding: 18px 0;
}

.not-checked-in,
.leave-approved-status,
.checked-in-status {
  min-height: 170px;
}

.checkin-prompt {
  display: grid;
  justify-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.checkin-prompt > .el-icon {
  padding: 12px;
  border-radius: 18px;
  background: #ecfeff;
  color: #0891b2 !important;
}

.prompt-text,
.schedule-title,
.detail-item,
.time-range,
.schedule-notes,
.label,
.leave-text {
  color: #4b7186;
}

.value,
.record-date,
.leave-details .info-label {
  color: #123f55;
}

.schedule-tag,
.schedule-day :deep(.el-tag),
.card-header :deep(.el-tag),
.record-header :deep(.el-tag),
.schedule-actions :deep(.el-tag) {
  border-radius: 999px;
  font-weight: 700;
}

.checkin-btn,
.swap-button,
.action-buttons .el-button,
.swap-actions .el-button {
  border-radius: 999px;
  font-weight: 750;
  cursor: pointer;
}

.checkin-btn,
.swap-button,
.action-buttons :deep(.el-button--primary),
.swap-actions :deep(.el-button--success) {
  border: none !important;
  background: linear-gradient(135deg, #67e8f9, #7dd3fc) !important;
  color: #075985 !important;
  box-shadow: 0 10px 24px rgba(14, 165, 233, 0.2);
}

.action-buttons :deep(.el-button--warning) {
  border: none !important;
  background: linear-gradient(135deg, #fde68a, #fef3c7) !important;
  color: #92400e !important;
  box-shadow: 0 10px 24px rgba(245, 158, 11, 0.18);
}

.swap-actions :deep(.el-button--danger) {
  border-color: rgba(244, 63, 94, 0.24);
  background: #fff1f2;
  color: #be123c;
}

.checkin-btn:hover,
.swap-button:hover,
.action-buttons .el-button:hover,
.swap-actions .el-button:hover {
  filter: brightness(1.02);
  transform: translateY(-1px);
}

.swap-banner {
  border: 1px solid rgba(245, 158, 11, 0.22);
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255, 251, 235, 0.94), rgba(255, 247, 237, 0.76));
  color: #92400e;
}

.leave-details {
  border: 1px solid rgba(34, 197, 94, 0.14);
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(240, 253, 244, 0.88), rgba(255, 255, 255, 0.78));
}

.leave-message h3 {
  color: #047857;
}

.schedule-list,
.records-list {
  display: grid;
  gap: 12px;
}

.schedule-item,
.record-item {
  margin: 0;
  padding: 16px;
  border: 1px solid rgba(14, 165, 233, 0.1);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.68);
}

.schedule-item:hover,
.record-item:hover {
  background: #ecfeff;
  transform: none;
  border-color: rgba(14, 165, 233, 0.22);
}

.schedule-info {
  margin-top: 0;
}

.record-header {
  gap: 12px;
}

.record-details {
  gap: 10px 16px;
}

.detail-item {
  padding: 5px 9px;
  border-radius: 999px;
  background: rgba(240, 249, 255, 0.72);
}

.swap-details .detail-item {
  border-radius: 12px;
  line-height: 1.5;
}

.schedule-actions,
.swap-actions {
  flex-wrap: wrap;
}

.records-card .card-header {
  justify-content: space-between;
}

.records-card .card-header :deep(.el-button) {
  color: #0284c7;
  font-weight: 750;
}

.checkin-form,
.checkout-form {
  padding: 4px 0;
}

.form-item label {
  color: #0f4f62;
  font-weight: 750;
}

.schedule-time {
  color: #0284c7;
  font-weight: 750;
}

:deep(.el-dialog) {
  border-radius: 22px;
  background:
    linear-gradient(135deg, rgba(248, 253, 255, 0.98), rgba(255, 255, 255, 0.96)),
    radial-gradient(circle at top right, rgba(125, 211, 252, 0.18), transparent 36%);
  box-shadow: 0 28px 72px rgba(8, 47, 73, 0.18);
  overflow: hidden;
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 20px 24px 14px;
  border-bottom: 1px solid rgba(14, 165, 233, 0.12);
}

:deep(.el-dialog__title) {
  color: #0c4a6e;
  font-weight: 800;
}

:deep(.el-dialog__body) {
  padding: 20px 24px 8px;
}

:deep(.el-dialog__footer) {
  padding: 14px 24px 20px;
  border-top: 1px solid rgba(14, 165, 233, 0.1);
  background: rgba(240, 249, 255, 0.5);
}

:deep(.el-dialog__footer .el-button) {
  min-width: 92px;
  border-radius: 999px;
  font-weight: 750;
}

:deep(.el-dialog__footer .el-button--primary) {
  border: none;
  background: linear-gradient(135deg, #67e8f9, #7dd3fc);
  color: #075985;
  box-shadow: 0 10px 24px rgba(14, 165, 233, 0.18);
}

:deep(.el-dialog__footer .el-button--warning) {
  border: none;
  background: linear-gradient(135deg, #fde68a, #fef3c7);
  color: #92400e;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 0 0 1px rgba(14, 165, 233, 0.14) inset;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus),
:deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px #38bdf8 inset, 0 0 0 4px rgba(56, 189, 248, 0.14);
}

@media (max-width: 768px) {
  .duty-checkin-container {
    width: 100%;
    padding: 14px;
  }

  .header-content {
    padding: 20px;
    border-radius: 18px;
  }

  .page-title {
    font-size: 24px;
  }

  .page-subtitle {
    font-size: 14px;
  }

  .section-title {
    font-size: 18px;
  }

  .modern-card :deep(.el-card__body) {
    padding: 14px;
  }

  .not-checked-in,
  .leave-approved-status,
  .checked-in-status {
    min-height: auto;
  }

  .schedule-item,
  .record-item {
    padding: 14px;
  }

  .schedule-actions,
  .swap-actions,
  .action-buttons {
    width: 100%;
  }

  .schedule-actions .el-button,
  .swap-actions .el-button,
  .action-buttons .el-button {
    flex: 1;
  }

  .record-header {
    align-items: flex-start;
    flex-direction: column;
  }

  :deep(.el-dialog) {
    width: 94vw !important;
  }
}
</style>
