package com.example.photography.service;

import com.example.photography.dto.request.DutyCheckinRequest;
import com.example.photography.dto.request.DutyScheduleRequest;
import com.example.photography.dto.request.DutySwapRequestCreateRequest;
import com.example.photography.dto.request.DutySwapRequestDecisionRequest;
import com.example.photography.model.entity.DutyRecord;
import com.example.photography.model.entity.DutySchedule;
import com.example.photography.model.entity.DutySwapRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * 办公室执勤服务接口
 */
public interface DutyService {
    
    // ========== 执勤排班管理 ==========
    
    /**
     * 创建执勤排班
     */
    DutySchedule createDutySchedule(DutyScheduleRequest request);
    
    /**
     * 更新执勤排班
     */
    DutySchedule updateDutySchedule(Long id, DutyScheduleRequest request);
    
    /**
     * 删除执勤排班
     */
    void deleteDutySchedule(Long id);
    
    /**
     * 启用/禁用执勤排班
     */
    void toggleDutyScheduleStatus(Long id, boolean active);
    
    /**
     * 根据ID获取执勤排班
     */
    DutySchedule findDutyScheduleById(Long id);
    
    /**
     * 获取所有执勤排班
     */
    List<DutySchedule> getAllDutySchedules();
    
    /**
     * 搜索执勤排班
     */
    List<DutySchedule> searchDutySchedules(String keyword, Integer dayOfWeek);
    
    /**
     * 获取启用的执勤排班
     */
    List<DutySchedule> getActiveDutySchedules();
    
    /**
     * 根据用户ID获取执勤排班
     */
    List<DutySchedule> getUserDutySchedules(Long userId);
    
    /**
     * 根据星期几获取执勤排班
     */
    List<DutySchedule> getDutySchedulesByDayOfWeek(Integer dayOfWeek);
    
    /**
     * 获取用户当前的执勤排班
     */
    DutySchedule getCurrentDutySchedule(Long userId);
    
    // ========== 执勤记录管理 ==========
    
    /**
     * 签到
     */
    DutyRecord checkin(Long userId, DutyCheckinRequest request);
    
    /**
     * 签退
     */
    DutyRecord checkout(Long userId, DutyCheckinRequest request);
    
    /**
     * 检查用户是否可以执勤打卡
     */
    boolean canDutyCheckin(Long userId);
    
    /**
     * 获取用户执勤记录
     */
    Page<DutyRecord> getUserDutyRecords(Long userId, Pageable pageable);
    
    /**
     * 获取所有执勤记录
     */
    Page<DutyRecord> getAllDutyRecords(Pageable pageable);
    
    /**
     * 搜索执勤记录
     */
    Page<DutyRecord> searchDutyRecords(Pageable pageable, String keyword, String status, LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据日期范围获取执勤记录
     */
    List<DutyRecord> getDutyRecordsByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据状态获取执勤记录
     */
    List<DutyRecord> getDutyRecordsByStatus(String status);
    
    /**
     * 获取用户某天的执勤记录
     */
    DutyRecord getUserDutyRecordByDate(Long userId, LocalDate date);
    
    /**
     * 导出执勤记录到Excel
     */
    byte[] exportDutyRecordsToExcel(LocalDate startDate, LocalDate endDate);
    
    /**
     * 生成执勤记录（为未来日期创建执勤记录）
     */
    void generateDutyRecords(LocalDate startDate, LocalDate endDate);
    
    /**
     * 删除执勤记录（物理删除）
     */
    void deleteDutyRecord(Long id);
    
    /**
     * 获取执勤统计信息
     */
    DutyStatistics getDutyStatistics();

    // ========== 排班调换管理 ==========

    /**
     * 发起排班调换申请（当前登录用户）
     */
    DutySwapRequest createDutySwapRequest(Long requesterId, DutySwapRequestCreateRequest request);

    /**
     * 被调换人处理排班调换申请（同意 / 拒绝）
     */
    DutySwapRequest handleDutySwapRequest(Long requestId, Long operatorId, DutySwapRequestDecisionRequest decisionRequest);

    /**
     * 管理员查看所有调换申请
     */
    java.util.List<DutySwapRequest> getAllDutySwapRequests();

    /**
     * 当前用户相关的调换申请（作为发起人或被调换人）
     */
    java.util.List<DutySwapRequest> getUserDutySwapRequests(Long userId);

    /**
     * 删除排班调换申请（物理删除，管理员）
     */
    void deleteDutySwapRequest(Long id);
    
    /**
     * 执勤统计信息类
     */
    class DutyStatistics {
        private long totalSchedules;
        private long activeSchedules;
        private long totalRecords;
        private long todayDuties;
        private long thisWeekDuties;
        private long completedDuties;
        private long missedDuties;
        
        // Constructors, getters and setters
        public DutyStatistics() {}
        
        public DutyStatistics(long totalSchedules, long activeSchedules, long totalRecords,
                            long todayDuties, long thisWeekDuties, long completedDuties, long missedDuties) {
            this.totalSchedules = totalSchedules;
            this.activeSchedules = activeSchedules;
            this.totalRecords = totalRecords;
            this.todayDuties = todayDuties;
            this.thisWeekDuties = thisWeekDuties;
            this.completedDuties = completedDuties;
            this.missedDuties = missedDuties;
        }
        
        public long getTotalSchedules() { return totalSchedules; }
        public void setTotalSchedules(long totalSchedules) { this.totalSchedules = totalSchedules; }
        
        public long getActiveSchedules() { return activeSchedules; }
        public void setActiveSchedules(long activeSchedules) { this.activeSchedules = activeSchedules; }
        
        public long getTotalRecords() { return totalRecords; }
        public void setTotalRecords(long totalRecords) { this.totalRecords = totalRecords; }
        
        public long getTodayDuties() { return todayDuties; }
        public void setTodayDuties(long todayDuties) { this.todayDuties = todayDuties; }
        
        public long getThisWeekDuties() { return thisWeekDuties; }
        public void setThisWeekDuties(long thisWeekDuties) { this.thisWeekDuties = thisWeekDuties; }
        
        public long getCompletedDuties() { return completedDuties; }
        public void setCompletedDuties(long completedDuties) { this.completedDuties = completedDuties; }
        
        public long getMissedDuties() { return missedDuties; }
        public void setMissedDuties(long missedDuties) { this.missedDuties = missedDuties; }
    }
}
