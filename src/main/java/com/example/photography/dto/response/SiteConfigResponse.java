package com.example.photography.dto.response;

import com.example.photography.model.entity.SiteConfig;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站点配置响应DTO
 */
@Data
public class SiteConfigResponse {
    
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private SiteConfig.ConfigType configType;
    private String configTypeDescription;
    private Boolean enabled;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 从实体类转换
     */
    public static SiteConfigResponse fromEntity(SiteConfig siteConfig) {
        if (siteConfig == null) {
            return null;
        }
        
        SiteConfigResponse response = new SiteConfigResponse();
        response.setId(siteConfig.getId());
        response.setConfigKey(siteConfig.getConfigKey());
        response.setConfigValue(maskSensitiveValue(siteConfig));
        response.setDescription(siteConfig.getDescription());
        response.setConfigType(siteConfig.getConfigType());
        response.setConfigTypeDescription(siteConfig.getConfigType().getDescription());
        response.setEnabled(siteConfig.getEnabled());
        response.setSortOrder(siteConfig.getSortOrder());
        response.setCreatedAt(siteConfig.getCreatedAt());
        response.setUpdatedAt(siteConfig.getUpdatedAt());
        
        return response;
    }

    private static String maskSensitiveValue(SiteConfig siteConfig) {
        if (SiteConfig.Keys.MAIL_QQ_AUTH_CODE.equals(siteConfig.getConfigKey())) {
            String value = siteConfig.getConfigValue();
            return value == null || value.isBlank() ? "" : "******";
        }
        return siteConfig.getConfigValue();
    }
}
