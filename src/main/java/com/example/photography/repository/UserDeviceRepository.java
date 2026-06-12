package com.example.photography.repository;

import com.example.photography.model.entity.User;
import com.example.photography.model.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 用户设备Repository
 */
@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    
    /**
     * 查找用户的激活设备
     */
    @Query("SELECT ud FROM UserDevice ud JOIN FETCH ud.user WHERE ud.user = :user AND ud.isActive = true AND ud.deleted = false")
    Optional<UserDevice> findActiveDeviceByUser(@Param("user") User user);
    
    /**
     * 根据设备指纹查找设备
     */
    @Query("SELECT ud FROM UserDevice ud WHERE ud.deviceFingerprint = :fingerprint AND ud.deleted = false")
    Optional<UserDevice> findByDeviceFingerprint(@Param("fingerprint") String fingerprint);
    
    /**
     * 查找用户的所有设备
     */
    @Query("SELECT ud FROM UserDevice ud JOIN FETCH ud.user WHERE ud.user = :user AND ud.deleted = false ORDER BY ud.lastActiveAt DESC")
    List<UserDevice> findAllByUser(@Param("user") User user);
    
    /**
     * 查找用户的所有设备（包括已删除的）
     */
    @Query("SELECT ud FROM UserDevice ud JOIN FETCH ud.user WHERE ud.user = :user ORDER BY ud.lastActiveAt DESC")
    List<UserDevice> findAllByUserIncludingDeleted(@Param("user") User user);
    
    /**
     * 根据用户ID查找所有激活设备
     */
    @Query("SELECT ud FROM UserDevice ud JOIN FETCH ud.user WHERE ud.user.id = :userId AND ud.isActive = true AND ud.deleted = false")
    List<UserDevice> findAllByUser_IdAndIsActiveTrueAndDeletedFalse(@Param("userId") Long userId);
    
    /**
     * 查找未激活或状态异常的设备
     */
    @Query("SELECT ud FROM UserDevice ud JOIN FETCH ud.user WHERE (ud.isActive = false OR ud.bindStatus != :activeStatus) AND ud.deleted = false")
    List<UserDevice> findByIsActiveFalseOrBindStatusNot(@Param("activeStatus") UserDevice.BindStatus activeStatus);

    /**
     * 查找用户在指定类型集合中的激活设备
     */
    @Query("SELECT ud FROM UserDevice ud JOIN FETCH ud.user WHERE ud.user = :user AND ud.isActive = true AND ud.deleted = false AND ud.deviceType IN (:types)")
    List<UserDevice> findActiveDevicesByUserAndTypes(@Param("user") User user, @Param("types") Collection<UserDevice.DeviceType> types);

    /**
     * 根据用户与指纹查找激活设备
     */
    @Query("SELECT ud FROM UserDevice ud JOIN FETCH ud.user WHERE ud.user = :user AND ud.isActive = true AND ud.deleted = false AND ud.deviceFingerprint = :fingerprint")
    Optional<UserDevice> findActiveByUserAndDeviceFingerprint(@Param("user") User user, @Param("fingerprint") String fingerprint);
    
    /**
     * 检查用户是否已绑定设备
     */
    @Query("SELECT COUNT(ud) > 0 FROM UserDevice ud WHERE ud.user = :user AND ud.isActive = true AND ud.deleted = false")
    boolean hasActiveDevice(@Param("user") User user);
    
    /**
     * 停用用户的所有设备
     */
    @Modifying
    @Query("UPDATE UserDevice ud SET ud.isActive = false, ud.updatedAt = :now WHERE ud.user = :user AND ud.deleted = false")
    void deactivateAllDevicesByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    /**
     * 更新设备最后活跃时间
     */
    @Modifying
    @Query("UPDATE UserDevice ud SET ud.lastActiveAt = :activeAt, ud.updatedAt = :now WHERE ud.id = :deviceId")
    void updateLastActiveTime(@Param("deviceId") Long deviceId, @Param("activeAt") LocalDateTime activeAt, @Param("now") LocalDateTime now);
    
    /**
     * 查找长时间未活跃的设备
     */
    @Query("SELECT ud FROM UserDevice ud WHERE ud.lastActiveAt < :threshold AND ud.isActive = true AND ud.deleted = false")
    List<UserDevice> findInactiveDevices(@Param("threshold") LocalDateTime threshold);
    
    /**
     * 统计用户设备数量
     */
    @Query("SELECT COUNT(ud) FROM UserDevice ud WHERE ud.user = :user AND ud.deleted = false")
    long countByUser(@Param("user") User user);
    
    /**
     * 查找所有激活的设备
     */
    @Query("SELECT ud FROM UserDevice ud JOIN FETCH ud.user WHERE ud.isActive = true AND ud.deleted = false ORDER BY ud.lastActiveAt DESC")
    List<UserDevice> findAllActiveDevices();
    
    /**
     * 查找所有设备（包括停用的设备）- 用于管理员查看
     */
    @Query("SELECT ud FROM UserDevice ud JOIN FETCH ud.user WHERE ud.deleted = false ORDER BY ud.lastActiveAt DESC")
    List<UserDevice> findAllDevicesForAdmin();
    
    /**
     * 删除用户的所有设备绑定（物理删除）
     */
    void deleteByUserId(Long userId);
    
    /**
     * 删除用户指定类型的设备（物理删除）
     */
    @Modifying
    @Query("DELETE FROM UserDevice ud WHERE ud.user = :user AND ud.deviceType = :deviceType")
    void deleteByUserAndDeviceType(@Param("user") User user, @Param("deviceType") UserDevice.DeviceType deviceType);
    
    /**
     * 删除用户所有可能冲突的激活设备（物理删除）
     */
    @Modifying
    @Query("DELETE FROM UserDevice ud WHERE ud.user = :user AND ud.isActive = true AND ud.bindStatus = 'ACTIVE'")
    void deleteActiveDevicesByUser(@Param("user") User user);
    
    /**
     * 查找用户指定类型的已停用设备
     */
    @Query("SELECT ud FROM UserDevice ud WHERE ud.user = :user AND ud.deviceType = :deviceType AND ud.isActive = false AND ud.deleted = false ORDER BY ud.lastActiveAt DESC")
    List<UserDevice> findSuspendedDevicesByUserAndType(@Param("user") User user, @Param("deviceType") UserDevice.DeviceType deviceType);
}
