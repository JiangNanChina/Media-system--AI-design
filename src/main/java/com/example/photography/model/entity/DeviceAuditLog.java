package com.example.photography.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 设备审计日志实体
 * 记录所有设备绑定、停用、激活等操作的历史记录
 */
@Entity
@Table(name = "device_audit_logs")
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceAuditLog extends BaseEntity {
    
    /**
     * 用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * 设备ID（可能为null，如创建新设备时）
     */
    @Column(name = "device_id")
    private Long deviceId;
    
    /**
     * 设备指纹
     */
    @Column(name = "device_fingerprint", nullable = false)
    private String deviceFingerprint;
    
    /**
     * 设备名称
     */
    @Column(name = "device_name")
    private String deviceName;
    
    /**
     * 设备类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private UserDevice.DeviceType deviceType;
    
    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;
    
    /**
     * 操作描述
     */
    @Column(name = "action_description", length = 500)
    private String actionDescription;
    
    /**
     * IP地址
     */
    @Column(name = "ip_address")
    private String ipAddress;
    
    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 1000)
    private String userAgent;
    
    /**
     * 操作时间
     */
    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;
    
    /**
     * 操作类型枚举
     */
    public enum ActionType {
        DEVICE_CREATED("设备创建"),
        DEVICE_ACTIVATED("设备激活"),
        DEVICE_DEACTIVATED("设备停用"),
        DEVICE_REACTIVATED("设备重新激活"),
        DEVICE_DELETED("设备删除"),
        LOGIN_SUCCESS("登录成功"),
        LOGIN_FAILED("登录失败"),
        SUSPICIOUS_ACTIVITY("可疑活动");
        
        private final String description;
        
        ActionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
