package com.example.photography.service.impl;

import com.example.photography.model.entity.SiteConfig;
import com.example.photography.service.SiteConfigService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 从站点配置读取邮件相关设置。
 */
@Service
public class MailSettingsService {
    @Autowired
    private SiteConfigService siteConfigService;

    public MailSettings getSettings() {
        MailSettings settings = new MailSettings();
        settings.setEnabled(getBoolean(SiteConfig.Keys.MAIL_ENABLED, false));
        settings.setSmtpHost(getString(SiteConfig.Keys.MAIL_SMTP_HOST, "smtp.qq.com"));
        settings.setSmtpPort(getInt(SiteConfig.Keys.MAIL_SMTP_PORT, 465, 1, 65535));
        settings.setSmtpSslEnabled(getBoolean(SiteConfig.Keys.MAIL_SMTP_SSL_ENABLED, true));
        settings.setQqAccount(getString(SiteConfig.Keys.MAIL_QQ_ACCOUNT, ""));
        settings.setQqAuthCode(getString(SiteConfig.Keys.MAIL_QQ_AUTH_CODE, ""));
        settings.setSenderName(getString(SiteConfig.Keys.MAIL_SENDER_NAME, "融媒体管理系统"));
        return settings;
    }

    public MailSettings getRequiredSettings() {
        MailSettings settings = getSettings();
        if (!settings.isEnabled()) {
            throw new RuntimeException("邮件功能未启用，请先在站点设置中开启QQ邮箱与提醒");
        }
        if (!hasRequiredFields(settings)) {
            throw new RuntimeException("QQ邮箱SMTP配置不完整，请在站点设置中填写邮箱账号和授权码");
        }
        MailSettingsValidator.validateSmtpHost(settings.getSmtpHost());
        MailSettingsValidator.validateQqAccount(settings.getQqAccount());
        return settings;
    }

    public boolean isMailEnabled() {
        return getSettings().isEnabled();
    }

    public boolean isMailConfigured() {
        return isConfigured(getSettings());
    }

    public boolean isFeatureEnabled(String configKey) {
        return getBoolean(configKey, true);
    }

    public int getReminderAdvanceMinutes() {
        return getInt(SiteConfig.Keys.MAIL_REMINDER_ADVANCE_MINUTES, 30, 0, 1440);
    }

    public int getOverdueReminderIntervalHours() {
        return getInt(SiteConfig.Keys.MAIL_OVERDUE_REMINDER_INTERVAL_HOURS, 24, 1, 24 * 30);
    }

    public int getLogRetentionDays() {
        return getInt(SiteConfig.Keys.MAIL_LOG_RETENTION_DAYS, 30, 1, 3650);
    }

    private boolean isConfigured(MailSettings settings) {
        if (!hasRequiredFields(settings)) {
            return false;
        }

        try {
            MailSettingsValidator.validateSmtpHost(settings.getSmtpHost());
            MailSettingsValidator.validateQqAccount(settings.getQqAccount());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasRequiredFields(MailSettings settings) {
        return StringUtils.hasText(settings.getSmtpHost())
                && StringUtils.hasText(settings.getQqAccount())
                && StringUtils.hasText(settings.getQqAuthCode());
    }

    private String getString(String key, String defaultValue) {
        String value = siteConfigService.getConfigValue(key, defaultValue);
        return value == null ? defaultValue : value.trim();
    }

    private boolean getBoolean(String key, boolean defaultValue) {
        String value = siteConfigService.getConfigValue(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    private int getInt(String key, int defaultValue, int min, int max) {
        String value = siteConfigService.getConfigValue(key, String.valueOf(defaultValue));
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < min || parsed > max) {
                return defaultValue;
            }
            return parsed;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Getter
    public static class MailSettings {
        private boolean enabled;
        private String smtpHost;
        private int smtpPort;
        private boolean smtpSslEnabled;
        private String qqAccount;
        private String qqAuthCode;
        private String senderName;

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setSmtpHost(String smtpHost) {
            this.smtpHost = smtpHost;
        }

        public void setSmtpPort(int smtpPort) {
            this.smtpPort = smtpPort;
        }

        public void setSmtpSslEnabled(boolean smtpSslEnabled) {
            this.smtpSslEnabled = smtpSslEnabled;
        }

        public void setQqAccount(String qqAccount) {
            this.qqAccount = qqAccount;
        }

        public void setQqAuthCode(String qqAuthCode) {
            this.qqAuthCode = qqAuthCode;
        }

        public void setSenderName(String senderName) {
            this.senderName = senderName;
        }
    }
}
