package com.example.photography.service.impl;

import com.example.photography.model.entity.EquipmentCategory;
import com.example.photography.repository.EquipmentCategoryRepository;
import com.example.photography.service.EquipmentCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 设备分类服务实现类
 */
@Service
@Transactional
public class EquipmentCategoryServiceImpl implements EquipmentCategoryService {
    
    @Autowired
    private EquipmentCategoryRepository categoryRepository;
    
    @Override
    public java.util.Optional<EquipmentCategory> findByName(String name) {
        return categoryRepository.findByNameAndDeletedFalse(name);
    }
    
    @Override
    public EquipmentCategory save(EquipmentCategory category) {
        return categoryRepository.save(category);
    }
    
    @Override
    public EquipmentCategory createCategory(EquipmentCategory category) {
        // 检查名称是否已存在
        if (categoryRepository.existsByNameAndDeletedFalse(category.getName())) {
            throw new RuntimeException("分类名称已存在");
        }
        
        // 如果没有设置排序号，自动设置为最大值+1
        if (category.getSortOrder() == null) {
            Integer maxSortOrder = categoryRepository.getMaxSortOrder();
            category.setSortOrder(maxSortOrder + 1);
        }
        
        return categoryRepository.save(category);
    }
    
    @Override
    public EquipmentCategory updateCategory(Long id, EquipmentCategory category) {
        EquipmentCategory existingCategory = findById(id);
        
        // 检查名称是否已存在（排除当前分类）
        if (!existingCategory.getName().equals(category.getName()) && 
            categoryRepository.existsByNameAndDeletedFalseAndIdNot(category.getName(), id)) {
            throw new RuntimeException("分类名称已存在");
        }
        
        // 更新字段
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        if (category.getSortOrder() != null) {
            existingCategory.setSortOrder(category.getSortOrder());
        }
        if (category.getIsActive() != null) {
            existingCategory.setIsActive(category.getIsActive());
        }
        
        return categoryRepository.save(existingCategory);
    }
    
    @Override
    public void deleteCategory(Long id) {
        EquipmentCategory category = findById(id);
        
        // TODO: 检查是否有设备使用此分类，如果有则不允许删除
        // 可以在后续添加设备关联检查
        
        category.setDeleted(true);
        categoryRepository.save(category);
    }
    
    @Override
    @Transactional(readOnly = true)
    public EquipmentCategory findById(Long id) {
        return categoryRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("分类不存在"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<EquipmentCategory> findAll(Pageable pageable) {
        return categoryRepository.findByDeletedFalse(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EquipmentCategory> findAllActive() {
        return categoryRepository.findByDeletedFalseAndIsActiveTrueOrderBySortOrderAscNameAsc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EquipmentCategory> findAllList() {
        return categoryRepository.findByDeletedFalseOrderBySortOrderAscNameAsc();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<EquipmentCategory> searchCategories(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll(pageable);
        }
        return categoryRepository.findByNameContainingAndDeletedFalse(keyword.trim(), pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return categoryRepository.existsByNameAndDeletedFalse(name);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        return categoryRepository.existsByNameAndDeletedFalseAndIdNot(name, excludeId);
    }
    
    @Override
    public void toggleCategoryStatus(Long id) {
        EquipmentCategory category = findById(id);
        category.setIsActive(!category.getIsActive());
        categoryRepository.save(category);
    }
    
    @Override
    public void updateSortOrder(Long id, Integer sortOrder) {
        EquipmentCategory category = findById(id);
        category.setSortOrder(sortOrder);
        categoryRepository.save(category);
    }
    
    @Override
    public void batchUpdateSortOrder(List<Long> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            Long categoryId = categoryIds.get(i);
            EquipmentCategory category = findById(categoryId);
            category.setSortOrder(i + 1);
            categoryRepository.save(category);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public CategoryStatistics getCategoryStatistics() {
        long totalCategories = categoryRepository.countByDeletedFalse();
        long activeCategories = categoryRepository.countByDeletedFalseAndIsActiveTrue();
        long inactiveCategories = totalCategories - activeCategories;
        
        return new CategoryStatistics(totalCategories, activeCategories, inactiveCategories);
    }
}
