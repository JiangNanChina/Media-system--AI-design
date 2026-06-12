package com.example.photography.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 打卡配置实体
 * 包含完整的打卡地点和时间信息
 */
@Entity
@Table(name = "checkin_configurations")
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CheckinConfiguration extends BaseEntity {
    
    /**
     * 配置名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 配置描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    // ========== 地点信息 ==========
    
    /**
     * 地点名称
     */
    @Column(name = "location_name", nullable = false, length = 100)
    private String locationName;
    
    /**
     * 地点地址
     */
    @Column(name = "location_address", length = 200)
    private String locationAddress;
    
    /**
     * 地点描述
     */
    @Column(name = "location_description", length = 500)
    private String locationDescription;
    
    /**
     * 经度
     */
    @Column(name = "longitude")
    private Double longitude;
    
    /**
     * 纬度
     */
    @Column(name = "latitude")
    private Double latitude;
    
    // ========== 时间信息 ==========
    
    /**
     * 时段名称
     */
    @Column(name = "session_name", nullable = false, length = 100)
    private String sessionName;
    
    /**
     * 开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    /**
     * 结束时间
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    /**
     * 时段描述
     */
    @Column(name = "session_description", length = 500)
    private String sessionDescription;

    /**
     * 需要打卡的星期（ISO 值，1=周一，7=周日）
     */
    @Column(name = "required_weekdays", length = 20)
    private String requiredWeekdays = "1,2,3,4";
    
    // ========== 配置选项 ==========
    
    /**
     * 是否启用
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    /**
     * 排序序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    /**
     * 是否允许提前打卡（分钟）
     */
    @Column(name = "early_checkin_minutes")
    private Integer earlyCheckinMinutes = 0;
    
    /**
     * 是否允许延迟打卡（分钟）
     */
    @Column(name = "late_checkin_minutes")
    private Integer lateCheckinMinutes = 0;
    
    /**
     * 二维码内容（用于二维码签到）
     */
    @Column(name = "qr_code", length = 1000)
    private String qrCode;
    
    /**
     * WiFi SSID（用于WiFi签到）
     */
    @Column(name = "wifi_ssid", length = 100)
    private String wifiSsid;
    
    /**
     * 配置创建者
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    /**
     * 需要打卡的用户列表（多对多关系）
     */
    @JsonIgnore // 避免序列化时的懒加载问题
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "checkin_config_users",
        joinColumns = @JoinColumn(name = "config_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> requiredUsers = new HashSet<>();
}
