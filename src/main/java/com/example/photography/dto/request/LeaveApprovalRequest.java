package com.example.photography.dto.request;

import com.example.photography.model.entity.LeaveRequest;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 请假审批请求DTO
 */
@Data
public class LeaveApprovalRequest {
    
    @NotNull(message = "审批状态不能为空")
    private LeaveRequest.RequestStatus status;
    
    @Size(max = 1000, message = "审批备注长度不能超过1000字符")
    private String approveNotes;
}