package com.example.photography.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 站点配置实体类
 * 用于管理登录界面背景、网站LOGO等全局配置
 */
@Entity
@Table(name = "site_configs")
@Data
@EqualsAndHashCode(callSuper = true)
public class SiteConfig extends BaseEntity {
    
    /**
     * 配置键名（唯一）
     */
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;
    
    /**
     * 配置值
     */
    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;
    
    /**
     * 配置描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 配置类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false)
    private ConfigType configType;
    
    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    /**
     * 排序权重
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    /**
     * 配置类型枚举
     */
    public enum ConfigType {
        /**
         * 文本配置
         */
        TEXT("文本"),
        
        /**
         * 图片文件路径
         */
        IMAGE("图片"),
        
        /**
         * 颜色值
         */
        COLOR("颜色"),
        
        /**
         * 数字
         */
        NUMBER("数字"),
        
        /**
         * 布尔值
         */
        BOOLEAN("布尔值"),
        
        /**
         * JSON配置
         */
        JSON("JSON");
        
        private final String description;
        
        ConfigType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 预定义配置键常量
     */
    public static class Keys {
        /**
         * 网站LOGO
         */
        public static final String SITE_LOGO = "site.logo";
        
        /**
         * 登录页面背景图
         */
        public static final String LOGIN_BACKGROUND = "login.background";
        
        /**
         * 网站标题
         */
        public static final String SITE_TITLE = "site.title";
        
        /**
         * 网站副标题
         */
        public static final String SITE_SUBTITLE = "site.subtitle";
        
        /**
         * 主题色
         */
        public static final String PRIMARY_COLOR = "theme.primary_color";
        
        /**
         * 登录页面标题
         */
        public static final String LOGIN_TITLE = "login.title";
        
        /**
         * 登录页面欢迎语
         */
        public static final String LOGIN_WELCOME = "login.welcome";

        /**
         * 是否启用邮件能力
         */
        public static final String MAIL_ENABLED = "mail.enabled";

        /**
         * SMTP服务器地址
         */
        public static final String MAIL_SMTP_HOST = "mail.smtp_host";

        /**
         * SMTP服务器端口
         */
        public static final String MAIL_SMTP_PORT = "mail.smtp_port";

        /**
         * 是否启用SMTP SSL
         */
        public static final String MAIL_SMTP_SSL_ENABLED = "mail.smtp_ssl_enabled";

        /**
         * QQ邮箱账号
         */
        public static final String MAIL_QQ_ACCOUNT = "mail.qq_account";

        /**
         * QQ邮箱SMTP授权码
         */
        public static final String MAIL_QQ_AUTH_CODE = "mail.qq_auth_code";

        /**
         * 发件人显示名称
         */
        public static final String MAIL_SENDER_NAME = "mail.sender_name";

        /**
         * 提醒提前分钟数
         */
        public static final String MAIL_REMINDER_ADVANCE_MINUTES = "mail.reminder_advance_minutes";

        /**
         * 设备逾期提醒间隔小时数
         */
        public static final String MAIL_OVERDUE_REMINDER_INTERVAL_HOURS = "mail.overdue_reminder_interval_hours";

        /**
         * 执勤提醒开关
         */
        public static final String MAIL_DUTY_REMINDER_ENABLED = "mail.duty_reminder_enabled";

        /**
         * 晚自习打卡提醒开关
         */
        public static final String MAIL_CHECKIN_REMINDER_ENABLED = "mail.checkin_reminder_enabled";

        /**
         * 请假审批提醒开关
         */
        public static final String MAIL_LEAVE_APPROVAL_REMINDER_ENABLED = "mail.leave_approval_reminder_enabled";

        /**
         * 设备逾期归还提醒开关
         */
        public static final String MAIL_BORROW_OVERDUE_REMINDER_ENABLED = "mail.borrow_overdue_reminder_enabled";
    }
}
