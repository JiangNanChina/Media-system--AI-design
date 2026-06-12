package com.example.photography.service;

import com.example.photography.model.entity.EquipmentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 设备分类服务接口
 */
public interface EquipmentCategoryService {
    
    /**
     * 创建设备分类
     */
    EquipmentCategory createCategory(EquipmentCategory category);
    
    /**
     * 更新设备分类
     */
    EquipmentCategory updateCategory(Long id, EquipmentCategory category);
    
    /**
     * 删除设备分类（软删除）
     */
    void deleteCategory(Long id);
    
    /**
     * 根据ID获取分类
     */
    EquipmentCategory findById(Long id);
    
    /**
     * 获取所有分类（分页）
     */
    Page<EquipmentCategory> findAll(Pageable pageable);
    
    /**
     * 获取所有激活的分类（列表）
     */
    List<EquipmentCategory> findAllActive();
    
    /**
     * 获取所有分类（列表）
     */
    List<EquipmentCategory> findAllList();
    
    /**
     * 根据关键字搜索分类
     */
    Page<EquipmentCategory> searchCategories(String keyword, Pageable pageable);
    
    /**
     * 根据名称查找分类
     */
    java.util.Optional<EquipmentCategory> findByName(String name);
    
    /**
     * 保存分类
     */
    EquipmentCategory save(EquipmentCategory category);
    
    /**
     * 检查分类名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 检查分类名称是否存在（排除指定ID）
     */
    boolean existsByNameAndIdNot(String name, Long excludeId);
    
    /**
     * 启用/禁用分类
     */
    void toggleCategoryStatus(Long id);
    
    /**
     * 调整分类排序
     */
    void updateSortOrder(Long id, Integer sortOrder);
    
    /**
     * 批量更新排序
     */
    void batchUpdateSortOrder(List<Long> categoryIds);
    
    /**
     * 获取分类统计信息
     */
    CategoryStatistics getCategoryStatistics();
    
    /**
     * 分类统计信息类
     */
    class CategoryStatistics {
        private long totalCategories;
        private long activeCategories;
        private long inactiveCategories;
        
        public CategoryStatistics() {}
        
        public CategoryStatistics(long totalCategories, long activeCategories, long inactiveCategories) {
            this.totalCategories = totalCategories;
            this.activeCategories = activeCategories;
            this.inactiveCategories = inactiveCategories;
        }
        
        // Getters and Setters
        public long getTotalCategories() { return totalCategories; }
        public void setTotalCategories(long totalCategories) { this.totalCategories = totalCategories; }
        
        public long getActiveCategories() { return activeCategories; }
        public void setActiveCategories(long activeCategories) { this.activeCategories = activeCategories; }
        
        public long getInactiveCategories() { return inactiveCategories; }
        public void setInactiveCategories(long inactiveCategories) { this.inactiveCategories = inactiveCategories; }
    }
}
