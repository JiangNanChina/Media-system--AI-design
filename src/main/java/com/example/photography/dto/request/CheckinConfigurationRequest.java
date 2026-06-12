package com.example.photography.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

/**
 * 打卡配置请求DTO
 */
@Data
@Schema(description = "打卡配置请求")
public class CheckinConfigurationRequest {
    
    @NotBlank(message = "配置名称不能为空")
    @Size(min = 2, max = 100, message = "配置名称长度必须在2-100字符之间")
    @Schema(description = "配置名称", example = "图书馆晚自习")
    private String name;
    
    @Size(max = 500, message = "配置描述长度不能超过500字符")
    @Schema(description = "配置描述", example = "图书馆晚自习时间的打卡配置")
    private String description;
    
    // ========== 地点信息 ==========
    
    @NotBlank(message = "地点名称不能为空")
    @Size(min = 2, max = 100, message = "地点名称长度必须在2-100字符之间")
    @Schema(description = "地点名称", example = "图书馆自习室")
    private String locationName;
    
    @Size(max = 200, message = "地点地址长度不能超过200字符")
    @Schema(description = "地点地址", example = "图书馆3楼东侧")
    private String locationAddress;
    
    @Size(max = 500, message = "地点描述长度不能超过500字符")
    @Schema(description = "地点描述", example = "安静的学习环境")
    private String locationDescription;
    
    @Deprecated
    @Schema(description = "经度（已废弃，GPS定位功能已移除）", example = "116.407395")
    private Double longitude;
    
    @Deprecated
    @Schema(description = "纬度（已废弃，GPS定位功能已移除）", example = "39.904211")
    private Double latitude;
    
    // ========== 时间信息 ==========
    
    @NotBlank(message = "时段名称不能为空")
    @Size(min = 2, max = 100, message = "时段名称长度必须在2-100字符之间")
    @Schema(description = "时段名称", example = "晚自习")
    private String sessionName;
    
    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", example = "19:00")
    private LocalTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", example = "21:30")
    private LocalTime endTime;
    
    @Size(max = 500, message = "时段描述长度不能超过500字符")
    @Schema(description = "时段描述", example = "晚上自习时间")
    private String sessionDescription;

    @Schema(description = "需要晚自习打卡的星期，1=周一，7=周日", example = "[1, 2, 3, 4]")
    private List<Integer> requiredWeekdays;
    
    // ========== 配置选项 ==========
    
    @Schema(description = "是否启用", example = "true")
    private Boolean isActive = true;
    
    @Schema(description = "排序序号", example = "0")
    private Integer sortOrder = 0;
    
    @Schema(description = "允许提前打卡分钟数", example = "5")
    private Integer earlyCheckinMinutes = 0;
    
    @Schema(description = "允许延迟打卡分钟数", example = "10")
    private Integer lateCheckinMinutes = 0;
    
    @Size(max = 1000, message = "二维码内容长度不能超过1000字符")
    @Schema(description = "二维码内容", example = "{\"configId\":1,\"type\":\"CHECKIN\"}")
    private String qrCode;
    
    @Size(max = 100, message = "WiFi SSID长度不能超过100字符")
    @Schema(description = "WiFi SSID", example = "CompanyWifi")
    private String wifiSsid;
    
    // ========== 考勤人员 ==========
    
    @Schema(description = "需要打卡的用户ID列表", example = "[1, 2, 3]")
    private List<Long> requiredUserIds;
}
