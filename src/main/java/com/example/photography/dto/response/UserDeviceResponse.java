package com.example.photography.dto.response;

import com.example.photography.model.entity.UserDevice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户设备响应DTO
 */
@Data
@Schema(description = "用户设备响应")
public class UserDeviceResponse {
    
    @Schema(description = "设备ID")
    private Long id;
    
    @Schema(description = "用户信息")
    private UserBasicResponse user;
    
    @Schema(description = "设备指纹")
    private String deviceFingerprint;
    
    @Schema(description = "设备名称")
    private String deviceName;
    
    @Schema(description = "设备类型")
    private String deviceType;
    
    @Schema(description = "设备类型描述")
    private String deviceTypeDescription;
    
    @Schema(description = "操作系统信息")
    private String osInfo;
    
    @Schema(description = "浏览器信息")
    private String browserInfo;
    
    @Schema(description = "屏幕分辨率")
    private String screenResolution;
    
    @Schema(description = "时区")
    private String timezone;
    
    @Schema(description = "语言设置")
    private String language;
    
    @Schema(description = "IP地址")
    private String ipAddress;
    
    @Schema(description = "是否激活")
    private Boolean isActive;
    
    @Schema(description = "绑定状态")
    private String bindStatus;
    
    @Schema(description = "绑定状态描述")
    private String bindStatusDescription;
    
    @Schema(description = "首次绑定时间")
    private LocalDateTime firstBoundAt;
    
    @Schema(description = "最后活跃时间")
    private LocalDateTime lastActiveAt;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    
    /**
     * 从实体转换为响应DTO
     */
    public static UserDeviceResponse fromEntity(UserDevice device) {
        if (device == null) {
            return null;
        }
        
        UserDeviceResponse response = new UserDeviceResponse();
        response.setId(device.getId());
        
        // 用户信息
        if (device.getUser() != null) {
            response.setUser(UserBasicResponse.fromUser(device.getUser()));
        }
        
        response.setDeviceFingerprint(maskFingerprint(device.getDeviceFingerprint()));
        response.setDeviceName(device.getDeviceName());
        response.setDeviceType(device.getDeviceType() != null ? device.getDeviceType().name() : null);
        response.setDeviceTypeDescription(device.getDeviceType() != null ? device.getDeviceType().getDescription() : null);
        response.setOsInfo(device.getOsInfo());
        response.setBrowserInfo(device.getBrowserInfo());
        response.setScreenResolution(device.getScreenResolution());
        response.setTimezone(device.getTimezone());
        response.setLanguage(device.getLanguage());
        response.setIpAddress(maskIpAddress(device.getIpAddress()));
        response.setIsActive(device.getIsActive());
        response.setBindStatus(device.getBindStatus() != null ? device.getBindStatus().name() : null);
        response.setBindStatusDescription(device.getBindStatus() != null ? device.getBindStatus().getDescription() : null);
        response.setFirstBoundAt(device.getFirstBoundAt());
        response.setLastActiveAt(device.getLastActiveAt());
        response.setCreatedAt(device.getCreatedAt());
        response.setUpdatedAt(device.getUpdatedAt());
        
        return response;
    }
    
    /**
     * 掩码设备指纹（隐私保护）
     */
    private static String maskFingerprint(String fingerprint) {
        if (fingerprint == null || fingerprint.length() <= 8) {
            return fingerprint;
        }
        return fingerprint.substring(0, 4) + "****" + fingerprint.substring(fingerprint.length() - 4);
    }
    
    /**
     * 掩码IP地址（隐私保护）
     */
    private static String maskIpAddress(String ipAddress) {
        if (ipAddress == null) {
            return null;
        }
        
        // IPv4掩码
        if (ipAddress.contains(".")) {
            String[] parts = ipAddress.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".***." + parts[3];
            }
        }
        
        // IPv6或其他格式的简单掩码
        if (ipAddress.length() > 8) {
            return ipAddress.substring(0, 4) + "****" + ipAddress.substring(ipAddress.length() - 4);
        }
        
        return ipAddress;
    }
}
