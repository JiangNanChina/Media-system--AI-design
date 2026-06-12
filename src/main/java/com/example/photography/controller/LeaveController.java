package com.example.photography.controller;

import com.example.photography.dto.request.LeaveApprovalRequest;
import com.example.photography.dto.request.LeaveRequestRequest;
import com.example.photography.dto.response.ApiResponse;
import com.example.photography.dto.response.CheckinConfigurationResponse;
import com.example.photography.model.entity.LeaveRequest;
import com.example.photography.model.entity.CheckinConfiguration;
import com.example.photography.model.entity.DutySchedule;
import com.example.photography.model.entity.User;
import com.example.photography.repository.UserRepository;
import com.example.photography.service.LeaveService;
import com.example.photography.service.CheckinConfigurationService;
import com.example.photography.service.DutyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请假管理控制器
 */
@RestController
@RequestMapping("/leave-requests")
@Tag(name = "请假管理", description = "请假申请、审批、记录查询等操作")
public class LeaveController {
    
    @Autowired
    private LeaveService leaveService;
    
    @Autowired
    private CheckinConfigurationService checkinConfigurationService;
    
    @Autowired
    private DutyService dutyService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 测试认证状态
     */
    @GetMapping("/test-auth")
    @Operation(summary = "测试用户认证状态", description = "用于调试用户认证问题")
    public ApiResponse<Map<String, Object>> testAuth() {
        System.out.println("=== 测试认证状态 API 被调用 ===");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("认证信息: " + authentication);
            
            Map<String, Object> result = new HashMap<>();
            
            if (authentication == null) {
                result.put("status", "认证信息为null");
                return ApiResponse.success(result);
            }
            
            result.put("name", authentication.getName());
            result.put("principal", authentication.getPrincipal().toString());
            result.put("details", authentication.getDetails());
            result.put("authorities", authentication.getAuthorities().toString());
            result.put("authenticated", authentication.isAuthenticated());
            
            System.out.println("认证测试结果: " + result);
            return ApiResponse.success(result);
            
        } catch (Exception e) {
            System.out.println("认证测试异常: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 提交请假申请
     */
    @PostMapping("/submit")
    @Operation(summary = "提交请假申请", description = "用户提交请假申请")
    public ApiResponse<LeaveRequest> submitLeaveRequest(@Valid @RequestBody LeaveRequestRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            
            // 添加详细日志
            System.out.println("收到请假申请：");
            System.out.println("用户ID: " + userId);
            System.out.println("请假类型: " + request.getLeaveType());
            System.out.println("开始日期: " + request.getStartDate());
            System.out.println("结束日期: " + request.getEndDate());
            System.out.println("请假原因: " + request.getReason());
            System.out.println("联系电话: " + request.getContactPhone());
            System.out.println("执勤排班IDs: " + request.getDutyScheduleIds());
            System.out.println("打卡配置ID: " + request.getCheckinConfigurationId());
            
            LeaveRequest leaveRequest = leaveService.submitLeaveRequest(request, userId);
            return ApiResponse.success("请假申请提交成功", leaveRequest);
        } catch (Exception e) {
            System.err.println("提交请假申请失败: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 审批请假申请
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "审批请假申请", description = "管理员审批请假申请")
    public ApiResponse<LeaveRequest> approveLeaveRequest(@PathVariable Long id, 
                                                       @Valid @RequestBody LeaveApprovalRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long approverId = (Long) authentication.getDetails();
            LeaveRequest leaveRequest = leaveService.approveLeaveRequest(id, request, approverId);
            return ApiResponse.success("审批完成", leaveRequest);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 取消请假申请
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消请假申请", description = "用户取消自己的请假申请")
    public ApiResponse<LeaveRequest> cancelLeaveRequest(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            LeaveRequest leaveRequest = leaveService.cancelLeaveRequest(id, userId);
            return ApiResponse.success("请假申请已取消", leaveRequest);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取请假申请详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取请假申请详情", description = "根据ID获取请假申请详情")
    public ApiResponse<LeaveRequest> getLeaveRequestById(@PathVariable Long id) {
        try {
            LeaveRequest request = leaveService.getLeaveRequestById(id);
            return ApiResponse.success(request);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户的请假申请
     */
    @GetMapping("/my-requests")
    @Operation(summary = "获取我的请假申请", description = "获取当前用户的请假申请")
    public ApiResponse<Page<LeaveRequest>> getMyLeaveRequests(
            @RequestParam(required = false) LeaveRequest.RequestStatus status,
            @RequestParam(required = false) LeaveRequest.LeaveType leaveType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        System.out.println("=== 获取我的请假申请 API 被调用 ===");
        System.out.println("请求参数: status=" + status + ", leaveType=" + leaveType + ", page=" + page + ", size=" + size);
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("认证信息: " + authentication);
            
            if (authentication == null) {
                System.out.println("错误：认证信息为null");
                return ApiResponse.error("用户未认证");
            }
            
            System.out.println("认证用户名: " + authentication.getName());
            System.out.println("认证详情: " + authentication.getDetails());
            System.out.println("认证主体: " + authentication.getPrincipal());
            System.out.println("认证权限: " + authentication.getAuthorities());
            
            Object details = authentication.getDetails();
            if (details == null) {
                System.out.println("错误：认证详情为null，尝试从principal获取用户信息");
                
                // 尝试从principal获取用户名，然后查找用户
                String username = authentication.getName();
                if (username != null && !username.equals("anonymousUser")) {
                    try {
                        User user = userRepository.findByUsernameAndDeletedFalse(username)
                                .orElseThrow(() -> new RuntimeException("用户不存在: " + username));
                        Long userId = user.getId();
                        System.out.println("从用户名获取到用户ID: " + userId);
                        
                        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "applyTime"));
                        Page<LeaveRequest> requests = leaveService.getUserLeaveRequests(userId, status, leaveType, pageable);
                        return ApiResponse.success(requests);
                    } catch (Exception e) {
                        System.out.println("从用户名查找用户失败: " + e.getMessage());
                        return ApiResponse.error("无法获取用户信息: " + e.getMessage());
                    }
                } else {
                    return ApiResponse.error("无法获取用户ID，认证详情为空且用户名无效");
                }
            }
            
            Long userId;
            try {
                userId = (Long) details;
                System.out.println("成功从details获取用户ID: " + userId);
            } catch (ClassCastException e) {
                System.out.println("错误：用户ID类型转换失败，details类型: " + details.getClass().getName() + ", 值: " + details);
                return ApiResponse.error("用户ID格式错误");
            }
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "applyTime"));
            System.out.println("调用服务方法获取请假申请DTO...");
            
            // 调用Service方法获取DTO响应
            Map<String, Object> response = leaveService.getUserLeaveRequestsDTO(userId, status, leaveType, pageable);
            System.out.println("成功获取请假申请DTO");
            
            return (ApiResponse<Page<LeaveRequest>>) (Object) ApiResponse.success(response);
            
        } catch (Exception e) {
            System.out.println("获取我的请假申请发生异常: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有请假申请（管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取所有请假申请", description = "管理员获取所有请假申请")
    public ApiResponse<Page<LeaveRequest>> getAllLeaveRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LeaveRequest.RequestStatus status,
            @RequestParam(required = false) LeaveRequest.LeaveType leaveType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "applyTime"));
            Map<String, Object> dto = leaveService.getAllLeaveRequestsDTO(keyword, status, leaveType, startDate, endDate, pageable);
            return (ApiResponse) ApiResponse.success(dto);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取待审批的请假申请
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取待审批请假申请", description = "管理员获取待审批的请假申请")
    public ApiResponse<List<LeaveRequest>> getPendingLeaveRequests() {
        try {
            List<LeaveRequest> requests = leaveService.getPendingLeaveRequests();
            return ApiResponse.success(requests);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取紧急请假申请
     */
    @GetMapping("/emergency")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取紧急请假申请", description = "管理员获取紧急请假申请")
    public ApiResponse<List<LeaveRequest>> getEmergencyLeaveRequests() {
        try {
            List<LeaveRequest> requests = leaveService.getEmergencyLeaveRequests();
            return ApiResponse.success(requests);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 检查用户在指定日期是否有请假
     */
    @GetMapping("/check")
    @Operation(summary = "检查请假状态", description = "检查用户在指定日期是否有已批准的请假")
    public ApiResponse<Boolean> hasApprovedLeave(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            boolean hasLeave = leaveService.hasApprovedLeave(userId, date);
            return ApiResponse.success("请假状态检查完成", hasLeave);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 上传请假附件
     */
    @PostMapping("/{id}/upload")
    @Operation(summary = "上传请假附件", description = "为请假申请上传附件")
    public ApiResponse<String> uploadLeaveAttachment(@PathVariable Long id, 
                                                   @RequestParam("file") MultipartFile file) {
        try {
            String filePath = leaveService.uploadLeaveAttachment(id, file);
            return ApiResponse.success("附件上传成功", filePath);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 删除请假申请（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除请假申请", description = "管理员删除请假申请")
    public ApiResponse<Void> deleteLeaveRequest(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long adminId = (Long) authentication.getDetails();
            System.out.println("管理员 " + adminId + " 正在删除请假申请: " + id);
            
            leaveService.deleteLeaveRequest(id);
            return ApiResponse.success("请假申请删除成功");
        } catch (Exception e) {
            System.err.println("删除请假申请失败: " + e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取请假统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取请假统计数据", description = "获取用户的请假统计数据")
    public ApiResponse<Map<String, Object>> getLeaveStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            Map<String, Object> statistics = leaveService.getLeaveStatistics(userId, startDate, endDate);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取全局请假统计数据（管理员）
     */
    @GetMapping("/statistics/global")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取全局请假统计", description = "管理员获取全局请假统计数据")
    public ApiResponse<Map<String, Object>> getGlobalLeaveStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Map<String, Object> statistics = leaveService.getGlobalLeaveStatistics(startDate, endDate);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取请假类型统计
     */
    @GetMapping("/statistics/types")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取请假类型统计", description = "管理员获取请假类型统计")
    public ApiResponse<List<Map<String, Object>>> getLeaveTypeStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Map<String, Object>> statistics = leaveService.getLeaveTypeStatistics(startDate, endDate);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取请假趋势统计
     */
    @GetMapping("/statistics/trends")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取请假趋势统计", description = "管理员获取请假趋势统计")
    public ApiResponse<List<Map<String, Object>>> getLeaveTrendStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Map<String, Object>> statistics = leaveService.getLeaveTrendStatistics(startDate, endDate);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 导出请假记录
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "导出请假记录", description = "管理员导出请假记录")
    public ResponseEntity<byte[]> exportLeaveRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LeaveRequest.RequestStatus status,
            @RequestParam(required = false) LeaveRequest.LeaveType leaveType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            byte[] excelData = leaveService.exportLeaveRequests(keyword, status, leaveType, startDate, endDate);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "leave_requests.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取用户的打卡配置
     */
    @GetMapping("/checkin-configurations")
    @Operation(summary = "获取用户打卡配置", description = "获取当前用户可以请假的打卡配置")
    public ApiResponse<List<CheckinConfiguration>> getUserCheckinConfigurations() {
        try {
            List<CheckinConfigurationResponse> configResponses = checkinConfigurationService.getActiveConfigurations();
            // 转换为实体对象（简化处理）
            List<CheckinConfiguration> configurations = configResponses.stream()
                .map(response -> {
                    CheckinConfiguration config = new CheckinConfiguration();
                    config.setId(response.getId());
                    config.setName(response.getName());
                    config.setStartTime(response.getStartTime());
                    config.setEndTime(response.getEndTime());
                    return config;
                })
                .toList();
            return ApiResponse.success(configurations);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户的执勤排班
     */
    @GetMapping("/duty-schedules")
    @Operation(summary = "获取用户执勤排班", description = "获取当前用户的执勤排班")
    public ApiResponse<List<DutySchedule>> getUserDutySchedules() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = (Long) authentication.getDetails();
            List<DutySchedule> schedules = dutyService.getUserDutySchedules(userId);
            return ApiResponse.success(schedules);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 管理员批量清理已处理的请假申请
     */
    @PostMapping("/admin/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量清理已处理申请", description = "管理员批量物理删除已处理的请假申请以节省存储空间")
    public ApiResponse<Map<String, Object>> cleanupProcessedRequests(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate beforeDate,
            @RequestParam(defaultValue = "false") boolean includeApproved,
            @RequestParam(defaultValue = "true") boolean includeRejected,
            @RequestParam(defaultValue = "true") boolean includeCancelled) {
        try {
            int deletedCount = leaveService.cleanupProcessedLeaveRequests(
                beforeDate, includeApproved, includeRejected, includeCancelled);
            
            Map<String, Object> result = Map.of(
                "deletedCount", deletedCount,
                "beforeDate", beforeDate,
                "includeApproved", includeApproved,
                "includeRejected", includeRejected,
                "includeCancelled", includeCancelled
            );
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("清理失败: " + e.getMessage());
        }
    }
    
    /**
     * 物理删除请假申请
     */
    @DeleteMapping("/{id}/physical")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "物理删除请假申请", description = "彻底删除请假申请及相关数据（仅管理员）")
    public ApiResponse<Void> physicalDeleteLeaveRequest(@PathVariable Long id) {
        try {
            leaveService.physicalDeleteLeaveRequest(id);
            return ApiResponse.success("请假申请及相关数据已彻底删除");
        } catch (Exception e) {
            return ApiResponse.error("物理删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量物理删除请假申请
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量物理删除请假申请", description = "管理员批量彻底删除请假申请记录")
    public ApiResponse<Map<String, Object>> batchPhysicalDeleteLeaveRequests(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ApiResponse.error("请选择要删除的请假申请");
            }
            int deletedCount = leaveService.physicalDeleteLeaveRequests(ids);
            Map<String, Object> result = Map.of(
                    "requestedCount", ids.size(),
                    "deletedCount", deletedCount
            );
            return ApiResponse.success("批量删除完成", result);
        } catch (Exception e) {
            return ApiResponse.error("批量删除失败: " + e.getMessage());
        }
    }
}
