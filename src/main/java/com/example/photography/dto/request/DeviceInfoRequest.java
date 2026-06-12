package com.example.photography.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 设备信息请求DTO
 */
@Data
@Schema(description = "设备信息请求")
public class DeviceInfoRequest {
    
    @NotBlank(message = "设备指纹不能为空")
    @Size(max = 500, message = "设备指纹长度不能超过500字符")
    @Schema(description = "设备指纹（唯一标识）", required = true)
    private String deviceFingerprint;
    
    @Size(max = 200, message = "设备名称长度不能超过200字符")
    @Schema(description = "设备名称")
    private String deviceName;
    
    @Schema(description = "设备类型", allowableValues = {"MOBILE", "TABLET", "DESKTOP", "UNKNOWN"})
    private String deviceType;
    
    @Size(max = 200, message = "操作系统信息长度不能超过200字符")
    @Schema(description = "操作系统信息")
    private String osInfo;
    
    @Size(max = 200, message = "浏览器信息长度不能超过200字符")
    @Schema(description = "浏览器信息")
    private String browserInfo;
    
    @Size(max = 50, message = "屏幕分辨率长度不能超过50字符")
    @Schema(description = "屏幕分辨率")
    private String screenResolution;
    
    @Size(max = 50, message = "时区长度不能超过50字符")
    @Schema(description = "时区")
    private String timezone;
    
    @Size(max = 50, message = "语言设置长度不能超过50字符")
    @Schema(description = "语言设置")
    private String language;
}
