package com.example.photography.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 被调换人或管理员对排班调换申请进行处理的请求 DTO
 */
public class DutySwapRequestDecisionRequest {

    /**
     * 是否同意（true = 同意，false = 拒绝）
     */
    @NotNull(message = "决策结果不能为空")
    private Boolean approve;

    /**
     * 处理备注
     */
    @Size(max = 500, message = "备注不能超过500个字符")
    private String reason;

    public Boolean getApprove() {
        return approve;
    }

    public void setApprove(Boolean approve) {
        this.approve = approve;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}


