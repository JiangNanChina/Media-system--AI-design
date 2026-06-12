package com.example.photography.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 位置验证请求DTO
 */
@Data
@Schema(description = "位置验证请求")
public class LocationValidationRequest {
    
    @NotNull(message = "位置ID不能为空")
    @Schema(description = "位置ID")
    private Long locationId;
    
    @NotNull(message = "纬度不能为空")
    @Schema(description = "当前纬度")
    private Double latitude;
    
    @NotNull(message = "经度不能为空")
    @Schema(description = "当前经度")
    private Double longitude;
}
