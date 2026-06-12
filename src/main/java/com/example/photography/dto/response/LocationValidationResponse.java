package com.example.photography.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 位置验证响应DTO
 */
@Data
@Schema(description = "位置验证响应")
public class LocationValidationResponse {
    
    @Schema(description = "验证是否通过")
    private boolean valid;
    
    @Schema(description = "验证信息")
    private String message;
    
    @Schema(description = "距离目标位置的距离（米）")
    private Double distance;
    
    @Schema(description = "允许的最大距离（米）")
    private Integer allowedRadius;
    
    public LocationValidationResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
    
    public LocationValidationResponse(boolean valid, String message, Double distance, Integer allowedRadius) {
        this.valid = valid;
        this.message = message;
        this.distance = distance;
        this.allowedRadius = allowedRadius;
    }
}
