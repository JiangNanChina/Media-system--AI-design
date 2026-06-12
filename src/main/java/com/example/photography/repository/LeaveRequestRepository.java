package com.example.photography.repository;

import com.example.photography.model.entity.LeaveRequest;
import com.example.photography.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 请假申请Repository
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    
    /**
     * 查找用户的请假申请
     */
    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user u LEFT JOIN FETCH u.department " +
           "WHERE l.user = :user AND l.deleted = false ORDER BY l.applyTime DESC")
    Page<LeaveRequest> findByUser(@Param("user") User user, Pageable pageable);
    
    /**
     * 查找用户指定状态的请假申请
     */
    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user u LEFT JOIN FETCH u.department " +
           "WHERE l.user = :user AND l.status = :status AND l.deleted = false ORDER BY l.applyTime DESC")
    Page<LeaveRequest> findByUserAndStatus(@Param("user") User user, @Param("status") LeaveRequest.RequestStatus status, Pageable pageable);
    
    /**
     * 查找用户指定类型的请假申请
     */
    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user u LEFT JOIN FETCH u.department " +
           "WHERE l.user = :user AND l.leaveType = :leaveType AND l.deleted = false ORDER BY l.applyTime DESC")
    Page<LeaveRequest> findByUserAndLeaveType(@Param("user") User user, @Param("leaveType") LeaveRequest.LeaveType leaveType, Pageable pageable);
    
    /**
     * 查找用户指定状态和类型的请假申请
     */
    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user u LEFT JOIN FETCH u.department " +
           "WHERE l.user = :user AND l.status = :status AND l.leaveType = :leaveType AND l.deleted = false ORDER BY l.applyTime DESC")
    Page<LeaveRequest> findByUserAndStatusAndLeaveType(@Param("user") User user, @Param("status") LeaveRequest.RequestStatus status, 
                                                      @Param("leaveType") LeaveRequest.LeaveType leaveType, Pageable pageable);
    
    /**
     * 查找指定状态的请假申请
     */
    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user u LEFT JOIN FETCH u.department " +
           "WHERE l.status = :status AND l.deleted = false ORDER BY l.applyTime DESC")
    Page<LeaveRequest> findByStatus(@Param("status") LeaveRequest.RequestStatus status, Pageable pageable);
    
    /**
     * 查找指定类型的请假申请
     */
    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user u LEFT JOIN FETCH u.department " +
           "WHERE l.leaveType = :leaveType AND l.deleted = false ORDER BY l.applyTime DESC")
    Page<LeaveRequest> findByLeaveType(@Param("leaveType") LeaveRequest.LeaveType leaveType, Pageable pageable);
    
    /**
     * 查找用户在指定日期范围的请假申请
     */
    @Query("SELECT l FROM LeaveRequest l WHERE l.user = :user " +
           "AND ((l.startDate <= :endDate AND l.endDate >= :startDate)) " +
           "AND l.deleted = false ORDER BY l.startDate ASC")
    List<LeaveRequest> findByUserAndDateRange(@Param("user") User user, 
                                            @Param("startDate") LocalDate startDate, 
                                            @Param("endDate") LocalDate endDate);
    
    /**
     * 查找指定日期范围的所有请假申请
     */
    @Query("SELECT l FROM LeaveRequest l WHERE " +
           "((l.startDate <= :endDate AND l.endDate >= :startDate)) " +
           "AND l.deleted = false ORDER BY l.startDate ASC")
    List<LeaveRequest> findByDateRange(@Param("startDate") LocalDate startDate, 
                                     @Param("endDate") LocalDate endDate);
    
    /**
     * 查找用户指定日期是否有已批准的请假
     */
    @Query("SELECT l FROM LeaveRequest l WHERE l.user = :user " +
           "AND l.status = 'APPROVED' " +
           "AND l.startDate <= :date AND l.endDate >= :date " +
           "AND l.deleted = false")
    List<LeaveRequest> findApprovedLeaveForUserAndDate(@Param("user") User user, 
                                                      @Param("date") LocalDate date);
    
    /**
     * 统计用户的请假次数
     */
    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.user = :user AND l.deleted = false")
    long countByUser(@Param("user") User user);
    
    /**
     * 统计用户已批准的请假天数
     */
    @Query("SELECT COALESCE(SUM(l.daysCount), 0) FROM LeaveRequest l WHERE l.user = :user " +
           "AND l.status = 'APPROVED' AND l.deleted = false")
    long sumApprovedDaysByUser(@Param("user") User user);
    
    /**
     * 统计待审批的请假申请数量
     */
    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.status = 'PENDING' AND l.deleted = false")
    long countPendingRequests();
    
    /**
     * 查找紧急请假申请
     */
    @Query("SELECT l FROM LeaveRequest l WHERE l.emergency = true AND l.deleted = false ORDER BY l.applyTime DESC")
    List<LeaveRequest> findEmergencyRequests();
    
    /**
     * 查找即将到期的请假申请（需要审批的）
     */
    @Query("SELECT l FROM LeaveRequest l WHERE l.status = 'PENDING' " +
           "AND l.startDate <= :date AND l.deleted = false ORDER BY l.startDate ASC")
    List<LeaveRequest> findExpiringRequests(@Param("date") LocalDate date);
    
    /**
     * 统计各状态的请假申请数量
     */
    @Query("SELECT l.status, COUNT(l) FROM LeaveRequest l WHERE l.deleted = false GROUP BY l.status")
    List<Object[]> countByStatus();
    
    /**
     * 统计各类型的请假申请数量
     */
    @Query("SELECT l.leaveType, COUNT(l) FROM LeaveRequest l WHERE l.deleted = false GROUP BY l.leaveType")
    List<Object[]> countByLeaveType();
    
    /**
     * 查找审批人的待办请假申请
     */
    @Query("SELECT l FROM LeaveRequest l WHERE l.status = 'PENDING' AND l.deleted = false ORDER BY l.applyTime ASC")
    List<LeaveRequest> findPendingRequestsForApprover();
    
    /**
     * 查找所有未删除的请假申请
     */
    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user u LEFT JOIN FETCH u.department " +
           "WHERE l.deleted = false ORDER BY l.applyTime DESC")
    Page<LeaveRequest> findAllActive(Pageable pageable);
    
    /**
     * 查找所有未删除的请假申请（带排序）
     */
    @Query("SELECT l FROM LeaveRequest l WHERE l.deleted = false")
    List<LeaveRequest> findByDeletedFalse(Sort sort);
    
    /**
     * 管理员复合搜索请假申请
     */
    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user u LEFT JOIN FETCH u.department d " +
           "WHERE l.deleted = false " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(u.realName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR l.status = :status) " +
           "AND (:leaveType IS NULL OR l.leaveType = :leaveType) " +
           "AND (:startDate IS NULL OR l.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR l.endDate <= :endDate) " +
           "ORDER BY l.applyTime DESC")
    Page<LeaveRequest> findByAdminSearch(@Param("keyword") String keyword,
                                        @Param("status") LeaveRequest.RequestStatus status,
                                        @Param("leaveType") LeaveRequest.LeaveType leaveType,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        Pageable pageable);
    
    /**
     * 查找指定日期已批准的请假记录
     */
    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user " +
           "WHERE l.status = 'APPROVED' AND l.deleted = false " +
           "AND :date BETWEEN l.startDate AND l.endDate " +
           "ORDER BY l.applyTime DESC")
    List<LeaveRequest> findApprovedLeavesByDate(@Param("date") LocalDate date);
    
    /**
     * 根据ID查找请假申请（预加载用户和审核人信息）
     */
    @Query("SELECT l FROM LeaveRequest l LEFT JOIN FETCH l.user u LEFT JOIN FETCH u.department " +
           "LEFT JOIN FETCH l.approver a LEFT JOIN FETCH a.department " +
           "WHERE l.id = :id")
    Optional<LeaveRequest> findByIdWithUser(@Param("id") Long id);
    
    /**
     * 查找指定日期之前的已处理请假申请（用于批量清理）
     */
    @Query("SELECT l FROM LeaveRequest l WHERE l.applyTime < :beforeDate " +
           "AND l.status IN :statuses AND l.deleted = false")
    List<LeaveRequest> findProcessedRequestsBeforeDate(@Param("beforeDate") LocalDate beforeDate, 
                                                      @Param("statuses") List<LeaveRequest.RequestStatus> statuses);
    
    /**
     * 批量删除指定日期之前的已处理请假申请
     */
    @Modifying
    @Query("DELETE FROM LeaveRequest l WHERE l.applyTime < :beforeDate " +
           "AND l.status IN :statuses AND l.deleted = false")
    int deleteProcessedRequestsBeforeDate(@Param("beforeDate") LocalDate beforeDate, 
                                        @Param("statuses") List<LeaveRequest.RequestStatus> statuses);
    
    /**
     * 删除用户的所有请假记录（物理删除）
     */
    void deleteByUserId(Long userId);
    
    /**
     * 删除该用户审批的所有请假记录（物理删除）
     */
    void deleteByApproverId(Long approverId);
}