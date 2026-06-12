package com.example.photography.repository;

import com.example.photography.model.entity.DeviceAuditLog;
import com.example.photography.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备审计日志Repository
 */
@Repository
public interface DeviceAuditLogRepository extends JpaRepository<DeviceAuditLog, Long> {
    
    /**
     * 查找用户的审计日志
     */
    @Query("SELECT dal FROM DeviceAuditLog dal WHERE dal.user = :user ORDER BY dal.actionTime DESC")
    Page<DeviceAuditLog> findByUser(@Param("user") User user, Pageable pageable);
    
    /**
     * 查找指定时间范围内的审计日志
     */
    @Query("SELECT dal FROM DeviceAuditLog dal WHERE dal.actionTime BETWEEN :startTime AND :endTime ORDER BY dal.actionTime DESC")
    Page<DeviceAuditLog> findByActionTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime, 
                                                Pageable pageable);
    
    /**
     * 查找可疑活动日志
     */
    @Query("SELECT dal FROM DeviceAuditLog dal WHERE dal.actionType = 'SUSPICIOUS_ACTIVITY' ORDER BY dal.actionTime DESC")
    Page<DeviceAuditLog> findSuspiciousActivities(Pageable pageable);
    
    /**
     * 统计用户在指定时间内的设备变更次数
     */
    @Query("SELECT COUNT(dal) FROM DeviceAuditLog dal WHERE dal.user = :user AND dal.actionTime >= :since AND dal.actionType IN ('DEVICE_CREATED', 'DEVICE_REACTIVATED')")
    long countDeviceChangesByUserSince(@Param("user") User user, @Param("since") LocalDateTime since);
    
    /**
     * 查找设备的操作历史
     */
    @Query("SELECT dal FROM DeviceAuditLog dal WHERE dal.deviceFingerprint = :fingerprint ORDER BY dal.actionTime DESC")
    List<DeviceAuditLog> findByDeviceFingerprint(@Param("fingerprint") String fingerprint);
    
    /**
     * 删除指定设备的所有审计日志
     */
    @Modifying
    @Query("DELETE FROM DeviceAuditLog dal WHERE dal.deviceId = :deviceId")
    void deleteByDeviceId(@Param("deviceId") Long deviceId);
    
    /**
     * 删除指定用户的所有审计日志
     */
    @Modifying
    @Query("DELETE FROM DeviceAuditLog dal WHERE dal.user = :user")
    void deleteByUser(@Param("user") User user);
}
