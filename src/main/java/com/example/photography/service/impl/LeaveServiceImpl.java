package com.example.photography.service.impl;

import com.example.photography.dto.request.LeaveApprovalRequest;
import com.example.photography.dto.request.LeaveRequestRequest;
import com.example.photography.model.entity.LeaveRequest;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import com.example.photography.model.entity.DutyRecord;
import com.example.photography.model.entity.DutySchedule;
import com.example.photography.model.entity.CheckinConfiguration;
import com.example.photography.model.entity.CheckinRecord;
import com.example.photography.repository.LeaveRequestRepository;
import com.example.photography.repository.UserRepository;
import com.example.photography.repository.DutyRecordRepository;
import com.example.photography.repository.CheckinRecordRepository;
import com.example.photography.service.EmailNotificationService;
import com.example.photography.service.LeaveService;
import com.example.photography.utils.CheckinWeekdayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.photography.util.FileUploadUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Optional;

/**
 * 请假管理服务实现类
 */
@Service
@Transactional
@Slf4j
public class LeaveServiceImpl implements LeaveService {
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DutyRecordRepository dutyRecordRepository;
    
    @Autowired
    private CheckinRecordRepository checkinRecordRepository;
    
    @Autowired
    private com.example.photography.repository.DutyScheduleRepository dutyScheduleRepository;
    
    @Autowired
    private com.example.photography.repository.CheckinConfigurationRepository checkinConfigurationRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;
    
    // private final String uploadDir = "uploads/leave/"; // 未使用，保留占位可在实现附件上传目录时启用
    
    @Override
    public LeaveRequest submitLeaveRequest(LeaveRequestRequest request, Long userId) {
        // 获取用户信息（预加载部门）
        User user = userRepository.findByIdWithDepartment(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证请假申请
        if (!validateLeaveRequest(request, userId)) {
            throw new RuntimeException("请假申请验证失败");
        }
        
        // 创建请假申请
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setUser(user);
        leaveRequest.setLeaveType(request.getLeaveType());
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setReason(request.getReason());
        leaveRequest.setAttachment(request.getAttachment());
        leaveRequest.setEmergency(request.getEmergency());
        leaveRequest.setContactPhone(request.getContactPhone());
        leaveRequest.setContactPerson(request.getContactPerson());
        leaveRequest.setCheckinConfigurationId(request.getCheckinConfigurationId());
        leaveRequest.setDutyScheduleIds(serializeDutyScheduleIds(request.getDutyScheduleIds()));
        leaveRequest.setApplyTime(LocalDateTime.now());
        leaveRequest.setStatus(LeaveRequest.RequestStatus.PENDING);
        
        // 计算请假天数
        int days = calculateLeaveDays(request.getStartDate(), request.getEndDate());
        leaveRequest.setDaysCount(days);
        
        // 保存请假申请
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);

        try {
            emailNotificationService.notifyLeaveApproval(savedRequest);
        } catch (Exception e) {
            log.warn("请假审批邮件提醒发送失败: leaveRequestId={}", savedRequest.getId(), e);
        }
        
        // 重新查询以确保包含完整的关联数据
        return leaveRequestRepository.findByIdWithUser(savedRequest.getId())
                .orElse(savedRequest);
    }
    
