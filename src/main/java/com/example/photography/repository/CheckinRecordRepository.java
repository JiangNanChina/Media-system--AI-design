package com.example.photography.repository;

import com.example.photography.model.entity.CheckinRecord;
import com.example.photography.model.entity.CheckinConfiguration;
import com.example.photography.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 打卡记录Repository
 */
@Repository
public interface CheckinRecordRepository extends JpaRepository<CheckinRecord, Long> {
    
    /**
     * 查找用户在指定日期和配置的打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.user = :user AND r.configuration = :configuration " +
           "AND DATE(r.checkinTime) = :date AND r.deleted = false")
    Optional<CheckinRecord> findByUserAndConfigurationAndDate(@Param("user") User user, 
                                                             @Param("configuration") CheckinConfiguration configuration, 
                                                             @Param("date") LocalDate date);
    
    /**
     * 查找用户的打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.user = :user AND r.deleted = false ORDER BY r.checkinTime DESC")
    Page<CheckinRecord> findByUser(@Param("user") User user, Pageable pageable);
    
    /**
     * 查找用户指定日期范围的打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.user = :user " +
           "AND r.checkinTime >= :startDate AND r.checkinTime < :endDate " +
           "AND r.deleted = false ORDER BY r.checkinTime DESC")
    List<CheckinRecord> findByUserAndDateRange(@Param("user") User user, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查找指定日期的所有打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE DATE(r.checkinTime) = :date " +
           "AND r.deleted = false ORDER BY r.checkinTime DESC")
    List<CheckinRecord> findByDate(@Param("date") LocalDate date);
    
    /**
     * 查找指定配置的打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.configuration = :configuration AND r.deleted = false ORDER BY r.checkinTime DESC")
    Page<CheckinRecord> findByConfiguration(@Param("configuration") CheckinConfiguration configuration, Pageable pageable);
    
    /**
     * 根据配置ID查找打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.configuration.id = :configurationId AND r.deleted = false ORDER BY r.checkinTime DESC")
    Page<CheckinRecord> findByConfigurationId(@Param("configurationId") Long configurationId, Pageable pageable);
    
    /**
     * 统计用户的打卡次数
     */
    @Query("SELECT COUNT(r) FROM CheckinRecord r WHERE r.user = :user AND r.deleted = false")
    long countByUser(@Param("user") User user);
    
