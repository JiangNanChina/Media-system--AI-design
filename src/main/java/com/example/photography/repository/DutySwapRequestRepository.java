package com.example.photography.repository;

import com.example.photography.model.entity.DutySwapRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 办公室执勤排班调换申请 Repository
 */
@Repository
public interface DutySwapRequestRepository extends JpaRepository<DutySwapRequest, Long> {

    /**
     * 当前用户作为发起人或被调换人时，查询其相关的调换申请（按创建时间倒序，主动加载关联数据）
     */
    @Query("SELECT dsr FROM DutySwapRequest dsr " +
            "LEFT JOIN FETCH dsr.requester r LEFT JOIN FETCH r.department " +
            "LEFT JOIN FETCH dsr.targetUser t LEFT JOIN FETCH t.department " +
            "LEFT JOIN FETCH dsr.requesterSchedule rs LEFT JOIN FETCH rs.user ru LEFT JOIN FETCH ru.department " +
            "LEFT JOIN FETCH dsr.targetSchedule ts LEFT JOIN FETCH ts.user tu LEFT JOIN FETCH tu.department " +
            "WHERE dsr.deleted = false AND (r.id = :userId OR t.id = :userId) " +
            "ORDER BY dsr.createdAt DESC")
    List<DutySwapRequest> findByUserIdWithDetails(@Param("userId") Long userId);

    /**
     * 管理员查看所有未删除的调换申请（主动加载关联数据）
     */
    @Query("SELECT dsr FROM DutySwapRequest dsr " +
            "LEFT JOIN FETCH dsr.requester r LEFT JOIN FETCH r.department " +
            "LEFT JOIN FETCH dsr.targetUser t LEFT JOIN FETCH t.department " +
            "LEFT JOIN FETCH dsr.requesterSchedule rs LEFT JOIN FETCH rs.user ru LEFT JOIN FETCH ru.department " +
            "LEFT JOIN FETCH dsr.targetSchedule ts LEFT JOIN FETCH ts.user tu LEFT JOIN FETCH tu.department " +
            "WHERE dsr.deleted = false " +
            "ORDER BY dsr.createdAt DESC")
    List<DutySwapRequest> findAllWithDetails();

    /**
     * 查询某个用户在指定日期已同意的调换申请（用于计算当日有效排班）
     */
    @Query("SELECT dsr FROM DutySwapRequest dsr " +
            "LEFT JOIN FETCH dsr.requester r LEFT JOIN FETCH r.department " +
            "LEFT JOIN FETCH dsr.targetUser t LEFT JOIN FETCH t.department " +
            "LEFT JOIN FETCH dsr.requesterSchedule rs LEFT JOIN FETCH rs.user ru LEFT JOIN FETCH ru.department " +
            "LEFT JOIN FETCH dsr.targetSchedule ts LEFT JOIN FETCH ts.user tu LEFT JOIN FETCH tu.department " +
            "WHERE dsr.deleted = false AND dsr.status = 'APPROVED' " +
            "AND dsr.swapDate = :swapDate AND (r.id = :userId OR t.id = :userId)")
    List<DutySwapRequest> findApprovedByUserAndDate(@Param("userId") Long userId,
                                                    @Param("swapDate") LocalDate swapDate);

    /**
     * 根据排班ID查找与之关联的所有调换申请（无论作为发起排班还是目标排班）
     */
    List<DutySwapRequest> findByRequesterSchedule_IdOrTargetSchedule_Id(Long requesterScheduleId,
                                                                        Long targetScheduleId);
}


