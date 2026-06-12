package com.example.photography.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 打卡记录实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "checkin_records")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CheckinRecord extends BaseEntity {
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 打卡用户
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuration_id", nullable = false)
    private CheckinConfiguration configuration; // 打卡配置
    
    @Column(name = "checkin_time", nullable = false)
    private LocalDateTime checkinTime; // 签到时间
    
    @Column(name = "checkout_time")
    private LocalDateTime checkoutTime; // 签退时间
    
    @Column(name = "checkin_latitude", columnDefinition = "DECIMAL(10,7)")
    private Double checkinLatitude; // 签到纬度
    
    @Column(name = "checkin_longitude", columnDefinition = "DECIMAL(11,7)")
    private Double checkinLongitude; // 签到经度
    
    @Column(name = "checkout_latitude", columnDefinition = "DECIMAL(10,7)")
    private Double checkoutLatitude; // 签退纬度
    
    @Column(name = "checkout_longitude", columnDefinition = "DECIMAL(11,7)")
    private Double checkoutLongitude; // 签退经度
    
    @Column(name = "checkin_address", length = 500)
    private String checkinAddress; // 签到详细地址
    
    @Column(name = "checkout_address", length = 500)
    private String checkoutAddress; // 签退详细地址
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CheckinStatus status = CheckinStatus.NORMAL; // 打卡状态
    
    @Column(name = "is_late", nullable = false)
    private Boolean isLate = false; // 是否迟到
    
    @Column(name = "late_minutes")
    private Integer lateMinutes = 0; // 迟到分钟数
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes; // 持续时间（分钟）
    
    @Column(name = "checkin_method", length = 20)
    private String checkinMethod = "GPS"; // 打卡方式：GPS, QR_CODE, WIFI
    
    @Column(name = "device_info", length = 1000)
    private String deviceInfo; // 设备信息
    
    @Column(name = "checkin_photo", length = 500)
    private String checkinPhoto; // 签到照片
    
    @Column(name = "checkout_photo", length = 500)
    private String checkoutPhoto; // 签退照片
    
    @Column(name = "notes", length = 1000)
    private String notes; // 备注
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress; // IP地址
    
    @Column(name = "user_agent", length = 500)
    private String userAgent; // 用户代理
    
    @Enumerated(EnumType.STRING)
    @Column(name = "audit_status", length = 20)
    private AuditStatus auditStatus = AuditStatus.NOT_REQUIRED; // 审核状态
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audited_by")
    private User auditedBy; // 审核人
    
    @Column(name = "audit_time")
    private LocalDateTime auditTime; // 审核时间
    
    @Column(name = "audit_notes", length = 500)
    private String auditNotes; // 审核备注
    
    /**
     * 审核状态枚举
     */
    public enum AuditStatus {
        NOT_REQUIRED("无需审核"),    // GPS、二维码等自动验证方式
        PENDING("待审核"),           // 管理员审核方式提交后的初始状态
        APPROVED("已通过"),          // 管理员审核通过
        REJECTED("已拒绝");          // 管理员审核拒绝（标记为缺勤）
        
        private final String description;
        
        AuditStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 打卡状态枚举
     */
    public enum CheckinStatus {
        NORMAL("正常"),
        LATE("迟到"),
        EARLY_LEAVE("早退"),
        ABSENT("缺席"),
        MAKEUP("补签"),
        LEAVE("请假");
        
        private final String description;
        
        CheckinStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // ========== 前端显示字段 ==========
    // 注意：这些方法标记为 @JsonIgnore，避免序列化时触发懒加载
    // 如需在 JSON 中包含这些字段，请使用 DTO 转换
    
    /**
     * 获取地点名称（前端显示用）
     */
    @JsonIgnore
    public String getLocationName() {
        return configuration != null ? configuration.getLocationName() : null;
    }
    
    /**
     * 获取时段名称（前端显示用）
     */
    @JsonIgnore
    public String getSessionName() {
        return configuration != null ? configuration.getSessionName() : null;
    }
    
    /**
     * 获取用户姓名（前端显示用）
     */
    @JsonIgnore
    public String getUserName() {
        return user != null ? user.getRealName() : null;
    }
    
    /**
     * 获取用户部门名称（前端显示用）
     */
    @JsonIgnore
    public String getDepartmentName() {
        return user != null && user.getDepartment() != null ? user.getDepartment().getName() : null;
    }
}
