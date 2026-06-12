package com.example.photography.dto.request;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * 签到请求DTO
 * 
 * 位置信息说明：
 * - 二维码签到：不需要位置信息（可选，仅用于记录）
 * - 管理员审核签到：需要提供位置信息（用于记录和审核参考）
 * 
 * 注：GPS定位签到功能已移除
 */
@Data
public class CheckinRequest {
    
    @NotNull(message = "配置ID不能为空")
    private Long configurationId;
    
    // 位置信息：可选，用于记录签到位置（不再用于验证）
    @DecimalMin(value = "-90.0", message = "纬度必须在-90到90之间")
    @DecimalMax(value = "90.0", message = "纬度必须在-90到90之间")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "经度必须在-180到180之间")
    @DecimalMax(value = "180.0", message = "经度必须在-180到180之间")
    private Double longitude;
    
    @Size(max = 500, message = "详细地址长度不能超过500字符")
    private String address;
    
    @Size(max = 20, message = "打卡方式长度不能超过20字符")
    private String checkinMethod = "QR_CODE"; // QR_CODE, MANUAL_AUDIT（GPS已移除）
    
    @Valid
    private DeviceInfoRequest deviceInfo;
    
    @Size(max = 1000, message = "备注长度不能超过1000字符")
    private String notes;
    
    @Size(max = 50, message = "IP地址长度不能超过50字符")
    private String ipAddress;
    
    @Size(max = 500, message = "用户代理长度不能超过500字符")
    private String userAgent;
    
    // 二维码打卡时使用
    private String qrCode;
    
    // WiFi字段已废弃（不再使用）
    private String wifiSsid;
    private String wifiMac;
}
