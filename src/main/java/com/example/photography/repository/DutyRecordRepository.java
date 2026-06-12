package com.example.photography.repository;

import com.example.photography.model.entity.DutyRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 执勤记录Repository
 */
@Repository
public interface DutyRecordRepository extends JpaRepository<DutyRecord, Long> {
    
    /**
     * 查找用户某天的执勤记录
     */
    @Query("SELECT dr FROM DutyRecord dr " +
           "LEFT JOIN FETCH dr.user u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH dr.dutySchedule ds " +
           "WHERE dr.deleted = false AND u.id = :userId AND dr.dutyDate = :dutyDate")
    Optional<DutyRecord> findByUser_IdAndDutyDateAndDeletedFalse(@Param("userId") Long userId, @Param("dutyDate") LocalDate dutyDate);

    /**
     * 查找用户某天某个排班的执勤记录
     */
    @Query("SELECT dr FROM DutyRecord dr " +
           "LEFT JOIN FETCH dr.user u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH dr.dutySchedule ds " +
           "WHERE dr.deleted = false AND u.id = :userId AND dr.dutyDate = :dutyDate " +
           "AND ds.id = :dutyScheduleId")
    Optional<DutyRecord> findByUser_IdAndDutyDateAndDutySchedule_IdAndDeletedFalse(
            @Param("userId") Long userId,
            @Param("dutyDate") LocalDate dutyDate,
            @Param("dutyScheduleId") Long dutyScheduleId);

    /**
     * 查找用户某天的所有执勤记录
     */
    @Query("SELECT dr FROM DutyRecord dr " +
           "LEFT JOIN FETCH dr.user u " +
           "LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH dr.dutySchedule ds " +
           "WHERE dr.deleted = false AND u.id = :userId AND dr.dutyDate = :dutyDate " +
           "ORDER BY dr.id ASC")
    List<DutyRecord> findAllByUser_IdAndDutyDateAndDeletedFalse(
            @Param("userId") Long userId,
            @Param("dutyDate") LocalDate dutyDate);
    
    /**
     * 根据用户ID查找执勤记录
     */
    @Query(value = "SELECT dr FROM DutyRecord dr LEFT JOIN FETCH dr.user u LEFT JOIN FETCH u.department LEFT JOIN FETCH dr.dutySchedule ds WHERE dr.deleted = false AND u.id = :userId ORDER BY dr.dutyDate DESC",
           countQuery = "SELECT COUNT(dr) FROM DutyRecord dr WHERE dr.deleted = false AND dr.user.id = :userId")
    Page<DutyRecord> findByUser_IdAndDeletedFalseOrderByDutyDateDesc(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 根据状态查找执勤记录
     */
    List<DutyRecord> findByStatusAndDeletedFalse(String status);
    
    /**
     * 根据日期范围查找执勤记录
     */
    @Query("SELECT dr FROM DutyRecord dr WHERE dr.deleted = false AND " +
           "dr.dutyDate BETWEEN :startDate AND :endDate ORDER BY dr.dutyDate DESC")
    List<DutyRecord> findByDateRange(@Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate);
    
    /**
     * 根据执勤排班ID查找记录
     */
    List<DutyRecord> findByDutyScheduleIdAndDeletedFalse(Long dutyScheduleId);
    
    /**
     * 查找某个日期的所有执勤记录
     */
    @Query("SELECT dr FROM DutyRecord dr LEFT JOIN FETCH dr.user u LEFT JOIN FETCH u.department LEFT JOIN FETCH dr.dutySchedule ds WHERE dr.deleted = false AND dr.dutyDate = :dutyDate")
    List<DutyRecord> findByDutyDateAndDeletedFalse(@Param("dutyDate") LocalDate dutyDate);
    
    /**
     * 统计用户执勤次数
     */
    long countByUser_IdAndDeletedFalse(Long userId);
    
    /**
     * 统计某个月的执勤次数
     */
    @Query("SELECT COUNT(dr) FROM DutyRecord dr WHERE dr.deleted = false AND " +
           "YEAR(dr.dutyDate) = :year AND MONTH(dr.dutyDate) = :month")
    long countByYearAndMonth(@Param("year") int year, @Param("month") int month);
    
    /**
     * 查找所有执勤记录（主动加载用户、部门、排班）
     */
    @Query(value = "SELECT dr FROM DutyRecord dr LEFT JOIN FETCH dr.user u LEFT JOIN FETCH u.department LEFT JOIN FETCH dr.dutySchedule ds WHERE dr.deleted = false ORDER BY dr.dutyDate DESC",
           countQuery = "SELECT COUNT(dr) FROM DutyRecord dr WHERE dr.deleted = false")
    Page<DutyRecord> findByDeletedFalseOrderByDutyDateDesc(Pageable pageable);
    
    /**
     * 统计所有执勤记录总数
     */
    long countByDeletedFalse();
    
    /**
     * 删除用户的所有执勤记录（物理删除）
     */
    void deleteByUserId(Long userId);
}
