<template>
  <div class="modern-table-container">
    <!-- 表格头部工具栏 -->
    <div v-if="$slots.toolbar || title" class="table-toolbar">
      <slot name="toolbar">
        <div class="toolbar-content">
          <div class="toolbar-title">
            <el-icon v-if="icon" class="toolbar-icon">
              <component :is="icon" />
            </el-icon>
            <span class="title-text">{{ title }}</span>
            <el-tag v-if="total !== undefined" type="info" size="small">
              共 {{ total }} 条
            </el-tag>
          </div>
          <div v-if="$slots.actions" class="toolbar-actions">
            <slot name="actions"></slot>
          </div>
        </div>
      </slot>
    </div>

    <!-- 现代化表格 -->
    <div class="table-wrapper">
      <el-table
        ref="tableRef"
        v-bind="$attrs"
        :class="['modern-table', { 'striped': striped, 'hover': hover }]"
        v-on="$listeners"
      >
        <slot></slot>
        
        <!-- 空状态 -->
        <template #empty>
          <div class="table-empty">
            <el-icon class="empty-icon"><DocumentDelete /></el-icon>
            <p class="empty-text">{{ emptyText }}</p>
          </div>
        </template>
      </el-table>
    </div>

    <!-- 分页 -->
    <div v-if="pagination && total > 0" class="table-pagination">
      <el-pagination
        v-bind="paginationProps"
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
        class="modern-pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits, defineSlots } from 'vue'
import { DocumentDelete } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: ''
  },
  striped: {
    type: Boolean,
    default: true
  },
  hover: {
    type: Boolean,
    default: true
  },
  emptyText: {
    type: String,
    default: '暂无数据'
  },
  pagination: {
    type: Boolean,
    default: true
  },
  total: {
    type: Number,
    default: 0
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 20
  },
  paginationProps: {
    type: Object,
    default: () => ({
      background: true,
      layout: 'total, sizes, prev, pager, next, jumper',
      pageSizes: [10, 20, 50, 100]
    })
  }
})

const emit = defineEmits(['current-change', 'size-change'])
defineSlots()

const tableRef = ref()

const handleCurrentChange = (page) => {
  emit('current-change', page)
}

const handleSizeChange = (size) => {
  emit('size-change', size)
}

// 暴露表格实例方法
const clearSelection = () => {
  tableRef.value?.clearSelection()
}

const toggleRowSelection = (row, selected) => {
  tableRef.value?.toggleRowSelection(row, selected)
}

const toggleAllSelection = () => {
  tableRef.value?.toggleAllSelection()
}

const setCurrentRow = (row) => {
  tableRef.value?.setCurrentRow(row)
}

defineExpose({
  clearSelection,
  toggleRowSelection,
  toggleAllSelection,
  setCurrentRow,
  tableRef
})
</script>

<style scoped>
/* 现代化表格容器 */
.modern-table-container {
  background: var(--color-white);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  transition: all var(--duration-normal) var(--easing-ease);
}

.modern-table-container:hover {
  box-shadow: var(--shadow-xl);
}

/* 表格工具栏 */
.table-toolbar {
  padding: var(--spacing-4) var(--spacing-6);
  border-bottom: 1px solid var(--color-divider);
  background: var(--color-secondary-50);
}

.toolbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-4);
}

.toolbar-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-3);
  flex: 1;
  min-width: 0;
}

.toolbar-icon {
  font-size: var(--font-size-xl);
  color: var(--color-primary-500);
  flex-shrink: 0;
}

.title-text {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-2);
  flex-shrink: 0;
  flex-wrap: wrap;
}

/* 表格包装器 */
.table-wrapper {
  position: relative;
}

/* 现代化表格样式 */
.modern-table {
  width: 100%;
}

.modern-table :deep(.el-table__header-wrapper) {
  background: var(--color-secondary-50);
}

.modern-table :deep(.el-table__header th) {
  background: var(--color-secondary-50);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-semibold);
  border-bottom: 2px solid var(--color-divider);
  padding: var(--spacing-4) var(--spacing-3);
}

.modern-table :deep(.el-table__body tr) {
  transition: background-color var(--duration-normal) var(--easing-ease);
}

.modern-table.hover :deep(.el-table__body tr:hover) {
  background: var(--color-primary-50);
}

.modern-table.striped :deep(.el-table__body tr:nth-child(even)) {
  background: rgba(0, 0, 0, 0.02);
}

.modern-table :deep(.el-table__body .el-table__cell) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  padding: var(--spacing-4) var(--spacing-3);
}

.modern-table :deep(.el-table__row--selected) {
  background: var(--color-primary-100) !important;
}

.modern-table :deep(.el-table__row--selected:hover) {
  background: var(--color-primary-200) !important;
}

/* 空状态样式 */
.table-empty {
  padding: var(--spacing-10) var(--spacing-6);
  text-align: center;
  color: var(--color-text-placeholder);
}

.empty-icon {
  font-size: 64px;
  color: var(--color-text-disabled);
  margin-bottom: var(--spacing-4);
}

.empty-text {
  font-size: var(--font-size-lg);
  margin: 0;
}

/* 现代化分页 */
.table-pagination {
  padding: var(--spacing-4) var(--spacing-6);
  border-top: 1px solid var(--color-divider);
  background: var(--color-secondary-50);
  display: flex;
  justify-content: center;
}

.modern-pagination {
  background: transparent;
}

.modern-pagination :deep(.el-pagination__total),
.modern-pagination :deep(.el-pagination__sizes),
.modern-pagination :deep(.el-pagination__jump) {
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
}

.modern-pagination :deep(.btn-prev),
.modern-pagination :deep(.btn-next),
.modern-pagination :deep(.el-pager li) {
  background: var(--color-white);
  border: 1px solid var(--color-divider);
  border-radius: var(--radius-base);
  margin: 0 var(--spacing-1);
  transition: all var(--duration-normal) var(--easing-ease);
}

.modern-pagination :deep(.btn-prev:hover),
.modern-pagination :deep(.btn-next:hover),
.modern-pagination :deep(.el-pager li:hover) {
  background: var(--color-primary-500);
  color: var(--color-white);
  border-color: var(--color-primary-500);
  transform: translateY(-1px);
}

.modern-pagination :deep(.el-pager li.is-active) {
  background: var(--color-primary-500);
  color: var(--color-white);
  border-color: var(--color-primary-500);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .table-toolbar {
    padding: var(--spacing-3) var(--spacing-4);
  }
  
  .toolbar-content {
    flex-direction: column;
    align-items: stretch;
    gap: var(--spacing-3);
  }
  
  .toolbar-title {
    justify-content: center;
  }
  
  .toolbar-actions {
    justify-content: center;
  }
  
  .table-pagination {
    padding: var(--spacing-3) var(--spacing-4);
  }
  
  .modern-pagination :deep(.el-pagination__sizes),
  .modern-pagination :deep(.el-pagination__jump) {
    display: none;
  }
  
  .modern-pagination {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .modern-table :deep(.el-table__header th),
  .modern-table :deep(.el-table__body .el-table__cell) {
    padding: var(--spacing-2);
    font-size: var(--font-size-sm);
  }
  
  .title-text {
    font-size: var(--font-size-base);
  }
  
  .toolbar-icon {
    font-size: var(--font-size-lg);
  }
  
  .empty-icon {
    font-size: 48px;
  }
  
  .empty-text {
    font-size: var(--font-size-base);
  }
}
</style>
