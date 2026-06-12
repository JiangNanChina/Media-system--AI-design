package com.example.photography.repository;

import com.example.photography.model.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 设备Repository
 */
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    
    /**
     * 根据序列号查找设备
     */
    Optional<Equipment> findBySerialNumberAndDeletedFalse(String serialNumber);
    
    /**
     * 检查序列号是否存在
     */
    boolean existsBySerialNumberAndDeletedFalse(String serialNumber);
    
    /**
     * 根据分类查找设备
     */
    @Query("SELECT e FROM Equipment e LEFT JOIN e.category c WHERE e.deleted = false AND " +
           "(e.categoryName = :category OR (c IS NOT NULL AND c.name = :category))")
    Page<Equipment> findByCategoryAndDeletedFalse(@Param("category") String category, Pageable pageable);
    
    /**
     * 根据状态查找设备
     */
    List<Equipment> findByStatusAndDeletedFalse(String status);
    
    /**
     * 查找所有未删除的设备
     */
    @Query("SELECT e FROM Equipment e WHERE e.deleted = false")
    Page<Equipment> findByDeletedFalse(Pageable pageable);
    
    /**
     * 查找所有未删除的设备（带排序）
     */
    @Query("SELECT e FROM Equipment e WHERE e.deleted = false")
    List<Equipment> findByDeletedFalse(Sort sort);
    
    /**
     * 查找所有未删除的设备（不分页）
     */
    @Query("SELECT e FROM Equipment e WHERE e.deleted = false")
    List<Equipment> findByDeletedFalse();
    
    /**
     * 根据关键字搜索设备
     */
    @Query("SELECT e FROM Equipment e LEFT JOIN e.category c WHERE e.deleted = false AND " +
           "(e.name LIKE %:keyword% OR " +
           "e.categoryName LIKE %:keyword% OR " +
           "c.name LIKE %:keyword% OR " +
           "e.serialNumber LIKE %:keyword% OR " +
           "e.description LIKE %:keyword%)")
    Page<Equipment> searchEquipment(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据分类和关键字搜索设备
     */
    @Query("SELECT e FROM Equipment e LEFT JOIN e.category c WHERE e.deleted = false AND " +
           "(e.categoryName = :category OR (c IS NOT NULL AND c.name = :category)) AND " +
           "(e.name LIKE %:keyword% OR " +
           "e.serialNumber LIKE %:keyword% OR " +
           "e.description LIKE %:keyword%)")
    Page<Equipment> searchEquipmentByCategory(@Param("category") String category, 
                                            @Param("keyword") String keyword, 
                                            Pageable pageable);
    
    /**
     * 查找所有分类
     */
    @Query("SELECT DISTINCT COALESCE(c.name, e.categoryName) FROM Equipment e LEFT JOIN e.category c WHERE e.deleted = false ORDER BY COALESCE(c.name, e.categoryName)")
    List<String> findAllCategories();
    
    /**
     * 查找可用库存大于0的设备
     */
    List<Equipment> findByAvailableQuantityGreaterThanAndDeletedFalse(Integer quantity);
    
    /**
     * 统计设备数量
     */
    long countByDeletedFalse();
    
    /**
     * 根据分类统计设备数量
     */
    @Query("SELECT COUNT(e) FROM Equipment e LEFT JOIN e.category c " +
           "WHERE e.deleted = false AND " +
           "(e.categoryName = :category OR (c IS NOT NULL AND c.name = :category))")
    long countByCategoryAndDeletedFalse(@Param("category") String category);
    
    /**
     * 查找已软删除的设备
     */
    Page<Equipment> findByDeletedTrue(Pageable pageable);
    
    /**
     * 统计已软删除的设备数量
     */
    long countByDeletedTrue();
    
    /**
     * 查找指定天数前被软删除的设备
     */
    @Query("SELECT e FROM Equipment e WHERE e.deleted = true AND " +
           "e.updatedAt < :cutoffDate")
    List<Equipment> findDeletedEquipmentsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 统计指定天数前被软删除的设备数量
     */
    @Query("SELECT COUNT(e) FROM Equipment e WHERE e.deleted = true AND " +
           "e.updatedAt < :cutoffDate")
    long countDeletedEquipmentsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 查找所有有图片的设备（用于清理孤立文件）
     */
    @Query("SELECT e FROM Equipment e WHERE e.deleted = false AND " +
           "e.imageUrl IS NOT NULL AND e.imageUrl != ''")
    List<Equipment> findByImageUrlIsNotNull();
}