    /**
     * 统计用户在指定日期范围的打卡次数
     */
    @Query("SELECT COUNT(r) FROM CheckinRecord r WHERE r.user = :user " +
           "AND r.checkinTime >= :startDate AND r.checkinTime < :endDate AND r.deleted = false")
    long countByUserAndDateRange(@Param("user") User user, 
                                @Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * 统计指定日期的打卡人数
     */
    @Query("SELECT COUNT(DISTINCT r.user) FROM CheckinRecord r WHERE DATE(r.checkinTime) = :date AND r.deleted = false")
    long countDistinctUsersByDate(@Param("date") LocalDate date);
    
    /**
     * 查找迟到记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.isLate = true AND r.deleted = false ORDER BY r.checkinTime DESC")
    Page<CheckinRecord> findLateRecords(Pageable pageable);
    
    /**
     * 查找用户的迟到次数
     */
    @Query("SELECT COUNT(r) FROM CheckinRecord r WHERE r.user = :user AND r.isLate = true AND r.deleted = false")
    long countLateRecordsByUser(@Param("user") User user);
    
    /**
     * 查找未签退的记录（假设所有配置都需要签退）
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.checkoutTime IS NULL AND r.deleted = false ORDER BY r.checkinTime DESC")
    List<CheckinRecord> findUncheckedOutRecords();
    
    /**
     * 查找指定状态的记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.status = :status AND r.deleted = false ORDER BY r.checkinTime DESC")
    Page<CheckinRecord> findByStatus(@Param("status") CheckinRecord.CheckinStatus status, Pageable pageable);
    
    /**
     * 统计各状态的记录数量
     */
    @Query("SELECT r.status, COUNT(r) FROM CheckinRecord r WHERE r.deleted = false GROUP BY r.status")
    List<Object[]> countByStatus();
    
    /**
     * 查找最近的打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.user = :user AND r.deleted = false ORDER BY r.checkinTime DESC")
    List<CheckinRecord> findRecentRecordsByUser(@Param("user") User user, Pageable pageable);
    
    /**
     * 查找需要补签的记录（超过指定天数未签退）
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.checkoutTime IS NULL " +
           "AND r.checkinTime < :cutoffTime AND r.deleted = false")
    List<CheckinRecord> findRecordsNeedingMakeup(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * 根据用户ID和日期范围查找打卡记录（用于Excel导出）
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.user.id = :userId " +
           "AND DATE(r.checkinTime) >= :startDate AND DATE(r.checkinTime) <= :endDate " +
           "AND r.deleted = false ORDER BY r.checkinTime DESC")
    List<CheckinRecord> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
    
    /**
     * 根据日期范围查找所有打卡记录（用于Excel导出）
     */
    @Query("SELECT r FROM CheckinRecord r WHERE DATE(r.checkinTime) >= :startDate " +
           "AND DATE(r.checkinTime) <= :endDate AND r.deleted = false ORDER BY r.checkinTime DESC")
    List<CheckinRecord> findByDateRange(@Param("startDate") LocalDate startDate, 
                                      @Param("endDate") LocalDate endDate);
    
    /**
     * 根据用户ID和日期查找打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.user.id = :userId " +
           "AND DATE(r.checkinTime) = :date AND r.deleted = false")
    Optional<CheckinRecord> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * 根据用户ID查找打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.user.id = :userId AND r.deleted = false ORDER BY r.checkinTime DESC")
    Page<CheckinRecord> findByUserIdAndDeletedFalse(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 根据用户ID查找打卡记录（带关联查询）
     */
    @Query("SELECT r FROM CheckinRecord r " +
           "LEFT JOIN FETCH r.user u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH r.configuration " +
           "WHERE r.user.id = :userId AND r.deleted = false " +
           "ORDER BY r.checkinTime DESC")
    List<CheckinRecord> findByUserIdAndDeletedFalseWithFetch(@Param("userId") Long userId);
    
    /**
     * 查找所有未删除的打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.deleted = false ORDER BY r.checkinTime DESC")
    Page<CheckinRecord> findByDeletedFalse(Pageable pageable);
    
    /**
     * 管理员查询所有记录（带关联查询）
     */
    @Query("SELECT r FROM CheckinRecord r " +
           "LEFT JOIN FETCH r.user u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH r.configuration " +
           "WHERE r.deleted = false " +
           "ORDER BY r.checkinTime DESC")
    List<CheckinRecord> findAllWithFetch();
    
    /**
     * 根据时间范围查找未删除的打卡记录
     */
    List<CheckinRecord> findByCheckinTimeBetweenAndDeletedFalse(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据时间范围物理删除打卡记录
     */
    @Modifying
    @Query("DELETE FROM CheckinRecord r WHERE r.checkinTime BETWEEN :startTime AND :endTime AND r.deleted = false")
    int deleteByCheckinTimeBetweenAndDeletedFalse(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找最近的打卡记录（带关联查询）
     */
    @Query("SELECT r FROM CheckinRecord r " +
           "LEFT JOIN FETCH r.user u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH r.configuration " +
           "WHERE r.deleted = false " +
           "ORDER BY r.checkinTime DESC")
    List<CheckinRecord> findRecentRecordsWithFetch(Pageable pageable);
    
    /**
     * 统计指定配置的打卡记录数量
     */
    @Query("SELECT COUNT(r) FROM CheckinRecord r WHERE r.configuration.id = :configurationId")
    long countByConfigurationId(@Param("configurationId") Long configurationId);
    
    /**
     * 根据配置ID查找打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.configuration.id = :configurationId")
    List<CheckinRecord> findByConfigurationId(@Param("configurationId") Long configurationId);
    
    /**
     * 统计未删除的打卡记录数量
     */
    @Query("SELECT COUNT(r) FROM CheckinRecord r WHERE r.deleted = false")
    long countByDeletedFalse();
    
    /**
     * 根据配置ID删除所有关联的打卡记录
     */
    @Modifying
    @Query("DELETE FROM CheckinRecord r WHERE r.configuration.id = :configurationId")
    void deleteByConfigurationId(@Param("configurationId") Long configurationId);
    
    /**
     * 根据配置ID和时间范围查找打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.configuration " +
           "WHERE r.configuration.id = :configId AND r.checkinTime BETWEEN :startTime AND :endTime " +
           "AND r.deleted = false ORDER BY r.checkinTime ASC")
    List<CheckinRecord> findByConfigurationIdAndCheckinTimeBetween(
        @Param("configId") Long configId, 
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据用户ID和签到日期查找打卡记录
     */
    @Query("SELECT r FROM CheckinRecord r WHERE r.user.id = :userId " +
           "AND DATE(r.checkinTime) = :checkinDate AND r.deleted = false")
    List<CheckinRecord> findByUser_IdAndCheckinDate(@Param("userId") Long userId, @Param("checkinDate") LocalDate checkinDate);
    
    /**
     * 根据用户ID和签到日期查找打卡记录（带JOIN FETCH避免懒加载问题）
     */
    @Query("SELECT r FROM CheckinRecord r " +
           "LEFT JOIN FETCH r.user u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH r.configuration c " +
           "WHERE r.user.id = :userId AND DATE(r.checkinTime) = :checkinDate AND r.deleted = false")
    List<CheckinRecord> findByUserIdAndCheckinDateWithFetch(@Param("userId") Long userId, @Param("checkinDate") LocalDate checkinDate);
    
    /**
     * 删除用户的所有打卡记录（物理删除）
     */
    void deleteByUserId(Long userId);
    
    /**
     * 根据审核状态查找打卡记录（使用JOIN FETCH避免懒加载问题）
     */
    @Query("SELECT r FROM CheckinRecord r " +
           "LEFT JOIN FETCH r.user u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH r.configuration c " +
           "WHERE r.auditStatus = :auditStatus AND r.deleted = false " +
           "ORDER BY r.checkinTime DESC")
    Page<CheckinRecord> findByAuditStatus(@Param("auditStatus") CheckinRecord.AuditStatus auditStatus, Pageable pageable);
    
    /**
     * 统计待审核记录数量
     */
    @Query("SELECT COUNT(r) FROM CheckinRecord r WHERE r.auditStatus = 'PENDING' AND r.deleted = false")
    long countPendingAuditRecords();
}
