package com.example.photography.dto.request;

import com.example.photography.model.entity.LeaveRequest;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * 请假申请请求DTO
 */
@Data
public class LeaveRequestRequest {
    
    @NotNull(message = "请假类型不能为空")
    private LeaveRequest.LeaveType leaveType;
    
    @NotNull(message = "请假开始日期不能为空")
    private LocalDate startDate;
    
    @NotNull(message = "请假结束日期不能为空")
    private LocalDate endDate;
    
    @NotBlank(message = "请假原因不能为空")
    @Size(max = 1000, message = "请假原因长度不能超过1000字符")
    private String reason;
    
    @Size(max = 500, message = "附件URL长度不能超过500字符")
    private String attachment;
    
    private Boolean emergency = false;
    
    // 临时放宽联系电话验证规则
    @Size(min = 1, max = 20, message = "联系电话长度应在1-20位之间")
    private String contactPhone;
    
    @Size(max = 100, message = "紧急联系人姓名长度不能超过100字符")
    private String contactPerson;
    
    // 执勤请假相关字段
    private java.util.List<Long> dutyScheduleIds;
    
    // 打卡请假相关字段
    private Long checkinConfigurationId;
}