package com.example.photography.service;

import com.example.photography.model.entity.LeaveRequest;
import com.example.photography.dto.request.LeaveRequestRequest;
import com.example.photography.dto.request.LeaveApprovalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 请假管理服务接口
 */
public interface LeaveService {
    
    /**
     * 提交请假申请
     */
    LeaveRequest submitLeaveRequest(LeaveRequestRequest request, Long userId);
    
    /**
     * 审批请假申请
     */
    LeaveRequest approveLeaveRequest(Long id, LeaveApprovalRequest request, Long approverId);
    
    /**
     * 取消请假申请
     */
    LeaveRequest cancelLeaveRequest(Long id, Long userId);
    
    /**
     * 删除请假申请（软删除）
     */
    void deleteLeaveRequest(Long id);
    
    /**
     * 物理删除请假申请及相关数据
     */
    void physicalDeleteLeaveRequest(Long id);
    
    /**
     * 批量物理删除请假申请
     * @param ids 请假申请ID列表
     * @return 实际删除的数量
     */
    int physicalDeleteLeaveRequests(List<Long> ids);
    
    /**
     * 获取请假申请详情
     */
    LeaveRequest getLeaveRequestById(Long id);
    
    /**
     * 获取用户的请假申请
     */
    Page<LeaveRequest> getUserLeaveRequests(Long userId, LeaveRequest.RequestStatus status, Pageable pageable);
    
    /**
     * 获取用户的请假申请（支持状态和类型筛选）
     */
    Page<LeaveRequest> getUserLeaveRequests(Long userId, LeaveRequest.RequestStatus status, 
                                          LeaveRequest.LeaveType leaveType, Pageable pageable);
    
    /**
     * 获取用户的请假申请DTO（避免懒加载问题）
     */
    Map<String, Object> getUserLeaveRequestsDTO(Long userId, LeaveRequest.RequestStatus status, 
                                               LeaveRequest.LeaveType leaveType, Pageable pageable);
    
    /**
     * 获取所有请假申请（管理员）
     */
    Page<LeaveRequest> getAllLeaveRequests(String keyword, LeaveRequest.RequestStatus status, 
                                         LeaveRequest.LeaveType leaveType, LocalDate startDate, 
                                         LocalDate endDate, Pageable pageable);

    /**
     * 管理员获取所有请假申请（返回DTO以避免懒加载序列化问题）
     */
    Map<String, Object> getAllLeaveRequestsDTO(String keyword, LeaveRequest.RequestStatus status,
                                              LeaveRequest.LeaveType leaveType, LocalDate startDate,
                                              LocalDate endDate, Pageable pageable);
    
    /**
     * 获取待审批的请假申请
     */
    List<LeaveRequest> getPendingLeaveRequests();
    
    /**
     * 获取即将到期的请假申请
     */
    List<LeaveRequest> getExpiringLeaveRequests(int days);
    
    /**
     * 获取紧急请假申请
     */
    List<LeaveRequest> getEmergencyLeaveRequests();
    
    /**
     * 检查用户在指定日期是否有已批准的请假
     */
    boolean hasApprovedLeave(Long userId, LocalDate date);

    /**
     * 检查用户在指定日期、指定打卡配置是否有已批准的打卡请假或通用请假
     */
    boolean hasApprovedCheckinLeave(Long userId, LocalDate date, Long checkinConfigurationId);

    /**
     * 检查用户在指定日期、指定执勤排班是否有已批准的执勤请假或通用请假
     */
    boolean hasApprovedDutyLeave(Long userId, LocalDate date, Long dutyScheduleId);
    
    /**
     * 获取用户在指定日期范围的请假申请
     */
    List<LeaveRequest> getUserLeaveRequests(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 上传请假附件
     */
    String uploadLeaveAttachment(Long id, MultipartFile file);
    
    /**
     * 管理员批量清理已处理的请假申请（物理删除）
     * @param beforeDate 清理此日期之前的申请
     * @param includeApproved 是否包括已批准的申请
     * @param includeRejected 是否包括已拒绝的申请
     * @param includeCancelled 是否包括已取消的申请
     * @return 清理的记录数量
     */
    int cleanupProcessedLeaveRequests(LocalDate beforeDate, boolean includeApproved, 
                                    boolean includeRejected, boolean includeCancelled);
    
    /**
     * 获取请假统计数据
     */
    Map<String, Object> getLeaveStatistics(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取全局请假统计数据（管理员）
     */
    Map<String, Object> getGlobalLeaveStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * 导出请假记录
     */
    byte[] exportLeaveRequests(String keyword, LeaveRequest.RequestStatus status, 
                              LeaveRequest.LeaveType leaveType, LocalDate startDate, LocalDate endDate);
    
    /**
     * 计算请假天数
     */
    int calculateLeaveDays(LocalDate startDate, LocalDate endDate);
    
    /**
     * 验证请假申请是否有效
     */
    boolean validateLeaveRequest(LeaveRequestRequest request, Long userId);
    
    /**
     * 检查请假日期是否冲突
     */
    boolean hasConflictingLeave(Long userId, LocalDate startDate, LocalDate endDate, Long excludeId);
    
    /**
     * 获取请假类型统计
     */
    List<Map<String, Object>> getLeaveTypeStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取请假趋势统计
     */
    List<Map<String, Object>> getLeaveTrendStatistics(LocalDate startDate, LocalDate endDate);
    
    /**
     * 自动处理过期的请假申请
     */
    void processExpiredLeaveRequests();
}
