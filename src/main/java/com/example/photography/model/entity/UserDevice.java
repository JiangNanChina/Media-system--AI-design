package com.example.photography.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户设备绑定实体
 * 用于防止多设备登录作弊
 */
@Entity
@Table(name = "user_devices")
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDevice extends BaseEntity {
    
    /**
     * 关联用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * 设备指纹（唯一标识）
     */
    @Column(name = "device_fingerprint", nullable = false, length = 500)
    private String deviceFingerprint;
    
    /**
     * 设备名称/型号
     */
    @Column(name = "device_name", length = 200)
    private String deviceName;
    
    /**
     * 设备类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false)
    private DeviceType deviceType;
    
    /**
     * 操作系统信息
     */
    @Column(name = "os_info", length = 200)
    private String osInfo;
    
    /**
     * 浏览器信息
     */
    @Column(name = "browser_info", length = 200)
    private String browserInfo;
    
    /**
     * 屏幕分辨率
     */
    @Column(name = "screen_resolution", length = 50)
    private String screenResolution;
    
    /**
     * 时区
     */
    @Column(name = "timezone", length = 50)
    private String timezone;
    
    /**
     * 语言设置
     */
    @Column(name = "language", length = 50)
    private String language;
    
    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    /**
     * 是否激活
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    /**
     * 首次绑定时间
     */
    @Column(name = "first_bound_at", nullable = false)
    private LocalDateTime firstBoundAt;
    
    /**
     * 最后活跃时间
     */
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;
    
    /**
     * 绑定状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "bind_status", nullable = false)
    private BindStatus bindStatus = BindStatus.ACTIVE;
    
    /**
     * 设备类型枚举
     */
    public enum DeviceType {
        MOBILE("移动设备"),
        TABLET("平板设备"),
        DESKTOP("桌面设备"),
        UNKNOWN("未知设备");
        
        private final String description;
        
        DeviceType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 绑定状态枚举
     */
    public enum BindStatus {
        ACTIVE("激活"),
        SUSPENDED("暂停"),
        REVOKED("撤销");
        
        private final String description;
        
        BindStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
