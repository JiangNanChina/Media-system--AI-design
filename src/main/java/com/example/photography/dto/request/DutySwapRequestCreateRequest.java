package com.example.photography.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 发起排班调换申请请求 DTO
 */
public class DutySwapRequestCreateRequest {

    /**
     * 当前登录用户自己的排班ID
     */
    @NotNull(message = "发起人排班ID不能为空")
    private Long requesterScheduleId;

    /**
     * 想要调换的对方排班ID
     */
    @NotNull(message = "被调换人排班ID不能为空")
    private Long targetScheduleId;

    /**
     * 申请原因
     */
    @Size(max = 500, message = "申请原因不能超过500个字符")
    private String reason;

    /**
     * 计划调换的具体日期（只在该日期调换，不影响其他日期的排班）
     */
    @NotNull(message = "调换日期不能为空")
    private LocalDate swapDate;

    public Long getRequesterScheduleId() {
        return requesterScheduleId;
    }

    public void setRequesterScheduleId(Long requesterScheduleId) {
        this.requesterScheduleId = requesterScheduleId;
    }

    public Long getTargetScheduleId() {
        return targetScheduleId;
    }

    public void setTargetScheduleId(Long targetScheduleId) {
        this.targetScheduleId = targetScheduleId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getSwapDate() {
        return swapDate;
    }

    public void setSwapDate(LocalDate swapDate) {
        this.swapDate = swapDate;
    }
}


