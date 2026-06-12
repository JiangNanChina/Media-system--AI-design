package com.example.photography.repository;

import com.example.photography.model.entity.BorrowRecord;
import com.example.photography.model.enums.BorrowStatus;
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
 * 借还记录Repository
 */
@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    
    /**
     * 根据ID查找未删除的借还记录
     */
    Optional<BorrowRecord> findByIdAndDeletedFalse(Long id);
    
    /**
     * 根据用户ID查找借还记录
     */
    Page<BorrowRecord> findByUser_IdAndDeletedFalse(Long userId, Pageable pageable);
    
    /**
     * 根据用户ID统计借还记录数量
     */
    long countByUser_IdAndDeletedFalse(Long userId);
    
    /**
     * 根据用户ID和状态统计借还记录数量
     */
    long countByUser_IdAndStatusAndDeletedFalse(Long userId, BorrowStatus status);
    
    /**
     * 根据设备ID查找借还记录
     */
    Page<BorrowRecord> findByEquipment_IdAndDeletedFalse(Long equipmentId, Pageable pageable);
    
    @Query("SELECT b FROM BorrowRecord b LEFT JOIN FETCH b.user LEFT JOIN FETCH b.equipment WHERE b.equipment.id = :equipmentId AND b.deleted = false")
    Page<BorrowRecord> findByEquipment_IdAndDeletedFalseWithDetails(@Param("equipmentId") Long equipmentId, Pageable pageable);
    
    /**
     * 根据状态查找借还记录
     */
    Page<BorrowRecord> findByStatusAndDeletedFalse(BorrowStatus status, Pageable pageable);
    
    /**
     * 根据状态查找借还记录（预加载用户和设备信息）
     */
    @Query("SELECT br FROM BorrowRecord br " +
           "LEFT JOIN FETCH br.user u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH br.equipment e " +
           "LEFT JOIN FETCH br.approvedBy " +
           "WHERE br.status = :status AND br.deleted = false")
    Page<BorrowRecord> findByStatusAndDeletedFalseWithDetails(@Param("status") BorrowStatus status, Pageable pageable);
    
    /**
     * 查找所有未删除的借还记录
     */
    Page<BorrowRecord> findByDeletedFalse(Pageable pageable);
    
    /**
     * 查找所有未删除的借还记录（带排序）
     */
    List<BorrowRecord> findByDeletedFalse(Sort sort);
    
    /**
     * 查找所有未删除的借还记录（预加载用户和设备信息）
     */
    @Query("SELECT br FROM BorrowRecord br " +
           "LEFT JOIN FETCH br.user u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH br.equipment e " +
           "LEFT JOIN FETCH br.approvedBy " +
           "WHERE br.deleted = false")
    Page<BorrowRecord> findByDeletedFalseWithDetails(Pageable pageable);
    
    /**
     * 根据用户ID查找借还记录（预加载用户和设备信息）
     */
    @Query("SELECT br FROM BorrowRecord br " +
           "LEFT JOIN FETCH br.user u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH br.equipment e " +
           "LEFT JOIN FETCH br.approvedBy " +
           "WHERE br.user.id = :userId AND br.deleted = false")
    Page<BorrowRecord> findByUser_IdAndDeletedFalseWithDetails(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 根据ID查找单条借还记录（预加载用户和设备信息）
     */
    @Query("SELECT br FROM BorrowRecord br " +
           "LEFT JOIN FETCH br.user u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH br.equipment e " +
           "LEFT JOIN FETCH br.approvedBy " +
           "WHERE br.id = :id AND br.deleted = false")
    Optional<BorrowRecord> findByIdAndDeletedFalseWithDetails(@Param("id") Long id);
    
    /**
     * 根据用户ID和状态查找记录
     */
    List<BorrowRecord> findByUser_IdAndStatusAndDeletedFalse(Long userId, BorrowStatus status);
    
    /**
     * 查找待审核的记录
     */
    List<BorrowRecord> findByStatusAndDeletedFalseOrderByCreatedAtAsc(BorrowStatus status);
    
    /**
     * 查找逾期的记录
     */
    @Query("SELECT br FROM BorrowRecord br LEFT JOIN FETCH br.user u LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH br.equipment e LEFT JOIN FETCH e.category " +
           "WHERE br.deleted = false AND br.status = 'BORROWED' AND br.expectedReturnTime < :currentTime")
    List<BorrowRecord> findOverdueRecords(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 根据状态和时间范围查找记录（用于导出）
     */
    List<BorrowRecord> findByStatusAndCreatedAtBetweenAndDeletedFalseOrderByCreatedAtDesc(
            BorrowStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 根据状态查找记录（用于导出）
     */
    List<BorrowRecord> findByStatusAndDeletedFalseOrderByCreatedAtDesc(BorrowStatus status);
    
    /**
     * 根据时间范围查找记录（用于导出）
     */
    List<BorrowRecord> findByCreatedAtBetweenAndDeletedFalseOrderByCreatedAtDesc(
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 查找所有记录（用于导出）
     */
    List<BorrowRecord> findByDeletedFalseOrderByCreatedAtDesc();
    
    /**
     * 根据时间范围查找记录
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.deleted = false AND " +
           "br.createdAt BETWEEN :startTime AND :endTime")
    List<BorrowRecord> findByDateRange(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计借还记录数量
     */
    long countByDeletedFalse();
    
    /**
     * 根据状态统计记录数量
     */
    long countByStatusAndDeletedFalse(BorrowStatus status);
    
    /**
     * 查找已软删除的记录
     */
    Page<BorrowRecord> findByDeletedTrue(Pageable pageable);
    
    /**
     * 统计已软删除的记录数量
     */
    long countByDeletedTrue();
    
    /**
     * 查找指定天数前被软删除的记录
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.deleted = true AND " +
           "br.updatedAt < :cutoffDate")
    List<BorrowRecord> findDeletedRecordsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 统计指定天数前被软删除的记录数量
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.deleted = true AND " +
           "br.updatedAt < :cutoffDate")
    long countDeletedRecordsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 根据设备ID查找所有相关的借用记录（包括已删除的）
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.equipment.id = :equipmentId")
    List<BorrowRecord> findByEquipmentId(@Param("equipmentId") Long equipmentId);
    
    /**
     * 根据设备ID查找所有相关的借用记录，按创建时间倒序排列
     */
    @Query("SELECT br FROM BorrowRecord br LEFT JOIN FETCH br.user u LEFT JOIN FETCH u.department " +
           "WHERE br.equipment.id = :equipmentId ORDER BY br.createdAt DESC")
    List<BorrowRecord> findByEquipmentIdOrderByCreatedTimeDesc(@Param("equipmentId") Long equipmentId);
    
    /**
     * 统计某设备在指定状态下被借出的总数量（仅统计未删除记录）
     */
    @Query("SELECT COALESCE(SUM(br.quantity), 0) FROM BorrowRecord br " +
           "WHERE br.equipment.id = :equipmentId AND br.status = :status AND br.deleted = false")
    Integer sumQuantityByEquipmentIdAndStatus(@Param("equipmentId") Long equipmentId,
                                              @Param("status") BorrowStatus status);
    
    /**
     * 删除用户的所有借还记录（物理删除）
     */
    void deleteByUserId(Long userId);
    
    /**
     * 删除该用户审批的所有借还记录（物理删除）
     */
    void deleteByApprovedById(Long approverId);
    
}
