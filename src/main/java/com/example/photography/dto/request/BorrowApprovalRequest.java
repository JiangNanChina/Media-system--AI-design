package com.example.photography.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 借用审批请求DTO
 */
public class BorrowApprovalRequest {
    
    @NotNull(message = "审批结果不能为空")
    private Boolean approved; // true-批准，false-拒绝
    
    private String approvalNotes; // 审批备注
    
    // Constructors
    public BorrowApprovalRequest() {}
    
    public BorrowApprovalRequest(Boolean approved, String approvalNotes) {
        this.approved = approved;
        this.approvalNotes = approvalNotes;
    }
    
    // Getters and Setters
    public Boolean getApproved() {
        return approved;
    }
    
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
    
    public String getApprovalNotes() {
        return approvalNotes;
    }
    
    public void setApprovalNotes(String approvalNotes) {
        this.approvalNotes = approvalNotes;
    }
}
