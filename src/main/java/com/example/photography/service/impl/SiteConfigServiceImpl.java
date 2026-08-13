package com.example.photography.service.impl;

import com.example.photography.dto.request.SiteConfigRequest;
import com.example.photography.model.entity.SiteConfig;
import com.example.photography.repository.SiteConfigRepository;
import com.example.photography.service.SiteConfigService;
import com.example.photography.service.SensitiveConfigCrypto;
import com.example.photography.util.FileUploadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站点配置Service实现类
 */
@Service
@Slf4j
@Transactional
public class SiteConfigServiceImpl implements SiteConfigService {
    private static final String SENSITIVE_MASK = "******";
    
    @Autowired
    private SiteConfigRepository siteConfigRepository;
    
    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Autowired
    private SensitiveConfigCrypto sensitiveConfigCrypto;
    
    @Value("${app.upload.site-assets-dir:site}")
    private String siteAssetsDir;
    
    @Override
    @Transactional(readOnly = true)
    public List<SiteConfig> getAllConfigs() {
        return siteConfigRepository.findAllConfigs();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<SiteConfig> getAllEnabledConfigs() {
        return siteConfigRepository.findAllEnabledConfigs();
    }
    
    @Override
    @Transactional(readOnly = true)
    public SiteConfig getConfigByKey(String configKey) {
        return siteConfigRepository.findByConfigKeyAndDeletedFalse(configKey).orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public String getConfigValue(String configKey) {
        return getConfigValue(configKey, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public String getConfigValue(String configKey, String defaultValue) {
        SiteConfig config = getConfigByKey(configKey);
        if (config == null || !config.getEnabled()) return defaultValue;
        if (SiteConfig.Keys.MAIL_QQ_AUTH_CODE.equals(configKey)) {
            String decrypted = sensitiveConfigCrypto.decrypt(config.getConfigValue());
            return StringUtils.hasText(decrypted) ? decrypted : defaultValue;
        }
        return config.getConfigValue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getConfigValues(List<String> configKeys) {
        List<SiteConfig> configs = siteConfigRepository.findByConfigKeysAndEnabled(configKeys);
        return configs.stream()
                .collect(Collectors.toMap(
                        SiteConfig::getConfigKey,
                        SiteConfig::getConfigValue,
                        (existing, replacement) -> existing
                ));
    }
    
    @Override
    public SiteConfig saveOrUpdateConfig(SiteConfigRequest request) {
        SiteConfig existingConfig = getConfigByKey(request.getConfigKey());
        
        if (existingConfig != null) {
            // 更新现有配置
            existingConfig.setConfigValue(resolveConfigValueForSave(request, existingConfig));
            existingConfig.setDescription(request.getDescription());
            existingConfig.setConfigType(request.getConfigType());
            existingConfig.setEnabled(request.getEnabled());
            existingConfig.setSortOrder(request.getSortOrder());
            return siteConfigRepository.save(existingConfig);
        } else {
            // 创建新配置
            SiteConfig newConfig = new SiteConfig();
            newConfig.setConfigKey(request.getConfigKey());
            newConfig.setConfigValue(resolveConfigValueForSave(request, null));
            newConfig.setDescription(request.getDescription());
            newConfig.setConfigType(request.getConfigType());
            newConfig.setEnabled(request.getEnabled());
            newConfig.setSortOrder(request.getSortOrder());
            return siteConfigRepository.save(newConfig);
        }
    }
    
    @Override
    public SiteConfig updateConfig(Long id, SiteConfigRequest request) {
        SiteConfig config = siteConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        config.setConfigValue(resolveConfigValueForSave(request, config));
        config.setDescription(request.getDescription());
        config.setConfigType(request.getConfigType());
        config.setEnabled(request.getEnabled());
        config.setSortOrder(request.getSortOrder());
        
        return siteConfigRepository.save(config);
    }
    
    @Override
    public void deleteConfig(Long id) {
        SiteConfig config = siteConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在"));
        
        config.setDeleted(true);
        siteConfigRepository.save(config);
    }
    
    @Override
    public String uploadLogo(MultipartFile file) {
        try {
            String logoPath = fileUploadUtil.uploadFile(file, siteAssetsDir);
            
            // 更新LOGO配置
            SiteConfigRequest request = new SiteConfigRequest();
            request.setConfigKey(SiteConfig.Keys.SITE_LOGO);
            request.setConfigValue(logoPath);
            request.setDescription("网站LOGO");
            request.setConfigType(SiteConfig.ConfigType.IMAGE);
            request.setEnabled(true);
            
            saveOrUpdateConfig(request);
            
            return logoPath;
        } catch (Exception e) {
            log.error("上传LOGO失败", e);
            throw new RuntimeException("上传LOGO失败: " + e.getMessage());
        }
    }
    
    @Override
    public String uploadLoginBackground(MultipartFile file) {
        try {
            String backgroundPath = fileUploadUtil.uploadFile(file, siteAssetsDir);
            
            // 更新登录背景配置
            SiteConfigRequest request = new SiteConfigRequest();
            request.setConfigKey(SiteConfig.Keys.LOGIN_BACKGROUND);
            request.setConfigValue(backgroundPath);
            request.setDescription("登录页面背景图");
            request.setConfigType(SiteConfig.ConfigType.IMAGE);
            request.setEnabled(true);
            
            saveOrUpdateConfig(request);
            
            return backgroundPath;
        } catch (Exception e) {
            log.error("上传登录背景失败", e);
            throw new RuntimeException("上传登录背景失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getPublicConfigs() {
        // 定义公开的配置键
        List<String> publicKeys = Arrays.asList(
                SiteConfig.Keys.SITE_LOGO,
                SiteConfig.Keys.LOGIN_BACKGROUND,
                SiteConfig.Keys.SITE_TITLE,
                SiteConfig.Keys.SITE_SUBTITLE,
                SiteConfig.Keys.PRIMARY_COLOR,
                SiteConfig.Keys.LOGIN_TITLE,
                SiteConfig.Keys.LOGIN_WELCOME
        );
        
        log.debug("=== 获取公开配置 ===");
        log.debug("请求的配置键: {}", publicKeys);
        
        Map<String, String> result = getConfigValues(publicKeys);
        log.debug("返回的配置值: {}", result);
        
        return result;
    }
    
    @Override
    public void initDefaultConfigs() {
        log.info("初始化默认站点配置...");
        
        createDefaultConfigIfNotExists(
                SiteConfig.Keys.SITE_TITLE,
                "融媒体管理系统",
                "网站标题",
                SiteConfig.ConfigType.TEXT
        );
        
        createDefaultConfigIfNotExists(
                SiteConfig.Keys.SITE_SUBTITLE,
                "Photography System",
                "网站副标题",
                SiteConfig.ConfigType.TEXT
        );
        
        createDefaultConfigIfNotExists(
                SiteConfig.Keys.LOGIN_TITLE,
                "融媒体管理系统",
                "登录页面标题",
                SiteConfig.ConfigType.TEXT
        );
        
        createDefaultConfigIfNotExists(
                SiteConfig.Keys.LOGIN_WELCOME,
                "欢迎回来，请登录您的账户",
                "登录页面欢迎语",
                SiteConfig.ConfigType.TEXT
        );
        
        createDefaultConfigIfNotExists(
                SiteConfig.Keys.PRIMARY_COLOR,
                "#409EFF",
                "主题色",
                SiteConfig.ConfigType.COLOR
        );

        createMailDefaultConfigs();
        
        log.info("默认站点配置初始化完成");
    }
    
    @Override
    public void resetToDefaults() {
        log.info("重置为默认配置...");
        
        try {
            // 重置方式1：更新现有配置为默认值，而不是删除重建
            resetConfigToDefault(SiteConfig.Keys.SITE_TITLE, "融媒体管理系统", "网站标题", SiteConfig.ConfigType.TEXT);
            resetConfigToDefault(SiteConfig.Keys.SITE_SUBTITLE, "Photography System", "网站副标题", SiteConfig.ConfigType.TEXT);
            resetConfigToDefault(SiteConfig.Keys.LOGIN_TITLE, "融媒体管理系统", "登录页面标题", SiteConfig.ConfigType.TEXT);
            resetConfigToDefault(SiteConfig.Keys.LOGIN_WELCOME, "欢迎回来，请登录您的账户", "登录页面欢迎语", SiteConfig.ConfigType.TEXT);
            resetConfigToDefault(SiteConfig.Keys.PRIMARY_COLOR, "#409EFF", "主题色", SiteConfig.ConfigType.COLOR);
            resetMailConfigDefaults();
            
            // 删除其他非默认配置（如自定义LOGO和背景）
            List<String> defaultKeys = Arrays.asList(
                SiteConfig.Keys.SITE_TITLE,
                SiteConfig.Keys.SITE_SUBTITLE, 
                SiteConfig.Keys.LOGIN_TITLE,
                SiteConfig.Keys.LOGIN_WELCOME,
                SiteConfig.Keys.PRIMARY_COLOR,
                SiteConfig.Keys.MAIL_ENABLED,
                SiteConfig.Keys.MAIL_SMTP_HOST,
                SiteConfig.Keys.MAIL_SMTP_PORT,
                SiteConfig.Keys.MAIL_SMTP_SSL_ENABLED,
                SiteConfig.Keys.MAIL_QQ_ACCOUNT,
                SiteConfig.Keys.MAIL_QQ_AUTH_CODE,
                SiteConfig.Keys.MAIL_SENDER_NAME,
                SiteConfig.Keys.MAIL_REMINDER_ADVANCE_MINUTES,
                SiteConfig.Keys.MAIL_OVERDUE_REMINDER_INTERVAL_HOURS,
                SiteConfig.Keys.MAIL_LOG_RETENTION_DAYS,
                SiteConfig.Keys.MAIL_DUTY_REMINDER_ENABLED,
                SiteConfig.Keys.MAIL_CHECKIN_REMINDER_ENABLED,
                SiteConfig.Keys.MAIL_LEAVE_APPROVAL_REMINDER_ENABLED,
                SiteConfig.Keys.MAIL_BORROW_OVERDUE_REMINDER_ENABLED,
                SiteConfig.Keys.JOIN_INTERVIEW_QQ_GROUP
            );
            
            List<SiteConfig> allConfigs = getAllConfigs();
            for (SiteConfig config : allConfigs) {
                if (!defaultKeys.contains(config.getConfigKey())) {
                    config.setDeleted(true);
                }
            }
            siteConfigRepository.saveAll(allConfigs);
            
            log.info("配置重置完成");
        } catch (Exception e) {
            log.error("重置配置失败", e);
            throw new RuntimeException("重置失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置单个配置为默认值
     */
    private void resetConfigToDefault(String key, String value, String description, SiteConfig.ConfigType type) {
        SiteConfig existingConfig = getConfigByKey(key);
        if (existingConfig != null) {
            // 更新现有配置
            existingConfig.setConfigValue(value);
            existingConfig.setDescription(description);
            existingConfig.setConfigType(type);
            existingConfig.setEnabled(true);
            existingConfig.setDeleted(false);
            siteConfigRepository.save(existingConfig);
            log.debug("更新配置: {} = {}", key, value);
        } else {
            // 创建新配置
            createDefaultConfigIfNotExists(key, value, description, type);
        }
    }
    
    /**
     * 创建默认配置（如果不存在）
     */
    private void createDefaultConfigIfNotExists(String key, String value, String description, SiteConfig.ConfigType type) {
        if (!siteConfigRepository.existsByConfigKeyAndDeletedFalse(key)) {
            SiteConfig config = new SiteConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setDescription(description);
            config.setConfigType(type);
            config.setEnabled(true);
            config.setSortOrder(0);
            
            siteConfigRepository.save(config);
            log.debug("创建默认配置: {} = {}", key, value);
        }
    }

    private void createMailDefaultConfigs() {
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_ENABLED, "false", "是否启用QQ邮箱验证码与提醒", SiteConfig.ConfigType.BOOLEAN);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_SMTP_HOST, "smtp.qq.com", "QQ邮箱SMTP服务器", SiteConfig.ConfigType.TEXT);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_SMTP_PORT, "465", "QQ邮箱SMTP端口", SiteConfig.ConfigType.NUMBER);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_SMTP_SSL_ENABLED, "true", "是否启用SMTP SSL", SiteConfig.ConfigType.BOOLEAN);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_QQ_ACCOUNT, "", "QQ邮箱账号", SiteConfig.ConfigType.TEXT);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_QQ_AUTH_CODE, "", "QQ邮箱SMTP授权码", SiteConfig.ConfigType.TEXT);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_SENDER_NAME, "融媒体管理系统", "邮件发件人名称", SiteConfig.ConfigType.TEXT);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_REMINDER_ADVANCE_MINUTES, "30", "执勤和晚自习提醒提前分钟数", SiteConfig.ConfigType.NUMBER);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_OVERDUE_REMINDER_INTERVAL_HOURS, "24", "设备逾期归还提醒间隔小时数", SiteConfig.ConfigType.NUMBER);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_LOG_RETENTION_DAYS, "30", "邮件发送日志与验证码记录保留天数", SiteConfig.ConfigType.NUMBER);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_DUTY_REMINDER_ENABLED, "true", "执勤提醒开关", SiteConfig.ConfigType.BOOLEAN);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_CHECKIN_REMINDER_ENABLED, "true", "晚自习打卡提醒开关", SiteConfig.ConfigType.BOOLEAN);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_LEAVE_APPROVAL_REMINDER_ENABLED, "true", "请假审批提醒开关", SiteConfig.ConfigType.BOOLEAN);
        createDefaultConfigIfNotExists(SiteConfig.Keys.MAIL_BORROW_OVERDUE_REMINDER_ENABLED, "true", "设备逾期归还提醒开关", SiteConfig.ConfigType.BOOLEAN);
        createDefaultConfigIfNotExists(SiteConfig.Keys.JOIN_INTERVIEW_QQ_GROUP, "", "入部面试QQ群号", SiteConfig.ConfigType.TEXT);
    }

    private void resetMailConfigDefaults() {
        resetConfigToDefault(SiteConfig.Keys.MAIL_ENABLED, "false", "是否启用QQ邮箱验证码与提醒", SiteConfig.ConfigType.BOOLEAN);
        resetConfigToDefault(SiteConfig.Keys.MAIL_SMTP_HOST, "smtp.qq.com", "QQ邮箱SMTP服务器", SiteConfig.ConfigType.TEXT);
        resetConfigToDefault(SiteConfig.Keys.MAIL_SMTP_PORT, "465", "QQ邮箱SMTP端口", SiteConfig.ConfigType.NUMBER);
        resetConfigToDefault(SiteConfig.Keys.MAIL_SMTP_SSL_ENABLED, "true", "是否启用SMTP SSL", SiteConfig.ConfigType.BOOLEAN);
        resetConfigToDefault(SiteConfig.Keys.MAIL_QQ_ACCOUNT, "", "QQ邮箱账号", SiteConfig.ConfigType.TEXT);
        resetConfigToDefault(SiteConfig.Keys.MAIL_QQ_AUTH_CODE, "", "QQ邮箱SMTP授权码", SiteConfig.ConfigType.TEXT);
        resetConfigToDefault(SiteConfig.Keys.MAIL_SENDER_NAME, "融媒体管理系统", "邮件发件人名称", SiteConfig.ConfigType.TEXT);
        resetConfigToDefault(SiteConfig.Keys.MAIL_REMINDER_ADVANCE_MINUTES, "30", "执勤和晚自习提醒提前分钟数", SiteConfig.ConfigType.NUMBER);
        resetConfigToDefault(SiteConfig.Keys.MAIL_OVERDUE_REMINDER_INTERVAL_HOURS, "24", "设备逾期归还提醒间隔小时数", SiteConfig.ConfigType.NUMBER);
        resetConfigToDefault(SiteConfig.Keys.MAIL_LOG_RETENTION_DAYS, "30", "邮件发送日志与验证码记录保留天数", SiteConfig.ConfigType.NUMBER);
        resetConfigToDefault(SiteConfig.Keys.MAIL_DUTY_REMINDER_ENABLED, "true", "执勤提醒开关", SiteConfig.ConfigType.BOOLEAN);
        resetConfigToDefault(SiteConfig.Keys.MAIL_CHECKIN_REMINDER_ENABLED, "true", "晚自习打卡提醒开关", SiteConfig.ConfigType.BOOLEAN);
        resetConfigToDefault(SiteConfig.Keys.MAIL_LEAVE_APPROVAL_REMINDER_ENABLED, "true", "请假审批提醒开关", SiteConfig.ConfigType.BOOLEAN);
        resetConfigToDefault(SiteConfig.Keys.MAIL_BORROW_OVERDUE_REMINDER_ENABLED, "true", "设备逾期归还提醒开关", SiteConfig.ConfigType.BOOLEAN);
        resetConfigToDefault(SiteConfig.Keys.JOIN_INTERVIEW_QQ_GROUP, "", "入部面试QQ群号", SiteConfig.ConfigType.TEXT);
    }

    private String resolveConfigValueForSave(SiteConfigRequest request, SiteConfig existingConfig) {
        if (SiteConfig.Keys.MAIL_SMTP_HOST.equals(request.getConfigKey())) {
            MailSettingsValidator.validateSmtpHost(request.getConfigValue());
            return MailSettingsValidator.normalizeSmtpHost(request.getConfigValue());
        }

        if (SiteConfig.Keys.MAIL_QQ_ACCOUNT.equals(request.getConfigKey())) {
            String account = request.getConfigValue() == null ? "" : request.getConfigValue().trim();
            if (StringUtils.hasText(account)) {
                MailSettingsValidator.validateQqAccount(account);
            }
            return account;
        }

        if (!SiteConfig.Keys.MAIL_QQ_AUTH_CODE.equals(request.getConfigKey())) {
            return request.getConfigValue();
        }

        String incomingValue = request.getConfigValue();
        if (!StringUtils.hasText(incomingValue) || SENSITIVE_MASK.equals(incomingValue.trim())) {
            return existingConfig != null ? existingConfig.getConfigValue() : "";
        }
        return sensitiveConfigCrypto.encrypt(incomingValue.trim());
    }
    
    @Override
    @Transactional
    public void fixDuplicatePaths() {
        try {
            log.info("开始修复重复的uploads路径...");
            
            // 获取所有图片配置
            List<SiteConfig> imageConfigs = siteConfigRepository.findAllConfigs().stream()
                .filter(config -> config.getConfigType() == SiteConfig.ConfigType.IMAGE)
                .filter(config -> config.getConfigValue() != null && config.getConfigValue().contains("/uploads/uploads/"))
                .collect(Collectors.toList());
            
            log.info("找到 {} 个需要修复的图片配置", imageConfigs.size());
            
            for (SiteConfig config : imageConfigs) {
                String oldPath = config.getConfigValue();
                String newPath = oldPath.replace("/uploads/uploads/", "/uploads/");
                config.setConfigValue(newPath);
                log.info("修复路径: {} -> {}", oldPath, newPath);
            }
            
            if (!imageConfigs.isEmpty()) {
                siteConfigRepository.saveAll(imageConfigs);
                log.info("路径修复完成，共修复 {} 个配置项", imageConfigs.size());
            } else {
                log.info("没有需要修复的路径");
            }
            
        } catch (Exception e) {
            log.error("修复路径失败", e);
            throw new RuntimeException("修复路径失败: " + e.getMessage());
        }
    }
}
