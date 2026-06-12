package com.example.photography.repository;

import com.example.photography.model.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 部门Repository
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    /**
     * 根据名称查找部门
     */
    Optional<Department> findByNameAndDeletedFalse(String name);
    
    /**
     * 查找所有未删除的部门
     */
    List<Department> findByDeletedFalse();
    
    /**
     * 分页查找所有未删除的部门
     */
    Page<Department> findByDeletedFalse(Pageable pageable);
    
    /**
     * 根据名称搜索部门（分页）
     */
    Page<Department> findByNameContainingIgnoreCaseAndDeletedFalse(String name, Pageable pageable);
    
    /**
     * 检查部门名称是否存在
     */
    boolean existsByNameAndDeletedFalse(String name);
    
    /**
     * 统计部门数量
     */
    long countByDeletedFalse();
}
