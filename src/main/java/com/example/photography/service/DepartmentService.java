package com.example.photography.service;

import com.example.photography.dto.request.DepartmentCreateRequest;
import com.example.photography.dto.response.DepartmentResponse;
import com.example.photography.model.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DepartmentService {
    
    /**
     * 创建部门
     */
    Department createDepartment(DepartmentCreateRequest request);
    
    /**
     * 创建部门（返回DTO）
     */
    DepartmentResponse createDepartmentResponse(DepartmentCreateRequest request);
    
    /**
     * 更新部门信息
     */
    Department updateDepartment(Long id, DepartmentCreateRequest request);
    
    /**
     * 删除部门
     */
    void deleteDepartment(Long id);
    
    /**
     * 根据ID查找部门
     */
    Department findById(Long id);
    
    /**
     * 查找所有部门
     */
    List<Department> findAllDepartments();
    
    /**
     * 查找所有部门（返回DTO）
     */
    List<DepartmentResponse> findAllDepartmentsResponse();
    
    /**
     * 分页查找所有部门
     */
    Page<Department> findAllDepartments(Pageable pageable);
    
    /**
     * 根据关键字搜索部门
     */
    Page<Department> searchDepartments(String keyword, Pageable pageable);
    
    /**
     * 分页查找所有部门（包含用户数量）
     */
    Page<DepartmentResponse> findAllDepartmentsWithUserCount(Pageable pageable);
    
    /**
     * 根据关键字搜索部门（包含用户数量）
     */
    Page<DepartmentResponse> searchDepartmentsWithUserCount(String keyword, Pageable pageable);
    
    /**
     * 检查部门名称是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 初始化默认部门
     */
    void initializeDefaultDepartments();
    
    /**
     * 获取部门统计信息
     */
    DepartmentStatistics getDepartmentStatistics();
    
    /**
     * 部门统计信息类
     */
    class DepartmentStatistics {
        private long totalDepartments;
        private long photographyDeptCount;
        private long editingDeptCount;
        private long reviewDeptCount;
        private long publicityDeptCount;
        private long customDeptCount;
        
        // Constructors, getters and setters
        public DepartmentStatistics() {}
        
        public DepartmentStatistics(long totalDepartments, long photographyDeptCount, 
                                  long editingDeptCount, long reviewDeptCount, 
                                  long publicityDeptCount, long customDeptCount) {
            this.totalDepartments = totalDepartments;
            this.photographyDeptCount = photographyDeptCount;
            this.editingDeptCount = editingDeptCount;
            this.reviewDeptCount = reviewDeptCount;
            this.publicityDeptCount = publicityDeptCount;
            this.customDeptCount = customDeptCount;
        }
        
        public long getTotalDepartments() { return totalDepartments; }
        public void setTotalDepartments(long totalDepartments) { this.totalDepartments = totalDepartments; }
        
        public long getPhotographyDeptCount() { return photographyDeptCount; }
        public void setPhotographyDeptCount(long photographyDeptCount) { this.photographyDeptCount = photographyDeptCount; }
        
        public long getEditingDeptCount() { return editingDeptCount; }
        public void setEditingDeptCount(long editingDeptCount) { this.editingDeptCount = editingDeptCount; }
        
        public long getReviewDeptCount() { return reviewDeptCount; }
        public void setReviewDeptCount(long reviewDeptCount) { this.reviewDeptCount = reviewDeptCount; }
        
        public long getPublicityDeptCount() { return publicityDeptCount; }
        public void setPublicityDeptCount(long publicityDeptCount) { this.publicityDeptCount = publicityDeptCount; }
        
        public long getCustomDeptCount() { return customDeptCount; }
        public void setCustomDeptCount(long customDeptCount) { this.customDeptCount = customDeptCount; }
    }
}
