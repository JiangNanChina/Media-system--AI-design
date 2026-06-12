<template>
  <div class="duty-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon><OfficeBuilding /></el-icon>
            办公执勤管理
          </h1>
          <p class="page-description">管理办公室值班执勤安排</p>
        </div>
        <div class="header-right">
          <el-button
            type="primary"
            class="header-action-btn"
            @click="showScheduleDialog = true"
            :icon="Plus"
          >
            添加排班
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card stat-card--total">
          <SkeletonLoader v-if="statsLoading" type="stats" />
          <div v-else class="stat-content">
            <div class="stat-value">{{ stats.totalSchedules || 0 }}</div>
            <div class="stat-label">总排班数</div>
            <div class="stat-icon stat-icon--total">
              <el-icon><Calendar /></el-icon>
            </div>
          </div>
        </div>
        
        <div class="stat-card stat-card--active">
          <SkeletonLoader v-if="statsLoading" type="stats" />
          <div v-else class="stat-content">
            <div class="stat-value">{{ stats.activeSchedules || 0 }}</div>
            <div class="stat-label">启用排班</div>
            <div class="stat-icon stat-icon--active">
              <el-icon><CircleCheck /></el-icon>
            </div>
          </div>
        </div>
        
        <div class="stat-card stat-card--today">
          <SkeletonLoader v-if="statsLoading" type="stats" />
          <div v-else class="stat-content">
            <div class="stat-value">{{ stats.todayDuties || 0 }}</div>
            <div class="stat-label">今日执勤</div>
            <div class="stat-icon stat-icon--today">
              <el-icon><Clock /></el-icon>
            </div>
          </div>
        </div>
        
        <div class="stat-card stat-card--completed">
          <SkeletonLoader v-if="statsLoading" type="stats" />
          <div v-else class="stat-content">
            <div class="stat-value">{{ stats.completedDuties || 0 }}</div>
            <div class="stat-label">已完成</div>
            <div class="stat-icon stat-icon--completed">
              <el-icon><Check /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能选项卡 -->
    <div class="tabs-section">
      <el-card shadow="never">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <!-- 排班管理 -->
          <el-tab-pane label="排班管理" name="schedule">
            <div class="schedule-section">
              <!-- 搜索区域 -->
              <el-card class="search-card">
                <el-row :gutter="16" class="search-row">
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-input
                      v-model="scheduleSearch.keyword"
                      placeholder="搜索用户姓名"
                      clearable
                      class="search-input"
                    >
                      <template #prefix>
                        <el-icon><Search /></el-icon>
                      </template>
                    </el-input>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-select
                      v-model="scheduleSearch.dayOfWeek"
                      placeholder="选择星期"
                      clearable
                      class="search-select"
                    >
                      <el-option label="周一" :value="1" />
                      <el-option label="周二" :value="2" />
                      <el-option label="周三" :value="3" />
                      <el-option label="周四" :value="4" />
                      <el-option label="周五" :value="5" />
                      <el-option label="周六" :value="6" />
                      <el-option label="周日" :value="7" />
                    </el-select>
                  </el-col>
                  <el-col :xs="24" :sm="24" :md="8" :lg="12">
                    <div class="search-actions">
                      <el-button
                        type="primary"
                        class="toolbar-action toolbar-action--primary"
                        @click="fetchSchedules"
                        :icon="Search"
                      >
                        <span v-if="!isMobile">搜索</span>
                      </el-button>
                      <el-button
                        class="toolbar-action toolbar-action--ghost"
                        @click="resetScheduleSearch"
                        :icon="Refresh"
                      >
                        <span v-if="!isMobile">重置</span>
                      </el-button>
                    </div>
                  </el-col>
                </el-row>
              </el-card>

              <!-- 排班列表 -->
              <div class="schedules-list">
                <SkeletonLoader v-if="scheduleLoading" type="table" :rows="5" />
                <EmptyState v-else-if="schedules.length === 0" type="no-data" description="暂无排班安排" />
                
                <!-- 桌面端表格 -->
                <el-table v-if="!isMobile && schedules.length > 0" :data="schedules" stripe>
                  <el-table-column prop="user.realName" label="执勤人员" width="120" />
                  <el-table-column prop="user.department.name" label="所属部门" width="150" />
                  <el-table-column label="执勤日期" width="100">
                    <template #default="{ row }">
                      <el-tag size="small">{{ getDayName(row.dayOfWeek) }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="执勤时间" width="200">
                    <template #default="{ row }">
                      <div class="time-range">
                        {{ row.startTime }} - {{ row.endTime }}
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="notes" label="备注" show-overflow-tooltip />
                  <el-table-column prop="active" label="状态" width="80">
                    <template #default="{ row }">
                      <el-tag :type="row.active ? 'success' : 'danger'" size="small">
                        {{ row.active ? '启用' : '禁用' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="260" fixed="right">
                    <template #default="{ row }">
                      <div class="table-actions">
                        <el-button
                          type="primary"
                          class="action-btn action-btn--edit"
                          size="small"
                          @click="editSchedule(row)"
                          :icon="Edit"
                        >
                          编辑
                        </el-button>
                        <el-button
                          type="info"
                          class="action-btn action-btn--swap"
                          size="small"
                          @click="openAdminSwapDialog(row)"
                          :disabled="!row.active"
                          :icon="RefreshRight"
                        >
                          调换
                        </el-button>
                        <el-button
                          :type="row.active ? 'warning' : 'success'"
                          :class="['action-btn', row.active ? 'action-btn--warning' : 'action-btn--success']"
                          size="small"
                          @click="toggleScheduleStatus(row)"
                          :icon="row.active ? SwitchButton : CircleCheck"
                        >
                          {{ row.active ? '禁用' : '启用' }}
                        </el-button>
                        <el-button
                          type="danger"
                          class="action-btn action-btn--delete"
                          size="small"
                          @click="deleteSchedule(row)"
                          :icon="Delete"
                        >
                          删除
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 移动端卡片布局 -->
                <div v-if="isMobile && schedules.length > 0" class="mobile-cards">
                  <div v-for="schedule in schedules" :key="schedule.id" class="mobile-card">
                    <div class="card-header">
                      <div class="card-title">
                        <span class="user-name">{{ schedule.user?.realName }}</span>
                        <div class="card-badges">
                          <el-tag :type="schedule.active ? 'success' : 'danger'" size="small">
                            {{ schedule.active ? '启用' : '禁用' }}
                          </el-tag>
                          <el-tag size="small">{{ getDayName(schedule.dayOfWeek) }}</el-tag>
                        </div>
                      </div>
                    </div>
                    
                    <div class="card-content">
                      <div class="card-info">
                        <div class="info-item">
                          <span class="info-label">所属部门:</span>
                          <span class="info-value">{{ schedule.user?.department?.name || '无部门' }}</span>
                        </div>
                        <div class="info-item">
                          <span class="info-label">执勤时间:</span>
                          <span class="info-value time-range">{{ schedule.startTime }} - {{ schedule.endTime }}</span>
                        </div>
                        <div class="info-item" v-if="schedule.notes">
                          <span class="info-label">备注:</span>
                          <span class="info-value">{{ schedule.notes }}</span>
                        </div>
                      </div>
                    </div>
                    
                    <div class="card-actions">
                      <el-button class="action-btn action-btn--edit" type="primary" size="small" @click="editSchedule(schedule)">
                        <el-icon><Edit /></el-icon>
                        编辑
                      </el-button>
                      <el-button
                        type="info"
                        class="action-btn action-btn--swap"
                        size="small"
                        @click="openAdminSwapDialog(schedule)"
                        :disabled="!schedule.active"
                      >
                        <el-icon><RefreshRight /></el-icon>
                        调换
                      </el-button>
                      <el-button 
                        :type="schedule.active ? 'warning' : 'success'" 
                        :class="['action-btn', schedule.active ? 'action-btn--warning' : 'action-btn--success']"
                        size="small" 
                        @click="toggleScheduleStatus(schedule)"
                      >
                        <el-icon>
                          <SwitchButton v-if="schedule.active" />
                          <CircleCheck v-else />
                        </el-icon>
                        {{ schedule.active ? '禁用' : '启用' }}
                      </el-button>
                      <el-button class="action-btn action-btn--delete" type="danger" size="small" @click="deleteSchedule(schedule)">
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 执勤记录 -->
          <el-tab-pane label="执勤记录" name="records">
            <div class="records-section">
              <!-- 搜索区域 -->
              <el-card class="search-card">
                <el-row :gutter="16" class="search-row">
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-input
                      v-model="recordSearch.keyword"
                      placeholder="搜索用户姓名"
                      clearable
                      class="search-input"
                    >
                      <template #prefix>
                        <el-icon><Search /></el-icon>
                      </template>
                    </el-input>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-date-picker
                      v-model="recordSearch.dateRange"
                      type="daterange"
                      range-separator="至"
                      start-placeholder="开始日期"
                      end-placeholder="结束日期"
                      class="search-date-picker"
                    />
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-select
                      v-model="recordSearch.status"
                      placeholder="执勤状态"
                      clearable
                      class="search-select"
                    >
                      <el-option label="待执勤" value="待执勤" />
                      <el-option label="执勤中" value="执勤中" />
                      <el-option label="已完成" value="已完成" />
                      <el-option label="已请假" value="已请假" />
                      <el-option label="缺勤" value="缺勤" />
                    </el-select>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <div class="search-actions">
                      <el-button
                        type="primary"
                        class="toolbar-action toolbar-action--primary"
                        @click="fetchRecords"
                        :icon="Search"
                      >
                        <span v-if="!isMobile">搜索</span>
                      </el-button>
                      <el-button
                        class="toolbar-action toolbar-action--ghost"
                        @click="resetRecordSearch"
                        :icon="Refresh"
                      >
                        <span v-if="!isMobile">重置</span>
                      </el-button>
                    </div>
                  </el-col>
                </el-row>
              </el-card>

              <!-- 记录列表 -->
              <div class="records-list">
                <SkeletonLoader v-if="recordLoading" type="table" :rows="5" />
                <EmptyState v-else-if="records.length === 0" type="no-data" description="暂无执勤记录" />
                
                <!-- 桌面端表格 -->
                <el-table v-if="!isMobile && records.length > 0" :data="records" stripe>
                  <el-table-column label="执勤人员" width="120">
                    <template #default="{ row }">
                      {{ row.userRealName || row.user?.realName || '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="所属部门" width="150">
                    <template #default="{ row }">
                      {{ row.departmentName || row.user?.department?.name || '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="dutyDate" label="执勤日期" width="120">
                    <template #default="{ row }">
                      {{ formatDate(row.dutyDate) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="计划时间" width="200">
                    <template #default="{ row }">
                      <div class="time-range">
                        {{ row.startTime || row.dutySchedule?.startTime || '-' }} -
                        {{ row.endTime || row.dutySchedule?.endTime || '-' }}
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column label="签到时间" width="120">
                    <template #default="{ row }">
                      <span v-if="row.checkinTime">{{ formatTime(row.checkinTime) }}</span>
                      <el-tag v-else type="info" size="small">未签到</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="签退时间" width="120">
                    <template #default="{ row }">
                      <span v-if="row.checkoutTime">{{ formatTime(row.checkoutTime) }}</span>
                      <el-tag v-else type="warning" size="small">未签退</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getStatusType(row.status)" size="small">
                        {{ row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="notes" label="备注" show-overflow-tooltip />
                  <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                      <div class="table-actions table-actions--compact">
                        <el-button
                          type="primary"
                          class="action-btn action-btn--detail"
                          size="small"
                          @click="viewRecordDetail(row)"
                          :icon="View"
                        >
                          详情
                        </el-button>
                        <el-button
                          type="danger"
                          class="action-btn action-btn--delete"
                          size="small"
                          plain
                          @click="deleteRecord(row)"
                          :icon="Delete"
                        >
                          删除
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 移动端卡片布局 -->
                <div v-if="isMobile && records.length > 0" class="mobile-cards">
                  <div v-for="record in records" :key="record.id" class="mobile-card">
                    <div class="card-header">
                      <div class="card-title">
                        <span class="user-name">{{ record.userRealName }}</span>
                        <div class="card-badges">
                          <el-tag :type="getStatusType(record.status)" size="small">
                            {{ record.status }}
                          </el-tag>
                        </div>
                      </div>
                    </div>
                    
                    <div class="card-content">
                      <div class="card-info">
                        <div class="info-item">
                          <span class="info-label">执勤日期:</span>
                          <span class="info-value">{{ formatDate(record.dutyDate) }}</span>
                        </div>
                        <div class="info-item">
                          <span class="info-label">计划时间:</span>
                          <span class="info-value time-range">{{ record.startTime }} - {{ record.endTime }}</span>
                        </div>
                        <div class="info-item">
                          <span class="info-label">签到时间:</span>
                          <span class="info-value">
                            <span v-if="record.checkinTime">{{ formatTime(record.checkinTime) }}</span>
                            <el-tag v-else type="info" size="small">未签到</el-tag>
                          </span>
                        </div>
                        <div class="info-item">
                          <span class="info-label">签退时间:</span>
                          <span class="info-value">
                            <span v-if="record.checkoutTime">{{ formatTime(record.checkoutTime) }}</span>
                            <el-tag v-else type="warning" size="small">未签退</el-tag>
                          </span>
                        </div>
                        <div class="info-item" v-if="record.notes">
                          <span class="info-label">备注:</span>
                          <span class="info-value">{{ record.notes }}</span>
                        </div>
                      </div>
                    </div>
                    
                    <div class="card-actions">
                      <el-button class="action-btn action-btn--detail" type="primary" size="small" @click="viewRecordDetail(record)">
                        <el-icon><View /></el-icon>
                        详情
                      </el-button>
                      <el-button class="action-btn action-btn--delete" type="danger" size="small" plain @click="deleteRecord(record)">
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 排班调换管理 -->
          <el-tab-pane label="调换申请管理" name="swap">
            <div class="records-section">
              <el-card class="search-card" v-if="!swapLoading">
                <el-row :gutter="16" class="search-row">
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-select
                      v-model="swapFilters.status"
                      placeholder="调换状态"
                      clearable
                      class="search-select"
                    >
                      <el-option label="待处理" value="PENDING" />
                      <el-option label="已同意" value="APPROVED" />
                      <el-option label="已拒绝" value="REJECTED" />
                      <el-option label="已撤销" value="CANCELLED" />
                    </el-select>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="10" :lg="8">
                    <el-date-picker
                      v-model="swapFilters.dateRange"
                      type="daterange"
                      range-separator="至"
                      start-placeholder="开始日期"
                      end-placeholder="结束日期"
                      class="search-date-picker"
                      value-format="YYYY-MM-DD"
                    />
                  </el-col>
                  <el-col :xs="24" :sm="24" :md="6" :lg="4">
                    <div class="search-actions">
                      <el-button
                        class="toolbar-action toolbar-action--ghost"
                        @click="resetSwapFilters"
                        :icon="Refresh"
                      >
                        <span v-if="!isMobile">重置</span>
                      </el-button>
                    </div>
                  </el-col>
                </el-row>
              </el-card>

              <div class="records-list">
                <SkeletonLoader v-if="swapLoading" type="table" :rows="5" />
                <EmptyState
                  v-else-if="swapRequests.length === 0"
                  type="no-data"
                  description="暂无排班调换申请"
                />

                <el-table
                  v-if="!swapLoading && swapRequests.length > 0"
                  :data="filteredSwapRequests"
                  stripe
                >
                  <el-table-column label="发起人" width="120">
                    <template #default="{ row }">
                      {{ row.requester?.realName }}
                    </template>
                  </el-table-column>
                  <el-table-column label="被调换人" width="120">
                    <template #default="{ row }">
                      {{ row.targetUser?.realName }}
                    </template>
                  </el-table-column>
                  <el-table-column label="我的排班" min-width="220">
                    <template #default="{ row }">
                      {{ formatSwapSchedule(row.requesterSchedule) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="对方排班" min-width="220">
                    <template #default="{ row }">
                      {{ formatSwapSchedule(row.targetSchedule, true) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="调换类型" width="120">
                    <template #default="{ row }">
                      <el-tag v-if="isCrossWeekSwap(row)" type="danger" effect="plain" size="small">
                        跨星期
                      </el-tag>
                      <el-tag v-else type="info" effect="plain" size="small">
                        同星期
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="调换日期" width="160">
                    <template #default="{ row }">
                      {{ formatDate(row.swapDate) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="reason" label="申请原因" show-overflow-tooltip />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getSwapStatusType(row.status)" size="small">
                        {{ getSwapStatusLabel(row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="260" fixed="right">
                    <template #default="{ row }">
                      <div class="table-actions">
                        <el-button
                          type="success"
                          class="action-btn action-btn--success"
                          size="small"
                          @click="handleAdminSwapDecision(row, true)"
                          :disabled="row.status !== 'PENDING'"
                          :icon="CircleCheck"
                        >
                          同意
                        </el-button>
                        <el-button
                          type="danger"
                          class="action-btn action-btn--reject"
                          size="small"
                          plain
                          @click="handleAdminSwapDecision(row, false)"
                          :disabled="row.status !== 'PENDING'"
                          :icon="CircleClose"
                        >
                          拒绝
                        </el-button>
                        <el-button
                          type="danger"
                          class="action-btn action-btn--delete"
                          size="small"
                          plain
                          @click="deleteSwapRequest(row)"
                          :icon="Delete"
                        >
                          删除
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <!-- 添加/编辑排班对话框 -->
    <el-dialog
      v-model="showScheduleDialog"
      :title="editingSchedule ? '编辑排班' : '添加排班'"
      :width="isMobile ? '95%' : '500px'"
      :before-close="handleScheduleDialogClose"
      class="schedule-dialog"
    >
      <el-form
        ref="scheduleFormRef"
        :model="scheduleForm"
        :rules="scheduleRules"
        :label-width="isMobile ? '80px' : '100px'"
        class="schedule-form"
      >
        <el-form-item label="执勤人员" prop="userId">
          <el-select
            v-model="scheduleForm.userId"
            placeholder="请选择执勤人员"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="`${user.realName} (${user.departmentName || '无部门'})`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="执勤日期" prop="dayOfWeek">
          <el-select
            v-model="scheduleForm.dayOfWeek"
            placeholder="请选择执勤日期"
            style="width: 100%"
          >
            <el-option label="周一" :value="1" />
            <el-option label="周二" :value="2" />
            <el-option label="周三" :value="3" />
            <el-option label="周四" :value="4" />
            <el-option label="周五" :value="5" />
            <el-option label="周六" :value="6" />
            <el-option label="周日" :value="7" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="开始时间" prop="startTime">
          <el-time-picker
            v-model="scheduleForm.startTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="请选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="结束时间" prop="endTime">
          <el-time-picker
            v-model="scheduleForm.endTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="请选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input
            v-model="scheduleForm.notes"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
        
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="提前签到" prop="earlyCheckinMinutes">
              <el-input-number
                v-model="scheduleForm.earlyCheckinMinutes"
                :min="0"
                :max="180"
                :step="5"
                style="width: 100%"
                controls-position="right"
              />
              <div class="form-tip">允许提前签到的分钟数（0-180分钟）</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="延迟签到" prop="lateCheckinMinutes">
              <el-input-number
                v-model="scheduleForm.lateCheckinMinutes"
                :min="0"
                :max="120"
                :step="5"
                style="width: 100%"
                controls-position="right"
              />
              <div class="form-tip">允许延迟签到的分钟数（0-120分钟）</div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="状态">
          <el-switch
            v-model="scheduleForm.active"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleScheduleDialogClose">取消</el-button>
        <el-button type="primary" @click="saveSchedule" :loading="scheduleSaving">
          {{ editingSchedule ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 记录详情对话框 -->
    <el-dialog
      v-model="showRecordDialog"
      title="执勤记录详情"
      :width="isMobile ? '95%' : '600px'"
      class="record-dialog"
    >
      <div v-if="viewingRecord" class="record-detail duty-detail-panel">
        <div class="detail-summary">
          <div class="detail-avatar">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="detail-summary-main">
            <div class="detail-name">
              {{ viewingRecord.userRealName || viewingRecord.user?.realName || '-' }}
            </div>
            <div class="detail-subtitle">
              {{ viewingRecord.departmentName || viewingRecord.user?.department?.name || '无部门' }}
            </div>
          </div>
          <el-tag :type="getStatusType(viewingRecord.status)" size="large" class="detail-status">
            {{ viewingRecord.status }}
          </el-tag>
        </div>

        <div class="detail-grid">
          <div class="detail-cell">
            <div class="detail-label">
              <el-icon><Calendar /></el-icon>
              执勤日期
            </div>
            <div class="detail-value">{{ formatDate(viewingRecord.dutyDate) }}</div>
          </div>
          <div class="detail-cell">
            <div class="detail-label">
              <el-icon><Clock /></el-icon>
              计划时间
            </div>
            <div class="detail-value">
              {{ viewingRecord.startTime || viewingRecord.dutySchedule?.startTime || '-' }}
              -
              {{ viewingRecord.endTime || viewingRecord.dutySchedule?.endTime || '-' }}
            </div>
          </div>
          <div class="detail-cell">
            <div class="detail-label">
              <el-icon><Check /></el-icon>
              实际签到
            </div>
            <div class="detail-value">
              {{ viewingRecord.checkinTime ? formatTime(viewingRecord.checkinTime) : '未签到' }}
            </div>
          </div>
          <div class="detail-cell">
            <div class="detail-label">
              <el-icon><CircleCheck /></el-icon>
              实际签退
            </div>
            <div class="detail-value">
              {{ viewingRecord.checkoutTime ? formatTime(viewingRecord.checkoutTime) : '未签退' }}
            </div>
          </div>
          <div class="detail-cell">
            <div class="detail-label">
              <el-icon><Clock /></el-icon>
              执勤时长
            </div>
            <div class="detail-value">
              {{ viewingRecord.checkinTime && viewingRecord.checkoutTime ? calculateDuration(viewingRecord.checkinTime, viewingRecord.checkoutTime) : '-' }}
            </div>
          </div>
          <div class="detail-cell detail-note-cell">
            <div class="detail-label">
              <el-icon><Edit /></el-icon>
              备注信息
            </div>
            <div class="detail-value detail-note">
              {{ viewingRecord.notes || '暂无备注' }}
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showRecordDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 管理员发起排班调换对话框 -->
    <el-dialog
      v-model="showSwapDialog"
      title="发起排班调换"
      :width="isMobile ? '95%' : '520px'"
      class="schedule-dialog"
    >
      <div class="record-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="发起人">
            {{ selectedSwapSchedule?.user?.realName }}（{{ selectedSwapSchedule?.user?.department?.name || '无部门' }}）
          </el-descriptions-item>
          <el-descriptions-item label="当前排班">
            {{ formatSwapSchedule(selectedSwapSchedule) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-form class="schedule-form" label-width="90px">
        <el-form-item label="调换对象">
          <el-select
            v-model="swapForm.targetScheduleId"
            placeholder="请选择要调换的排班"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="item in availableSwapTargets"
              :key="item.id"
              :label="formatSwapOption(item)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="申请原因">
          <el-input
            v-model="swapForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入申请原因（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
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
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, Edit, Delete, View, Check, Clock, Calendar,
  CircleCheck, CircleClose, RefreshRight, SwitchButton, OfficeBuilding
} from '@element-plus/icons-vue'

// 响应式屏幕尺寸检测
const windowWidth = ref(window.innerWidth)

// 计算属性：判断是否为移动设备
const isMobile = computed(() => windowWidth.value <= 768)

// 监听窗口大小变化
const handleResize = () => {
  windowWidth.value = window.innerWidth
}
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import EmptyState from '@/components/EmptyState.vue'
import request from '@/utils/request'

// 数据状态
const activeTab = ref('schedule')
const statsLoading = ref(false)
const scheduleLoading = ref(false)
const recordLoading = ref(false)
const scheduleSaving = ref(false)
const swapLoading = ref(false)
const swapSubmitting = ref(false)

// 统计数据
const stats = ref({})

// 排班相关
const schedules = ref([])
const scheduleSearch = reactive({
  keyword: '',
  dayOfWeek: null
})

// 执勤记录相关
const records = ref([])
const recordSearch = reactive({
  keyword: '',
  dateRange: null,
  status: null
})

// 用户列表
const users = ref([])

// 排班调换相关
const swapRequests = ref([])
const swapFilters = reactive({
  status: '',
  dateRange: null
})
const showSwapDialog = ref(false)
const selectedSwapSchedule = ref(null)
const swapForm = reactive({
  requesterScheduleId: null,
  targetScheduleId: null,
  reason: ''
})
const activeSchedules = ref([])

// 对话框状态
const showScheduleDialog = ref(false)
const showRecordDialog = ref(false)
const editingSchedule = ref(null)
const viewingRecord = ref(null)

// 排班表单
const scheduleForm = reactive({
  userId: null,
  dayOfWeek: null,
  startTime: '',
  endTime: '',
  notes: '',
  active: true,
  earlyCheckinMinutes: 30,
  lateCheckinMinutes: 15
})

const scheduleFormRef = ref()

// 表单验证规则
const scheduleRules = {
  userId: [
    { required: true, message: '请选择执勤人员', trigger: 'change' }
  ],
  dayOfWeek: [
    { required: true, message: '请选择执勤日期', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  earlyCheckinMinutes: [
    { required: true, message: '请输入提前签到时间', trigger: 'blur' },
    { type: 'number', min: 0, max: 180, message: '提前签到时间必须在0-180分钟之间', trigger: 'blur' }
  ],
  lateCheckinMinutes: [
    { required: true, message: '请输入延迟签到时间', trigger: 'blur' },
    { type: 'number', min: 0, max: 120, message: '延迟签到时间必须在0-120分钟之间', trigger: 'blur' }
  ]
}

// 获取统计数据
const fetchStats = async () => {
  statsLoading.value = true
  try {
    const response = await request.get('/duty/statistics')
    stats.value = {
      totalSchedules: response.data.totalSchedules || 0,
      activeSchedules: response.data.activeSchedules || 0,
      todayDuties: response.data.todayDuties || 0,
      completedDuties: response.data.completedDuties || 0
    }
  } catch (error) {
    ElMessage.error('获取统计数据失败')
  } finally {
    statsLoading.value = false
  }
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    // 使用与用户管理页面相同的接口，获取所有用户数据
    const response = await request.get('/users', {
      params: {
        page: 0,
        size: 1000  // 获取足够多的用户
      }
    })
    
    if (response.data && response.data.content) {
      users.value = response.data.content
      console.log('用户数据加载成功:', users.value)
    } else {
      users.value = []
      console.warn('用户数据格式异常:', response.data)
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

// 获取排班列表
const fetchSchedules = async () => {
  scheduleLoading.value = true
  try {
    const params = {}
    if (scheduleSearch.keyword) {
      params.keyword = scheduleSearch.keyword
    }
    if (scheduleSearch.dayOfWeek !== null && scheduleSearch.dayOfWeek !== undefined) {
      params.dayOfWeek = scheduleSearch.dayOfWeek
    }
    
    const response = await request.get('/duty/schedules', { params })
    schedules.value = response.data || []
  } catch (error) {
    ElMessage.error('获取排班列表失败')
  } finally {
    scheduleLoading.value = false
  }
}

// 获取执勤记录
const fetchRecords = async () => {
  recordLoading.value = true
  try {
    const params = {
      page: 0,
      size: 1000
    }
    
    if (recordSearch.keyword) {
      params.keyword = recordSearch.keyword
    }
    if (recordSearch.status) {
      params.status = recordSearch.status
    }
    if (recordSearch.dateRange && recordSearch.dateRange.length === 2) {
      params.startDate = recordSearch.dateRange[0].toISOString().split('T')[0]
      params.endDate = recordSearch.dateRange[1].toISOString().split('T')[0]
    }
    
    const response = await request.get('/duty/records', { params })
    records.value = response.data?.content || response.data || []
  } catch (error) {
    ElMessage.error('获取执勤记录失败')
  } finally {
    recordLoading.value = false
  }
}

// 切换选项卡
const handleTabChange = (tabName) => {
  if (tabName === 'schedule') {
    fetchSchedules()
  } else if (tabName === 'records') {
    fetchRecords()
  } else if (tabName === 'swap') {
    fetchSwapRequests()
  }
}

// 重置排班搜索
const resetScheduleSearch = () => {
  scheduleSearch.keyword = ''
  scheduleSearch.dayOfWeek = null
  fetchSchedules()
}

// 重置记录搜索
const resetRecordSearch = () => {
  recordSearch.keyword = ''
  recordSearch.dateRange = null
  recordSearch.status = null
  fetchRecords()
}

const resetSwapFilters = () => {
  swapFilters.status = ''
  swapFilters.dateRange = null
}

// 编辑排班
const editSchedule = (schedule) => {
  editingSchedule.value = schedule
  Object.assign(scheduleForm, {
    userId: schedule.user.id,
    dayOfWeek: schedule.dayOfWeek,
    startTime: schedule.startTime,
    endTime: schedule.endTime,
    notes: schedule.notes,
    active: schedule.active,
    earlyCheckinMinutes: schedule.earlyCheckinMinutes || 30,
    lateCheckinMinutes: schedule.lateCheckinMinutes || 15
  })
  showScheduleDialog.value = true
}

// 切换排班状态
const toggleScheduleStatus = async (schedule) => {
  try {
    const action = schedule.active ? '禁用' : '启用'
    await ElMessageBox.confirm(
      `确定要${action}该排班吗？`,
      '确认操作',
      { type: 'warning' }
    )
    
    await request.post(`/api/duty/schedules/${schedule.id}/toggle`, {
      active: !schedule.active
    })
    
    schedule.active = !schedule.active
    ElMessage.success(`${action}成功`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 删除排班
const deleteSchedule = async (schedule) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该排班吗？此操作不可恢复！',
      '确认删除',
      { type: 'warning' }
    )
    
    await request.delete(`/duty/schedules/${schedule.id}`)
    
    const index = schedules.value.findIndex(item => item.id === schedule.id)
    if (index > -1) {
      schedules.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 保存排班
const saveSchedule = async () => {
  try {
    await scheduleFormRef.value.validate()
    scheduleSaving.value = true
    
    if (editingSchedule.value) {
      // 更新排班
      await request.put(`/duty/schedules/${editingSchedule.value.id}`, scheduleForm)
      ElMessage.success('排班更新成功')
    } else {
      // 添加排班
      await request.post('/duty/schedules', scheduleForm)
      ElMessage.success('排班添加成功')
    }
    
    // 重新获取排班列表以确保数据一致性
    await fetchSchedules()
    
    handleScheduleDialogClose()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    scheduleSaving.value = false
  }
}

// 关闭排班对话框
const handleScheduleDialogClose = () => {
  showScheduleDialog.value = false
  editingSchedule.value = null
  Object.assign(scheduleForm, {
    userId: null,
    dayOfWeek: null,
    startTime: '',
    endTime: '',
    notes: '',
    active: true,
    earlyCheckinMinutes: 30,
    lateCheckinMinutes: 15
  })
  scheduleFormRef.value?.clearValidate()
}

// 查看记录详情
const viewRecordDetail = (record) => {
  viewingRecord.value = record
  showRecordDialog.value = true
}

// 删除执勤记录
const deleteRecord = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${record.userRealName} 在 ${formatDate(record.dutyDate)} 的执勤记录吗？删除后将无法恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    console.log('正在删除执勤记录:', record.id)
    await request.delete(`/duty/records/${record.id}`)
    
    ElMessage.success('执勤记录删除成功')
    
    // 刷新记录列表和统计数据
    fetchRecords()
    fetchStats()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除执勤记录失败:', error)
      ElMessage.error('删除失败')
    }
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

// 获取所有排班调换申请
const fetchSwapRequests = async () => {
  swapLoading.value = true
  try {
    const response = await request.get('/duty/swap-requests')
    swapRequests.value = response.data || []
  } catch (error) {
    console.error('获取排班调换申请失败:', error)
    ElMessage.error('获取排班调换申请失败')
  } finally {
    swapLoading.value = false
  }
}

// 管理员打开发起调换对话框
const openAdminSwapDialog = (schedule) => {
  selectedSwapSchedule.value = schedule
  swapForm.requesterScheduleId = schedule.id
  swapForm.targetScheduleId = null
  swapForm.reason = ''
  // 确保已加载启用排班
  if (!activeSchedules.value.length) {
    fetchActiveSchedules().then(() => {
      showSwapDialog.value = true
    })
  } else {
    showSwapDialog.value = true
  }
}

// 可选调换目标
const availableSwapTargets = computed(() => {
  if (!swapForm.requesterScheduleId || !selectedSwapSchedule.value) return []
  const requesterUserId = selectedSwapSchedule.value.user?.id
  return activeSchedules.value.filter(item => {
    return (
      item.id !== swapForm.requesterScheduleId &&
      item.user &&
      item.user.id !== requesterUserId &&
      item.active
    )
  })
})

const filteredSwapRequests = computed(() => {
  return swapRequests.value.filter(item => {
    let statusMatch = true
    let dateMatch = true

    if (swapFilters.status) {
      statusMatch = item.status === swapFilters.status
    }

    if (Array.isArray(swapFilters.dateRange) && swapFilters.dateRange.length === 2) {
      const [start, end] = swapFilters.dateRange
      if (start && (!item.swapDate || item.swapDate < start)) {
        dateMatch = false
      }
      if (end && (!item.swapDate || item.swapDate > end)) {
        dateMatch = false
      }
    }

    return statusMatch && dateMatch
  })
})

// 提交排班调换申请
const handleCreateSwapRequest = async () => {
  if (!swapForm.requesterScheduleId || !swapForm.targetScheduleId) {
    ElMessage.warning('请选择要调换的排班')
    return
  }
  try {
    swapSubmitting.value = true
    await request.post('/duty/swap-requests', swapForm)
    ElMessage.success('排班调换申请已提交')
    showSwapDialog.value = false
    await fetchSwapRequests()
  } catch (error) {
    console.error('提交排班调换申请失败:', error)
    ElMessage.error(error.response?.data?.message || '提交排班调换申请失败')
  } finally {
    swapSubmitting.value = false
  }
}

// 管理员处理调换申请
const handleAdminSwapDecision = async (item, approve) => {
  try {
    await ElMessageBox.confirm(
      `确定要${approve ? '同意' : '拒绝'}该调换申请吗？`,
      '确认操作',
      { type: 'warning' }
    )
    swapSubmitting.value = true
    await request.post(`/duty/swap-requests/${item.id}/decision`, {
      approve,
      reason: ''
    })
    ElMessage.success(approve ? '已同意该调换申请' : '已拒绝该调换申请')
    await fetchSwapRequests()
    await fetchSchedules()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('处理排班调换申请失败:', error)
      ElMessage.error(error.response?.data?.message || '处理排班调换申请失败')
    }
  } finally {
    swapSubmitting.value = false
  }
}

// 删除排班调换申请
const deleteSwapRequest = async (item) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条排班调换申请吗？此操作不可恢复！',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    await request.delete(`/duty/swap-requests/${item.id}`)
    ElMessage.success('排班调换申请删除成功')
    await fetchSwapRequests()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除排班调换申请失败:', error)
      ElMessage.error(error.response?.data?.message || '删除排班调换申请失败')
    }
  }
}

// 工具函数
const getDayName = (dayOfWeek) => {
  const days = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return days[dayOfWeek] || ''
}

const getStatusType = (status) => {
  const statusTypes = {
    '待执勤': 'info',
    '执勤中': 'warning',
    '已完成': 'success',
    '已请假': 'info',
    '缺勤': 'danger'
  }
  return statusTypes[status] || 'info'
}

// 调换状态类型
const getSwapStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

// 调换状态中文
const getSwapStatusLabel = (status) => {
  const map = {
    PENDING: '待处理',
    APPROVED: '已同意',
    REJECTED: '已拒绝',
    CANCELLED: '已撤销'
  }
  return map[status] || status
}

// 格式化排班信息
const formatSwapSchedule = (schedule, withUser = false) => {
  if (!schedule) return '未知排班'
  const base = `${getDayName(schedule.dayOfWeek)} ${schedule.startTime} - ${schedule.endTime}`
  if (withUser) {
    const userName = schedule.user?.realName || '未知用户'
    return `${userName} · ${base}`
  }
  return base
}

const isCrossWeekSwap = (swap) => {
  const requesterDay = swap.requesterSchedule?.dayOfWeek
  const targetDay = swap.targetSchedule?.dayOfWeek
  return requesterDay !== undefined && targetDay !== undefined && requesterDay !== targetDay
}

// 选项格式
const formatSwapOption = (schedule) => {
  if (!schedule) return ''
  const userName = schedule.user?.realName || '未知用户'
  const dept = schedule.user?.department?.name || '无部门'
  return `${userName}（${dept}） - ${getDayName(schedule.dayOfWeek)} ${schedule.startTime} - ${schedule.endTime}`
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const weekday = weekdays[date.getDay()]
  const dateString = date.toLocaleDateString('zh-CN')
  return `${dateString} ${weekday}`
}

const formatTime = (timeStr) => {
  return new Date(timeStr).toLocaleString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 计算执勤时长
const calculateDuration = (startTime, endTime) => {
  if (!startTime || !endTime) return ''
  
  const start = new Date(startTime)
  const end = new Date(endTime)
  const diffMs = end - start
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  return `${diffHours}小时${diffMinutes}分钟`
}

// 生命周期
onMounted(() => {
  fetchStats()
  fetchUsers()
  fetchSchedules()
  fetchActiveSchedules()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.duty-management-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-description {
  color: #6b7280;
  margin: 0;
  font-size: 14px;
}

.header-right {
  flex-shrink: 0;
}

.header-action-btn.el-button {
  height: 40px;
  padding: 0 18px;
  border: 1px solid rgba(14, 165, 233, 0.22);
  border-radius: 999px;
  background:
    linear-gradient(135deg, rgba(224, 242, 254, 0.96), rgba(187, 247, 208, 0.88));
  color: #075985;
  font-weight: 800;
  box-shadow: 0 12px 26px rgba(14, 116, 144, 0.14);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.header-action-btn.el-button:hover,
.header-action-btn.el-button:focus {
  border-color: rgba(14, 165, 233, 0.42);
  background:
    linear-gradient(135deg, rgba(207, 250, 254, 0.98), rgba(187, 247, 208, 0.96));
  color: #064e3b;
  box-shadow: 0 16px 34px rgba(14, 116, 144, 0.18);
  transform: translateY(-1px);
}

.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  min-height: 126px;
  padding: 24px;
  border: 1px solid rgba(14, 165, 233, 0.14);
  border-radius: 20px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(240, 253, 250, 0.82));
  box-shadow: 0 18px 38px rgba(14, 116, 144, 0.08);
  position: relative;
  overflow: hidden;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.stat-card::before {
  content: '';
  position: absolute;
  inset: 18px auto 18px 0;
  width: 4px;
  border-radius: 0 999px 999px 0;
  background: var(--stat-accent, #22d3ee);
  opacity: 0.82;
}

.stat-card:hover {
  border-color: rgba(14, 165, 233, 0.24);
  box-shadow: 0 22px 48px rgba(14, 116, 144, 0.12);
  transform: translateY(-2px);
}

.stat-card--total {
  --stat-accent: #22d3ee;
  --stat-color: #0369a1;
  --stat-icon-bg: linear-gradient(145deg, rgba(224, 242, 254, 0.96), rgba(207, 250, 254, 0.86));
}

.stat-card--active {
  --stat-accent: #34d399;
  --stat-color: #047857;
  --stat-icon-bg: linear-gradient(145deg, rgba(220, 252, 231, 0.96), rgba(209, 250, 229, 0.86));
}

.stat-card--today {
  --stat-accent: #60a5fa;
  --stat-color: #2563eb;
  --stat-icon-bg: linear-gradient(145deg, rgba(219, 234, 254, 0.96), rgba(224, 242, 254, 0.86));
}

.stat-card--completed {
  --stat-accent: #fbbf24;
  --stat-color: #b45309;
  --stat-icon-bg: linear-gradient(145deg, rgba(254, 249, 195, 0.96), rgba(255, 237, 213, 0.88));
}

.stat-content {
  display: flex;
  flex-direction: column;
  min-height: 78px;
  padding-right: 76px;
  position: relative;
  z-index: 1;
}

.stat-value {
  font-size: 34px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
  letter-spacing: 0;
}

.stat-label {
  font-size: 14px;
  color: #475569;
  font-weight: 650;
}

.stat-icon {
  position: absolute;
  top: 50%;
  right: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  border: 1px solid rgba(255, 255, 255, 0.82);
  border-radius: 19px;
  background: var(--stat-icon-bg);
  color: var(--stat-color, #0891b2);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.86),
    0 14px 28px rgba(14, 116, 144, 0.12);
  transform: translateY(-50%);
}

.stat-icon::after {
  content: '';
  position: absolute;
  inset: 7px 9px auto 9px;
  height: 1px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
}

.stat-icon .el-icon {
  font-size: 28px;
  filter: drop-shadow(0 4px 8px rgba(14, 116, 144, 0.12));
}

.tabs-section {
  margin-bottom: 24px;
}

/* 搜索卡片样式 */
.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-row {
  align-items: flex-end;
}

.search-input,
.search-select,
.search-date-picker {
  width: 100%;
}

.search-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
}

.toolbar-action.el-button {
  min-width: 84px;
  height: 36px;
  padding: 0 16px;
  font-size: 13px;
  font-weight: 750;
  border-radius: 999px;
  border: 1px solid rgba(14, 165, 233, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  box-shadow: 0 10px 22px rgba(14, 116, 144, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.toolbar-action.el-button + .toolbar-action.el-button {
  margin-left: 0;
}

.toolbar-action--primary.el-button {
  background: linear-gradient(135deg, rgba(207, 250, 254, 0.98), rgba(224, 242, 254, 0.95));
  color: #075985;
}

.toolbar-action--ghost.el-button {
  background: rgba(255, 255, 255, 0.86);
  color: #0f766e;
  border-color: rgba(20, 184, 166, 0.16);
}

.toolbar-action.el-button:hover,
.toolbar-action.el-button:focus {
  border-color: rgba(14, 165, 233, 0.34);
  color: #0c4a6e;
  box-shadow: 0 14px 28px rgba(14, 116, 144, 0.12);
  transform: translateY(-1px);
}

.time-range {
  font-size: 13px;
  color: #374151;
}

.record-detail {
  padding: 16px 0;
}

.record-dialog :deep(.el-dialog) {
  border-radius: 24px;
  background:
    linear-gradient(135deg, rgba(248, 253, 255, 0.98), rgba(255, 255, 255, 0.96)),
    radial-gradient(circle at 92% 0%, rgba(75, 211, 180, 0.14), transparent 34%);
  box-shadow: 0 28px 72px rgba(8, 47, 73, 0.18);
  overflow: hidden;
}

.record-dialog :deep(.el-dialog__header) {
  margin: 0;
  padding: 22px 26px 14px;
  border-bottom: 1px solid rgba(14, 165, 233, 0.12);
}

.record-dialog :deep(.el-dialog__title) {
  color: #0c4a6e;
  font-size: 20px;
  font-weight: 800;
}

.record-dialog :deep(.el-dialog__body) {
  padding: 18px 24px 8px;
}

.schedule-dialog :deep(.el-dialog__footer),
.record-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px 22px;
  border-top: 1px solid rgba(14, 165, 233, 0.1);
  background: rgba(240, 249, 255, 0.52);
}

.schedule-dialog :deep(.el-dialog__footer .el-button),
.record-dialog :deep(.el-dialog__footer .el-button) {
  min-width: 92px;
  height: 38px;
  border-radius: 999px;
  border-color: rgba(14, 165, 233, 0.24);
  background: linear-gradient(135deg, rgba(240, 249, 255, 0.96), rgba(255, 255, 255, 0.96));
  color: #0369a1;
  font-weight: 750;
  box-shadow: 0 10px 20px rgba(14, 116, 144, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.schedule-dialog :deep(.el-dialog__footer .el-button--primary),
.record-dialog :deep(.el-dialog__footer .el-button--primary) {
  border-color: rgba(14, 165, 233, 0.24);
  background: linear-gradient(135deg, rgba(207, 250, 254, 0.98), rgba(187, 247, 208, 0.9));
  color: #075985;
}

.schedule-dialog :deep(.el-dialog__footer .el-button:hover),
.record-dialog :deep(.el-dialog__footer .el-button:hover) {
  border-color: rgba(14, 165, 233, 0.38);
  color: #0c4a6e;
  box-shadow: 0 14px 26px rgba(14, 116, 144, 0.12);
  transform: translateY(-1px);
}

.duty-detail-panel {
  padding: 0;
}

.detail-summary {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border: 1px solid rgba(14, 165, 233, 0.14);
  border-radius: 20px;
  background:
    radial-gradient(circle at 92% 0%, rgba(125, 211, 252, 0.22), transparent 36%),
    linear-gradient(135deg, rgba(236, 254, 255, 0.88), rgba(255, 255, 255, 0.86));
  box-shadow: 0 14px 30px rgba(14, 116, 144, 0.08);
}

.detail-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 18px;
  background: linear-gradient(135deg, #67e8f9, #7dd3fc);
  color: #075985;
  font-size: 24px;
  flex-shrink: 0;
}

.detail-summary-main {
  min-width: 0;
  flex: 1;
}

.detail-name {
  color: #0c4a6e;
  font-size: 18px;
  font-weight: 800;
  line-height: 1.3;
}

.detail-subtitle {
  margin-top: 4px;
  color: #4b7186;
  font-size: 13px;
}

.detail-status {
  flex-shrink: 0;
  border-radius: 999px;
  font-weight: 750;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.detail-cell {
  min-height: 88px;
  padding: 14px 16px;
  border: 1px solid rgba(14, 165, 233, 0.12);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 10px 24px rgba(14, 116, 144, 0.05);
}

.detail-label {
  display: flex;
  align-items: center;
  gap: 7px;
  color: #4b7186;
  font-size: 13px;
  font-weight: 750;
}

.detail-label .el-icon {
  color: #0891b2;
}

.detail-value {
  margin-top: 10px;
  color: #123f55;
  font-size: 16px;
  font-weight: 750;
  line-height: 1.45;
  word-break: break-word;
}

.detail-note-cell {
  grid-column: 1 / -1;
  min-height: auto;
}

.detail-note {
  color: #334155;
  font-size: 15px;
  font-weight: 600;
}


.form-tip {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
  line-height: 1.4;
}

/* 移动端卡片样式 */
.mobile-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.mobile-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.card-header {
  padding: 16px;
  border-bottom: 1px solid #f5f7fa;
  background: #fafbfc;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  flex: 1;
}

.card-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.card-content {
  padding: 16px;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.info-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  flex-shrink: 0;
  min-width: 80px;
}

.info-value {
  font-size: 14px;
  color: #303133;
  text-align: right;
  flex: 1;
}

.card-actions {
  padding: 12px 16px;
  border-top: 1px solid rgba(14, 165, 233, 0.1);
  background: linear-gradient(135deg, rgba(240, 249, 255, 0.74), rgba(255, 255, 255, 0.9));
  display: flex;
  gap: 9px;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
}

.table-actions,
.action-buttons {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  align-items: center;
}

.table-actions--compact {
  max-width: 190px;
  margin: 0 auto;
}

.action-btn.el-button {
  --action-color: #075985;
  --action-border: rgba(14, 165, 233, 0.18);
  --action-bg: linear-gradient(135deg, rgba(224, 242, 254, 0.96), rgba(207, 250, 254, 0.9));
  --action-shadow: rgba(14, 116, 144, 0.1);
  flex: 1 1 86px;
  max-width: 116px;
  min-width: 84px;
  height: 34px;
  margin-left: 0 !important;
  padding: 0 10px;
  border: 1px solid var(--action-border);
  border-radius: 13px;
  background: var(--action-bg);
  color: var(--action-color);
  font-size: 12px;
  font-weight: 780;
  letter-spacing: 0;
  box-shadow: 0 9px 18px var(--action-shadow);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  white-space: nowrap;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.action-btn.el-button .el-icon {
  font-size: 14px;
  margin-right: 0;
}

.action-btn.el-button:hover,
.action-btn.el-button:focus {
  border-color: color-mix(in srgb, var(--action-color) 24%, transparent);
  color: var(--action-color);
  box-shadow: 0 13px 24px var(--action-shadow);
  transform: translateY(-1px);
}

.action-btn.el-button.is-disabled,
.action-btn.el-button.is-disabled:hover {
  border-color: rgba(148, 163, 184, 0.18);
  background: linear-gradient(135deg, rgba(241, 245, 249, 0.82), rgba(255, 255, 255, 0.72));
  color: #94a3b8;
  box-shadow: none;
  transform: none;
}

.action-btn--edit.el-button,
.action-btn--detail.el-button {
  --action-color: #0369a1;
  --action-border: rgba(14, 165, 233, 0.2);
  --action-bg: linear-gradient(135deg, rgba(224, 242, 254, 0.98), rgba(207, 250, 254, 0.9));
  --action-shadow: rgba(14, 116, 144, 0.12);
}

.action-btn--swap.el-button {
  --action-color: #0f766e;
  --action-border: rgba(20, 184, 166, 0.22);
  --action-bg: linear-gradient(135deg, rgba(204, 251, 241, 0.96), rgba(240, 253, 250, 0.92));
  --action-shadow: rgba(15, 118, 110, 0.1);
}

.action-btn--success.el-button {
  --action-color: #047857;
  --action-border: rgba(34, 197, 94, 0.22);
  --action-bg: linear-gradient(135deg, rgba(220, 252, 231, 0.98), rgba(209, 250, 229, 0.92));
  --action-shadow: rgba(22, 163, 74, 0.1);
}

.action-btn--warning.el-button,
.action-btn--reject.el-button {
  --action-color: #b45309;
  --action-border: rgba(251, 191, 36, 0.28);
  --action-bg: linear-gradient(135deg, rgba(254, 249, 195, 0.98), rgba(255, 237, 213, 0.92));
  --action-shadow: rgba(217, 119, 6, 0.09);
}

.action-btn--delete.el-button {
  --action-color: #be123c;
  --action-border: rgba(251, 113, 133, 0.24);
  --action-bg: linear-gradient(135deg, rgba(255, 241, 242, 0.98), rgba(255, 228, 230, 0.9));
  --action-shadow: rgba(225, 29, 72, 0.09);
}

.card-actions .action-btn.el-button {
  flex: 1 1 calc(50% - 8px);
  max-width: none;
  min-width: 88px;
  height: 36px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .duty-management-container {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .stat-card {
    min-height: 112px;
    padding: 18px;
  }

  .stat-content {
    min-height: 66px;
    padding-right: 64px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
    border-radius: 16px;
  }

  .stat-icon .el-icon {
    font-size: 24px;
  }
  
  .search-row .el-col {
    margin-bottom: 12px;
  }
  
  .search-actions {
    justify-content: center;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .toolbar-action.el-button {
    flex: 1;
    min-width: auto;
    height: 38px;
    font-size: 14px;
  }
  
  .card-actions .action-btn.el-button {
    height: 36px;
    font-size: 13px;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .info-label {
    min-width: auto;
    font-size: 13px;
  }
  
  .info-value {
    text-align: left;
    font-size: 13px;
  }
  
  /* 手机端对话框全屏 */
  .schedule-dialog .el-dialog,
  .record-dialog .el-dialog {
    margin: 0;
    width: 100% !important;
    height: 100vh;
    max-height: none;
    border-radius: 0;
    display: flex;
    flex-direction: column;
  }
  
  .schedule-dialog .el-dialog__body,
  .record-dialog .el-dialog__body {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
  }
  
  .schedule-form .el-form-item {
    margin-bottom: 16px;
  }

  .record-dialog :deep(.el-dialog) {
    width: 94vw !important;
    height: auto;
    max-height: 88vh;
    margin: 6vh auto 0 !important;
    border-radius: 22px;
  }

  .record-dialog :deep(.el-dialog__body) {
    max-height: 66vh;
    padding: 16px;
    overflow-y: auto;
  }

  .detail-summary {
    align-items: flex-start;
    padding: 16px;
  }

  .detail-avatar {
    width: 44px;
    height: 44px;
    border-radius: 15px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .detail-cell {
    min-height: 78px;
    padding: 13px 14px;
  }

  .detail-value {
    font-size: 15px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .header-action-btn.el-button,
  .toolbar-action.el-button,
  .action-btn.el-button,
  .stat-card,
  .schedule-dialog :deep(.el-dialog__footer .el-button),
  .record-dialog :deep(.el-dialog__footer .el-button) {
    transition: none;
  }

  .header-action-btn.el-button:hover,
  .toolbar-action.el-button:hover,
  .action-btn.el-button:hover,
  .stat-card:hover,
  .schedule-dialog :deep(.el-dialog__footer .el-button:hover),
  .record-dialog :deep(.el-dialog__footer .el-button:hover) {
    transform: none;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .page-title {
    font-size: 20px;
  }

  .detail-summary {
    flex-wrap: wrap;
  }

  .detail-status {
    margin-left: 58px;
  }
}
</style>
