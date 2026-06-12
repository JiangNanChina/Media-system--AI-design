package com.example.photography.repository;

import com.example.photography.model.entity.EquipmentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备分类Repository
 */
@Repository
public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, Long> {
    
    /**
     * 根据ID查找未删除的分类
     */
    Optional<EquipmentCategory> findByIdAndDeletedFalse(Long id);
    
    /**
     * 查找所有未删除的分类
     */
    List<EquipmentCategory> findByDeletedFalseOrderBySortOrderAscNameAsc();
    
    /**
     * 查找所有未删除且激活的分类
     */
    List<EquipmentCategory> findByDeletedFalseAndIsActiveTrueOrderBySortOrderAscNameAsc();
    
    /**
     * 分页查找未删除的分类
     */
    Page<EquipmentCategory> findByDeletedFalse(Pageable pageable);
    
    /**
     * 根据名称查找分类（排除已删除）
     */
    Optional<EquipmentCategory> findByNameAndDeletedFalse(String name);
    
    /**
     * 检查名称是否存在（排除已删除和指定ID）
     */
    @Query("SELECT COUNT(c) > 0 FROM EquipmentCategory c WHERE c.name = :name AND c.deleted = false AND c.id != :excludeId")
    boolean existsByNameAndDeletedFalseAndIdNot(@Param("name") String name, @Param("excludeId") Long excludeId);
    
    /**
     * 检查名称是否存在（排除已删除）
     */
    boolean existsByNameAndDeletedFalse(String name);
    
    /**
     * 根据名称模糊查询未删除的分类
     */
    @Query("SELECT c FROM EquipmentCategory c WHERE c.name LIKE %:keyword% AND c.deleted = false ORDER BY c.sortOrder ASC, c.name ASC")
    Page<EquipmentCategory> findByNameContainingAndDeletedFalse(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 获取最大排序号
     */
    @Query("SELECT COALESCE(MAX(c.sortOrder), 0) FROM EquipmentCategory c WHERE c.deleted = false")
    Integer getMaxSortOrder();
    
    /**
     * 统计未删除的分类数量
     */
    long countByDeletedFalse();
    
    /**
     * 统计未删除且激活的分类数量
     */
    long countByDeletedFalseAndIsActiveTrue();
}
