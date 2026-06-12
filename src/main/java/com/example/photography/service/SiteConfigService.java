package com.example.photography.service;

import com.example.photography.dto.request.SiteConfigRequest;
import com.example.photography.model.entity.SiteConfig;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 站点配置Service接口
 */
public interface SiteConfigService {
    
    /**
     * 获取所有配置
     */
    List<SiteConfig> getAllConfigs();
    
    /**
     * 获取所有启用的配置
     */
    List<SiteConfig> getAllEnabledConfigs();
    
    /**
     * 根据配置键获取配置
     */
    SiteConfig getConfigByKey(String configKey);
    
    /**
     * 根据配置键获取配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 获取配置值，如果不存在返回默认值
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 批量获取配置值
     */
    Map<String, String> getConfigValues(List<String> configKeys);
    
    /**
     * 创建或更新配置
     */
    SiteConfig saveOrUpdateConfig(SiteConfigRequest request);
    
    /**
     * 根据ID更新配置
     */
    SiteConfig updateConfig(Long id, SiteConfigRequest request);
    
    /**
     * 删除配置
     */
    void deleteConfig(Long id);
    
    /**
     * 上传并设置LOGO
     */
    String uploadLogo(MultipartFile file);
    
    /**
     * 上传并设置登录背景
     */
    String uploadLoginBackground(MultipartFile file);
    
    /**
     * 获取公开配置（用于前端展示）
     */
    Map<String, String> getPublicConfigs();
    
    /**
     * 初始化默认配置
     */
    void initDefaultConfigs();
    
    /**
     * 重置为默认配置
     */
    void resetToDefaults();
    
    /**
     * 修复重复的uploads路径
     */
    void fixDuplicatePaths();
}
