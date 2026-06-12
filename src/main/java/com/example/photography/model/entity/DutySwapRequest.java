package com.example.photography.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 办公室执勤排班调换申请实体
 */
@Entity
@Table(name = "duty_swap_requests")
public class DutySwapRequest extends BaseEntity {

    /**
     * 发起调换申请的用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    @NotNull(message = "发起人不能为空")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password",
            "borrowRecords", "dutyRecords", "leaveRequests"})
    private User requester;

    /**
     * 被申请调换的用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    @NotNull(message = "被调换人不能为空")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password",
            "borrowRecords", "dutyRecords", "leaveRequests"})
    private User targetUser;

    /**
     * 发起人的排班
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_schedule_id", nullable = false)
    @NotNull(message = "发起人排班不能为空")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DutySchedule requesterSchedule;

    /**
     * 被调换人的排班
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_schedule_id", nullable = false)
    @NotNull(message = "被调换人排班不能为空")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DutySchedule targetSchedule;

    /**
     * 申请状态：PENDING(待处理)、APPROVED(已同意)、REJECTED(已拒绝)、CANCELLED(已撤销)
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "PENDING";

    /**
     * 申请备注
     */
    @Column(name = "reason", length = 500)
    private String reason;

    /**
     * 被调换人回复备注
     */
    @Column(name = "response_reason", length = 500)
    private String responseReason;

    /**
     * 本次调换对应的具体日期（仅在该日期生效，不改变长期排班配置）
     */
    @Column(name = "swap_date")
    private LocalDate swapDate;

    // Getters and Setters

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public DutySchedule getRequesterSchedule() {
        return requesterSchedule;
    }

    public void setRequesterSchedule(DutySchedule requesterSchedule) {
        this.requesterSchedule = requesterSchedule;
    }

    public DutySchedule getTargetSchedule() {
        return targetSchedule;
    }

    public void setTargetSchedule(DutySchedule targetSchedule) {
        this.targetSchedule = targetSchedule;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getResponseReason() {
        return responseReason;
    }

    public void setResponseReason(String responseReason) {
        this.responseReason = responseReason;
    }

    public LocalDate getSwapDate() {
        return swapDate;
    }

    public void setSwapDate(LocalDate swapDate) {
        this.swapDate = swapDate;
    }
}

