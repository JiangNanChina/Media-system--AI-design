package com.example.photography.dto.response;

import com.example.photography.model.entity.CheckinConfiguration;
import com.example.photography.utils.CheckinWeekdayUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 打卡配置响应DTO
 */
@Data
@Schema(description = "打卡配置响应")
public class CheckinConfigurationResponse {
    
    @Schema(description = "配置ID")
    private Long id;
    
    @Schema(description = "配置名称")
    private String name;
    
    @Schema(description = "配置描述")
    private String description;
    
    // ========== 地点信息 ==========
    
    @Schema(description = "地点名称")
    private String locationName;
    
    @Schema(description = "地点地址")
    private String locationAddress;
    
    @Schema(description = "地点描述")
    private String locationDescription;
    
    @Schema(description = "经度")
    private Double longitude;
    
    @Schema(description = "纬度")
    private Double latitude;
    
    // ========== 时间信息 ==========
    
    @Schema(description = "时段名称")
    private String sessionName;
    
    @Schema(description = "开始时间")
    private LocalTime startTime;
    
    @Schema(description = "结束时间")
    private LocalTime endTime;
    
    @Schema(description = "时段描述")
    private String sessionDescription;

    @Schema(description = "需要晚自习打卡的星期，1=周一，7=周日")
    private List<Integer> requiredWeekdays;
    
    // ========== 配置选项 ==========
    
    @Schema(description = "是否启用")
    private Boolean isActive;
    
    @Schema(description = "排序序号")
    private Integer sortOrder;
    
    @Schema(description = "允许提前打卡分钟数")
    private Integer earlyCheckinMinutes;
    
    @Schema(description = "允许延迟打卡分钟数")
    private Integer lateCheckinMinutes;
    
    @Schema(description = "二维码内容")
    private String qrCode;
    
    @Schema(description = "WiFi SSID")
    private String wifiSsid;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    
    @Schema(description = "创建者信息")
    private UserBasicResponse createdBy;
    
    // ========== 考勤人员 ==========
    
    @Schema(description = "需要打卡的用户列表")
    private List<UserBasicResponse> requiredUsers;
    
    /**
     * 从实体转换为响应DTO
     */
    public static CheckinConfigurationResponse fromEntity(CheckinConfiguration configuration) {
        if (configuration == null) {
            return null;
        }
        
        CheckinConfigurationResponse response = new CheckinConfigurationResponse();
        response.setId(configuration.getId());
        response.setName(configuration.getName());
        response.setDescription(configuration.getDescription());
        
        // 地点信息
        response.setLocationName(configuration.getLocationName());
        response.setLocationAddress(configuration.getLocationAddress());
        response.setLocationDescription(configuration.getLocationDescription());
        response.setLongitude(configuration.getLongitude());
        response.setLatitude(configuration.getLatitude());
        
        // 时间信息
        response.setSessionName(configuration.getSessionName());
        response.setStartTime(configuration.getStartTime());
        response.setEndTime(configuration.getEndTime());
        response.setSessionDescription(configuration.getSessionDescription());
        response.setRequiredWeekdays(CheckinWeekdayUtils.parseRequiredWeekdays(configuration.getRequiredWeekdays()));
        
        // 配置选项
        response.setIsActive(configuration.getIsActive());
        response.setSortOrder(configuration.getSortOrder());
        response.setEarlyCheckinMinutes(configuration.getEarlyCheckinMinutes());
        response.setLateCheckinMinutes(configuration.getLateCheckinMinutes());
        response.setQrCode(configuration.getQrCode());
        response.setWifiSsid(configuration.getWifiSsid());
        response.setCreatedAt(configuration.getCreatedAt());
        response.setUpdatedAt(configuration.getUpdatedAt());
        
        // 转换创建者信息（安全处理懒加载）
        try {
            if (configuration.getCreatedBy() != null) {
                response.setCreatedBy(UserBasicResponse.fromUser(configuration.getCreatedBy()));
            }
        } catch (Exception e) {
            // 忽略 createdBy 加载失败
            response.setCreatedBy(null);
        }
        
        // 转换需要打卡的用户列表（UserBasicResponse.fromUser 已处理懒加载异常）
        if (configuration.getRequiredUsers() != null) {
            response.setRequiredUsers(
                configuration.getRequiredUsers().stream()
                    .map(UserBasicResponse::fromUser)
                    .filter(user -> user != null)  // 过滤掉可能的 null 值
                    .collect(Collectors.toList())
            );
        }
        
        return response;
    }
}
