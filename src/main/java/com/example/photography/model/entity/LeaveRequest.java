package com.example.photography.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "leave_requests")
public class LeaveRequest extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user; // 申请用户
    
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    private LeaveType leaveType; // 请假类型
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // 请假开始日期
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // 请假结束日期
    
    @Column(name = "reason", nullable = false, length = 1000)
    private String reason; // 请假原因
    
    @Column(name = "attachment_urls", length = 2000)
    private String attachment; // 附件URL列表（JSON格式）
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING; // 审批状态
    
    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime; // 申请时间
    
    @Column(name = "approve_time")
    private LocalDateTime approveTime; // 审批时间
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User approver; // 审批人
    
    @Column(name = "approve_notes", length = 1000)
    private String approveNotes; // 审批备注
    
    @Column(name = "days_count")
    private Integer daysCount; // 请假天数
    
    @Column(name = "emergency", nullable = false)
    private Boolean emergency = false; // 是否紧急
    
    @Column(name = "contact_phone", length = 20)
    private String contactPhone; // 联系电话
    
    @Column(name = "contact_person", length = 100)
    private String contactPerson; // 紧急联系人

    @Column(name = "checkin_configuration_id")
    private Long checkinConfigurationId; // 打卡请假对应的打卡配置

    @Column(name = "duty_schedule_ids", length = 1000)
    private String dutyScheduleIds; // 执勤请假对应的排班ID，逗号分隔
    
    /**
     * 请假类型枚举
     */
    public enum LeaveType {
        DUTY_LEAVE("执勤请假"),
        CHECKIN_LEAVE("打卡请假"),
        OTHER("其他");
        
        private final String description;
        
        LeaveType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 审批状态枚举
     */
    public enum RequestStatus {
        PENDING("待审批"),
        APPROVED("已批准"),
        REJECTED("已拒绝"),
        CANCELLED("已取消");
        
        private final String description;
        
        RequestStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
