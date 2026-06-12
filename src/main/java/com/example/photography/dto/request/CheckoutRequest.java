package com.example.photography.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 签退请求DTO
 */
@Data
public class CheckoutRequest {
    
    @NotNull(message = "打卡记录ID不能为空")
    private Long recordId;
    
    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0", message = "纬度必须在-90到90之间")
    @DecimalMax(value = "90.0", message = "纬度必须在-90到90之间")
    private Double latitude;
    
    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0", message = "经度必须在-180到180之间")
    @DecimalMax(value = "180.0", message = "经度必须在-180到180之间")
    private Double longitude;
    
    @Size(max = 500, message = "详细地址长度不能超过500字符")
    private String address;
    
    @Size(max = 1000, message = "备注长度不能超过1000字符")
    private String notes;
    
    @Size(max = 50, message = "IP地址长度不能超过50字符")
    private String ipAddress;
    
    @Size(max = 500, message = "用户代理长度不能超过500字符")
    private String userAgent;
}