    @Override
    public LeaveRequest approveLeaveRequest(Long id, LeaveApprovalRequest request, Long approverId) {
        // 获取请假申请（预加载用户和部门信息）
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdWithUser(id)
                .orElseThrow(() -> new RuntimeException("请假申请不存在"));
        
        // 检查申请状态
        if (leaveRequest.getStatus() != LeaveRequest.RequestStatus.PENDING) {
            throw new RuntimeException("该请假申请已经处理过了");
        }
        
        // 获取审批人（预加载部门信息）
        User approver = userRepository.findByIdWithDepartment(approverId)
                .orElseThrow(() -> new RuntimeException("审批人不存在"));
        if (!canApprove(approver, leaveRequest.getUser())) {
            throw new org.springframework.security.access.AccessDeniedException("无权审批该请假申请");
        }
        
        // 更新审批信息
        leaveRequest.setStatus(request.getStatus());
        leaveRequest.setApprover(approver);
        leaveRequest.setApproveTime(LocalDateTime.now());
        leaveRequest.setApproveNotes(request.getApproveNotes());
        
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        // 如果请假审核通过，自动更新相关记录状态
        if (request.getStatus() == LeaveRequest.RequestStatus.APPROVED) {
            System.out.println("=== 请假审批通过，开始更新相关记录 ===");
            System.out.println("请假类型: " + leaveRequest.getLeaveType());
            System.out.println("请假日期: " + leaveRequest.getStartDate() + " 到 " + leaveRequest.getEndDate());
            updateRelatedRecordsStatus(leaveRequest);
            try {
                emailNotificationService.notifyLeaveApprovedToApplicant(savedRequest);
            } catch (Exception e) {
                log.warn("请假审批通过通知申请人失败: leaveRequestId={}", savedRequest.getId(), e);
            }
            System.out.println("=== 相关记录更新完成 ===");
        }
        
        // 重新查询以确保返回完整的关联数据
        return leaveRequestRepository.findByIdWithUser(savedRequest.getId())
                .orElse(savedRequest);
    }
    
    @Override
    public LeaveRequest cancelLeaveRequest(Long id, Long userId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByIdWithUser(id)
                .orElseThrow(() -> new RuntimeException("请假申请不存在"));
        
        // 检查是否是申请人
        if (!leaveRequest.getUser().getId().equals(userId)) {
            throw new RuntimeException("只能取消自己的请假申请");
        }
        
        // 检查申请状态
        if (leaveRequest.getStatus() != LeaveRequest.RequestStatus.PENDING) {
            throw new RuntimeException("只能取消待审批的申请");
        }
        
        // 物理删除记录以节省存储空间
        leaveRequestRepository.delete(leaveRequest);
        
        // 返回删除的记录（用于前端显示）
        return leaveRequest;
    }
    
