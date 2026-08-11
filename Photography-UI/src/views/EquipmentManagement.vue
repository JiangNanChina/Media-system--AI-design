<template>
  <div class="equipment-management">
    <div class="page-header">
      <h1 class="page-title">设备管理</h1>
      <p class="page-subtitle">管理摄影器材信息、库存和状态</p>
    </div>
    
    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <div class="toolbar-layout">
        <!-- 搜索条件 -->
        <div class="filter-group">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索设备名称/序列号"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>

          <el-select 
            v-model="searchForm.category" 
            placeholder="选择分类" 
            clearable
          >
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>

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
        </div>

        <!-- 管理员操作按钮 -->
        <div v-if="userStore.isAdmin" class="admin-actions">
          <el-button
            type="success"
            @click="showCreateDialog = true"
          >
            <el-icon><Plus /></el-icon>
            <span>新增设备</span>
          </el-button>
          <el-button
            type="warning"
            @click="handleCategoryManagement"
          >
            <el-icon><Setting /></el-icon>
            <span>分类管理</span>
          </el-button>
          <el-button
            type="info"
            @click="handleExportEquipment"
          >
            <el-icon><Download /></el-icon>
            <span>导出Excel</span>
          </el-button>
          <el-button
            type="danger"
            plain
            @click="showCleanupDialog = true"
          >
            <el-icon><Delete /></el-icon>
            <span>数据清理</span>
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 设备列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">
            <el-icon><Camera /></el-icon>
            设备列表 (共 {{ pagination.total }} 条)
          </span>
        </div>
      </template>
      
      <!-- 桌面端表格视图 -->
      <el-table
        v-loading="loading"
        :data="equipmentList"
        stripe
        style="width: 100%"
        class="desktop-table"
        empty-text=""
        element-loading-text="正在加载设备数据..."
        element-loading-background="rgba(255, 255, 255, 0.8)"
      >
        <!-- 自定义空状态 -->
        <template #empty>
          <EmptyState 
            :type="searchForm.keyword || searchForm.category ? 'no-search' : 'no-data'"
            :title="searchForm.keyword || searchForm.category ? '无搜索结果' : '暂无设备'"
            :description="searchForm.keyword || searchForm.category ? '没有找到匹配的设备，请尝试其他搜索条件' : '还没有添加任何设备，点击新增设备开始'"
            :action="userStore.isAdmin && !searchForm.keyword && !searchForm.category ? '新增设备' : ''"
            size="small"
            @action="showCreateDialog = true"
          />
        </template>
        <el-table-column label="设备图片" width="100">
          <template #default="{ row }">
            <div class="equipment-image">
              <el-image
                v-if="row.imageUrl"
                :src="getImageUrl(row.imageUrl)"
                :preview-src-list="[getImageUrl(row.imageUrl)]"
                :preview-teleported="true"
                fit="cover"
                style="width: 60px; height: 60px; border-radius: 6px; cursor: pointer;"
                @click.stop
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                    <span>加载失败</span>
                  </div>
                </template>
              </el-image>
              <div v-else class="no-image">
                <el-icon size="24" color="#c0c4cc"><Picture /></el-icon>
                <span>无图片</span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="name" label="设备名称" min-width="150" show-overflow-tooltip />
        
        <el-table-column label="分类" width="100">
          <template #default="scope">
            {{ getCategoryDisplayName(scope.row) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="serialNumber" label="序列号" min-width="120" show-overflow-tooltip />
        
        <el-table-column label="库存信息" width="120">
          <template #default="{ row }">
            <div class="stock-info">
              <div class="stock-item">
                <span class="stock-label">总数:</span>
                <span class="stock-value">{{ row.stockQuantity }}</span>
              </div>
              <div class="stock-item">
                <span class="stock-label">可用:</span>
                <span class="stock-value" :class="row.availableQuantity > 0 ? 'text-success' : 'text-danger'">
                  {{ row.availableQuantity }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="specifications" label="规格参数" min-width="200" show-overflow-tooltip />
        
        <el-table-column
          label="操作"
          :width="userStore.isAdmin ? 188 : 86"
          fixed="right"
          align="center"
          class-name="operations-column"
        >
          <template #default="{ row }">
            <div class="row-actions" @click.stop>
              <el-tooltip content="查看详情" placement="top" :show-after="180">
                <el-button
                  class="icon-action view-action"
                  circle
                  aria-label="查看详情"
                  @click="handleView(row)"
                >
                  <el-icon><View /></el-icon>
                </el-button>
              </el-tooltip>
              <template v-if="userStore.isAdmin">
                <el-tooltip content="编辑设备" placement="top" :show-after="180">
                  <el-button
                    class="icon-action edit-action"
                    circle
                    aria-label="编辑设备"
                    @click="handleEdit(row)"
                  >
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="管理图片" placement="top" :show-after="180">
                  <el-button
                    class="icon-action image-action"
                    circle
                    aria-label="管理图片"
                    @click="handleUploadImage(row)"
                  >
                    <el-icon><Picture /></el-icon>
                  </el-button>
                </el-tooltip>
                <el-tooltip content="删除设备" placement="top" :show-after="180">
                  <el-button
                    class="icon-action delete-action"
                    circle
                    aria-label="删除设备"
                    @click="handleDelete(row)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-tooltip>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 移动端卡片视图 -->
      <div class="mobile-cards">
        <!-- 骨架屏加载状态 -->
        <template v-if="loading">
          <SkeletonLoader 
            v-for="i in 6" 
            :key="i" 
            type="card" 
          />
        </template>
        
        <!-- 空状态 -->
        <EmptyState 
          v-else-if="equipmentList.length === 0"
          :type="searchForm.keyword || searchForm.category ? 'no-search' : 'no-data'"
          :title="searchForm.keyword || searchForm.category ? '无搜索结果' : '暂无设备'"
          :description="searchForm.keyword || searchForm.category ? '没有找到匹配的设备，请尝试其他搜索条件' : '还没有添加任何设备，点击新增设备开始'"
          :action="userStore.isAdmin && !searchForm.keyword && !searchForm.category ? '新增设备' : ''"
          @action="showCreateDialog = true"
        />
        
        <!-- 设备卡片列表 -->
        <div
          v-else
          v-for="equipment in equipmentList"
          :key="equipment.id"
          class="equipment-card card-hover"
          @click="handleView(equipment)"
        >
          <div class="card-header">
            <div class="equipment-image-mobile">
              <el-image
                v-if="equipment.imageUrl"
                :src="getImageUrl(equipment.imageUrl)"
                :preview-src-list="[getImageUrl(equipment.imageUrl)]"
                :preview-teleported="true"
                fit="cover"
                style="width: 50px; height: 50px; border-radius: 6px;"
                @click.stop
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="no-image-mobile">
                <el-icon size="20" color="#c0c4cc"><Picture /></el-icon>
              </div>
            </div>
            <div class="equipment-info-mobile">
              <h4 class="equipment-name-mobile">{{ equipment.name }}</h4>
              <div class="equipment-meta">
                <el-tag size="small" type="primary">{{ getCategoryDisplayName(equipment) }}</el-tag>
                <span class="serial-number">{{ equipment.serialNumber }}</span>
              </div>
            </div>
            <div class="equipment-status">
              <el-tag 
                :type="getEquipmentStatusType(equipment.status)" 
                size="small"
              >
                {{ equipment.status }}
              </el-tag>
            </div>
          </div>
          
          <div class="card-content">
            <div class="stock-info-mobile">
              <div class="stock-item-mobile">
                <span class="stock-label">总库存:</span>
                <span class="stock-value">{{ equipment.stockQuantity }}</span>
              </div>
              <div class="stock-item-mobile">
                <span class="stock-label">可用:</span>
                <span 
                  class="stock-value" 
                  :class="equipment.availableQuantity > 0 ? 'text-success' : 'text-danger'"
                >
                  {{ equipment.availableQuantity }}
                </span>
              </div>
            </div>
            
            <div v-if="equipment.specifications" class="specifications-mobile">
              <span class="spec-label">规格:</span>
              <span class="spec-content">{{ equipment.specifications }}</span>
            </div>
          </div>
          
          <div class="card-actions row-actions compact-row-actions" @click.stop>
            <el-button
              class="icon-action view-action"
              circle
              aria-label="查看详情"
              @click="handleView(equipment)"
            >
              <el-icon><View /></el-icon>
            </el-button>
            <template v-if="userStore.isAdmin">
              <el-button
                class="icon-action edit-action"
                circle
                aria-label="编辑设备"
                @click="handleEdit(equipment)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button
                class="icon-action image-action"
                circle
                aria-label="管理图片"
                @click="handleUploadImage(equipment)"
              >
                <el-icon><Picture /></el-icon>
              </el-button>
              <el-button
                class="icon-action delete-action"
                circle
                aria-label="删除设备"
                @click="handleDelete(equipment)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
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
    
    <!-- 新增/编辑设备对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="!userStore.isAdmin && editingEquipment ? '查看设备详情' : (editingEquipment ? '编辑设备' : '新增设备')"
      :width="isMobile ? '95%' : '700px'"
      :close-on-click-modal="false"
      class="equipment-dialog"
    >
      <!-- 设备图片展示区域 -->
      <div v-if="editingEquipment" class="equipment-image-preview">
        <div class="image-container">
          <el-image
            v-if="editingEquipment.imageUrl"
            :src="getImageUrl(editingEquipment.imageUrl)"
            :preview-src-list="[getImageUrl(editingEquipment.imageUrl)]"
            :preview-teleported="true"
            fit="cover"
            style="width: 200px; height: 150px; border-radius: 8px;"
          >
            <template #error>
              <div class="image-slot">
                <el-icon size="40"><Picture /></el-icon>
                <p>图片加载失败</p>
              </div>
            </template>
          </el-image>
          <div v-else class="no-image">
            <el-icon size="40"><Picture /></el-icon>
            <p>暂无图片</p>
          </div>
        </div>
        <div class="image-info">
          <h4>{{ editingEquipment.name }}</h4>
          <p class="equipment-category">{{ getCategoryDisplayName(editingEquipment) }}</p>
          <p class="equipment-serial">序列号: {{ editingEquipment.serialNumber }}</p>
        </div>
      </div>

      <el-form
        ref="equipmentFormRef"
        :model="equipmentForm"
        :rules="equipmentRules"
        :label-width="isMobile ? '80px' : '100px'"
        class="equipment-form"
      >
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="设备名称" prop="name">
              <el-input 
                v-model="equipmentForm.name" 
                placeholder="请输入设备名称" 
                :readonly="isReadOnlyMode"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="分类" prop="category">
              <el-select 
                v-model="equipmentForm.category" 
                placeholder="选择分类" 
                style="width: 100%"
                :disabled="isReadOnlyMode"
                clearable
              >
                <el-option
                  v-for="category in categories"
                  :key="category"
                  :label="category"
                  :value="category"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="序列号" prop="serialNumber">
              <el-input 
                v-model="equipmentForm.serialNumber" 
                placeholder="请输入序列号"
                :disabled="!!editingEquipment"
                :readonly="isReadOnlyMode"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="总库存" prop="stockQuantity">
              <el-input-number
                v-model="equipmentForm.stockQuantity"
                :min="0"
                :max="9999"
                style="width: 100%"
                :disabled="isReadOnlyMode"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="可用数量" prop="availableQuantity">
              <el-input-number
                v-model="equipmentForm.availableQuantity"
                :min="0"
                :max="equipmentForm.stockQuantity || 9999"
                style="width: 100%"
                :disabled="isReadOnlyMode"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="损坏数量" prop="damagedQuantity">
              <el-input-number
                v-model="equipmentForm.damagedQuantity"
                :min="0"
                :max="equipmentForm.stockQuantity || 9999"
                style="width: 100%"
                :disabled="isReadOnlyMode"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="isMobile ? 10 : 20" v-if="userStore.isAdmin">
          <el-col :xs="24" :sm="12">
            <el-form-item label="设备状态" prop="status">
              <el-select 
                v-model="equipmentForm.status" 
                placeholder="请选择设备状态"
                style="width: 100%"
                :disabled="isReadOnlyMode"
                clearable
              >
                <el-option label="正常" value="正常" />
                <el-option label="借出" value="借出" />
                <el-option label="损坏" value="损坏" />
                <el-option label="维修中" value="维修中" />
                <el-option label="报废" value="报废" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :span="24">
            <el-form-item label="规格参数" prop="specifications">
              <el-input
                v-model="equipmentForm.specifications"
                type="textarea"
                :rows="isMobile ? 2 : 3"
                placeholder="请输入设备规格参数"
                :readonly="isReadOnlyMode"
                :autosize="{ minRows: 2, maxRows: 6 }"
                show-word-limit
                maxlength="500"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :span="24">
            <el-form-item label="设备描述" prop="description">
              <el-input
                v-model="equipmentForm.description"
                type="textarea"
                :rows="isMobile ? 2 : 3"
                placeholder="请输入设备描述"
                :readonly="isReadOnlyMode"
                :autosize="{ minRows: 2, maxRows: 6 }"
                show-word-limit
                maxlength="500"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleCancelEdit">
            {{ !userStore.isAdmin && editingEquipment ? '关闭' : '取消' }}
          </el-button>
          <el-button 
            v-if="userStore.isAdmin"
            type="primary" 
            @click="handleSaveEquipment" 
            :loading="saving"
          >
            {{ editingEquipment ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 设备详情查看对话框 -->
    <el-dialog
      v-model="showViewDialog"
      width="860px"
      :close-on-click-modal="false"
      class="equipment-view-dialog"
    >
      <template #header>
        <div class="detail-dialog-header">
          <div class="detail-dialog-title">
            <span class="detail-dialog-icon">
              <el-icon><InfoFilled /></el-icon>
            </span>
            <div>
              <h3>设备详情</h3>
              <p>查看设备基础信息、库存状态与规格参数</p>
            </div>
          </div>
        </div>
      </template>

      <div v-if="viewingEquipment" class="equipment-detail-view">
        <!-- 设备基本信息卡片 -->
        <el-card class="detail-card basic-info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><InfoFilled /></el-icon>
              <span class="header-title">基本信息</span>
            </div>
          </template>
          
          <div class="detail-content">
            <div class="equipment-header">
              <!-- 设备图片 -->
              <div class="equipment-image-large">
                <el-image
                  v-if="viewingEquipment.imageUrl"
                  :src="getImageUrl(viewingEquipment.imageUrl)"
                  :preview-src-list="[getImageUrl(viewingEquipment.imageUrl)]"
                  :preview-teleported="true"
                  fit="cover"
                  style="width: 200px; height: 150px; border-radius: 12px;"
                >
                  <template #error>
                    <div class="image-slot-large">
                      <el-icon size="50"><Picture /></el-icon>
                      <p>图片加载失败</p>
                    </div>
                  </template>
                </el-image>
                <div v-else class="no-image-large">
                  <el-icon size="50"><Picture /></el-icon>
                  <p>暂无图片</p>
                </div>
              </div>
              
              <!-- 设备标题信息 -->
              <div class="equipment-title-info">
                <h2 class="equipment-name">{{ viewingEquipment.name }}</h2>
                <div class="equipment-meta">
                  <el-tag type="primary" size="large" style="margin-right: 10px;">
                    {{ getCategoryDisplayName(viewingEquipment) }}
                  </el-tag>
                  <el-tag 
                    :type="viewingEquipment.availableQuantity > 0 ? 'success' : 'danger'" 
                    size="large"
                  >
                    {{ viewingEquipment.availableQuantity > 0 ? '有库存' : '无库存' }}
                  </el-tag>
                </div>
                <p class="equipment-serial">序列号：{{ viewingEquipment.serialNumber }}</p>
              </div>
            </div>
            
            <!-- 详细信息 -->
            <div class="detail-grid">
              <div class="detail-item">
                <label class="detail-label">设备名称</label>
                <span class="detail-value">{{ viewingEquipment.name }}</span>
              </div>
              
              <div class="detail-item">
                <label class="detail-label">设备分类</label>
                <span class="detail-value">{{ getCategoryDisplayName(viewingEquipment) }}</span>
              </div>
              
              <div class="detail-item">
                <label class="detail-label">序列号</label>
                <span class="detail-value">{{ viewingEquipment.serialNumber }}</span>
              </div>
              
              <div class="detail-item">
                <label class="detail-label">设备状态</label>
                <span class="detail-value">
                  <el-tag :type="getEquipmentStatusType(viewingEquipment.status)" size="small">
                    {{ viewingEquipment.status || '正常' }}
                  </el-tag>
                </span>
              </div>
            </div>
          </div>
        </el-card>
        
        <!-- 库存信息卡片 -->
        <el-card class="detail-card stock-info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><Box /></el-icon>
              <span class="header-title">库存信息</span>
            </div>
          </template>
          
          <div class="stock-grid">
            <div class="stock-item-detail">
              <div class="stock-number">{{ viewingEquipment.stockQuantity }}</div>
              <div class="stock-label">总库存</div>
            </div>
            <div class="stock-item-detail">
              <div class="stock-number available">{{ viewingEquipment.availableQuantity }}</div>
              <div class="stock-label">可用数量</div>
            </div>
            <div class="stock-item-detail">
              <div class="stock-number borrowed">{{ viewingEquipment.stockQuantity - viewingEquipment.availableQuantity - (viewingEquipment.damagedQuantity || 0) }}</div>
              <div class="stock-label">借出数量</div>
            </div>
            <div class="stock-item-detail">
              <div class="stock-number damaged">{{ viewingEquipment.damagedQuantity || 0 }}</div>
              <div class="stock-label">损坏数量</div>
            </div>
          </div>
        </el-card>
        
        <!-- 规格参数卡片 -->
        <el-card class="detail-card specs-card" shadow="never" v-if="viewingEquipment.specifications">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><DocumentCopy /></el-icon>
              <span class="header-title">规格参数</span>
            </div>
          </template>
          
          <div class="specs-content">
            <p>{{ viewingEquipment.specifications }}</p>
          </div>
        </el-card>
        
        <!-- 设备描述卡片 -->
        <el-card class="detail-card description-card" shadow="never" v-if="viewingEquipment.description">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><Document /></el-icon>
              <span class="header-title">设备描述</span>
            </div>
          </template>
          
          <div class="description-content">
            <p>{{ viewingEquipment.description }}</p>
          </div>
        </el-card>
      </div>
      
      <template #footer>
        <div class="dialog-footer detail-dialog-footer">
          <el-button class="dialog-action secondary-action" @click="showViewDialog = false">
            <el-icon><Close /></el-icon>
            <span>关闭</span>
          </el-button>
          <el-button 
            v-if="userStore.isAdmin" 
            class="dialog-action primary-action"
            type="primary"
            @click="handleEditFromView"
          >
            <el-icon><Edit /></el-icon>
            <span>编辑设备</span>
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 图片管理对话框 -->
    <el-dialog
      v-model="showImageDialog"
      :title="`设备图片管理 - ${currentEquipment?.name || ''}`"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentEquipment" class="image-management-container">
        <!-- 设备信息 -->
        <div class="equipment-info-header">
          <div class="equipment-basic-info">
            <h3>{{ currentEquipment.name }}</h3>
            <p>{{ getCategoryDisplayName(currentEquipment) }} | {{ currentEquipment.serialNumber }}</p>
          </div>
        </div>
        
        <!-- 当前图片展示 -->
        <div class="current-image-section">
          <h4>
            <el-icon class="section-icon"><Picture /></el-icon>
            当前图片
          </h4>
          <div class="image-display">
            <el-image
              v-if="currentEquipment?.imageUrl"
              :src="getImageUrl(currentEquipment.imageUrl)"
              :preview-src-list="[getImageUrl(currentEquipment.imageUrl)]"
              :preview-teleported="true"
              fit="cover"
              style="width: 300px; height: 200px; border-radius: 12px; border: 2px solid #e4e7ed;"
            >
              <template #error>
                <div class="image-error-slot">
                  <el-icon size="40"><Picture /></el-icon>
                  <p>图片加载失败</p>
                </div>
              </template>
            </el-image>
            <div v-else class="no-current-image">
              <el-icon size="60"><Picture /></el-icon>
              <p>暂无图片</p>
              <span>请上传设备图片</span>
            </div>
          </div>
        </div>
        
        <!-- 上传区域 -->
        <div class="upload-section">
          <h4>
            <el-icon class="section-icon"><UploadFilled /></el-icon>
            {{ currentEquipment?.imageUrl ? '更换图片' : '上传图片' }}
          </h4>
          <DragUpload
            ref="dragUploadRef"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :multiple="false"
            accept="image/*"
            :max-size="10 * 1024 * 1024"
            :max-count="1"
            title="将图片文件拖到此处，或点击选择文件"
            subtitle="支持 JPG、PNG 格式上传"
            :tips="['支持 JPG、PNG、GIF 格式', '单个文件不超过 10MB', '建议尺寸：800x600 像素，保证图片清晰度']"
            :auto-upload="true"
            :show-file-list="false"
            @success="handleImageUploadSuccess"
            @error="handleImageUploadError"
          />
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeImageDialog">关闭</el-button>
          <el-button 
            v-if="currentEquipment?.imageUrl" 
            type="danger" 
            @click="handleRemoveImage"
          >
            <el-icon><Delete /></el-icon>
            删除图片
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分类管理对话框 -->
    <el-dialog
      v-model="showCategoryDialog"
      title="分类管理"
      width="90%"
      :max-width="900"
      :close-on-click-modal="false"
      class="category-dialog"
    >
      <div class="category-management">
        <!-- 分类表单 -->
        <el-card class="category-form-card" style="margin-bottom: 20px;">
          <template #header>
            <span>{{ editingCategory ? '编辑分类' : '新增分类' }}</span>
          </template>
          
          <el-form
            ref="categoryFormRef"
            :model="categoryForm"
            :rules="categoryRules"
            :label-width="isMobile ? '80px' : '100px'"
            class="category-form"
          >
            <el-row :gutter="20">
              <el-col :xs="24" :sm="16" :md="16">
                <el-form-item label="分类名称" prop="name">
                  <el-input 
                    v-model="categoryForm.name" 
                    placeholder="请输入分类名称"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="8" :md="8">
                <el-form-item label="排序" prop="sortOrder">
                  <el-input-number 
                    v-model="categoryForm.sortOrder" 
                    :min="1" 
                    :max="999"
                    controls-position="right"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row>
              <el-col :span="24">
                <el-form-item label="描述" prop="description">
                  <el-input 
                    v-model="categoryForm.description" 
                    type="textarea" 
                    :rows="2"
                    placeholder="请输入分类描述（可选）"
                    :maxlength="200"
                    show-word-limit
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row>
              <el-col :span="24">
                <el-form-item label="状态" class="status-form-item">
                  <el-switch 
                    v-model="categoryForm.isActive"
                    active-text="启用"
                    inactive-text="禁用"
                    :active-color="'#13ce66'"
                    :inactive-color="'#ff4949'"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item class="form-buttons">
              <div class="button-group">
                <el-button 
                  type="primary" 
                  @click="handleSaveCategory" 
                  :loading="saving"
                  icon="Check"
                >
                  {{ editingCategory ? '更新分类' : '创建分类' }}
                </el-button>
                <el-button 
                  @click="handleCancelCategory" 
                  v-if="editingCategory"
                  icon="Close"
                >
                  取消编辑
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 分类列表 -->
        <el-card class="category-list-card">
          <template #header>
            <div class="list-header">
              <span>分类列表</span>
              <el-tag type="info" size="small">共 {{ categoryList.length }} 个分类</el-tag>
            </div>
          </template>
          
          <!-- 桌面端表格 -->
          <el-table 
            :data="categoryList" 
            style="width: 100%" 
            v-loading="loading"
            :class="{ 'mobile-table': isMobile }"
            v-if="!isMobile"
          >
            <el-table-column prop="name" label="分类名称" min-width="120" show-overflow-tooltip />
            <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
            <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
            <el-table-column label="状态" width="80" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.isActive ? 'success' : 'danger'" size="small">
                  {{ scope.row.isActive ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" align="center">
              <template #default="scope">
                <div class="action-buttons">
                  <el-button type="primary" size="small" @click="handleEditCategory(scope.row)" plain>
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button 
                    :type="scope.row.isActive ? 'warning' : 'success'"
                    size="small" 
                    @click="handleToggleCategoryStatus(scope.row)"
                    plain
                  >
                    {{ scope.row.isActive ? '禁用' : '启用' }}
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="handleDeleteCategory(scope.row)"
                    plain
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 移动端卡片列表 -->
          <div class="mobile-category-list" v-if="isMobile">
            <div 
              v-for="category in categoryList" 
              :key="category.id" 
              class="mobile-category-item"
            >
              <div class="category-info">
                <div class="category-name">
                  {{ category.name }}
                  <el-tag :type="category.isActive ? 'success' : 'danger'" size="small">
                    {{ category.isActive ? '启用' : '禁用' }}
                  </el-tag>
                </div>
                <div class="category-description" v-if="category.description">
                  {{ category.description }}
                </div>
                <div class="category-sort">
                  排序: {{ category.sortOrder }}
                </div>
              </div>
              <div class="category-actions">
                <el-button type="primary" size="small" @click="handleEditCategory(category)" plain>
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button 
                  :type="category.isActive ? 'warning' : 'success'"
                  size="small" 
                  @click="handleToggleCategoryStatus(category)"
                  plain
                >
                  {{ category.isActive ? '禁用' : '启用' }}
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="handleDeleteCategory(category)"
                  plain
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showCategoryDialog = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 数据清理对话框 -->
    <el-dialog
      v-model="showCleanupDialog"
      title="设备数据清理管理"
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
              <h3>设备数据清理统计</h3>
              <el-button size="small" @click="fetchCleanupStatistics">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-number">{{ cleanupStats.totalDeletedEquipments || 0 }}</div>
                <div class="stat-label">已删除设备总数</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-number">{{ cleanupStats.deletedLastWeek || 0 }}</div>
                <div class="stat-label">一周前删除的设备</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="stat-number">{{ cleanupStats.deletedLastMonth || 0 }}</div>
                <div class="stat-label">一个月前删除的设备</div>
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
              @click="handleAutoCleanupEquipments(7)"
              :disabled="!cleanupStats.deletedLastWeek"
            >
              清理一周前的设备 ({{ cleanupStats.deletedLastWeek || 0 }} 个)
            </el-button>
            <el-button 
              type="danger" 
              @click="handleAutoCleanupEquipments(30)"
              :disabled="!cleanupStats.deletedLastMonth"
            >
              清理一个月前的设备 ({{ cleanupStats.deletedLastMonth || 0 }} 个)
            </el-button>
            <el-button 
              type="danger" 
              @click="handleAutoCleanupEquipments(90)"
              :disabled="!cleanupStats.deletedThreeMonthsAgo"
            >
              清理三个月前的设备 ({{ cleanupStats.deletedThreeMonthsAgo || 0 }} 个)
            </el-button>
            <el-button 
              type="danger" 
              plain
              @click="handleBatchPhysicalDeleteEquipments"
              :disabled="selectedDeletedEquipments.length === 0"
            >
              批量删除选中设备 ({{ selectedDeletedEquipments.length }})
            </el-button>
          </div>
        </el-card>

        <!-- 已删除设备列表 -->
        <el-card class="records-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>已删除设备列表</h3>
              <div>
                <el-button size="small" @click="fetchDeletedEquipments">
                  <el-icon><Refresh /></el-icon>
                  刷新列表
                </el-button>
              </div>
            </div>
          </template>
          
          <el-table
            :data="deletedEquipments"
            v-loading="cleaning"
            element-loading-text="加载中..."
            @selection-change="selectedDeletedEquipments = $event.map(item => item.id)"
            style="width: 100%"
          >
            <el-table-column type="selection" width="50" />
            <el-table-column prop="id" label="设备ID" width="80" />
            <el-table-column label="设备信息" min-width="200">
              <template #default="{ row }">
                <div class="equipment-info">
                  <div class="equipment-name">{{ row.name }}</div>
                  <div class="equipment-category">{{ getCategoryDisplayName(row) }}</div>
                  <div class="equipment-serial">{{ row.serialNumber }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="库存信息" width="120">
              <template #default="{ row }">
                <div class="stock-info">
                  <div>总数：{{ row.stockQuantity }}</div>
                  <div>可用：{{ row.availableQuantity }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getEquipmentStatusType(row.status)" size="small">
                  {{ row.status || '正常' }}
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
                  @click="handlePhysicalDeleteEquipment(row)"
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
              @size-change="(size) => { cleanupPagination.size = size; fetchDeletedEquipments() }"
            />
          </div>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { 
  Search, Refresh, Plus, Setting, Camera, View, Edit, Picture, Delete,
  UploadFilled, InfoFilled, Box, DocumentCopy, Document, Download, Close
} from '@element-plus/icons-vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import EmptyState from '@/components/EmptyState.vue'
import DragUpload from '@/components/DragUpload.vue'
import { smartPreload } from '@/utils/imageCache'

const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const isMobile = ref(false)
const showCreateDialog = ref(false)
const showViewDialog = ref(false)  // 新增：专门的查看对话框
const showImageDialog = ref(false)
const showCategoryDialog = ref(false)
const showCleanupDialog = ref(false)
const cleaning = ref(false)
const dragUploadRef = ref(null)  // DragUpload组件引用
const editingEquipment = ref(null)
const viewingEquipment = ref(null)  // 新增：查看的设备
const currentEquipment = ref(null)
const editingCategory = ref(null)
const equipmentList = ref([])
const categories = ref([])
const categoryList = ref([])

// 数据清理相关
const cleanupStats = ref({})
const deletedEquipments = ref([])
const selectedDeletedEquipments = ref([])
const cleanupPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 分类表单
const categoryForm = reactive({
  name: '',
  description: '',
  sortOrder: 1,
  isActive: true
})

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: ''
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 是否为只读模式（普通用户查看设备详情时）
const isReadOnlyMode = computed(() => {
  return !userStore.isAdmin && !!editingEquipment.value
})

// 设备表单
const equipmentForm = reactive({
  name: '',
  category: '',
  serialNumber: '',
  stockQuantity: 1,
  availableQuantity: 1,
  damagedQuantity: 0,
  status: '正常',
  specifications: '',
  description: ''
})

// 表单验证规则
const equipmentRules = {
  name: [
    { required: true, message: '请输入设备名称', trigger: 'blur' },
    { min: 2, max: 100, message: '设备名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  serialNumber: [
    { required: true, message: '请输入序列号', trigger: 'blur' },
    { min: 2, max: 50, message: '序列号长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  stockQuantity: [
    { required: true, message: '请输入总库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存数量不能小于0', trigger: 'blur' }
  ]
}

// 分类表单验证规则
const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '分类名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序号', trigger: 'blur' },
    { type: 'number', min: 1, max: 999, message: '排序号范围为 1-999', trigger: 'blur' }
  ]
}

// 上传配置
const uploadUrl = computed(() => 
  currentEquipment.value ? `${request.defaults.baseURL}/equipment/${currentEquipment.value.id}/image` : ''
)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

// 构建完整的图片URL - 使用后端解析后的路径（/api/images/...）并缓存
const imageUrlCache = new Map()
const getImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  
  // 从缓存中获取，确保相同输入返回相同结果
  if (imageUrlCache.has(imageUrl)) {
    return imageUrlCache.get(imageUrl)
  }
  
  let finalUrl = ''
  
  // 如果是完整URL，直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    finalUrl = imageUrl
  } else {
    const cleanPath = imageUrl.trim()
    // 提取文件名
    const fileName = cleanPath.split('/').pop()
    // 统一走后端图片接口，使用解析后的绝对路径
    finalUrl = `/api/images/equipment/${fileName}`
  }
  
  // 缓存结果
  imageUrlCache.set(imageUrl, finalUrl)
  return finalUrl
}

// 监听设备状态变化，自动调整库存
watch(() => equipmentForm.status, (newStatus, oldStatus) => {
  if (!editingEquipment.value || newStatus === oldStatus) return
  
  console.log('[前端状态监听] 设备状态变更:', oldStatus, '→', newStatus)
  
  // 从损坏改为正常：自动将损坏数量的部分转为可用数量
  if (oldStatus === '损坏' && newStatus === '正常') {
    const damagedQty = equipmentForm.damagedQuantity || 0
    if (damagedQty > 0) {
      // 提示用户状态变更会影响库存
      ElMessageBox.confirm(
        `检测到设备状态从"损坏"改为"正常"，是否要将 ${damagedQty} 个损坏设备修复为可用状态？`,
        '库存调整确认',
        {
          confirmButtonText: '是，自动修复',
          cancelButtonText: '否，手动调整',
          type: 'info'
        }
      ).then(() => {
        // 自动修复：损坏数量转为可用数量
        equipmentForm.availableQuantity = (equipmentForm.availableQuantity || 0) + damagedQty
        equipmentForm.damagedQuantity = 0
        ElMessage.success(`已将 ${damagedQty} 个损坏设备修复为可用状态`)
      }).catch(() => {
        // 用户选择手动调整，不做任何操作
        console.log('用户选择手动调整库存')
      })
    }
  }
  // 从正常改为损坏：提示用户需要设置损坏数量
  else if (oldStatus === '正常' && newStatus === '损坏') {
    ElMessage.warning('设备状态已改为损坏，请手动设置损坏数量')
  }
})

// 获取分类显示名称
const getCategoryDisplayName = (equipment) => {
  // 优先使用后端提供的 categoryDisplayName
  if (equipment.categoryDisplayName) {
    return equipment.categoryDisplayName
  }
  // 备用逻辑
  if (equipment.category && equipment.category.name) {
    return equipment.category.name
  }
  return equipment.categoryName || '未分类'
}

const equipmentFormRef = ref()

// 获取设备列表
const fetchEquipment = async () => {
  try {
    loading.value = true
    
    const baseParams = {
      page: pagination.page - 1,
      size: pagination.size
    }
    
    let response
    
    // 根据搜索条件选择不同的API端点
    if (searchForm.keyword && searchForm.category) {
      // 有关键字和分类：在分类中搜索
      response = await request.get(`/equipment/category/${searchForm.category}/search`, { 
        params: { ...baseParams, keyword: searchForm.keyword }
      })
    } else if (searchForm.keyword) {
      // 只有关键字：全局搜索
      response = await request.get('/equipment/search', { 
        params: { ...baseParams, keyword: searchForm.keyword }
      })
    } else if (searchForm.category) {
      // 只有分类：按分类过滤
      response = await request.get(`/equipment/category/${searchForm.category}`, { 
        params: baseParams
      })
    } else {
      // 没有搜索条件：获取所有设备
      response = await request.get('/equipment', { 
        params: baseParams
      })
    }
    
    if (response.data) {
      equipmentList.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
      
      // 预加载设备图片
      const imageUrls = equipmentList.value
        .map(equipment => equipment.imageUrl)
        .filter(Boolean)
        .map(getImageUrl)
      
      if (imageUrls.length > 0) {
        smartPreload(imageUrls, { viewport: true, delay: 200 })
      }
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('获取设备列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索设备
const handleSearch = () => {
  pagination.page = 1
  fetchEquipment()
}

// 重置搜索
const handleReset = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  pagination.page = 1
  fetchEquipment()
}

// 查看设备详情
const handleView = (equipment) => {
  viewingEquipment.value = equipment
  showViewDialog.value = true
}

// 编辑设备
const handleEdit = (equipment) => {
  editingEquipment.value = equipment
  Object.keys(equipmentForm).forEach(key => {
    if (key === 'category') {
      // 特殊处理分类字段
      equipmentForm[key] = getCategoryDisplayName(equipment)
    } else {
      equipmentForm[key] = equipment[key] || (key === 'stockQuantity' || key === 'availableQuantity' ? 1 : '')
    }
  })
  showCreateDialog.value = true
}

// 从查看对话框转到编辑
const handleEditFromView = () => {
  if (viewingEquipment.value) {
    showViewDialog.value = false
    handleEdit(viewingEquipment.value)
  }
}

// 取消编辑
const handleCancelEdit = () => {
  showCreateDialog.value = false
  editingEquipment.value = null
  resetForm()
}

// 重置表单
const resetForm = () => {
  Object.keys(equipmentForm).forEach(key => {
    if (key === 'stockQuantity' || key === 'availableQuantity') {
      equipmentForm[key] = 1
    } else if (key === 'status') {
      equipmentForm[key] = '正常'
    } else {
      equipmentForm[key] = ''
    }
  })
  if (equipmentFormRef.value) {
    equipmentFormRef.value.clearValidate()
  }
}

// 保存设备
const handleSaveEquipment = async () => {
  if (!equipmentFormRef.value) return
  
  try {
    await equipmentFormRef.value.validate()
    saving.value = true
    
    // 确保可用数量不超过总库存
    if (equipmentForm.availableQuantity > equipmentForm.stockQuantity) {
      equipmentForm.availableQuantity = equipmentForm.stockQuantity
    }
    
    if (editingEquipment.value) {
      // 编辑设备
      await request.put(`/equipment/${editingEquipment.value.id}`, equipmentForm)
      ElMessage.success('设备更新成功')
    } else {
      // 新增设备，设置可用数量等于总库存
      equipmentForm.availableQuantity = equipmentForm.stockQuantity
      await request.post('/equipment', equipmentForm)
      ElMessage.success('设备创建成功')
    }
    
    showCreateDialog.value = false
    editingEquipment.value = null
    resetForm()
    fetchEquipment()
  } catch (error) {
    console.error('保存设备失败:', error)
    ElMessage.error('保存设备失败')
  } finally {
    saving.value = false
  }
}

// 删除设备
const handleDelete = async (equipment) => {
  try {
    await ElMessageBox.confirm(`确定要删除设备 "${equipment.name}" 吗？此操作不可恢复！`, '确认删除', {
      type: 'error',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    
    await request.delete(`/equipment/${equipment.id}`)
    ElMessage.success('设备删除成功')
    fetchEquipment()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除设备失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 上传图片
const handleUploadImage = (equipment) => {
  currentEquipment.value = equipment
  showImageDialog.value = true
  
  // 重置DragUpload组件状态，确保每次打开都是干净的状态
  nextTick(() => {
    if (dragUploadRef.value) {
      dragUploadRef.value.reset()
    }
  })
}

// 图片上传前检查（DragUpload组件已内置验证，这里保留用于兼容性）
const beforeImageUpload = (file) => {
  // DragUpload 组件已经处理了大部分验证
  // 这里可以添加额外的业务逻辑验证
  return true
}

// 图片上传成功
const handleImageUploadSuccess = (response, file) => {
  console.log('图片上传响应:', response)
  
  if (response.success !== false && response.data) {
    ElMessage.success(`图片 ${file.name} 上传成功`)
    // 更新当前设备的图片URL
    if (currentEquipment.value) {
      // 处理不同的响应格式
      const imageUrl = response.data.imageUrl || response.data
      currentEquipment.value.imageUrl = imageUrl
      console.log('更新设备图片URL:', imageUrl)
      console.log('完整图片URL:', getImageUrl(imageUrl))
      
      // 同时更新设备列表中对应的项目
      const equipmentIndex = equipmentList.value.findIndex(item => item.id === currentEquipment.value.id)
      if (equipmentIndex !== -1) {
        equipmentList.value[equipmentIndex].imageUrl = imageUrl
      }
    }
    
    // 🔧 关键修复：上传成功后重置DragUpload组件状态，确保下次上传正常
    if (dragUploadRef.value) {
      dragUploadRef.value.reset()
      console.log('✅ 图片上传成功，已重置上传组件状态')
    }
    
    // 不需要重新获取列表，直接更新即可
  } else {
    console.error('图片上传失败:', response)
    ElMessage.error('图片上传失败')
  }
}

// 图片上传失败
const handleImageUploadError = (error, file) => {
  console.error('图片上传失败:', error)
  ElMessage.error(`图片 ${file.name} 上传失败: ${error.message || '网络错误'}`)
  
  // 🔧 上传失败时也重置DragUpload组件状态，确保下次上传正常
  if (dragUploadRef.value) {
    dragUploadRef.value.reset()
    console.log('❌ 图片上传失败，已重置上传组件状态')
  }
}

// 关闭图片对话框
const closeImageDialog = () => {
  showImageDialog.value = false
  // 重置DragUpload组件状态
  if (dragUploadRef.value) {
    dragUploadRef.value.reset()
  }
}

// 删除设备图片
const handleRemoveImage = async () => {
  if (!currentEquipment.value) return
  
  try {
    await ElMessageBox.confirm(
      '确定要删除这张设备图片吗？删除后无法恢复！',
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    await request.delete(`/equipment/${currentEquipment.value.id}/image`)
    ElMessage.success('图片删除成功')
    
    // 更新当前设备和列表中的图片URL
    currentEquipment.value.imageUrl = null
    const equipmentIndex = equipmentList.value.findIndex(item => item.id === currentEquipment.value.id)
    if (equipmentIndex !== -1) {
      equipmentList.value[equipmentIndex].imageUrl = null
    }
    
    // 重置DragUpload组件状态，清理文件列表
    if (dragUploadRef.value) {
      dragUploadRef.value.reset()
    }
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除图片失败:', error)
      ElMessage.error('删除图片失败')
    }
  }
}

// 分页处理
const handleSizeChange = (newSize) => {
  pagination.size = newSize
  pagination.page = 1
  fetchEquipment()
}

const handleCurrentChange = (newPage) => {
  pagination.page = newPage
  fetchEquipment()
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await request.get('/equipment-categories/active')
    if (response.data) {
      categories.value = response.data.map(category => category.name)
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    // 如果获取分类失败，使用备用分类
    categories.value = ['相机', '镜头', '三脚架', '闪光灯', '录音设备', '无人机', '其他']
  }
}

// ===== 分类管理相关方法 =====

// 获取所有分类列表（包括禁用的）
const fetchAllCategories = async () => {
  try {
    loading.value = true
    const response = await request.get('/equipment-categories/list')
    if (response.data) {
      categoryList.value = response.data
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 打开分类管理对话框
const handleCategoryManagement = () => {
  showCategoryDialog.value = true
  fetchAllCategories()
}

// 导出设备列表
const handleExportEquipment = async () => {
  try {
    // 构建查询参数
    const params = {}
    if (searchForm.category && searchForm.category !== 'all') {
      params.category = searchForm.category
    }
    if (searchForm.status && searchForm.status !== 'all') {
      params.status = searchForm.status
    }
    
    // 发送导出请求
    const response = await request.get('/export/equipment-list', {
      params,
      responseType: 'blob'
    })
    
    // 创建下载链接
    const blob = new Blob([response.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名
    const timestamp = new Date().toISOString().slice(0, 19).replace(/[-:T]/g, '')
    const filename = `设备清单_${timestamp}.xlsx`
    
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('设备清单导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败: ' + (error.response?.data?.message || error.message))
  }
}

// 重置分类表单
const resetCategoryForm = () => {
  Object.keys(categoryForm).forEach(key => {
    if (key === 'sortOrder') {
      categoryForm[key] = 1
    } else if (key === 'isActive') {
      categoryForm[key] = true
    } else {
      categoryForm[key] = ''
    }
  })
}

// 保存分类
const categoryFormRef = ref()
const handleSaveCategory = async () => {
  if (!categoryFormRef.value) return
  
  try {
    await categoryFormRef.value.validate()
    saving.value = true
    
    if (editingCategory.value) {
      // 编辑分类
      await request.put(`/equipment-categories/${editingCategory.value.id}`, categoryForm)
      ElMessage.success('分类更新成功')
    } else {
      // 新增分类
      await request.post('/equipment-categories', categoryForm)
      ElMessage.success('分类创建成功')
    }
    
    editingCategory.value = null
    resetCategoryForm()
    fetchAllCategories()
    fetchCategories() // 刷新设备表单的分类列表
  } catch (error) {
    console.error('保存分类失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('保存分类失败')
    }
  } finally {
    saving.value = false
  }
}

// 编辑分类
const handleEditCategory = (category) => {
  editingCategory.value = category
  Object.keys(categoryForm).forEach(key => {
    categoryForm[key] = category[key] || (key === 'sortOrder' ? 1 : (key === 'isActive' ? true : ''))
  })
}

// 取消编辑分类
const handleCancelCategory = () => {
  editingCategory.value = null
  resetCategoryForm()
}

// 切换分类状态
const handleToggleCategoryStatus = async (category) => {
  try {
    await request.put(`/equipment-categories/${category.id}/toggle-status`)
    ElMessage.success(`分类已${category.isActive ? '禁用' : '启用'}`)
    fetchAllCategories()
    fetchCategories() // 刷新设备表单的分类列表
  } catch (error) {
    console.error('切换分类状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除分类
const handleDeleteCategory = async (category) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类 "${category.name}" 吗？此操作不可恢复！`, 
      '确认删除', 
      {
        type: 'error',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    await request.delete(`/equipment-categories/${category.id}`)
    ElMessage.success('分类删除成功')
    fetchAllCategories()
    fetchCategories() // 刷新设备表单的分类列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      if (error.response?.data?.message) {
        ElMessage.error(error.response.data.message)
      } else {
        ElMessage.error('删除分类失败')
      }
    }
  }
}

// 数据清理相关方法
const fetchCleanupStatistics = async () => {
  try {
    console.log('正在获取设备清理统计信息...')
    const response = await request.get('/equipment/cleanup/statistics')
    console.log('设备统计API完整响应:', response)
    
    // 修复：正确检查响应格式，request.js已经处理了ApiResponse结构
    if (response && response.success !== false) {
      cleanupStats.value = response.data
      console.log('设备统计数据设置成功:', cleanupStats.value)
    } else {
      console.error('设备统计API返回错误:', response?.message)
      throw new Error(response?.message || '获取设备统计信息失败')
    }
  } catch (error) {
    console.error('获取清理统计失败:', error)
    ElMessage.error(`获取清理统计失败: ${error.message || error}`)
  }
}

const fetchDeletedEquipments = async () => {
  try {
    cleaning.value = true
    const response = await request.get('/equipment/deleted', {
      params: {
        page: cleanupPagination.page - 1,
        size: cleanupPagination.size
      }
    })
    console.log('设备删除记录API响应完整数据:', response)
    
    // 修复：正确检查响应格式，request.js已经处理了ApiResponse结构
    if (response && response.success !== false) {
      deletedEquipments.value = response.data.content || []
      cleanupPagination.total = response.data.totalElements || 0
      console.log('成功获取已删除设备:', deletedEquipments.value.length, '条')
    } else {
      console.error('设备删除记录API返回错误:', response?.message)
      ElMessage.error(response?.message || '获取已删除设备失败')
    }
  } catch (error) {
    console.error('获取已删除设备失败:', error)
    if (error.response) {
      console.error('设备错误响应状态:', error.response.status)
      console.error('设备错误响应数据:', error.response.data)
      ElMessage.error(`请求失败: ${error.response.status} - ${error.response.data?.message || '未知错误'}`)
    } else if (error.request) {
      console.error('设备请求未收到响应:', error.request)
      ElMessage.error('网络请求失败，请检查后端服务是否正常运行')
    } else {
      console.error('设备请求设置错误:', error.message)
      ElMessage.error(`请求错误: ${error.message}`)
    }
  } finally {
    cleaning.value = false
  }
}

const handleCleanupPageChange = (page) => {
  cleanupPagination.page = page
  fetchDeletedEquipments()
}

const handlePhysicalDeleteEquipment = async (equipment) => {
  try {
    await ElMessageBox.confirm(
      `确定要永久删除"${equipment.name}"吗？删除后无法恢复！`,
      '永久删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    const response = await request.delete(`/equipment/cleanup/${equipment.id}`)
    console.log('设备单个删除API响应:', response)
    
    // 修复：正确检查响应格式，request.js已经处理了ApiResponse结构
    if (response && response.success !== false) {
      ElMessage.success('设备已永久删除')
      fetchDeletedEquipments()
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

const handleBatchPhysicalDeleteEquipments = async () => {
  if (selectedDeletedEquipments.value.length === 0) {
    ElMessage.warning('请选择要删除的设备')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要永久删除选中的 ${selectedDeletedEquipments.value.length} 个设备吗？删除后无法恢复！`,
      '批量永久删除确认',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    const response = await request.delete('/equipment/cleanup/batch', {
      data: selectedDeletedEquipments.value
    })
    console.log('设备批量删除API响应:', response)
    
    // 修复：正确检查响应格式，request.js已经处理了ApiResponse结构
    if (response && response.success !== false) {
      ElMessage.success(`成功删除 ${selectedDeletedEquipments.value.length} 个设备`)
      selectedDeletedEquipments.value = []
      fetchDeletedEquipments()
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

const handleAutoCleanupEquipments = async (daysOld) => {
  try {
    await ElMessageBox.confirm(
      `确定要自动清理 ${daysOld} 天前的已删除设备吗？此操作将永久删除这些数据！`,
      '自动清理确认',
      {
        type: 'warning',
        confirmButtonText: '确定清理',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    const response = await request.delete('/equipment/cleanup/auto', {
      params: { daysOld }
    })
    // 修复：正确检查响应格式，request.js已经处理了ApiResponse结构
    if (response && response.success !== false) {
      ElMessage.success(response.data || '自动清理完成')
      fetchDeletedEquipments()
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

const handleCleanupDialogOpen = () => {
  fetchCleanupStatistics()
  fetchDeletedEquipments()
}

const handleCleanupDialogClose = () => {
  selectedDeletedEquipments.value = []
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

const getEquipmentStatusType = (status) => {
  switch (status) {
    case '正常': return 'success'
    case '借出': return 'info'
    case '损坏': return 'danger'
    case '维修中': return 'warning'
    case '报废': return 'danger'
    default: return 'success'
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 组件挂载时获取数据
// 检查是否为移动端
const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

// 处理窗口大小变化
const handleResize = () => {
  checkMobile()
}

onMounted(() => {
  fetchCategories()
  fetchEquipment()
  
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
/* 现代化设备管理页面 */
.equipment-management {
  max-width: 1400px;
  margin: 0 auto;
  min-height: auto;
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
  border-radius: 18px;
  box-shadow: 0 14px 36px rgba(18, 85, 116, 0.08);
  margin-bottom: var(--spacing-5);
  border: 1px solid rgba(98, 177, 210, 0.16);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition: border-color var(--duration-fast) var(--easing-ease), box-shadow var(--duration-fast) var(--easing-ease);
}

.search-card:hover,
.table-card:hover {
  border-color: rgba(24, 185, 236, 0.24);
  box-shadow: 0 18px 44px rgba(18, 85, 116, 0.1);
}

.search-card :deep(.el-card__body) {
  padding: 18px 20px;
}

.table-card :deep(.el-card__header) {
  padding: 18px 20px;
  border-bottom: 1px solid rgba(98, 177, 210, 0.14);
}

.table-card :deep(.el-card__body) {
  padding: 0 20px 18px;
}

.toolbar-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-group {
  display: grid;
  grid-template-columns: minmax(260px, 1.1fr) minmax(220px, 1fr) auto;
  gap: 14px;
  align-items: center;
}

.filter-group :deep(.el-input),
.filter-group :deep(.el-select) {
  width: 100%;
}

/* 基础搜索操作 */
.basic-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-start;
  white-space: nowrap;
}

.basic-actions .el-button + .el-button,
.admin-actions .el-button + .el-button {
  margin-left: 0;
}

/* 管理员操作行 */
.admin-actions-row {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f2f5;
}

.admin-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
}

.admin-actions .el-button {
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

.admin-actions .el-button .el-icon {
  font-size: 14px;
}

.admin-actions .el-button span {
  white-space: nowrap;
}

/* 中等屏幕优化 */
@media (max-width: 1024px) and (min-width: 769px) {
  .filter-group {
    grid-template-columns: minmax(220px, 1fr) minmax(190px, 0.8fr) auto;
    gap: 12px;
  }

  .admin-actions {
    gap: 10px;
  }
  
  .admin-actions .el-button {
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

.equipment-image {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.equipment-image .el-image {
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.equipment-image .el-image:hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.no-image {
  width: 60px;
  height: 60px;
  border: 2px dashed #e4e7ed;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.image-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 12px;
}

.stock-info {
  font-size: 12px;
}

.stock-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 2px;
}

.stock-label {
  color: #909399;
}

.stock-value {
  font-weight: 500;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  align-items: center;
}

.action-buttons .el-button {
  margin: 0;
  padding: 4px 8px;
}

/* 清除可能的伪元素或装饰 */
.action-buttons::before,
.action-buttons::after {
  display: none !important;
}

.action-buttons .el-button::before,
.action-buttons .el-button::after {
  display: none !important;
}

/* 确保操作列内容正确对齐 */
.el-table .el-table__cell {
  position: relative;
}

.el-table .el-table__cell::before,
.el-table .el-table__cell::after {
  display: none !important;
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

/* 设备弹窗样式 */
.equipment-dialog .el-dialog__body {
  padding: 20px;
}

.equipment-form .el-form-item {
  margin-bottom: 18px;
}

.equipment-form .el-form-item__label {
  font-weight: 500;
  color: #606266;
}

.equipment-form .el-input-number {
  width: 100%;
}

.equipment-form .el-input-number .el-input__inner {
  text-align: left;
}

.image-upload-container {
  text-align: center;
}

.current-image {
  margin-bottom: 20px;
}

.current-image h4,
.upload-section h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.equipment-uploader {
  width: 100%;
}

.equipment-uploader :deep(.el-upload-dragger) {
  width: 100%;
  height: 180px;
}

/* 图片管理对话框样式 */
.image-management-container {
  max-height: 70vh;
  overflow-y: auto;
}

.equipment-info-header {
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}

.equipment-basic-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.equipment-basic-info p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.current-image-section,
.upload-section {
  margin-bottom: 24px;
}

.current-image-section h4,
.upload-section h4 {
  display: flex;
  align-items: center;
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-icon {
  margin-right: 8px;
  color: #409eff;
  font-size: 18px;
}

.image-display {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.image-error-slot {
  width: 300px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: #f5f7fa;
  border-radius: 12px;
  border: 2px solid #e4e7ed;
}

.image-error-slot p {
  margin: 8px 0 0 0;
  font-size: 14px;
}

.no-current-image {
  width: 300px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  background: #fafafa;
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.no-current-image:hover {
  border-color: #409eff;
  color: #409eff;
}

.no-current-image p {
  margin: 12px 0 8px 0;
  font-size: 16px;
  font-weight: 500;
}

.no-current-image span {
  font-size: 13px;
  color: #909399;
}

.upload-section .equipment-uploader {
  margin-top: 12px;
}

.upload-section :deep(.el-upload-dragger) {
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  padding: 40px 20px;
  transition: all 0.3s ease;
}

.upload-section :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.upload-section :deep(.el-upload__tip) {
  margin-top: 12px;
  line-height: 1.5;
}

.upload-section :deep(.el-upload__tip p) {
  margin: 4px 0;
  color: #909399;
  font-size: 13px;
}

/* 设备图片预览样式 */
.equipment-image-preview {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.image-container {
  flex-shrink: 0;
}

.image-container .no-image {
  width: 200px;
  height: 150px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: #fff;
}

.image-container .no-image p {
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
}

.image-slot p {
  margin: 8px 0 0 0;
  font-size: 14px;
}

.image-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.image-info h4 {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.image-info p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
}

.image-info .equipment-category {
  color: #409eff;
  font-weight: 500;
}

.image-info .equipment-serial {
  color: #909399;
  font-size: 13px;
}

/* 修复图片预览可能的样式冲突 */
.el-image-viewer__wrapper {
  z-index: 9999 !important;
}

.el-image-viewer__mask {
  background-color: rgba(0, 0, 0, 0.8) !important;
}

/* 确保预览时不影响页面布局 */
.el-image__preview {
  cursor: zoom-in;
}

.el-image__inner {
  transition: transform 0.3s ease;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .toolbar-layout {
    gap: 12px;
  }

  .filter-group {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .basic-actions {
    flex-direction: row;
    width: 100%;
    gap: 10px;
  }
  
  .basic-actions .el-button {
    flex: 1 1 0;
    width: auto;
    justify-content: center;
  }
  
  .admin-actions-row {
    margin-top: 12px;
    padding-top: 12px;
  }
  
  .admin-actions {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
    justify-content: stretch;
  }
  
  .admin-actions .el-button {
    width: 100%;
    min-width: auto;
    margin-left: 0;
    height: 40px;
    font-size: 14px;
    justify-content: center;
    align-items: center;
  }
  
  /* 移动端弹窗优化 */
  .equipment-dialog .el-dialog {
    margin: 5vh auto;
  }
  
  .equipment-dialog .el-dialog__header {
    padding: 16px 20px 10px;
  }
  
  .equipment-dialog .el-dialog__body {
    padding: 10px 20px 20px;
  }
  
  .equipment-dialog .el-dialog__footer {
    padding: 10px 20px 20px;
  }
  
  .equipment-form .el-form-item {
    margin-bottom: 16px;
  }
  
  .equipment-form .el-form-item__label {
    font-size: 14px;
    line-height: 1.4;
  }
  
  .equipment-form .el-input,
  .equipment-form .el-select,
  .equipment-form .el-textarea {
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
  .equipment-management {
    padding: 0 10px;
  }
}

/* 分类管理样式优化 */
.category-dialog {
  min-width: 320px;
}

.category-management {
  padding: 0 8px;
}

.category-management .category-form-card {
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.category-management .category-list-card {
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.category-management .el-card__header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #e6e6e6;
  font-weight: 500;
  padding: 16px 20px;
}

.category-management .list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.category-form .el-form-item {
  margin-bottom: 18px;
}

.category-form .status-form-item .el-form-item__content {
  justify-content: flex-start;
}

.category-form .form-buttons {
  margin-top: 24px;
  margin-bottom: 0;
}

.category-form .button-group {
  display: flex;
  gap: 12px;
  justify-content: flex-start;
}

.category-management .el-table th {
  background-color: #fafafa;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

/* 移动端分类列表样式 */
.mobile-category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fff;
}

.category-info {
  flex: 1;
  min-width: 0;
}

.category-name {
  font-weight: 500;
  font-size: 16px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.category-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.4;
}

.category-sort {
  font-size: 12px;
  color: #909399;
}

.category-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex-shrink: 0;
  margin-left: 12px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .category-dialog .el-dialog {
    width: 95% !important;
    margin: 5vh auto !important;
  }
  
  .category-management {
    padding: 0 4px;
  }
  
  .category-form .button-group {
    flex-direction: column;
  }
  
  .category-form .button-group .el-button {
    width: 100%;
    margin: 0;
  }
  
  .mobile-category-item {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .category-actions {
    flex-direction: row;
    justify-content: center;
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .category-management .el-card__header {
    padding: 12px 16px;
  }
  
  .category-form {
    padding: 16px;
  }
  
  .list-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

/* 设备详情查看样式 */
:deep(.equipment-view-dialog.el-dialog),
:deep(.equipment-view-dialog .el-dialog) {
  width: min(92vw, 860px) !important;
  border: 1px solid rgba(255, 255, 255, 0.72) !important;
  border-radius: 28px !important;
  background:
    radial-gradient(circle at 88% 10%, rgba(75, 211, 180, 0.12), transparent 32%),
    linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(238, 251, 255, 0.9)) !important;
  box-shadow: 0 34px 90px rgba(18, 85, 116, 0.22) !important;
  overflow: hidden;
}

:deep(.equipment-view-dialog.el-dialog .el-dialog__header),
:deep(.equipment-view-dialog .el-dialog__header) {
  padding: 24px 28px 18px !important;
  margin: 0 !important;
  border-bottom: 1px solid rgba(98, 177, 210, 0.18);
}

:deep(.equipment-view-dialog.el-dialog .el-dialog__body),
:deep(.equipment-view-dialog .el-dialog__body) {
  padding: 18px 20px 0 !important;
  background: linear-gradient(180deg, rgba(240, 251, 255, 0.28), rgba(255, 255, 255, 0.16));
}

:deep(.equipment-view-dialog.el-dialog .el-dialog__footer),
:deep(.equipment-view-dialog .el-dialog__footer) {
  padding: 16px 24px 22px !important;
  border-top: 1px solid rgba(98, 177, 210, 0.18);
  background: rgba(255, 255, 255, 0.6);
}

:deep(.equipment-view-dialog .el-dialog__headerbtn) {
  top: 22px;
  right: 22px;
  width: 34px;
  height: 34px;
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
}

.detail-dialog-header {
  padding-right: 44px;
}

.detail-dialog-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-dialog-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  color: #0876a5;
  background: var(--button-primary-bg);
  border: 1px solid var(--button-primary-border);
  border-radius: 14px;
  box-shadow: 0 10px 24px rgba(24, 185, 236, 0.12);
}

.detail-dialog-title h3 {
  margin: 0;
  color: #123044;
  font-size: 22px;
  font-weight: 800;
  line-height: 1.2;
}

.detail-dialog-title p {
  margin: 5px 0 0;
  color: #6b879a;
  font-size: 13px;
}

.equipment-detail-view {
  max-height: min(68vh, 720px);
  padding: 0 4px 18px 0;
  overflow-y: auto;
}

.detail-card {
  margin-bottom: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 22px;
  box-shadow: 0 12px 28px rgba(18, 174, 231, 0.07);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.detail-card:last-child {
  margin-bottom: 0;
}

.equipment-detail-view .card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #123044;
}

.detail-card :deep(.el-card__header) {
  padding: 15px 20px !important;
  background: rgba(248, 253, 255, 0.78) !important;
  border-bottom: 1px solid rgba(98, 177, 210, 0.16) !important;
}

.detail-card :deep(.el-card__body) {
  padding: 20px 22px !important;
}

.header-icon {
  margin-right: 8px;
  color: #18aee6;
  font-size: 18px;
}

.header-title {
  font-size: 16px;
}

.detail-content {
  padding: 0;
}

.equipment-header {
  display: flex;
  gap: 22px;
  margin-bottom: 24px;
  align-items: flex-start;
}

.equipment-image-large {
  flex-shrink: 0;
}

.no-image-large {
  width: 200px;
  height: 150px;
  border: 1px dashed rgba(148, 163, 184, 0.38);
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: rgba(248, 250, 252, 0.78);
}

.no-image-large p {
  margin: 8px 0 0 0;
  font-size: 14px;
}

.image-slot-large {
  width: 200px;
  height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  background: rgba(248, 250, 252, 0.78);
  border-radius: 18px;
}

.image-slot-large p {
  margin: 8px 0 0 0;
  font-size: 14px;
}

.equipment-title-info {
  flex: 1;
  padding-top: 8px;
}

.equipment-name {
  font-size: 24px;
  font-weight: 800;
  color: #123044;
  margin: 0 0 12px 0;
  line-height: 1.3;
}

.equipment-meta {
  margin-bottom: 12px;
}

.equipment-serial {
  margin: 0;
  color: #7691a4;
  font-size: 14px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 7px;
  min-height: 74px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.64);
  border: 1px solid rgba(98, 177, 210, 0.14);
  border-radius: 16px;
}

.detail-label {
  margin-bottom: 0;
  color: #6b879a;
  font-size: 12px;
  font-weight: 700;
}

.detail-value {
  color: #123044;
  font-size: 15px;
  font-weight: 700;
}

.stock-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.stock-item-detail {
  min-height: 100px;
  padding: 18px 12px;
  text-align: center;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.86), rgba(240, 251, 255, 0.72));
  border: 1px solid rgba(98, 177, 210, 0.14);
  border-radius: 18px;
}

.stock-number {
  font-family: var(--font-family-mono);
  font-size: 34px;
  font-weight: 800;
  color: #123044;
  margin-bottom: 8px;
  line-height: 1;
}

.stock-number.available {
  color: #21b98b;
}

.stock-number.borrowed {
  color: #f4a62a;
}

.stock-number.damaged {
  color: #f05268;
}

.stock-label {
  color: #6b879a;
  font-size: 13px;
  font-weight: 700;
}

.specs-content,
.description-content {
  padding: 2px 2px 4px;
  color: #496579;
  font-size: 15px;
  line-height: 1.7;
}

.specs-content p,
.description-content p {
  margin: 0;
  word-break: break-word;
  white-space: pre-wrap;
}

.detail-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.dialog-action {
  min-width: 104px !important;
  height: 42px !important;
  padding: 0 18px !important;
  border-radius: 999px !important;
  font-weight: 800 !important;
}

.dialog-action .el-icon {
  margin-right: 6px;
}

.dialog-action.secondary-action {
  color: #496579 !important;
  background: rgba(255, 255, 255, 0.78) !important;
  border: 1px solid rgba(98, 177, 210, 0.24) !important;
}

.dialog-action.primary-action {
  color: #0876a5 !important;
  background: var(--button-primary-bg) !important;
  border: 1px solid var(--button-primary-border) !important;
  box-shadow: 0 12px 28px rgba(24, 185, 236, 0.14) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .equipment-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .equipment-image-large,
  .no-image-large,
  .image-slot-large {
    width: 100%;
    max-width: 300px;
    margin: 0 auto;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .stock-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }
  
  .equipment-name {
    font-size: 20px;
    text-align: center;
  }
  
  .equipment-meta {
    justify-content: center;
  }
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
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #1976d2;
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

.equipment-category {
  font-size: 12px;
  color: #1976d2;
  background: #e3f2fd;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  width: fit-content;
}

.equipment-serial {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.stock-info {
  font-size: 12px;
  line-height: 1.5;
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

/* 移动端卡片视图 */
.mobile-cards {
  display: none !important;
}

.equipment-management .desktop-table {
  display: block !important;
}

.equipment-management .mobile-cards {
  display: none !important;
}

.desktop-table :deep(.el-table__empty-block) {
  min-height: 360px;
  width: 100% !important;
}

.desktop-table :deep(.el-table__empty-text) {
  width: 100%;
  line-height: normal;
}

.equipment-management .mobile-cards > .empty-state {
  grid-column: 1 / -1;
  min-height: 320px;
}

.equipment-card {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  grid-template-areas:
    "header header"
    "content actions";
  column-gap: 18px;
  row-gap: 14px;
  align-items: end;
  min-height: 188px;
  margin-bottom: 0;
  padding: 22px;
  background:
    radial-gradient(circle at 92% 12%, rgba(229, 249, 255, 0.74), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(240, 251, 255, 0.78));
  border: 1px solid rgba(98, 177, 210, 0.2);
  border-radius: 22px;
  box-shadow: 0 12px 28px rgba(18, 174, 231, 0.08);
  cursor: pointer;
  transition: transform var(--duration-normal) var(--easing-spring), border-color var(--duration-fast) var(--easing-ease), box-shadow var(--duration-fast) var(--easing-ease);
}

.equipment-card:hover {
  transform: translateY(-2px);
  border-color: rgba(24, 185, 236, 0.34);
  box-shadow: 0 18px 42px rgba(18, 174, 231, 0.14);
}

.card-header {
  grid-area: header;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 0;
}

.equipment-image-mobile {
  flex-shrink: 0;
}

.no-image-mobile {
  width: 62px;
  height: 62px;
  border-radius: 10px;
  background: rgba(248, 250, 252, 0.88);
  border: 1px solid rgba(226, 232, 240, 0.82);
  display: flex;
  align-items: center;
  justify-content: center;
}

.equipment-info-mobile {
  flex: 1;
  min-width: 0;
}

.equipment-name-mobile {
  margin: 0 0 10px 0;
  color: #123044;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.35;
}

.equipment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.serial-number {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.equipment-status {
  flex-shrink: 0;
  margin-left: auto;
}

.card-content {
  grid-area: content;
  align-self: end;
  min-width: 0;
  margin-bottom: 0;
}

.stock-info-mobile {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.stock-item-mobile {
  display: flex;
  align-items: center;
  gap: 6px;
  min-height: 30px;
  padding: 5px 10px;
  font-size: 13px;
  line-height: 1;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 999px;
}

.stock-label {
  color: #6b879a;
  font-size: 12px;
  font-weight: 600;
}

.stock-value {
  color: #123044;
  font-family: var(--font-family-mono);
  font-size: 15px;
  font-weight: 700;
}

.specifications-mobile {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  min-height: 30px;
  padding: 5px 10px;
  font-size: 13px;
  line-height: 1.2;
  background: rgba(239, 252, 255, 0.72);
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 999px;
}

.spec-label {
  flex: 0 0 auto;
  color: #6b879a;
  font-size: 12px;
  font-weight: 600;
}

.spec-content {
  min-width: 0;
  overflow: hidden;
  color: #123044;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  border-top: 1px solid rgba(98, 177, 210, 0.18);
  padding-top: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .equipment-management {
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
  
  .admin-actions .el-button {
    height: 42px;
    font-size: 13px;
    padding: 10px 16px;
    font-weight: 500;
  }
  
  .equipment-management .desktop-table {
    display: none !important;
  }

  .equipment-management .mobile-cards {
    display: grid !important;
    grid-template-columns: 1fr;
    gap: 14px;
  }
  
  .pagination-container {
    margin-top: 12px;
  }
  
  .pagination-container .el-pagination {
    justify-content: center;
  }
  
  /* 数据清理对话框响应式 */
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

@media (max-width: 480px) {
  .equipment-management {
    padding: 0 4px;
  }
  
  .equipment-card {
    padding: 12px;
  }
  
  /* 手机端弹窗全屏优化 */
  .equipment-dialog .el-dialog {
    margin: 0;
    width: 100% !important;
    height: 100vh;
    max-height: none;
    border-radius: 0;
    display: flex;
    flex-direction: column;
  }
  
  .equipment-dialog .el-dialog__header {
    padding: 12px 16px 8px;
    border-bottom: 1px solid #ebeef5;
    flex-shrink: 0;
  }
  
  .equipment-dialog .el-dialog__title {
    font-size: 16px;
  }
  
  .equipment-dialog .el-dialog__body {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
  }
  
  .equipment-dialog .el-dialog__footer {
    padding: 12px 16px 16px;
    border-top: 1px solid #ebeef5;
    flex-shrink: 0;
  }
  
  .equipment-form .el-form-item {
    margin-bottom: 12px;
  }
  
  .equipment-form .el-form-item__label {
    font-size: 13px;
    text-align: left !important;
    padding-right: 8px;
  }
  
  .equipment-form .el-input,
  .equipment-form .el-select,
  .equipment-form .el-textarea {
    font-size: 14px;
  }
  
  .equipment-form .el-input-number {
    font-size: 14px;
  }
  
  .equipment-name-mobile {
    font-size: 15px;
  }
  
  .stock-info-mobile {
    flex-direction: column;
    gap: 8px;
  }
  
  .card-actions .el-button {
    padding: 4px 8px;
  }
}

/* Fresh icon action group for equipment rows */
.desktop-table :deep(.operations-column .cell) {
  display: flex;
  justify-content: center;
  overflow: visible;
  padding-left: 10px;
  padding-right: 10px;
}

.row-actions {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-wrap: nowrap;
  gap: 6px;
  width: 152px;
  max-width: 100%;
  padding: 4px;
  border: 1px solid rgba(98, 177, 210, 0.2);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.86), 0 8px 18px rgba(18, 174, 231, 0.08);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
}

.row-actions:has(.icon-action:only-child) {
  width: 42px;
}

.row-actions .icon-action,
.card-actions.row-actions .icon-action {
  width: 30px !important;
  height: 30px !important;
  min-width: 30px !important;
  min-height: 30px !important;
  margin: 0 !important;
  padding: 0 !important;
  border-radius: 999px !important;
  border: 1px solid rgba(98, 177, 210, 0.2) !important;
  background: rgba(255, 255, 255, 0.82) !important;
  color: #0876a5 !important;
  box-shadow: none !important;
  transform: none !important;
}

.row-actions .icon-action :deep(.el-icon) {
  margin: 0 !important;
  font-size: 15px;
}

.row-actions .icon-action:hover {
  transform: translateY(-1px) !important;
}

.row-actions .view-action:hover {
  border-color: rgba(24, 185, 236, 0.42) !important;
  background: #e8f9ff !important;
  color: #067aa8 !important;
}

.row-actions .edit-action {
  color: #0f8f72 !important;
}

.row-actions .edit-action:hover {
  border-color: rgba(33, 185, 139, 0.42) !important;
  background: #e7fbf4 !important;
  color: #087f63 !important;
}

.row-actions .image-action {
  color: #8a6508 !important;
}

.row-actions .image-action:hover {
  border-color: rgba(244, 185, 66, 0.46) !important;
  background: #fff6dc !important;
  color: #815008 !important;
}

.row-actions .delete-action {
  color: #b4233e !important;
}

.row-actions .delete-action:hover {
  border-color: rgba(240, 82, 104, 0.46) !important;
  background: #fff0f3 !important;
  color: #941b32 !important;
}

.card-actions.row-actions {
  grid-area: actions;
  align-self: end;
  justify-self: end;
  justify-content: center;
  width: auto;
  padding: 5px;
  border: 1px solid rgba(98, 177, 210, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.68);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.86), 0 8px 18px rgba(18, 174, 231, 0.08);
}

@media (max-width: 480px) {
  .equipment-card {
    grid-template-columns: 1fr;
    grid-template-areas:
      "header"
      "content"
      "actions";
    row-gap: 12px;
  }

  .card-actions.row-actions {
    justify-self: start;
    gap: 8px;
  }

  .card-actions.row-actions .icon-action {
    width: 34px !important;
    height: 34px !important;
    min-width: 34px !important;
    min-height: 34px !important;
  }
}
</style>
