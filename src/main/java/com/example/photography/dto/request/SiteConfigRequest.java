package com.example.photography.dto.request;

import com.example.photography.model.entity.SiteConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 站点配置请求DTO
 */
@Data
public class SiteConfigRequest {
    
    /**
     * 配置键名
     */
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置描述
     */
    private String description;
    
    /**
     * 配置类型
     */
    @NotNull(message = "配置类型不能为空")
    private SiteConfig.ConfigType configType;
    
    /**
     * 是否启用
     */
    private Boolean enabled = true;
    
    /**
     * 排序权重
     */
    private Integer sortOrder = 0;
}
