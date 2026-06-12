package com.example.photography.repository;

import com.example.photography.model.entity.CheckinConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 打卡配置数据访问层
 */
@Repository
public interface CheckinConfigurationRepository extends JpaRepository<CheckinConfiguration, Long> {
    
    /**
     * 查找所有启用且未删除的配置（包含创建者信息）
     */
    @Query("SELECT c FROM CheckinConfiguration c LEFT JOIN FETCH c.createdBy u LEFT JOIN FETCH u.department WHERE c.isActive = true AND c.deleted = false ORDER BY c.sortOrder ASC")
    List<CheckinConfiguration> findByIsActiveTrueAndDeletedFalseOrderBySortOrderAsc();
    
    /**
     * 查找所有未删除的配置
     */
    List<CheckinConfiguration> findByDeletedFalseOrderBySortOrderAsc();
    
    /**
     * 分页查找所有未删除的配置（包含创建者信息）
     */
    @Query("SELECT c FROM CheckinConfiguration c LEFT JOIN FETCH c.createdBy u LEFT JOIN FETCH u.department WHERE c.deleted = false")
    Page<CheckinConfiguration> findByDeletedFalse(Pageable pageable);
    
    /**
     * 根据名称搜索配置（包含创建者信息）
     */
    @Query("SELECT c FROM CheckinConfiguration c LEFT JOIN FETCH c.createdBy u LEFT JOIN FETCH u.department WHERE c.deleted = false AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<CheckinConfiguration> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据地点名称查找配置（包含创建者信息）
     */
    @Query("SELECT c FROM CheckinConfiguration c LEFT JOIN FETCH c.createdBy u LEFT JOIN FETCH u.department WHERE c.locationName = :locationName AND c.deleted = false")
    List<CheckinConfiguration> findByLocationName(@Param("locationName") String locationName);
    
    /**
     * 根据时段名称查找配置（包含创建者信息）
     */
    @Query("SELECT c FROM CheckinConfiguration c LEFT JOIN FETCH c.createdBy u LEFT JOIN FETCH u.department WHERE c.sessionName = :sessionName AND c.deleted = false")
    List<CheckinConfiguration> findBySessionName(@Param("sessionName") String sessionName);
    
    /**
     * 查找指定地点名称和时段名称的配置
     */
    @Query("SELECT c FROM CheckinConfiguration c WHERE c.locationName = :locationName AND " +
           "c.sessionName = :sessionName AND c.deleted = false")
    Optional<CheckinConfiguration> findByLocationNameAndSessionName(@Param("locationName") String locationName, 
                                                                   @Param("sessionName") String sessionName);
    
    /**
     * 检查名称是否已存在（排除指定ID）
     */
    @Query("SELECT COUNT(c) > 0 FROM CheckinConfiguration c WHERE c.name = :name AND " +
           "c.deleted = false AND (:excludeId IS NULL OR c.id != :excludeId)")
    boolean existsByNameAndNotDeleted(@Param("name") String name, @Param("excludeId") Long excludeId);
    
    /**
     * 统计启用的配置数量
     */
    long countByIsActiveTrueAndDeletedFalse();
    
    /**
     * 统计总配置数量
     */
    long countByDeletedFalse();
    
    /**
     * 根据ID查找配置（包含创建者信息）
     */
    @Query("SELECT c FROM CheckinConfiguration c LEFT JOIN FETCH c.createdBy u LEFT JOIN FETCH u.department WHERE c.id = :id")
    Optional<CheckinConfiguration> findByIdWithCreatedBy(@Param("id") Long id);
    
    /**
     * 根据ID查找配置（包含用户列表）
     */
    @Query("SELECT c FROM CheckinConfiguration c LEFT JOIN FETCH c.requiredUsers WHERE c.id = :id")
    Optional<CheckinConfiguration> findByIdWithUsers(@Param("id") Long id);
    
    /**
     * 查找所有启用的配置（包含用户列表）
     * 注意：由于 JPA 限制，不能在一个查询中对多个集合使用 FETCH JOIN
     * 这里先加载配置和考勤人员列表，createdBy 需要在服务层处理
     */
    @Query("SELECT DISTINCT c FROM CheckinConfiguration c " +
           "LEFT JOIN FETCH c.requiredUsers u " +
           "LEFT JOIN FETCH u.department " +
           "WHERE c.isActive = true AND c.deleted = false " +
           "ORDER BY c.sortOrder ASC")
    List<CheckinConfiguration> findAllActiveWithUsers();
}
