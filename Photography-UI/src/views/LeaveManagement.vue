<template>
  <div class="leave-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon><DocumentRemove /></el-icon>
            请假管理
          </h1>
          <p class="page-description">申请和管理请假记录</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="showLeaveDialog = true" :icon="Plus">
            申请请假
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <SkeletonLoader v-if="statsLoading" type="stats" />
          <div v-else class="stat-content">
            <div class="stat-value">{{ stats.totalRequests || 0 }}</div>
            <div class="stat-label">总申请数</div>
            <div class="stat-icon">
              <el-icon><Document /></el-icon>
            </div>
          </div>
        </div>
        
        <div class="stat-card">
          <SkeletonLoader v-if="statsLoading" type="stats" />
          <div v-else class="stat-content">
            <div class="stat-value">{{ stats.pendingRequests || 0 }}</div>
            <div class="stat-label">待审批</div>
            <div class="stat-icon">
              <el-icon><Clock /></el-icon>
            </div>
          </div>
        </div>
        
        <div class="stat-card">
          <SkeletonLoader v-if="statsLoading" type="stats" />
          <div v-else class="stat-content">
            <div class="stat-value">{{ stats.approvedRequests || 0 }}</div>
            <div class="stat-label">已批准</div>
            <div class="stat-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
          </div>
        </div>
        
        <div class="stat-card">
          <SkeletonLoader v-if="statsLoading" type="stats" />
          <div v-else class="stat-content">
            <div class="stat-value">{{ stats.rejectedRequests || 0 }}</div>
            <div class="stat-label">已拒绝</div>
            <div class="stat-icon">
              <el-icon><CircleClose /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能选项卡 -->
    <div class="tabs-section">
      <el-card shadow="never">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <!-- 我的申请 -->
          <el-tab-pane label="我的申请" name="my-requests">
            <div class="requests-section">
              <!-- 搜索区域 -->
              <el-card class="search-card">
                <el-row :gutter="16" class="search-row">
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-select
                      v-model="myRequestSearch.status"
                      placeholder="申请状态"
                      clearable
                      class="search-select"
                    >
                      <el-option label="待审批" value="PENDING" />
                      <el-option label="已批准" value="APPROVED" />
                      <el-option label="已拒绝" value="REJECTED" />
                      <el-option label="已取消" value="CANCELLED" />
                    </el-select>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="8" :lg="6">
                    <el-select
                      v-model="myRequestSearch.leaveType"
                      placeholder="请假类型"
                      clearable
                      class="search-select"
                    >
                      <el-option label="执勤请假" value="DUTY_LEAVE" />
                      <el-option label="打卡请假" value="CHECKIN_LEAVE" />
                      <el-option label="其他" value="OTHER" />
                    </el-select>
                  </el-col>
                  <el-col :xs="24" :sm="24" :md="8" :lg="12">
                    <div class="search-actions">
                      <el-button type="primary" @click="fetchMyRequests" :icon="Search">
                        <span v-if="!isMobile">搜索</span>
                      </el-button>
                      <el-button @click="resetMyRequestSearch" :icon="Refresh">
                        <span v-if="!isMobile">重置</span>
                      </el-button>
                    </div>
                  </el-col>
                </el-row>
              </el-card>

              <!-- 申请列表 -->
              <div class="requests-list">
                <SkeletonLoader v-if="myRequestLoading" type="table" :rows="5" />
                <EmptyState v-else-if="myRequests.length === 0" type="no-data" description="暂无请假申请" />
                
                <!-- 桌面端表格 -->
                <div class="desktop-table" v-show="!isMobile && myRequests.length > 0">
                  <el-table :data="myRequests" stripe>
                    <el-table-column label="请假类型" width="120">
                      <template #default="{ row }">
                        <el-tag :type="getLeaveTypeColor(row.leaveType)" size="small">
                          {{ getLeaveTypeName(row.leaveType) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="请假日期" width="180">
                      <template #default="{ row }">
                        <div class="date-range">
                          {{ formatDate(row.startDate) }}
                          <span v-if="row.endDate && row.endDate !== row.startDate">
                            至 {{ formatDate(row.endDate) }}
                          </span>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column label="请假天数" width="100">
                      <template #default="{ row }">
                        <el-tag size="small">{{ calculateDays(row.startDate, row.endDate) }}天</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="reason" label="请假原因" show-overflow-tooltip />
                    <el-table-column label="申请时间" width="150">
                      <template #default="{ row }">
                        {{ formatDateTime(row.applyTime) }}
                      </template>
                    </el-table-column>
                    <el-table-column label="状态" width="100">
                      <template #default="{ row }">
                        <el-tag :type="getStatusColor(row.status)" size="small">
                          {{ getStatusName(row.status) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="180" fixed="right">
                      <template #default="{ row }">
                        <el-button
                          type="primary"
                          size="small"
                          @click="viewRequestDetail(row)"
                          :icon="View"
                        >
                          详情
                        </el-button>
                        <el-button
                          v-if="row.status === 'PENDING'"
                          type="warning"
                          size="small"
                          @click="cancelRequest(row)"
                          :icon="Close"
                        >
                          取消
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
                
                <!-- 移动端卡片布局 -->
                <div v-if="isMobile && myRequests.length > 0" class="mobile-cards">
                  <div v-for="request in myRequests" :key="request.id" class="mobile-card">
                    <div class="card-header">
                      <div class="card-title">
                        <el-tag :type="getLeaveTypeColor(request.leaveType)" size="small">
                          {{ getLeaveTypeName(request.leaveType) }}
                        </el-tag>
                        <div class="card-badges">
                          <el-tag :type="getStatusColor(request.status)" size="small">
                            {{ getStatusName(request.status) }}
                          </el-tag>
                        </div>
                        <el-checkbox
                          v-if="isMobile"
                          :model-value="selectedAdminIds.includes(request.id)"
                          @change="checked => toggleMobileSelection(request.id, checked)"
                          class="mobile-select-checkbox"
                        >
                          选择
                        </el-checkbox>
                      </div>
                    </div>
                    
                    <div class="card-content">
                      <div class="card-info">
                        <div class="info-item">
                          <span class="info-label">请假日期:</span>
                          <span class="info-value">
                            {{ formatDate(request.startDate) }}
                            <span v-if="request.endDate && request.endDate !== request.startDate">
                              至 {{ formatDate(request.endDate) }}
                            </span>
                          </span>
                        </div>
                        <div class="info-item">
                          <span class="info-label">请假天数:</span>
                          <span class="info-value">{{ calculateDays(request.startDate, request.endDate) }}天</span>
                        </div>
                        <div class="info-item">
                          <span class="info-label">申请时间:</span>
                          <span class="info-value">{{ formatDateTime(request.applyTime) }}</span>
                        </div>
                        <div class="info-item">
                          <span class="info-label">请假原因:</span>
                          <span class="info-value reason-text">{{ request.reason }}</span>
                        </div>
                      </div>
                    </div>
                    
                    <div class="card-actions">
                      <el-button type="primary" size="small" @click="viewRequestDetail(request)">
                        <el-icon><View /></el-icon>
                        详情
                      </el-button>
                      <el-button
                        v-if="request.status === 'PENDING'"
                        type="warning"
                        size="small"
                        @click="cancelRequest(request)"
                      >
                        <el-icon><Close /></el-icon>
                        取消
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 管理员审核 -->
          <el-tab-pane label="审核管理" name="admin-review" v-if="isAdmin">
            <div class="admin-review-section">
              <!-- 搜索区域 -->
              <el-card class="search-card admin-search-card">
                <el-row :gutter="isMobile ? 12 : 16" class="search-row">
                  <el-col :xs="24" :sm="12" :md="6" :lg="5" :xl="4">
                    <div class="search-item">
                      <label class="search-label" v-if="!isMobile">申请人</label>
                      <el-input
                        v-model="adminSearch.keyword"
                        placeholder="搜索申请人姓名"
                        clearable
                        class="search-input"
                        :prefix-icon="Search"
                      />
                    </div>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="6" :lg="4" :xl="3">
                    <div class="search-item">
                      <label class="search-label" v-if="!isMobile">状态</label>
                      <el-select
                        v-model="adminSearch.status"
                        placeholder="申请状态"
                        clearable
                        class="search-select"
                      >
                        <el-option label="待审批" value="PENDING" />
                        <el-option label="已批准" value="APPROVED" />
                        <el-option label="已拒绝" value="REJECTED" />
                        <el-option label="已取消" value="CANCELLED" />
                      </el-select>
                    </div>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="6" :lg="4" :xl="3">
                    <div class="search-item">
                      <label class="search-label" v-if="!isMobile">类型</label>
                      <el-select
                        v-model="adminSearch.leaveType"
                        placeholder="请假类型"
                        clearable
                        class="search-select"
                      >
                        <el-option label="执勤请假" value="DUTY_LEAVE" />
                        <el-option label="打卡请假" value="CHECKIN_LEAVE" />
                        <el-option label="其他" value="OTHER" />
                      </el-select>
                    </div>
                  </el-col>
                  <el-col :xs="24" :sm="12" :md="6" :lg="7" :xl="8">
                    <div class="search-item">
                      <label class="search-label" v-if="!isMobile">日期范围</label>
                      <el-date-picker
                        v-model="adminSearch.dateRange"
                        type="daterange"
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        class="search-date-picker"
                        :size="isMobile ? 'default' : 'default'"
                        :teleported="true"
                        :popper-class="isMobile ? 'mobile-date-picker-popper' : 'desktop-date-picker-popper'"
                        :placement="isMobile ? 'bottom' : 'bottom-start'"
                        :clearable="true"
                        :editable="false"
                        :shortcuts="datePickerShortcuts"
                      />
                    </div>
                  </el-col>
                  <el-col :xs="24" :sm="24" :md="24" :lg="24" :xl="6">
                    <div class="search-item search-actions-wrapper">
                      <label class="search-label invisible" v-if="!isMobile">操作</label>
                      <div class="search-actions">
                        <el-button type="primary" @click="fetchAdminRequests" :icon="Search" :size="isMobile ? 'default' : 'default'">
                          <span v-if="!isMobile">搜索</span>
                        </el-button>
                        <el-button @click="resetAdminSearch" :icon="Refresh" :size="isMobile ? 'default' : 'default'">
                          <span v-if="!isMobile">重置</span>
                        </el-button>
                      </div>
                    </div>
                  </el-col>
                </el-row>
              </el-card>

              <!-- 快捷筛选 -->
              <el-card class="quick-filters-card">
                <div class="quick-filters-header">
                  <span class="filters-title">快捷筛选</span>
                </div>
                <div class="quick-filters">
                  <div class="filters-wrapper" v-if="!isMobile">
                    <el-button-group class="filters-group">
                      <el-button 
                        :type="adminSearch.status === 'PENDING' ? 'primary' : ''" 
                        @click="quickFilterAdmin('PENDING')"
                        :icon="Clock"
                        class="filter-btn"
                      >
                        待审批 ({{ pendingCount }})
                      </el-button>
                      <el-button 
                        :type="adminSearch.status === 'APPROVED' ? 'success' : ''" 
                        @click="quickFilterAdmin('APPROVED')"
                        :icon="CircleCheck"
                        class="filter-btn"
                      >
                        已批准
                      </el-button>
                      <el-button 
                        :type="adminSearch.status === 'REJECTED' ? 'danger' : ''" 
                        @click="quickFilterAdmin('REJECTED')"
                        :icon="CircleClose"
                        class="filter-btn"
                      >
                        已拒绝
                      </el-button>
                      <el-button 
                        :type="adminSearch.status === '' ? 'info' : ''" 
                        @click="quickFilterAdmin('')"
                        :icon="List"
                        class="filter-btn"
                      >
                        全部
                      </el-button>
                    </el-button-group>
                  </div>
                  
                  <!-- 移动端快捷筛选 -->
                  <div class="mobile-filters" v-else>
                    <el-row :gutter="8">
                      <el-col :span="12">
                        <el-button 
                          :type="adminSearch.status === 'PENDING' ? 'primary' : ''" 
                          @click="quickFilterAdmin('PENDING')"
                          :icon="Clock"
                          class="mobile-filter-btn"
                          block
                        >
                          待审批 ({{ pendingCount }})
                        </el-button>
                      </el-col>
                      <el-col :span="12">
                        <el-button 
                          :type="adminSearch.status === 'APPROVED' ? 'success' : ''" 
                          @click="quickFilterAdmin('APPROVED')"
                          :icon="CircleCheck"
                          class="mobile-filter-btn"
                          block
                        >
                          已批准
                        </el-button>
                      </el-col>
                    </el-row>
                    <el-row :gutter="8" style="margin-top: 8px;">
                      <el-col :span="12">
                        <el-button 
                          :type="adminSearch.status === 'REJECTED' ? 'danger' : ''" 
                          @click="quickFilterAdmin('REJECTED')"
                          :icon="CircleClose"
                          class="mobile-filter-btn"
                          block
                        >
                          已拒绝
                        </el-button>
                      </el-col>
                      <el-col :span="12">
                        <el-button 
                          :type="adminSearch.status === '' ? 'info' : ''" 
                          @click="quickFilterAdmin('')"
                          :icon="List"
                          class="mobile-filter-btn"
                          block
                        >
                          全部
                        </el-button>
                      </el-col>
                    </el-row>
                  </div>
                </div>
              </el-card>

              <!-- 申请列表 -->
              <div class="requests-list">
                <div
                  class="list-actions"
                  v-if="adminRequests.length > 0"
                >
                  <div class="selection-info">
                    已选 <strong>{{ selectedAdminIds.length }}</strong> 条申请
                  </div>
                  <el-button
                    type="danger"
                    plain
                    :icon="Delete"
                    :disabled="!hasSelectedAdminRequests || batchDeleting"
                    :loading="batchDeleting"
                    @click="handleBatchDelete"
                  >
                    批量删除
                  </el-button>
                </div>
                <SkeletonLoader v-if="adminRequestLoading" type="table" :rows="5" />
                <EmptyState v-else-if="adminRequests.length === 0" type="no-data" description="暂无请假申请" />
                
                <!-- 桌面端表格 -->
                <div class="desktop-table" v-show="!isMobile && adminRequests.length > 0">
                  <el-table
                    ref="adminTableRef"
                    :data="adminRequests"
                    stripe
                    row-key="id"
                    @selection-change="handleAdminSelectionChange"
                  >
                    <el-table-column type="selection" width="55" reserve-selection />
                    <el-table-column label="申请人" width="120">
                      <template #default="{ row }">
                        <div class="user-info">
                          <div class="user-name">{{ row.user?.realName || row.user?.username }}</div>
                          <div class="user-dept">{{ getUserDepartmentName(row.user) }}</div>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column label="请假类型" width="120">
                      <template #default="{ row }">
                        <el-tag :type="getLeaveTypeColor(row.leaveType)" size="small">
                          {{ getLeaveTypeName(row.leaveType) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="请假时间" width="180">
                      <template #default="{ row }">
                        <div class="date-info">
                          <div>{{ formatDate(row.startDate) }}</div>
                          <div v-if="row.endDate !== row.startDate" class="end-date">
                            至 {{ formatDate(row.endDate) }}
                          </div>
                          <div class="duration">共 {{ calculateDays(row.startDate, row.endDate) }} 天</div>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column label="申请状态" width="100">
                      <template #default="{ row }">
                        <el-tag :type="getStatusColor(row.status)" size="small">
                          {{ getStatusName(row.status) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="申请时间" width="120">
                      <template #default="{ row }">
                        {{ formatDateTime(row.applyTime) }}
                      </template>
                    </el-table-column>
                    <el-table-column label="请假原因" min-width="150">
                      <template #default="{ row }">
                        <el-tooltip :content="row.reason" placement="top" :disabled="row.reason.length <= 30">
                          <span class="reason-text">{{ row.reason.length > 30 ? row.reason.substring(0, 30) + '...' : row.reason }}</span>
                        </el-tooltip>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="280" fixed="right">
                      <template #default="{ row }">
                        <div class="action-buttons">
                          <el-button size="small" @click="viewRequestDetail(row)" :icon="View">
                            详情
                          </el-button>
                          <el-button 
                            v-if="row.status === 'PENDING'" 
                            size="small" 
                            type="success" 
                            @click="approveRequest(row)"
                            :icon="Check"
                          >
                            批准
                          </el-button>
                          <el-button 
                            v-if="row.status === 'PENDING'" 
                            size="small" 
                            type="danger" 
                            @click="rejectRequest(row)"
                            :icon="Close"
                          >
                            拒绝
                          </el-button>
                          <el-button 
                            size="small" 
                            type="danger" 
                            plain
                            @click="deleteRequest(row)"
                            :icon="Delete"
                          >
                            删除
                          </el-button>
                        </div>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>

                <!-- 移动端卡片 -->
                <div class="mobile-cards admin-mobile-cards" v-show="isMobile && adminRequests.length > 0">
                  <div class="admin-request-card" v-for="request in adminRequests" :key="request.id">
                    <!-- 卡片头部 -->
                    <div class="admin-card-header">
                      <div class="applicant-info">
                        <div class="applicant-main">
                          <span class="applicant-name">{{ request.user?.realName || request.user?.username }}</span>
                          <el-tag :type="getLeaveTypeColor(request.leaveType)" size="small" class="leave-type-tag">
                            {{ getLeaveTypeName(request.leaveType) }}
                          </el-tag>
                        </div>
                        <div class="applicant-dept">{{ getUserDepartmentName(request.user) }}</div>
                      </div>
                      <div class="status-section">
                        <el-tag :type="getStatusColor(request.status)" size="small" class="status-tag">
                          {{ getStatusName(request.status) }}
                        </el-tag>
                        <div class="apply-time">{{ formatDateTime(request.applyTime) }}</div>
                      </div>
                    </div>
                    
                    <!-- 卡片内容 -->
                    <div class="admin-card-content">
                      <div class="leave-details">
                        <div class="detail-row">
                          <el-icon class="detail-icon"><Clock /></el-icon>
                          <div class="detail-content">
                            <span class="detail-label">请假时间</span>
                            <div class="detail-value">
                              <span class="date-range">{{ formatDate(request.startDate) }}</span>
                              <span v-if="request.endDate !== request.startDate" class="date-to">
                                至 {{ formatDate(request.endDate) }}
                              </span>
                              <el-tag size="small" type="info" class="duration-tag">
                                {{ calculateDays(request.startDate, request.endDate) }}天
                              </el-tag>
                            </div>
                          </div>
                        </div>
                        
                        <div class="detail-row">
                          <el-icon class="detail-icon"><Document /></el-icon>
                          <div class="detail-content">
                            <span class="detail-label">请假原因</span>
                            <div class="detail-value reason-content">{{ request.reason }}</div>
                          </div>
                        </div>
                        
                        <div class="detail-row" v-if="request.contactPhone">
                          <el-icon class="detail-icon"><Phone /></el-icon>
                          <div class="detail-content">
                            <span class="detail-label">联系电话</span>
                            <div class="detail-value">{{ request.contactPhone }}</div>
                          </div>
                        </div>
                        
                        <div class="detail-row" v-if="request.emergency">
                          <el-icon class="detail-icon" style="color: #f56c6c;"><Warning /></el-icon>
                          <div class="detail-content">
                            <span class="detail-label">紧急程度</span>
                            <el-tag type="danger" size="small">紧急</el-tag>
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    <!-- 卡片操作区 -->
                    <div class="admin-card-actions">
                      <div class="primary-actions">
                        <el-button 
                          v-if="request.status === 'PENDING'" 
                          type="success" 
                          size="small"
                          @click="approveRequest(request)"
                          :icon="Check"
                          class="action-btn approve-btn"
                        >
                          批准
                        </el-button>
                        <el-button 
                          v-if="request.status === 'PENDING'" 
                          type="danger" 
                          size="small"
                          @click="rejectRequest(request)"
                          :icon="Close"
                          class="action-btn reject-btn"
                        >
                          拒绝
                        </el-button>
                      </div>
                      <div class="secondary-actions">
                        <el-button 
                          type="primary" 
                          size="small" 
                          plain
                          @click="viewRequestDetail(request)" 
                          :icon="View"
                          class="action-btn detail-btn"
                        >
                          详情
                        </el-button>
                        <el-button 
                          type="danger" 
                          size="small" 
                          plain
                          @click="deleteRequest(request)"
                          :icon="Delete"
                          class="action-btn delete-btn"
                        >
                          删除
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 分页 -->
                <div class="pagination-wrapper" v-if="adminRequests.length > 0">
                  <el-pagination
                    v-model:current-page="adminPagination.page"
                    v-model:page-size="adminPagination.size"
                    :total="adminPagination.total"
                    :page-sizes="[10, 20, 50, 100]"
                    :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
                    :small="isMobile"
                    @current-change="handleAdminPageChange"
                    @size-change="handleAdminSizeChange"
                  />
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <!-- 请假申请对话框 -->
    <el-dialog
      v-model="showLeaveDialog"
      title="请假申请"
      :width="isMobile ? '95%' : '600px'"
      :before-close="handleLeaveDialogClose"
      class="leave-dialog"
    >
      <el-form
        ref="leaveFormRef"
        :model="leaveForm"
        :rules="leaveRules"
        :label-width="isMobile ? '80px' : '100px'"
        class="leave-form"
      >
        <el-form-item label="请假类型" prop="leaveType">
          <el-select
            v-model="leaveForm.leaveType"
            placeholder="请选择请假类型"
            style="width: 100%"
            @change="handleLeaveTypeChange"
          >
            <el-option label="执勤请假" value="DUTY_LEAVE" />
            <el-option label="打卡请假" value="CHECKIN_LEAVE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        
        <!-- 打卡请假时间选择 -->
        <el-form-item v-if="leaveForm.leaveType === 'CHECKIN_LEAVE'" label="打卡配置" prop="checkinConfigurationId">
          <el-select
            v-model="leaveForm.checkinConfigurationId"
            placeholder="请选择要请假的打卡配置"
            style="width: 100%"
            @change="handleCheckinConfigChange"
          >
            <el-option
              v-for="config in checkinConfigurations"
              :key="config.id"
              :label="`${config.name} (${config.startTime} - ${config.endTime})`"
              :value="config.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="leaveForm.leaveType === 'CHECKIN_LEAVE'" label="请假日期" prop="checkinDates">
          <el-date-picker
            v-model="leaveForm.checkinDates"
            type="dates"
            placeholder="选择要请假的日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <!-- 执勤请假时间选择 -->
        <el-form-item v-if="leaveForm.leaveType === 'DUTY_LEAVE'" label="执勤排班" prop="dutyScheduleIds">
          <div class="duty-schedule-selection">
            <el-checkbox-group v-model="leaveForm.dutyScheduleIds">
              <div v-for="schedule in dutySchedules" :key="schedule.id" class="duty-schedule-item">
                <el-checkbox :value="schedule.id">
                  <div class="schedule-info">
                    <span class="schedule-day">{{ getDayName(schedule.dayOfWeek) }}</span>
                    <span class="schedule-time">{{ schedule.startTime }} - {{ schedule.endTime }}</span>
                    <span class="schedule-notes" v-if="schedule.notes">{{ schedule.notes }}</span>
                  </div>
                </el-checkbox>
              </div>
            </el-checkbox-group>
          </div>
        </el-form-item>
        
        <el-form-item v-if="leaveForm.leaveType === 'DUTY_LEAVE'" label="请假日期" prop="dutyDates">
          <el-date-picker
            v-model="leaveForm.dutyDates"
            type="dates"
            placeholder="选择要请假的日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <!-- 其他请假日期选择 -->
        <el-form-item v-if="leaveForm.leaveType === 'OTHER'" label="请假日期" prop="dateRange">
          <el-date-picker
            v-model="leaveForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="请假原因" prop="reason">
          <el-input
            v-model="leaveForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明请假原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="联系电话">
          <el-input
            v-model="leaveForm.contactPhone"
            placeholder="请输入联系电话"
            maxlength="20"
          />
        </el-form-item>
        
        <el-form-item label="是否紧急">
          <el-switch
            v-model="leaveForm.emergency"
            active-text="紧急"
            inactive-text="普通"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleLeaveDialogClose">取消</el-button>
        <el-button type="primary" @click="submitLeaveRequest" :loading="leaveSaving">
          提交申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情查看对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="请假申请详情"
      :width="isMobile ? '95%' : '600px'"
      class="detail-dialog"
    >
      <div v-if="viewingRequest" class="request-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请人">
            {{ viewingRequest.user?.realName || '未知用户' }}
          </el-descriptions-item>
          <el-descriptions-item label="所属部门">
            {{ getUserDepartmentName(viewingRequest.user) }}
          </el-descriptions-item>
          <el-descriptions-item label="请假类型">
            <el-tag :type="getLeaveTypeColor(viewingRequest.leaveType)" size="small">
              {{ getLeaveTypeName(viewingRequest.leaveType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="getStatusColor(viewingRequest.status)" size="small">
              {{ getStatusName(viewingRequest.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请假日期">
            <div class="date-range">
              {{ formatDate(viewingRequest.startDate) }}
              <span v-if="viewingRequest.endDate && viewingRequest.endDate !== viewingRequest.startDate">
                至 {{ formatDate(viewingRequest.endDate) }}
              </span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="请假天数">
            <el-tag size="small">{{ calculateDays(viewingRequest.startDate, viewingRequest.endDate) }}天</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ formatDateTime(viewingRequest.applyTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话" v-if="viewingRequest.contactPhone">
            {{ viewingRequest.contactPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="紧急程度">
            <el-tag :type="viewingRequest.emergency ? 'danger' : 'info'" size="small">
              {{ viewingRequest.emergency ? '紧急' : '普通' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审批人" v-if="viewingRequest.approver">
            {{ viewingRequest.approver.realName }}
          </el-descriptions-item>
          <el-descriptions-item label="审批时间" v-if="viewingRequest.approveTime">
            {{ formatDateTime(viewingRequest.approveTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="审批意见" :span="2" v-if="viewingRequest.approveNotes">
            {{ viewingRequest.approveNotes }}
          </el-descriptions-item>
          <el-descriptions-item label="请假原因" :span="2">
            <div class="reason-text">{{ viewingRequest.reason }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button 
          v-if="viewingRequest && viewingRequest.status === 'PENDING'"
          type="danger" 
          @click="cancelRequestFromDetail"
        >
          取消申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="showReviewDialog"
      :title="reviewAction === 'approve' ? '审批通过' : '审批拒绝'"
      :width="isMobile ? '95%' : '500px'"
      class="review-dialog"
    >
      <div v-if="reviewingRequest" class="review-form">
        <!-- 申请信息摘要 -->
        <el-card shadow="never" style="margin-bottom: 16px;">
          <div class="request-summary">
            <div class="summary-item">
              <span class="label">申请人：</span>
              <span>{{ reviewingRequest.user?.realName || '未知用户' }}</span>
            </div>
            <div class="summary-item">
              <span class="label">请假类型：</span>
              <el-tag :type="getLeaveTypeColor(reviewingRequest.leaveType)" size="small">
                {{ getLeaveTypeName(reviewingRequest.leaveType) }}
              </el-tag>
            </div>
            <div class="summary-item">
              <span class="label">请假时间：</span>
              <span>{{ formatDate(reviewingRequest.startDate) }} 至 {{ formatDate(reviewingRequest.endDate) }}</span>
            </div>
            <div class="summary-item">
              <span class="label">请假原因：</span>
              <span>{{ reviewingRequest.reason }}</span>
            </div>
          </div>
        </el-card>

        <!-- 审核表单 -->
        <el-form :model="reviewForm" :rules="reviewRules" ref="reviewFormRef" label-width="80px">
          <el-form-item label="审批结果" prop="approved">
            <el-radio-group v-model="reviewForm.approved">
              <el-radio :label="true">
                <el-icon style="color: #67c23a; margin-right: 4px;"><CircleCheck /></el-icon>
                批准
              </el-radio>
              <el-radio :label="false">
                <el-icon style="color: #f56c6c; margin-right: 4px;"><CircleClose /></el-icon>
                拒绝
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审批意见" prop="notes">
            <el-input
              v-model="reviewForm.notes"
              type="textarea"
              :rows="4"
              :placeholder="reviewForm.approved ? '请输入批准意见（可选）' : '请输入拒绝原因'"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitReview"
          :loading="reviewSubmitting"
        >
          {{ reviewForm.approved ? '批准申请' : '拒绝申请' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, View, Close, Clock,
  CircleCheck, CircleClose, Document, DocumentRemove,
  Check, List, Delete, Phone, Warning
} from '@element-plus/icons-vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import EmptyState from '@/components/EmptyState.vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

// 用户信息
const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

// 响应式屏幕尺寸检测
const windowWidth = ref(window.innerWidth)

// 计算属性：判断是否为移动设备
const isMobile = computed(() => windowWidth.value <= 768)

// 监听窗口大小变化
const handleResize = () => {
  windowWidth.value = window.innerWidth
}

// 数据状态
const activeTab = ref('my-requests')
const statsLoading = ref(false)
const myRequestLoading = ref(false)
const adminRequestLoading = ref(false)
const leaveSaving = ref(false)

// 日期选择器快捷选项
const datePickerShortcuts = [
  {
    text: '今天',
    value: () => {
      const today = new Date()
      return [today, today]
    }
  },
  {
    text: '昨天',
    value: () => {
      const yesterday = new Date()
      yesterday.setTime(yesterday.getTime() - 3600 * 1000 * 24)
      return [yesterday, yesterday]
    }
  },
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 统计数据
const stats = ref({})

// 我的申请
const myRequests = ref([])
const myRequestSearch = reactive({
  status: null,
  leaveType: null
})

// 管理员审核
const adminRequests = ref([])
const selectedAdminIds = ref([])
const batchDeleting = ref(false)
const adminTableRef = ref(null)
const adminSearch = reactive({
  keyword: '',
  status: '',
  leaveType: '',
  dateRange: null
})
const adminPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})
const pendingCount = ref(0)
const hasSelectedAdminRequests = computed(() => selectedAdminIds.value.length > 0)

const clearAdminSelection = () => {
  selectedAdminIds.value = []
  if (adminTableRef.value?.clearSelection) {
    adminTableRef.value.clearSelection()
  }
}

// 打卡配置和执勤排班数据
const checkinConfigurations = ref([])
const dutySchedules = ref([])

// 对话框状态
const showLeaveDialog = ref(false)

// 审核对话框状态
const showReviewDialog = ref(false)
const reviewingRequest = ref(null)
const reviewAction = ref('')
const reviewSubmitting = ref(false)
const reviewForm = reactive({
  approved: true,
  notes: ''
})
const reviewRules = {
  notes: [
    { 
      validator: (rule, value, callback) => {
        if (!reviewForm.approved && !value) {
          callback(new Error('拒绝申请时必须填写拒绝原因'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 请假表单
const leaveForm = reactive({
  leaveType: '',
  dateRange: null,
  checkinConfigurationId: null,
  checkinDates: [],
  dutyScheduleIds: [],
  dutyDates: [],
  reason: '',
  contactPhone: '',
  emergency: false
})

const leaveFormRef = ref()

// 表单验证规则
const leaveRules = {
  leaveType: [
    { required: true, message: '请选择请假类型', trigger: 'change' }
  ],
  dateRange: [
    { required: true, message: '请选择请假日期', trigger: 'change' }
  ],
  checkinConfigurationId: [
    { required: true, message: '请选择打卡配置', trigger: 'change' }
  ],
  checkinDates: [
    { required: true, message: '请选择请假日期', trigger: 'change' }
  ],
  dutyScheduleIds: [
    { required: true, message: '请选择执勤排班', trigger: 'change' }
  ],
  dutyDates: [
    { required: true, message: '请选择请假日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入请假原因', trigger: 'blur' },
    { min: 10, message: '请假原因至少10个字符', trigger: 'blur' }
  ]
}

// 获取统计数据
const fetchStats = async () => {
  statsLoading.value = true
  try {
    // 管理员获取全局统计，普通用户获取个人统计
    const endpoint = isAdmin.value ? '/leave-requests/statistics/global' : '/leave-requests/statistics'
    const response = await request.get(endpoint)
    stats.value = response.data || {
      totalRequests: 0,
      pendingRequests: 0,
      approvedRequests: 0,
      rejectedRequests: 0
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
    stats.value = {
      totalRequests: 0,
      pendingRequests: 0,
      approvedRequests: 0,
      rejectedRequests: 0
    }
  } finally {
    statsLoading.value = false
  }
}

// 获取我的申请
const fetchMyRequests = async () => {
  myRequestLoading.value = true
  try {
    // 构建查询参数
    const params = {
      page: 0,
      size: 1000  // 获取所有数据，后续可以加分页
    }
    
    // 添加搜索条件
    if (myRequestSearch.status) {
      params.status = myRequestSearch.status
    }
    if (myRequestSearch.leaveType) {
      params.leaveType = myRequestSearch.leaveType
    }
    
    const response = await request.get('/leave-requests/my-requests', { params })
    myRequests.value = response.data.content || []
  } catch (error) {
    console.error('获取申请列表失败:', error)
    ElMessage.error('获取申请列表失败')
    myRequests.value = []
  } finally {
    myRequestLoading.value = false
  }
}

// 切换选项卡
const handleTabChange = (tabName) => {
  if (tabName === 'my-requests') {
    fetchMyRequests()
  } else if (tabName === 'admin-review') {
    fetchAdminRequests()
    fetchPendingCount()
  }
}

// 重置搜索
const resetMyRequestSearch = () => {
  myRequestSearch.status = null
  myRequestSearch.leaveType = null
  fetchMyRequests()
}

// 管理员审核相关方法

// 获取所有申请（管理员）
const fetchAdminRequests = async () => {
  adminRequestLoading.value = true
  try {
    const params = {
      page: adminPagination.page - 1,
      size: adminPagination.size
    }
    
    // 添加搜索条件
    if (adminSearch.keyword) {
      params.keyword = adminSearch.keyword
    }
    if (adminSearch.status) {
      params.status = adminSearch.status
    }
    if (adminSearch.leaveType) {
      params.leaveType = adminSearch.leaveType
    }
    if (adminSearch.dateRange && adminSearch.dateRange.length === 2) {
      params.startDate = adminSearch.dateRange[0]
      params.endDate = adminSearch.dateRange[1]
    }
    
    const response = await request.get('/leave-requests', { params })
    adminRequests.value = response.data.content || []
    adminPagination.total = response.data.totalElements || 0
    clearAdminSelection()
  } catch (error) {
    console.error('获取申请列表失败:', error)
    ElMessage.error('获取申请列表失败')
    adminRequests.value = []
    clearAdminSelection()
  } finally {
    adminRequestLoading.value = false
  }
}

// 获取待审批数量
const fetchPendingCount = async () => {
  try {
    const response = await request.get('/leave-requests/pending')
    pendingCount.value = response.data.length || 0
  } catch (error) {
    console.error('获取待审批数量失败:', error)
    pendingCount.value = 0
  }
}

// 快捷筛选（管理员）
const quickFilterAdmin = (status) => {
  adminSearch.status = status
  adminPagination.page = 1
  fetchAdminRequests()
}

// 重置管理员搜索
const resetAdminSearch = () => {
  adminSearch.keyword = ''
  adminSearch.status = ''
  adminSearch.leaveType = ''
  adminSearch.dateRange = null
  adminPagination.page = 1
  fetchAdminRequests()
}

const handleAdminSelectionChange = (selection) => {
  selectedAdminIds.value = selection.map(item => item.id)
}

const toggleMobileSelection = (id, checked) => {
  if (checked) {
    if (!selectedAdminIds.value.includes(id)) {
      selectedAdminIds.value = [...selectedAdminIds.value, id]
    }
  } else {
    selectedAdminIds.value = selectedAdminIds.value.filter(itemId => itemId !== id)
  }
}

const handleBatchDelete = async () => {
  if (!selectedAdminIds.value.length) {
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要物理删除选中的 ${selectedAdminIds.value.length} 条请假申请吗？此操作不可恢复。`,
      '批量删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    batchDeleting.value = true
    // 使用请求体传递ID列表，避免URL中出现非法字符
    await request.delete('/leave-requests/batch', { data: selectedAdminIds.value })
    ElMessage.success('批量删除成功')
    clearAdminSelection()
    fetchAdminRequests()
    fetchPendingCount()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  } finally {
    batchDeleting.value = false
  }
}

// 分页处理
const handleAdminPageChange = (page) => {
  adminPagination.page = page
  fetchAdminRequests()
}

const handleAdminSizeChange = (size) => {
  adminPagination.size = size
  adminPagination.page = 1
  fetchAdminRequests()
}

// 审批申请
const approveRequest = (request) => {
  reviewingRequest.value = request
  reviewAction.value = 'approve'
  reviewForm.approved = true
  reviewForm.notes = ''
  showReviewDialog.value = true
}

// 拒绝申请
const rejectRequest = (request) => {
  reviewingRequest.value = request
  reviewAction.value = 'reject'
  reviewForm.approved = false
  reviewForm.notes = ''
  showReviewDialog.value = true
}

// 删除申请
const deleteRequest = async (leaveRequest) => {
  try {
    // 第一步：选择删除类型
    const { value: deleteType } = await ElMessageBox.prompt(
      `请选择删除类型：\n\n1. 软删除：隐藏申请但保留数据\n2. 物理删除：彻底删除申请及相关数据\n\n申请人：${leaveRequest.user.realName}`,
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
      ? `确定要彻底删除 ${leaveRequest.user.realName} 的请假申请吗？\n\n风险提示：此操作将永久删除该申请的所有数据，无法恢复！`
      : `确定要删除 ${leaveRequest.user.realName} 的请假申请吗？\n\n申请将被隐藏，但数据会保留。`
    
    // 第二步：确认删除
    await ElMessageBox.confirm(warningMessage, `确认${deleteTypeText}`, {
      customClass: 'delete-confirm-message-box',
      confirmButtonText: `确定${deleteTypeText}`,
      confirmButtonClass: isPhysicalDelete ? 'el-button--danger' : 'el-button--primary',
      cancelButtonText: '取消',
      type: isPhysicalDelete ? 'error' : 'warning',
      dangerouslyUseHTMLString: false
    })
    
    // 执行删除
    const endpoint = isPhysicalDelete ? `/leave-requests/${leaveRequest.id}/physical` : `/leave-requests/${leaveRequest.id}`
    console.log('正在删除请假申请:', leaveRequest.id, '删除类型:', deleteTypeText)
    
    await request.delete(endpoint)
    
    ElMessage.success(`请假申请${deleteTypeText}成功`)
    
    // 刷新列表和统计数据
    fetchAdminRequests()
    fetchPendingCount()
    fetchStats()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除申请失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 审核表单引用
const reviewFormRef = ref(null)

// 提交审核
const submitReview = async () => {
  try {
    // 表单验证
    if (reviewFormRef.value) {
      const isValid = await reviewFormRef.value.validate().catch(() => false)
      if (!isValid) {
        return
      }
    }
    
    // 如果拒绝申请但没有填写原因，手动验证
    if (!reviewForm.approved && !reviewForm.notes.trim()) {
      ElMessage.error('拒绝申请时必须填写拒绝原因')
      return
    }
    
    reviewSubmitting.value = true
    
    const reviewData = {
      status: reviewForm.approved ? 'APPROVED' : 'REJECTED',
      approveNotes: reviewForm.notes || ''
    }
    
    await request.post(`/leave-requests/${reviewingRequest.value.id}/approve`, reviewData)
    
    ElMessage.success(reviewForm.approved ? '申请已批准' : '申请已拒绝')
    showReviewDialog.value = false
    
    // 刷新列表和统计数据
    fetchAdminRequests()
    fetchPendingCount()
    fetchStats()
    
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  } finally {
    reviewSubmitting.value = false
  }
}

// 详情对话框状态
const showDetailDialog = ref(false)
const viewingRequest = ref(null)

// 查看详情
const viewRequestDetail = (request) => {
  viewingRequest.value = request
  showDetailDialog.value = true
}

// 取消申请
const cancelRequest = async (leaveRequest) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消该请假申请吗？',
      '确认取消',
      { type: 'warning' }
    )
    
    await request.post(`/leave-requests/${leaveRequest.id}/cancel`)
    
    ElMessage.success('申请已取消')
    // 重新获取申请列表和统计数据
    fetchMyRequests()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消申请失败:', error)
      ElMessage.error('取消失败')
    }
  }
}

// 从详情对话框取消申请
const cancelRequestFromDetail = async () => {
  if (!viewingRequest.value) return
  
  try {
    await ElMessageBox.confirm(
      '确定要取消该请假申请吗？',
      '确认取消',
      { type: 'warning' }
    )
    
    await request.post(`/leave-requests/${viewingRequest.value.id}/cancel`)
    
    ElMessage.success('申请已取消')
    showDetailDialog.value = false
    // 重新获取申请列表和统计数据
    fetchMyRequests()
    fetchStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消申请失败:', error)
      ElMessage.error('取消失败')
    }
  }
}

// 提交请假申请
const submitLeaveRequest = async () => {
  try {
    await leaveFormRef.value.validate()
    leaveSaving.value = true
    
    let requestData = {
      leaveType: leaveForm.leaveType,
      reason: leaveForm.reason,
      emergency: leaveForm.emergency,
      contactPhone: leaveForm.contactPhone
    }
    
    // 根据请假类型构建不同的请求数据
    if (leaveForm.leaveType === 'CHECKIN_LEAVE') {
      // 打卡请假：发送多个单独的请假申请
      const promises = leaveForm.checkinDates.map(date => {
        // 确保日期格式为 YYYY-MM-DD 字符串
        const formattedDate = date instanceof Date 
          ? date.toISOString().split('T')[0] 
          : date
        
        const requestPayload = {
          ...requestData,
          startDate: formattedDate,
          endDate: formattedDate,
          checkinConfigurationId: leaveForm.checkinConfigurationId
        }
        console.log('发送打卡请假请求:', requestPayload)
        return request.post('/leave-requests/submit', requestPayload)
      })
      await Promise.all(promises)
    } else if (leaveForm.leaveType === 'DUTY_LEAVE') {
      // 执勤请假：发送多个单独的请假申请
      const promises = leaveForm.dutyDates.map(date => {
        return request.post('/leave-requests/submit', {
          ...requestData,
          startDate: date,
          endDate: date,
          dutyScheduleIds: leaveForm.dutyScheduleIds
        })
      })
      await Promise.all(promises)
    } else {
      // 其他请假：传统的日期范围请假
      requestData.startDate = leaveForm.dateRange[0]
      requestData.endDate = leaveForm.dateRange[1]
      await request.post('/leave-requests/submit', requestData)
    }
    
    ElMessage.success('请假申请提交成功')
    handleLeaveDialogClose()
    // 重新获取我的申请列表和统计数据
    fetchMyRequests()
    fetchStats()
  } catch (error) {
    console.error('提交请假申请失败:', error)
    ElMessage.error('提交失败')
  } finally {
    leaveSaving.value = false
  }
}

// 获取打卡配置
const fetchCheckinConfigurations = async () => {
  try {
    const response = await request.get('/leave-requests/checkin-configurations')
    checkinConfigurations.value = response.data || []
  } catch (error) {
    console.error('获取打卡配置失败:', error)
    ElMessage.error('获取打卡配置失败')
  }
}

// 获取执勤排班
const fetchDutySchedules = async () => {
  try {
    const response = await request.get('/leave-requests/duty-schedules')
    dutySchedules.value = response.data || []
  } catch (error) {
    console.error('获取执勤排班失败:', error)
    ElMessage.error('获取执勤排班失败')
  }
}

// 处理请假类型变化
const handleLeaveTypeChange = () => {
  // 清空相关字段
  leaveForm.dateRange = null
  leaveForm.checkinConfigurationId = null
  leaveForm.checkinDates = []
  leaveForm.dutyScheduleIds = []
  leaveForm.dutyDates = []
  
  // 根据请假类型加载相关数据
  if (leaveForm.leaveType === 'CHECKIN_LEAVE') {
    fetchCheckinConfigurations()
  } else if (leaveForm.leaveType === 'DUTY_LEAVE') {
    fetchDutySchedules()
  }
}

// 处理打卡配置变化
const handleCheckinConfigChange = () => {
  leaveForm.checkinDates = []
}

// 关闭请假对话框
const handleLeaveDialogClose = () => {
  showLeaveDialog.value = false
  Object.assign(leaveForm, {
    leaveType: '',
    dateRange: null,
    checkinConfigurationId: null,
    checkinDates: [],
    dutyScheduleIds: [],
    dutyDates: [],
    reason: '',
    contactPhone: '',
    emergency: false
  })
  leaveFormRef.value?.clearValidate()
}

// 工具函数
const getUserDepartmentName = (user) => {
  return user?.department?.name || user?.departmentName || '无部门'
}

const getLeaveTypeName = (type) => {
  const types = {
    'DUTY_LEAVE': '执勤请假',
    'CHECKIN_LEAVE': '打卡请假',
    'OTHER': '其他'
  }
  return types[type] || type
}

const getLeaveTypeColor = (type) => {
  const colors = {
    'DUTY_LEAVE': 'primary',
    'CHECKIN_LEAVE': 'success',
    'OTHER': 'info'
  }
  return colors[type] || 'info'
}

const getDayName = (dayOfWeek) => {
  const days = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return days[dayOfWeek] || ''
}

const getStatusName = (status) => {
  const statuses = {
    'PENDING': '待审批',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消'
  }
  return statuses[status] || status
}

const getStatusColor = (status) => {
  const colors = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return colors[status] || 'info'
}

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleDateString()
}

const formatDateTime = (dateTimeStr) => {
  return new Date(dateTimeStr).toLocaleString()
}

const calculateDays = (startDate, endDate) => {
  if (!startDate || !endDate) return 0
  const start = new Date(startDate)
  const end = new Date(endDate)
  const diffTime = Math.abs(end - start)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays + 1 // 包含开始和结束日期
}

// 生命周期
onMounted(() => {
  window.addEventListener('resize', handleResize)
  fetchStats()
  fetchMyRequests()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.leave-management-container {
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

.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  border: 1px solid #e5e7eb;
  position: relative;
  overflow: hidden;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.stat-icon {
  position: absolute;
  top: 24px;
  right: 24px;
  font-size: 32px;
  color: #d1d5db;
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

.admin-search-card {
  margin-bottom: 16px;
}

.search-row {
  align-items: flex-end;
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.search-label {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  line-height: 1;
}

.search-label.invisible {
  visibility: hidden;
}

.search-input,
.search-select,
.search-date-picker {
  width: 100%;
}

.search-actions-wrapper {
  justify-content: flex-start;
}

.search-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  align-items: center;
}

.search-actions .el-button {
  min-width: 80px;
  height: 32px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* 快捷筛选样式 */
.quick-filters-card {
  margin-bottom: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.06);
}

.quick-filters-header {
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f2f5;
}

.filters-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.quick-filters {
  padding: 0;
}

.filters-wrapper {
  display: flex;
  justify-content: center;
}

.filters-group {
  display: flex;
  gap: 0;
}

.filter-btn {
  height: 36px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 0;
  border-left: none;
  padding: 8px 16px;
  transition: all 0.3s;
}

.filter-btn:first-child {
  border-left: 1px solid;
  border-top-left-radius: 6px;
  border-bottom-left-radius: 6px;
}

.filter-btn:last-child {
  border-top-right-radius: 6px;
  border-bottom-right-radius: 6px;
}

.mobile-filters {
  width: 100%;
}

.mobile-filter-btn {
  height: 36px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 6px;
}

/* 日期选择器样式优化 */
.search-date-picker {
  width: 100%;
  min-width: 240px;
}

.search-date-picker .el-input__wrapper {
  min-height: 32px;
  min-width: 240px;
  padding: 1px 8px;
}

.search-date-picker .el-input__inner {
  font-size: 13px;
  text-align: center;
  min-width: 220px;
}

.search-date-picker .el-range-input {
  font-size: 13px;
  width: 45%;
  text-align: center;
}

.search-date-picker .el-range-separator {
  font-size: 12px;
  color: #909399;
  width: 10%;
  text-align: center;
  flex-shrink: 0;
}

.search-date-picker .el-input__suffix {
  right: 8px;
}

/* 确保日期文字不被截断 */
.search-date-picker .el-range-input {
  background: transparent;
  border: none;
  outline: none;
  display: inline-block;
  text-align: center;
  vertical-align: middle;
  appearance: none;
  color: #606266;
  font-size: 13px;
  height: 28px;
  line-height: 28px;
  width: 45%;
  min-width: 85px;
}

.search-date-picker .el-range-input::placeholder {
  color: #c0c4cc;
  font-size: 12px;
}

.search-date-picker .el-range__close-icon {
  font-size: 14px;
  color: #c0c4cc;
  width: 16px;
  height: 16px;
  cursor: pointer;
  flex-shrink: 0;
}

.search-date-picker .el-input__validateIcon {
  display: none;
}

/* 日期选择器弹出层样式 */
:deep(.desktop-date-picker-popper) {
  z-index: 9999 !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15) !important;
  border-radius: 8px !important;
  border: 1px solid #e4e7ed !important;
}

:deep(.mobile-date-picker-popper) {
  z-index: 9999 !important;
  max-width: 90vw !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15) !important;
  border-radius: 8px !important;
  border: 1px solid #e4e7ed !important;
}

:deep(.mobile-date-picker-popper .el-picker-panel) {
  max-width: 90vw !important;
  overflow: hidden;
}

:deep(.mobile-date-picker-popper .el-date-range-picker__content) {
  min-width: 280px !important;
  max-width: 90vw !important;
}

:deep(.mobile-date-picker-popper .el-picker-panel__content) {
  max-width: 100% !important;
  overflow-x: auto;
}

/* 日期选择器日历样式 */
:deep(.el-date-range-picker__content .el-date-table) {
  font-size: 13px;
}

:deep(.el-date-range-picker__content .el-date-table th) {
  font-size: 12px;
  font-weight: 500;
  color: #909399;
  padding: 8px 0;
}

:deep(.el-date-range-picker__content .el-date-table td) {
  padding: 4px 0;
}

:deep(.el-date-range-picker__content .el-date-table td .el-date-table__cell) {
  width: 32px;
  height: 32px;
  line-height: 32px;
  font-size: 12px;
}

/* 快捷选项样式 */
:deep(.el-picker-panel__sidebar) {
  width: 110px !important;
  border-right: 1px solid #e4e7ed;
}

:deep(.el-picker-panel__shortcut) {
  font-size: 12px !important;
  padding: 8px 12px !important;
  margin: 2px 4px !important;
  border-radius: 4px !important;
  transition: all 0.3s ease !important;
}

:deep(.el-picker-panel__shortcut:hover) {
  background-color: #f0f9ff !important;
  color: #409eff !important;
}

.date-range {
  font-size: 13px;
  line-height: 1.4;
}

/* 执勤排班选择样式 */
.duty-schedule-selection {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
}

.duty-schedule-item {
  margin-bottom: 8px;
  padding: 8px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.duty-schedule-item:hover {
  background-color: #f5f7fa;
}

.schedule-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.schedule-day {
  font-weight: 600;
  color: #409eff;
}

.schedule-time {
  color: #606266;
  font-size: 14px;
}

.schedule-notes {
  color: #909399;
  font-size: 12px;
}

/* 详情对话框样式 */
.request-detail {
  padding: 16px 0;
}

.reason-text {
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
  max-height: 120px;
  overflow-y: auto;
}

.date-range {
  font-weight: 500;
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
  word-break: break-word;
}

.card-actions {
  padding: 12px 16px;
  border-top: 1px solid #f5f7fa;
  background: #fafbfc;
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
}

.card-actions .el-button {
  flex: 1;
  min-width: 80px;
  height: 32px;
}

/* 管理员审核卡片样式 */
.admin-mobile-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.admin-request-card {
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.3s ease;
}

.admin-request-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* 管理员卡片头部 */
.admin-card-header {
  padding: 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-bottom: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.applicant-info {
  flex: 1;
}

.applicant-main {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.applicant-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.leave-type-tag {
  font-weight: 500;
}

.applicant-dept {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.2;
}

.status-section {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.status-tag {
  font-weight: 500;
}

.apply-time {
  font-size: 11px;
  color: #9ca3af;
  white-space: nowrap;
}

/* 管理员卡片内容 */
.admin-card-content {
  padding: 16px;
}

.leave-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.detail-icon {
  color: #6b7280;
  font-size: 16px;
  margin-top: 2px;
  flex-shrink: 0;
}

.detail-content {
  flex: 1;
  min-width: 0;
}

.detail-label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
  display: block;
  margin-bottom: 4px;
}

.detail-value {
  font-size: 14px;
  color: #374151;
  line-height: 1.4;
}

.date-range {
  font-weight: 500;
  color: #1f2937;
}

.date-to {
  color: #6b7280;
  margin: 0 4px;
}

.duration-tag {
  margin-left: 8px;
  font-weight: 500;
}

.reason-content {
  word-break: break-word;
  line-height: 1.5;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

/* 管理员卡片操作区 */
.admin-card-actions {
  padding: 12px 16px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.primary-actions {
  display: flex;
  gap: 8px;
}

.secondary-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  flex: 1;
  height: 36px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  transition: all 0.3s ease;
}

.approve-btn:hover {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(103, 194, 58, 0.3);
}

.reject-btn:hover {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(245, 108, 108, 0.3);
}

.detail-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
}

.delete-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(245, 108, 108, 0.2);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .leave-management-container {
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
    padding: 16px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .stat-icon {
    font-size: 24px;
    top: 16px;
    right: 16px;
  }
  
  .search-row .el-col {
    margin-bottom: 12px;
  }
  
  .search-item {
    gap: 4px;
  }
  
  .search-label {
    display: none;
  }
  
  .search-actions-wrapper {
    justify-content: center;
  }
  
  .search-actions {
    justify-content: center;
    flex-wrap: wrap;
    gap: 10px;
    width: 100%;
  }
  
  .search-actions .el-button {
    flex: 1;
    min-width: auto;
    height: 36px;
    font-size: 14px;
  }
  
  .admin-search-card {
    margin-bottom: 12px;
  }
  
  .quick-filters-card {
    margin-bottom: 16px;
  }
  
  .quick-filters-header {
    margin-bottom: 8px;
    padding-bottom: 6px;
  }
  
  .filters-title {
    font-size: 13px;
  }
  
  .mobile-filter-btn {
    height: 32px;
    font-size: 12px;
    padding: 6px 12px;
  }
  
  /* 移动端日期选择器优化 */
  .search-date-picker {
    min-width: 100% !important;
  }
  
  .search-date-picker .el-input__wrapper {
    min-height: 36px;
    min-width: 100% !important;
    padding: 1px 12px;
  }
  
  .search-date-picker .el-input__inner {
    font-size: 14px;
    min-width: auto !important;
  }
  
  .search-date-picker .el-range-input {
    font-size: 14px;
    width: 42%;
  }
  
  .search-date-picker .el-range-separator {
    font-size: 13px;
    width: 16%;
  }
  
  .card-actions .el-button {
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
  .leave-dialog .el-dialog,
  .detail-dialog .el-dialog,
  .review-dialog .el-dialog {
    margin: 0;
    width: 100% !important;
    height: 100vh;
    max-height: none;
    border-radius: 0;
    display: flex;
    flex-direction: column;
  }
  
  .leave-dialog .el-dialog__body,
  .detail-dialog .el-dialog__body,
  .review-dialog .el-dialog__body {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
  }
  
  .leave-form .el-form-item {
    margin-bottom: 16px;
  }
  
  .quick-filters .el-button-group {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .quick-filters .el-button-group .el-button {
    flex: 1;
    min-width: auto;
    margin: 0;
  }
}

/* 管理员审核样式 */
.admin-review-section {
  padding: 0;
}

.quick-filters {
  margin-bottom: 16px;
}

.list-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.selection-info {
  color: #606266;
  font-size: 14px;
}

.selection-info strong {
  color: #303133;
  margin: 0 4px;
}

.mobile-select-checkbox {
  margin-left: auto;
  font-size: 12px;
  color: #606266;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 2px;
}

.user-dept {
  font-size: 12px;
  color: #6b7280;
}

.date-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.end-date {
  font-size: 12px;
  color: #6b7280;
}

.duration {
  font-size: 12px;
  color: #059669;
  font-weight: 500;
}

.reason-text {
  line-height: 1.4;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 审核对话框样式 */
.review-form {
  padding: 0;
}

.request-summary {
  padding: 16px;
  background: #f9fafb;
  border-radius: 6px;
}

.summary-item {
  display: flex;
  margin-bottom: 8px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-item .label {
  font-weight: 500;
  color: #374151;
  width: 80px;
  flex-shrink: 0;
}

/* 移动端卡片样式增强 */
.request-card .card-header .user-info {
  flex: 1;
}

.request-card .card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
    justify-content: center;
  }
  
  .quick-filters .el-button-group .el-button {
    padding: 8px 12px;
    font-size: 12px;
  }
  
  /* 超小屏幕日期选择器优化 */
  :deep(.mobile-date-picker-popper) {
    max-width: 95vw !important;
    left: 2.5vw !important;
    right: 2.5vw !important;
    transform: none !important;
  }
  
  :deep(.mobile-date-picker-popper .el-date-range-picker__content) {
    min-width: 260px !important;
    max-width: 95vw !important;
  }
  
  :deep(.mobile-date-picker-popper .el-picker-panel__sidebar) {
    width: 90px !important;
  }
  
  :deep(.mobile-date-picker-popper .el-picker-panel__shortcut) {
    font-size: 11px !important;
    padding: 6px 8px !important;
  }
  
  :deep(.mobile-date-picker-popper .el-date-range-picker__content .el-date-table td .el-date-table__cell) {
    width: 28px;
    height: 28px;
    line-height: 28px;
    font-size: 11px;
  }
  
  /* 超小屏幕日期输入框优化 */
  .search-date-picker .el-range-input {
    width: 40% !important;
    min-width: 75px !important;
    font-size: 12px !important;
  }
  
  .search-date-picker .el-range-separator {
    width: 20% !important;
    font-size: 12px !important;
  }
  
  .search-date-picker .el-input__wrapper {
    padding: 1px 8px !important;
  }
}
</style>