    @Override
    @Transactional(readOnly = true)
    public LeaveRequest getLeaveRequestById(Long id) {
        return leaveRequestRepository.findByIdWithUser(id)
                .filter(request -> !request.getDeleted())
                .orElseThrow(() -> new RuntimeException("请假申请不存在"));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequest> getUserLeaveRequests(Long userId, LeaveRequest.RequestStatus status, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        if (status != null) {
            return leaveRequestRepository.findByUserAndStatus(user, status, pageable);
        } else {
            return leaveRequestRepository.findByUser(user, pageable);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequest> getUserLeaveRequests(Long userId, LeaveRequest.RequestStatus status, 
                                                 LeaveRequest.LeaveType leaveType, Pageable pageable) {
        User user = userRepository.findByIdWithDepartment(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 根据参数组合调用不同的repository方法
        if (status != null && leaveType != null) {
            return leaveRequestRepository.findByUserAndStatusAndLeaveType(user, status, leaveType, pageable);
        } else if (status != null) {
            return leaveRequestRepository.findByUserAndStatus(user, status, pageable);
        } else if (leaveType != null) {
            return leaveRequestRepository.findByUserAndLeaveType(user, leaveType, pageable);
        } else {
            return leaveRequestRepository.findByUser(user, pageable);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserLeaveRequestsDTO(Long userId, LeaveRequest.RequestStatus status, 
                                                      LeaveRequest.LeaveType leaveType, Pageable pageable) {
        System.out.println("Service: 开始获取用户请假申请DTO，用户ID: " + userId);
        
        // 先获取实体数据
        Page<LeaveRequest> requests = getUserLeaveRequests(userId, status, leaveType, pageable);
        System.out.println("Service: 成功获取请假申请，总数: " + requests.getTotalElements());
        
        // 在事务内构建DTO
        List<Map<String, Object>> simplifiedContent = new ArrayList<>();
        
        for (LeaveRequest request : requests.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", request.getId());
            item.put("leaveType", request.getLeaveType() != null ? request.getLeaveType().name() : null);
            item.put("startDate", request.getStartDate());
            item.put("endDate", request.getEndDate());
            item.put("reason", request.getReason());
            item.put("status", request.getStatus() != null ? request.getStatus().name() : null);
            item.put("applyTime", request.getApplyTime());
            item.put("approveTime", request.getApproveTime());
            item.put("approveNotes", request.getApproveNotes());
            item.put("daysCount", request.getDaysCount());
            item.put("emergency", request.getEmergency());
            item.put("contactPhone", request.getContactPhone());
            item.put("contactPerson", request.getContactPerson());
            item.put("attachment", request.getAttachment());
            item.put("checkinConfigurationId", request.getCheckinConfigurationId());
            item.put("dutyScheduleIds", parseDutyScheduleIds(request.getDutyScheduleIds()));
            
            // 在事务内安全地访问懒加载关联对象
            if (request.getUser() != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", request.getUser().getId());
                userInfo.put("username", request.getUser().getUsername());
                userInfo.put("realName", request.getUser().getRealName());
                
                // 访问部门信息
                if (request.getUser().getDepartment() != null) {
                    Map<String, Object> deptInfo = new HashMap<>();
                    deptInfo.put("id", request.getUser().getDepartment().getId());
                    deptInfo.put("name", request.getUser().getDepartment().getName());
                    userInfo.put("department", deptInfo);
                }
                item.put("user", userInfo);
            }
            
            // 访问审批人信息
            if (request.getApprover() != null) {
                Map<String, Object> approverInfo = new HashMap<>();
                approverInfo.put("id", request.getApprover().getId());
                approverInfo.put("username", request.getApprover().getUsername());
                approverInfo.put("realName", request.getApprover().getRealName());
                item.put("approver", approverInfo);
            }
            
            simplifiedContent.add(item);
        }
        
        // 创建分页响应
        Map<String, Object> pageResponse = new HashMap<>();
        pageResponse.put("content", simplifiedContent);
        pageResponse.put("totalElements", requests.getTotalElements());
        pageResponse.put("totalPages", requests.getTotalPages());
        pageResponse.put("size", requests.getSize());
        pageResponse.put("number", requests.getNumber());
        pageResponse.put("first", requests.isFirst());
        pageResponse.put("last", requests.isLast());
        pageResponse.put("empty", requests.isEmpty());
        
        System.out.println("Service: DTO响应创建成功，包含 " + simplifiedContent.size() + " 条记录");
        return pageResponse;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<LeaveRequest> getAllLeaveRequests(String keyword, LeaveRequest.RequestStatus status, 
                                                 LeaveRequest.LeaveType leaveType, LocalDate startDate, 
                                                 LocalDate endDate, Pageable pageable) {
        // 使用复合搜索方法
        return leaveRequestRepository.findByAdminSearch(keyword, status, leaveType, startDate, endDate, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAllLeaveRequestsDTO(String keyword, LeaveRequest.RequestStatus status,
                                                     LeaveRequest.LeaveType leaveType, LocalDate startDate,
                                                     LocalDate endDate, Pageable pageable) {
        Page<LeaveRequest> requests = getAllLeaveRequests(keyword, status, leaveType, startDate, endDate, pageable);

        List<Map<String, Object>> content = new ArrayList<>();
        for (LeaveRequest request : requests.getContent()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", request.getId());
            item.put("leaveType", request.getLeaveType() != null ? request.getLeaveType().name() : null);
            item.put("startDate", request.getStartDate());
            item.put("endDate", request.getEndDate());
            item.put("reason", request.getReason());
            item.put("status", request.getStatus() != null ? request.getStatus().name() : null);
            item.put("applyTime", request.getApplyTime());
            item.put("approveTime", request.getApproveTime());
            item.put("approveNotes", request.getApproveNotes());
            item.put("daysCount", request.getDaysCount());
            item.put("emergency", request.getEmergency());
            item.put("contactPhone", request.getContactPhone());
            item.put("contactPerson", request.getContactPerson());
            item.put("checkinConfigurationId", request.getCheckinConfigurationId());
            item.put("dutyScheduleIds", parseDutyScheduleIds(request.getDutyScheduleIds()));

            if (request.getUser() != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", request.getUser().getId());
                userInfo.put("username", request.getUser().getUsername());
                userInfo.put("realName", request.getUser().getRealName());
                if (request.getUser().getDepartment() != null) {
                    userInfo.put("departmentName", request.getUser().getDepartment().getName());
                }
                item.put("user", userInfo);
            }

            if (request.getApprover() != null) {
                Map<String, Object> approverInfo = new HashMap<>();
                approverInfo.put("id", request.getApprover().getId());
                approverInfo.put("username", request.getApprover().getUsername());
                approverInfo.put("realName", request.getApprover().getRealName());
                item.put("approver", approverInfo);
            }

            content.add(item);
        }

        Map<String, Object> pageResponse = new HashMap<>();
        pageResponse.put("content", content);
        pageResponse.put("totalElements", requests.getTotalElements());
        pageResponse.put("totalPages", requests.getTotalPages());
        pageResponse.put("size", requests.getSize());
        pageResponse.put("number", requests.getNumber());
        pageResponse.put("first", requests.isFirst());
        pageResponse.put("last", requests.isLast());
        pageResponse.put("empty", requests.isEmpty());
        return pageResponse;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatus(LeaveRequest.RequestStatus.PENDING, 
                org.springframework.data.domain.Pageable.unpaged()).getContent();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequest> getExpiringLeaveRequests(int days) {
        LocalDate cutoffDate = LocalDate.now().plusDays(days);
        return leaveRequestRepository.findExpiringRequests(cutoffDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequest> getEmergencyLeaveRequests() {
        return leaveRequestRepository.findEmergencyRequests();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasApprovedLeave(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        List<LeaveRequest> requests = leaveRequestRepository.findApprovedLeaveForUserAndDate(user, date);
        return !requests.isEmpty();
    }

    private boolean canApprove(User approver, User applicant) {
        UserRole role = approver.getRole();
        if (role.isSuperAdmin()) return !approver.getId().equals(applicant.getId());
        if (applicant.getRole() == UserRole.MEMBER) {
            if (role == UserRole.DIRECTOR) return true;
            return role == UserRole.MINISTER && approver.getDepartment() != null && applicant.getDepartment() != null
                    && approver.getDepartment().getId().equals(applicant.getDepartment().getId());
        }
        return applicant.getRole() == UserRole.MINISTER && role == UserRole.DIRECTOR;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasApprovedCheckinLeave(Long userId, LocalDate date, Long checkinConfigurationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return leaveRequestRepository.findApprovedLeaveForUserAndDate(user, date).stream()
                .anyMatch(request -> appliesToCheckinConfiguration(request, checkinConfigurationId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasApprovedDutyLeave(Long userId, LocalDate date, Long dutyScheduleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return leaveRequestRepository.findApprovedLeaveForUserAndDate(user, date).stream()
                .anyMatch(request -> appliesToDutySchedule(request, dutyScheduleId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequest> getUserLeaveRequests(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        return leaveRequestRepository.findByUserAndDateRange(user, startDate, endDate);
    }
    
    @Override
    public String uploadLeaveAttachment(Long id, MultipartFile file) {
        // 使用统一的文件上传工具
        FileUploadUtil fileUploadUtil = new FileUploadUtil();
        String fileUrl = fileUploadUtil.uploadFile(file, "leave");
        
        // 更新请假申请的附件信息
        LeaveRequest leaveRequest = getLeaveRequestById(id);
        leaveRequest.setAttachment(fileUrl);
        leaveRequestRepository.save(leaveRequest);
        
        return fileUrl;
    }
    
    @Override
    @Transactional
    public int cleanupProcessedLeaveRequests(LocalDate beforeDate, boolean includeApproved, 
                                           boolean includeRejected, boolean includeCancelled) {
        List<LeaveRequest.RequestStatus> statusesToCleanup = new ArrayList<>();
        
        if (includeApproved) {
            statusesToCleanup.add(LeaveRequest.RequestStatus.APPROVED);
        }
        if (includeRejected) {
            statusesToCleanup.add(LeaveRequest.RequestStatus.REJECTED);
        }
        if (includeCancelled) {
            statusesToCleanup.add(LeaveRequest.RequestStatus.CANCELLED);
        }
        
        if (statusesToCleanup.isEmpty()) {
            return 0;
        }
        
        // 执行批量删除
        return leaveRequestRepository.deleteProcessedRequestsBeforeDate(beforeDate, statusesToCleanup);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getLeaveStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 总申请次数
        long totalRequests = leaveRequestRepository.countByUser(user);
        
        // 已批准请假天数
        long approvedDays = leaveRequestRepository.sumApprovedDaysByUser(user);
        
        stats.put("totalRequests", totalRequests);
        stats.put("approvedDays", approvedDays);
        
        return stats;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getGlobalLeaveStatistics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        // 各状态统计
        List<Object[]> statusStats = leaveRequestRepository.countByStatus();
        
        long totalRequests = 0;
        long pendingRequests = 0;
        long approvedRequests = 0;
        long rejectedRequests = 0;
        
        for (Object[] row : statusStats) {
            String status = row[0].toString();
            Long count = (Long) row[1];
            totalRequests += count;
            
            switch (status) {
                case "PENDING":
                    pendingRequests = count;
                    break;
                case "APPROVED":
                    approvedRequests = count;
                    break;
                case "REJECTED":
                    rejectedRequests = count;
                    break;
            }
        }
        
        // 返回前端期望的格式
        stats.put("totalRequests", totalRequests);
        stats.put("pendingRequests", pendingRequests);
        stats.put("approvedRequests", approvedRequests);
        stats.put("rejectedRequests", rejectedRequests);
        
        return stats;
    }
    
    @Override
    public byte[] exportLeaveRequests(String keyword, LeaveRequest.RequestStatus status, 
                                    LeaveRequest.LeaveType leaveType, LocalDate startDate, LocalDate endDate) {
        // TODO: 实现Excel导出功能
        throw new RuntimeException("Excel导出功能待实现");
    }
    
    @Override
    public int calculateLeaveDays(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
    
    @Override
    public boolean validateLeaveRequest(LeaveRequestRequest request, Long userId) {
        System.out.println("=== 验证请假申请 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("请假类型: " + request.getLeaveType());
        System.out.println("开始日期: " + request.getStartDate());
        System.out.println("结束日期: " + request.getEndDate());
        System.out.println("请假原因: " + request.getReason());
        
        // 验证日期范围
        if (request.getEndDate().isBefore(request.getStartDate())) {
            System.err.println("❌ 日期验证失败: 结束日期早于开始日期");
            throw new RuntimeException("结束日期不能早于开始日期");
        }

        if (request.getLeaveType() == LeaveRequest.LeaveType.CHECKIN_LEAVE
                && request.getCheckinConfigurationId() == null) {
            throw new RuntimeException("打卡请假必须选择打卡配置");
        }

        if (request.getLeaveType() == LeaveRequest.LeaveType.DUTY_LEAVE
                && (request.getDutyScheduleIds() == null || request.getDutyScheduleIds().isEmpty())) {
            throw new RuntimeException("执勤请假必须选择执勤排班");
        }
        
        // 检查是否有冲突的请假申请
        boolean hasConflict = hasConflictingLeave(userId, request.getStartDate(), request.getEndDate(), null);
        System.out.println("是否有冲突的请假申请: " + hasConflict);
        
        // 临时禁用冲突检查以便调试
        if (hasConflict) {
            System.err.println("⚠️ 发现冲突的请假申请，但暂时允许通过（调试模式）");
            // return false; // 临时注释掉
        }
        
        System.out.println("✅ 验证通过");
        return true;
    }
    
    @Override
    public boolean hasConflictingLeave(Long userId, LocalDate startDate, LocalDate endDate, Long excludeId) {
        System.out.println("=== 检查冲突的请假申请 ===");
        System.out.println("用户ID: " + userId + ", 日期范围: " + startDate + " 到 " + endDate);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        List<LeaveRequest> conflictingRequests = leaveRequestRepository.findByUserAndDateRange(user, startDate, endDate);
        System.out.println("找到的请假申请数量: " + conflictingRequests.size());
        
        for (LeaveRequest request : conflictingRequests) {
            System.out.println("  - 申请ID: " + request.getId() + 
                             ", 类型: " + request.getLeaveType() + 
                             ", 状态: " + request.getStatus() + 
                             ", 日期: " + request.getStartDate() + " 到 " + request.getEndDate());
        }
        
        boolean hasConflict = conflictingRequests.stream()
                .anyMatch(request -> 
                    (excludeId == null || !request.getId().equals(excludeId)) &&
                    (request.getStatus() == LeaveRequest.RequestStatus.PENDING || 
                     request.getStatus() == LeaveRequest.RequestStatus.APPROVED));
        
        System.out.println("是否有冲突: " + hasConflict);
        return hasConflict;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLeaveTypeStatistics(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = leaveRequestRepository.countByLeaveType();
        List<Map<String, Object>> stats = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("type", row[0].toString());
            stat.put("count", row[1]);
            stats.add(stat);
        }
        
        return stats;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLeaveTrendStatistics(LocalDate startDate, LocalDate endDate) {
        // TODO: 实现趋势统计
        return new ArrayList<>();
    }
    
    @Override
    public void processExpiredLeaveRequests() {
        // TODO: 实现过期请假申请处理
    }
    
    /**
     * 更新相关记录状态
     * 当请假审核通过时，自动更新执勤和打卡记录状态
     */
    private void updateRelatedRecordsStatus(LeaveRequest leaveRequest) {
        User user = leaveRequest.getUser();
        LocalDate startDate = leaveRequest.getStartDate();
        LocalDate endDate = leaveRequest.getEndDate();
        LeaveRequest.LeaveType leaveType = leaveRequest.getLeaveType();
        
        // 遍历请假日期范围
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            
            // 处理执勤请假
            if (leaveType == LeaveRequest.LeaveType.DUTY_LEAVE) {
                updateDutyRecordStatus(user, currentDate, leaveRequest);
            }
            
            // 处理打卡请假
            if (leaveType == LeaveRequest.LeaveType.CHECKIN_LEAVE) {
                updateCheckinRecordStatus(user, currentDate, leaveRequest);
            }
            
            // 处理其他类型请假（同时影响执勤和打卡）
            if (leaveType == LeaveRequest.LeaveType.OTHER) {
                updateDutyRecordStatus(user, currentDate, leaveRequest);
                updateCheckinRecordStatus(user, currentDate, leaveRequest);
            }
            
            currentDate = currentDate.plusDays(1);
        }
    }
    
    /**
     * 更新执勤记录状态
     */
    private void updateDutyRecordStatus(User user, LocalDate date, LeaveRequest leaveRequest) {
        List<DutySchedule> targetSchedules = getTargetDutySchedules(user, date, leaveRequest);
        if (targetSchedules.isEmpty()) {
            log.info("未找到需要标记请假的执勤排班: userId={}, date={}, leaveRequestId={}",
                    user.getId(), date, leaveRequest.getId());
            return;
        }

        for (DutySchedule schedule : targetSchedules) {
            updateDutyRecordStatus(user, date, leaveRequest, schedule);
        }
    }

    private void updateDutyRecordStatus(User user, LocalDate date, LeaveRequest leaveRequest, DutySchedule schedule) {
        Optional<DutyRecord> dutyRecordOpt = dutyRecordRepository
                .findByUser_IdAndDutyDateAndDutySchedule_IdAndDeletedFalse(user.getId(), date, schedule.getId());

        DutyRecord dutyRecord = dutyRecordOpt.orElseGet(() -> new DutyRecord(user, schedule, date));

        if (dutyRecord.getCheckinTime() != null && !"缺勤".equals(dutyRecord.getStatus())) {
            log.info("执勤记录已有签到时间，跳过请假状态覆盖: recordId={}, userId={}, date={}, scheduleId={}",
                    dutyRecord.getId(), user.getId(), date, schedule.getId());
            return;
        }

        dutyRecord.setStatus("已请假");
        String approverName = leaveRequest.getApprover() != null ? leaveRequest.getApprover().getRealName() : "系统";
        dutyRecord.setNotes("请假申请已批准 - " + leaveRequest.getReason() + " | 审核人：" + approverName);
        dutyRecordRepository.save(dutyRecord);
    }
    
    /**
     * 更新打卡记录状态
     */
    private void updateCheckinRecordStatus(User user, LocalDate date, LeaveRequest leaveRequest) {
        List<CheckinConfiguration> targetConfigs = getTargetCheckinConfigurations(leaveRequest, date);
        if (targetConfigs.isEmpty()) {
            log.info("未找到需要标记请假的打卡配置: userId={}, date={}, leaveRequestId={}",
                    user.getId(), date, leaveRequest.getId());
            return;
        }

        String approverName = leaveRequest.getApprover() != null ? leaveRequest.getApprover().getRealName() : "系统";
        String notes = "请假申请已批准 - " + leaveRequest.getReason() + " | 审核人：" + approverName;

        for (CheckinConfiguration config : targetConfigs) {
            CheckinRecord record = checkinRecordRepository
                    .findByUserAndConfigurationAndDate(user, config, date)
                    .orElseGet(() -> {
                        CheckinRecord leaveRecord = new CheckinRecord();
                        leaveRecord.setUser(user);
                        leaveRecord.setConfiguration(config);
                        leaveRecord.setCheckinTime(date.atTime(config.getStartTime()));
                        leaveRecord.setIsLate(false);
                        leaveRecord.setLateMinutes(0);
                        return leaveRecord;
                    });

            record.setStatus(CheckinRecord.CheckinStatus.LEAVE);
            record.setAuditStatus(CheckinRecord.AuditStatus.NOT_REQUIRED);
            record.setIsLate(false);
            record.setLateMinutes(0);
            record.setNotes(notes);
            record.setUpdatedAt(LocalDateTime.now());
            checkinRecordRepository.save(record);
        }
    }

    private List<CheckinConfiguration> getTargetCheckinConfigurations(LeaveRequest leaveRequest, LocalDate date) {
        Long configId = leaveRequest.getLeaveType() == LeaveRequest.LeaveType.CHECKIN_LEAVE
                ? leaveRequest.getCheckinConfigurationId()
                : null;

        if (configId != null) {
            return checkinConfigurationRepository.findById(configId)
                    .filter(config -> !Boolean.TRUE.equals(config.getDeleted()))
                    .filter(config -> CheckinWeekdayUtils.isRequiredOnDate(config.getRequiredWeekdays(), date))
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }

        if (leaveRequest.getLeaveType() == LeaveRequest.LeaveType.CHECKIN_LEAVE) {
            return Collections.emptyList();
        }

        return checkinConfigurationRepository.findByIsActiveTrueAndDeletedFalseOrderBySortOrderAsc().stream()
                .filter(config -> CheckinWeekdayUtils.isRequiredOnDate(config.getRequiredWeekdays(), date))
                .toList();
    }

    private List<DutySchedule> getTargetDutySchedules(User user, LocalDate date, LeaveRequest leaveRequest) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        Set<Long> selectedScheduleIds = parseDutyScheduleIdSet(leaveRequest.getDutyScheduleIds());

        if (selectedScheduleIds.isEmpty()) {
            if (leaveRequest.getLeaveType() == LeaveRequest.LeaveType.DUTY_LEAVE) {
                return Collections.emptyList();
            }
            return dutyScheduleRepository.findByUser_IdAndDayOfWeekAndActiveTrueAndDeletedFalse(user.getId(), dayOfWeek);
        }

        List<DutySchedule> schedules = new ArrayList<>();
        for (Long scheduleId : selectedScheduleIds) {
            dutyScheduleRepository.findById(scheduleId)
                    .filter(schedule -> !Boolean.TRUE.equals(schedule.getDeleted()))
                    .filter(schedule -> Boolean.TRUE.equals(schedule.getActive()))
                    .filter(schedule -> schedule.getUser() != null && schedule.getUser().getId().equals(user.getId()))
                    .filter(schedule -> schedule.getDayOfWeek() != null && schedule.getDayOfWeek().equals(dayOfWeek))
                    .ifPresent(schedules::add);
        }
        return schedules;
    }

    private boolean appliesToCheckinConfiguration(LeaveRequest request, Long checkinConfigurationId) {
        if (request.getLeaveType() == LeaveRequest.LeaveType.OTHER) {
            return true;
        }

        if (request.getLeaveType() != LeaveRequest.LeaveType.CHECKIN_LEAVE) {
            return false;
        }

        Long targetConfigId = request.getCheckinConfigurationId();
        return targetConfigId != null && checkinConfigurationId != null && targetConfigId.equals(checkinConfigurationId);
    }

    private boolean appliesToDutySchedule(LeaveRequest request, Long dutyScheduleId) {
        if (request.getLeaveType() == LeaveRequest.LeaveType.OTHER) {
            return true;
        }

        if (request.getLeaveType() != LeaveRequest.LeaveType.DUTY_LEAVE) {
            return false;
        }

        Set<Long> targetScheduleIds = parseDutyScheduleIdSet(request.getDutyScheduleIds());
        return dutyScheduleId != null && targetScheduleIds.contains(dutyScheduleId);
    }

    private String serializeDutyScheduleIds(List<Long> dutyScheduleIds) {
        if (dutyScheduleIds == null || dutyScheduleIds.isEmpty()) {
            return null;
        }

        StringJoiner joiner = new StringJoiner(",");
        Set<Long> uniqueIds = new LinkedHashSet<>(dutyScheduleIds);
        for (Long id : uniqueIds) {
            if (id != null) {
                joiner.add(id.toString());
            }
        }
        String serialized = joiner.toString();
        return serialized.isBlank() ? null : serialized;
    }

    private List<Long> parseDutyScheduleIds(String dutyScheduleIds) {
        return new ArrayList<>(parseDutyScheduleIdSet(dutyScheduleIds));
    }

    private Set<Long> parseDutyScheduleIdSet(String dutyScheduleIds) {
        Set<Long> ids = new LinkedHashSet<>();
        if (dutyScheduleIds == null || dutyScheduleIds.isBlank()) {
            return ids;
        }

        for (String idText : dutyScheduleIds.split(",")) {
            if (idText == null || idText.isBlank()) {
                continue;
            }
            try {
                ids.add(Long.parseLong(idText.trim()));
            } catch (NumberFormatException e) {
                log.warn("忽略无法解析的执勤排班ID: {}", idText);
            }
        }
        return ids;
    }
    
    @Override
    @Transactional
    public void deleteLeaveRequest(Long id) {
        LeaveRequest leaveRequest = getLeaveRequestById(id);
        leaveRequest.setDeleted(true);
        leaveRequestRepository.save(leaveRequest);
        System.out.println("请假申请已软删除: " + id);
    }
    
    @Override
    @Transactional
    public void physicalDeleteLeaveRequest(Long id) {
        LeaveRequest leaveRequest = getLeaveRequestById(id);
        
        System.out.println("开始物理删除请假申请ID: " + id + ", 申请人: " + 
                          (leaveRequest.getUser() != null ? leaveRequest.getUser().getRealName() : "未知"));
        
        // 检查是否有关联的打卡记录需要删除
        // 注意：LeaveRequest本身没有直接的关联数据需要级联删除
        // 但如果将来有相关的通知、文件等，可以在这里添加删除逻辑
        
        // 删除请假申请记录
        System.out.println("删除请假申请记录...");
        leaveRequestRepository.delete(leaveRequest);
        
        System.out.println("请假申请物理删除完成: " + leaveRequest.getUser().getRealName());
    }

    @Override
    @Transactional
    public int physicalDeleteLeaveRequests(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        List<LeaveRequest> requests = leaveRequestRepository.findAllById(ids)
                .stream()
                .filter(request -> request != null && !Boolean.TRUE.equals(request.getDeleted()))
                .toList();

        if (requests.isEmpty()) {
            return 0;
        }

        leaveRequestRepository.deleteAllInBatch(requests);
        return requests.size();
    }
}
