package com.example.photography.repository;

import com.example.photography.model.entity.DutySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * 执勤排班Repository
 */
@Repository
public interface DutyScheduleRepository extends JpaRepository<DutySchedule, Long> {
    
    /**
     * 根据用户ID查找排班（主动加载用户和部门信息）
     */
    @Query("SELECT ds FROM DutySchedule ds LEFT JOIN FETCH ds.user u LEFT JOIN FETCH u.department " +
           "WHERE ds.deleted = false AND ds.active = true AND u.id = :userId " +
           "ORDER BY ds.dayOfWeek ASC, ds.startTime ASC")
    List<DutySchedule> findByUser_IdAndActiveTrueAndDeletedFalse(@Param("userId") Long userId);
    
    /**
     * 根据星期几查找排班（主动加载用户和部门信息）
     */
    @Query("SELECT ds FROM DutySchedule ds LEFT JOIN FETCH ds.user u LEFT JOIN FETCH u.department " +
           "WHERE ds.deleted = false AND ds.active = true AND ds.dayOfWeek = :dayOfWeek " +
           "ORDER BY ds.startTime ASC")
    List<DutySchedule> findByDayOfWeekAndActiveTrueAndDeletedFalse(@Param("dayOfWeek") Integer dayOfWeek);
    
    /**
     * 根据用户ID和星期几查找排班（主动加载用户和部门信息）
     */
    @Query("SELECT ds FROM DutySchedule ds LEFT JOIN FETCH ds.user u LEFT JOIN FETCH u.department " +
           "WHERE ds.deleted = false AND ds.active = true AND u.id = :userId AND ds.dayOfWeek = :dayOfWeek " +
           "ORDER BY ds.startTime ASC")
    List<DutySchedule> findByUser_IdAndDayOfWeekAndActiveTrueAndDeletedFalse(@Param("userId") Long userId, @Param("dayOfWeek") Integer dayOfWeek);
    
    /**
     * 查找用户在某天某个时间段的排班
     */
    @Query("SELECT ds FROM DutySchedule ds LEFT JOIN FETCH ds.user u LEFT JOIN FETCH u.department WHERE ds.deleted = false AND ds.active = true AND " +
           "u.id = :userId AND ds.dayOfWeek = :dayOfWeek AND ds.startTime <= :currentTime AND ds.endTime >= :currentTime")
    Optional<DutySchedule> findCurrentDutySchedule(@Param("userId") Long userId, 
                                                  @Param("dayOfWeek") Integer dayOfWeek,
                                                  @Param("currentTime") LocalTime currentTime);
    
    /**
     * 查找所有启用的排班（主动加载用户信息）
     */
    @Query("SELECT ds FROM DutySchedule ds LEFT JOIN FETCH ds.user u LEFT JOIN FETCH u.department " +
           "WHERE ds.deleted = false AND ds.active = true " +
           "ORDER BY ds.dayOfWeek ASC, ds.startTime ASC")
    List<DutySchedule> findByActiveTrueAndDeletedFalseOrderByDayOfWeekAscStartTimeAsc();
    
    /**
     * 查找所有排班（主动加载用户信息）
     */
    @Query("SELECT ds FROM DutySchedule ds LEFT JOIN FETCH ds.user u LEFT JOIN FETCH u.department " +
           "WHERE ds.deleted = false " +
           "ORDER BY ds.dayOfWeek ASC, ds.startTime ASC")
    List<DutySchedule> findByDeletedFalseOrderByDayOfWeekAscStartTimeAsc();
    
    /**
     * 检查时间冲突
     */
    @Query("SELECT ds FROM DutySchedule ds WHERE ds.deleted = false AND ds.active = true AND " +
           "ds.dayOfWeek = :dayOfWeek AND ds.id != :excludeId AND " +
           "((ds.startTime <= :startTime AND ds.endTime > :startTime) OR " +
           "(ds.startTime < :endTime AND ds.endTime >= :endTime) OR " +
           "(ds.startTime >= :startTime AND ds.endTime <= :endTime))")
    List<DutySchedule> findConflictingSchedules(@Param("dayOfWeek") Integer dayOfWeek,
                                               @Param("startTime") LocalTime startTime,
                                               @Param("endTime") LocalTime endTime,
                                               @Param("excludeId") Long excludeId);
    
    /**
     * 统计用户排班数量
     */
    long countByUser_IdAndActiveTrueAndDeletedFalse(Long userId);
    
    /**
     * 根据ID查找排班（急切加载用户和部门信息）
     */
    @Query("SELECT ds FROM DutySchedule ds LEFT JOIN FETCH ds.user u LEFT JOIN FETCH u.department " +
           "WHERE ds.id = :id AND ds.deleted = false")
    Optional<DutySchedule> findByIdWithUser(@Param("id") Long id);
    
    /**
     * 统计所有排班总数
     */
    long countByDeletedFalse();
    
    /**
     * 根据用户ID删除所有排班记录（物理删除用户时使用）
     */
    void deleteByUserId(Long userId);
}
