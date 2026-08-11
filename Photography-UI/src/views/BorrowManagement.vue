<template>
  <div class="borrow-management">
    <div class="page-header">
      <h1 class="page-title">借还管理</h1>
      <p class="page-subtitle">管理设备借用申请、审核和归还流程</p>
    </div>
    
    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <!-- 搜索条件 -->
      <el-row :gutter="16" class="search-row">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户名/设备名"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-select 
            v-model="searchForm.status" 
            placeholder="借用状态" 
            clearable
            style="width: 100%"
          >
            <el-option label="待审核" value="PENDING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已借出" value="BORROWED" />
            <el-option label="已归还" value="RETURNED" />
          </el-select>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="8" :lg="12">
          <div class="basic-actions">
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              <span v-if="!isMobile">搜索</span>
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              <span v-if="!isMobile">重置</span>
            </el-button>
          </div>
        </el-col>
      </el-row>
      
      <!-- 功能操作按钮 -->
      <el-row :gutter="16" class="action-buttons-row">
        <el-col :span="24">
          <div class="action-buttons">
            <el-button type="success" @click="showBorrowDialog = true">
              <el-icon><Plus /></el-icon>
              <span>申请借用</span>
            </el-button>
            <el-button v-if="userStore.isAdmin" type="warning" @click="showExportDialog = true">
              <el-icon><Download /></el-icon>
              <span>导出Excel</span>
            </el-button>
            <el-button v-if="userStore.isAdmin" type="danger" plain @click="showCleanupDialog = true">
              <el-icon><Delete /></el-icon>
              <span>数据清理</span>
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 借用记录列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">
            <el-icon><Box /></el-icon>
            借用记录 (共 {{ pagination.total }} 条)
          </span>
        </div>
      </template>
      
      <!-- 桌面端表格视图 -->
      <el-table
        v-loading="loading"
        :data="borrowList"
        stripe
        style="width: 100%"
        class="desktop-table"
        empty-text=""
        element-loading-text="正在加载借用记录..."
        element-loading-background="rgba(255, 255, 255, 0.8)"
      >
        <!-- 自定义空状态 -->
        <template #empty>
          <EmptyState 
            :type="searchForm.keyword || searchForm.status ? 'no-search' : 'no-data'"
            :title="searchForm.keyword || searchForm.status ? '无搜索结果' : '暂无借用记录'"
            :description="searchForm.keyword || searchForm.status ? '没有找到匹配的借用记录，请尝试其他搜索条件' : '还没有任何借用记录，点击申请借用开始'"
            :action="!searchForm.keyword && !searchForm.status ? '申请借用' : ''"
            size="small"
            @action="showBorrowDialog = true"
          />
        </template>
        <el-table-column prop="id" label="编号" width="80" />
        
        <el-table-column label="用户信息" min-width="150">
          <template #default="{ row }">
            <div class="user-info">
              <div class="user-name">{{ row.user?.realName || row.user?.username }}</div>
              <div class="user-dept">{{ row.user?.departmentName || '未分配部门' }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="设备信息" min-width="200">
          <template #default="{ row }">
            <div class="equipment-info">
              <div class="equipment-name">{{ row.equipment?.name }}</div>
              <div class="equipment-detail">
                <span class="equipment-category">{{ row.equipment?.category }}</span>
                <span class="equipment-serial">{{ row.equipment?.serialNumber }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="quantity" label="数量" width="80" />
        
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="purpose" label="借用目的" min-width="150" show-overflow-tooltip />
        
        <el-table-column label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <!-- 待审核状态 - 管理员可审核，用户可取消 -->
            <template v-if="row.status === 'PENDING'">
              <el-button 
                v-if="userStore.isAdmin" 
                type="text" 
                size="small" 
                @click="handleApprove(row)"
              >
                <el-icon><Check /></el-icon>
                审核
              </el-button>
              <el-button 
                v-if="!userStore.isAdmin && row.user?.id === userStore.userInfo?.id" 
                type="text" 
                size="small" 
                class="text-warning"
                @click="handleCancel(row)"
              >
                <el-icon><Close /></el-icon>
                取消
              </el-button>
            </template>
            
            <!-- 已批准状态 - 管理员可标记为已借出 -->
            <el-button 
              v-if="row.status === 'APPROVED' && userStore.isAdmin" 
              type="text" 
              size="small" 
              @click="handleBorrow(row)"
            >
              <el-icon><Box /></el-icon>
              借出
            </el-button>
            
            <!-- 已借出状态 - 可归还 -->
            <el-button 
              v-if="row.status === 'BORROWED'" 
              type="text" 
              size="small" 
              @click="handleReturn(row)"
            >
              <el-icon><Check /></el-icon>
              归还
            </el-button>
            
            <!-- 查看详情 -->
            <el-button type="text" size="small" @click="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            
            <!-- 删除记录 - 仅管理员可删除已完成或已拒绝的记录 -->
            <el-button 
              v-if="userStore.isAdmin && (row.status === 'RETURNED' || row.status === 'REJECTED')" 
              type="text" 
              size="small" 
              class="text-danger"
              @click="handleDelete(row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 移动端卡片视图 -->
      <div class="mobile-cards">
        <!-- 骨架屏加载状态 -->
        <template v-if="loading">
          <SkeletonLoader 
            v-for="i in 5" 
            :key="i" 
            type="card" 
          />
        </template>
        
        <!-- 空状态 -->
        <EmptyState 
          v-else-if="borrowList.length === 0"
          :type="searchForm.keyword || searchForm.status ? 'no-search' : 'no-data'"
          :title="searchForm.keyword || searchForm.status ? '无搜索结果' : '暂无借用记录'"
          :description="searchForm.keyword || searchForm.status ? '没有找到匹配的借用记录，请尝试其他搜索条件' : '还没有任何借用记录，点击申请借用开始'"
          :action="!searchForm.keyword && !searchForm.status ? '申请借用' : ''"
          @action="showBorrowDialog = true"
        />
        
        <!-- 借用记录卡片列表 -->
        <div
          v-else
          v-for="record in borrowList"
          :key="record.id"
          class="borrow-card card-hover"
          @click="handleViewDetail(record)"
        >
          <div class="card-header">
            <div class="record-id">#{{ record.id }}</div>
            <el-tag :type="getStatusType(record.status)" size="small">
              {{ getStatusText(record.status) }}
            </el-tag>
          </div>
          
          <div class="card-content">
            <div class="user-section">
              <div class="section-title">申请人</div>
              <div class="user-info-mobile">
                <div class="user-name">{{ record.user?.realName || record.user?.username }}</div>
                <div class="user-dept">{{ record.user?.departmentName || '未分配部门' }}</div>
              </div>
            </div>
            
            <div class="equipment-section">
              <div class="section-title">设备信息</div>
              <div class="equipment-info-mobile">
                <div class="equipment-name">{{ record.equipment?.name }}</div>
                <div class="equipment-detail">
                  <span class="equipment-category">{{ record.equipment?.category }}</span>
                  <span class="equipment-serial">{{ record.equipment?.serialNumber }}</span>
                </div>
              </div>
            </div>
            
            <div class="borrow-details">
              <div class="detail-item">
                <span class="detail-label">数量:</span>
                <span class="detail-value">{{ record.quantity }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">申请时间:</span>
                <span class="detail-value">{{ formatTime(record.createdAt) }}</span>
              </div>
              <div v-if="record.purpose" class="detail-item">
                <span class="detail-label">借用目的:</span>
                <span class="detail-value">{{ record.purpose }}</span>
              </div>
            </div>
          </div>
          
          <div class="card-actions" @click.stop>
            <!-- 待审核状态 - 管理员可审核，用户可取消 -->
            <template v-if="record.status === 'PENDING'">
              <el-button 
                v-if="userStore.isAdmin" 
                type="text" 
                size="small" 
                @click="handleApprove(record)"
              >
                <el-icon><Check /></el-icon>
              </el-button>
              <el-button 
                v-if="!userStore.isAdmin && record.user?.id === userStore.userInfo?.id" 
                type="text" 
                size="small" 
                class="text-warning"
                @click="handleCancel(record)"
              >
                <el-icon><Close /></el-icon>
              </el-button>
            </template>
            
            <!-- 已批准状态 - 管理员可标记为已借出 -->
            <el-button 
              v-if="record.status === 'APPROVED' && userStore.isAdmin" 
              type="text" 
              size="small" 
              @click="handleBorrow(record)"
            >
              <el-icon><Box /></el-icon>
            </el-button>
            
            <!-- 已借出状态 - 可归还 -->
            <el-button 
              v-if="record.status === 'BORROWED'" 
              type="text" 
              size="small" 
              @click="handleReturn(record)"
            >
              <el-icon><Check /></el-icon>
            </el-button>
            
            <!-- 查看详情 -->
            <el-button type="text" size="small" @click="handleViewDetail(record)">
              <el-icon><View /></el-icon>
            </el-button>
            
            <!-- 删除记录 - 仅管理员可删除已完成或已拒绝的记录 -->
            <el-button 
              v-if="userStore.isAdmin && (record.status === 'RETURNED' || record.status === 'REJECTED')" 
              type="text" 
              size="small" 
              class="text-danger"
              @click="handleDelete(record)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      
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
    
    <!-- 申请借用对话框 -->
    <el-dialog
      v-model="showBorrowDialog"
      width="680px"
      :close-on-click-modal="false"
      class="borrow-request-dialog"
    >
      <template #header>
        <div class="borrow-request-header">
          <div class="borrow-request-icon">
            <el-icon><Box /></el-icon>
          </div>
          <div class="borrow-request-heading">
            <h3>申请借用设备</h3>
            <p>选择设备、填写归还时间和用途，提交后等待管理员审核</p>
          </div>
        </div>
      </template>

      <el-form
        ref="borrowFormRef"
        :model="borrowForm"
        :rules="borrowRules"
        label-position="top"
        class="borrow-request-form"
      >
        <div class="borrow-form-panel">
          <el-form-item label="借用人类型" prop="borrowerType" class="borrow-form-item borrower-type-field">
            <el-segmented
              v-model="borrowForm.borrowerType"
              :options="[{ label: '本人借用', value: 'INTERNAL' }, { label: '代外部人员借用', value: 'EXTERNAL' }]"
              class="borrower-type-segmented"
              aria-label="借用人类型"
            />
            <div class="borrow-field-hint">外部借用由当前账号作为经办人，并按当前账号所属部门审核。</div>
          </el-form-item>
          <el-form-item label="选择设备" prop="equipmentId" class="borrow-form-item equipment-field">
            <el-select
              v-model="borrowForm.equipmentId"
              placeholder="请选择要借用的设备"
              style="width: 100%"
              filterable
              clearable
              no-data-text="暂无可借设备"
              class="borrow-equipment-select"
              popper-class="borrow-equipment-select-popper"
            >
              <el-option
                v-for="equipment in availableEquipment"
                :key="equipment.id"
                :label="getEquipmentOptionLabel(equipment)"
                :value="equipment.id"
                :disabled="equipment.availableQuantity <= 0"
              >
                <div class="borrow-equipment-option">
                  <div class="option-main">
                    <span class="option-name">{{ equipment.name }}</span>
                    <span class="option-available" :class="{ 'is-empty': equipment.availableQuantity <= 0 }">
                      可用 {{ equipment.availableQuantity || 0 }}
                    </span>
                  </div>
                  <div class="option-meta">
                    <span>{{ getEquipmentCategoryName(equipment) }}</span>
                    <span>{{ equipment.serialNumber || '无编号' }}</span>
                  </div>
                </div>
              </el-option>
            </el-select>

            <div v-if="selectedBorrowEquipment" class="selected-equipment-panel">
              <div class="selected-equipment-icon">
                <el-icon><Box /></el-icon>
              </div>
              <div class="selected-equipment-info">
                <div class="selected-equipment-name">{{ selectedBorrowEquipment.name }}</div>
                <div class="selected-equipment-meta">
                  <span>{{ getEquipmentCategoryName(selectedBorrowEquipment) }}</span>
                  <span>{{ selectedBorrowEquipment.serialNumber || '无编号' }}</span>
                </div>
              </div>
              <div class="selected-equipment-stock">
                <span>可借</span>
                <strong>{{ selectedBorrowEquipment.availableQuantity || 0 }}</strong>
              </div>
            </div>
          </el-form-item>

          <div class="borrow-form-grid">
            <el-form-item label="借用数量" prop="quantity" class="borrow-form-item">
              <el-input-number
                v-model="borrowForm.quantity"
                :min="1"
                :max="maxBorrowQuantity"
                :disabled="!selectedBorrowEquipment"
                style="width: 100%"
              />
              <div class="borrow-field-hint">
                {{ selectedBorrowEquipment ? `最多可借 ${maxBorrowQuantity} 件` : '请先选择设备' }}
              </div>
            </el-form-item>
            
            <el-form-item label="预期归还日期" prop="expectedReturnTime" class="borrow-form-item">
              <el-date-picker
                v-model="borrowForm.expectedReturnTime"
                type="datetime"
                placeholder="选择归还日期和时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </el-form-item>
          </div>

          <Transition name="external-borrower">
            <div v-if="borrowForm.borrowerType === 'EXTERNAL'" class="external-borrower-fields">
              <el-form-item label="外部借用类型" prop="externalBorrowerType"><el-select v-model="borrowForm.externalBorrowerType" placeholder="请选择"><el-option label="学院" value="COLLEGE" /><el-option label="校内部门" value="DEPARTMENT" /><el-option label="老师" value="TEACHER" /></el-select></el-form-item>
              <el-form-item :label="externalOrganizationLabel" prop="externalOrganization">
                <el-select
                  v-if="borrowForm.externalBorrowerType === 'COLLEGE'"
                  v-model="borrowForm.externalOrganization"
                  placeholder="请选择学院"
                  filterable
                  clearable
                  no-data-text="请先在学院管理中添加学院"
                >
                  <el-option
                    v-for="college in colleges"
                    :key="college.id"
                    :label="college.name"
                    :value="college.name"
                  />
                </el-select>
                <el-input
                  v-else
                  v-model.trim="borrowForm.externalOrganization"
                  :placeholder="externalOrganizationPlaceholder"
                  maxlength="150"
                />
              </el-form-item>
              <el-form-item label="联系人姓名" prop="externalContactName"><el-input v-model.trim="borrowForm.externalContactName" maxlength="80" /></el-form-item>
              <el-form-item label="联系人手机号" prop="externalPhone"><el-input v-model.trim="borrowForm.externalPhone" maxlength="11" /></el-form-item>
              <el-form-item label="联系人QQ邮箱" prop="externalEmail"><el-input v-model.trim="borrowForm.externalEmail" type="email" maxlength="120" /></el-form-item>
            </div>
          </Transition>
          
          <el-form-item label="借用目的" prop="borrowReason" class="borrow-form-item purpose-field">
            <el-input
              v-model="borrowForm.borrowReason"
              type="textarea"
              :rows="4"
              placeholder="请说明借用设备的具体目的，例如拍摄任务、活动名称、使用场景等"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </div>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer borrow-request-footer">
          <el-button class="borrow-dialog-button secondary-action" @click="handleCancelBorrow">
            <el-icon><Close /></el-icon>
            <span>取消</span>
          </el-button>
          <el-button
            class="borrow-dialog-button primary-action"
            type="primary"
            @click="handleSubmitBorrow"
            :loading="submitting"
          >
            <el-icon><Check /></el-icon>
            <span>提交申请</span>
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 导出Excel对话框 -->
    <el-dialog
      v-model="showExportDialog"
      title="导出借用记录"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="exportFormRef"
        :model="exportForm"
        label-width="100px"
      >
        <el-form-item label="状态筛选">
          <el-select v-model="exportForm.status" placeholder="选择状态（可选）" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已借出" value="BORROWED" />
            <el-option label="已归还" value="RETURNED" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="exportForm.startDate"
            type="date"
            placeholder="选择开始日期（可选）"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="exportForm.endDate"
            type="date"
            placeholder="选择结束日期（可选）"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelExport">取消</el-button>
          <el-button type="primary" @click="handleExportExcel" :loading="exporting">
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="借用记录详情"
      width="800px"
      :close-on-click-modal="false"
      @close="handleCloseDetail"
    >
      <div v-if="detailLoading" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载详情中...</span>
      </div>
      
      <div v-else-if="detailRecord" class="detail-content">
        <!-- 基本信息 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
              <el-tag :type="getStatusType(detailRecord.status)" size="large">
                {{ getStatusText(detailRecord.status) }}
              </el-tag>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">记录编号：</span>
                <span class="detail-value">{{ detailRecord.id }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">申请时间：</span>
                <span class="detail-value">{{ formatTime(detailRecord.createdAt) }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">借用数量：</span>
                <span class="detail-value">{{ detailRecord.quantity }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">预期归还时间：</span>
                <span class="detail-value">{{ formatTime(detailRecord.expectedReturnTime) }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 用户信息 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <span class="card-title">申请人信息</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">姓名：</span>
                <span class="detail-value">{{ detailRecord.user?.realName || '未知' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">用户名：</span>
                <span class="detail-value">{{ detailRecord.user?.username || '未知' }}</span>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="detail-item">
                <span class="detail-label">所属部门：</span>
                <span class="detail-value">{{ detailRecord.user?.departmentName || '未知' }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 设备信息 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <span class="card-title">设备信息</span>
          </template>
          
          <div class="equipment-detail-content">
            <!-- 设备图片 -->
            <div class="equipment-image-section">
              <el-image
                v-if="detailRecord.equipment?.imageUrl"
                :src="getImageUrl(detailRecord.equipment.imageUrl)"
                style="width: 200px; height: 150px; border-radius: 8px;"
                fit="cover"
                :preview-src-list="[getImageUrl(detailRecord.equipment.imageUrl)]"
                preview-teleported
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon size="40"><Picture /></el-icon>
                    <p>图片加载失败</p>
                  </div>
                </template>
              </el-image>
              <div v-else class="no-equipment-image">
                <el-icon size="40"><Picture /></el-icon>
                <p>暂无设备图片</p>
              </div>
            </div>
            
            <!-- 设备信息 -->
            <div class="equipment-info-section">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">设备名称：</span>
                    <span class="detail-value">{{ detailRecord.equipment?.name || '未知' }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="detail-item">
                    <span class="detail-label">设备类别：</span>
                    <span class="detail-value">{{ detailRecord.equipment?.category || '未知' }}</span>
                  </div>
                </el-col>
                <el-col :span="24">
                  <div class="detail-item">
                    <span class="detail-label">序列号：</span>
                    <span class="detail-value">{{ detailRecord.equipment?.serialNumber || '未知' }}</span>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-card>

        <!-- 借用目的 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <span class="card-title">借用目的</span>
          </template>
          
          <div class="detail-item">
            <span class="detail-value">{{ detailRecord.purpose || detailRecord.borrowReason || '未填写' }}</span>
          </div>
        </el-card>

        <!-- 审核信息 -->
        <el-card 
          v-if="detailRecord.status !== 'PENDING'" 
          class="detail-section" 
          shadow="never"
        >
          <template #header>
            <span class="card-title">审核信息</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">审核人：</span>
                <span class="detail-value">{{ detailRecord.approvedByName || '未知' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">审核时间：</span>
                <span class="detail-value">{{ formatTime(detailRecord.approvalTime) }}</span>
              </div>
            </el-col>
            <el-col :span="24" v-if="detailRecord.approvalNotes">
              <div class="detail-item">
                <span class="detail-label">审核备注：</span>
                <span class="detail-value">{{ detailRecord.approvalNotes }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 归还信息 -->
        <el-card 
          v-if="detailRecord.status === 'RETURNED'" 
          class="detail-section" 
          shadow="never"
        >
          <template #header>
            <span class="card-title">归还信息</span>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="detail-item">
                <span class="detail-label">实际归还时间：</span>
                <span class="detail-value">{{ formatTime(detailRecord.actualReturnTime) }}</span>
              </div>
            </el-col>
            <el-col :span="24" v-if="detailRecord.returnNotes">
              <div class="detail-item">
                <span class="detail-label">归还备注：</span>
                <span class="detail-value">{{ detailRecord.returnNotes }}</span>
              </div>
            </el-col>
            <el-col :span="24" v-if="detailRecord.damageDescription">
              <div class="detail-item">
                <span class="detail-label">损坏描述：</span>
                <span class="detail-value text-danger">{{ detailRecord.damageDescription }}</span>
              </div>
            </el-col>
            
            <!-- 归还图片展示 -->
            <el-col :span="24" v-if="getReturnImages(detailRecord).length > 0">
              <div class="detail-item">
                <span class="detail-label">归还时状态图片：</span>
                <div class="return-images-grid">
                  <div 
                    v-for="(imageUrl, index) in getReturnImages(detailRecord)" 
                    :key="index"
                    class="return-image-item"
                  >
                    <el-image
                      :src="getImageUrl(imageUrl)"
                      :preview-src-list="getReturnImages(detailRecord).map(url => getImageUrl(url))"
                      :initial-index="index"
                      preview-teleported
                      fit="cover"
                      class="return-image"
                    >
                      <template #error>
                        <div class="image-error">
                          <el-icon size="30"><Picture /></el-icon>
                          <p>加载失败</p>
                        </div>
                      </template>
                    </el-image>
                    <div class="image-overlay">
                      <span class="image-index">{{ index + 1 }}</span>
                    </div>
                  </div>
                </div>
                <div class="images-tip">
                  <el-icon><InfoFilled /></el-icon>
                  <div class="tip-content">
                    <div class="tip-header">
                      <span class="tip-title">
                        {{ detailRecord.damageDescription ? '🔧' : '✅' }} 
                        {{ detailRecord.damageDescription ? '设备损坏状态记录' : '设备归还状态记录' }}
                      </span>
                      <el-tag 
                        size="small" 
                        :type="detailRecord.damageDescription ? 'danger' : 'success'" 
                        style="margin-left: 8px;"
                      >
                        {{ getReturnImages(detailRecord).length }} 张照片
                      </el-tag>
                    </div>
                    <div class="tip-description">
                      <span v-if="detailRecord.damageDescription">
                        📍 这些照片记录了设备的损坏情况，
                      </span>
                      <span v-else>
                        📍 这些照片证明设备状态良好，
                      </span>
                      <span v-if="getReturnImages(detailRecord).length === 1">
                        点击可放大查看详情
                      </span>
                      <span v-else>
                        点击可放大浏览，支持键盘 ← → 切换
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCloseDetail">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 设备归还对话框 -->
    <el-dialog
      v-model="showReturnDialog"
      title="设备归还"
      :width="isMobile ? '95%' : '700px'"
      :close-on-click-modal="false"
      @close="handleCloseReturnDialog"
      class="return-dialog"
    >
      <div v-if="returningRecord" class="return-dialog-container">
        <!-- 设备信息 -->
        <el-card class="equipment-info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><Box /></el-icon>
              <span class="header-title">归还设备信息</span>
            </div>
          </template>
          
          <div class="equipment-info">
            <div class="equipment-basic">
              <div class="equipment-image-small">
                <el-image
                  v-if="returningRecord.equipment?.imageUrl"
                  :src="getImageUrl(returningRecord.equipment.imageUrl)"
                  style="width: 80px; height: 60px; border-radius: 6px;"
                  fit="cover"
                />
                <div v-else class="no-image-small">
                  <el-icon size="20"><Picture /></el-icon>
                </div>
              </div>
              <div class="equipment-details">
                <h4>{{ returningRecord.equipment?.name }}</h4>
                <p>{{ returningRecord.equipment?.categoryDisplayName || returningRecord.equipment?.categoryName }} | {{ returningRecord.equipment?.serialNumber }}</p>
                <p>借用数量：{{ returningRecord.quantity }} 台</p>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 归还表单 -->
        <el-form 
          ref="returnFormRef" 
          :model="returnForm" 
          :rules="returnRules" 
          :label-width="isMobile ? '100px' : '120px'"
          class="return-form"
        >
          <!-- 设备状态选择 -->
          <el-form-item label="设备状态" prop="condition" required>
            <div class="radio-group-container">
              <el-radio-group v-model="returnForm.condition" @change="handleConditionChange" class="status-radio-group">
                <el-radio label="normal" class="status-radio">
                  <div class="radio-content">
                    <el-icon color="#67c23a" class="radio-icon"><CircleCheckFilled /></el-icon>
                    <span class="radio-text">设备正常</span>
                  </div>
                </el-radio>
                <el-radio label="damaged" class="status-radio">
                  <div class="radio-content">
                    <el-icon color="#f56c6c" class="radio-icon"><CircleCloseFilled /></el-icon>
                    <span class="radio-text">设备有损坏</span>
                  </div>
                </el-radio>
              </el-radio-group>
            </div>
          </el-form-item>

          <!-- 设备当前状态图片上传 -->
          <el-form-item label="设备状态图片" prop="returnImages" required>
            <div class="image-upload-section">
              <div class="upload-description">
                <el-alert
                  :title="returnForm.condition === 'damaged' ? '请上传设备损坏情况的照片' : '请上传设备当前状态的照片'"
                  :type="returnForm.condition === 'damaged' ? 'warning' : 'info'"
                  :closable="false"
                  show-icon
                  class="upload-alert"
                />
              </div>
              
              <div class="upload-container">
                <el-upload
                  class="return-image-uploader"
                  :action="returnImageUploadUrl"
                  :headers="uploadHeaders"
                  :file-list="returnForm.returnImages"
                  :on-success="handleReturnImageUploadSuccess"
                  :on-remove="handleReturnImageRemove"
                  :before-upload="beforeReturnImageUpload"
                  :limit="returnForm.condition === 'damaged' ? 5 : 3"
                  multiple
                  list-type="picture-card"
                  accept="image/*"
                >
                  <div class="upload-content">
                    <el-icon class="upload-icon"><Plus /></el-icon>
                    <div class="upload-text">点击上传</div>
                  </div>
                </el-upload>
                
                <div class="upload-tips">
                  <p class="tip-main">{{ returnForm.condition === 'damaged' ? '最多上传5张图片' : '最多上传3张图片' }}</p>
                  <p class="tip-sub">支持 JPG、PNG 格式，单个文件不超过 20MB</p>
                </div>
              </div>
            </div>
          </el-form-item>

          <!-- 归还备注 -->
          <el-form-item 
            v-if="returnForm.condition === 'normal'"
            label="归还备注" 
            prop="returnNotes"
          >
            <el-input
              v-model="returnForm.returnNotes"
              type="textarea"
              :rows="4"
              placeholder="请填写归还备注（可选）..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <!-- 损坏描述 -->
          <el-form-item 
            v-if="returnForm.condition === 'damaged'"
            label="损坏描述" 
            prop="damageDescription"
          >
            <el-input
              v-model="returnForm.damageDescription"
              type="textarea"
              :rows="4"
              placeholder="请详细描述设备损坏情况..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showReturnDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleConfirmReturn"
            :loading="returning"
            :disabled="!canConfirmReturn"
          >
            <el-icon><Check /></el-icon>
            确认归还
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 数据清理对话框 -->
    <el-dialog
      v-model="showCleanupDialog"
      title="数据清理管理"
      width="90%"
      :max-width="1200"
      :close-on-click-modal="false"
      @open="handleCleanupDialogOpen"
      @close="handleCleanupDialogClose"
    >
      <div class="cleanup-container">
        <!-- 清理统计概览 -->
        <el-card class="stats-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>数据清理统计</h3>
              <el-button size="small" @click="fetchCleanupStatistics">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-number">{{ cleanupStats.totalDeletedRecords || 0 }}</div>
                <div class="stat-label">已删除记录总数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-number">{{ cleanupStats.deletedLastWeek || 0 }}</div>
                <div class="stat-label">一周前删除的记录</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-number">{{ cleanupStats.deletedLastMonth || 0 }}</div>
                <div class="stat-label">一个月前删除的记录</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-number">{{ formatFileSize(cleanupStats.estimatedSizeKB || 0) }}</div>
                <div class="stat-label">预计可释放空间</div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 快速清理操作 -->
        <el-card class="actions-card" shadow="hover">
          <template #header>
            <h3>快速清理操作</h3>
          </template>
          
          <div class="quick-actions">
            <el-button 
              type="danger" 
              @click="handleAutoCleanup(7)"
              :disabled="!cleanupStats.deletedLastWeek"
            >
              清理一周前的记录 ({{ cleanupStats.deletedLastWeek || 0 }} 条)
            </el-button>
            <el-button 
              type="danger" 
              @click="handleAutoCleanup(30)"
              :disabled="!cleanupStats.deletedLastMonth"
            >
              清理一个月前的记录 ({{ cleanupStats.deletedLastMonth || 0 }} 条)
            </el-button>
            <el-button 
              type="danger" 
              @click="handleAutoCleanup(90)"
              :disabled="!cleanupStats.deletedThreeMonthsAgo"
            >
              清理三个月前的记录 ({{ cleanupStats.deletedThreeMonthsAgo || 0 }} 条)
            </el-button>
            <el-button 
              type="danger" 
              plain
              @click="handleBatchPhysicalDelete"
              :disabled="selectedDeletedRecords.length === 0"
            >
              批量删除选中记录 ({{ selectedDeletedRecords.length }})
            </el-button>
          </div>
        </el-card>

        <!-- 已删除记录列表 -->
        <el-card class="records-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>已删除记录列表</h3>
              <div>
                <el-button size="small" @click="fetchDeletedRecords">
                  <el-icon><Refresh /></el-icon>
                  刷新列表
                </el-button>
              </div>
            </div>
          </template>
          
          <el-table
            :data="deletedRecords"
            v-loading="cleaning"
            element-loading-text="加载中..."
            @selection-change="selectedDeletedRecords = $event.map(item => item.id)"
            style="width: 100%"
          >
            <el-table-column type="selection" width="50" />
            <el-table-column prop="id" label="记录ID" width="80" />
            <el-table-column label="设备信息" min-width="160">
              <template #default="{ row }">
                <div class="equipment-info">
                  <div class="equipment-name">{{ row.equipmentName || '未知设备' }}</div>
                  <div class="equipment-serial">{{ row.equipmentSerial || '无编号' }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="借用用户" width="120">
              <template #default="{ row }">
                <div>{{ row.realName || row.username || '未知用户' }}</div>
              </template>
            </el-table-column>
            <el-table-column label="借用状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="删除时间" width="160">
              <template #default="{ row }">
                <div>{{ formatDateTime(row.updatedAt) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="danger"
                  size="small"
                  @click="handlePhysicalDelete(row)"
                >
                  永久删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="cleanupPagination.page"
              :page-size="cleanupPagination.size"
              :total="cleanupPagination.total"
              layout="total, prev, pager, next, sizes"
              :page-sizes="[10, 20, 50, 100]"
              @current-change="handleCleanupPageChange"
              @size-change="(size) => { cleanupPagination.size = size; fetchDeletedRecords() }"
            />
          </div>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Plus, 
  Check, 
  Close, 
  Box, 
  View, 
  Delete, 
  Download,
  Loading,
  Picture,
  CircleCheckFilled,
  CircleCloseFilled,
  InfoFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import EmptyState from '@/components/EmptyState.vue'

const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const isMobile = ref(false)
const showBorrowDialog = ref(false)
const showExportDialog = ref(false)
const showCleanupDialog = ref(false)
const exporting = ref(false)
const cleaning = ref(false)
const borrowList = ref([])
const availableEquipment = ref([])
const colleges = ref([])

// 数据清理相关
const cleanupStats = ref({})
const deletedRecords = ref([])
const selectedDeletedRecords = ref([])
const cleanupPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: ''
})

// 归还表单
const returnForm = reactive({
  condition: 'normal', // 'normal' | 'damaged'
  returnNotes: '',
  damageDescription: '',
  returnImages: [] // 上传的图片列表
})

// 归还表单验证规则
const returnRules = {
  condition: [
    { required: true, message: '请选择设备状态', trigger: 'change' }
  ],
  returnImages: [
    { 
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请上传设备状态图片'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  damageDescription: [
    { 
      validator: (rule, value, callback) => {
        if (returnForm.condition === 'damaged' && (!value || value.trim().length < 5)) {
          callback(new Error('请详细描述设备损坏情况（至少5个字符）'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 借用表单
const borrowForm = reactive({
  equipmentId: '',
  quantity: 1,
  expectedReturnTime: '',
  borrowReason: '',
  borrowerType: 'INTERNAL',
  externalBorrowerType: '',
  externalOrganization: '',
  externalContactName: '',
  externalPhone: '',
  externalEmail: ''
})

// 导出表单
const exportForm = reactive({
  status: '',
  startDate: '',
  endDate: ''
})

// 上传配置
const returnImageUploadUrl = computed(() => {
  return returningRecord.value ? `${request.defaults.baseURL}/borrows/${returningRecord.value.id}/return-images` : ''
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const getEquipmentCategoryName = (equipment) => {
  if (!equipment) return '未分类'

  if (equipment.categoryDisplayName) return equipment.categoryDisplayName
  if (equipment.categoryName) return equipment.categoryName

  const category = equipment.category
  if (typeof category === 'string') return category || '未分类'
  if (category && typeof category === 'object') {
    return category.name || category.categoryName || category.displayName || category.description || '未分类'
  }

  return '未分类'
}

const getEquipmentOptionLabel = (equipment) => {
  if (!equipment) return ''
  return [
    equipment.name,
    getEquipmentCategoryName(equipment),
    equipment.serialNumber,
    `可用 ${equipment.availableQuantity || 0}`
  ].filter(Boolean).join(' ')
}

const selectedBorrowEquipment = computed(() => {
  return availableEquipment.value.find(equipment => String(equipment.id) === String(borrowForm.equipmentId)) || null
})

const maxBorrowQuantity = computed(() => {
  const available = Number(selectedBorrowEquipment.value?.availableQuantity || 0)
  return Math.max(1, available)
})

const externalOrganizationLabel = computed(() => {
  if (borrowForm.externalBorrowerType === 'COLLEGE') return '学院'
  if (borrowForm.externalBorrowerType === 'TEACHER') return '所属单位'
  return '学院、部门或单位'
})

const externalOrganizationPlaceholder = computed(() => {
  if (borrowForm.externalBorrowerType === 'TEACHER') return '请输入老师所属单位'
  return '请输入学院、部门或单位'
})

// 表单引用
const returnFormRef = ref()

// 是否可以确认归还
const canConfirmReturn = computed(() => {
  return returnForm.condition && returnForm.returnImages.length > 0
})

const validateBorrowQuantity = (rule, value, callback) => {
  const quantity = Number(value)
  if (!Number.isFinite(quantity) || quantity < 1) {
    callback(new Error('数量至少为1'))
    return
  }

  if (selectedBorrowEquipment.value && quantity > maxBorrowQuantity.value) {
    callback(new Error(`当前设备最多可借 ${maxBorrowQuantity.value} 件`))
    return
  }

  callback()
}

// 表单验证规则
const borrowRules = {
  borrowerType: [{ required: true, message: '请选择借用人类型', trigger: 'change' }],
  equipmentId: [
    { required: true, message: '请选择设备', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入借用数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量至少为1', trigger: 'blur' },
    { validator: validateBorrowQuantity, trigger: 'change' }
  ],
  expectedReturnTime: [
    { required: true, message: '请选择预期归还日期和时间', trigger: 'change' }
  ],
  borrowReason: [
    { required: true, message: '请填写借用目的', trigger: 'blur' },
    { min: 5, max: 200, message: '借用目的长度在 5 到 200 个字符', trigger: 'blur' }
  ],
  externalBorrowerType: [{ validator: (_rule, value, callback) => borrowForm.borrowerType !== 'EXTERNAL' || value ? callback() : callback(new Error('请选择外部借用类型')), trigger: 'change' }],
  externalOrganization: [{
    validator: (_rule, value, callback) => {
      if (borrowForm.borrowerType !== 'EXTERNAL' || value?.trim()) {
        callback()
        return
      }
      callback(new Error(borrowForm.externalBorrowerType === 'COLLEGE' ? '请选择学院' : '请填写学院、部门或单位'))
    },
    trigger: ['blur', 'change']
  }],
  externalContactName: [{ validator: (_rule, value, callback) => borrowForm.borrowerType !== 'EXTERNAL' || value?.trim() ? callback() : callback(new Error('请填写联系人姓名')), trigger: 'blur' }],
  externalPhone: [{ validator: (_rule, value, callback) => borrowForm.borrowerType !== 'EXTERNAL' || /^1[3-9]\d{9}$/.test(value) ? callback() : callback(new Error('请输入有效手机号')), trigger: 'blur' }],
  externalEmail: [{ validator: (_rule, value, callback) => borrowForm.borrowerType !== 'EXTERNAL' || /^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(value) ? callback() : callback(new Error('请输入有效邮箱')), trigger: 'blur' }]
}

const borrowFormRef = ref()

// 获取借用记录列表
const fetchBorrowRecords = async () => {
  try {
    console.log('===== 开始获取借用记录 =====')
    loading.value = true
    
    // 确保用户信息已加载
    if (!userStore.userInfo) {
      console.warn('用户信息未加载，跳过获取借用记录')
      return
    }
    
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
    
    // 根据用户角色选择不同的API端点
    console.log('用户信息:', userStore.userInfo)
    console.log('用户角色:', userStore.userInfo?.role)
    console.log('是否管理员:', userStore.isAdmin)
    
    let apiUrl = '/borrows'
    if (!userStore.isAdmin) {
      // 普通用户只能查看自己的借用记录
      apiUrl = '/borrows/my-records'
    }
    
    console.log('选择的API端点:', apiUrl)
    console.log('请求参数:', params)
    
    const response = await request.get(apiUrl, { params })
    console.log('API响应:', response)
    
    if (response.data) {
      const oldListLength = borrowList.value.length
      borrowList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
      
      console.log('数据更新前记录数量:', oldListLength)
      console.log('数据更新后记录数量:', borrowList.value.length)
      console.log('总记录数:', pagination.total)
      console.log('===== 借用记录获取完成 =====')
    }
  } catch (error) {
    console.error('获取借用记录失败:', error)
    ElMessage.error('获取借用记录失败')
  } finally {
    loading.value = false
  }
}

// 获取可用设备
const fetchAvailableEquipment = async () => {
  try {
    const response = await request.get('/equipment/available')
    
    if (response.data) {
      availableEquipment.value = response.data || []
    }
  } catch (error) {
    console.error('获取可用设备失败:', error)
  }
}

const fetchColleges = async () => {
  try {
    const response = await request.get('/colleges/list', { silent: true })
    colleges.value = response.data || []
  } catch (error) {
    console.error('获取学院列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchBorrowRecords()
}

// 重置搜索
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.page = 1
  fetchBorrowRecords()
}

// 取消借用申请
const handleCancelBorrow = () => {
  showBorrowDialog.value = false
  resetBorrowForm()
}

// 删除借用记录
const handleDelete = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除记录ID为 ${record.id} 的借用记录吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await request.delete(`/borrows/${record.id}`)
    if (response.success !== false) {
      ElMessage.success('借用记录删除成功')
      console.log('删除记录完成，刷新数据...')
      await fetchBorrowRecords()
      console.log('删除记录后数据刷新完成')
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除借用记录失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 取消导出
const handleCancelExport = () => {
  showExportDialog.value = false
  resetExportForm()
}

// 重置导出表单
const resetExportForm = () => {
  Object.keys(exportForm).forEach(key => {
    exportForm[key] = ''
  })
}

// 导出Excel
const handleExportExcel = async () => {
  try {
    exporting.value = true
    
    // 构建查询参数
    const params = {}
    if (exportForm.status) {
      params.status = exportForm.status
    }
    if (exportForm.startDate) {
      params.startDate = exportForm.startDate
    }
    if (exportForm.endDate) {
      params.endDate = exportForm.endDate
    }
    
    // 发送导出请求
    const response = await request.get('/export/borrow-records', {
      params,
      responseType: 'blob'
    })
    
    console.log('Excel导出响应:', response)
    console.log('响应数据大小:', response.data ? response.data.size : 'null')
    
    // 创建下载链接
    const blob = new Blob([response.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    
    console.log('创建的Blob大小:', blob.size)
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名
    const timestamp = new Date().toISOString().slice(0, 19).replace(/[-:T]/g, '')
    let filename = `借用记录_${timestamp}.xlsx`
    if (exportForm.status) {
      const statusText = getStatusText(exportForm.status)
      filename = `借用记录_${statusText}_${timestamp}.xlsx`
    }
    
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('Excel文件导出成功')
    showExportDialog.value = false
    resetExportForm()
    
  } catch (error) {
    console.error('导出Excel失败:', error)
    ElMessage.error('导出失败，请重试')
  } finally {
    exporting.value = false
  }
}

// 重置借用表单
const resetBorrowForm = () => {
  Object.keys(borrowForm).forEach(key => {
    if (key === 'quantity') {
      borrowForm[key] = 1
    } else if (key === 'borrowerType') {
      borrowForm[key] = 'INTERNAL'
    } else {
      borrowForm[key] = ''
    }
  })
  if (borrowFormRef.value) {
    borrowFormRef.value.clearValidate()
  }
}

// 提交借用申请
const handleSubmitBorrow = async () => {
  if (!borrowFormRef.value) return
  
  try {
    // 表单验证
    const isValid = await borrowFormRef.value.validate((valid, fields) => {
      if (!valid) {
        console.log('表单验证失败:', fields)
        return false
      }
      return true
    })
    
    if (!isValid) {
      ElMessage.warning('请完善表单信息')
      return
    }
    
    submitting.value = true
    
    // 构造请求数据
    const requestData = {
      equipmentId: borrowForm.equipmentId,
      quantity: borrowForm.quantity,
      expectedReturnTime: borrowForm.expectedReturnTime, // ISO字符串格式，后端会自动转换
      borrowReason: borrowForm.borrowReason,
      borrowerType: borrowForm.borrowerType,
      externalBorrowerType: borrowForm.borrowerType === 'EXTERNAL' ? borrowForm.externalBorrowerType : null,
      externalOrganization: borrowForm.borrowerType === 'EXTERNAL' ? borrowForm.externalOrganization : null,
      externalContactName: borrowForm.borrowerType === 'EXTERNAL' ? borrowForm.externalContactName : null,
      externalPhone: borrowForm.borrowerType === 'EXTERNAL' ? borrowForm.externalPhone : null,
      externalEmail: borrowForm.borrowerType === 'EXTERNAL' ? borrowForm.externalEmail : null
    }
    
    console.log('提交借用申请数据:', requestData)
    
    await request.post('/borrows/request', requestData)
    ElMessage.success('借用申请提交成功')
    
    showBorrowDialog.value = false
    resetBorrowForm()
    
    // 刷新数据
    console.log('借用申请提交成功，刷新数据...')
    await fetchBorrowRecords()
    console.log('数据刷新完成')
  } catch (error) {
    console.error('提交借用申请失败:', error)
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('提交申请失败')
    }
  } finally {
    submitting.value = false
  }
}

// 审核申请
const handleApprove = async (record) => {
  try {
    const { value: result } = await ElMessageBox.prompt('请填写审核意见', '审核借用申请', {
      confirmButtonText: '批准',
      cancelButtonText: '拒绝',
      inputPlaceholder: '审核备注',
      showCancelButton: true,
      distinguishCancelAndClose: true
    })
    
    await request.put(`/borrows/${record.id}/approve`, {
      approved: true,
      approvalNotes: result
    })
    
    ElMessage.success('审核完成')
    console.log('审核完成，刷新数据...')
    await fetchBorrowRecords()
    console.log('审核后数据刷新完成')
  } catch (action) {
    if (action === 'cancel') {
      // 拒绝申请
      try {
        const { value: reason } = await ElMessageBox.prompt('请填写拒绝原因', '拒绝借用申请', {
          confirmButtonText: '确定拒绝',
          inputPlaceholder: '拒绝原因',
          inputValidator: (value) => {
            if (!value) {
              return '请填写拒绝原因'
            }
            return true
          }
        })
        
        await request.put(`/borrows/${record.id}/approve`, {
          approved: false,
          approvalNotes: reason
        })
        
        ElMessage.success('已拒绝申请')
        console.log('拒绝申请完成，刷新数据...')
        await fetchBorrowRecords()
        console.log('拒绝申请后数据刷新完成')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('拒绝申请失败:', error)
        }
      }
    }
  }
}

// 标记为已借出
const handleBorrow = async (record) => {
  try {
    await ElMessageBox.confirm('确定标记为已借出吗？', '确认操作', {
      type: 'info'
    })
    
    await request.put(`/borrows/${record.id}/borrow`)
    ElMessage.success('已标记为借出')
    console.log('标记借出完成，刷新数据...')
    await fetchBorrowRecords()
    console.log('标记借出后数据刷新完成')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记借出失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 归还设备 - 打开归还对话框
const handleReturn = (record) => {
  returningRecord.value = record
  resetReturnForm()
  showReturnDialog.value = true
}

// 重置归还表单
const resetReturnForm = () => {
  returnForm.condition = 'normal'
  returnForm.returnNotes = ''
  returnForm.damageDescription = ''
  returnForm.returnImages = []
  if (returnFormRef.value) {
    returnFormRef.value.clearValidate()
  }
}

// 关闭归还对话框
const handleCloseReturnDialog = () => {
  resetReturnForm()
  returningRecord.value = null
}

// 设备状态改变时的处理
const handleConditionChange = (value) => {
  // 清空对应的表单字段
  if (value === 'normal') {
    returnForm.damageDescription = ''
  } else {
    returnForm.returnNotes = ''
  }
  // 重新验证
  if (returnFormRef.value) {
    returnFormRef.value.clearValidate(['returnNotes', 'damageDescription'])
  }
}

// 上传前检查
const beforeReturnImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt20M = file.size / 1024 / 1024 < 20
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt20M) {
    ElMessage.error('图片大小不能超过 20MB')
    return false
  }
  return true
}

// 图片上传成功
const handleReturnImageUploadSuccess = (response, file) => {
  console.log('归还图片上传成功:', response, file)
  if (response.success && response.data) {
    // 添加到图片列表
    const imageInfo = {
      uid: file.uid,
      name: file.name,
      url: response.data.imageUrl || response.data,
      status: 'done'
    }
    returnForm.returnImages.push(imageInfo)
    ElMessage.success('图片上传成功')
    
    // 触发表单验证
    if (returnFormRef.value) {
      returnFormRef.value.validateField('returnImages')
    }
  } else {
    ElMessage.error('图片上传失败')
  }
}

// 删除上传的图片
const handleReturnImageRemove = (file) => {
  const index = returnForm.returnImages.findIndex(img => img.uid === file.uid)
  if (index > -1) {
    returnForm.returnImages.splice(index, 1)
    // 触发表单验证
    if (returnFormRef.value) {
      returnFormRef.value.validateField('returnImages')
    }
  }
}

// 确认归还
const handleConfirmReturn = async () => {
  if (!returnFormRef.value || !returningRecord.value) return
  
  try {
    // 表单验证
    await returnFormRef.value.validate()
    
    returning.value = true
    
    // 构造归还数据
    const returnData = {
      condition: returnForm.condition,
      returnNotes: returnForm.condition === 'normal' ? returnForm.returnNotes : '设备有损坏',
      damageDescription: returnForm.condition === 'damaged' ? returnForm.damageDescription : '',
      returnImages: returnForm.returnImages.map(img => img.url)
    }
    
    await request.put(`/borrows/${returningRecord.value.id}/return`, returnData)
    
    ElMessage.success('设备归还成功')
    showReturnDialog.value = false
    await fetchBorrowRecords()
  } catch (error) {
    console.error('归还失败:', error)
    ElMessage.error('归还失败')
  } finally {
    returning.value = false
  }
}

// 取消申请
const handleCancel = async (record) => {
  try {
    await ElMessageBox.confirm('确定要取消这个借用申请吗？', '确认取消', {
      type: 'warning'
    })
    
    await request.put(`/borrows/${record.id}/cancel`)
    ElMessage.success('申请已取消')
    console.log('取消申请完成，刷新数据...')
    await fetchBorrowRecords()
    console.log('取消申请后数据刷新完成')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消申请失败:', error)
      ElMessage.error('取消失败')
    }
  }
}

// 查看详情
const showDetailDialog = ref(false)
const detailRecord = ref(null)
const showReturnDialog = ref(false)
const returningRecord = ref(null)
const returning = ref(false)
const detailLoading = ref(false)

const handleViewDetail = async (record) => {
  try {
    detailLoading.value = true
    showDetailDialog.value = true
    
    // 调用后端API获取详细信息
    const response = await request.get(`/borrows/${record.id}`)
    detailRecord.value = response.data
    
    console.log('借用记录详情:', response.data)
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败: ' + (error.response?.data?.message || error.message))
    showDetailDialog.value = false
  } finally {
    detailLoading.value = false
  }
}

// 关闭详情对话框
const handleCloseDetail = () => {
  showDetailDialog.value = false
  detailRecord.value = null
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'BORROWED': 'primary',
    'RETURNED': 'info',
    'OVERDUE': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待审核',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'BORROWED': '已借出',
    'RETURNED': '已归还',
    'OVERDUE': '已逾期'
  }
  return textMap[status] || '未知'
}

// 分页处理
const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1
  fetchBorrowRecords()
}

const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchBorrowRecords()
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN')
}

// 获取图片URL：根据路径判断图片类型
const getImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) return imageUrl
  
  const clean = imageUrl.trim()
  
  // 如果路径包含 returns，使用 returns 接口
  if (clean.includes('/returns/')) {
    const fileName = clean.split('/').pop()
    return `/api/images/returns/${fileName}`
  }
  
  // 如果路径包含 equipment，使用 equipment 接口
  if (clean.includes('/equipment/')) {
    const fileName = clean.split('/').pop()
    return `/api/images/equipment/${fileName}`
  }
  
  // 默认使用 equipment 接口（向后兼容）
  const fileName = clean.split('/').pop()
  return `/api/images/equipment/${fileName}`
}

// 获取归还图片列表
const getReturnImages = (record) => {
  if (!record || !record.returnImages) return []
  
  try {
    // 如果是字符串，尝试解析JSON
    if (typeof record.returnImages === 'string') {
      return JSON.parse(record.returnImages)
    }
    // 如果已经是数组，直接返回
    if (Array.isArray(record.returnImages)) {
      return record.returnImages
    }
  } catch (error) {
    console.error('解析归还图片数据失败:', error)
  }
  
  return []
}

// 监听用户信息变化，当用户信息加载完成后获取数据
watch(
  () => userStore.userInfo,
  (newUserInfo) => {
    if (newUserInfo) {
      fetchBorrowRecords()
    }
  },
  { immediate: true }
)

watch(
  () => borrowForm.equipmentId,
  () => {
    if (!selectedBorrowEquipment.value) {
      borrowForm.quantity = 1
      return
    }

    if (borrowForm.quantity > maxBorrowQuantity.value) {
      borrowForm.quantity = maxBorrowQuantity.value
    }
  }
)

watch(
  () => borrowForm.borrowerType,
  (borrowerType) => {
    if (borrowerType !== 'EXTERNAL') {
      borrowForm.externalBorrowerType = ''
      borrowForm.externalOrganization = ''
      borrowForm.externalContactName = ''
      borrowForm.externalPhone = ''
      borrowForm.externalEmail = ''
    }
  }
)

watch(
  () => borrowForm.externalBorrowerType,
  (newType, oldType) => {
    if (newType !== oldType) {
      borrowForm.externalOrganization = ''
    }
  }
)

// 数据清理相关方法
const fetchCleanupStatistics = async () => {
  try {
    console.log('正在获取清理统计信息...')
    const response = await request.get('/borrows/cleanup/statistics')
    console.log('统计API完整响应:', response)
    
    // 修复：正确检查响应格式，request.js已经处理了ApiResponse结构
    if (response && response.success !== false) {
      cleanupStats.value = response.data
      console.log('统计数据设置成功:', cleanupStats.value)
    } else {
      console.error('统计API返回错误:', response?.message)
      throw new Error(response?.message || '获取统计信息失败')
    }
  } catch (error) {
    console.error('获取清理统计失败:', error)
    ElMessage.error(`获取清理统计失败: ${error.message || error}`)
    throw error
  }
}

const fetchDeletedRecords = async () => {
  try {
    cleaning.value = true
    const response = await request.get('/borrows/deleted', {
      params: {
        page: cleanupPagination.page - 1,
        size: cleanupPagination.size
      }
    })
    console.log('API响应完整数据:', response)
    
    // 修复：正确检查响应格式，request.js已经处理了ApiResponse结构
    if (response && response.success !== false) {
      deletedRecords.value = response.data.content || []
      cleanupPagination.total = response.data.totalElements || 0
      console.log('成功获取已删除记录:', deletedRecords.value.length, '条')
    } else {
      console.error('API返回错误:', response?.message)
      ElMessage.error(response?.message || '获取已删除记录失败')
    }
  } catch (error) {
    console.error('获取已删除记录失败:', error)
    if (error.response) {
      console.error('错误响应状态:', error.response.status)
      console.error('错误响应数据:', error.response.data)
      ElMessage.error(`请求失败: ${error.response.status} - ${error.response.data?.message || '未知错误'}`)
    } else if (error.request) {
      console.error('请求未收到响应:', error.request)
      ElMessage.error('网络请求失败，请检查后端服务是否正常运行')
    } else {
      console.error('请求设置错误:', error.message)
      ElMessage.error(`请求错误: ${error.message}`)
    }
  } finally {
    cleaning.value = false
  }
}

const handleCleanupPageChange = (page) => {
  cleanupPagination.page = page
  fetchDeletedRecords()
}

const handlePhysicalDelete = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确定要永久删除"${record.equipment?.name || '设备'}"的借用记录吗？删除后无法恢复！`,
      '永久删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    const response = await request.delete(`/borrows/cleanup/${record.id}`)
    console.log('单个删除API响应:', response)
    
    // 修复：正确检查响应格式，request.js已经处理了ApiResponse结构
    if (response && response.success !== false) {
      ElMessage.success('记录已永久删除')
      fetchDeletedRecords()
      fetchCleanupStatistics()
    } else {
      ElMessage.error(response?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('永久删除失败:', error)
      ElMessage.error('永久删除失败')
    }
  }
}

const handleBatchPhysicalDelete = async () => {
  if (selectedDeletedRecords.value.length === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要永久删除选中的 ${selectedDeletedRecords.value.length} 条记录吗？删除后无法恢复！`,
      '批量永久删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    const response = await request.delete('/borrows/cleanup/batch', {
      data: selectedDeletedRecords.value
    })
    console.log('批量删除API响应:', response)
    
    // 修复：正确检查响应格式，request.js已经处理了ApiResponse结构
    if (response && response.success !== false) {
      ElMessage.success(`成功删除 ${selectedDeletedRecords.value.length} 条记录`)
      selectedDeletedRecords.value = []
      fetchDeletedRecords()
      fetchCleanupStatistics()
    } else {
      ElMessage.error(response?.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const handleAutoCleanup = async (daysOld) => {
  try {
    await ElMessageBox.confirm(
      `确定要自动清理 ${daysOld} 天前的已删除记录吗？此操作将永久删除这些数据！`,
      '自动清理确认',
      {
        type: 'warning',
        confirmButtonText: '确定清理',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    const response = await request.delete('/borrows/cleanup/auto', {
      params: { daysOld }
    })
    // 修复：正确检查响应格式，request.js已经处理了ApiResponse结构
    if (response && response.success !== false) {
      ElMessage.success(response.data || '自动清理完成')
      fetchDeletedRecords()
      fetchCleanupStatistics()
    } else {
      ElMessage.error(response?.message || '自动清理失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('自动清理失败:', error)
      ElMessage.error('自动清理失败')
    }
  }
}

const handleCleanupDialogOpen = async () => {
  // 添加小延迟确保用户认证状态稳定
  await new Promise(resolve => setTimeout(resolve, 100))
  
  // 检查用户权限
  if (!userStore.isAdmin) {
    ElMessage.error('您没有权限执行此操作')
    return
  }
  
  console.log('开始加载数据清理信息...')
  try {
    await fetchCleanupStatistics()
    console.log('统计信息加载完成')
    await fetchDeletedRecords()
    console.log('删除记录加载完成')
  } catch (error) {
    console.error('数据清理对话框初始化失败:', error)
    ElMessage.error('数据清理对话框初始化失败')
  }
}

const handleCleanupDialogClose = () => {
  selectedDeletedRecords.value = []
  cleanupPagination.page = 1
}

const formatFileSize = (sizeKB) => {
  if (sizeKB < 1024) {
    return `${sizeKB} KB`
  } else if (sizeKB < 1024 * 1024) {
    return `${(sizeKB / 1024).toFixed(1)} MB`
  } else {
    return `${(sizeKB / (1024 * 1024)).toFixed(1)} GB`
  }
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 组件挂载时获取设备数据
// 检查是否为移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 处理窗口大小变化
const handleResize = () => {
  checkMobile()
}

onMounted(() => {
  fetchAvailableEquipment()
  fetchColleges()
  
  // 初始检查移动端
  checkMobile()
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 清理事件监听器
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.external-borrower-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
  max-height: 360px;
  padding: 18px;
  margin-bottom: 18px;
  background: rgba(21, 133, 173, 0.06);
  border: 1px solid rgba(21, 133, 173, 0.18);
  border-radius: 6px;
  overflow: hidden;
  transform-origin: top;
  will-change: opacity, transform, max-height;
}

.external-borrower-fields .el-select { width: 100%; }

.external-borrower-enter-active,
.external-borrower-leave-active {
  overflow: hidden;
  transition:
    max-height 280ms var(--easing-ease),
    opacity 220ms var(--easing-ease),
    transform 240ms var(--easing-ease),
    padding 280ms var(--easing-ease),
    margin-bottom 280ms var(--easing-ease),
    border-color 240ms var(--easing-ease);
}

.external-borrower-enter-from,
.external-borrower-leave-to {
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
  margin-bottom: 0;
  opacity: 0;
  border-color: rgba(21, 133, 173, 0);
  transform: translateY(-8px);
}

.external-borrower-enter-to,
.external-borrower-leave-from {
  max-height: 360px;
  opacity: 1;
  transform: translateY(0);
}

@media (prefers-reduced-motion: reduce) {
  .external-borrower-fields,
  .external-borrower-enter-active,
  .external-borrower-leave-active {
    transition: none;
    transform: none;
  }
}

@media (max-width: 640px) {
  .external-borrower-fields {
    grid-template-columns: 1fr;
    max-height: 520px;
  }

  .external-borrower-enter-to,
  .external-borrower-leave-from {
    max-height: 520px;
  }
}
/* 归还对话框样式 */
.return-dialog .el-dialog__body {
  padding: 20px;
}

.return-dialog-container {
  max-height: 70vh;
  overflow-y: auto;
}

.return-form .el-form-item {
  margin-bottom: 24px;
}

.return-form .el-form-item__label {
  font-weight: 500;
  color: #606266;
}

/* 单选按钮组样式 */
.radio-group-container {
  width: 100%;
}

.status-radio-group {
  display: flex;
  gap: 20px;
  width: 100%;
}

.status-radio {
  flex: 1;
  margin-right: 0 !important;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s;
  background: #fafafa;
}

.status-radio:hover {
  border-color: #409eff;
  background: #f0f8ff;
}

.status-radio.is-checked {
  border-color: #409eff;
  background: #ecf5ff;
}

.radio-content {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.radio-icon {
  font-size: 18px;
}

.radio-text {
  font-size: 14px;
  font-weight: 500;
}

/* 图片上传区域样式 */
.image-upload-section {
  width: 100%;
}

.upload-alert {
  margin-bottom: 16px;
}

.upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.return-image-uploader {
  width: 100%;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
}

.upload-icon {
  font-size: 24px;
  color: #8c939d;
}

.upload-text {
  font-size: 14px;
  color: #8c939d;
}

.upload-tips {
  text-align: center;
  color: #909399;
}

.tip-main {
  font-size: 14px;
  margin: 0 0 4px 0;
  font-weight: 500;
}

.tip-sub {
  font-size: 12px;
  margin: 0;
}

.equipment-info-card {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}

.equipment-info-card .card-header {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.equipment-info-card .header-icon {
  margin-right: 8px;
  color: #409eff;
  font-size: 16px;
}

.equipment-info-card .header-title {
  font-size: 16px;
}

.equipment-basic {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.equipment-image-small {
  flex-shrink: 0;
}

.no-image-small {
  width: 80px;
  height: 60px;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  background: #fafafa;
}

.equipment-details {
  flex: 1;
}

.equipment-details h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.equipment-details p {
  margin: 4px 0;
  color: #606266;
  font-size: 14px;
}

.return-form {
  margin-top: 20px;
}

.return-form :deep(.el-radio) {
  display: flex;
  align-items: center;
  margin-right: 20px;
  margin-bottom: 12px;
}

.return-form :deep(.el-radio__label) {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
}

.image-upload-section {
  width: 100%;
}

.upload-description {
  margin-bottom: 16px;
}

.return-image-uploader {
  width: 100%;
}

.return-image-uploader :deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  border: 2px dashed #d9d9d9;
  background: #fafafa;
  transition: all 0.3s ease;
}

.return-image-uploader :deep(.el-upload--picture-card:hover) {
  border-color: #409eff;
  background: #f0f9ff;
}

.return-image-uploader .upload-icon {
  font-size: 28px;
  color: #8c939d;
  transition: color 0.3s ease;
}

.return-image-uploader :deep(.el-upload--picture-card:hover) .upload-icon {
  color: #409eff;
}

.return-image-uploader :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  margin: 0 8px 8px 0;
}

.return-image-uploader :deep(.el-upload__tip) {
  margin-top: 12px;
  line-height: 1.5;
}

.return-image-uploader :deep(.el-upload__tip p) {
  margin: 4px 0;
  color: #909399;
  font-size: 13px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .equipment-basic {
    flex-direction: column;
    gap: 12px;
  }
  
  .equipment-image-small,
  .no-image-small {
    width: 100%;
    max-width: 120px;
    margin: 0 auto;
  }
  
  .return-form :deep(.el-radio) {
    width: 100%;
    margin-right: 0;
  }
  
  .return-image-uploader :deep(.el-upload--picture-card) {
    width: 100px;
    height: 100px;
  }
  
  .return-image-uploader :deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 100px;
    height: 100px;
  }
}

/* 归还图片展示样式 */
.return-images-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
  margin-bottom: 12px;
}

.return-image-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 2px solid #e4e7ed;
}

.return-image-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: #409eff;
}

.return-image {
  width: 120px;
  height: 120px;
  border-radius: 6px;
  cursor: pointer;
  display: block;
}

.image-overlay {
  position: absolute;
  top: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border-radius: 12px;
  padding: 2px 6px;
  font-size: 12px;
  font-weight: 500;
}

.image-index {
  color: white;
  font-size: 12px;
}

.image-error {
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  background: #f5f7fa;
  border-radius: 6px;
}

.image-error p {
  margin: 4px 0 0 0;
  font-size: 12px;
}

.images-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #bae6fd;
  margin-top: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.images-tip .el-icon {
  color: #0ea5e9;
  font-size: 16px;
  flex-shrink: 0;
}

.images-tip .tip-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.images-tip .tip-header {
  display: flex;
  align-items: center;
  font-weight: 500;
}

.images-tip .tip-title {
  font-size: 14px;
  color: #303133;
}

.images-tip .tip-description {
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
}

.images-tip .tip-description span {
  margin-right: 2px;
}

/* 数据清理对话框样式 */
.cleanup-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-card,
.actions-card,
.records-card {
  border-radius: 8px;
}

.stats-card .el-card__header,
.actions-card .el-card__header,
.records-card .el-card__header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.stats-card .el-card__body {
  padding: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px 10px;
  border-radius: 6px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
  font-family: 'Arial', monospace;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px 20px;
}

.quick-actions .el-button {
  border-radius: 6px;
  font-weight: 500;
}

.equipment-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.equipment-name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.equipment-serial {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.records-card .el-table {
  border-radius: 6px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 16px 0;
  border-top: 1px solid #f0f0f0;
  margin-top: 16px;
}

/* 数据清理对话框响应式 */
@media (max-width: 768px) {
  .cleanup-container {
    gap: 16px;
  }

  .stats-card .el-row .el-col {
    margin-bottom: 16px;
  }

  .quick-actions {
    flex-direction: column;
    gap: 8px;
  }

  .quick-actions .el-button {
    width: 100%;
  }

  .stat-item {
    padding: 16px 8px;
  }

  .stat-number {
    font-size: 24px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .return-images-grid {
    gap: 8px;
  }
  
  .return-image-item {
    flex: 0 0 calc(50% - 4px);
  }
  
  .return-image {
    width: 100%;
    height: 100px;
  }
  
  .image-error {
    width: 100%;
    height: 100px;
  }
}

@media (max-width: 480px) {
  .return-image-item {
    flex: 0 0 100%;
  }
  
  .return-image {
    height: 120px;
  }
  
  .image-error {
    height: 120px;
  }
}
</style>

<style scoped>
.borrow-management {
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

/* 基础搜索操作 */
.basic-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
}

/* 功能操作按钮行 */
.action-buttons-row {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
}

.action-buttons .el-button {
  min-width: 110px;
  height: 36px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 6px;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.action-buttons .el-button .el-icon {
  font-size: 14px;
}

.action-buttons .el-button span {
  white-space: nowrap;
}

/* 中等屏幕优化 */
@media (max-width: 1024px) and (min-width: 769px) {
  .action-buttons {
    gap: 10px;
  }
  
  .action-buttons .el-button {
    min-width: 100px;
    font-size: 12px;
  }
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

.user-info,
.equipment-info {
  font-size: 14px;
}

.user-name,
.equipment-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.user-dept,
.equipment-detail {
  font-size: 12px;
  color: #909399;
}

.equipment-detail {
  display: flex;
  gap: 8px;
}

.equipment-category {
  background: #f0f2f5;
  padding: 2px 6px;
  border-radius: 3px;
}

.text-warning {
  color: #e6a23c !important;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .basic-actions {
    flex-direction: column;
    width: 100%;
    gap: 12px;
  }
  
  .basic-actions .el-button {
    width: 100%;
    justify-content: center;
  }
  
  .action-buttons-row {
    margin-top: 12px;
    padding-top: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 10px;
    justify-content: stretch;
  }
  
  .action-buttons .el-button {
    width: 100%;
    min-width: auto;
    height: 40px;
    font-size: 14px;
    justify-content: center;
    align-items: center;
  }
  
  /* 移动端归还弹窗优化 */
  .return-dialog .el-dialog {
    margin: 5vh auto;
  }
  
  .return-dialog .el-dialog__header {
    padding: 16px 20px 10px;
  }
  
  .return-dialog .el-dialog__body {
    padding: 10px 20px 20px;
  }
  
  .return-dialog .el-dialog__footer {
    padding: 10px 20px 20px;
  }
  
  .return-form .el-form-item {
    margin-bottom: 20px;
  }
  
  .return-form .el-form-item__label {
    font-size: 14px;
    line-height: 1.4;
  }
  
  .status-radio-group {
    flex-direction: column;
    gap: 12px;
  }
  
  .status-radio {
    padding: 12px;
  }
  
  .radio-content {
    justify-content: flex-start;
  }
  
  .upload-content {
    padding: 16px;
  }
  
  .upload-icon {
    font-size: 20px;
  }
  
  .dialog-footer {
    flex-direction: column-reverse;
    gap: 8px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
    margin: 0;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .search-row .el-col {
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .borrow-management {
    padding: 0 10px;
  }
}

/* 详情对话框样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #909399;
  font-size: 14px;
}

.loading-container .el-icon {
  font-size: 24px;
  margin-bottom: 12px;
}

.detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 16px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.detail-item {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
  min-height: 24px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  display: inline-block;
  width: 120px;
  font-weight: 500;
  color: #606266;
  font-size: 14px;
  flex-shrink: 0;
  line-height: 24px;
}

.detail-value {
  color: #303133;
  font-size: 14px;
  line-height: 24px;
  word-break: break-word;
  flex: 1;
}

/* 设备详情内容布局 */
.equipment-detail-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.equipment-image-section {
  flex-shrink: 0;
}

.equipment-info-section {
  flex: 1;
}

/* 设备图片样式 */
.no-equipment-image {
  width: 200px;
  height: 150px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: #fafafa;
}

.no-equipment-image p {
  margin: 8px 0 0 0;
  font-size: 14px;
}

.image-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #909399;
  background: #f5f7fa;
  border-radius: 8px;
}

.image-slot p {
  margin: 8px 0 0 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .equipment-detail-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .equipment-image-section {
    align-self: center;
  }
}

.text-danger {
  color: #f56c6c;
}

.text-warning {
  color: #e6a23c;
}

.text-success {
  color: #67c23a;
}

/* 移动端卡片视图 */
.mobile-cards {
  display: none;
}

.borrow-card {
  background: white;
  border-radius: 8px;
  margin-bottom: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.borrow-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.borrow-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.record-id {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  font-family: monospace;
}

.borrow-card .card-content {
  margin-bottom: 12px;
}

.user-section,
.equipment-section {
  margin-bottom: 12px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 6px;
}

.user-info-mobile {
  padding-left: 8px;
}

.user-info-mobile .user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.user-info-mobile .user-dept {
  font-size: 12px;
  color: #909399;
}

.equipment-info-mobile {
  padding-left: 8px;
}

.equipment-info-mobile .equipment-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.equipment-info-mobile .equipment-detail {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.equipment-info-mobile .equipment-category {
  font-size: 12px;
  color: #1976d2;
  background: #e3f2fd;
  padding: 2px 6px;
  border-radius: 4px;
}

.equipment-info-mobile .equipment-serial {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.borrow-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.borrow-details .detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 0;
  min-height: auto;
}

.borrow-details .detail-item:nth-child(odd) {
  grid-column: 1 / 2;
}

.borrow-details .detail-item:nth-child(even) {
  grid-column: 2 / 3;
}

.borrow-details .detail-item:last-child {
  grid-column: 1 / -1;
}

.borrow-details .detail-label {
  font-size: 12px;
  color: #606266;
  width: auto;
  flex-shrink: 0;
  line-height: 1.4;
}

.borrow-details .detail-value {
  font-size: 12px;
  color: #303133;
  line-height: 1.4;
  word-break: break-word;
}

.borrow-card .card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .borrow-management {
    padding: 0 8px;
  }
  
  .page-header {
    text-align: center;
    margin-bottom: 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  .page-subtitle {
    font-size: 14px;
  }
  
  .search-card {
    margin-bottom: 16px;
  }
  
  .search-row .el-col {
    margin-bottom: 12px;
  }
  
  .basic-actions {
    gap: 6px;
  }
  
  .basic-actions .el-button {
    font-size: 12px;
    padding: 8px 12px;
  }
  
  .action-buttons .el-button {
    height: 42px;
    font-size: 13px;
    padding: 10px 16px;
    font-weight: 500;
  }
  
  /* 手机端归还弹窗全屏优化 */
  .return-dialog .el-dialog {
    margin: 0;
    width: 100% !important;
    height: 100vh;
    max-height: none;
    border-radius: 0;
    display: flex;
    flex-direction: column;
  }
  
  .return-dialog .el-dialog__header {
    padding: 12px 16px 8px;
    border-bottom: 1px solid #ebeef5;
    flex-shrink: 0;
  }
  
  .return-dialog .el-dialog__title {
    font-size: 16px;
  }
  
  .return-dialog .el-dialog__body {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
  }
  
  .return-dialog .el-dialog__footer {
    padding: 12px 16px 16px;
    border-top: 1px solid #ebeef5;
    flex-shrink: 0;
  }
  
  .return-form .el-form-item {
    margin-bottom: 16px;
  }
  
  .return-form .el-form-item__label {
    font-size: 13px;
    text-align: left !important;
    padding-right: 8px;
  }
  
  .status-radio {
    padding: 10px;
  }
  
  .radio-text {
    font-size: 13px;
  }
  
  .upload-alert .el-alert__title {
    font-size: 13px;
  }
  
  .tip-main {
    font-size: 13px;
  }
  
  .tip-sub {
    font-size: 11px;
  }
  
  .desktop-table {
    display: none;
  }
  
  .mobile-cards {
    display: block;
  }
  
  .pagination-container {
    margin-top: 12px;
  }
  
  .pagination-container .el-pagination {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .borrow-management {
    padding: 0 4px;
  }
  
  
  .borrow-card {
    padding: 12px;
  }
  
  .borrow-details {
    grid-template-columns: 1fr;
    gap: 6px;
  }
  
  .borrow-details .detail-item:nth-child(odd),
  .borrow-details .detail-item:nth-child(even) {
    grid-column: 1;
  }
  
  .card-actions .el-button {
    padding: 4px 8px;
  }
}

/* 详情对话框响应式设计 */
@media (max-width: 768px) {
  .detail-item {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-label {
    width: auto;
    margin-bottom: 4px;
  }
  
  .el-dialog {
    width: 95% !important;
    margin: 5% auto !important;
  }
}

/* 借用申请设备选择优化 */
:deep(.borrow-request-dialog.el-dialog),
:deep(.borrow-request-dialog .el-dialog) {
  width: min(92vw, 720px) !important;
  border: 1px solid rgba(255, 255, 255, 0.72) !important;
  border-radius: 28px !important;
  background:
    radial-gradient(circle at 90% 8%, rgba(75, 211, 180, 0.14), transparent 34%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(240, 251, 255, 0.9)) !important;
  box-shadow: 0 34px 90px rgba(18, 85, 116, 0.22) !important;
  overflow: visible;
}

:deep(.borrow-request-dialog .el-dialog__header) {
  padding: 24px 28px 18px !important;
  margin: 0 !important;
  border-bottom: 1px solid rgba(98, 177, 210, 0.18);
}

:deep(.borrow-request-dialog .el-dialog__body) {
  padding: 18px 22px 0 !important;
  background: linear-gradient(180deg, rgba(240, 251, 255, 0.34), rgba(255, 255, 255, 0.18));
}

:deep(.borrow-request-dialog .el-dialog__footer) {
  padding: 16px 24px 22px !important;
  border-top: 1px solid rgba(98, 177, 210, 0.18);
  background: rgba(255, 255, 255, 0.62);
}

:deep(.borrow-request-dialog .el-dialog__headerbtn) {
  top: 22px;
  right: 22px;
  width: 34px;
  height: 34px;
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
}

.borrow-request-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-right: 44px;
}

.borrow-request-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  color: #0876a5;
  background: var(--button-primary-bg);
  border: 1px solid var(--button-primary-border);
  border-radius: 15px;
  box-shadow: 0 10px 24px rgba(24, 185, 236, 0.12);
}

.borrow-request-heading h3 {
  margin: 0;
  color: #123044;
  font-size: 22px;
  font-weight: 800;
  line-height: 1.2;
}

.borrow-request-heading p {
  margin: 5px 0 0;
  color: #6b879a;
  font-size: 13px;
}

.borrow-request-form {
  padding: 0;
}

.borrow-form-panel {
  padding: 18px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 24px;
  box-shadow: 0 12px 28px rgba(18, 174, 231, 0.07);
}

.borrow-form-grid {
  display: grid;
  grid-template-columns: minmax(0, 0.82fr) minmax(0, 1.18fr);
  gap: 14px;
}

.borrow-form-item {
  margin-bottom: 16px !important;
}

.borrow-form-item :deep(.el-form-item__label) {
  margin-bottom: 8px !important;
  color: #496579 !important;
  font-size: 13px !important;
  font-weight: 800 !important;
  line-height: 1.2 !important;
}

.borrow-form-item :deep(.el-input__wrapper),
.borrow-form-item :deep(.el-select__wrapper),
.borrow-form-item :deep(.el-textarea__inner),
.borrow-form-item :deep(.el-input-number .el-input__wrapper) {
  min-height: 44px;
  border-radius: 16px !important;
  background: rgba(255, 255, 255, 0.86) !important;
}

.borrow-form-item :deep(.el-input-number__decrease),
.borrow-form-item :deep(.el-input-number__increase) {
  background: rgba(240, 251, 255, 0.8);
  border-color: rgba(98, 177, 210, 0.2);
}

.borrower-type-segmented {
  --el-segmented-bg-color: rgba(255, 255, 255, 0.74);
  --el-segmented-color: #496579;
  --el-segmented-item-hover-bg-color: rgba(232, 249, 255, 0.88);
  --el-segmented-item-hover-color: #0876a5;
  --el-segmented-item-active-bg-color: rgba(210, 242, 252, 0.9);
  --el-segmented-item-selected-bg-color: var(--button-primary-bg);
  --el-segmented-item-selected-color: #0876a5;
  width: fit-content;
  max-width: 100%;
  min-height: 42px;
  padding: 4px;
  border: 1px solid rgba(98, 177, 210, 0.2);
  border-radius: 16px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.88), 0 8px 18px rgba(18, 174, 231, 0.07);
}

.borrower-type-segmented :deep(.el-segmented__group) {
  gap: 4px;
}

.borrower-type-segmented :deep(.el-segmented__item) {
  min-width: 108px;
  min-height: 34px;
  padding: 0 14px;
  border-radius: 12px;
  color: #496579;
  font-size: 13px;
  font-weight: 800;
  transition: background-color var(--duration-fast) var(--easing-ease), color var(--duration-fast) var(--easing-ease);
}

.borrower-type-segmented :deep(.el-segmented__item-selected) {
  border: 1px solid var(--button-primary-border);
  border-radius: 12px;
  box-shadow: 0 8px 18px rgba(24, 185, 236, 0.12);
}

.borrower-type-segmented :deep(.el-segmented__item.is-selected) {
  color: #0876a5;
}

.borrower-type-segmented :deep(.el-segmented__item.is-focus-visible .el-segmented__item-label) {
  outline: 2px solid rgba(24, 185, 236, 0.32);
  outline-offset: 4px;
  border-radius: 8px;
}

.borrower-type-segmented :deep(.el-segmented__item-label) {
  line-height: 1;
  white-space: nowrap;
}

.borrower-type-field :deep(.el-form-item__content) {
  display: flex;
  align-items: center;
  gap: 12px;
}

.borrower-type-field .borrow-field-hint {
  flex: 1 1 220px;
  margin-top: 0;
  line-height: 1.5;
}

.purpose-field {
  margin-bottom: 0 !important;
}

.purpose-field :deep(.el-textarea__inner) {
  min-height: 108px !important;
  padding: 12px 14px;
}

.borrow-equipment-select {
  width: 100%;
}

.borrow-equipment-select :deep(.el-select__wrapper) {
  min-height: 44px;
  border-radius: 16px !important;
}

.borrow-field-hint {
  margin-top: 7px;
  color: #7691a4;
  font-size: 12px;
  font-weight: 600;
}

.selected-equipment-panel {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  padding: 13px 14px;
  background:
    radial-gradient(circle at 96% 10%, rgba(75, 211, 180, 0.14), transparent 36%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(240, 251, 255, 0.76));
  border: 1px solid rgba(98, 177, 210, 0.2);
  border-radius: 18px;
  box-shadow: 0 10px 24px rgba(18, 174, 231, 0.08);
}

.selected-equipment-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: #0876a5;
  background: var(--button-primary-bg);
  border: 1px solid var(--button-primary-border);
  border-radius: 13px;
}

.selected-equipment-info {
  min-width: 0;
}

.selected-equipment-name {
  overflow: hidden;
  color: #123044;
  font-size: 15px;
  font-weight: 800;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-equipment-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 5px;
  color: #6b879a;
  font-size: 12px;
  font-weight: 600;
}

.selected-equipment-meta span {
  padding: 2px 7px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(98, 177, 210, 0.16);
  border-radius: 999px;
}

.selected-equipment-stock {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 10px;
  color: #087f63;
  background: rgba(231, 251, 244, 0.86);
  border: 1px solid rgba(33, 185, 139, 0.24);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.selected-equipment-stock strong {
  font-family: var(--font-family-mono);
  font-size: 16px;
}

.borrow-request-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.borrow-dialog-button {
  min-width: 110px !important;
  height: 42px !important;
  padding: 0 18px !important;
  border-radius: 999px !important;
  font-weight: 800 !important;
}

.borrow-dialog-button .el-icon {
  margin-right: 6px;
}

.borrow-dialog-button.secondary-action {
  color: #496579 !important;
  background: rgba(255, 255, 255, 0.78) !important;
  border: 1px solid rgba(98, 177, 210, 0.24) !important;
}

.borrow-dialog-button.primary-action {
  color: #0876a5 !important;
  background: var(--button-primary-bg) !important;
  border: 1px solid var(--button-primary-border) !important;
  box-shadow: 0 12px 28px rgba(24, 185, 236, 0.14) !important;
}

:global(.borrow-equipment-select-popper) {
  padding: 8px !important;
  border: 1px solid rgba(255, 255, 255, 0.7) !important;
  border-radius: 22px !important;
  background: rgba(255, 255, 255, 0.96) !important;
  box-shadow: 0 24px 70px rgba(18, 85, 116, 0.18) !important;
}

:global(.borrow-equipment-select-popper .el-select-dropdown__list) {
  padding: 2px !important;
}

:global(.borrow-equipment-select-popper .el-select-dropdown__item) {
  height: auto !important;
  min-height: 64px;
  margin: 4px 0;
  padding: 0 !important;
  line-height: 1.4 !important;
  border-radius: 16px;
  transition: background var(--duration-fast) var(--easing-ease), border-color var(--duration-fast) var(--easing-ease);
}

:global(.borrow-equipment-select-popper .el-select-dropdown__item.is-hovering),
:global(.borrow-equipment-select-popper .el-select-dropdown__item.is-selected) {
  background: rgba(229, 249, 255, 0.78) !important;
}

:global(.borrow-equipment-select-popper .el-select-dropdown__item.is-disabled) {
  opacity: 0.58;
}

:global(.borrow-equipment-option) {
  padding: 10px 14px;
}

:global(.borrow-equipment-option .option-main) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

:global(.borrow-equipment-option .option-name) {
  overflow: hidden;
  color: #123044;
  font-size: 14px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:global(.borrow-equipment-option .option-available) {
  flex: 0 0 auto;
  padding: 3px 8px;
  color: #087f63;
  background: rgba(231, 251, 244, 0.9);
  border: 1px solid rgba(33, 185, 139, 0.24);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

:global(.borrow-equipment-option .option-available.is-empty) {
  color: #b4233e;
  background: rgba(255, 240, 243, 0.9);
  border-color: rgba(240, 82, 104, 0.24);
}

:global(.borrow-equipment-option .option-meta) {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

:global(.borrow-equipment-option .option-meta span) {
  padding: 2px 7px;
  color: #6b879a;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(98, 177, 210, 0.14);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

@media (max-width: 640px) {
  :deep(.borrow-request-dialog.el-dialog),
  :deep(.borrow-request-dialog .el-dialog) {
    width: 94vw !important;
  }

  .borrow-request-header {
    align-items: flex-start;
  }

  .borrow-form-panel {
    padding: 14px;
  }

  .borrow-form-grid {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .borrower-type-field :deep(.el-form-item__content) {
    align-items: stretch;
    flex-direction: column;
    gap: 8px;
  }

  .borrower-type-field .borrow-field-hint {
    flex: none;
  }

  .borrower-type-segmented {
    width: 100%;
  }

  .borrower-type-segmented :deep(.el-segmented__item) {
    min-width: 0;
    padding: 0 10px;
  }

  .selected-equipment-panel {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .selected-equipment-stock {
    grid-column: 1 / -1;
    width: fit-content;
  }

  .borrow-request-footer {
    flex-direction: column-reverse;
  }

  .borrow-dialog-button {
    width: 100% !important;
  }
}
</style>
