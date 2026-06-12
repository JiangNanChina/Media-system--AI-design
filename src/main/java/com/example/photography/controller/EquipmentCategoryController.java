package com.example.photography.controller;

import com.example.photography.dto.response.ApiResponse;
import com.example.photography.model.entity.EquipmentCategory;
import com.example.photography.service.EquipmentCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备分类管理控制器
 */
@RestController
@RequestMapping("/equipment-categories")
@Tag(name = "设备分类管理", description = "设备分类的增删改查和管理功能")
public class EquipmentCategoryController {
    
    @Autowired
    private EquipmentCategoryService categoryService;
    
    @GetMapping
    @Operation(summary = "获取设备分类列表", description = "分页获取设备分类列表")
    public ApiResponse<Page<EquipmentCategory>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            
            Page<EquipmentCategory> categories;
            if (keyword != null && !keyword.trim().isEmpty()) {
                categories = categoryService.searchCategories(keyword, pageable);
            } else {
                categories = categoryService.findAll(pageable);
            }
            
            return ApiResponse.success(categories);
        } catch (Exception e) {
            return ApiResponse.error("获取分类列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/list")
    @Operation(summary = "获取所有分类列表", description = "获取所有分类的简单列表（不分页）")
    public ApiResponse<List<EquipmentCategory>> getAllCategories() {
        try {
            List<EquipmentCategory> categories = categoryService.findAllList();
            return ApiResponse.success(categories);
        } catch (Exception e) {
            return ApiResponse.error("获取分类列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/active")
    @Operation(summary = "获取激活的分类列表", description = "获取所有激活状态的分类列表")
    public ApiResponse<List<EquipmentCategory>> getActiveCategories() {
        try {
            List<EquipmentCategory> categories = categoryService.findAllActive();
            return ApiResponse.success(categories);
        } catch (Exception e) {
            return ApiResponse.error("获取激活分类列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情", description = "根据ID获取设备分类详情")
    public ApiResponse<EquipmentCategory> getCategoryById(@PathVariable Long id) {
        try {
            EquipmentCategory category = categoryService.findById(id);
            return ApiResponse.success(category);
        } catch (Exception e) {
            return ApiResponse.error("获取分类详情失败: " + e.getMessage());
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建设备分类", description = "创建新的设备分类（仅管理员）")
    public ApiResponse<EquipmentCategory> createCategory(@Valid @RequestBody EquipmentCategory category) {
        try {
            EquipmentCategory createdCategory = categoryService.createCategory(category);
            return ApiResponse.success("分类创建成功", createdCategory);
        } catch (Exception e) {
            return ApiResponse.error("创建分类失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新设备分类", description = "更新设备分类信息（仅管理员）")
    public ApiResponse<EquipmentCategory> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody EquipmentCategory category) {
        try {
            EquipmentCategory updatedCategory = categoryService.updateCategory(id, category);
            return ApiResponse.success("分类更新成功", updatedCategory);
        } catch (Exception e) {
            return ApiResponse.error("更新分类失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除设备分类", description = "删除设备分类（仅管理员）")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ApiResponse.success("分类删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除分类失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "切换分类状态", description = "启用或禁用分类（仅管理员）")
    public ApiResponse<Void> toggleCategoryStatus(@PathVariable Long id) {
        try {
            categoryService.toggleCategoryStatus(id);
            return ApiResponse.success("分类状态切换成功");
        } catch (Exception e) {
            return ApiResponse.error("切换分类状态失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/sort")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新分类排序", description = "更新分类的排序号（仅管理员）")
    public ApiResponse<Void> updateCategorySortOrder(
            @PathVariable Long id, 
            @RequestBody Map<String, Integer> request) {
        try {
            Integer sortOrder = request.get("sortOrder");
            if (sortOrder == null) {
                return ApiResponse.error("排序号不能为空");
            }
            categoryService.updateSortOrder(id, sortOrder);
            return ApiResponse.success("排序更新成功");
        } catch (Exception e) {
            return ApiResponse.error("更新排序失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/batch-sort")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量更新排序", description = "批量更新分类排序（仅管理员）")
    public ApiResponse<Void> batchUpdateSortOrder(@RequestBody List<Long> categoryIds) {
        try {
            categoryService.batchUpdateSortOrder(categoryIds);
            return ApiResponse.success("批量排序更新成功");
        } catch (Exception e) {
            return ApiResponse.error("批量更新排序失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/check-name")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "检查分类名称", description = "检查分类名称是否已存在（仅管理员）")
    public ApiResponse<Boolean> checkCategoryName(
            @RequestParam String name,
            @RequestParam(required = false) Long excludeId) {
        try {
            boolean exists;
            if (excludeId != null) {
                exists = categoryService.existsByNameAndIdNot(name, excludeId);
            } else {
                exists = categoryService.existsByName(name);
            }
            return ApiResponse.success(!exists); // 返回是否可用（不存在则可用）
        } catch (Exception e) {
            return ApiResponse.error("检查分类名称失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取分类统计", description = "获取分类统计信息（仅管理员）")
    public ApiResponse<EquipmentCategoryService.CategoryStatistics> getCategoryStatistics() {
        try {
            EquipmentCategoryService.CategoryStatistics statistics = categoryService.getCategoryStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取分类统计失败: " + e.getMessage());
        }
    }
}
